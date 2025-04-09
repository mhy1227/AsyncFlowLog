package com.asyncflow.log.service.impl;

import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.model.event.LogEvent;
import com.asyncflow.log.model.event.LogEventFactory;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.service.AsyncLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步日志服务实现类
 * 整合日志事件工厂、事件队列和消费者线程池，提供完整的异步日志功能
 */
@Slf4j
@Service
public class AsyncLogServiceImpl implements AsyncLogService {
    
    /**
     * 日志事件工厂
     */
    @Autowired
    private LogEventFactory eventFactory;
    
    /**
     * 事件队列
     */
    @Autowired
    private EventQueue eventQueue;
    
    /**
     * 消费者线程池
     */
    @Autowired
    private ConsumerPool consumerPool;
    
    /**
     * 运行状态
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    /**
     * 初始化服务
     */
    @PostConstruct
    public void init() {
        start();
    }
    
    /**
     * 销毁服务
     */
    @PreDestroy
    public void destroy() {
        shutdown();
    }
    
    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            log.info("启动异步日志服务");
            consumerPool.setEventQueue(eventQueue);
            consumerPool.start();
        }
    }
    
    @Override
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            log.info("关闭异步日志服务");
            consumerPool.shutdown();
        }
    }
    
    @Override
    public void flush() {
        log.debug("刷新异步日志服务");
        // 这里不需要额外操作，因为消费者线程池已经在持续处理队列中的日志事件
    }
    
    @Override
    public boolean error(String message) {
        return log("ERROR", message);
    }
    
    @Override
    public boolean error(String message, String exception) {
        return log("ERROR", message, exception);
    }
    
    @Override
    public boolean error(String message, Map<String, String> context) {
        return log("ERROR", message, context);
    }
    
    @Override
    public boolean error(String message, String exception, Map<String, String> context) {
        return log("ERROR", message, exception, context);
    }
    
    @Override
    public boolean warn(String message) {
        return log("WARN", message);
    }
    
    @Override
    public boolean warn(String message, Map<String, String> context) {
        return log("WARN", message, context);
    }
    
    @Override
    public boolean info(String message) {
        return log("INFO", message);
    }
    
    @Override
    public boolean info(String message, Map<String, String> context) {
        return log("INFO", message, context);
    }
    
    @Override
    public boolean debug(String message) {
        return log("DEBUG", message);
    }
    
    @Override
    public boolean debug(String message, Map<String, String> context) {
        return log("DEBUG", message, context);
    }
    
    @Override
    public boolean log(String level, String message) {
        if (!running.get()) {
            log.warn("异步日志服务未启动");
            return false;
        }
        
        try {
            LogEvent event = eventFactory.createLogEvent(level, message);
            return submitEvent(event);
        } catch (Exception e) {
            log.error("创建日志事件异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean log(String level, String message, Map<String, String> context) {
        if (!running.get()) {
            log.warn("异步日志服务未启动");
            return false;
        }
        
        try {
            LogEvent event = eventFactory.createLogEvent(level, message, context);
            return submitEvent(event);
        } catch (Exception e) {
            log.error("创建日志事件异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean log(String level, String message, String exception) {
        if (!running.get()) {
            log.warn("异步日志服务未启动");
            return false;
        }
        
        try {
            LogEvent event = eventFactory.createLogEventWithException(level, message, exception);
            return submitEvent(event);
        } catch (Exception e) {
            log.error("创建日志事件异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean log(String level, String message, String exception, Map<String, String> context) {
        if (!running.get()) {
            log.warn("异步日志服务未启动");
            return false;
        }
        
        try {
            // 创建完整日志事件
            LogEvent event = eventFactory.createFullLogEvent(level, message, context, null, null, exception);
            return submitEvent(event);
        } catch (Exception e) {
            log.error("创建日志事件异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean log(String level, String message, String className, String methodName) {
        if (!running.get()) {
            log.warn("异步日志服务未启动");
            return false;
        }
        
        try {
            LogEvent event = eventFactory.createLogEventWithLocation(level, message, className, methodName);
            return submitEvent(event);
        } catch (Exception e) {
            log.error("创建日志事件异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean log(String level, String message, String className, String methodName, Map<String, String> context) {
        if (!running.get()) {
            log.warn("异步日志服务未启动");
            return false;
        }
        
        try {
            // 创建完整日志事件
            LogEvent event = eventFactory.createFullLogEvent(level, message, context, className, methodName, null);
            return submitEvent(event);
        } catch (Exception e) {
            log.error("创建日志事件异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 提交日志事件到队列
     * @param event 日志事件
     * @return 是否成功提交
     */
    private boolean submitEvent(LogEvent event) {
        try {
            // 先尝试非阻塞提交
            boolean result = eventQueue.offer(event);
            if (!result) {
                // 如果队列已满，记录一条警告日志
                log.warn("日志队列已满，丢弃日志: {}", event.getMessage());
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("提交日志事件被中断: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("提交日志事件异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public int getQueueSize() {
        return eventQueue.size();
    }
    
    @Override
    public int getActiveThreadCount() {
        return consumerPool.getActiveCount();
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
} 