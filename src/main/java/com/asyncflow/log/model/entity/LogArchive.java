package com.asyncflow.log.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogArchive {
    private Long id;
    private Long fileId;
    private String archivePath;
    private LocalDateTime archiveTime;
    private LocalDateTime createTime;
} 