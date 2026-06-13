package com.sharecampus.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 校享云 — API 网关
 * <p>
 * 职责：路由转发、统一鉴权、Sentinel 限流、跨域处理
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CampusGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusGatewayApplication.class, args);
    }
}
