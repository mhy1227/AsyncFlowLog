package com.asyncflow.log.exception;

import lombok.Getter;

@Getter
public class AsyncLogException extends RuntimeException {
    
    private final String code;
    
    public AsyncLogException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    
    public AsyncLogException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }
    
    public AsyncLogException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public AsyncLogException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}