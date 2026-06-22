package com.sharecampus.tenant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.sharecampus.tenant.mapper")
public class CampusTenantApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusTenantApplication.class, args);
    }
}
