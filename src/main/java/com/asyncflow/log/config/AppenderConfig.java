package com.asyncflow.log.config;

import com.asyncflow.log.appender.AppenderFactory;
import com.asyncflow.log.appender.LogAppender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 日志写入器配置类
 * 用于提供日志写入器实例和相关配置
 */
@Slf4j
@Configuration
public class AppenderConfig {
    
    @Autowired
    private AppenderFactory appenderFactory;
    
    /**
     * 创建并注册日志写入器实例
     * 
     * @return 日志写入器实例
     */
    @Bean(destroyMethod = "close")
    public LogAppender logAppender() {
        log.info("初始化日志写入器");
        LogAppender appender = appenderFactory.createAppender();
        
        // 初始化写入器
        if (!appender.initialize()) {
            log.error("初始化日志写入器失败");
            throw new RuntimeException("初始化日志写入器失败");
        }
        
        return appender;
    }
} 