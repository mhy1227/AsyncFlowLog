# AsyncFlowLog 集成指南

## 1. 概述
- **目标**：说明如何在现有 Spring Boot 项目中引入 AsyncFlowLog，实现主业务与日志写入解耦的高性能异步日志链路。
- **核心能力**：提供统一的日志事件模型、可配置的内存队列、线程池消费者以及可扩展的日志写入器（默认落盘）。
- **适用场景**：高并发/低延迟业务、需要批量落盘或扩展到 MQ、数据库、ES 等渠道的日志采集。

## 2. 关键组成模块
| 模块 | 包路径 | 职责概述 |
| --- | --- | --- |
| 日志事件模块 | `com.asyncflow.log.model.event` | 定义 `LogEvent` 接口及 `LogEventDTO` 默认实现，通过 `LogEventFactory` 统一构造事件对象。 |
| 队列管理模块 | `com.asyncflow.log.queue` | 提供 `EventQueue` 抽象及 `LinkedEventQueue` 默认实现，负责日志事件的缓冲与容量控制。 |
| 消费者线程池模块 | `com.asyncflow.log.consumer` | 以 `ThreadPoolConsumer` 为核心，异步从队列拉取事件并交给处理器，支持监控指标查询。 |
| 日志写入器模块 | `com.asyncflow.log.appender` | 抽象写入契约，默认 `FileAppender` 将日志落盘；`LogEventHandler` 负责与消费者线程池对接。 |

## 3. 系统初始化流程
1. Spring 容器启动时加载 `AppenderConfig`、`EventHandlerConfig`、`QueueFactory`、`ConsumerFactory` 等配置 Bean。
2. `AppenderFactory` 依据 `async.log.appender.*` 参数创建并初始化 `LogAppender`（默认 `FileAppender`）。
3. `LogEventHandler` 注入写入器并完成初始化；`QueueFactory`、`ConsumerFactory` 读取队列与线程池尺寸。
4. `AsyncLogServiceImpl` 在 `@PostConstruct` 阶段调用 `consumerPool.start()`，启动消费者线程并连接事件队列与写入器。
5. 业务调用 `AsyncLogService` 时生成 `LogEvent` → 入队 → 消费线程异步写入 → `FileAppender` 落盘或其他目标。

## 4. 集成步骤
### 4.1 引入依赖
在业务项目的 `pom.xml` 中添加（示例）：
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>asyncflowlog</artifactId>
    <version>1.0.0</version>
</dependency>
```
确保 `@ComponentScan` 能覆盖 `com.asyncflow.log` 包。

### 4.2 配置参数
在 `application.yml`（或 `application.properties`）中新增：
```yaml
async:
  log:
    queue:
      type: linked          # 当前内置 LinkedBlockingQueue
      capacity: 10000
    consumer:
      core-size: 2
      max-size: 4
      keep-alive: 60
    appender:
      type: file             # 其他类型可自行扩展 LogAppender
      file-path: /var/log/async
      file-name-pattern: async-log-%s.log
      batch-size: 100
      flush-interval: 1000   # ms
      auto-flush: false
```
可根据业务吞吐、磁盘路径、刷盘策略调整这些值。

### 4.3 注入与调用
```java
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private AsyncLogService asyncLogService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDTO>> login(@RequestBody LoginDTO dto) {
        UserDTO user = userService.login(dto);

        LogEvent event = new LogEventDTO("INFO", "用户登录成功")
            .addContext("username", dto.getUsername())
            .withLocation(getClass().getName(), "login");

        asyncLogService.log(event.getLevel(), event.getMessage(), event.getContext());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
```
或直接调用快捷方法：`asyncLogService.info("业务完成", contextMap);`

### 4.4 启用 AOP 操作日志（可选）
- 引入 `com.asyncflow.log.aop` 相关配置。
- 在业务方法上添加 `@OperationLog` 注解，系统自动捕获操作信息并通过同一条异步链路落盘或入库。

## 5. 扩展与自定义
- **自定义写入器**：实现 `LogAppender` 接口（如数据库、MQ、ElasticSearch），并通过自定义 `AppenderFactory` 或 Spring Bean 覆盖默认配置。
- **队列与线程池**：实现 `EventQueue`、`ConsumerPool` 新版本（如无锁队列、虚拟线程调度），在配置或 Bean 定义中替换默认实现。
- **监控与报警**：结合 `AsyncLogService` 的 `getQueueSize()`、`getActiveThreadCount()`、`LogEventHandler` 的统计指标，接入 Prometheus/Grafana 等监控体系。

## 6. 常见问题与排查
| 问题 | 可能原因 | 解决方案 |
| --- | --- | --- |
| 日志未落盘 | 目录无写权限/写入器未初始化 | 检查 `async.log.appender.file-path` 权限，确认日志启动时 `AppenderConfig` 未报错。 |
| 队列爆满丢日志 | 队列容量过小或消费过慢 | 增大 `capacity`，提高 `consumer` 线程数，或启用批量写入/自定义写入器。 |
| 线程池未启动 | Bean 未加载或服务未调用 `start()` | 确保 `AsyncLogServiceImpl` 在 Spring 容器中，排查 `@ComponentScan` 范围。 |
| 时间/线程名缺失 | 事件未设置相关字段 | 使用 `LogEventFactory` 提供的 `withLocation`、`createLogEventWithThread` 等方法完善上下文。 |

## 7. 最佳实践
- 日志目录与应用实例隔离，避免单文件过大；必要时配合日志轮转/归档脚本。
- 结合 `batch-size` 与 `auto-flush` 平衡吞吐与数据实时性，关键业务建议在关闭前调用 `AsyncLogService.flush()`。
- 若要长期复用，可将 AsyncFlowLog 打包为自研 Starter（提供自动配置与条件装配）。
- 对高可靠场景，将 `FileAppender` 替换为持久化 MQ/数据库写入器，并启用失败重试与补偿机制。

> 如需深入了解设计原理或模块细节，可参考 `docs/async_design.md`、`docs/analysis` 下的各类分析文档。
