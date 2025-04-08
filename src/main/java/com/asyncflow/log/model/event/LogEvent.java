package com.asyncflow.log.model.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 日志事件接口
 * 定义日志事件的基本属性和方法
 */
public interface LogEvent {
    
    /**
     * 获取日志时间戳
     * @return 日志时间戳
     */
    LocalDateTime getTimestamp();
    
    /**
     * 获取日志级别
     * @return 日志级别
     */
    String getLevel();
    
    /**
     * 获取日志消息
     * @return 日志消息
     */
    String getMessage();
    
    /**
     * 获取日志上下文信息
     * @return 日志上下文信息
     */
    Map<String, String> getContext();
    
    /**
     * 获取线程名称
     * @return 线程名称
     */
    String getThreadName();
    
    /**
     * 获取类名
     * @return 类名
     */
    String getClassName();
    
    /**
     * 获取方法名
     * @return 方法名
     */
    String getMethodName();
    
    /**
     * 获取异常信息
     * @return 异常信息
     */
    String getException();
    
    /**
     * 获取日志ID
     * @return 日志ID
     */
    String getLogId();
} 