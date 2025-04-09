package com.asyncflow.log.config;

import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.queue.QueueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 队列配置类
 * 用于提供队列实例和相关配置
 */
@Slf4j
@Configuration
public class QueueConfig {
    
    @Autowired
    private QueueFactory queueFactory;
    
    /**
     * 创建并注册事件队列实例
     * 
     * @return 事件队列实例
     */
    @Bean
    public EventQueue eventQueue() {
        log.info("初始化事件队列");
        return queueFactory.createQueue();
    }
} 