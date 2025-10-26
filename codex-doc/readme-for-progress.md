# Progress 摘要（定时清理与归档 + 文档与图示）

本文汇总当前与“定时清理与归档（Scheduler）”相关的落地进展、使用要点与后续建议，便于团队快速对齐与交付。

## 1. 已完成（文档与可视化）
- 参数说明：codex-doc/SchedulerDesign/parameters.md:1
- 使用指南（启用/测试/回滚）：codex-doc/SchedulerDesign/quickstart.md:1
- 实现说明（原理与入口）：codex-doc/SchedulerDesign/implementation.md:1
- 故障排查：codex-doc/SchedulerDesign/troubleshooting.md:1
- Cron 速查表：codex-doc/SchedulerDesign/cron_guide.md:1
- Linux chmod 权限速查：codex-doc/SchedulerDesign/chmod_guide.md:1
- 流程图总览/索引：codex-doc/diagrams/overview.md:1
- 主链路时序：codex-doc/diagrams/main_sequence.md:1
- 优雅停机时序：codex-doc/diagrams/shutdown_sequence.md:1
- 定时任务时序：codex-doc/diagrams/scheduler_sequence.md:1
- 定时任务异常流：codex-doc/diagrams/scheduler_error_flow.md:1
- 组件关系：codex-doc/diagrams/components.md:1
- 部署视图：codex-doc/diagrams/deployment.md:1
- 文件轮转状态：codex-doc/diagrams/file_rotation_state.md:1
- README 入口已追加“定时清理与归档（Scheduler）”“整体流程图（Mermaid）”“开发扩展与排查”

（新增）重名规避：当 `archive.dir`/`raw-dir` 存在同名目标时，归档/迁移的文件名自动追加时间戳（yyyyMMdd-HHmmss），避免覆盖。

## 2. 配置快照（当前默认：测试高频）
- 文件：src/main/resources/application.yml:1
- 说明：便于观察定期日志
```yaml
async:
  log:
    maintenance:
      enabled: true
      cron: "0/15 * * * * ?"      # 每 15 秒（测试）
      timezone: "Asia/Shanghai"
    retention:
      days: 7                      # 删除阈值（含今天）
    archive:
      enabled: true
      dir: logs/archive
      days: 1                      # 归档阈值（建议 < retention.days）
      compress: zip
    file:
      path: logs/async             # 与写入器目录保持一致
```

- 上线建议（低峰执行）
```yaml
async:
  log:
    maintenance:
      enabled: true
      cron: "0 5 2 * * ?"         # 每日 02:05
      timezone: "Asia/Shanghai"
    retention:
      days: 14
    archive:
      enabled: true
      dir: logs/archive
      days: 3
      compress: zip
    file:
      path: logs/async
```

## 3. 验收清单（Checklist）
- 线程出现：`async-log-maintenance-`；
- 行为正确：首轮删除/归档历史文件，其后“跳过当天文件”；
- 目录结果：`logs/async` 仅保留当天与未达阈值文件；`logs/archive` 生成 .zip；
- 安全边界：仅处理严格早于今天的 `async-log-YYYY-MM-DD.log`；当天文件永不处理；
- 故障定位：参考 troubleshooting.md；表达式参考 cron_guide.md；权限参考 chmod_guide.md。

## 4. 后续建议（按需选择）
- 归档记录入库（可选开关）：整合 LogArchiveService，将归档结果写入 `log_archive` 表；
- 集群互斥：引入 ShedLock 或外部调度（K8s CronJob）；
- 指标与告警：将 archived/deleted/errors 暴露至 Micrometer/Prometheus；
- Dry-run & 最小留存保护：新增只统计不执行、以及“永不低于 N 天”的保护开关。

## 5. 关联入口（README）
- 根 README 已追加：
  - “定时清理与归档（Scheduler）”模块简介与示例；
  - “整体流程图（Mermaid）”与“更多图示”清单；
  - “开发扩展与排查”入口，指向扩展指南与故障排查。

—— 更新日期：2025-10-23 ——
