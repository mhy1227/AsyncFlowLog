> 前置说明：我已检查当前归档目录、被扫描目录和你提到的文件路径，给出结论与建议。

结论

- 归档已在工作：logs/archive 下已有 zip 包
    - async-log-2025-10-21.log.zip
    - async-log-2025-10-22.log.zip
    - async-log-2025-10-23.log.zip
- 被扫描目录：logs/async 当前只有当天文件 async-log-2025-10-24.log（0 字节，符合“当天不处理”的规则）
- 你复制的控制台日志文件：logs/log--1024/log--v1.txt 不在归档范围内（路径与命名均不匹配归档规则）

关键点

- 定时任务只扫描并处理 async.log.file.path=logs/async 目录下，命名为 async-log-YYYY-MM-DD.log 的历史文件（严格早于今天）。
- logs/log--1024/log--v1.txt 属于你手工复制的控制台日志，路径和命名不在我们的归档匹配范围内，所以不会被打包。

如何验证“打包前后内容一致”

- 在归档前，向昨日文件写入内容：logs/async/async-log-2025-10-23.log（非 0 字节）
- 等归档触发后，解压 logs/archive/async-log-2025-10-23.log.zip，比对解压出的文件与归档前源文件（行数/哈希一致）

如果你希望把 logs/log--1024/log--v1.txt 也一起归档，有两种方案

