package com.asyncflow.log.queue;

import com.asyncflow.log.model.event.LogEvent;
import com.asyncflow.log.model.event.LogEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LinkedEventQueue单元测试类
 */
public class LinkedEventQueueTest {
    
    private EventQueue eventQueue;
    private final int CAPACITY = 10;
    
    @BeforeEach
    public void setUp() {
        eventQueue = new LinkedEventQueue(CAPACITY);
    }
    
    @Test
    public void testOffer() throws InterruptedException {
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        
        // 测试添加成功
        boolean result = eventQueue.offer(event);
        assertTrue(result);
        assertEquals(1, eventQueue.size());
        
        // 测试添加null
        result = eventQueue.offer(null);
        assertFalse(result);
        assertEquals(1, eventQueue.size());
    }
    
    @Test
    public void testPut() throws InterruptedException {
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        
        // 测试添加成功
        eventQueue.put(event);
        assertEquals(1, eventQueue.size());
        
        // 测试添加null
        eventQueue.put(null);
        assertEquals(1, eventQueue.size());
    }
    
    @Test
    public void testPoll() throws InterruptedException {
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        eventQueue.put(event);
        
        // 测试获取成功
        LogEvent result = eventQueue.poll();
        assertNotNull(result);
        assertEquals("INFO", result.getLevel());
        assertEquals("测试消息", result.getMessage());
        assertEquals(0, eventQueue.size());
        
        // 测试队列为空
        result = eventQueue.poll();
        assertNull(result);
    }
    
    @Test
    public void testTake() throws InterruptedException {
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        eventQueue.put(event);
        
        // 测试获取成功
        LogEvent result = eventQueue.take();
        assertNotNull(result);
        assertEquals("INFO", result.getLevel());
        assertEquals("测试消息", result.getMessage());
        assertEquals(0, eventQueue.size());
    }
    
    @Test
    public void testCapacityAndSize() throws InterruptedException {
        assertEquals(CAPACITY, eventQueue.capacity());
        assertEquals(0, eventQueue.size());
        
        // 填充队列
        for (int i = 0; i < CAPACITY; i++) {
            LogEvent event = new LogEventDTO("INFO", "测试消息" + i);
            eventQueue.put(event);
        }
        
        assertEquals(CAPACITY, eventQueue.size());
        assertTrue(eventQueue.isFull());
    }
    
    @Test
    public void testIsEmptyAndIsFull() throws InterruptedException {
        assertTrue(eventQueue.isEmpty());
        assertFalse(eventQueue.isFull());
        
        // 添加一个事件
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        eventQueue.put(event);
        
        assertFalse(eventQueue.isEmpty());
        assertFalse(eventQueue.isFull());
        
        // 填充队列
        for (int i = 1; i < CAPACITY; i++) {
            event = new LogEventDTO("INFO", "测试消息" + i);
            eventQueue.put(event);
        }
        
        assertFalse(eventQueue.isEmpty());
        assertTrue(eventQueue.isFull());
        
        // 清空队列
        eventQueue.clear();
        assertTrue(eventQueue.isEmpty());
        assertFalse(eventQueue.isFull());
    }
    
    @Test
    public void testGetUsage() throws InterruptedException {
        assertEquals(0.0, eventQueue.getUsage(), 0.01);
        
        // 添加一个事件
        LogEvent event = new LogEventDTO("INFO", "测试消息");
        eventQueue.put(event);
        
        assertEquals(0.1, eventQueue.getUsage(), 0.01);
        
        // 填充队列
        for (int i = 1; i < CAPACITY; i++) {
            event = new LogEventDTO("INFO", "测试消息" + i);
            eventQueue.put(event);
        }
        
        assertEquals(1.0, eventQueue.getUsage(), 0.01);
    }
} 