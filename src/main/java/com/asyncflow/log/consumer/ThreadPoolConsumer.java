package com.asyncflow.log.consumer;

import com.asyncflow.log.model.event.LogEvent;
import com.asyncflow.log.queue.EventQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于ThreadPoolExecutor的消费者线程池实现
 */
@Slf4j
public class ThreadPoolConsumer implements ConsumerPool {
    
    /**
     * 默认核心线程数
     */
    private static final int DEFAULT_CORE_SIZE = 2;
    
    /**
     * 默认最大线程数
     */
    private static final int DEFAULT_MAX_SIZE = 4;
    
    /**
     * 默认线程存活时间（秒）
     */
    private static final long DEFAULT_KEEP_ALIVE = 60L;
    
    /**
     * 线程池
     */
    private final ThreadPoolExecutor executor;
    
    /**
     * 事件队列
     */
    private EventQueue eventQueue;
    
    /**
     * 事件处理器
     */
    private EventHandler eventHandler;
    
    /**
     * 消费者线程运行标志
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    /**
     * 消费者任务
     */
    private final ConsumerTask consumerTask = new ConsumerTask();
    
    /**
     * 默认构造函数
     */
    public ThreadPoolConsumer() {
        this(DEFAULT_CORE_SIZE, DEFAULT_MAX_SIZE, DEFAULT_KEEP_ALIVE);
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param keepAlive 线程存活时间（秒）
     */
    public ThreadPoolConsumer(int coreSize, int maxSize, long keepAlive) {
        this.executor = new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAlive, 
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private int count = 0;
                    
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("log-consumer-" + count++);
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        log.info("创建ThreadPoolConsumer，核心线程数: {}, 最大线程数: {}, 存活时间: {}秒", 
                coreSize, maxSize, keepAlive);
    }
    
    @Override
    public void start() {
        if (eventQueue == null) {
            throw new IllegalStateException("事件队列未设置");
        }
        
        if (eventHandler == null) {
            throw new IllegalStateException("事件处理器未设置");
        }
        
        if (running.compareAndSet(false, true)) {
            log.info("启动消费者线程池");
            
            // 初始化事件处理器
            eventHandler.initialize();
            
            // 提交消费者任务
            for (int i = 0; i < executor.getCorePoolSize(); i++) {
                executor.submit(consumerTask);
            }
        } else {
            log.warn("消费者线程池已经启动");
        }
    }
    
    @Override
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            log.info("关闭消费者线程池");
            executor.shutdown();
            
            // 关闭事件处理器
            if (eventHandler != null) {
                try {
                    eventHandler.close();
                } catch (Exception e) {
                    log.error("关闭事件处理器异常", e);
                }
            }
        }
    }
    
    @Override
    public void shutdownNow() {
        if (running.compareAndSet(true, false)) {
            log.info("立即关闭消费者线程池");
            executor.shutdownNow();
            
            // 关闭事件处理器
            if (eventHandler != null) {
                try {
                    eventHandler.close();
                } catch (Exception e) {
                    log.error("关闭事件处理器异常", e);
                }
            }
        }
    }
    
    @Override
    public boolean submit(LogEvent event) {
        if (!running.get()) {
            log.warn("消费者线程池已关闭，无法提交任务");
            return false;
        }
        
        try {
            executor.submit(() -> {
                try {
                    eventHandler.handle(event);
                } catch (Exception e) {
                    log.error("处理日志事件异常", e);
                    eventHandler.handleException(event, e);
                }
            });
            return true;
        } catch (RejectedExecutionException e) {
            log.error("提交任务被拒绝", e);
            return false;
        }
    }
    
    @Override
    public void setEventQueue(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }
    
    @Override
    public EventQueue getEventQueue() {
        return eventQueue;
    }
    
    @Override
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
    
    @Override
    public EventHandler getEventHandler() {
        return eventHandler;
    }
    
    @Override
    public int getActiveCount() {
        return executor.getActiveCount();
    }
    
    @Override
    public int getQueueSize() {
        return executor.getQueue().size();
    }
    
    @Override
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }
    
    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }
    
    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }
    
    /**
     * 消费者任务
     * 不断从队列中获取日志事件并处理
     */
    private class ConsumerTask implements Runnable {
        @Override
        public void run() {
            log.info("消费者线程启动: {}", Thread.currentThread().getName());
            
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    // 从队列中获取日志事件
                    LogEvent event = eventQueue.take();
                    
                    // 处理日志事件
                    try {
                        eventHandler.handle(event);
                    } catch (Exception e) {
                        log.error("处理日志事件异常", e);
                        eventHandler.handleException(event, e);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("消费者线程被中断: {}", Thread.currentThread().getName());
                } catch (Exception e) {
                    log.error("消费者线程异常: {}", Thread.currentThread().getName(), e);
                }
            }
            
            log.info("消费者线程退出: {}", Thread.currentThread().getName());
        }
    }
} 