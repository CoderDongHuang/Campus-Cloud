package com.sharecampus.common.mq;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * MQ 模块自动配置
 * <p>
 * 扫描当前包下的 Component，避免下游服务手动指定 scanBasePackages
 */
@Configuration
@ComponentScan(basePackageClasses = MqAutoConfiguration.class)
public class MqAutoConfiguration {
}
