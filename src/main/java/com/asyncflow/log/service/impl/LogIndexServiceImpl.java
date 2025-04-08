package com.asyncflow.log.service.impl;

import com.asyncflow.log.mapper.LogIndexMapper;
import com.asyncflow.log.model.entity.LogIndex;
import com.asyncflow.log.service.LogIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LogIndexServiceImpl implements LogIndexService {

    @Autowired
    private LogIndexMapper logIndexMapper;

    @Override
    @Transactional
    public LogIndex createLogIndex(LogIndex logIndex) {
        logIndexMapper.insert(logIndex);
        return logIndex;
    }

    @Override
    public LogIndex getLogIndexById(Long id) {
        return logIndexMapper.selectById(id);
    }

    @Override
    public List<LogIndex> getLogIndexesByFileId(Long fileId) {
        return logIndexMapper.selectByFileId(fileId);
    }

    @Override
    public List<LogIndex> getLogIndexesByLevelAndTimeRange(String level, String startTime, String endTime) {
        return logIndexMapper.selectByLevelAndTimeRange(level, startTime, endTime);
    }

    @Override
    public List<LogIndex> getLogIndexesByKeyword(String keyword) {
        return logIndexMapper.selectByKeyword(keyword);
    }
} 