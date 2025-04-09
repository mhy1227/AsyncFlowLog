package com.asyncflow.log.appender;

import com.asyncflow.log.model.event.LogEvent;

import java.util.List;

/**
 * 日志写入器接口
 * 定义将日志事件写入到不同目标的方法
 */
public interface LogAppender {
    
    /**
     * 写入单条日志事件
     * @param event 日志事件
     * @return 是否写入成功
     */
    boolean append(LogEvent event);
    
    /**
     * 批量写入日志事件
     * @param events 日志事件列表
     * @return 成功写入的事件数量
     */
    int append(List<LogEvent> events);
    
    /**
     * 刷新缓冲区，确保所有日志被写入
     */
    void flush();
    
    /**
     * 初始化写入器
     * @return 是否初始化成功
     */
    boolean initialize();
    
    /**
     * 关闭写入器，释放资源
     */
    void close();
    
    /**
     * 获取写入器名称
     * @return 写入器名称
     */
    String getName();
    
    /**
     * 获取写入器类型
     * @return 写入器类型
     */
    String getType();
    
    /**
     * 写入器是否已初始化
     * @return 是否已初始化
     */
    boolean isInitialized();
    
    /**
     * 获取写入的日志事件计数
     * @return 写入计数
     */
    long getAppendCount();
} 