# AsyncFlowLog 定时任务参数说明（Maintenance & Archive）

## 1. 作用概览
- 通过定时任务对历史日志进行“归档（可选）+ 清理（删除）”，控制磁盘占用、保留必要历史。
- 任务按 cron 周期触发；每轮执行很快结束，等待下一次触发。
- 仅处理“严格早于今天”的文件；当天的活跃日志不会被归档/删除。

## 2. 关键参数
- async.log.maintenance.enabled（布尔，默认 false）
  - 是否启用定时维护任务（归档/清理）。
- async.log.maintenance.cron（字符串，cron 表达式）
  - 触发时间；示例：
    - 测试高频：`0/15 * * * * ?`（每 15 秒）
    - 每日 02:05：`0 5 2 * * ?`
- async.log.maintenance.timezone（字符串，默认 Asia/Shanghai）
  - cron 所使用的时区；建议显式设置避免服务器默认时区差异。
- async.log.retention.days（整数，默认 7）
  - 留存天数（含今天）。删除规则：文件日期 ≤（今天 − retention.days），且严格早于今天。
  - 示例：今天为 23 号、retention=7，则 ≤ 16 号的文件会被删除，17–22 号根据归档策略处理，23 号跳过。
- async.log.file.path（字符串，默认 logs）
  - 日志所在目录（定时任务扫描此目录）。请与写入器目录保持一致。
  - 如使用 FileAppender 的配置键 `async.log.appender.file-path`，请保证两者路径一致（例如都为 `logs/async`）。

- async.log.archive.enabled（布尔，默认 false）
  - 是否开启归档（在删除前，对达到归档阈值的历史文件先压缩到归档目录，再删除源文件）。
- async.log.archive.dir（字符串，默认 logs/archive）
  - 归档文件存放目录；不存在会自动创建。
- async.log.archive.raw-dir（字符串，默认 logs/archive/raw）
  - 原始 .log 的迁移目录（当采用“归档后保留原始”策略时使用）。
- async.log.archive.days（整数，默认 3，建议 < retention.days）
  - 归档阈值（含今天）。归档范围为：
    - 今天 − retention.days < 文件日期 ≤ 今天 − archive.days（且严格早于今天）。
    - 更早的文件已进入“删除”分支；更近的文件暂不处理。
- async.log.archive.compress（字符串，默认 zip）
  - 归档压缩格式；当前实现仅支持 zip（归档产物命名为 `原文件名.log.zip`）。

## 2.1 归档策略说明（当前默认）
- 行为：满足归档阈值的历史日志会被压缩为 zip 到 `async.log.archive.dir`，同时“原始 .log”会被迁移到 `async.log.archive.raw-dir`（保留原始文本，便于审计/对比）。
- 当天文件永不处理；仅处理“严格早于今天”的文件。
- 删除策略：达到“删除阈值（retention.days）”的文件直接删除（不会再归档）。
- 建议：为 `raw-dir` 目录配置单独的留存与外部清理（运维层面，例如按 7/14 天再清理）。
 - 重名处理：若目标目录已存在同名文件，系统会在文件名中追加时间戳后再落盘，例如：
   - `async-log-2025-10-23.log.zip` → `async-log-2025-10-23.log.20251024-070501.zip`
   - `async-log-2025-10-23.log`（raw）→ `async-log-2025-10-23.20251024-070501.log`

## 3. 推荐配置
- 生产环境（示例）
  - 每日 02:05 执行；保留 14 天；超过 3 天先归档到 `logs/archive`
  ```yaml
  async:
    log:
      maintenance:
        enabled: true
        cron: "0 5 2 * * ?"
        timezone: "Asia/Shanghai"
      retention:
        days: 14
      archive:
        enabled: true
        dir: logs/archive
        days: 3
        compress: zip
      file:
        path: logs/async   # 与写入器一致
  ```
- 本地测试（示例）
  - 每 15 秒执行；保留 7 天；超过 1 天归档到 `logs/archive`
  ```yaml
  async:
    log:
      maintenance:
        enabled: true
        cron: "0/15 * * * * ?"
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

## 4. 运行日志与验收
- 执行线程前缀：`async-log-maintenance-…`
- 典型输出：
  - `已归档历史日志文件: …/async-log-2025-10-22.log -> …/logs/archive/async-log-2025-10-22.log.zip`
  - `已清理历史日志文件: …/async-log-2025-10-21.log`
  - 汇总：`日志清理完成，归档: X，删除: Y，跳过: Z，错误: N，目录: …，归档天数: A，保留天数: B`
- 目录结果：
  - `async.log.file.path` 下仅保留当天文件和未达阈值的历史文件；
  - `async.log.archive.dir` 出现 zip 归档文件。

## 5. 注意事项与边界
- 文件命名要求：仅处理匹配 `async-log-YYYY-MM-DD.log` 的文件；当天文件永不处理。
- 路径对齐：`async.log.file.path` 必须与写入器目录一致（例如 FileAppender 的 `async.log.appender.file-path`）。
- 参数关系：建议 `archive.days < retention.days`，否则归档范围可能为空；二者都基于“含今天”的天数计算。
- 时区/DST：生产环境建议固定 `timezone`；避免夏令时导致的触发偏移。
- 集群：仅在单实例启用，或引入分布式锁/外部调度，避免重复归档/清理。
- 仅 zip 支持：当前仅实现 zip 压缩；如需 gzip/7z 可在后续扩展。

## 6. 故障排查速查
- 未触发：检查 `enabled`、`cron`、`timezone` 是否生效；观察是否有 `async-log-maintenance-` 线程日志。
- 无文件处理：核对 `file.path` 与写入器目录是否一致、文件名是否符合日期模式、参数阈值是否设置合理。
- 归档失败：查看错误堆栈；确认 `archive.dir` 可写、磁盘空间充足；失败轮次不会影响主日志流水线。

文件：codex-doc/SchedulerDesign/parameters.md
