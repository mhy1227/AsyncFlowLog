# 部署视图（Mermaid）

```mermaid
flowchart LR
  subgraph Single[单实例]
    App1[AsyncFlowLog 应用]
    FS1[(FileSystem)]
    DB1[(DB/MySQL)]
    App1 --- FS1
    App1 --- DB1
  end

  subgraph Cluster[多实例]
    AppA[App A]
    AppB[App B]
    FS[(共享存储或各自磁盘)]
    DB[(DB/MySQL)]
    AppA --- FS
    AppB --- FS
    AppA --- DB
    AppB --- DB

    subgraph Mutex[互斥选项]
      Shed[ShedLock/DB锁]
      Cron[K8s CronJob/外部调度]
    end
  end

  note right of Cluster: 定时任务只应跑一次\n— 仅单节点开启\n— 或使用分布式锁\n— 或交由外部Cron
```
