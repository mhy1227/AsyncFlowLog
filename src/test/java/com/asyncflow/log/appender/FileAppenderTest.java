package com.asyncflow.log.appender;

import com.asyncflow.log.model.event.LogEvent;
import com.asyncflow.log.model.event.LogEventDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileAppender单元测试类
 */
public class FileAppenderTest {
    
    @TempDir
    Path tempDir;
    
    private FileAppender appender;
    private String testFilePath;
    private String testFileName;
    
    @BeforeEach
    public void setUp() {
        testFilePath = tempDir.toString();
        testFileName = "test-log-%s.log";
        appender = new FileAppender("TestAppender", testFilePath, testFileName);
    }
    
    @AfterEach
    public void tearDown() {
        if (appender.isInitialized()) {
            appender.close();
        }
    }
    
    @Test
    public void testInitialize() {
        // 测试初始化
        assertTrue(appender.initialize());
        assertTrue(appender.isInitialized());
        
        // 验证日志目录和文件是否创建
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String expectedFileName = String.format(testFileName, dateStr);
        File logFile = new File(testFilePath, expectedFileName);
        assertTrue(logFile.exists());
    }
    
    @Test
    public void testAppendSingle() throws Exception {
        // 初始化写入器
        appender.initialize();
        
        // 创建测试日志事件
        LogEvent event = new LogEventDTO("INFO", "测试单条日志消息");
        
        // 测试写入
        boolean result = appender.append(event);
        assertTrue(result);
        assertEquals(1, appender.getAppendCount());
        
        // 刷新确保写入磁盘
        appender.flush();
        
        // 验证日志文件内容
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String expectedFileName = String.format(testFileName, dateStr);
        Path logFilePath = Paths.get(testFilePath, expectedFileName);
        List<String> lines = Files.readAllLines(logFilePath);
        
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("INFO"));
        assertTrue(lines.get(0).contains("测试单条日志消息"));
    }
    
    @Test
    public void testAppendBatch() throws Exception {
        // 初始化写入器
        appender.initialize();
        
        // 创建测试日志事件列表
        List<LogEvent> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(new LogEventDTO("INFO", "测试批量日志消息 " + i));
        }
        
        // 测试批量写入
        int count = appender.append(events);
        assertEquals(5, count);
        assertEquals(5, appender.getAppendCount());
        
        // 刷新确保写入磁盘
        appender.flush();
        
        // 验证日志文件内容
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String expectedFileName = String.format(testFileName, dateStr);
        Path logFilePath = Paths.get(testFilePath, expectedFileName);
        List<String> lines = Files.readAllLines(logFilePath);
        
        assertEquals(5, lines.size());
        for (int i = 0; i < 5; i++) {
            assertTrue(lines.get(i).contains("INFO"));
            assertTrue(lines.get(i).contains("测试批量日志消息 " + i));
        }
    }
    
    @Test
    public void testCloseAndReopen() throws Exception {
        // 初始化写入器
        appender.initialize();
        
        // 写入一条日志
        LogEvent event = new LogEventDTO("INFO", "测试关闭前日志消息");
        appender.append(event);
        appender.flush();
        
        // 关闭写入器
        appender.close();
        assertFalse(appender.isInitialized());
        
        // 重新初始化写入器
        appender.initialize();
        assertTrue(appender.isInitialized());
        
        // 写入另一条日志
        event = new LogEventDTO("INFO", "测试重新打开后日志消息");
        appender.append(event);
        appender.flush();
        
        // 验证日志文件内容
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String expectedFileName = String.format(testFileName, dateStr);
        Path logFilePath = Paths.get(testFilePath, expectedFileName);
        List<String> lines = Files.readAllLines(logFilePath);
        
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("测试关闭前日志消息"));
        assertTrue(lines.get(1).contains("测试重新打开后日志消息"));
    }
    
    @Test
    public void testFormatLogEvent() throws Exception {
        // 初始化写入器
        appender.initialize();
        
        // 创建完整的测试日志事件
        LogEventDTO event = new LogEventDTO("ERROR", "测试异常日志消息");
        event.setThreadName("TestThread");
        event.withLocation("com.test.TestClass", "testMethod");
        event.withException("NullPointerException: 对象为空");
        
        // 写入日志
        appender.append(event);
        appender.flush();
        
        // 验证日志格式
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String expectedFileName = String.format(testFileName, dateStr);
        Path logFilePath = Paths.get(testFilePath, expectedFileName);
        List<String> lines = Files.readAllLines(logFilePath);
        
        String logLine = lines.get(0);
        assertTrue(logLine.contains("[TestThread]"));
        assertTrue(logLine.contains("ERROR"));
        assertTrue(logLine.contains("com.test.TestClass.testMethod"));
        assertTrue(logLine.contains("测试异常日志消息"));
        assertTrue(logLine.contains("NullPointerException"));
    }
} 