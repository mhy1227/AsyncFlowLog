

> 前置说明：按你当前仓库，简要介绍 AsyncFlowLog 的定位、架构、能力、配置与定时模块，便于快速上手与沟通。

项目定位

- 基于 Spring Boot 的应用内异步日志组件，走“日志事件 → 队列 → 消费者线程池 → 写入器”的流水线，实现主线程无阻塞的高吞吐写日志。
- 既可当作应用内日志 SDK 使用，也可扩展对接 MQ/DB/ES 等后端。

架构与模块

- 事件与队列：LogEvent、LogEventFactory、EventQueue、LinkedEventQueue
- 消费侧：ThreadPoolConsumer（N 线程消费）、LogEventHandler（支持批量）
- 写入器：LogAppender 接口、默认 FileAppender（按日滚动）
- AOP 审计：@OperationLog + OperationLogAspect
- 定时维护：LogRetentionScheduler（归档 + 清理）
- 配置与装配：QueueConfig、ConsumerConfig、AppenderConfig、SchedulerConfig
- 可观测性：AsyncLogMetrics、AsyncLogHealthIndicator、Actuator

关键能力

- 异步与高吞吐：主线程只产生日志事件；IO 由消费者线程异步完成
- 批量与滚动：处理器批量提交，FileAppender 按天轮转
- 可扩展：Appender 可替换/并行扩展（文件、数据库、消息队列等）
- 安全停机：优雅关闭时自动 flush 并释放资源

配置与集成（精简示例）

- 引用后可直接注入 AsyncLogService 使用，或在业务方法上加 @OperationLog
- 主要参数位于 async.log.*：队列容量、消费者线程池、写入器类型/路径、批量与 flush 周期等
- 参考：README.md:1、codex-doc/async_log_config_guide.md:1

定时归档/清理（已实现）

- 目标：控制磁盘占用；仅处理“严格早于今天”的历史文件
- 策略：达到删除阈值直接删除；未达删除阈值但达到归档阈值则 zip 到归档目录后删除源文件；其余跳过
- 关键参数（示例）
    - async.log.maintenance.enabled=true
    - async.log.maintenance.cron="0 5 2 * * ?"（每日 02:05）
    - async.log.maintenance.timezone="Asia/Shanghai"
    - async.log.retention.days=14
    - async.log.archive.enabled=true
    - async.log.archive.dir=logs/archive
    - async.log.archive.days=3（建议 < retention）
    - async.log.archive.compress=zip
    - async.log.file.path=logs/async（需与写入器目录一致）
- 文档与实现：codex-doc/SchedulerDesign/parameters.md:1，codex-doc/SchedulerDesign/quickstart.md:1，codex-doc/SchedulerDesign/implementation.md:1

可观测性

- Actuator 暴露健康与基础指标；异步日志健康检查与自定义指标已接入
- 可扩展将“归档/删除/错误计数”暴露到 Prometheus（待选增强）

运行环境

- JDK 21、Spring Boot 2.7.18（内嵌 Tomcat 9）
- 持久化部分（如示例 Mapper/表）使用 MySQL；日志文件默认写入 logs/async

适用与边界

- 适用：应用内日志统一采集、解耦 IO、批量/滚动、可扩展后端
- 边界：不等同于 MQ/日志平台；跨服务传输与集中检索需外部系统配套
- 集群中的定时任务需互斥（单实例开启/分布式锁/外部调度）

可视化图示

- 总览与流程：codex-doc/diagrams/overview.md:1
- 主链路时序：codex-doc/diagrams/main_sequence.md:1
- 优雅停机时序：codex-doc/diagrams/shutdown_sequence.md:1
- 定时任务时序/异常流：codex-doc/diagrams/scheduler_sequence.md:1，codex-doc/diagrams/scheduler_error_flow.md:1
- 组件关系、部署、文件轮转：codex-doc/diagrams/components.md:1，codex-doc/diagrams/deployment.md:1，codex-doc/diagrams/file_rotation_state.md:1 