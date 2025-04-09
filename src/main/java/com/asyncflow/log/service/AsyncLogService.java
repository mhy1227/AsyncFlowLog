package com.asyncflow.log.service;

import java.util.Map;

/**
 * 异步日志服务接口
 * 提供异步记录日志的方法
 */
public interface AsyncLogService {
    
    /**
     * 记录ERROR级别日志
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    boolean error(String message);
    
    /**
     * 记录ERROR级别日志，带异常信息
     * @param message 日志消息
     * @param exception 异常信息
     * @return 是否成功提交到队列
     */
    boolean error(String message, String exception);
    
    /**
     * 记录ERROR级别日志，带上下文信息
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    boolean error(String message, Map<String, String> context);
    
    /**
     * 记录ERROR级别日志，带异常和上下文信息
     * @param message 日志消息
     * @param exception 异常信息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    boolean error(String message, String exception, Map<String, String> context);
    
    /**
     * 记录WARN级别日志
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    boolean warn(String message);
    
    /**
     * 记录WARN级别日志，带上下文信息
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    boolean warn(String message, Map<String, String> context);
    
    /**
     * 记录INFO级别日志
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    boolean info(String message);
    
    /**
     * 记录INFO级别日志，带上下文信息
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    boolean info(String message, Map<String, String> context);
    
    /**
     * 记录DEBUG级别日志
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    boolean debug(String message);
    
    /**
     * 记录DEBUG级别日志，带上下文信息
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    boolean debug(String message, Map<String, String> context);
    
    /**
     * 记录指定级别的日志
     * @param level 日志级别
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    boolean log(String level, String message);
    
    /**
     * 记录指定级别的日志，带上下文信息
     * @param level 日志级别
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    boolean log(String level, String message, Map<String, String> context);
    
    /**
     * 记录指定级别的日志，带异常信息
     * @param level 日志级别
     * @param message 日志消息
     * @param exception 异常信息
     * @return 是否成功提交到队列
     */
    boolean log(String level, String message, String exception);
    
    /**
     * 记录指定级别的日志，带异常和上下文信息
     * @param level 日志级别
     * @param message 日志消息
     * @param exception 异常信息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    boolean log(String level, String message, String exception, Map<String, String> context);
    
    /**
     * 记录指定级别的日志，带调用位置信息
     * @param level 日志级别
     * @param message 日志消息
     * @param className 类名
     * @param methodName 方法名
     * @return 是否成功提交到队列
     */
    boolean log(String level, String message, String className, String methodName);
    
    /**
     * 记录指定级别的日志，带调用位置和上下文信息
     * @param level 日志级别
     * @param message 日志消息
     * @param className 类名
     * @param methodName 方法名
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    boolean log(String level, String message, String className, String methodName, Map<String, String> context);
    
    /**
     * 启动异步日志服务
     */
    void start();
    
    /**
     * 关闭异步日志服务
     */
    void shutdown();
    
    /**
     * 获取队列大小
     * @return 队列大小
     */
    int getQueueSize();
    
    /**
     * 获取活跃线程数
     * @return 活跃线程数
     */
    int getActiveThreadCount();
    
    /**
     * 刷新所有日志，确保写入
     */
    void flush();
    
    /**
     * 检查异步日志服务是否正在运行
     * @return 如果服务正在运行返回true，否则返回false
     */
    boolean isRunning();
} 