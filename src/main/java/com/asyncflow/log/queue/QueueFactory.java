package com.asyncflow.log.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 队列工厂类
 * 用于创建不同类型的事件队列
 */
@Slf4j
@Component
public class QueueFactory {
    
    /**
     * 队列类型
     */
    @Value("${async.log.queue.type:linked}")
    private String queueType;
    
    /**
     * 队列容量
     */
    @Value("${async.log.queue.capacity:10000}")
    private int queueCapacity;
    
    /**
     * 创建默认队列
     * 根据配置创建指定类型的队列
     * 
     * @return 事件队列
     */
    public EventQueue createQueue() {
        log.info("创建队列，类型: {}, 容量: {}", queueType, queueCapacity);
        
        if ("linked".equalsIgnoreCase(queueType)) {
            return createLinkedQueue(queueCapacity);
        }
        
        // 默认使用LinkedEventQueue
        log.warn("未知的队列类型: {}, 使用默认的LinkedEventQueue", queueType);
        return createLinkedQueue(queueCapacity);
    }
    
    /**
     * 创建指定容量的LinkedEventQueue
     * 
     * @param capacity 队列容量
     * @return LinkedEventQueue实例
     */
    public LinkedEventQueue createLinkedQueue(int capacity) {
        log.info("创建LinkedEventQueue，容量: {}", capacity);
        return new LinkedEventQueue(capacity);
    }
    
    /**
     * 创建具有默认容量的LinkedEventQueue
     * 
     * @return LinkedEventQueue实例
     */
    public LinkedEventQueue createLinkedQueue() {
        return createLinkedQueue(queueCapacity);
    }
    
    /**
     * 获取配置的队列类型
     * 
     * @return 队列类型
     */
    public String getQueueType() {
        return queueType;
    }
    
    /**
     * 获取配置的队列容量
     * 
     * @return 队列容量
     */
    public int getQueueCapacity() {
        return queueCapacity;
    }
} 