package com.asyncflow.log.mapper;

import com.asyncflow.log.model.entity.LogArchive;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LogArchiveMapper {
    
    @Insert("INSERT INTO log_archive (file_id, archive_path, archive_time, archive_reason) " +
            "VALUES (#{fileId}, #{archivePath}, #{archiveTime}, #{archiveReason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LogArchive logArchive);
    
    @Select("SELECT * FROM log_archive WHERE id = #{id}")
    LogArchive selectById(Long id);
    
    @Select("SELECT * FROM log_archive WHERE file_id = #{fileId}")
    LogArchive selectByFileId(Long fileId);
    
    @Select("SELECT * FROM log_archive WHERE archive_time BETWEEN #{startTime} AND #{endTime}")
    List<LogArchive> selectByTimeRange(@Param("startTime") String startTime, 
                                     @Param("endTime") String endTime);
} 