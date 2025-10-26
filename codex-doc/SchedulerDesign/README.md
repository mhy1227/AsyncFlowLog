# AsyncFlowLog 定时任务设计方案（SchedulerDesign）

## 1. 适用场景
- 日志归档/清理：按天/大小压缩与删除旧文件，控制磁盘占用。
- 失败补偿/离线回传：后端（DB/MQ/对象存储）不可用时落本地，恢复后定期重传。
- 批量时间阈/定时 flush：在“N 条或 T 毫秒”均触发的场景中，用时间阈兜底触发批量/flush。
- 运行自检/指标快照：定时汇总队列深度、吞吐、失败数，或做健康探测与告警。

## 2. 方案选型
### 2.1 Spring Scheduling（@Scheduled）
- 说明：启用 `@EnableScheduling` 后，使用 `@Scheduled(cron=...)`、`fixedRate`、`fixedDelay` 等触发。
- 优点：零额外依赖、与 Spring 环境深度集成、配置化支持强（可外置 cron）。
- 局限：默认不持久化任务状态/触发器；多实例需要额外机制避免并发执行。
- 适合：单体或简单集群（可通过“仅一个实例开启”或分布式锁来保证互斥）。

### 2.2 JDK ScheduledExecutorService
- 说明：使用 `ScheduledThreadPoolExecutor` 计算首个延迟后，固定 24h 周期执行。
- 优点：轻量、可控，依赖最小，不受 Spring 生命周期强绑定影响。
- 局限：不支持 cron 表达式；需自行处理时区/DST 变化（建议每日计算下一次 02:00）。
- 适合：简单周期任务、对时间点要求不高的场景。

### 2.3 Quartz Scheduler（可选）
- 说明：企业级调度，支持持久化、misfire 策略、集群协调、复杂日程。
- 优点：可靠性与可运维性强，适合复杂调度与多实例场景。
- 局限：引入与维护成本较高；对本项目“内置维护类任务”可能过重。
- 适合：跨实例强一致、需要任务可视化与持久化的复杂场景。

### 2.4 外部调度（ops）
- 说明：Linux cron、Windows 计划任务、Kubernetes CronJob 等。
- 优点：与应用解耦，出问题不影响主进程；适合文件级归档/清理。
- 局限：无法便利地访问应用内状态；多环境差异较大，需要运维介入。
- 适合：文件层面的轮转/清理、归档与搬运任务。

## 3. 与 AsyncFlowLog 的集成设计
### 3.1 包与模块
- maintenance：归档/清理等维护类任务（建议：`com.asyncflow.log.maintenance`）。
- recovery：失败补偿与离线回传（`com.asyncflow.log.recovery`）。
- monitor：指标汇总/自检（`com.asyncflow.log.monitor`）。
- config：调度相关 Bean 与参数注入（`com.asyncflow.log.config`）。

### 3.2 线程与隔离
- 使用独立的调度线程池（1–2 线程），线程名前缀 `async-log-maintenance-`。
- 不复用消费线程池，避免干扰日志主流水线。

### 3.3 配置建议（示例）
- `async.log.maintenance.enabled=false`（默认关闭）
- `async.log.maintenance.cron=0 5 2 * * ?`（每天 02:05 执行，避开 00:00 跨日滚动）
- `async.log.maintenance.timezone=Asia/Shanghai`
- `async.log.retention.days=7`
- `async.log.retry.enabled=false`
- `async.log.retry.cron=0 0/10 * * * ?`（示例）
- `async.log.metrics.snapshot.enabled=false`

### 3.4 集群互斥策略
- 单点启用：只在一个实例将 `maintenance.enabled=true`。
- 分布式锁：基于 Redis/DB 的租约或锁（如引入 ShedLock 等第三方方案）。
- 管控层调度：使用外部调度（如 K8s CronJob）确保集群只调度一次。

## 4. 推荐路线（当前项目）
- 首选：Spring Scheduling + cron（最小改动、配置化强）。
  - 使用 `@EnableScheduling` 与独立 `TaskScheduler` 线程池。
  - `LogRetentionScheduler` 每日 02:05 清理/归档旧日志；默认关闭，通过配置开启。
  - 如为多实例部署，先采用“单点启用”策略；如后续需要多活互斥，再演进到分布式锁。
- 不建议把“批量时间阈”用定时器实现（优先在消费者内部做时间窗口判断）；如必须定时，只做 `flush()`，不挤占消费线程。

## 5. 类与配置草案
```java
// SchedulerConfig.java（建议放 com.asyncflow.log.config）
@Configuration
@EnableScheduling
public class SchedulerConfig {
  @Bean(name = "logMaintenanceScheduler")
  public ThreadPoolTaskScheduler logMaintenanceScheduler() {
    ThreadPoolTaskScheduler t = new ThreadPoolTaskScheduler();
    t.setPoolSize(1);
    t.setThreadNamePrefix("async-log-maintenance-");
    t.setAwaitTerminationSeconds(30);
    t.setRemoveOnCancelPolicy(true);
    t.initialize();
    return t;
  }
}
```

```java
// LogRetentionScheduler.java（建议放 com.asyncflow.log.maintenance）
@Component
@ConditionalOnProperty(prefix = "async.log.maintenance", name = "enabled", havingValue = "true")
public class LogRetentionScheduler {
  @Value("${async.log.retention.days:7}")
  private int retentionDays;

  @Scheduled(
    cron = "${async.log.maintenance.cron:0 5 2 * * ?}",
    zone = "${async.log.maintenance.timezone:Asia/Shanghai}"
  )
  public void cleanOldLogs() {
    // 1) 计算过期时间点  2) 扫描日志目录  3) 删除/压缩  4) 失败时降级记录
  }
}
```

```yaml
# application.yml 片段（默认关闭）
async:
  log:
    maintenance:
      enabled: false
      cron: "0 5 2 * * ?"
      timezone: "Asia/Shanghai"
    retention:
      days: 7
```

## 6. 风险与边界
- 时间与时区：cron 建议显式 `zone`；如使用 JDK 方案需处理 DST。
- 多实例并发：默认“单点启用”；若需全活，需加锁或外部调度。
- IO 峰值：归档/压缩可能占用磁盘与 CPU，错峰到 02:00–03:00，避开跨日轮转。
- 任务过长：避免与主业务抢占资源；加入超时/分片处理与失败重试（带冷却时间）。

## 7. 启用步骤（建议）
1) 合并 `SchedulerConfig` 与 `LogRetentionScheduler`；
2) application.yml 开关置为 `true`，设置 cron 与保留天数；
3) 单实例先行验证，再考虑多实例下的互斥策略；
4) 观察日志与磁盘水位，按需调优执行时间与保留策略。

## 8. 验收标准
- 任务按期在预定时间执行，日志可见；
- 旧日志按保留策略被清理/归档，无误删；
- 主日志流水线（生产/消费/写入）无明显抖动；
- 开关可一键关闭，回滚不影响主流程。

---

附：文档导航
- 参数说明：parameters.md
- Cron 速查表：cron_guide.md
- 使用指南（启用/测试/回滚）：quickstart.md
- 实现说明（原理与代码入口）：implementation.md
- 故障排查：troubleshooting.md
- 规划（Roadmap）：roadmap.md
