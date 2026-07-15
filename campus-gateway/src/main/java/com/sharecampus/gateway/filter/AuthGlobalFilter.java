package com.sharecampus.gateway.filter;

import com.sharecampus.common.security.JwtTokenProvider;
import com.sharecampus.common.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 全局鉴权过滤器
 * <p>
 * 1. 白名单路径直接放行
 * 2. 非白名单路径必须携带有效 JWT Token
 * 3. 解析 Token 后将用户信息注入 Header 透传给下游
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> whiteUrls = java.util.List.of(
        "/api/v1/auth/login", "/api/v1/auth/login/sms", "/api/v1/auth/register",
        "/api/v1/auth/refresh", "/api/v1/auth/sms/send", "/api/v1/product/**",
        "/doc.html", "/v3/api-docs/**", "/ws/**", "/actuator/health"
    );

    public AuthGlobalFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. 白名单放行
        if (isWhiteUrl(path)) {
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "缺少 Token");
        }
        String token = authHeader.substring(7);

        // 3. 校验 Token
        if (!jwtTokenProvider.validateToken(token)) {
            return unauthorized(exchange, "Token 无效或已过期");
        }

        // 4. 解析用户信息，注入 Header 透传给下游
        try {
            UserContext context = jwtTokenProvider.parseToken(token);
            log.info("JWT解析成功: userId={}, tenantId={}", context.getUserId(), context.getTenantId());
            exchange = exchange.mutate()
                    .request(r -> r
                            .header("X-Gateway-Key", "campus-cloud-gateway-2026")
                            .header("X-User-Id", String.valueOf(context.getUserId()))
                            .header("X-Tenant-Id", String.valueOf(context.getTenantId()))
                            .header("X-User-Type", context.getUserType() != null ? context.getUserType() : "")
                            .header("X-Roles", context.getRoles() != null ? String.join(",", context.getRoles()) : "")
                    ).build();
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            return unauthorized(exchange, "Token 格式错误");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100; // 最早执行
    }

    // ==================== 内部方法 ====================

    private boolean isWhiteUrl(String path) {
        if (whiteUrls == null) return false;
        for (String pattern : whiteUrls) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.warn("鉴权拒绝: path={}, reason={}", exchange.getRequest().getURI().getPath(), message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}";
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes())));
    }
}
