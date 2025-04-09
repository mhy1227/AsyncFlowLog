package com.asyncflow.log.monitor;

import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.service.AsyncLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 异步日志系统健康检查指标
 * 用于监控异步日志系统的健康状态
 */
@Slf4j
@Component
public class AsyncLogHealthIndicator implements HealthIndicator {
    
    /**
     * 队列使用率警告阈值（80%）
     */
    private static final double QUEUE_USAGE_WARN_THRESHOLD = 0.8;
    
    /**
     * 队列使用率危险阈值（95%）
     */
    private static final double QUEUE_USAGE_DANGER_THRESHOLD = 0.95;
    
    /**
     * 异步日志服务
     */
    @Autowired
    private AsyncLogService asyncLogService;
    
    /**
     * 事件队列
     */
    @Autowired
    private EventQueue eventQueue;
    
    /**
     * A方法
     * 消费者线程池
     */
    @Autowired
    private ConsumerPool consumerPool;
    
    @Override
    public Health health() {
        log.debug("检查异步日志系统健康状态");
        Health.Builder builder = new Health.Builder();
        
        try {
            // 检查服务是否在运行
            if (!asyncLogService.isRunning()) {
                return builder.down()
                    .withDetail("error", "异步日志服务未运行")
                    .build();
            }
            
            // 检查队列状态
            int queueSize = eventQueue.size();
            int queueCapacity = eventQueue.capacity();
            double queueUsage = (double) queueSize / queueCapacity;
            
            builder.withDetail("queue.size", queueSize);
            builder.withDetail("queue.capacity", queueCapacity);
            builder.withDetail("queue.usage", String.format("%.2f%%", queueUsage * 100));
            
            // 检查消费者线程池状态
            int activeThreads = consumerPool.getActiveCount();
            boolean isShutdown = consumerPool.isShutdown();
            long completedTasks = consumerPool.getCompletedTaskCount();
            
            builder.withDetail("consumer.activeThreads", activeThreads);
            builder.withDetail("consumer.isShutdown", isShutdown);
            builder.withDetail("consumer.completedTasks", completedTasks);
            
            // 综合评估健康状态
            if (isShutdown) {
                return builder.down()
                    .withDetail("error", "消费者线程池已关闭")
                    .build();
            } else if (queueUsage >= QUEUE_USAGE_DANGER_THRESHOLD) {
                return builder.down()
                    .withDetail("error", "队列使用率过高，可能导致日志丢失")
                    .build();
            } else if (queueUsage >= QUEUE_USAGE_WARN_THRESHOLD) {
                return builder.outOfService()
                    .withDetail("warning", "队列使用率较高，性能可能下降")
                    .build();
            } else {
                return builder.up()
                    .withDetail("status", "异步日志系统运行正常")
                    .build();
            }
        } catch (Exception e) {
            log.error("健康检查过程中发生异常", e);
            return builder.down()
                .withDetail("error", "健康检查异常: " + e.getMessage())
                .build();
        }
    }
} 