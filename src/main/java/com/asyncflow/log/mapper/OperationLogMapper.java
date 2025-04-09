package com.asyncflow.log.mapper;

import com.asyncflow.log.model.entity.OperationLogRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface OperationLogMapper {
    
    /**
     * 插入操作日志
     * 
     * @param log 操作日志记录
     * @return 影响行数
     */
    @Insert("INSERT INTO operation_log(" +
            "user_id, username, module, operation_type, description, method, " +
            "request_url, ip, request_params, result, duration, status, error_message, operation_time) " +
            "VALUES(" +
            "#{userId}, #{username}, #{module}, #{operationType}, #{description}, #{method}, " +
            "#{requestUrl}, #{ip}, #{requestParams}, #{result}, #{duration}, #{status}, #{errorMessage}, #{operationTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OperationLogRecord log);
    
    /**
     * 根据用户ID查询操作日志
     * 
     * @param userId 用户ID
     * @return 操作日志列表
     */
    @Select("SELECT * FROM operation_log WHERE user_id = #{userId} ORDER BY operation_time DESC")
    List<OperationLogRecord> findByUserId(String userId);
    
    /**
     * 根据操作类型查询操作日志
     * 
     * @param operationType 操作类型
     * @return 操作日志列表
     */
    @Select("SELECT * FROM operation_log WHERE operation_type = #{operationType} ORDER BY operation_time DESC")
    List<OperationLogRecord> findByOperationType(String operationType);
} 