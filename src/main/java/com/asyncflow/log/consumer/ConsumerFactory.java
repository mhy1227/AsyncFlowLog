package com.asyncflow.log.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 消费者工厂类
 * 用于创建消费者线程池
 */
@Slf4j
@Component
public class ConsumerFactory {
    
    /**
     * 核心线程数
     */
    @Value("${async.log.consumer.core-size:2}")
    private int coreSize;
    
    /**
     * 最大线程数
     */
    @Value("${async.log.consumer.max-size:4}")
    private int maxSize;
    
    /**
     * 线程存活时间（秒）
     */
    @Value("${async.log.consumer.keep-alive:60}")
    private long keepAlive;
    
    /**
     * 创建默认消费者线程池
     * 
     * @return 消费者线程池
     */
    public ConsumerPool createConsumerPool() {
        log.info("创建消费者线程池，核心线程数: {}, 最大线程数: {}, 存活时间: {}秒", 
                coreSize, maxSize, keepAlive);
        return new ThreadPoolConsumer(coreSize, maxSize, keepAlive);
    }
    
    /**
     * 创建指定参数的消费者线程池
     * 
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param keepAlive 线程存活时间（秒）
     * @return 消费者线程池
     */
    public ConsumerPool createConsumerPool(int coreSize, int maxSize, long keepAlive) {
        log.info("创建消费者线程池，核心线程数: {}, 最大线程数: {}, 存活时间: {}秒", 
                coreSize, maxSize, keepAlive);
        return new ThreadPoolConsumer(coreSize, maxSize, keepAlive);
    }
    
    /**
     * 获取配置的核心线程数
     * 
     * @return 核心线程数
     */
    public int getCoreSize() {
        return coreSize;
    }
    
    /**
     * 获取配置的最大线程数
     * 
     * @return 最大线程数
     */
    public int getMaxSize() {
        return maxSize;
    }
    
    /**
     * 获取配置的线程存活时间
     * 
     * @return 线程存活时间（秒）
     */
    public long getKeepAlive() {
        return keepAlive;
    }
} 