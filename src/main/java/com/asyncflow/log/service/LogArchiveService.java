package com.asyncflow.log.service;

import com.asyncflow.log.model.entity.LogArchive;

import java.util.List;

/**
 * 日志归档服务接口
 */
public interface LogArchiveService {
    
    /**
     * 创建日志归档记录
     * @param logArchive 日志归档信息
     * @return 创建后的日志归档记录
     */
    LogArchive createLogArchive(LogArchive logArchive);
    
    /**
     * 根据ID获取日志归档记录
     * @param id 归档ID
     * @return 日志归档记录
     */
    LogArchive getLogArchiveById(Long id);
    
    /**
     * 根据文件ID获取日志归档记录
     * @param fileId 文件ID
     * @return 日志归档记录
     */
    LogArchive getLogArchiveByFileId(Long fileId);
    
    /**
     * 根据时间范围查询日志归档记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志归档记录列表
     */
    List<LogArchive> getLogArchivesByTimeRange(String startTime, String endTime);
    
    /**
     * 归档日志文件
     * @param fileId 要归档的日志文件ID
     * @param archivePath 归档路径
     * @param archiveReason 归档原因
     * @return 创建的归档记录
     */
    LogArchive archiveLogFile(Long fileId, String archivePath, String archiveReason);
} 