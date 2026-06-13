package com.sharecampus.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 用户上下文过滤器
 * <p>
 * 从 Gateway 透传的 Header 中解析用户信息，写入 ThreadLocal。
 * 每个下游服务启动时自动注册此 Filter。
 */
@Slf4j
public class UserContextFilter implements Filter {

    /** Gateway 鉴权后注入的 Header */
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_USER_TYPE = "X-User-Type";
    private static final String HEADER_ROLES = "X-Roles";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            String userId = httpRequest.getHeader(HEADER_USER_ID);
            if (userId != null) {
                UserContext context = UserContext.builder()
                        .userId(Long.valueOf(userId))
                        .tenantId(parseLong(httpRequest.getHeader(HEADER_TENANT_ID)))
                        .userType(httpRequest.getHeader(HEADER_USER_TYPE))
                        .roles(parseList(httpRequest.getHeader(HEADER_ROLES)))
                        .build();
                UserContext.set(context);
            }
            chain.doFilter(request, response);
        } finally {
            UserContext.clear(); // 请求结束，清除 ThreadLocal 防止内存泄漏
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return Long.valueOf(value); } catch (NumberFormatException e) { return null; }
    }

    private java.util.List<String> parseList(String value) {
        if (value == null || value.isEmpty()) return java.util.Collections.emptyList();
        return java.util.Arrays.asList(value.split(","));
    }
}
