package com.sharecampus.common.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 全局配置
 * <p>
 * 注册分页插件。租户隔离拦截器待分页验证通过后再启用（Task 1-3）。
 * <p>
 * 注：之前 StackOverflow 的根因是 UserContext 的 Lombok @Data，
 * 该问题已在调试阶段修复（UserContext 改为手写 getter/setter）。
 * 因此 @Configuration 可安全恢复。
 */
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 租户隔离拦截器
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantInterceptor()));
        return interceptor;
    }
}
