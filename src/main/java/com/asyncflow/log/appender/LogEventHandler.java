package com.asyncflow.log.appender;

import com.asyncflow.log.consumer.EventHandler;
import com.asyncflow.log.model.event.LogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 日志事件处理器
 * 实现将日志事件写入到日志写入器
 */
@Slf4j
public class LogEventHandler implements EventHandler {
    
    /**
     * 日志写入器
     */
    private final LogAppender appender;
    
    /**
     * 批量大小
     */
    private final int batchSize;
    
    /**
     * 处理计数器
     */
    private final AtomicLong processedCount = new AtomicLong(0);
    
    /**
     * 错误计数器
     */
    private final AtomicLong errorCount = new AtomicLong(0);
    
    /**
     * 批次列表
     */
    private final List<LogEvent> batchList;
    
    /**
     * 构造函数
     * @param appender 日志写入器
     */
    public LogEventHandler(LogAppender appender) {
        this(appender, 100);
    }
    
    /**
     * 带批量大小的构造函数
     * @param appender 日志写入器
     * @param batchSize 批量大小
     */
    public LogEventHandler(LogAppender appender, int batchSize) {
        this.appender = appender;
        this.batchSize = batchSize;
        this.batchList = new ArrayList<>(batchSize);
    }
    
    @Override
    public boolean handle(LogEvent event) {
        if (event == null) {
            return false;
        }
        
        try {
            boolean result = appender.append(event);
            if (result) {
                processedCount.incrementAndGet();
            }
            return result;
        } catch (Exception e) {
            log.error("处理日志事件异常: {}", e.getMessage(), e);
            errorCount.incrementAndGet();
            return false;
        }
    }
    
    @Override
    public int handleBatch(Iterable<LogEvent> events) {
        if (events == null) {
            return 0;
        }
        
        // 将事件添加到批次列表
        List<LogEvent> tempBatch = new ArrayList<>();
        events.forEach(tempBatch::add);
        
        if (CollectionUtils.isEmpty(tempBatch)) {
            return 0;
        }
        
        try {
            int count = appender.append(tempBatch);
            processedCount.addAndGet(count);
            return count;
        } catch (Exception e) {
            log.error("批量处理日志事件异常: {}", e.getMessage(), e);
            errorCount.incrementAndGet();
            return 0;
        }
    }
    
    @Override
    public void handleException(LogEvent event, Throwable throwable) {
        errorCount.incrementAndGet();
        log.error("处理日志事件发生异常: {}", throwable.getMessage(), throwable);
        
        // 尝试记录错误事件本身
        try {
            if (event != null) {
                appender.append(event);
            }
        } catch (Exception e) {
            log.error("记录异常事件时发生二次异常: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void initialize() {
        log.info("初始化日志事件处理器，批量大小: {}", batchSize);
        if (!appender.isInitialized()) {
            appender.initialize();
        }
    }
    
    @Override
    public void close() {
        log.info("关闭日志事件处理器，已处理: {}, 错误: {}", processedCount.get(), errorCount.get());
        
        // 刷新写入器
        appender.flush();
    }
    
    /**
     * 获取处理的日志事件计数
     * @return 处理计数
     */
    public long getProcessedCount() {
        return processedCount.get();
    }
    
    /**
     * 获取处理错误的日志事件计数
     * @return 错误计数
     */
    public long getErrorCount() {
        return errorCount.get();
    }
    
    /**
     * 获取日志写入器
     * @return 日志写入器
     */
    public LogAppender getAppender() {
        return appender;
    }
    
    /**
     * 获取批量大小
     * @return 批量大小
     */
    public int getBatchSize() {
        return batchSize;
    }
} 