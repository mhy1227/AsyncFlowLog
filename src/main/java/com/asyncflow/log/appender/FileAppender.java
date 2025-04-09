package com.asyncflow.log.appender;

import com.asyncflow.log.model.event.LogEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 文件日志写入器
 * 实现将日志事件写入文件的功能
 */
@Slf4j
public class FileAppender extends AbstractLogAppender {
    
    /**
     * 文件类型标识
     */
    private static final String FILE_TYPE = "file";
    
    /**
     * 默认日期格式
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 默认时间格式
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 默认文件名格式
     */
    private static final String DEFAULT_FILE_NAME_PATTERN = "async-log-%s.log";
    
    /**
     * 默认文件路径
     */
    private static final String DEFAULT_FILE_PATH = "logs";
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件名模式
     */
    private String fileNamePattern;
    
    /**
     * 当前文件名
     */
    private String currentFileName;
    
    /**
     * 当前日期
     */
    private LocalDate currentDate;
    
    /**
     * 文件写入器
     */
    private BufferedWriter writer;
    
    /**
     * 锁
     */
    private final ReentrantLock lock = new ReentrantLock();
    
    /**
     * 是否自动刷新
     */
    private boolean autoFlush = false;
    
    /**
     * 默认构造函数
     */
    public FileAppender() {
        this("FileAppender", DEFAULT_FILE_PATH, DEFAULT_FILE_NAME_PATTERN);
    }
    
    /**
     * 带参数的构造函数
     * @param name 写入器名称
     * @param filePath 文件路径
     * @param fileNamePattern 文件名模式
     */
    public FileAppender(String name, String filePath, String fileNamePattern) {
        super(name, FILE_TYPE);
        this.filePath = filePath;
        this.fileNamePattern = fileNamePattern;
        this.currentDate = LocalDate.now();
    }
    
    @Override
    protected boolean doInitialize() {
        try {
            // 确保目录存在
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建日志目录: {}", filePath);
            }
            
            // 创建当前文件名
            refreshCurrentFileName();
            
            // 创建写入器
            openWriter();
            
            log.info("文件日志写入器初始化成功: {}", currentFileName);
            return true;
        } catch (IOException e) {
            log.error("初始化文件日志写入器失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    protected void doClose() {
        lock.lock();
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
                writer = null;
                log.info("关闭文件日志写入器: {}", currentFileName);
            }
        } catch (IOException e) {
            log.error("关闭文件日志写入器失败: {}", e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    protected boolean doAppend(LogEvent event) throws Exception {
        checkRotation();
        
        String logText = formatLogEvent(event);
        
        lock.lock();
        try {
            writer.write(logText);
            writer.newLine();
            
            if (autoFlush) {
                writer.flush();
            }
            
            return true;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    protected int doAppendBatch(List<LogEvent> events) throws Exception {
        checkRotation();
        
        int count = 0;
        lock.lock();
        try {
            for (LogEvent event : events) {
                String logText = formatLogEvent(event);
                writer.write(logText);
                writer.newLine();
                count++;
            }
            
            if (autoFlush) {
                writer.flush();
            }
            
            return count;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void flush() {
        if (!isInitialized()) {
            return;
        }
        
        lock.lock();
        try {
            if (writer != null) {
                writer.flush();
                log.debug("刷新文件日志写入器: {}", currentFileName);
            }
        } catch (IOException e) {
            log.error("刷新文件日志写入器失败: {}", e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 检查是否需要轮转日志文件
     * @throws IOException IO异常
     */
    private void checkRotation() throws IOException {
        LocalDate today = LocalDate.now();
        if (!today.equals(currentDate)) {
            lock.lock();
            try {
                // 再次检查，避免多线程问题
                if (!today.equals(currentDate)) {
                    // 关闭当前写入器
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                    
                    // 更新日期和文件名
                    currentDate = today;
                    refreshCurrentFileName();
                    
                    // 创建新的写入器
                    openWriter();
                    
                    log.info("轮转日志文件: {}", currentFileName);
                }
            } finally {
                lock.unlock();
            }
        }
    }
    
    /**
     * 刷新当前文件名
     */
    private void refreshCurrentFileName() {
        String dateStr = currentDate.format(DATE_FORMATTER);
        currentFileName = String.format(fileNamePattern, dateStr);
    }
    
    /**
     * 打开写入器
     * @throws IOException IO异常
     */
    private void openWriter() throws IOException {
        String fullPath = filePath + File.separator + currentFileName;
        File file = new File(fullPath);
        
        // 如果文件所在目录不存在，则创建
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        
        // 创建写入器，以追加模式打开文件
        writer = new BufferedWriter(new FileWriter(file, true));
        log.info("打开日志文件: {}", fullPath);
    }
    
    /**
     * 格式化日志事件
     * @param event 日志事件
     * @return 格式化后的日志文本
     */
    private String formatLogEvent(LogEvent event) {
        StringBuilder sb = new StringBuilder();
        
        // 添加时间戳
        sb.append(event.getTimestamp().format(TIME_FORMATTER));
        sb.append(" [");
        
        // 添加线程名称
        String threadName = event.getThreadName();
        if (threadName == null || threadName.isEmpty()) {
            threadName = Thread.currentThread().getName();
        }
        sb.append(threadName);
        sb.append("] ");
        
        // 添加日志级别
        sb.append(event.getLevel());
        sb.append(" ");
        
        // 添加位置信息
        if (event.getClassName() != null && event.getMethodName() != null) {
            sb.append(event.getClassName());
            sb.append(".");
            sb.append(event.getMethodName());
            sb.append(" - ");
        }
        
        // 添加日志消息
        sb.append(event.getMessage());
        
        // 添加异常信息
        if (event.getException() != null) {
            sb.append(" - Exception: ");
            sb.append(event.getException());
        }
        
        return sb.toString();
    }
    
    /**
     * 设置文件路径
     * @param filePath 文件路径
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * 获取文件路径
     * @return 文件路径
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * 设置文件名模式
     * @param fileNamePattern 文件名模式
     */
    public void setFileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
    }
    
    /**
     * 获取文件名模式
     * @return 文件名模式
     */
    public String getFileNamePattern() {
        return fileNamePattern;
    }
    
    /**
     * 设置是否自动刷新
     * @param autoFlush 是否自动刷新
     */
    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }
    
    /**
     * 获取是否自动刷新
     * @return 是否自动刷新
     */
    public boolean isAutoFlush() {
        return autoFlush;
    }
} 