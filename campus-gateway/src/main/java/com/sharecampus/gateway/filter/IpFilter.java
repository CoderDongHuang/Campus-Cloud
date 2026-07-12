package com.sharecampus.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * IP 黑白名单过滤器
 * <p>
 * 配置 campus.security.ip-whitelist 和 campus.security.ip-blacklist。
 * 白名单优先：如果设置了白名单，只有白名单内 IP 可访问；
 * 否则只拦截黑名单 IP。
 */
@Slf4j
@Component
public class IpFilter implements GlobalFilter, Ordered {

    @Value("${campus.security.ip-whitelist:}") private List<String> whitelist;
    @Value("${campus.security.ip-blacklist:}") private List<String> blacklist;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        String path = exchange.getRequest().getURI().getPath();

        // 白名单优先
        if (whitelist != null && !whitelist.isEmpty()) {
            if (!whitelist.contains(ip)) {
                log.warn("IP不在白名单: ip={}, path={}", ip, path);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        // 黑名单检查
        if (blacklist != null && blacklist.contains(ip)) {
            log.warn("IP在黑名单: ip={}, path={}", ip, path);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200; // 在AuthGlobalFilter之前执行
    }
}
