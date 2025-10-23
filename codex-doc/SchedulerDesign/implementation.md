# 定时任务实现说明（Implementation）

## 1. 代码入口与包结构
- 调度器配置：`src/main/java/com/asyncflow/log/config/SchedulerConfig.java:1`
  - 提供独立的 `ThreadPoolTaskScheduler`（1 线程，前缀 `async-log-maintenance-`），并通过 `SchedulingConfigurer` 让 `@Scheduled` 使用该调度器。
- 任务实现：`src/main/java/com/asyncflow/log/maintenance/LogRetentionScheduler.java:1`
  - 负责扫描日志目录、按日期规则执行“归档（可选）+ 删除”，并统计每轮结果。

## 2. 核心流程
1) 触发调度
- `@Scheduled(cron = "${async.log.maintenance.cron:0 5 2 * * ?}", zone = "${async.log.maintenance.timezone:Asia/Shanghai}")`
- 支持通过配置覆盖 `cron` 与时区。

2) 目录扫描与文件筛选
- 扫描 `async.log.file.path`（默认 `logs`，建议与写入器目录一致，如 `logs/async`）。
- 匹配文件名模式：`async-log-(\d{4}-\d{2}-\d{2}).log`。
- 仅处理“严格早于今天”的历史文件；当天文件直接跳过。

3) 日期阈值与动作判定
- 计算两个阈值：
  - 删除阈值：`today.minusDays(retentionDays)`
  - 归档阈值：`today.minusDays(archiveDays)`
- 判定顺序（先删后档，避免老文件漏删）：
  - 若 `fileDate ≤ 删除阈值` → 直接删除；
  - 否则若启用归档且 `fileDate ≤ 归档阈值` → 归档（zip 到 `async.log.archive.dir`），成功后删除源文件；
  - 否则 → 跳过（尚未到归档/删除时机）。

4) 归档实现
- 仅支持 zip：`ZipOutputStream` 将 `*.log` 写入 `*.log.zip`；归档目录不存在会自动创建。
- 避免重复：若归档目标已存在则直接跳过创建（保持幂等）。

5) 结果统计与日志
- 计数器：`archived/deleted/skipped/errors`；
- 汇总日志示例：`日志清理完成，归档: X，删除: Y，跳过: Z，错误: N，目录: …，归档天数: A，保留天数: B`。

## 3. 配置绑定
- 通过 `@Value` 绑定以下参数（详见 parameters.md）：
  - `async.log.maintenance.enabled|cron|timezone`
  - `async.log.retention.days`
  - `async.log.file.path`
  - `async.log.archive.enabled|dir|days|compress`
- 推荐关系：`archive.days < retention.days`；否则归档范围可能为空。

## 4. 并发与边界
- 独立线程池：调度与主消费/写入线程池隔离，减少干扰。
- 集群：默认按单实例启用；如需多实例运行，建议引入分布式锁或改用外部调度（K8s CronJob）。
- 安全保护：
  - 当天文件永不处理；
  - 仅处理符合命名和日期模式的文件；
  - 归档失败不会影响下轮执行，也不影响主流水线。
- 仅 zip 支持：后续可扩展 gzip/7z 等；
- IO 峰值：建议将 `cron` 设在业务低峰（如 02:05），并按需调整保留/归档策略。

## 5. 可扩展点
- 归档记录入库：对接 `LogArchiveService`，将归档结果写入 `log_archive` 表，便于检索与审计。
- 指标暴露：将归档/删除/错误计数暴露到 Micrometer（Prometheus），配合告警阈值。
- Dry-Run：新增“仅统计不执行”的开关，便于预演策略调整的影响范围。
- 分布式互斥：引入 ShedLock 等组件，保证集群下只跑一次。
