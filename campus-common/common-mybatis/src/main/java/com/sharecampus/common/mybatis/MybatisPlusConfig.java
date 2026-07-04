package com.sharecampus.common.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 全局配置
 * <p>
 * 注册分页插件和租户隔离拦截器，所有引入 common-mybatis 的服务自动生效。
 */
// 联调期间禁用自定义 MyBatis-Plus 拦截器（会导致 StackOverflow，待排查）
// @Configuration
public class MybatisPlusConfig {
    // @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
