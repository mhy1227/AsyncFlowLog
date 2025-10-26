# AsyncFlowLog 压测报告（2025-10-26）

## 1. 摘要（Summary）
- 使用 JMeter 对 `POST /api/test/log` 进行压力测试，目标验证异步日志链路和操作日志写入能力。
- 压力参数：200 线程，Ramp-Up 30 秒，循环 100 次；所有请求返回 HTTP 200。
- MySQL `operation_log` 在写入约 10,975 条后触发账户 `max_questions` 限额，导致后续写入失败。
- 结论：接口可用，但数据库限额成为可观察到的瓶颈；需调整账户配置或切换写入策略。

## 2. 目标与范围（Scope）
- 测试接口：`POST /api/test/log`
- 日志路径：AsyncLogService → 队列 → 线程池 → 文件写入 + operation_log 入库
- 测试目的：验证业务接口的吞吐与稳定性，观察日志入库行为。

## 3. 环境信息（Environment）
- 应用：Java 21、Spring Boot 2.7.18、内嵌 Tomcat 9.0.83
- 数据库：MySQL（账户 `hmylyn`，默认 `max_questions=36000`）
- 数据源：Druid 连接池（默认配置）
- 监控：应用日志、Actuator 指标、JMeter 日志

## 4. 压测模型（Load Model）
| 参数 | 数值 | 说明 |
| --- | --- | --- |
| 并发线程 | 200 | JMeter Thread Group 默认线程数 |
| Ramp-Up | 30 秒 | 每秒约 6~7 个线程启动 |
| 循环次数 | 100 | 每线程执行 100 次请求 |
| 请求路径 | `/api/test/log` | 带 `@OperationLog` 的演示接口 |
| 请求体 | `{"level":"INFO","message":"performance-test","userId":"<随机6位>"}` | JSON |

## 5. 结果与观察（Results）
- HTTP 层：所有请求返回 200，JMeter ResultTree 显示 `Error Count = 0`。
- 数据库层：达到约 10,975 条时出现 `java.sql.SQLSyntaxErrorException: User 'hmylyn' has exceeded the 'max_questions' resource (current value: 36000)`，操作日志写入失败。
- 异步链路：队列、线程池运行正常；文件写入无异常。

## 6. 结论（Conclusion）
- 接口可用性良好，异步链路无阻塞；
- 瓶颈在于 MySQL 账户限额；
- 下一步需调整 DB 限额或使用专用写入渠道（如 MQ、ES）。

## 7. 建议（Recommendations）
1. 数据库层：
   - 临时调整限额 `ALTER USER ... WITH MAX_QUERIES_PER_HOUR 0;`
   - 压测/生产使用独立账户，合理配置限额。
2. 压测执行：
   - 分批提高线程数（300/500/1000…），观察队列与响应时间。
   - 保存 JMeter CSV/HTML 报表，记录吞吐与延迟指标。
3. 可靠性：
   - 增强 `OperationLogServiceImpl` 的失败处理与告警；
   - 定期清理 `operation_log`，或用 MQ/对象存储替代。

## 8. 附录（Appendix）
| 附件 | 说明 |
| --- | --- |
| `codex-doc/20251026/jmter/sample_async_log_test.jmx` | JMeter 压测脚本 |
| `codex-doc/20251026/log--codex/log-jmeter-result-tree-v1.txt` | HTTP 执行结果日志 |
| `codex-doc/20251026/log--codex/log--controll--v1.txt` | 应用日志（包含数据库限额错误） |
| `src/main/resources/db/init.sql` | 数据库建表脚本 |

> 如需详细吞吐/延迟，请结合 JMeter Summary/AggregateReport。
