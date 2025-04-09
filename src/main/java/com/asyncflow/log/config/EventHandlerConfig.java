package com.asyncflow.log.config;

import com.asyncflow.log.appender.AppenderFactory;
import com.asyncflow.log.appender.LogAppender;
import com.asyncflow.log.appender.LogEventHandler;
import com.asyncflow.log.consumer.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 事件处理器配置类
 * 用于提供事件处理器实例和相关配置
 */
@Slf4j
@Configuration
public class EventHandlerConfig {
    
    @Autowired
    private LogAppender logAppender;
    
    @Autowired
    private AppenderFactory appenderFactory;
    
    /**
     * 创建并注册事件处理器实例
     * 
     * @return 事件处理器实例
     */
    @Bean
    public EventHandler eventHandler() {
        log.info("初始化事件处理器");
        return new LogEventHandler(logAppender, appenderFactory.getBatchSize());
    }
} 