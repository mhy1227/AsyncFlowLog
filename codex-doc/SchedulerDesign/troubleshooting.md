# 定时任务故障排查（Troubleshooting）

## 1. 未触发 / 不执行
- 检查开关与时间：`async.log.maintenance.enabled=true`，`cron` 表达式正确，`timezone` 明确（推荐 `Asia/Shanghai`）。
- 日志线程：是否出现 `async-log-maintenance-` 前缀的日志；如没有，临时将日志级别提高：
  - `logging.level.com.asyncflow.log.maintenance=DEBUG`
- 启动时间与最近触发点：例如 `0/15 * * * * ?` 启动于 15:24:03，首次触发是 15:24:15。

## 2. 无文件被处理
- 目录不一致：`async.log.file.path` 必须与写入器目录一致（如 FileAppender 的 `async.log.appender.file-path=logs/async`）。
- 文件名不匹配：仅处理 `async-log-YYYY-MM-DD.log` 格式，且“严格早于今天”的文件。
- 阈值关系不合理：`archive.days` 应小于 `retention.days`，否则归档范围为空。

## 3. 归档失败 / 压缩失败
- 归档目录权限：`async.log.archive.dir` 是否可写，空间是否充足。
- 压缩格式：当前仅支持 `zip`，其它值不会归档（会跳过）。
- 失败影响：失败轮次仅计入错误，不影响主流水线与下一轮执行。

## 4. 删除失败
- 文件占用：当天活跃文件不删除；历史文件若被占用（异常进程），Windows 可能删除失败；下轮重试或先释放句柄。
- 权限问题：确保运行账户对日志目录有删除权限。

## 5. 集群重复清理
- 仅单实例启用该开关；或引入分布式锁（ShedLock/DB 锁）；或改用外部调度（K8s CronJob）。

## 6. 时间与时区问题
- 服务器默认时区与配置不一致导致偏移；显式设置 `timezone`，并在 README 标注运行时区。
- 夏令时：建议在中国时区无影响；跨区部署需留意。

## 7. 观察与验收
- 任务日志：
  - `已归档历史日志文件: ... -> .../archive/...zip`
  - `已清理历史日志文件: ...`
  - 汇总：`日志清理完成，归档: X，删除: Y，跳过: Z，错误: N，目录: ...，归档天数: A，保留天数: B`
- 目录结果：`async.log.file.path` 仅保留当天文件；`async.log.archive.dir` 出现 zip 归档。

## 8. 常见环境问题（参考）
- Maven/JDK：确保 JDK 与 pom 的 `java.version` 一致（本项目为 21），编译插件使用 `<release>`；
- Lombok：IDE 安装插件并开启注解处理；
- DataSource：JDBC URL 需包含 `jdbc:mysql://`（不要缺少 `//`），网络连通可用；
- MyBatis：`mapper/*.xml` 路径正确且可扫描到。

## 9. 归档重名行为说明
- 现象：归档目录已存在同名 zip，或 raw 目录已存在同名 .log。
- 处理：系统会自动在目标文件名中追加时间戳（`yyyyMMdd-HHmmss`）后再写入，避免覆盖。
- 建议：若期望“同名即覆盖”，请告知以改为可配置策略；默认保持安全幂等。

```
文件：codex-doc/SchedulerDesign/troubleshooting.md
```
