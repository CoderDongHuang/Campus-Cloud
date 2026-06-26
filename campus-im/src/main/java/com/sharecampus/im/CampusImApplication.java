package com.sharecampus.im;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CampusImApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusImApplication.class, args);
    }
}