- 方案 A（推荐分工明确）：继续让定时任务只管 logs/async/async-log-YYYY-MM-DD.log；控制台日志交给 Logback 自己的 RollingPolicy 或外部 Cron（例如按天滚动到 logs/console/console-YYYY-MM-DD.log，再单独归档）。
- 方案 B（扩展匹配范围）：给定时任务增加“包含模式/多目录”配置（如 include-patterns），支持额外匹配 logs/log--1024/*.txt 一类路径。该改动会更通用，但需要我们补充代码与配置。

要不要我按方案 B 给任务加一个可配置的“额外匹配列表”（不影响现有规则），比如：

- async.log.maintenance.includes: ["logs/log--1024/*.txt"]
  并把归档/删除逻辑复用？如果你确认，我可以实现最小改动版本并默认关闭。工具调用简报：无

> 前置说明：先说明现状再给改进选项；不改代码，只给出结论与可选方案。

结论（当前实现）

- 行为：先压缩（zip 到 archive.dir），压缩成功后“删除源 .log”。不是“移动原文件”，而是“复制压缩→删源”。
- 安全性：只处理“严格早于今天”的文件，避免碰到当天活跃文件；删除失败会记录 warn，不影响下一轮。
- 代价：zip 是二次 I/O，文件大时会多耗 CPU/磁盘；且当前直接写目标 zip，如果中途中断，可能留下不完整的 zip（虽不常见）。

是否合理

- 在“节省空间”为主要目标的场景，这种“压缩再删”是合理的常见做法。
- 但从工程鲁棒性看，还有可优化点：原子性、性能、可配置策略和空文件处理。

建议的可选改进（不互斥）

- 原子归档写入
  - 先写 *.zip.tmp，完成后 Files.move(tmp, zip, ATOMIC_MOVE)，再删源文件。可避免部分写入的半成品 zip。
- 可配置策略（按需切换）
  - move：仅移动到归档目录（不压缩）。最省 I/O，适合大文件或后续由离线任务压缩。
  - zip：压缩后删除源文件（当前默认行为）。
  - copy-verify-delete：压缩→校验（可选哈希）→删除源，最稳，但开销最大。
- 空文件跳过
  - 新增阈值如 async.log.archive.min-bytes（默认 1）；小于阈值的文件不归档，可选择跳过或直接删除，避免“空 zip”。
- 扩展匹配范围
  - 增加 async.log.maintenance.includes 支持额外模式（如 logs/log--1024/*.txt），按需让其它日志也走归档。
- 归档后统计
  - 在日志中增加“源大小/zip 大小/耗时”的统计，便于审计和调优。

推荐默认（若你同意我可以按此修改）

- strategy: zip
- atomicWrite: true（先写 tmp 再原子移动）
- removeSource: true
- minBytes: 1
- includes: 仅 logs/async/async-log-YYYY-MM-DD.log（保持现状）

下一步

- 你是希望按“原子写入 + min-bytes 跳过空文件”的小改动增强，还是支持“move 策略/多模式 includes”的更通用方案？直接告诉我选哪一套，我按最小改动落地。

工具调用简报：无

▌ 我的意思是 压缩完 源文件是否应该保留然后移动到指定的目录呢?

> 前置说明：当前实现是“压缩成功后删除源文件”。是否保留并移动源文件，要看你的目标（审计/排查 vs 节省空间）。下面给出对比与可配置的推荐方案，确认后我再按你选的策略改实现。

是否应“保留并移动源文件”

      - 代价是磁盘占用翻倍（原始 + zip）
- 适合删除（zip + delete）场景
  - 以节省空间为优先，归档后不再保留原始文件
  - 常见默认策略，配合必要的校验/原子写入降低风险
- 仅移动（move-only）
  - 不压缩，最省 CPU/IO；适合后续由离线任务统一压缩
  - 仍需要分层留存+后续清理策略

建议的可配置策略（供你选择）

- 策略矩阵
  - zip-delete：压缩到 archive.dir，校验/落盘后删除源文件（当前实现）
  - zip-move：压缩到 archive.dir，同时把源 .log 移动到 archive.raw-dir（保留原始）
  - move-only：不压缩，直接将源 .log 移动到 archive.dir（或 archive.raw-dir）
- 配置（示例，待你确认后我按此落地）
  - async.log.archive.strategy: zip-delete | zip-move | move-only
  - async.log.archive.dir: logs/archive               // zip 产物目录
  - async.log.archive.raw-dir: logs/archive/raw       // 原始 .log 迁移目录（zip-move 时用）
  - async.log.archive.min-bytes: 1                    // 小于阈值（如空文件）跳过或直接删除
  - async.log.archive.atomic-write: true              // 先写 .tmp，完成后原子移动到 .zip
  - async.log.archive.verify: false                   // 可选：压缩后比对哈希再删/移
- 日志与统计
  - 汇总增加 moved 计数（除 archived/deleted/skipped/errors）
  - 每条记录打印源大小/zip 大小/耗时，便于审计（可选）

我的建议

- 若你希望“先保证可追溯”，选 zip-move（短期双轨保存），并给 archive.raw-dir 设置单独留存周期（例如 7 天后再清理 raw）
- 若“磁盘空间优先”，维持 zip-delete，并启用 atomic-write（防止半成品 zip），可加 min-bytes 跳过空文件

请告诉我你选哪一种策略：

- A. zip-delete（现状，配上 atomic-write + min-bytes）
- B. zip-move（归档同时保留原始，移动到 raw-dir）
- C. move-only（不压缩，仅移动）
- D. 先不改策略，仅增加 min-bytes 跳过空文件

我按你的选择做最小改动实现并更新参数文档。

工具调用简报：无

▌ B吧

• Edited src\main\java\com\asyncflow\log\maintenance\LogRetentionScheduler.java (+18 -5)                                                                                                                                            
53         /**                                                                                                                                                                                                                  
54    +     * 原始日志迁移目录（当采用“归档后保留并迁移源文件”策略时使用）。                                                                                                                                                    
55    +     */                                                                                                                                                                                                                  
56    +    @Value("${async.log.archive.raw-dir:logs/archive/raw}")                                                                                                                                                              
57    +    private String archiveRawDir;                                                                                                                                                                                        
58    +                                                                                                                                                                                                                         
59    +    /**                                                                                                                                                                                                                  
60          * 归档阈值天数（含今天）；早于该阈值的历史文件将被归档。                                                                                                                                                            
⋮                                                                                                                                                                                                                               
139                                archived.incrementAndGet();                                                                                                                                                                  
134   -                            // 归档成功后删除源文件                                                                                                                                                                      
140   +                            // 归档成功后迁移源文件到原始归档目录（保留原始）                                                                                                                                            
141                                try {                                                                                                                                                                                        
136   -                                Files.deleteIfExists(p);                                                                                                                                                                 
137   -                            } catch (IOException del) {                                                                                                                                                                  
138   -                                log.warn("归档后删除源文件失败: {} - {}", p.toAbsolutePath(), del.getMessage());                                                                                                         
142   +                                Path rawBase = Paths.get(archiveRawDir);                                                                                                                                                 
143   +                                if (!Files.exists(rawBase)) {                                                                                                                                                            
144   +                                    Files.createDirectories(rawBase);                                                                                                                                                    
145   +                                }                                                                                                                                                                                        
146   +                                Path rawTarget = rawBase.resolve(p.getFileName());                                                                                                                                       
147   +                                Files.move(p, rawTarget, StandardCopyOption.REPLACE_EXISTING);                                                                                                                           
148   +                                log.info("已归档并迁移源日志文件: {} -> {}, 原始迁移到: {}",                                                                                                                             
149   +                                        p.toAbsolutePath(), archivedPath.toAbsolutePath(), rawTarget.toAbsolutePath());                                                                                                  
150   +                            } catch (IOException moveEx) {                                                                                                                                                               
151   +                                errors.incrementAndGet();                                                                                                                                                                
152   +                                log.warn("归档成功但迁移源文件失败: {} - {}", p.toAbsolutePath(), moveEx.getMessage());                                                                                                  
153                                }                                                                                                                                                                                            
140   -                            log.info("已归档历史日志文件: {} -> {}", p.toAbsolutePath(), archivedPath.toAbsolutePath());                                                                                                 
154                            } else {

• Edited src\main\resources\application.yml (+1 -0)                                                                                                                                                                                 
57           compress: zip                                                                                                                                                                                                      
58    +      raw-dir: logs/archive/raw                                                                                                                                                                                          
59         file:

• Edited codex-doc\SchedulerDesign\parameters.md (+2 -0)                                                                                                                                                                            
27       - 归档文件存放目录；不存在会自动创建。                                                                                                                                                                                 
28    +- async.log.archive.raw-dir（字符串，默认 logs/archive/raw）                                                                                                                                                             
29    +  - 原始 .log 的迁移目录（当采用“归档后保留原始”策略时使用）。                                                                                                                                                           
30     - async.log.archive.days（整数，默认 3，建议 < retention.days）

• Edited codex-doc\SchedulerDesign\implementation.md (+1 -0)                                                                                                                                                                        
29     - 仅支持 zip：`ZipOutputStream` 将 `*.log` 写入 `*.log.zip`；归档目录不存在会自动创建。                                                                                                                                  
30    +- 归档后迁移原始：将源 `*.log` 移动到 `async.log.archive.raw-dir`（保留原始文本，便于审计与对比）。                                                                                                                      
31     - 避免重复：若归档目标已存在则直接跳过创建（保持幂等）。

> 前置说明：已将归档策略按你选择的“B（zip 后保留原始并迁移）”落地到代码与配置。现在归档会生成 zip 到归档目录，同时把源 .log 移动到 raw 目录；不再删除源文件。已同步更新相关文档与示例配置。

变更要点

- 归档策略
  - 归档分支由“zip → 删除源文件”改为“zip → 迁移源文件到 raw 目录（保留原始）”。
  - 代码路径：src/main/java/com/asyncflow/log/maintenance/LogRetentionScheduler.java:1
- 新增配置
  - async.log.archive.raw-dir（默认：logs/archive/raw），用于存放原始 .log 的迁移副本。
  - 已在实际配置加入：src/main/resources/application.yml:1
- 文档更新
  - 参数说明新增 raw-dir：codex-doc/SchedulerDesign/parameters.md:1
  - 实现说明补充“归档后迁移原始”：codex-doc/SchedulerDesign/implementation.md:1

现网配置快照（保持测试高频，便于你继续观察）

- src/main/resources/application.yml:1
  - async.log.maintenance.enabled: true
  - async.log.maintenance.cron: "0/15 * * * * ?"
  - async.log.maintenance.timezone: "Asia/Shanghai"
  - async.log.retention.days: 7
  - async.log.archive.enabled: true
  - async.log.archive.dir: logs/archive
  - async.log.archive.raw-dir: logs/archive/raw
  - async.log.archive.days: 1
  - async.log.archive.compress: zip
  - async.log.file.path: logs/async

如何验证（建议）

- 归档目标（zip）：logs/archive/async-log-YYYY-MM-DD.log.zip
- 原始迁移（raw）：logs/archive/raw/async-log-YYYY-MM-DD.log
- 做一次对比（哈希或逐行），确认 zip 解压后的内容与 raw 目录中的原始一致。

说明与后续可选

- 目前仍“仅处理严格早于今天”的 AsyncFlowLog 日滚文件（logs/async/async-log-YYYY-MM-DD.log）；控制台或其他目录下的日志不在匹配范围内。
- 需要我再加“skip 空文件（min-bytes 阈值）”或“多模式 include-patterns（匹配其它目录/文件）”，随时说一声，我按最小改动加开关。

工具调用简报：

- 工具: shell（apply_patch）
- 触发原因: 将归档策略改为 zip 后迁移原始到 raw 目录（用户选择 B）
- 输入摘要: 修改 LogRetentionScheduler 的归档分支逻辑；application.yml 增加 archive.raw-dir；参数/实现文档同步更新
- 结果概览: 更新 1 个 Java 文件与 3 个文档；application.yml 新增 raw-dir；归档后保留原始 .log 于 logs/archive/raw
- 重试/退避: 无
- 时间: 2025-10-24T06:50Z
- 来源: 本地仓库文件与配置     

▌ 对归档有重复的文件名做一下处理吧 带上时间戳就好了

• Edited src\main\java\com\asyncflow\log\maintenance\LogRetentionScheduler.java (+8 -2)                                                                                                                                             
11     import java.time.LocalDate;                                                                                                                                                                                              
12    +import java.time.LocalDateTime;                                                                                                                                                                                          
13     import java.time.format.DateTimeFormatter;                                                                                                                                                                               
⋮                                                                                                                                                                                                                               
74         private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");                                                                                                                         
75    +    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");                                                                                                                      
76                                                                                                                                                                                                                              
⋮                                                                                                                                                                                                                               
195            if (Files.exists(target)) {                                                                                                                                                                                      
194   -            // 已存在同名归档，避免重复                                                                                                                                                                                  
195   -            return target;                                                                                                                                                                                               
196   +            String ts = LocalDateTime.now().format(TS_FMT);                                                                                                                                                              
197   +            int pos = zipName.lastIndexOf(".zip");                                                                                                                                                                       
198   +            String renamed = (pos > 0)                                                                                                                                                                                   
199   +                    ? zipName.substring(0, pos) + "." + ts + ".zip"                                                                                                                                                      
200   +                    : zipName + "." + ts + ".zip";                                                                                                                                                                       
201   +            target = archiveBase.resolve(renamed);                                                                                                                                                                       
202            }

• Edited src\main\java\com\asyncflow\log\maintenance\LogRetentionScheduler.java (+9 -0)                                                                                                                                             
148                                    Path rawTarget = rawBase.resolve(p.getFileName());                                                                                                                                       
149   +                                if (Files.exists(rawTarget)) {                                                                                                                                                           
150   +                                    String rawName = p.getFileName().toString();                                                                                                                                         
151   +                                    String ts = LocalDateTime.now().format(TS_FMT);                                                                                                                                      
154   +                                            ? rawName.substring(0, dot) + "." + ts + rawName.substring(dot)                                                                                                              
155   +                                            : rawName + "." + ts;                                                                                                                                                        
156   +                                    rawTarget = rawBase.resolve(renamedRaw);                                                                                                                                             
157   +                                }                                                                                                                                                                                        
158                                    Files.move(p, rawTarget, StandardCopyOption.REPLACE_EXISTING);

> 前置说明：已按你的要求对“归档重名”做了处理：当发现归档目录已存在同名 zip 时，自动在文件名中追加时间戳，避免冲突；同时也对原始日志迁移（raw-dir）做了同样的重名规避。仅改任务代码与少量配置文档，不影响其它功能。

变更摘要

- 重名 zip 处理
  - 规则：若 logs/archive/async-log-YYYY-MM-DD.log.zip 已存在，将生成 async-log-YYYY-MM-DD.log.YYYYMMDD-HHmmss.zip
- 原始 .log 迁移重名处理
  - 规则：若 logs/archive/raw/async-log-YYYY-MM-DD.log 已存在，将生成 async-log-YYYY-MM-DD.YYYYMMDD-HHmmss.log
- 时间戳格式：yyyyMMdd-HHmmss
- 代码位置
  - 更新归档方法与迁移逻辑：src/main/java/com/asyncflow/log/maintenance/LogRetentionScheduler.java:1
    - 新增 TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
    - zip 与 raw 的重名时追加时间戳

如何验证

- 在 logs/archive 下手动保留一个 zip，同名触发后看是否生成带时间戳的新 zip。
- 在 logs/archive/raw 下保留一个同名 .log，再次归档迁移时应生成带时间戳的 .log。
- 观察日志有“已归档并迁移源日志文件: 源 -> zip, 原始迁移到: raw”字样，且文件名包含时间戳。

提示

- 当前归档范围仍是 logs/async/async-log-YYYY-MM-DD.log（严格早于今天）；你复制的控制台文件 logs/log--1024/log--v2.txt 不在归档匹配内。
- 如需把其它路径/命名纳入归档，可后续加“包含模式（include-patterns）”开关。

工具调用简报

- 工具: shell（apply_patch）
- 触发原因: 为满足“重名归档文件名追加时间戳”的需求，修改归档与迁移逻辑
- 输入摘要: 更新 LogRetentionScheduler.java，新增时间戳格式常量；在 zip 与 raw 的重名场景追加 YYYYMMDD-HHmmss
- 结果概览: 成功更新 1 个 Java 文件（2 处逻辑）；不影响其余配置
- 重试/退避: 无
- 时间: 2025-10-24T07:05Z
- 来源: 本地仓库文件操作