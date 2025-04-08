package com.asyncflow.log.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogFileStatus {
    
    ACTIVE("ACTIVE", "活动"),
    ARCHIVED("ARCHIVED", "已归档"),
    DELETED("DELETED", "已删除");
    
    private final String code;
    private final String desc;
    
    public static LogFileStatus fromCode(String code) {
        for (LogFileStatus status : LogFileStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return LogFileStatus.ACTIVE; // 默认返回ACTIVE状态
    }
}