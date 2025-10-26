# JMeter 压测结果解读模板（Template）

> 目的：给团队一个统一的结果解读框架，便于快速出结论与对齐优化方向。

## 1. 测试卡片（Test Card）
- 场景名称：
- 目标接口/系统：
- 版本/提交：
- 环境信息：
  - 应用版本 / JVM / 容器参数：
  - 依赖系统 / DB / 限额：
- 流量模型：并发（峰值/平均）、RPS、持续时长、RAMP-UP：
- 数据与脚本：JMX、CSV 数据、ThinkTime：

## 2. 关键指标（KPI）
| 指标 | 值 | 目标/阈值 | 说明 |
|---|---:|---:|---|
| 吞吐（Req/s） |  |  | Aggregate Report / Summary 中的 Throughput |
| 平均响应时间 |  |  | Average |
| 90/95/99 分位 |  |  | 90%/95%/99% Line |
| 最大响应时间 |  |  | Max |
| 错误率 |  |  | Error %（应 < 1% 或按需） |
| 并发峰值 |  |  | Active Threads over Time |

## 3. 结果抓取（What to read）
- JMeter 报表/监听器：
  - Summary Report / Aggregate Report：吞吐、平均、分位、错误率
  - Response Time Percentiles / Times：时延分布
  - Active Threads / Hits per Second：并发曲线与压测形状
  - View Results Tree（抽样）：错误样本、响应体校验（code、payload）
- 应用侧与系统侧：
  - 应用日志（ERROR/WARN、GC、拒绝/限流）
  - DB/依赖指标（连接、QPS、错误、限额命中）
  - 系统资源（CPU、内存、I/O、网络）

## 4. 结论框架（So what）
- 目标是否达成（TPS / 时延 / 错误率）：
- 瓶颈/异常点（应用、DB、外部依赖、资源）：
- 证据链（指标截图/日志片段/SQL/错误栈）：

## 5. 常见问题定位（Troubleshooting）
- 错误率高：
  - 查看 Response Code、断言；看应用 ERROR/WARN、依赖错误（网络/DB）
- 时延长：
  - 看分位数时延曲线、GC/CPU、慢 SQL/锁等待、外部调用
- 吞吐不足：
  - 看 Active Threads 与 Hits per Second；是否被限流/连接耗尽/队列满
- 依赖/配额命中：
  - 典型如 MySQL `max_questions`、连接池上限、外部 API 限流

## 6. 建议动作（Playbook）
- 压测侧：
  - RAMP-UP、分阶段升压、误差采样（预热）
  - 客户端/脚本参数对齐：并发、连接复用、ThinkTime、连接超时
- 应用侧：
  - 线程池/队列/连接池容量与策略；批量、降级、限流、熔断
- 依赖侧：
  - 数据库/缓存/外部 API 的配额与连接；慢查询与索引
- 观测侧：
  - 指标与日志的维度与粒度（按接口/依赖）

## 7. 结果摘要（一页）
- 核心结论：
- 指标表：吞吐、分位、错误率、峰值并发
- 主要问题与建议：
- 风险与下一步：

## 8. 附录（可选）
- 采集脚本与命令（示例）
```
# 无头压测
jmeter -n -t test.jmx -l results.jtl -e -o report-dir
# 将 jtl 转 CSV、用工具提取分位
```
- 术语速查：Percentiles、RPS/TPS、RAMP-UP、Warm-Up
