# 异步日志系统监控模块测试指南

本文档提供了对AsyncFlowLog系统监控模块进行测试的详细指南，包括单元测试、集成测试和手动测试方法。

## 1. 监控模块概述

AsyncFlowLog系统的监控模块由以下组件组成：

1. **健康检查** (`AsyncLogHealthIndicator`) - 提供系统健康状态检查
2. **指标监控** (`AsyncLogMetrics`) - 使用Micrometer收集系统性能指标
3. **监控API** (`AsyncLogMonitorController`) - 提供REST API接口查询监控数据

## 2. 单元测试

### 2.1 运行测试命令

```bash
# 运行所有测试
mvn test

# 只运行监控模块相关测试
mvn test -Dtest=AsyncLogHealthIndicatorTest,AsyncLogMetricsTest,AsyncLogMonitorControllerTest
```

### 2.2 健康检查测试 (`AsyncLogHealthIndicatorTest`)

`AsyncLogHealthIndicatorTest`测试了以下场景：

- **正常状态**: 队列使用率低，服务正常运行
- **警告状态**: 队列使用率超过80%阈值
- **故障状态**: 队列使用率超过95%阈值
- **故障状态**: 消费者线程池已关闭
- **故障状态**: 服务未运行
- **异常处理**: 健康检查过程中发生异常

### 2.3 指标监控测试 (`AsyncLogMetricsTest`)

`AsyncLogMetricsTest`测试了以下功能：

- **指标绑定**: 将指标绑定到Micrometer注册表
- **计数器更新**: 成功/失败/丢弃计数器的更新
- **服务状态指标**: 通过布尔值指标表示服务状态
- **队列使用率指标**: 通过百分比表示队列使用情况
- **getter方法**: 验证各计数器的getter方法

### 2.4 监控API测试 (`AsyncLogMonitorControllerTest`)

`AsyncLogMonitorControllerTest`测试了以下API端点：

- **/status**: 提供系统状态信息
- **/metrics**: 提供指标数据
- **/info**: 提供系统信息和资源使用情况
- **无指标场景**: 测试未启用指标监控的情况

## 3. 集成测试

### 3.1 准备工作

1. 启动应用程序：

```bash
mvn spring-boot:run
```

2. 确保应用程序启动正常，并且监控模块正确加载

### 3.2 测试健康检查端点

使用cURL或浏览器访问健康检查端点：

```bash
curl http://localhost:8080/actuator/health
```

预期返回内容：

```json
{
  "status": "UP",
  "components": {
    "asyncLog": {
      "status": "UP",
      "details": {
        "queue.size": 0,
        "queue.capacity": 1000,
        "queue.usage": "0.00%",
        "consumer.activeThreads": 2,
        "consumer.isShutdown": false,
        "consumer.completedTasks": 0,
        "status": "异步日志系统运行正常"
      }
    },
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### 3.3 测试Prometheus指标

访问Prometheus指标端点：

```bash
curl http://localhost:8080/actuator/prometheus
```

验证输出中包含以下自定义指标：

- `asynclog_queue_size`
- `asynclog_queue_capacity`
- `asynclog_queue_usage`
- `asynclog_consumer_active_threads`
- `asynclog_consumer_completed_tasks`
- `asynclog_service_running`
- `asynclog_events_success_count`
- `asynclog_events_failure_count`
- `asynclog_events_discarded_count`
- `asynclog_events_success_rate`

### 3.4 测试监控API端点

```bash
# 获取系统状态
curl http://localhost:8080/api/asynclog/monitor/status

# 获取指标数据
curl http://localhost:8080/api/asynclog/monitor/metrics

# 获取系统信息
curl http://localhost:8080/api/asynclog/monitor/info
```

验证每个端点返回的数据结构和内容是否符合预期

## 4. 手动测试场景

### 4.1 测试队列使用率阈值

1. 创建一个测试控制器或使用已有控制器生成大量日志：

```java
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private AsyncLogService asyncLogService;
    
    @GetMapping("/generate-logs")
    public String generateLogs(@RequestParam(defaultValue = "1000") int count) {
        for (int i = 0; i < count; i++) {
            asyncLogService.info("测试日志 #" + i);
        }
        return "已生成 " + count + " 条日志";
    }
}
```

2. 使用cURL或浏览器生成大量日志：

```bash
curl "http://localhost:8080/api/test/generate-logs?count=5000"
```

3. 立即检查健康状态：

```bash
curl http://localhost:8080/actuator/health
```

4. 验证预期行为：
   - 当队列使用率超过80%时，健康状态应变为`OUT_OF_SERVICE`
   - 当队列使用率超过95%时，健康状态应变为`DOWN`

### 4.2 测试指标计数器

1. 创建一个测试接口增加各种指标计数：

```java
@GetMapping("/test-metrics")
public String testMetrics(@RequestParam String type) {
    AsyncLogMetrics metrics = applicationContext.getBean(AsyncLogMetrics.class);
    
    switch (type) {
        case "success":
            metrics.incrementSuccessCount();
            return "成功计数器+1";
        case "failure":
            metrics.incrementFailureCount();
            return "失败计数器+1";
        case "discard":
            metrics.incrementDiscardedCount();
            return "丢弃计数器+1";
        default:
            return "未知类型";
    }
}
```

2. 使用cURL增加各类计数：

```bash
# 增加成功计数
curl "http://localhost:8080/api/test/test-metrics?type=success"

