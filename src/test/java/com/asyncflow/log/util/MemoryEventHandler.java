package com.asyncflow.log.util;

import com.asyncflow.log.consumer.EventHandler;
import com.asyncflow.log.model.event.LogEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 内存事件处理器，用于测试
 * 将日志事件保存在内存中，不写入外部存储
 */
@Slf4j
public class MemoryEventHandler implements EventHandler {
    
    private final List<LogEvent> events = new CopyOnWriteArrayList<>();
    
    @Override
    public void initialize() {
        log.info("MemoryEventHandler initialized");
    }
    
    @Override
    public boolean handle(LogEvent event) {
        events.add(event);
        log.debug("Event handled: {}", event);
        return true;
    }
    
    @Override
    public int handleBatch(Iterable<LogEvent> eventBatch) {
        int count = 0;
        for (LogEvent event : eventBatch) {
            events.add(event);
            count++;
        }
        log.debug("Batch handled: {} events", count);
        return count;
    }
    
    @Override
    public void handleException(LogEvent event, Throwable e) {
        log.error("Error handling event: {}", event, e);
    }
    
    @Override
    public void close() {
        log.info("MemoryEventHandler closed");
        events.clear();
    }
    
    /**
     * 获取处理过的所有事件
     */
    public List<LogEvent> getEvents() {
        return new ArrayList<>(events);
    }
    
    /**
     * 清空事件列表
     */
    public void clearEvents() {
        events.clear();
    }
} 