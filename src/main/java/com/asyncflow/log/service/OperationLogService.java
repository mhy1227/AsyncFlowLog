package com.asyncflow.log.service;

import com.asyncflow.log.model.entity.OperationLogRecord;
import java.util.List;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {
    
    /**
     * 保存操作日志
     * 
     * @param log 操作日志记录
     * @return 是否保存成功
     */
    boolean save(OperationLogRecord log);
    
    /**
     * 异步保存操作日志
     * 
     * @param log 操作日志记录
     */
    void asyncSave(OperationLogRecord log);
    
    /**
     * 根据用户ID查询操作日志
     * 
     * @param userId 用户ID
     * @return 操作日志列表
     */
    List<OperationLogRecord> findByUserId(String userId);
    
    /**
     * 根据操作类型查询操作日志
     * 
     * @param operationType 操作类型
     * @return 操作日志列表
     */
    List<OperationLogRecord> findByOperationType(String operationType);
} 