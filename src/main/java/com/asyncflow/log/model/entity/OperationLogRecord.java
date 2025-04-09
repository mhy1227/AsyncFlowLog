package com.asyncflow.log.model.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogRecord {
    
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 操作模块
     */
    private String module;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求IP
     */
    private String ip;
    
    /**
     * 请求参数
     */
    private String requestParams;
    
    /**
     * 返回结果
     */
    private String result;
    
    /**
     * 执行时长(毫秒)
     */
    private Long duration;
    
    /**
     * 操作状态（0成功 1失败）
     */
    private Integer status;
    
    /**
     * 异常信息
     */
    private String errorMessage;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
} 