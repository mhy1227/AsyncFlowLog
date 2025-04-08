package com.asyncflow.log.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    
    private final String code;
    
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    
    public BizException(String message) {
        super(message);
        this.code = "BIZ0001";
    }
    
    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }
}