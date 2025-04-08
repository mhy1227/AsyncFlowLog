package com.asyncflow.log.service;

import com.asyncflow.log.model.entity.LogFile;

import java.util.List;

public interface LogFileService {
    
    /**
     * 创建新的日志文件记录
     * @param logFile 日志文件信息
     * @return 创建后的日志文件记录
     */
    LogFile createLogFile(LogFile logFile);
    
    /**
     * 根据ID获取日志文件记录
     * @param id 日志文件ID
     * @return 日志文件记录
     */
    LogFile getLogFileById(Long id);
    
    /**
     * 根据文件路径获取日志文件记录
     * @param filePath 文件路径
     * @return 日志文件记录
     */
    LogFile getLogFileByPath(String filePath);
    
    /**
     * 根据状态获取日志文件列表
     * @param status 文件状态
     * @return 日志文件列表
     */
    List<LogFile> getLogFilesByStatus(String status);
    
    /**
     * 更新日志文件状态
     * @param id 日志文件ID
     * @param status 新的状态
     * @return 是否更新成功
     */
    boolean updateLogFileStatus(Long id, String status);
    
    /**
     * 更新日志文件信息
     * @param id 日志文件ID
     * @param fileSize 文件大小
     * @param endTime 结束时间
     * @return 是否更新成功
     */
    boolean updateLogFileInfo(Long id, Long fileSize, String endTime);
} 