# AsyncFlowLog 概览（适合 1 分钟速览）

## 1. 项目定位
- 基于 Spring Boot 的异步日志组件，通过“日志事件 → 内存队列 → 消费者线程池 → 写入器”的流水线，实现主线程无阻塞的高吞吐写日志。

## 2. 核心模块
- LogEvent / LogEventFactory：统一封装日志上下文与事件创建。
- EventQueue / LinkedEventQueue：缓冲待写日志，可替换为其他队列实现。
- ThreadPoolConsumer：后台线程池异步消费队列，交给写入器。
- LogAppender / FileAppender：默认写本地文件，可扩展 Kafka、DB 等。
- @OperationLog AOP：支持业务操作日志（审计）。

## 3. 配置 & 集成
- `application.yml` 中的 `async.log.*` 控制队列容量、线程池、写入器等。
- 引入依赖后可直接 `@Autowired` 使用 `AsyncLogService`，或通过 `@OperationLog` 注解。
- 提供配置指南、压测指南和示例 JMX（详见 codex-doc 目录）。

## 4. 亮点
- 可配置、可扩展、可监控。
- 通过接口抽象，替换写入器成本低。
- 适合作为应用内日志 SDK，也可无缝演进到 MQ/ES 等后端。

---

## 推荐阅读
- [README.md](../README.md)：概览与快速开始。
- [codex-doc/async_log_config_guide.md](./async_log_config_guide.md)：配置说明与集成示例。
- [codex-doc/20251022/jmter/jmeter_pressure_test.md](./20251022/jmter/jmeter_pressure_test.md)：压测方案。
