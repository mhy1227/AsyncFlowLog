package com.asyncflow.log.monitor;

import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.service.AsyncLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 异步日志健康检查指标测试类
 */
@ExtendWith(MockitoExtension.class)
public class AsyncLogHealthIndicatorTest {
    
    @Mock
    private AsyncLogService asyncLogService;
    
    @Mock
    private EventQueue eventQueue;
    
    @Mock
    private ConsumerPool consumerPool;
    
    @InjectMocks
    private AsyncLogHealthIndicator healthIndicator;
    
    @BeforeEach
    public void setUp() {
        // 设置基本模拟行为
        when(asyncLogService.isRunning()).thenReturn(true);
        when(eventQueue.capacity()).thenReturn(1000);
        when(eventQueue.size()).thenReturn(200);
        when(consumerPool.getActiveCount()).thenReturn(2);
        when(consumerPool.isShutdown()).thenReturn(false);
        when(consumerPool.getCompletedTaskCount()).thenReturn(10000L);
    }
    
    @Test
    public void testHealthUp() {
        // 正常状态：队列使用率低
        when(eventQueue.size()).thenReturn(200);  // 20% 使用率
        
        Health health = healthIndicator.health();
        
        assertEquals(Status.UP, health.getStatus());
        assertEquals("异步日志系统运行正常", health.getDetails().get("status"));
        assertEquals(200, health.getDetails().get("queue.size"));
        assertEquals(1000, health.getDetails().get("queue.capacity"));
        assertEquals("20.00%", health.getDetails().get("queue.usage"));
        assertEquals(2, health.getDetails().get("consumer.activeThreads"));
        assertEquals(false, health.getDetails().get("consumer.isShutdown"));
        assertEquals(10000L, health.getDetails().get("consumer.completedTasks"));
    }
    
    @Test
    public void testHealthOutOfService() {
        // 警告状态：队列使用率较高
        when(eventQueue.size()).thenReturn(850);  // 85% 使用率
        
        Health health = healthIndicator.health();
        
        assertEquals(Status.OUT_OF_SERVICE, health.getStatus());
        assertEquals("队列使用率较高，性能可能下降", health.getDetails().get("warning"));
        assertEquals("85.00%", health.getDetails().get("queue.usage"));
    }
    
    @Test
    public void testHealthDownQueueTooFull() {
        // 故障状态：队列接近满
        when(eventQueue.size()).thenReturn(960);  // 96% 使用率
        
        Health health = healthIndicator.health();
        
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("队列使用率过高，可能导致日志丢失", health.getDetails().get("error"));
        assertEquals("96.00%", health.getDetails().get("queue.usage"));
    }
    
    @Test
    public void testHealthDownConsumerShutdown() {
        // 故障状态：消费者线程池已关闭
        when(consumerPool.isShutdown()).thenReturn(true);
        
        Health health = healthIndicator.health();
        
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("消费者线程池已关闭", health.getDetails().get("error"));
    }
    
    @Test
    public void testHealthDownServiceNotRunning() {
        // 故障状态：服务未运行
        when(asyncLogService.isRunning()).thenReturn(false);
        
        Health health = healthIndicator.health();
        
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("异步日志服务未运行", health.getDetails().get("error"));
    }
    
    @Test
    public void testHealthWithException() {
        // 异常情况：检查过程发生异常
        when(eventQueue.size()).thenThrow(new RuntimeException("测试异常"));
        
        Health health = healthIndicator.health();
        
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().get("error").toString().contains("健康检查异常"));
    }
} 