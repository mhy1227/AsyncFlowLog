package com.asyncflow.log.constant;

public class LogConstants {
    
    // 日志级别
    public static final String LEVEL_DEBUG = "DEBUG";
    public static final String LEVEL_INFO = "INFO";
    public static final String LEVEL_WARN = "WARN";
    public static final String LEVEL_ERROR = "ERROR";
    
    // 日志文件状态
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_ARCHIVED = "ARCHIVED";
    public static final String STATUS_DELETED = "DELETED";
    
    // 文件路径
    public static final String BASE_LOG_PATH = "logs/";
    public static final String ARCHIVE_PATH = "logs/archive/";
    
    // 文件名格式
    public static final String FILE_NAME_FORMAT = "async_log_%s_%s.log"; // 日期, 级别
    
    // 队列配置
    public static final int DEFAULT_QUEUE_CAPACITY = 10000;
    
    // 线程池配置
    public static final int DEFAULT_CORE_POOL_SIZE = 2;
    public static final int DEFAULT_MAX_POOL_SIZE = 4;
    public static final int DEFAULT_KEEP_ALIVE_SECONDS = 60;
    
    // 分页配置
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    // 文件大小限制
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    
    // 日期格式
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
}