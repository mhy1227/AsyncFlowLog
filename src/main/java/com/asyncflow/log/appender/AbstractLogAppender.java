package com.asyncflow.log.appender;

import com.asyncflow.log.model.event.LogEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 日志写入器抽象基类
 * 实现LogAppender接口的通用功能
 */
@Slf4j
public abstract class AbstractLogAppender implements LogAppender {
    
    /**
     * 写入器名称
     */
    protected String name;
    
    /**
     * 写入器类型
     */
    protected String type;
    
    /**
     * 初始化状态
     */
    protected final AtomicBoolean initialized = new AtomicBoolean(false);
    
    /**
     * 写入计数器
     */
    protected final AtomicLong appendCount = new AtomicLong(0);
    
    /**
     * 构造函数
     * @param name 写入器名称
     * @param type 写入器类型
     */
    public AbstractLogAppender(String name, String type) {
        this.name = name;
        this.type = type;
    }
    
    @Override
    public boolean append(LogEvent event) {
        if (!isInitialized()) {
            log.warn("写入器 {} 尚未初始化", name);
            return false;
        }
        
        if (event == null) {
            log.warn("尝试写入空日志事件");
            return false;
        }
        
        try {
            boolean result = doAppend(event);
            if (result) {
                appendCount.incrementAndGet();
            }
            return result;
        } catch (Exception e) {
            log.error("写入日志事件时发生异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public int append(List<LogEvent> events) {
        if (!isInitialized()) {
            log.warn("写入器 {} 尚未初始化", name);
            return 0;
        }
        
        if (events == null || events.isEmpty()) {
            log.warn("尝试写入空日志事件列表");
            return 0;
        }
        
        try {
            int count = doAppendBatch(events);
            appendCount.addAndGet(count);
            return count;
        } catch (Exception e) {
            log.error("批量写入日志事件时发生异常: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public boolean initialize() {
        if (initialized.compareAndSet(false, true)) {
            log.info("初始化写入器 {}", name);
            return doInitialize();
        }
        return true;
    }
    
    @Override
    public void close() {
        if (initialized.compareAndSet(true, false)) {
            log.info("关闭写入器 {}", name);
            doClose();
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized.get();
    }
    
    @Override
    public long getAppendCount() {
        return appendCount.get();
    }
    
    /**
     * 实际的单条日志写入逻辑
     * @param event 日志事件
     * @return 是否写入成功
     * @throws Exception 写入异常
     */
    protected abstract boolean doAppend(LogEvent event) throws Exception;
    
    /**
     * 实际的批量日志写入逻辑
     * @param events 日志事件列表
     * @return 成功写入的事件数量
     * @throws Exception 写入异常
     */
    protected abstract int doAppendBatch(List<LogEvent> events) throws Exception;
    
    /**
     * 实际的初始化逻辑
     * @return 是否初始化成功
     */
    protected abstract boolean doInitialize();
    
    /**
     * 实际的关闭逻辑
     */
    protected abstract void doClose();
} 