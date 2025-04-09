package com.asyncflow.log.queue;

import com.asyncflow.log.model.event.LogEvent;

/**
 * 事件队列接口
 * 定义日志事件队列的基本操作
 */
public interface EventQueue {
    
    /**
     * 添加日志事件到队列
     * @param event 日志事件
     * @return 是否添加成功
     * @throws InterruptedException 如果在等待过程中被中断
     */
    boolean offer(LogEvent event) throws InterruptedException;
    
    /**
     * 添加日志事件到队列，如果队列已满则阻塞等待
     * @param event 日志事件
     * @throws InterruptedException 如果在等待过程中被中断
     */
    void put(LogEvent event) throws InterruptedException;
    
    /**
     * 添加日志事件到队列，最多等待指定的时间
     * @param event 日志事件
     * @param timeout 等待的最长时间（毫秒）
     * @return 是否添加成功
     * @throws InterruptedException 如果在等待过程中被中断
     */
    boolean offer(LogEvent event, long timeout) throws InterruptedException;
    
    /**
     * 从队列中获取日志事件，如果队列为空则返回null
     * @return 日志事件，如果队列为空则返回null
     */
    LogEvent poll();
    
    /**
     * 从队列中获取日志事件，如果队列为空则阻塞等待
     * @return 日志事件
     * @throws InterruptedException 如果在等待过程中被中断
     */
    LogEvent take() throws InterruptedException;
    
    /**
     * 从队列中获取日志事件，最多等待指定的时间
     * @param timeout 等待的最长时间（毫秒）
     * @return 日志事件，如果超时则返回null
     * @throws InterruptedException 如果在等待过程中被中断
     */
    LogEvent poll(long timeout) throws InterruptedException;
    
    /**
     * 获取队列中的事件数量
     * @return 事件数量
     */
    int size();
    
    /**
     * 获取队列的容量
     * @return 队列容量
     */
    int capacity();
    
    /**
     * 队列是否已满
     * @return 如果队列已满返回true，否则返回false
     */
    boolean isFull();
    
    /**
     * 队列是否为空
     * @return 如果队列为空返回true，否则返回false
     */
    boolean isEmpty();
    
    /**
     * 清空队列
     */
    void clear();
    
    /**
     * 获取队列的使用率
     * @return 使用率（0.0-1.0）
     */
    double getUsage();
} 