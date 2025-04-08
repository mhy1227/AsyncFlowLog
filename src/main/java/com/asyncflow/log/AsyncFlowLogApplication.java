package com.asyncflow.log;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.asyncflow.log.mapper")
@EnableConfigurationProperties
public class AsyncFlowLogApplication {
    public static void main(String[] args) {
        SpringApplication.run(AsyncFlowLogApplication.class, args);
    }
} 