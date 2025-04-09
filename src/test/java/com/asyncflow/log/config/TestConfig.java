package com.asyncflow.log.config;

import com.asyncflow.log.aspect.OperationLogAspect;
import com.asyncflow.log.consumer.ConsumerPool;
import com.asyncflow.log.consumer.EventHandler;
import com.asyncflow.log.controller.TestOperationLogController;
import com.asyncflow.log.mapper.OperationLogMapper;
import com.asyncflow.log.model.event.LogEventFactory;
import com.asyncflow.log.queue.EventQueue;
import com.asyncflow.log.queue.LinkedEventQueue;
import com.asyncflow.log.service.AsyncLogService;
import com.asyncflow.log.service.OperationLogService;
import com.asyncflow.log.service.impl.AsyncLogServiceImpl;
import com.asyncflow.log.util.MemoryEventHandler;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 测试专用配置类
 * 提供测试环境所需的各种模拟Bean
 */
@TestConfiguration
@Profile("test")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableWebMvc
public class TestConfig {
    
    /**
     * 提供测试控制器
     */
    @Bean
    public TestOperationLogController testOperationLogController() {
        return new TestOperationLogController();
    }
    
    /**
     * 提供测试用的事件队列
     */
    @Bean
    @Primary
    public EventQueue eventQueue() {
        return new LinkedEventQueue(10);
    }
    
    /**
     * 提供测试用的事件处理器
     */
    @Bean
    @Primary
    public EventHandler eventHandler() {
        return new MemoryEventHandler();
    }
    
    /**
     * 提供测试用的消费者线程池
     */
    @Bean
    @Primary
    public ConsumerPool consumerPool(EventHandler eventHandler, EventQueue eventQueue) {
        ConsumerPool pool = Mockito.mock(ConsumerPool.class);
        Mockito.when(pool.getEventHandler()).thenReturn(eventHandler);
        Mockito.when(pool.getEventQueue()).thenReturn(eventQueue);
        return pool;
    }
    
    /**
     * 提供测试用的事件工厂
     */
    @Bean
    @Primary
    public LogEventFactory logEventFactory() {
        return Mockito.mock(LogEventFactory.class);
    }
    
    /**
     * 提供测试用的操作日志Mapper
     */
    @Bean
    @Primary
    public OperationLogMapper operationLogMapper() {
        return Mockito.mock(OperationLogMapper.class);
    }
    
    /**
     * 提供测试用的异步日志服务
     */
    @Bean
    @Primary
    public AsyncLogService asyncLogService() {
        return Mockito.mock(AsyncLogService.class);
    }
    
    /**
     * 提供测试用的操作日志服务
     */
    @Bean
    @Primary
    public OperationLogService operationLogService() {
        return Mockito.mock(OperationLogService.class);
    }
    
    /**
     * 提供OperationLogAspect的Bean实例
     */
    @Bean
    public OperationLogAspect operationLogAspect(OperationLogService operationLogService) {
        return new OperationLogAspect(operationLogService);
    }
} 