package com.asyncflow.log.aspect;

import com.asyncflow.log.annotation.OperationLog;
import com.asyncflow.log.model.entity.OperationLogRecord;
import com.asyncflow.log.service.OperationLogService;
import com.asyncflow.log.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {
    
    private final OperationLogService operationLogService;
    
    /**
     * 构造函数注入
     * @param operationLogService 操作日志服务
     */
    @Autowired
    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }
    
    /**
     * 定义切点 - 所有带有@OperationLog注解的方法
     */
    @Pointcut("@annotation(com.asyncflow.log.annotation.OperationLog)")
    public void operationLogPointcut() {
    }
    
    /**
     * 环绕通知
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        
        // 准备日志记录对象
        OperationLogRecord logRecord = OperationLogRecord.builder()
                .operationTime(LocalDateTime.now())
                .method(method.getDeclaringClass().getName() + "." + method.getName())
                .build();
        
        // 设置请求相关信息
        if (request != null) {
            logRecord.setIp(getIpAddress(request));
            logRecord.setRequestUrl(request.getRequestURI());
            
            // TODO: 从请求中获取用户信息
            // logRecord.setUserId(getUserId(request));
            // logRecord.setUsername(getUsername(request));
        }
        
        // 设置注解中的信息
        logRecord.setDescription(operationLog.description());
        logRecord.setOperationType(operationLog.operationType());
        
        // 记录请求参数
        if (operationLog.recordParams()) {
            try {
                logRecord.setRequestParams(JsonUtils.objectToJson(joinPoint.getArgs()));
            } catch (Exception e) {
                logRecord.setRequestParams(Arrays.toString(joinPoint.getArgs()));
            }
        }
        
        Object result = null;
        try {
            // 执行原方法
            result = joinPoint.proceed();
            
            // 设置执行结果
            logRecord.setStatus(0); // 成功
            
            // 记录返回结果
            if (operationLog.recordResult() && result != null) {
                try {
                    logRecord.setResult(JsonUtils.objectToJson(result));
                } catch (Exception e) {
                    logRecord.setResult(result.toString());
                }
            }
            
        } catch (Throwable e) {
            // 发生异常
            logRecord.setStatus(1); // 失败
            
            // 记录异常信息
            if (operationLog.recordException()) {
                logRecord.setErrorMessage(e.getMessage());
            }
            
            // 继续抛出异常
            throw e;
        } finally {
            // 计算执行时长
            long endTime = System.currentTimeMillis();
            logRecord.setDuration(endTime - startTime);
            
            // 异步保存操作日志
            operationLogService.asyncSave(logRecord);
        }
        
        return result;
    }
    
    /**
     * 获取请求IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
} 