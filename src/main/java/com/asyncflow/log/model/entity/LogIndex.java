package com.asyncflow.log.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogIndex {
    private Long id;
    private Long fileId;
    private Integer lineNumber;
    private LocalDateTime logTime;
    private String level;
    private String keyword;
    private LocalDateTime createTime;
} 