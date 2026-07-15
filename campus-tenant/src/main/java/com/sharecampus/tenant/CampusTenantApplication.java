package com.sharecampus.tenant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.sharecampus.tenant.mapper")
public class CampusTenantApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusTenantApplication.class, args);
    }
}
