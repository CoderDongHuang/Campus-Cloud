package com.sharecampus.notify;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.sharecampus.notify.mapper")
public class CampusNotifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusNotifyApplication.class, args);
    }
}
