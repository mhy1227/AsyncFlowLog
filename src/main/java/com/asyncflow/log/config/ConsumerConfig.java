package com.asyncflow.log.config;

import com.asyncflow.log.consumer.ConsumerFactory;
import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.queue.EventQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消费者线程池配置类
 * 用于提供消费者线程池实例和相关配置
 */
@Slf4j
@Configuration
public class ConsumerConfig {
    
    @Autowired
    private ConsumerFactory consumerFactory;
    
    @Autowired
    private EventQueue eventQueue;
    
    /**
     * 创建并注册消费者线程池实例
     * 
     * @return 消费者线程池实例
     */
    @Bean(destroyMethod = "shutdown")
    public ConsumerPool consumerPool() {
        log.info("初始化消费者线程池");
        ConsumerPool consumerPool = consumerFactory.createConsumerPool();
        
        // 设置事件队列
        consumerPool.setEventQueue(eventQueue);
        
        return consumerPool;
    }
} 