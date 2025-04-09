package com.asyncflow.log.consumer;

import com.asyncflow.log.model.event.LogEvent;
import com.asyncflow.log.queue.EventQueue;

/**
 * 消费者线程池接口
 * 定义消费者线程池的基本操作
 */
public interface ConsumerPool {
    
    /**
     * 启动消费者线程池
     */
    void start();
    
    /**
     * 关闭消费者线程池
     */
    void shutdown();
    
    /**
     * 立即关闭消费者线程池
     */
    void shutdownNow();
    
    /**
     * 提交日志事件处理任务
     * @param event 日志事件
     * @return 是否提交成功
     */
    boolean submit(LogEvent event);
    
    /**
     * 设置事件队列
     * @param eventQueue 事件队列
     */
    void setEventQueue(EventQueue eventQueue);
    
    /**
     * 获取事件队列
     * @return 事件队列
     */
    EventQueue getEventQueue();
    
    /**
     * 设置日志事件处理器
     * @param handler 日志事件处理器
     */
    void setEventHandler(EventHandler handler);
    
    /**
     * 获取日志事件处理器
     * @return 日志事件处理器
     */
    EventHandler getEventHandler();
    
    /**
     * 获取活跃线程数
     * @return 活跃线程数
     */
    int getActiveCount();
    
    /**
     * 获取队列大小
     * @return 队列大小
     */
    int getQueueSize();
    
    /**
     * 获取已完成任务数
     * @return 已完成任务数
     */
    long getCompletedTaskCount();
    
    /**
     * 检查线程池是否已关闭
     * @return 是否已关闭
     */
    boolean isShutdown();
    
    /**
     * 检查线程池是否已终止
     * @return 是否已终止
     */
    boolean isTerminated();
} 