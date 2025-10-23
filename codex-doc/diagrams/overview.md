# AsyncFlowLog 整体流程图（Mermaid）

目录
- 主流水线（本页第1节）
- 定时维护（本页第2节）
- 主链路时序：main_sequence.md
- 优雅停机时序：shutdown_sequence.md
- 定时任务时序：scheduler_sequence.md
- 定时任务异常流：scheduler_error_flow.md
- 组件关系：components.md
- 部署视图：deployment.md
- 文件轮转状态：file_rotation_state.md

## 1) 主流水线（生产 → 队列 → 消费 → 写入）
```mermaid
flowchart LR
  subgraph Producer[日志产生方]
    A[业务代码] -->|调用| B[AsyncLogService]
    A2[@OperationLog AOP] --> B
  end

  B --> Q[EventQueue\n(LinkedEventQueue)]
  Q --> C[ThreadPoolConsumer\n(N 线程)]
  C --> H[LogEventHandler\n(批量=100)]
  H --> AP[LogAppender 接口]
  AP --> F[FileAppender\nlogs/async/async-log-YYYY-MM-DD.log]
  AP -. 可扩展 .-> K[(Kafka/DB/ES ...)]

  subgraph Observability[可观测性]
    M[AsyncLogMetrics] -->|指标| ACT[Actuator/Micrometer]
    HC[AsyncLogHealthIndicator] --> ACT
  end

  classDef dashed stroke-dasharray: 3 3
  class K dashed
```

## 2) 定时维护（归档 + 清理）
```mermaid
flowchart TD
  T[Cron 触发 @Scheduled] --> S[扫描目录\nasync.log.file.path]
  S --> F{文件名匹配\nasync-log-YYYY-MM-DD.log?}
  F -- 否 --> SKIP1[跳过]
  F -- 是 --> D{日期 < 今天?}
  D -- 否 --> SKIP2[跳过(当天文件)]
  D -- 是 --> DEL{日期 ≤ 今天 - retention.days?}
  DEL -- 是 --> X[删除]
  DEL -- 否 --> ARCH{archive.enabled 且\n日期 ≤ 今天 - archive.days?}
  ARCH -- 是 --> Z1[zip 压缩至 archive.dir] --> Z2[删除源文件]
  ARCH -- 否 --> SKIP3[跳过]
  X --> SUM[汇总: 归档/删除/跳过/错误]
  Z2 --> SUM
  SKIP1 --> SUM
  SKIP2 --> SUM
  SKIP3 --> SUM
```

> 注：仅处理“严格早于今天”的日志文件；当天活跃文件不归档、不删除。
