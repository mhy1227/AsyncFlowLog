package com.asyncflow.log.monitor;

import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.service.AsyncLogService;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 异步日志系统指标监控
 * 使用Micrometer框架监控异步日志系统的各项指标
 */
@Slf4j
@Component
@ConditionalOnClass(MeterBinder.class)
@ConditionalOnProperty(prefix = "async.log.monitor", name = "metrics.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncLogMetrics implements MeterBinder {
    
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
     * 消费者线程池
     */
    @Autowired
    private ConsumerPool consumerPool;
    
    /**
     * 记录处理成功的日志事件数
     */
    private final AtomicLong successCount = new AtomicLong(0);
    
    /**
     * 记录处理失败的日志事件数
     */
    private final AtomicLong failureCount = new AtomicLong(0);
    
    /**
     * 记录丢弃的日志事件数
     */
    private final AtomicLong discardedCount = new AtomicLong(0);
    
    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        log.info("初始化异步日志系统指标监控");
    }
    
    /**
     * 增加成功处理的日志事件数
     */
    public void incrementSuccessCount() {
        successCount.incrementAndGet();
    }
    
    /**
     * 增加处理失败的日志事件数
     */
    public void incrementFailureCount() {
        failureCount.incrementAndGet();
    }
    
    /**
     * 增加丢弃的日志事件数
     */
    public void incrementDiscardedCount() {
        discardedCount.incrementAndGet();
    }
    
    /**
     * 获取成功处理的日志事件数
     */
    public long getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * 获取处理失败的日志事件数
     */
    public long getFailureCount() {
        return failureCount.get();
    }
    
    /**
     * 获取丢弃的日志事件数
     */
    public long getDiscardedCount() {
        return discardedCount.get();
    }
    
    @Override
    public void bindTo(MeterRegistry registry) {
        // 注册队列指标
        Gauge.builder("asynclog.queue.size", eventQueue::size)
            .description("当前队列中的事件数量")
            .register(registry);
        
        Gauge.builder("asynclog.queue.capacity", eventQueue::capacity)
            .description("队列总容量")
            .register(registry);
        
        Gauge.builder("asynclog.queue.usage", () -> (double) eventQueue.size() / eventQueue.capacity())
            .description("队列使用率")
            .register(registry);
        
        // 注册消费者线程池指标
        Gauge.builder("asynclog.consumer.active_threads", consumerPool::getActiveCount)
            .description("活跃线程数")
            .register(registry);
        
        Gauge.builder("asynclog.consumer.completed_tasks", consumerPool::getCompletedTaskCount)
            .description("已完成任务数")
            .register(registry);
        
        Gauge.builder("asynclog.service.running", () -> asyncLogService.isRunning() ? 1 : 0)
            .description("异步日志服务是否运行中（1=运行，0=停止）")
            .register(registry);
        
        // 注册事件处理指标
        Gauge.builder("asynclog.events.success_count", this::getSuccessCount)
            .description("成功处理的日志事件数")
            .register(registry);
        
        Gauge.builder("asynclog.events.failure_count", this::getFailureCount)
            .description("处理失败的日志事件数")
            .register(registry);
        
        Gauge.builder("asynclog.events.discarded_count", this::getDiscardedCount)
            .description("丢弃的日志事件数")
            .register(registry);
        
        // 注册成功率指标
        Gauge.builder("asynclog.events.success_rate", () -> {
            long total = successCount.get() + failureCount.get();
            return total > 0 ? (double) successCount.get() / total : 1.0;
        })
            .description("日志事件处理成功率")
            .register(registry);
    }
} 