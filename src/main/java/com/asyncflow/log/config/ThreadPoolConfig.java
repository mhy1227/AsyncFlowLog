package com.asyncflow.log.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {
    
    @Value("${async.log.consumer.core-size:2}")
    private int corePoolSize;
    
    @Value("${async.log.consumer.max-size:4}")
    private int maxPoolSize;
    
    @Value("${async.log.consumer.keep-alive:60}")
    private int keepAliveSeconds;
    
    @Value("${async.log.queue.capacity:10000}")
    private int queueCapacity;
    
    @Bean("logThreadPool")
    public ThreadPoolTaskExecutor logThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("async-log-");
        
        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}