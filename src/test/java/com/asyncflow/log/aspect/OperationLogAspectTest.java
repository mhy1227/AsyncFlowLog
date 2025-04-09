package com.asyncflow.log.aspect;

import com.asyncflow.log.controller.TestOperationLogController;
import com.asyncflow.log.model.entity.OperationLogRecord;
import com.asyncflow.log.service.OperationLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 操作日志切面单元测试类
 */
@WebMvcTest(TestOperationLogController.class)
@Import(OperationLogAspectTest.TestConfig.class)
@ActiveProfiles("test")
public class OperationLogAspectTest {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspectTest.class);
    
    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class TestConfig {
        @Bean
        public OperationLogAspect operationLogAspect(OperationLogService operationLogService) {
            return new OperationLogAspect(operationLogService);
        }
        
        @Bean
        public TestOperationLogController testOperationLogController() {
            return new TestOperationLogController();
        }
    }
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OperationLogAspect operationLogAspect;
    
    @MockBean
    private OperationLogService operationLogService;
    
    @BeforeEach
    public void setup() {
        // 手动设置MockMvc以确保正确配置
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
        
        // 验证切面被正确创建
        assertNotNull(operationLogAspect, "OperationLogAspect不应为null");
        logger.info("OperationLogAspect已成功创建: {}", operationLogAspect);
        
        // 重置模拟对象
        reset(operationLogService);
    }
    
    @Test
    public void testGetRequestLogging() throws Exception {
        // 手动设置RequestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("param", "testValue");
        request.setServletPath("/api/test/log");
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        logger.info("开始执行GET请求测试...");
        
        // 执行GET请求
        mockMvc.perform(get("/api/test/log")
                .param("param", "testValue")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        logger.info("GET请求执行完成，等待验证日志记录...");
        
        // 验证日志服务的asyncSave方法被调用
        verify(operationLogService, timeout(5000)).asyncSave(any(OperationLogRecord.class));
        
        // 捕获传递给asyncSave方法的参数
        ArgumentCaptor<OperationLogRecord> logCaptor = ArgumentCaptor.forClass(OperationLogRecord.class);
        verify(operationLogService).asyncSave(logCaptor.capture());
        
        // 验证日志记录的内容
        OperationLogRecord capturedLog = logCaptor.getValue();
        logger.info("捕获到的日志记录: {}", capturedLog);
        
        assertEquals("测试GET操作", capturedLog.getDescription());
        assertEquals("QUERY", capturedLog.getOperationType());
        assertEquals(0, capturedLog.getStatus()); // 成功状态
        assertNotNull(capturedLog.getRequestParams()); // 请求参数应该被记录
        
        // 清理
        RequestContextHolder.resetRequestAttributes();
    }
    
    @Test
    public void testPostRequestLogging() throws Exception {
        // 手动设置RequestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/test/log");
        request.setMethod("POST");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        logger.info("开始执行POST请求测试...");
        
        // 执行POST请求
        String requestBody = "{\"name\":\"测试名称\",\"value\":123}";
        mockMvc.perform(post("/api/test/log")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        logger.info("POST请求执行完成，等待验证日志记录...");
        
        // 验证日志服务的asyncSave方法被调用
        verify(operationLogService, timeout(5000)).asyncSave(any(OperationLogRecord.class));
        
        // 捕获传递给asyncSave方法的参数
        ArgumentCaptor<OperationLogRecord> logCaptor = ArgumentCaptor.forClass(OperationLogRecord.class);
        verify(operationLogService).asyncSave(logCaptor.capture());
        
        // 验证日志记录的内容
        OperationLogRecord capturedLog = logCaptor.getValue();
        logger.info("捕获到的日志记录: {}", capturedLog);
        
        assertEquals("测试POST操作", capturedLog.getDescription());
        assertEquals("CREATE", capturedLog.getOperationType());
        assertEquals(0, capturedLog.getStatus()); // 成功状态
        assertNotNull(capturedLog.getRequestParams()); // 请求参数应该被记录
        assertNotNull(capturedLog.getResult()); // 返回结果应该被记录
        
        // 清理
        RequestContextHolder.resetRequestAttributes();
    }
    
    @Test
    public void testErrorLogging() throws Exception {
        // 手动设置RequestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/test/error");
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        logger.info("开始执行错误处理测试...");
        
        try {
            // 执行会导致异常的请求
            mockMvc.perform(get("/api/test/error")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError());
        } catch (Exception e) {
            logger.info("预期内的异常已捕获: {}", e.getMessage());
            // 忽略异常，因为我们期望它发生
        }
        
        logger.info("错误请求执行完成，等待验证日志记录...");
        
        // 验证日志服务的asyncSave方法被调用
        verify(operationLogService, timeout(5000)).asyncSave(any(OperationLogRecord.class));
        
        // 捕获传递给asyncSave方法的参数
        ArgumentCaptor<OperationLogRecord> logCaptor = ArgumentCaptor.forClass(OperationLogRecord.class);
        verify(operationLogService).asyncSave(logCaptor.capture());
        
        // 验证日志记录的内容
        OperationLogRecord capturedLog = logCaptor.getValue();
        logger.info("捕获到的日志记录: {}", capturedLog);
        
        assertEquals("测试异常操作", capturedLog.getDescription());
        assertEquals("ERROR", capturedLog.getOperationType());
        assertEquals(1, capturedLog.getStatus()); // 失败状态
        assertNotNull(capturedLog.getErrorMessage()); // 异常信息应该被记录
        
        // 清理
        RequestContextHolder.resetRequestAttributes();
    }
} 