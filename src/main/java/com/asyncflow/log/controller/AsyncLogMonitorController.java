package com.asyncflow.log.controller;

import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.monitor.AsyncLogMetrics;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.service.AsyncLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 异步日志监控REST API
 * 提供异步日志系统状态查询接口
 */
@Slf4j
@RestController
@RequestMapping("/api/asynclog/monitor")
@ConditionalOnProperty(prefix = "async.log.monitor", name = "api.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncLogMonitorController {
    
    @Autowired
    private AsyncLogService asyncLogService;
    
    @Autowired
    private EventQueue eventQueue;
    
    @Autowired
    private ConsumerPool consumerPool;
    
    @Autowired(required = false)
    private AsyncLogMetrics asyncLogMetrics;
    
    /**
     * 获取系统状态
     * @return 包含系统状态信息的响应
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.debug("请求异步日志系统状态");
        Map<String, Object> status = new HashMap<>();
        
        // 服务状态
        status.put("service_running", asyncLogService.isRunning());
        
        // 队列状态
        Map<String, Object> queueStats = new HashMap<>();
        queueStats.put("size", eventQueue.size());
        queueStats.put("capacity", eventQueue.capacity());
        queueStats.put("usage", String.format("%.2f%%", (double) eventQueue.size() / eventQueue.capacity() * 100));
        status.put("queue", queueStats);
        
        // 消费者状态
        Map<String, Object> consumerStats = new HashMap<>();
        consumerStats.put("active_threads", consumerPool.getActiveCount());
        consumerStats.put("completed_tasks", consumerPool.getCompletedTaskCount());
        consumerStats.put("is_shutdown", consumerPool.isShutdown());
        status.put("consumer", consumerStats);
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * 获取处理指标
     * @return 包含处理指标的响应
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        log.debug("请求异步日志系统指标");
        Map<String, Object> metrics = new HashMap<>();
        
        if (asyncLogMetrics != null) {
            // 事件处理指标
            Map<String, Object> eventStats = new HashMap<>();
            eventStats.put("success_count", asyncLogMetrics.getSuccessCount());
            eventStats.put("failure_count", asyncLogMetrics.getFailureCount());
            eventStats.put("discarded_count", asyncLogMetrics.getDiscardedCount());
            
            long total = asyncLogMetrics.getSuccessCount() + asyncLogMetrics.getFailureCount();
            double successRate = total > 0 ? (double) asyncLogMetrics.getSuccessCount() / total : 1.0;
            eventStats.put("success_rate", String.format("%.2f%%", successRate * 100));
            
            metrics.put("events", eventStats);
        } else {
            metrics.put("message", "指标监控未启用");
        }
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 获取系统信息
     * @return 包含系统信息的响应
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        log.debug("请求异步日志系统信息");
        Map<String, Object> info = new HashMap<>();
        
        // JVM内存信息
        Map<String, Object> memoryInfo = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        memoryInfo.put("max", formatSize(maxMemory));
        memoryInfo.put("total", formatSize(totalMemory));
        memoryInfo.put("free", formatSize(freeMemory));
        memoryInfo.put("used", formatSize(usedMemory));
        memoryInfo.put("usage", String.format("%.2f%%", (double) usedMemory / maxMemory * 100));
        
        info.put("memory", memoryInfo);
        
        // 线程信息
        Map<String, Object> threadInfo = new HashMap<>();
        threadInfo.put("count", Thread.activeCount());
        threadInfo.put("consumer_threads", consumerPool.getActiveCount());
        
        info.put("threads", threadInfo);
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * 格式化字节大小为人类可读形式
     * @param bytes 字节数
     * @return 格式化后的大小
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
} 