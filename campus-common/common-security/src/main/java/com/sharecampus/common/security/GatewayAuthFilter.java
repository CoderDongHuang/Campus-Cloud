package com.sharecampus.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;

/**
 * Gateway 鉴权过滤器
 * <p>
 * 确保下游服务只接受来自 Gateway 的请求。
 * Gateway 在转发时会注入 X-Gateway-Key 头，直接访问下游服务的请求会被拒绝。
 * <p>
 * 所有引入 common-security 的服务自动生效（通过 SecurityAutoConfiguration 注册）。
 */
@Slf4j
public class GatewayAuthFilter implements Filter {

    /** Gateway 与下游服务之间的共享密钥 */
    static final String GATEWAY_KEY_HEADER = "X-Gateway-Key";
    static final String GATEWAY_KEY_VALUE = "campus-cloud-gateway-2026";

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** 下游服务可公开访问的路径（不校验 Gateway Key） */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/**",           // 登录/注册
            "/api/v1/product/**",         // 商品浏览
            "/doc.html", "/v3/api-docs/**", "/webjars/**", "/swagger*/**",  // API文档
            "/actuator/health", "/actuator/info"  // 健康检查
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        // 公开路径无需网关密钥
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 非公开路径必须带网关密钥
        String gatewayKey = httpRequest.getHeader(GATEWAY_KEY_HEADER);
        if (!GATEWAY_KEY_VALUE.equals(gatewayKey)) {
            log.warn("拒绝非Gateway请求: path={}, ip={}", path, httpRequest.getRemoteAddr());
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(
                "{\"code\":401,\"message\":\"请通过 Gateway 访问\",\"data\":null}"
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        for (String pattern : PUBLIC_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
