# AsyncFlowLog 快速需求概览

## 1. 项目定位
- Spring Boot 异步日志组件：`LogEvent` → `EventQueue` → `ThreadPoolConsumer` → `LogAppender`。
- 默认写入文件（`FileAppender`），可扩展 Kafka、DB 等写入器。
- 提供 `AsyncLogService` 直接调用 & `@OperationLog` AOP 审计。

## 2. 核心模块
- `com.asyncflow.log.model.event`：日志事件与工厂。
- `com.asyncflow.log.queue`：队列抽象与 `LinkedEventQueue` 实现。
- `com.asyncflow.log.consumer`：消费者线程池与 `LogEventHandler`。
- `com.asyncflow.log.appender`：写入器抽象、`FileAppender`、后续可扩展。
- `com.asyncflow.log.controller`：监控接口、操作日志测试接口。

## 3. 配置 & 集成
- 主要配置入口：`application.yml` → `async.log.*`、`spring.datasource.*`、`logging.*`。
- 详细说明：`codex-doc/async_log_config_guide.md`。
- 压测方案：`codex-doc/20251022/jmter/jmeter_pressure_test.md`，示例脚本 `codex-doc/20251022/jmter/sample_async_log_test.jmx`。

## 4. 当前状态
- 操作日志批量插入导致表增长很快（约 19k 条），写库失败日志见 `OperationLogServiceImpl`。
- 数据源使用外部 MySQL，连接失败/额度不足会产生错误日志。
- 本地文件写入正常（路径 `logs/async`）。

## 5. 后续关注点
- 评估操作日志持久化策略（清理、限流、切换存储方式）。
- 补充压测数据（线程数、吞吐量、延迟），在文档中记录。
- 若需要高可靠性，评估 MQ/WAL 等写入方案。

## 6. 文档索引
- README.md（概览与快速开始）
- codex-doc/async_log_config_guide.md（配置、扩展、集成）
- codex-doc/20251022/jmter/jmeter_pressure_test.md（压测指南）
- codex-doc/20251022/asyncflowlog_interview.md（面试讲解要点）
- codex-doc/20251022/AnswerForinter.md（常见问答）
