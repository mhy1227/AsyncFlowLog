package com.asyncflow.log.consumer;

import com.asyncflow.log.model.event.LogEvent;
import com.asyncflow.log.model.event.LogEventDTO;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.queue.LinkedEventQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ThreadPoolConsumer单元测试类
 */
public class ThreadPoolConsumerTest {
    
    private ConsumerPool consumerPool;
    private EventQueue eventQueue;
    private EventHandler eventHandler;
    
    @BeforeEach
    public void setUp() {
        consumerPool = new ThreadPoolConsumer(2, 4, 60);
        eventQueue = new LinkedEventQueue(10);
        eventHandler = Mockito.mock(EventHandler.class);
        
        consumerPool.setEventQueue(eventQueue);
        consumerPool.setEventHandler(eventHandler);
    }
    
    @Test
    public void testStart() {
        consumerPool.start();
        
        verify(eventHandler, times(1)).initialize();
        assertTrue(((ThreadPoolConsumer) consumerPool).isShutdown() == false);
        
        // 清理
        consumerPool.shutdown();
    }
    
    @Test
    public void testShutdown() {
        consumerPool.start();
        consumerPool.shutdown();
        
        verify(eventHandler, times(1)).close();
        assertTrue(consumerPool.isShutdown());
    }
    
    @Test
    public void testSubmit() {
        // 设置事件处理器返回成功
        when(eventHandler.handle(any(LogEvent.class))).thenReturn(true);
        
        // 启动消费者线程池
        consumerPool.start();
        
        // 提交日志事件
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        boolean result = consumerPool.submit(event);
        
        assertTrue(result);
        
        // 等待一段时间确保事件被处理
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 验证事件处理器被调用
        verify(eventHandler, atLeastOnce()).handle(any(LogEvent.class));
        
        // 清理
        consumerPool.shutdown();
    }
    
    @Test
    public void testConsumerTask() throws InterruptedException {
        // 设置事件处理器返回成功
        when(eventHandler.handle(any(LogEvent.class))).thenReturn(true);
        
        // 创建CountDownLatch用于等待事件处理完成
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return true;
        }).when(eventHandler).handle(any(LogEvent.class));
        
        // 启动消费者线程池
        consumerPool.start();
        
        // 添加事件到队列
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        eventQueue.put(event);
        
        // 等待事件处理完成
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        
        // 验证事件处理器被调用
        verify(eventHandler, times(1)).handle(event);
        
        // 清理
        consumerPool.shutdown();
    }
    
    @Test
    public void testHandleException() throws InterruptedException {
        // 设置事件处理器抛出异常
        when(eventHandler.handle(any(LogEvent.class))).thenThrow(new RuntimeException("测试异常"));
        
        // 创建CountDownLatch用于等待异常处理完成
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(eventHandler).handleException(any(LogEvent.class), any(Throwable.class));
        
        // 启动消费者线程池
        consumerPool.start();
        
        // 添加事件到队列
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        eventQueue.put(event);
        
        // 等待异常处理完成
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        
        // 验证异常处理器被调用
        verify(eventHandler, times(1)).handleException(any(LogEvent.class), any(Throwable.class));
        
        // 清理
        consumerPool.shutdown();
    }
    
    @Test
    public void testGetters() {
        assertEquals(eventQueue, consumerPool.getEventQueue());
        assertEquals(eventHandler, consumerPool.getEventHandler());
    }
} 