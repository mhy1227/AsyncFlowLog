package com.asyncflow.log.model.event;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志事件工厂类
 * 提供多种创建日志事件的方法
 */
@Component
public class LogEventFactory {
    
    /**
     * 创建基础日志事件
     * @param level 日志级别
     * @param message 日志消息
     * @return 日志事件对象
     */
    public LogEventDTO createLogEvent(String level, String message) {
        return new LogEventDTO(level, message);
    }
    
    /**
     * 创建带上下文的日志事件
     * @param level 日志级别
     * @param message 日志消息
     * @param context 上下文信息
     * @return 日志事件对象
     */
    public LogEventDTO createLogEvent(String level, String message, Map<String, String> context) {
        return new LogEventDTO(level, message, context);
    }
    
    /**
     * 创建带异常信息的日志事件
     * @param level 日志级别
     * @param message 日志消息
     * @param exception 异常信息
     * @return 日志事件对象
     */
    public LogEventDTO createLogEventWithException(String level, String message, String exception) {
        return new LogEventDTO(level, message)
                .withException(exception);
    }
    
    /**
     * 创建带调用位置信息的日志事件
     * @param level 日志级别
     * @param message 日志消息
     * @param className 类名
     * @param methodName 方法名
     * @return 日志事件对象
     */
    public LogEventDTO createLogEventWithLocation(String level, String message, String className, String methodName) {
        return new LogEventDTO(level, message)
                .withLocation(className, methodName);
    }
    
    /**
     * 创建完整的日志事件
     * @param level 日志级别
     * @param message 日志消息
     * @param context 上下文信息
     * @param className 类名
     * @param methodName 方法名
     * @param exception 异常信息
     * @return 日志事件对象
     */
    public LogEventDTO createFullLogEvent(String level, String message, Map<String, String> context,
                                        String className, String methodName, String exception) {
        LogEventDTO event = new LogEventDTO(level, message, context);
        if (className != null && methodName != null) {
            event.withLocation(className, methodName);
        }
        if (exception != null) {
            event.withException(exception);
        }
        return event;
    }
    
    /**
     * 创建错误日志事件
     * @param message 错误消息
     * @param exception 异常信息
     * @return 日志事件对象
     */
    public LogEventDTO createErrorLogEvent(String message, String exception) {
        return createLogEventWithException("ERROR", message, exception);
    }
    
    /**
     * 创建警告日志事件
     * @param message 警告消息
     * @return 日志事件对象
     */
    public LogEventDTO createWarnLogEvent(String message) {
        return createLogEvent("WARN", message);
    }
    
    /**
     * 创建信息日志事件
     * @param message 信息消息
     * @return 日志事件对象
     */
    public LogEventDTO createInfoLogEvent(String message) {
        return createLogEvent("INFO", message);
    }
    
    /**
     * 创建调试日志事件
     * @param message 调试消息
     * @return 日志事件对象
     */
    public LogEventDTO createDebugLogEvent(String message) {
        return createLogEvent("DEBUG", message);
    }
    
    /**
     * 创建带时间戳的日志事件
     * @param level 日志级别
     * @param message 日志消息
     * @param timestamp 时间戳
     * @return 日志事件对象
     */
    public LogEventDTO createLogEventWithTimestamp(String level, String message, LocalDateTime timestamp) {
        LogEventDTO event = new LogEventDTO(level, message);
        event.setTimestamp(timestamp);
        return event;
    }
    
    /**
     * 创建带线程信息的日志事件
     * @param level 日志级别
     * @param message 日志消息
     * @param threadName 线程名称
     * @return 日志事件对象
     */
    public LogEventDTO createLogEventWithThread(String level, String message, String threadName) {
        LogEventDTO event = new LogEventDTO(level, message);
        event.setThreadName(threadName);
        return event;
    }
} 