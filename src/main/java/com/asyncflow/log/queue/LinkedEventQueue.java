package com.asyncflow.log.queue;

import com.asyncflow.log.model.event.LogEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 基于LinkedBlockingQueue实现的事件队列
 */
@Slf4j
public class LinkedEventQueue implements EventQueue {
    
    /**
     * 默认队列容量
     */
    private static final int DEFAULT_CAPACITY = 10000;
    
    /**
     * 内部队列
     */
    private final LinkedBlockingQueue<LogEvent> queue;
    
    /**
     * 队列容量
     */
    private final int capacity;
    
    /**
     * 默认构造函数，使用默认容量
     */
    public LinkedEventQueue() {
        this(DEFAULT_CAPACITY);
    }
    
    /**
     * 带容量的构造函数
     * @param capacity 队列容量
     */
    public LinkedEventQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedBlockingQueue<>(capacity);
        log.info("创建LinkedEventQueue，容量为: {}", capacity);
    }
    
    @Override
    public boolean offer(LogEvent event) throws InterruptedException {
        if (event == null) {
            log.warn("尝试添加null事件到队列");
            return false;
        }
        return queue.offer(event);
    }
    
    @Override
    public void put(LogEvent event) throws InterruptedException {
        if (event == null) {
            log.warn("尝试添加null事件到队列");
            return;
        }
        queue.put(event);
    }
    
    @Override
    public boolean offer(LogEvent event, long timeout) throws InterruptedException {
        if (event == null) {
            log.warn("尝试添加null事件到队列");
            return false;
        }
        return queue.offer(event, timeout, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public LogEvent poll() {
        return queue.poll();
    }
    
    @Override
    public LogEvent take() throws InterruptedException {
        return queue.take();
    }
    
    @Override
    public LogEvent poll(long timeout) throws InterruptedException {
        return queue.poll(timeout, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public int size() {
        return queue.size();
    }
    
    @Override
    public int capacity() {
        return capacity;
    }
    
    @Override
    public boolean isFull() {
        return queue.size() == capacity;
    }
    
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    @Override
    public void clear() {
        queue.clear();
    }
    
    @Override
    public double getUsage() {
        return (double) queue.size() / capacity;
    }
} 