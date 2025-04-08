# AsyncFlowLog 技术栈分析

## 基础框架选择

### Spring Boot 2.7.x
- 原因：
  - 成熟的微服务框架
  - 丰富的生态系统
  - 良好的自动配置能力
  - 完善的文档和社区支持
  - 内置监控和健康检查
  - 支持多种配置方式

### JDK 版本
- 选择 JDK 8
- 原因：
  - 企业级应用最稳定的版本
  - 兼容性好
  - Lambda 表达式支持
  - 并发包完善
  - 与 Spring Boot 2.7.x 完美匹配

## 核心依赖

### 1. 基础依赖
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    
    <!-- Web 支持（用于管理接口） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- MyBatis 支持 -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.3.1</version>
    </dependency>
    
    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- 配置处理 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- 工具类 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- 测试支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2. 可选依赖（根据需求选择）
```xml
<dependencies>
    <!-- 数据库连接池 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.2.18</version>
    </dependency>
    
    <!-- 消息队列支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    
    <!-- 监控支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- 验证支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

## 项目结构设计

```
src/main/java/com/asyncflow/log/
├── AsyncFlowLogApplication.java    # 启动类
├── config/                         # 配置类
│   ├── AsyncLogConfig.java
│   ├── MyBatisConfig.java
│   └── WebConfig.java
├── core/                          # 核心实现
│   ├── event/                     # 日志事件
│   │   ├── LogEvent.java
│   │   └── LogEventDTO.java
│   ├── queue/                     # 队列管理
│   │   ├── EventQueue.java
│   │   └── impl/
│   ├── consumer/                  # 消费者
│   │   ├── ConsumerPool.java
│   │   └── impl/
│   └── appender/                  # 日志写入器
│       ├── LogAppender.java
│       └── impl/
├── service/                       # 服务层
│   ├── AsyncLogService.java
│   └── impl/
├── controller/                    # 控制器层
│   └── LogController.java
├── mapper/                        # MyBatis Mapper
│   └── LogMapper.java
├── model/                         # 数据模型
│   ├── dto/
│   └── entity/
├── exception/                     # 异常处理
│   └── GlobalExceptionHandler.java
└── util/                          # 工具类
```

## 核心功能实现方案

### 1. 启动类
```java
@SpringBootApplication
@EnableConfigurationProperties(AsyncLogConfig.class)
@MapperScan("com.asyncflow.log.mapper")
public class AsyncFlowLogApplication {
    public static void main(String[] args) {
        SpringApplication.run(AsyncFlowLogApplication.class, args);
    }
}
```

### 2. 配置管理
```java
@Configuration
@ConfigurationProperties(prefix = "async.log")
@Data
@Validated
public class AsyncLogConfig {
    @NotNull
    private QueueConfig queue;
    
    @NotNull
    private ConsumerConfig consumer;
    
    @NotNull
    private AppenderConfig appender;
}
```

### 3. MyBatis 配置
```java
@Configuration
public class MyBatisConfig {
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setTypeAliasesPackage("com.asyncflow.log.model.entity");
        return factoryBean.getObject();
    }
}
```

### 4. Mapper 接口
```java
@Mapper
public interface LogMapper {
    @Insert("INSERT INTO log_event (timestamp, level, message, context) " +
            "VALUES (#{timestamp}, #{level}, #{message}, #{context})")
    int insert(LogEvent event);
    
    @Select("SELECT * FROM log_event WHERE level = #{level}")
    List<LogEvent> findByLevel(@Param("level") String level);
}
```

### 5. 队列实现
```java
@Component
@Slf4j
public class LogEventQueue {
    private final BlockingQueue<LogEvent> queue;
    
    @Autowired
    public LogEventQueue(AsyncLogConfig config) {
        this.queue = new LinkedBlockingQueue<>(config.getQueue().getCapacity());
        log.info("LogEventQueue initialized with capacity: {}", config.getQueue().getCapacity());
    }
}
```

### 6. 消费者线程池
```java
@Component
@Slf4j
public class LogConsumerPool {
    private final ExecutorService executor;
    
    @Autowired
    public LogConsumerPool(AsyncLogConfig config) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            config.getConsumer().getCoreSize(),
            config.getConsumer().getMaxSize(),
            config.getConsumer().getKeepAlive(),
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder()
                .setNameFormat("log-consumer-%d")
                .build()
        );
        this.executor = threadPoolExecutor;
        log.info("LogConsumerPool initialized with coreSize: {}, maxSize: {}", 
            config.getConsumer().getCoreSize(), 
            config.getConsumer().getMaxSize());
    }
}
```

### 7. 日志写入器
```java
@Component
@Slf4j
public class FileLogAppender implements LogAppender {
    @Value("${async.log.appender.file-path}")
    private String filePath;
    
    private final Object lock = new Object();
    
    @PostConstruct
    public void init() {
        log.info("FileLogAppender initialized with path: {}", filePath);
    }
    
    @PreDestroy
    public void destroy() {
        log.info("FileLogAppender destroyed");
    }
}
```

## 配置示例

```yaml
spring:
  application:
    name: async-flow-log
  datasource:
    url: jdbc:mysql://localhost:3306/async_log?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.asyncflow.log.model.entity
  configuration:
    map-underscore-to-camel-case: true

async:
  log:
    queue:
      type: linked
      capacity: 10000
    consumer:
      core-size: 2
      max-size: 4
      keep-alive: 60
    appender:
      type: file
      file-path: /var/log/async
      batch-size: 100
      flush-interval: 1000

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

## 优势分析

1. **Spring Boot 优势**
   - 自动配置简化开发
   - 内置监控支持
   - 丰富的扩展点
   - 完善的依赖管理
   - 支持多种配置方式
   - 提供健康检查
   - 支持优雅关闭

2. **技术选型优势**
   - JDK 8 稳定性好
   - 组件化设计灵活
   - 配置管理方便
   - 易于扩展和维护
   - 支持多种数据源
   - 提供监控指标
   - 支持动态配置

## 注意事项

1. **性能考虑**
   - 合理设置队列容量
   - 优化线程池配置
   - 注意内存使用
   - 使用批量处理
   - 避免频繁GC

2. **可靠性考虑**
   - 实现优雅关闭
   - 处理异常情况
   - 保证日志不丢失
   - 支持重试机制
   - 提供降级策略

3. **扩展性考虑**
   - 预留扩展接口
   - 支持动态配置
   - 考虑集群部署
   - 支持多种输出
   - 提供监控接口

## 后续优化方向

1. **性能优化**
   - 引入对象池
   - 批量处理优化
   - 异步批处理
   - 使用缓存
   - 优化GC

2. **功能增强**
   - 支持更多输出目标
   - 增强监控能力
   - 提供管理接口
   - 支持集群部署
   - 提供查询接口

3. **运维支持**
   - 完善监控指标
   - 提供运维工具
   - 优化部署方案
   - 支持动态配置
   - 提供告警机制 