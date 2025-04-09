package com.asyncflow.log.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解，用于标记需要记录操作日志的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 操作类型（例如：查询、新增、修改、删除等）
     */
    String operationType() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;
    
    /**
     * 是否记录返回结果
     */
    boolean recordResult() default false;
    
    /**
     * 是否记录异常信息
     */
    boolean recordException() default true;
} 