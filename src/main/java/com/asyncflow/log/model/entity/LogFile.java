package com.asyncflow.log.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogFile {
    private Long id;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String level;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 