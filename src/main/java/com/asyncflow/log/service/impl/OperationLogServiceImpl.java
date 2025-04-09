package com.asyncflow.log.service.impl;

import com.asyncflow.log.mapper.OperationLogMapper;
import com.asyncflow.log.model.entity.OperationLogRecord;
import com.asyncflow.log.service.AsyncLogService;
import com.asyncflow.log.service.OperationLogService;
import com.asyncflow.log.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务实现类
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    @Autowired
    private AsyncLogService asyncLogService;
    
    @Override
    public boolean save(OperationLogRecord logRecord) {
        try {
            int result = operationLogMapper.insert(logRecord);
            return result > 0;
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
            return false;
        }
    }
    
    @Async
    @Override
    public void asyncSave(OperationLogRecord logRecord) {
        boolean result = save(logRecord);
        
        // 使用现有的异步日志系统记录日志
        Map<String, String> context = new HashMap<>();
        context.put("userId", logRecord.getUserId());
        context.put("operation", logRecord.getOperationType());
        context.put("module", logRecord.getModule());
        context.put("status", logRecord.getStatus().toString());
        
        if (result) {
            asyncLogService.log("INFO", "操作日志已保存: " + logRecord.getDescription(), context);
        } else {
            asyncLogService.log("ERROR", "操作日志保存失败: " + logRecord.getDescription(), context);
        }
    }
    
    @Override
    public List<OperationLogRecord> findByUserId(String userId) {
        return operationLogMapper.findByUserId(userId);
    }
    
    @Override
    public List<OperationLogRecord> findByOperationType(String operationType) {
        return operationLogMapper.findByOperationType(operationType);
    }
} 