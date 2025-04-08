package com.asyncflow.log.service.impl;

import com.asyncflow.log.mapper.LogFileMapper;
import com.asyncflow.log.model.entity.LogFile;
import com.asyncflow.log.service.LogFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LogFileServiceImpl implements LogFileService {

    @Autowired
    private LogFileMapper logFileMapper;

    @Override
    @Transactional
    public LogFile createLogFile(LogFile logFile) {
        logFileMapper.insert(logFile);
        return logFile;
    }

    @Override
    public LogFile getLogFileById(Long id) {
        return logFileMapper.selectById(id);
    }

    @Override
    public LogFile getLogFileByPath(String filePath) {
        return logFileMapper.selectByFilePath(filePath);
    }

    @Override
    public List<LogFile> getLogFilesByStatus(String status) {
        return logFileMapper.selectByStatus(status);
    }

    @Override
    @Transactional
    public boolean updateLogFileStatus(Long id, String status) {
        return logFileMapper.updateStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean updateLogFileInfo(Long id, Long fileSize, String endTime) {
        return logFileMapper.updateFileInfo(id, fileSize, endTime) > 0;
    }
} 