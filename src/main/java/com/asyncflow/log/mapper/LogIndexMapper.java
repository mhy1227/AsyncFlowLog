package com.asyncflow.log.mapper;

import com.asyncflow.log.model.entity.LogIndex;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LogIndexMapper {
    
    @Insert("INSERT INTO log_index (file_id, line_number, log_time, level, keyword) " +
            "VALUES (#{fileId}, #{lineNumber}, #{logTime}, #{level}, #{keyword})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LogIndex logIndex);
    
    @Select("SELECT * FROM log_index WHERE id = #{id}")
    LogIndex selectById(Long id);
    
    @Select("SELECT * FROM log_index WHERE file_id = #{fileId}")
    List<LogIndex> selectByFileId(Long fileId);
    
    @Select("SELECT * FROM log_index WHERE level = #{level} AND log_time BETWEEN #{startTime} AND #{endTime}")
    List<LogIndex> selectByLevelAndTimeRange(@Param("level") String level, 
                                           @Param("startTime") String startTime, 
                                           @Param("endTime") String endTime);
    
    @Select("SELECT * FROM log_index WHERE keyword LIKE CONCAT('%', #{keyword}, '%')")
    List<LogIndex> selectByKeyword(String keyword);
} 