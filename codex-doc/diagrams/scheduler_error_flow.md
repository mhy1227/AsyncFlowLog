# 定时任务异常流（Mermaid 流程图）

```mermaid
flowchart TD
  START([开始]) --> SCAN[扫描目录]
  SCAN -->|IO异常| E1[记录 warn: 扫描失败] --> END
  SCAN --> LOOP{文件匹配?}
  LOOP -- 否 --> NEXT[下一个文件]
  LOOP -- 是 --> DATE{日期 < 今天?}
  DATE -- 否 --> SKIP1[跳过(当天)] --> NEXT
  DATE -- 是 --> DEL{≤ 删除阈值?}
  DEL -- 是 --> RM[删除文件]
  RM -->|OK| NEXT
  RM -->|异常| E2[记录 warn: 删除失败] --> NEXT
  DEL -- 否 --> ARCH{启用归档 且 ≤ 归档阈值?}
  ARCH -- 否 --> SKIP2[跳过] --> NEXT
  ARCH -- 是 --> ZIP[zip 压缩]
  ZIP -->|异常| E3[记录 warn: 压缩失败] --> NEXT
  ZIP -->|OK| DELSRC[删除源文件]
  DELSRC -->|异常| E4[记录 warn: 归档后删源失败] --> NEXT
  DELSRC -->|OK| NEXT
  NEXT --> LOOP
  LOOP -->|结束| SUM[打印汇总 归档/删除/跳过/错误]
  SUM --> END([结束])
```
