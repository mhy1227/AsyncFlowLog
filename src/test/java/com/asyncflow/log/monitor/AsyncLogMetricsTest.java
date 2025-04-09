package com.asyncflow.log.monitor;

import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.service.AsyncLogService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 异步日志指标监控测试类
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AsyncLogMetricsTest {
    
    @Mock
    private AsyncLogService asyncLogService;
    
    @Mock
    private EventQueue eventQueue;
    
    @Mock
    private ConsumerPool consumerPool;
    
    @InjectMocks
    private AsyncLogMetrics asyncLogMetrics;
    
    private MeterRegistry registry;
    
    @BeforeEach
    public void setUp() {
        // 设置基本模拟行为
        when(asyncLogService.isRunning()).thenReturn(true);
        when(eventQueue.capacity()).thenReturn(1000);
        when(eventQueue.size()).thenReturn(200); // 20% 使用率
        when(consumerPool.getActiveCount()).thenReturn(2);
        when(consumerPool.getCompletedTaskCount()).thenReturn(10000L);
        
        // 创建Meter注册表
        registry = new SimpleMeterRegistry();
    }
    
    @Test
    public void testBindToRegistry() {
        // 将指标绑定到注册表
        asyncLogMetrics.bindTo(registry);
        
        // 验证队列指标
        assertEquals(200, registry.get("asynclog.queue.size").gauge().value());
        assertEquals(1000, registry.get("asynclog.queue.capacity").gauge().value());
        assertEquals(0.2, registry.get("asynclog.queue.usage").gauge().value());
        
        // 验证消费者线程池指标
        assertEquals(2, registry.get("asynclog.consumer.active_threads").gauge().value());
        assertEquals(10000, registry.get("asynclog.consumer.completed_tasks").gauge().value());
        
        // 验证服务状态指标
        assertEquals(1, registry.get("asynclog.service.running").gauge().value());
        
        // 验证事件处理指标
        assertEquals(0, registry.get("asynclog.events.success_count").gauge().value());
        assertEquals(0, registry.get("asynclog.events.failure_count").gauge().value());
        assertEquals(0, registry.get("asynclog.events.discarded_count").gauge().value());
        
        // 初始成功率应为1.0（尚未处理任何事件）
        assertEquals(1.0, registry.get("asynclog.events.success_rate").gauge().value());
    }
    
    @Test
    public void testMetricsIncrement() {
        // 将指标绑定到注册表
        asyncLogMetrics.bindTo(registry);
        
        // 增加成功计数
        asyncLogMetrics.incrementSuccessCount();
        asyncLogMetrics.incrementSuccessCount();
        
        // 增加失败计数
        asyncLogMetrics.incrementFailureCount();
        
        // 增加丢弃计数
        asyncLogMetrics.incrementDiscardedCount();
        asyncLogMetrics.incrementDiscardedCount();
        asyncLogMetrics.incrementDiscardedCount();
        
        // 验证计数器
        assertEquals(2, registry.get("asynclog.events.success_count").gauge().value());
        assertEquals(1, registry.get("asynclog.events.failure_count").gauge().value());
        assertEquals(3, registry.get("asynclog.events.discarded_count").gauge().value());
        
        // 成功率应为 2/(2+1) = 0.6667
        assertEquals(0.6667, registry.get("asynclog.events.success_rate").gauge().value(), 0.0001);
    }
    
    @Test
    public void testServiceStatus() {
        // 设置服务为停止状态
        when(asyncLogService.isRunning()).thenReturn(false);
        
        // 将指标绑定到注册表
        asyncLogMetrics.bindTo(registry);
        
        // 验证服务状态指标
        assertEquals(0, registry.get("asynclog.service.running").gauge().value());
    }
    
    @Test
    public void testQueueUsage() {
        // 修改队列使用情况
        when(eventQueue.size()).thenReturn(800); // 80% 使用率
        
        // 将指标绑定到注册表
        asyncLogMetrics.bindTo(registry);
        
        // 验证队列指标
        assertEquals(800, registry.get("asynclog.queue.size").gauge().value());
        assertEquals(0.8, registry.get("asynclog.queue.usage").gauge().value());
    }
    
    @Test
    public void testGetters() {
        asyncLogMetrics.incrementSuccessCount();
        asyncLogMetrics.incrementSuccessCount();
        asyncLogMetrics.incrementFailureCount();
        asyncLogMetrics.incrementDiscardedCount();
        
        assertEquals(2, asyncLogMetrics.getSuccessCount());
        assertEquals(1, asyncLogMetrics.getFailureCount());
        assertEquals(1, asyncLogMetrics.getDiscardedCount());
    }
} 

