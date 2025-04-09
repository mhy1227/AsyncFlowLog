package com.asyncflow.log.controller;

import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.monitor.AsyncLogMetrics;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.service.AsyncLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 异步日志监控控制器测试类
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AsyncLogMonitorControllerTest {
        
    @Mock
    private AsyncLogService asyncLogService;
    
    @Mock
    private EventQueue eventQueue;
    
    @Mock
    private ConsumerPool consumerPool;
    
    @Mock
    private AsyncLogMetrics asyncLogMetrics;
    
    @InjectMocks
    private AsyncLogMonitorController controller;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    public void setUp() {
        // 设置基本模拟行为
        when(asyncLogService.isRunning()).thenReturn(true);
        when(eventQueue.capacity()).thenReturn(1000);
        when(eventQueue.size()).thenReturn(200); // 20% 使用率
        when(consumerPool.getActiveCount()).thenReturn(2);
        when(consumerPool.isShutdown()).thenReturn(false);
        when(consumerPool.getCompletedTaskCount()).thenReturn(10000L);
        
        // 配置AsyncLogMetrics
        when(asyncLogMetrics.getSuccessCount()).thenReturn(800L);
        when(asyncLogMetrics.getFailureCount()).thenReturn(200L);
        when(asyncLogMetrics.getDiscardedCount()).thenReturn(50L);
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }
    
    @Test
    public void testGetStatus() {
        ResponseEntity<Map<String, Object>> response = controller.getStatus();
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCodeValue());
        
        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        
        // 验证服务状态
        assertEquals(true, body.get("service_running"));
        
        // 验证队列状态
        Map<String, Object> queueStats = (Map<String, Object>) body.get("queue");
        assertNotNull(queueStats);
        assertEquals(200, queueStats.get("size"));
        assertEquals(1000, queueStats.get("capacity"));
        assertEquals("20.00%", queueStats.get("usage"));
        
        // 验证消费者状态
        Map<String, Object> consumerStats = (Map<String, Object>) body.get("consumer");
        assertNotNull(consumerStats);
        assertEquals(2, consumerStats.get("active_threads"));
        assertEquals(10000L, consumerStats.get("completed_tasks"));
        assertEquals(false, consumerStats.get("is_shutdown"));
    }
    
    @Test
    public void testGetMetrics() {
        ResponseEntity<Map<String, Object>> response = controller.getMetrics();
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCodeValue());
        
        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        
        // 验证事件处理指标
        Map<String, Object> eventStats = (Map<String, Object>) body.get("events");
        assertNotNull(eventStats);
        assertEquals(800L, eventStats.get("success_count"));
        assertEquals(200L, eventStats.get("failure_count"));
        assertEquals(50L, eventStats.get("discarded_count"));
        assertEquals("80.00%", eventStats.get("success_rate"));
    }
    
    @Test
    public void testGetMetricsWithoutMetrics() {
        // 创建新的控制器实例，未注入AsyncLogMetrics
        AsyncLogMonitorController noMetricsController = new AsyncLogMonitorController();
        
        // 使用反射注入依赖，但不包括AsyncLogMetrics
        ReflectionTestUtils.setField(noMetricsController, "asyncLogService", asyncLogService);
        ReflectionTestUtils.setField(noMetricsController, "eventQueue", eventQueue);
        ReflectionTestUtils.setField(noMetricsController, "consumerPool", consumerPool);
        
        ResponseEntity<Map<String, Object>> response = noMetricsController.getMetrics();
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCodeValue());
        
        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("指标监控未启用", body.get("message"));
    }
    
    @Test
    public void testGetInfo() {
        ResponseEntity<Map<String, Object>> response = controller.getInfo();
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCodeValue());
        
        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        
        // 验证内存信息
        Map<String, Object> memoryInfo = (Map<String, Object>) body.get("memory");
        assertNotNull(memoryInfo);
        assertNotNull(memoryInfo.get("max"));
        assertNotNull(memoryInfo.get("total"));
        assertNotNull(memoryInfo.get("free"));
        assertNotNull(memoryInfo.get("used"));
        assertNotNull(memoryInfo.get("usage"));
        
        // 验证线程信息
        Map<String, Object> threadInfo = (Map<String, Object>) body.get("threads");
        assertNotNull(threadInfo);
        assertTrue(((Integer) threadInfo.get("count")) > 0);
        assertEquals(2, threadInfo.get("consumer_threads"));
    }
    
    @Test
    public void testMvcEndpoints() throws Exception {
        // 测试/status端点
        mockMvc.perform(get("/api/asynclog/monitor/status"))
            .andExpect(status().isOk());
        
        // 测试/metrics端点
        mockMvc.perform(get("/api/asynclog/monitor/metrics"))
            .andExpect(status().isOk());
        
        // 测试/info端点
        mockMvc.perform(get("/api/asynclog/monitor/info"))
            .andExpect(status().isOk());
    }
} 

