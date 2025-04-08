package com.asyncflow.log.model.event;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 日志事件数据传输对象
 * 实现LogEvent接口，用于日志事件的传输
 */
@Data
public class LogEventDTO implements LogEvent {
    
    private String logId;
    private LocalDateTime timestamp;
    private String level;
    private String message;
    private Map<String, String> context;
    private String threadName;
    private String className;
    private String methodName;
    private String exception;
    
    /**
     * 默认构造函数
     * 自动生成日志ID和时间戳
     */
    public LogEventDTO() {
        this.logId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.context = new HashMap<>();
    }
    
    /**
     * 带参数的构造函数
     * @param level 日志级别
     * @param message 日志消息
     */
    public LogEventDTO(String level, String message) {
        this();
        this.level = level;
        this.message = message;
    }
    
    /**
     * 带参数的构造函数
     * @param level 日志级别
     * @param message 日志消息
     * @param context 上下文信息
     */
    public LogEventDTO(String level, String message, Map<String, String> context) {
        this(level, message);
        this.context = context;
    }
    
    /**
     * 添加上下文信息
     * @param key 键
     * @param value 值
     * @return 当前对象
     */
    public LogEventDTO addContext(String key, String value) {
        this.context.put(key, value);
        return this;
    }
    
    /**
     * 设置异常信息
     * @param exception 异常信息
     * @return 当前对象
     */
    public LogEventDTO withException(String exception) {
        this.exception = exception;
        return this;
    }
    
    /**
     * 设置调用位置信息
     * @param className 类名
     * @param methodName 方法名
     * @return 当前对象
     */
    public LogEventDTO withLocation(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
        return this;
    }
} 