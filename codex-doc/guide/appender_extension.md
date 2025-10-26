# Appender 扩展开发指南

## 1. 概述
- 目标：实现自定义日志写入器（Appender），替换或并行于默认 FileAppender。
- 接口：`com.asyncflow.log.appender.LogAppender`（建议继承 `AbstractLogAppender` 以复用通用生命周期与计数逻辑）。
- 生命周期：initialize() → append(单条/批量) → flush() → close()；`AbstractLogAppender` 已提供并发安全与计数统计钩子。

## 2. 最小骨架
```java
public class MyAppender extends AbstractLogAppender {
    public MyAppender(String name) { super(name, "my"); }

    @Override protected boolean doInitialize() {
        // 连接资源/创建句柄
        return true;
    }
    @Override protected boolean doAppend(LogEvent event) throws Exception {
        // 单条写入逻辑
        return true;
    }
    @Override protected int doAppendBatch(List<LogEvent> events) throws Exception {
        // 批量写入逻辑（建议与单条复用通路）
        int ok = 0; for (LogEvent e : events) if (doAppend(e)) ok++; return ok;
    }
    @Override protected void doClose() { /* 释放资源/句柄 */ }
}
```

## 3. 线程与并发
- `AbstractLogAppender` 已在 FileAppender 中示例了基于 lock 的并发写保护；你的实现也应确保多线程写入安全（例如单连接串行写/批量缓冲）。
- appendBatch 应保证“全有或尽量多有”，同时记录部分失败，不抛出导致全局崩溃的异常（由上层统计 error）。

## 4. 批量与 flush
- 建议支持批量接口（List<LogEvent>），由 `LogEventHandler.handleBatch` 调用。
- flush 在关闭前和必要场景调用；避免频繁 flush 造成性能抖动。

## 5. 错误处理与降级
- 所有 IO/网络异常应被捕获并记录（WARN/ERROR），避免影响主流水线。
- 可选的降级策略：
  - 写失败 → 退避（指数退避/固定冷却）、重试次数上限；
  - 本地缓冲（谨慎使用，注意磁盘水位与清理）。

## 6. 命名与类型
- `getType()` 返回你的类型标识（如 "db"、"kafka"、"es"、"http"）。
- 与装配工厂（AppenderFactory）对齐：确保配置项 `async.log.appender.type` 与工厂映射一致。

## 7. 配置建议（示例）
```yaml
async:
  log:
    appender:
      type: my                 # 你的类型标识
      # your.*: 自定义参数（连接串、Topic/Index、认证信息、批量大小等）
```
- 通过 `@Value` 或 `@ConfigurationProperties` 绑定参数；使用 `spring-boot-configuration-processor` 生成提示。

## 8. 指标与观测
- 可通过 Micrometer 计数你的 append 成功/失败、批量大小、延迟分布；
- 与 Actuator 集成后可被 Prometheus/Grafana 采集。

## 9. 测试与验收
- 单元：append 单条/批量的成功/失败路径；
- 集成：在本地接入真实/模拟后端，压测吞吐与延迟；
- 退出：优雅停机 flush/close 不丢数据；
- 观察：日志与指标符合预期，无资源泄露。

## 10. 常见注意事项
- 不要在主线程执行阻塞 IO；
- 注意字符集/时区一致性（UTF-8，Asia/Shanghai 或 UTC 统一）；
- 谨慎使用大内存缓冲，避免 OOM；
- 对外部系统的写入要设置合理超时与重试上限。

```
文件：codex-doc/guide/appender_extension.md
```