# 增加失败计数
curl "http://localhost:8080/api/test/test-metrics?type=failure"

# 增加丢弃计数
curl "http://localhost:8080/api/test/test-metrics?type=discard"
```

3. 验证指标变化：

```bash
curl http://localhost:8080/api/asynclog/monitor/metrics
```

### 4.3 测试服务状态监控

1. 停止异步日志服务：

```java
@GetMapping("/stop-service")
public String stopService() {
    asyncLogService.shutdown();
    return "异步日志服务已停止";
}
```

2. 访问接口停止服务：

```bash
curl http://localhost:8080/api/test/stop-service
```

3. 检查健康状态和指标：

```bash
curl http://localhost:8080/actuator/health

curl http://localhost:8080/api/asynclog/monitor/status
```

4. 预期结果：
   - 健康状态应变为`DOWN`，原因是"异步日志服务未运行"
   - 状态API应显示`service_running: false`

## 5. 性能测试

### 5.1 准备工作

1. 安装性能测试工具如JMeter或wrk
2. 配置测试脚本，例如wrk命令：

```bash
wrk -t8 -c100 -d30s "http://localhost:8080/api/test/generate-logs?count=1"
```

### 5.2 测试场景

1. **低负载测试**:
   - 使用少量并发用户（如10）
   - 持续5分钟
   - 验证指标稳定

2. **中负载测试**:
   - 使用中等并发用户（如50）
   - 持续5分钟
   - 验证队列使用率和处理速率

3. **高负载测试**:
   - 使用大量并发用户（如200）
   - 持续5分钟
   - 验证系统是否进入警告或故障状态

### 5.3 数据收集

在测试过程中，每分钟记录一次以下指标：

- 队列使用率
- 活跃线程数
- 成功/失败/丢弃计数
- 成功率
- JVM内存使用率

### 5.4 结果分析

分析收集的数据，验证以下内容：

- 系统在各种负载下的稳定性
- 警告和故障阈值是否正确触发
- 系统资源使用情况
- 性能瓶颈和优化机会

## 6. 可视化监控测试

### 6.1 Prometheus配置

1. 安装Prometheus并配置抓取应用指标：

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'asyncflow'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['localhost:8080']
```

2. 启动Prometheus：

```bash
prometheus --config.file=prometheus.yml
```

### 6.2 Grafana仪表盘

1. 安装并启动Grafana
2. 添加Prometheus数据源
3. 创建仪表盘，包含以下面板：
   - 队列使用率曲线图
   - 处理成功率饼图
   - 活跃线程数表格
   - 已完成任务数计数器
   - 各类计数器时序图

4. 在执行手动测试和性能测试时，观察仪表盘变化

## 7. 故障模拟与恢复测试

### 7.1 模拟队列满场景

1. 临时降低队列容量配置（如修改为10）
2. 生成大量日志
3. 验证健康状态变为`DOWN`
4. 恢复队列容量配置
5. 验证健康状态恢复

### 7.2 模拟消费者线程池故障

1. 关闭消费者线程池
2. 验证健康状态变为`DOWN`
3. 重启消费者线程池
4. 验证健康状态恢复

### 7.3 模拟系统资源不足

1. 限制JVM可用内存
2. 在高负载下运行系统
3. 观察系统行为和监控指标
4. 恢复正常配置

## 8. 测试结果记录模板

```
# 监控模块测试报告

## 基本信息
- 测试日期: YYYY-MM-DD
- 测试人员: XXX
- 系统版本: X.X.X
- 测试环境: [开发/测试/生产]

## 单元测试结果
- AsyncLogHealthIndicatorTest: [通过/失败]
- AsyncLogMetricsTest: [通过/失败]
- AsyncLogMonitorControllerTest: [通过/失败]

## 集成测试结果
- 健康检查端点: [通过/失败]
- Prometheus指标: [通过/失败]
- 监控API: [通过/失败]

## 手动测试结果
- 队列使用率阈值测试: [通过/失败]
- 指标计数器测试: [通过/失败]
- 服务状态监控测试: [通过/失败]

## 性能测试结果
- 低负载测试: [通过/失败]
- 中负载测试: [通过/失败]
- 高负载测试: [通过/失败]

## 故障模拟测试结果
- 队列满场景: [通过/失败]
- 消费者线程池故障: [通过/失败]
- 系统资源不足: [通过/失败]

## 问题与建议
1. [问题描述]
   - [解决方案或建议]
2. [问题描述]
   - [解决方案或建议]

## 结论
[总体测试结论]
```

## 9. 常见问题与排查

1. **健康检查显示DOWN但系统似乎正常**
   - 检查是否队列使用率超过阈值
   - 验证消费者线程池是否正常运行
   - 查看日志是否有异常

2. **指标不更新**
   - 确认AsyncLogMetrics被正确注入
   - 检查配置是否启用了指标（`async.log.monitor.metrics.enabled=true`）
   - 验证指标注册表是否正常工作

3. **监控API返回500错误**
   - 检查依赖注入是否正确
   - 查看应用日志中的异常信息
   - 验证配置是否启用了API（`async.log.monitor.api.enabled=true`）

## 10. 总结

完整的监控模块测试应覆盖单元测试、集成测试、手动测试、性能测试和故障模拟测试。通过这些测试，我们可以确保监控模块能够准确反映系统状态，及时发现潜在问题，并在高负载下保持稳定性。测试结果应记录在标准报告中，以供团队成员参考和改进。 