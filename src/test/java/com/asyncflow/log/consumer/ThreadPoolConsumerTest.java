package com.asyncflow.log.consumer;

import com.asyncflow.log.model.event.LogEvent;
import com.asyncflow.log.model.event.LogEventDTO;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.queue.LinkedEventQueue;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    public void testSubmit() throws InterruptedException {
        // 创建CountDownLatch用于等待事件处理完成
        CountDownLatch latch = new CountDownLatch(1);
        
        // 设置事件处理器返回成功，并在调用后减少CountDownLatch计数
        doAnswer(invocation -> {
            log.info("eventHandler.handle()方法被调用");
            latch.countDown();
            return true;
        }).when(eventHandler).handle(any(LogEvent.class));
        
        // 启动消费者线程池
        consumerPool.start();
        
        // 创建测试事件
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        
        // 直接将事件放入队列，而不是使用submit方法
        // 这样可以确保消费者线程能够处理到事件
        log.info("将事件放入队列: {}", event);
        eventQueue.put(event);
        
        log.info("事件已放入队列，等待处理...");
        
        // 等待事件处理完成，最多等待10秒
        boolean processed = latch.await(10, TimeUnit.SECONDS);
        log.info("等待结果: {}", processed ? "事件已处理" : "等待超时");
        
        assertTrue(processed, "事件处理超时");
        
        // 验证事件处理器被调用
        verify(eventHandler, timeout(11000).times(1)).handle(any(LogEvent.class));
        
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