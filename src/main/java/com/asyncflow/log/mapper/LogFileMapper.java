package com.asyncflow.log.mapper;

import com.asyncflow.log.model.entity.LogFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LogFileMapper {
    
    @Insert("INSERT INTO log_file (file_path, file_name, file_size, start_time, end_time, level, status) " +
            "VALUES (#{filePath}, #{fileName}, #{fileSize}, #{startTime}, #{endTime}, #{level}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LogFile logFile);
    
    @Select("SELECT * FROM log_file WHERE id = #{id}")
    LogFile selectById(Long id);
    
    @Select("SELECT * FROM log_file WHERE file_path = #{filePath}")
    LogFile selectByFilePath(String filePath);
    
    @Select("SELECT * FROM log_file WHERE status = #{status}")
    List<LogFile> selectByStatus(String status);
    
    @Update("UPDATE log_file SET status = #{status}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Update("UPDATE log_file SET file_size = #{fileSize}, end_time = #{endTime}, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int updateFileInfo(@Param("id") Long id, @Param("fileSize") Long fileSize, @Param("endTime") String endTime);
} 