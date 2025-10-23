# 定时任务使用指南（Quickstart）

## 1. 前置要求
- 代码已包含独立调度线程池与定时任务：
  - 调度器：src/main/java/com/asyncflow/log/config/SchedulerConfig.java:1
  - 定时任务：src/main/java/com/asyncflow/log/maintenance/LogRetentionScheduler.java:1
- 日志写入器目录需与定时任务扫描目录保持一致（例如都为 `logs/async`）。

## 2. 开启/关闭与常用配置
- 本地测试（高频便于观察）
  ```yaml
  async:
    log:
      maintenance:
        enabled: true
        cron: "0/15 * * * * ?"     # 每 15 秒
        timezone: "Asia/Shanghai"
      retention:
        days: 7
      archive:
        enabled: true
        dir: logs/archive
        days: 1
        compress: zip
      file:
        path: logs/async
  ```
- 生产建议（每日 02:05 执行）
  ```yaml
  async:
    log:
      maintenance:
        enabled: true
        cron: "0 5 2 * * ?"
        timezone: "Asia/Shanghai"
      retention:
        days: 14            # 自行设定
      archive:
        enabled: true
        dir: logs/archive
        days: 3             # 建议 < retention.days
        compress: zip
      file:
        path: logs/async
  ```
- 关闭定时维护
  ```yaml
  async:
    log:
      maintenance:
        enabled: false
  ```

## 3. 本地快速验证
1) 启用“高频” cron（如每 15 秒）。
2) 在 `logs/async` 放置历史文件，命名需匹配：`async-log-YYYY-MM-DD.log`。
   - 示例：2025-10-22（应被归档），2025-10-21（可能被删除，取决于 retention.days）。
3) 启动应用，观察线程前缀 `async-log-maintenance-…` 的日志：
   - 归档示例：`已归档历史日志文件: …/async-log-2025-10-22.log -> …/logs/archive/async-log-2025-10-22.log.zip`
   - 删除示例：`已清理历史日志文件: …/async-log-2025-10-21.log`
   - 汇总：`日志清理完成，归档: X，删除: Y，跳过: Z，错误: N …`
4) 目录验收：
   - `logs/async` 仅保留当天文件（以及未达阈值的历史文件）。
   - `logs/archive` 出现 zip 归档文件。

## 4. 回滚与安全
- 关闭开关即可回滚：`maintenance.enabled=false`。
- 调整为每日执行，避开业务高峰：`cron: "0 5 2 * * ?"`。
- 建议 `archive.days < retention.days`；当天文件永不处理。
- 集群只在单实例启用，或由统一调度（K8s CronJob）触发。

## 5. 常见问题
- 未触发：确认 enabled/cron/timezone 生效，查看是否有 `async-log-maintenance-` 线程日志。
- 无文件处理：检查 `file.path` 是否与写入器目录一致、文件名是否符合日期模式、阈值是否合理。
- 归档失败：确认 `archive.dir` 可写、磁盘空间足够；失败不会影响主日志流水线。
