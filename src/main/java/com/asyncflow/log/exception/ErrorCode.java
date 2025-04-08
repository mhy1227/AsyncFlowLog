package com.asyncflow.log.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    // 系统错误
    SYSTEM_ERROR("SYS0001", "系统错误"),
    PARAM_ERROR("SYS0002", "参数错误"),
    
    // 日志错误
    LOG_NOT_FOUND("LOG0001", "日志不存在"),
    LOG_WRITE_ERROR("LOG0002", "日志写入错误"),
    LOG_READ_ERROR("LOG0003", "日志读取错误"),
    LOG_QUEUE_FULL("LOG0004", "日志队列已满"),
    
    // 文件错误
    FILE_NOT_FOUND("FILE0001", "文件不存在"),
    FILE_CREATE_ERROR("FILE0002", "文件创建失败"),
    FILE_DELETE_ERROR("FILE0003", "文件删除失败"),
    
    // 数据库错误
    DB_INSERT_ERROR("DB0001", "数据库插入失败"),
    DB_UPDATE_ERROR("DB0002", "数据库更新失败"),
    DB_DELETE_ERROR("DB0003", "数据库删除失败"),
    DB_QUERY_ERROR("DB0004", "数据库查询失败");
    
    private final String code;
    private final String message;
}