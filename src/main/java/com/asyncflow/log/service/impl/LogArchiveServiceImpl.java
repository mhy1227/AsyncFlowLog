package com.asyncflow.log.service.impl;

import com.asyncflow.log.mapper.LogArchiveMapper;
import com.asyncflow.log.mapper.LogFileMapper;
import com.asyncflow.log.model.entity.LogArchive;
import com.asyncflow.log.model.entity.LogFile;
import com.asyncflow.log.service.LogArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogArchiveServiceImpl implements LogArchiveService {

    @Autowired
    private LogArchiveMapper logArchiveMapper;
    
    @Autowired
    private LogFileMapper logFileMapper;

    @Override
    @Transactional
    public LogArchive createLogArchive(LogArchive logArchive) {
        // 设置归档时间（如果未设置）
        if (logArchive.getArchiveTime() == null) {
            logArchive.setArchiveTime(LocalDateTime.now());
        }
        
        logArchiveMapper.insert(logArchive);
        return logArchive;
    }

    @Override
    public LogArchive getLogArchiveById(Long id) {
        return logArchiveMapper.selectById(id);
    }

    @Override
    public LogArchive getLogArchiveByFileId(Long fileId) {
        return logArchiveMapper.selectByFileId(fileId);
    }

    @Override
    public List<LogArchive> getLogArchivesByTimeRange(String startTime, String endTime) {
        return logArchiveMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    @Transactional
    public LogArchive archiveLogFile(Long fileId, String archivePath, String archiveReason) {
        // 1. 获取文件信息
        LogFile logFile = logFileMapper.selectById(fileId);
        if (logFile == null) {
            throw new IllegalArgumentException("Log file not found with ID: " + fileId);
        }
        
        // 2. 更新文件状态为已归档
        logFileMapper.updateStatus(fileId, "ARCHIVED");
        
        // 3. 创建归档记录
        LogArchive logArchive = new LogArchive();
        logArchive.setFileId(fileId);
        logArchive.setArchivePath(archivePath);
        logArchive.setArchiveTime(LocalDateTime.now());
        
        // 如果提供了归档原因，则设置
        if (archiveReason != null && !archiveReason.isEmpty()) {
            logArchive.setArchiveReason(archiveReason);
        }
        
        logArchiveMapper.insert(logArchive);
        
        return logArchive;
    }
} 