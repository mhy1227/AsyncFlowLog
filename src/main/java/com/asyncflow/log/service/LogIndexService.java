package com.asyncflow.log.service;

import com.asyncflow.log.model.entity.LogIndex;

import java.util.List;

public interface LogIndexService {
    
    /**
     * 创建日志索引
     * @param logIndex 日志索引信息
     * @return 创建后的日志索引
     */
    LogIndex createLogIndex(LogIndex logIndex);
    
    /**
     * 根据ID获取日志索引
     * @param id 索引ID
     * @return 日志索引
     */
    LogIndex getLogIndexById(Long id);
    
    /**
     * 根据文件ID获取日志索引列表
     * @param fileId 文件ID
     * @return 日志索引列表
     */
    List<LogIndex> getLogIndexesByFileId(Long fileId);
    
    /**
     * 根据级别和时间范围查询日志索引
     * @param level 日志级别
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志索引列表
     */
    List<LogIndex> getLogIndexesByLevelAndTimeRange(String level, String startTime, String endTime);
    
    /**
     * 根据关键词查询日志索引
     * @param keyword 关键词
     * @return 日志索引列表
     */
    List<LogIndex> getLogIndexesByKeyword(String keyword);
} 