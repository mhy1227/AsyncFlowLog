package com.asyncflow.log.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 调度器配置（仅用于维护类定时任务）。
 * 默认仅注册一个独立的调度线程池，避免干扰日志消费线程池。
 */
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    /**
     * 专用调度线程池（1-2 线程足够）。
     */
    @Bean(name = "asyncLogTaskScheduler")
    public ThreadPoolTaskScheduler asyncLogTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("async-log-maintenance-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 让 @Scheduled 使用上面的专用调度器，避免与默认调度器耦合。
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(asyncLogTaskScheduler());
    }
}

