package com.asyncflow.log.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * JSON工具类，用于对象和JSON字符串的转换
 */
@Slf4j
public class JsonUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // 注册Java8时间模块，支持LocalDateTime等类型
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    private JsonUtils() {
        // 工具类不允许实例化
    }
    
    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 需要转换的对象
     * @return JSON字符串，转换失败时返回null
     */
    public static String objectToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("将对象转换为JSON时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为对象
     *
     * @param jsonString JSON字符串
     * @param clazz      目标类
     * @param <T>        目标类型
     * @return 转换后的对象，转换失败时返回null
     */
    public static <T> T jsonToObject(String jsonString, Class<T> clazz) {
        if (!StringUtils.hasText(jsonString)) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            log.error("将JSON转换为对象时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 将对象转换为JSON字符串（兼容旧方法名）
     *
     * @param object 需要转换的对象
     * @return JSON字符串
     * @throws RuntimeException 转换失败时抛出异常
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            throw new RuntimeException("JSON序列化失败", e);
        }
    }
    
    /**
     * 将JSON字符串转换为对象（兼容旧方法名）
     *
     * @param json  JSON字符串
     * @param clazz 目标类
     * @param <T>   目标类型
     * @return 转换后的对象
     * @throws RuntimeException 转换失败时抛出异常
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }
}