package com.sharecampus.review;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication @EnableDiscoveryClient
@MapperScan("com.sharecampus.review.mapper")
public class CampusReviewApplication { public static void main(String[] args) { SpringApplication.run(CampusReviewApplication.class, args); } }
