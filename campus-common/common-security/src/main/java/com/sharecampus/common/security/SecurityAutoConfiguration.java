package com.sharecampus.common.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 安全模块自动配置
 * <p>
 * 其他服务引入 common-security 依赖后自动生效（spring.factories + @Configuration）
 */
@Configuration
@org.springframework.context.annotation.ComponentScan(basePackageClasses = SecurityAutoConfiguration.class)
public class SecurityAutoConfiguration {

    /** 自动注册 UserContextFilter，优先级最高 */
    @Bean
    public FilterRegistrationBean<UserContextFilter> userContextFilterRegistration() {
        FilterRegistrationBean<UserContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserContextFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE); // 最早执行
        return registration;
    }

    /** 自动注册 GatewayAuthFilter，确保请求来自 Gateway */
    @Bean
    public FilterRegistrationBean<GatewayAuthFilter> gatewayAuthFilterRegistration() {
        FilterRegistrationBean<GatewayAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GatewayAuthFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE + 1); // 仅次于 UserContextFilter
        return registration;
    }
}
