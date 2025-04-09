package com.asyncflow.log.appender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 日志写入器工厂类
 * 用于创建不同类型的日志写入器
 */
@Slf4j
@Component
public class AppenderFactory {
    
    /**
     * 写入器类型
     */
    @Value("${async.log.appender.type:file}")
    private String appenderType;
    
    /**
     * 文件路径
     */
    @Value("${async.log.appender.file-path:logs/async}")
    private String filePath;
    
    /**
     * 文件名模式
     */
    @Value("${async.log.appender.file-name-pattern:async-log-%s.log}")
    private String fileNamePattern;
    
    /**
     * 批量大小
     */
    @Value("${async.log.appender.batch-size:100}")
    private int batchSize;
    
    /**
     * 刷新间隔
     */
    @Value("${async.log.appender.flush-interval:1000}")
    private long flushInterval;
    
    /**
     * 是否自动刷新
     */
    @Value("${async.log.appender.auto-flush:false}")
    private boolean autoFlush;
    
    /**
     * 创建默认写入器
     * 根据配置创建指定类型的写入器
     * 
     * @return 日志写入器
     */
    public LogAppender createAppender() {
        log.info("创建写入器，类型: {}", appenderType);
        
        if ("file".equalsIgnoreCase(appenderType)) {
            return createFileAppender();
        }
        
        // 默认使用文件写入器
        log.warn("未知的写入器类型: {}, 使用默认的FileAppender", appenderType);
        return createFileAppender();
    }
    
    /**
     * 创建文件写入器
     * 
     * @return 文件写入器
     */
    public FileAppender createFileAppender() {
        log.info("创建文件写入器，路径: {}, 文件名模式: {}", filePath, fileNamePattern);
        
        FileAppender appender = new FileAppender("FileAppender", filePath, fileNamePattern);
        appender.setAutoFlush(autoFlush);
        
        return appender;
    }
    
    /**
     * 创建指定参数的文件写入器
     * 
     * @param name 写入器名称
     * @param filePath 文件路径
     * @param fileNamePattern 文件名模式
     * @param autoFlush 是否自动刷新
     * @return 文件写入器
     */
    public FileAppender createFileAppender(String name, String filePath, String fileNamePattern, boolean autoFlush) {
        log.info("创建文件写入器，名称: {}, 路径: {}, 文件名模式: {}, 自动刷新: {}", 
                name, filePath, fileNamePattern, autoFlush);
        
        FileAppender appender = new FileAppender(name, filePath, fileNamePattern);
        appender.setAutoFlush(autoFlush);
        
        return appender;
    }
    
    /**
     * 获取配置的写入器类型
     * 
     * @return 写入器类型
     */
    public String getAppenderType() {
        return appenderType;
    }
    
    /**
     * 获取配置的文件路径
     * 
     * @return 文件路径
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * 获取配置的文件名模式
     * 
     * @return 文件名模式
     */
    public String getFileNamePattern() {
        return fileNamePattern;
    }
    
    /**
     * 获取配置的批量大小
     * 
     * @return 批量大小
     */
    public int getBatchSize() {
        return batchSize;
    }
    
    /**
     * 获取配置的刷新间隔
     * 
     * @return 刷新间隔
     */
    public long getFlushInterval() {
        return flushInterval;
    }
    
    /**
     * 获取配置的自动刷新
     * 
     * @return 是否自动刷新
     */
    public boolean isAutoFlush() {
        return autoFlush;
    }
} 