package com.asyncflow.log;

import com.asyncflow.log.service.AsyncLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 异步日志服务工厂类
 * 提供获取异步日志服务实例的静态方法
 */
@Slf4j
@Component
public class AsyncLogServiceFactory implements ApplicationContextAware {
    
    /**
     * Spring应用上下文
     */
    private static ApplicationContext applicationContext;
    
    /**
     * 默认的异步日志服务实例
     */
    private static AsyncLogService defaultInstance;
    
    /**
     * 获取默认的异步日志服务实例
     * @return 异步日志服务实例
     */
    public static AsyncLogService getDefault() {
        if (defaultInstance == null) {
            synchronized (AsyncLogServiceFactory.class) {
                if (defaultInstance == null) {
                    if (applicationContext != null) {
                        defaultInstance = applicationContext.getBean(AsyncLogService.class);
                    } else {
                        log.error("ApplicationContext未初始化，无法获取AsyncLogService实例");
                        throw new IllegalStateException("ApplicationContext未初始化");
                    }
                }
            }
        }
        return defaultInstance;
    }
    
    /**
     * 记录ERROR级别日志
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    public static boolean error(String message) {
        return getDefault().error(message);
    }
    
    /**
     * 记录ERROR级别日志，带异常信息
     * @param message 日志消息
     * @param exception 异常信息
     * @return 是否成功提交到队列
     */
    public static boolean error(String message, String exception) {
        return getDefault().error(message, exception);
    }
    
    /**
     * 记录ERROR级别日志，带异常信息
     * @param message 日志消息
     * @param throwable 异常对象
     * @return 是否成功提交到队列
     */
    public static boolean error(String message, Throwable throwable) {
        return getDefault().error(message, throwable != null ? throwable.toString() : null);
    }
    
    /**
     * 记录ERROR级别日志，带上下文信息
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    public static boolean error(String message, Map<String, String> context) {
        return getDefault().error(message, context);
    }
    
    /**
     * 记录WARN级别日志
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    public static boolean warn(String message) {
        return getDefault().warn(message);
    }
    
    /**
     * 记录WARN级别日志，带上下文信息
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    public static boolean warn(String message, Map<String, String> context) {
        return getDefault().warn(message, context);
    }
    
    /**
     * 记录INFO级别日志
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    public static boolean info(String message) {
        return getDefault().info(message);
    }
    
    /**
     * 记录INFO级别日志，带上下文信息
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    public static boolean info(String message, Map<String, String> context) {
        return getDefault().info(message, context);
    }
    
    /**
     * 记录DEBUG级别日志
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    public static boolean debug(String message) {
        return getDefault().debug(message);
    }
    
    /**
     * 记录DEBUG级别日志，带上下文信息
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    public static boolean debug(String message, Map<String, String> context) {
        return getDefault().debug(message, context);
    }
    
    /**
     * 记录指定级别的日志
     * @param level 日志级别
     * @param message 日志消息
     * @return 是否成功提交到队列
     */
    public static boolean log(String level, String message) {
        return getDefault().log(level, message);
    }
    
    /**
     * 记录指定级别的日志，带上下文信息
     * @param level 日志级别
     * @param message 日志消息
     * @param context 上下文信息
     * @return 是否成功提交到队列
     */
    public static boolean log(String level, String message, Map<String, String> context) {
        return getDefault().log(level, message, context);
    }
    
    /**
     * 创建上下文构建器
     * @return 上下文构建器
     */
    public static ContextBuilder context() {
        return new ContextBuilder();
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AsyncLogServiceFactory.applicationContext = applicationContext;
    }
    
    /**
     * 上下文构建器
     * 用于构建日志上下文信息
     */
    public static class ContextBuilder {
        private final Map<String, String> context = new HashMap<>();
        
        /**
         * 添加上下文项
         * @param key 键
         * @param value 值
         * @return 上下文构建器
         */
        public ContextBuilder add(String key, String value) {
            context.put(key, value);
            return this;
        }
        
        /**
         * 添加上下文项（对象值）
         * @param key 键
         * @param value 值对象
         * @return 上下文构建器
         */
        public ContextBuilder add(String key, Object value) {
            context.put(key, value != null ? value.toString() : "null");
            return this;
        }
        
        /**
         * 获取构建的上下文
         * @return 上下文Map
         */
        public Map<String, String> build() {
            return new HashMap<>(context);
        }
        
        /**
         * 记录ERROR级别日志
         * @param message 日志消息
         * @return 是否成功提交到队列
         */
        public boolean error(String message) {
            return getDefault().error(message, context);
        }
        
        /**
         * 记录WARN级别日志
         * @param message 日志消息
         * @return 是否成功提交到队列
         */
        public boolean warn(String message) {
            return getDefault().warn(message, context);
        }
        
        /**
         * 记录INFO级别日志
         * @param message 日志消息
         * @return 是否成功提交到队列
         */
        public boolean info(String message) {
            return getDefault().info(message, context);
        }
        
        /**
         * 记录DEBUG级别日志
         * @param message 日志消息
         * @return 是否成功提交到队列
         */
        public boolean debug(String message) {
            return getDefault().debug(message, context);
        }
        
        /**
         * 记录指定级别的日志
         * @param level 日志级别
         * @param message 日志消息
         * @return 是否成功提交到队列
         */
        public boolean log(String level, String message) {
            return getDefault().log(level, message, context);
        }
    }
} 