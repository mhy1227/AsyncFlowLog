package com.asyncflow.log.service;

import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.consumer.EventHandler;
import com.asyncflow.log.model.event.LogEvent;
import com.asyncflow.log.model.event.LogEventFactory;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.service.impl.AsyncLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 异步日志服务单元测试类
 */
public class AsyncLogServiceTest {
    
    @Mock
    private LogEventFactory eventFactory;
    
    @Mock
    private EventQueue eventQueue;
    
    @Mock
    private ConsumerPool consumerPool;
    
    @Mock
    private LogEvent mockEvent;
    
    @InjectMocks
    private AsyncLogServiceImpl asyncLogService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // 设置eventFactory默认行为
        when(eventFactory.createLogEvent(anyString(), anyString())).thenReturn(mockEvent);
        when(eventFactory.createLogEvent(anyString(), anyString(), any(Map.class))).thenReturn(mockEvent);
        when(eventFactory.createLogEventWithException(anyString(), anyString(), anyString())).thenReturn(mockEvent);
        when(eventFactory.createFullLogEvent(anyString(), anyString(), any(Map.class), anyString(), anyString(), anyString())).thenReturn(mockEvent);
        
        // 设置eventQueue默认行为
        try {
            when(eventQueue.offer(any(LogEvent.class))).thenReturn(true);
        } catch (InterruptedException e) {
            fail("Mock设置失败");
        }
        
        // 启动服务
        asyncLogService.start();
    }
    
    @Test
    public void testStart() {
        // 验证服务启动时调用了ConsumerPool的方法
        verify(consumerPool).setEventQueue(eventQueue);
        verify(consumerPool).start();
        
        // 再次启动应该不再重复调用
        asyncLogService.start();
        verify(consumerPool, times(1)).start();
    }
    
    @Test
    public void testShutdown() {
        // 关闭服务
        asyncLogService.shutdown();
        
        // 验证调用了ConsumerPool的shutdown方法
        verify(consumerPool).shutdown();
        
        // 再次关闭不应再调用
        asyncLogService.shutdown();
        verify(consumerPool, times(1)).shutdown();
    }
    
    @Test
    public void testLogWithLevel() {
        // 测试基本日志记录
        boolean result = asyncLogService.log("INFO", "测试消息");
        
        // 验证结果
        assertTrue(result);
        verify(eventFactory).createLogEvent("INFO", "测试消息");
        try {
            verify(eventQueue).offer(mockEvent);
        } catch (InterruptedException e) {
            fail("验证失败");
        }
    }
    
    @Test
    public void testLogWithContext() {
        // 准备上下文
        Map<String, String> context = new HashMap<>();
        context.put("key1", "value1");
        context.put("key2", "value2");
        
        // 记录带上下文的日志
        boolean result = asyncLogService.log("INFO", "测试消息", context);
        
        // 验证结果
        assertTrue(result);
        verify(eventFactory).createLogEvent("INFO", "测试消息", context);
        try {
            verify(eventQueue).offer(mockEvent);
        } catch (InterruptedException e) {
            fail("验证失败");
        }
    }
    
    @Test
    public void testLogWithException() {
        // 记录带异常的日志
        boolean result = asyncLogService.log("ERROR", "测试异常", "NullPointerException");
        
        // 验证结果
        assertTrue(result);
        verify(eventFactory).createLogEventWithException("ERROR", "测试异常", "NullPointerException");
        try {
            verify(eventQueue).offer(mockEvent);
        } catch (InterruptedException e) {
            fail("验证失败");
        }
    }
    
    @Test
    public void testConvenienceMethods() {
        // 测试便捷方法
        asyncLogService.info("信息日志");
        verify(eventFactory).createLogEvent("INFO", "信息日志");
        
        asyncLogService.error("错误日志");
        verify(eventFactory).createLogEvent("ERROR", "错误日志");
        
        asyncLogService.warn("警告日志");
        verify(eventFactory).createLogEvent("WARN", "警告日志");
        
        asyncLogService.debug("调试日志");
        verify(eventFactory).createLogEvent("DEBUG", "调试日志");
    }
    
    @Test
    public void testQueueRejection() {
        // 模拟队列已满
        try {
            when(eventQueue.offer(any(LogEvent.class))).thenReturn(false);
        } catch (InterruptedException e) {
            fail("Mock设置失败");
        }
        
        // 记录日志
        boolean result = asyncLogService.info("测试队列已满");
        
        // 验证结果
        assertFalse(result);
    }
    
    @Test
    public void testQueueInterruption() {
        // 模拟中断异常
        try {
            when(eventQueue.offer(any(LogEvent.class))).thenThrow(new InterruptedException("测试中断"));
        } catch (InterruptedException e) {
            fail("Mock设置失败");
        }
        
        // 记录日志
        boolean result = asyncLogService.info("测试中断");
        
        // 验证结果
        assertFalse(result);
        assertTrue(Thread.currentThread().isInterrupted());
        
        // 重置中断状态
        Thread.interrupted();
    }
    
    @Test
    public void testEventFactoryException() {
        // 模拟工厂异常
        when(eventFactory.createLogEvent(anyString(), anyString())).thenThrow(new RuntimeException("测试异常"));
        
        // 记录日志
        boolean result = asyncLogService.info("测试工厂异常");
        
        // 验证结果
        assertFalse(result);
    }
    
    @Test
    public void testServiceNotRunning() {
        // 关闭服务
        asyncLogService.shutdown();
        
        // 尝试记录日志
        boolean result = asyncLogService.info("服务已关闭");
        
        // 验证结果
        assertFalse(result);
        verify(eventFactory, never()).createLogEvent(anyString(), anyString());
    }
    
    @Test
    public void testStatusMethods() {
        // 模拟状态
        when(eventQueue.size()).thenReturn(10);
        when(consumerPool.getActiveCount()).thenReturn(2);
        
        // 验证结果
        assertEquals(10, asyncLogService.getQueueSize());
        assertEquals(2, asyncLogService.getActiveThreadCount());
    }
} 