package com.asyncflow.log.controller;

import com.asyncflow.log.annotation.OperationLog;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试AOP操作日志的控制器，专用于测试环境
 */
@RestController
@RequestMapping("/api/test")
public class TestOperationLogController {

    /**
     * 测试GET请求的操作日志记录
     */
    @OperationLog(description = "测试GET操作", operationType = "QUERY")
    @GetMapping("/log")
    public Map<String, Object> testGet(@RequestParam(required = false) String param) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "测试GET请求操作日志");
        result.put("param", param);
        return result;
    }

    /**
     * 测试POST请求的操作日志记录
     */
    @OperationLog(description = "测试POST操作", operationType = "CREATE", recordResult = true)
    @PostMapping("/log")
    public Map<String, Object> testPost(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "测试POST请求操作日志");
        result.put("receivedParams", params);
        return result;
    }

    /**
     * 测试异常情况下的操作日志记录
     */
    @OperationLog(description = "测试异常操作", operationType = "ERROR")
    @GetMapping("/error")
    public Map<String, Object> testError() {
        throw new RuntimeException("测试异常情况下的操作日志记录");
    }
} 