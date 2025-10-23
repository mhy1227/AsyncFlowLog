# AsyncFlowLog JMeter 压测指南

## 1. 目标
- 验证异步日志链路在高并发下的吞吐能力和稳定性（队列长度、线程池活跃数、写入延迟）。
- 观察在不同配置（队列容量、线程池大小、批量写入）下的性能差异与资源占用。
- 提前发现潜在瓶颈（队列满、写入失败、数据库连接耗尽等）。

## 2. 前置准备
1. **环境**：建议在与生产相近的测试环境执行；确保 MySQL、日志目录或 Kafka（若使用）配置正确。
2. **服务配置**：根据目标测试场景调整 `async.log.*` 参数，例如设置较大的 `queue.capacity`、合理的线程池规模。
3. **监控**：提前准备好指标采集（Prometheus/Grafana 或 Actuator `/actuator/metrics`）以及日志观察面板。
4. **工具**：安装 Apache JMeter 5.5+；如需分布式压测，可准备多台负载机。

## 3. 测试对象
- 直接调用暴露 `AsyncLogService` 的 REST 接口（例如 `POST /api/log/test`）；
- 或者调用带 `@OperationLog` 注解的业务接口（例如 `POST /orders`）。

> 注意：如果接口包含复杂业务逻辑，在压测前可提供专用“伪接口”仅负责调用 `asyncLogService.log(...)`，避免业务逻辑成为干扰因素。

## 4. JMeter 测试计划示例

### 4.1 树形结构
```
Test Plan
 └─ Thread Group (业务压测组)
     ├─ HTTP Request (POST /api/log/test)
     ├─ HTTP Header Manager (Content-Type: application/json)
     ├─ CSV Data Set Config (可选：随机生成日志上下文)
     └─ Backend Listener / Summary Report
```

### 4.2 配置建议
- **Thread Group**
  - 线程数（Number of Threads）：根据目标负载设置，例如 200/500/1000；
  - Ramp-Up 时间：建议 30~60 秒，避免瞬时洪峰；
  - 循环次数：可设置固定循环次数（如 1000），或勾选“永远”并配合 `Duration` 控制测试时长。
- **HTTP Request**
  - 方法：POST
  - Body：`{"level":"INFO","message":"test-log","userId":"${userId}"}` 等；
  - 超时时间：根据环境设置，避免连接悬挂。
- **定时器/控制器（可选）**
  - 若需要模拟随机行为，可引入 Uniform Random Timer 或 Throughput Controller。
- **后置处理器**
  - 可添加 `JSON Extractor` 判断返回值中的成功标识。

### 4.3 监控指标
- **JMeter 侧**：吞吐量（Requests/sec）、响应时间（90/95/99 分位）、错误率。
- **服务端**：
  - `asynclog.queue.usage`、`asynclog.queue.size`
  - `asynclog.consumer.active`、`asynclog.consumer.completed`
  - 写入成功/失败计数、线程池拒绝次数
  - 系统资源（CPU、内存、磁盘 IO）
- **数据库/Kafka**：连接池耗尽、消息堆积等指标。

## 5. 实验方案建议
1. **基线测试**：使用默认配置（queue=10000、core=2、batch=100）跑 5~10 分钟，记录关键指标。
2. **峰值测试**：提高线程数（如 1000+）观察队列长度和写入成功率，确认是否出现丢日志或延迟升高。
3. **配置对比**：
   - 增大 `queue.capacity` + `core-size`
   - 调整 `batch-size`、开启 `auto-flush`
   - 更换写入器（file → kafka）
4. **稳定性测试**：长时间运行（30~60 分钟），观察资源是否稳定，日志文件是否持续增长，队列是否逐步清空。

## 6. 结果分析与记录
- 整理测试场景、参数、JMeter 报告截图或表格；
- 对比不同配置的吞吐量、响应时间、队列积压情况；
- 记录瓶颈与异常（如数据库连接错误、写入超时）。

## 7. 常见问题与排查
| 问题 | 排查方向 |
| --- | --- |
| 响应 500 或报错 | 查看服务日志（`logs/asyncflow.log`）、检查数据库连接或写入器状态 |
| 队列持续满/告警 | 增加消费者线程、扩容队列、优化写入目标（例如使用 Kafka） |
| 日志文件空白 | 确认接口确实调用到 `AsyncLogService`，以及文件/目录权限 |
| 延迟高/吞吐低 | 监控 CPU/IO；调整 `batch-size`、线程池参数；必要时增加实例或改写入器 |

## 8. 清理与回滚
- 压测完成后，恢复原始配置（`application.yml`）
- 清理测试产生的日志、数据库测试数据；
- 关闭或释放压测使用的环境资源。

> 建议为压测撰写测试报告，包括环境、配置、场景、结果和结论，便于后续优化或复盘。
## 9. 示例 JMX
仓库中提供了一个基础的 JMeter 脚本，可根据场景调整线程数、请求体等参数：
- 文件路径：
  - codex-doc/20251022/jmter/sample_async_log_test.jmx
- 使用方式：
  - 在 JMeter 中打开该脚本，修改 BASE_URL、线程数、循环次数等变量，保存后即可执行。
  - > 建议将自定义后的脚本也纳入版本控制或归档，方便复现测试。
