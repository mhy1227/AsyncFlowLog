package com.asyncflow.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "async.log")
@Data
@Validated
public class AsyncLogConfig {

    @NotNull
    private QueueConfig queue;
    
    @NotNull
    private ConsumerConfig consumer;
    
    @NotNull
    private AppenderConfig appender;
    
    @Data
    public static class QueueConfig {
        private String type = "linked";
        
        @Min(1000)
        private int capacity = 10000;
    }
    
    @Data
    public static class ConsumerConfig {
        @Min(1)
        private int coreSize = 2;
        
        @Min(1)
        private int maxSize = 4;
        
        @Min(10)
        private int keepAlive = 60;
    }
    
    @Data
    public static class AppenderConfig {
        @NotBlank
        private String type = "file";
        
        @NotBlank
        private String filePath = "logs/async";
        
        @Min(1)
        private int batchSize = 100;
        
        @Min(100)
        private int flushInterval = 1000;
    }
}