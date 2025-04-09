package com.asyncflow.log.consumer;

import com.asyncflow.log.model.event.LogEvent;

/**
 * 日志事件处理器接口
 * 定义日志事件的处理方法
 */
public interface EventHandler {
    
    /**
     * 处理日志事件
     * @param event 日志事件
     * @return 处理结果
     */
    boolean handle(LogEvent event);
    
    /**
     * 批量处理日志事件
     * @param events 日志事件列表
     * @return 成功处理的事件数量
     */
    int handleBatch(Iterable<LogEvent> events);
    
    /**
     * 处理异常情况
     * @param event 日志事件
     * @param throwable 异常
     */
    void handleException(LogEvent event, Throwable throwable);
    
    /**
     * 初始化处理器
     */
    void initialize();
    
    /**
     * 关闭处理器，释放资源
     */
    void close();
} 