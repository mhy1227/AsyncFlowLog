package com.asyncflow.log.config;

import com.asyncflow.log.service.AsyncLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

/**
 * 异步日志服务配置类
 * 用于配置异步日志服务及相关参数
 */
@Slf4j
@Configuration
public class AsyncLogServiceConfig {
    
    @Value("${async.log.service.enabled:true}")
    private boolean serviceEnabled;
    
    @Value("${async.log.service.auto-start:true}")
    private boolean autoStart;
    
    @Autowired
    private AsyncLogService asyncLogService;
    
    @PostConstruct
    public void init() {
        log.info("异步日志服务配置初始化");
        log.info("服务启用状态: {}", serviceEnabled);
        log.info("自动启动状态: {}", autoStart);
        
        if (serviceEnabled && autoStart) {
            asyncLogService.start();
            log.info("异步日志服务已启动");
        } else {
            log.info("异步日志服务未自动启动，需手动调用start()方法启动");
        }
    }
    
    /**
     * 注册服务关闭钩子，确保应用关闭时日志服务正常关闭
     */
    @Bean
    public Object asyncLogServiceShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("应用关闭，关闭异步日志服务");
            asyncLogService.flush();
            asyncLogService.shutdown();
        }));
        return new Object();
    }
} 