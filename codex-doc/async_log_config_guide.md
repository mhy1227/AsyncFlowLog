# AsyncFlowLog 配置参数指南

本文档介绍 `src/main/resources/application.yml` 中常用的配置项，说明每个参数的作用、默认值与常见调优建议。所有配置都可通过 Spring Boot 的标准机制（`application.yml`、环境变量、命令行参数等）覆盖。

---

## 1. 基础配置

```yaml
spring:
  application:
    name: async-flow-log
  datasource:
    # 建议通过环境变量 DB_URL/DB_USERNAME/DB_PASSWORD 注入，不要在仓库中存放真实凭据
    # 这里的默认值仅为示例，请按照自己的环境替换 <host>/<database>/<user>/<password>
    url: ${DB_URL:jdbc:mysql://<host>:3307/<database>?characterEncoding=utf8&serverTimezone=UTC}
    username: ${DB_USERNAME:your-username}
    password: ${DB_PASSWORD:your-password}
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
```

### 1.1 应用基本信息
- `spring.application.name`：服务名称，影响日志前缀、注册中心显示等；可按环境覆盖（例如 `async-flow-log-dev`）。

### 1.2 数据源（Druid）
- `spring.datasource.url/user/password`：JDBC 连接信息；可以通过环境变量 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 覆盖，记得包含 `jdbc:mysql://` 前缀。
- `driver-class-name`：MySQL 8 使用 `com.mysql.cj.jdbc.Driver`。
- `type`: 指定使用 DruidDataSource。
- `druid.initial-size/min-idle/max-active`：控制连接池容量（初始、最小空闲、最大活动连接数）。
- `druid.max-wait`：获取连接的最长等待时间（毫秒），超过会抛出异常。
- `validation-query`：连接保活语句，建议用轻量级查询（`SELECT 1`）。
- `test-while-idle/test-on-borrow/test-on-return`：控制连接活性检查行为；默认只在空闲时检测。
- `pool-prepared-statements` 与 `max-pool-prepared-statement-per-connection-size`：开启 PSCache，适合频繁执行重复 SQL。
- `filters`：启用统计、防火墙等过滤器（`stat,wall`）。
- `connection-properties`：附加 Druid 配置，这里开启慢 SQL 统计。

> **注意**：数据库连通性问题（如 URL 拼写错误、网络不可达）会导致 Druid 重试并在日志中报错。确认 `jdbc:mysql://` 前缀与正确的主机、端口、库名无误。

---

## 2. MyBatis 配置

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.asyncflow.log.model.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
```

- `mapper-locations`：XML Mapper 文件路径，默认 `classpath:mapper/*.xml`。
- `type-aliases-package`：实体类所在包，可用别名简化 XML 中的类名。
- `configuration.map-underscore-to-camel-case`：启用下划线转驼峰映射，例如 `log_time` → `logTime`。
- `cache-enabled`：是否启用 MyBatis 二级缓存；默认关闭，避免缓存导致的脏数据。

> 常见问题：
> - Mapper 重复扫描：确保只有一个 `@MapperScan` 或适当配置 `mybatis.mapper-locations`，避免重复注册。
> - XML 未加载：检查 `classpath` 路径是否正确；日志中会打印 “成功加载 N 个 Mapper XML 文件”。

---

## 3. 异步日志核心配置 (`async.log.*`)

```yaml
async:
  log:
    queue:
      type: linked
      capacity: 10000
    consumer:
      core-size: 2
      max-size: 4
      keep-alive: 60
    appender:
      type: file
      file-path: logs/async
      batch-size: 100
      flush-interval: 1000
```

### 3.1 队列 (`async.log.queue`)
- `type`: 队列实现类型。
  - `linked`（默认）：使用 `LinkedBlockingQueue`。
  - 若后续扩展，可支持 `array`、`disruptor` 等自定义实现。
- `capacity`: 队列容量（仅对有界队列有效）。容量越大，能缓冲的日志越多，但占用内存更高。建议结合峰值压力与内存情况调整。

常见搭配：
- **低延迟场景**：`capacity` 取 1000~5000，确保消费线程能及时处理，避免长时间排队。
- **高吞吐场景**：`capacity` 取 20000 以上，并结合批量写入/磁盘吞吐能力使用，防止洪峰时丢日志。
- **紧凑内存环境**：控制在 1000 左右，同时将日志落到 MQ 或缩短批量写入，减少内存占用。

### 3.2 消费者线程池 (`async.log.consumer`)
- `core-size`: 核心线程数；决定常态下同时处理日志的线程数量。日志写入 I/O 较快时不宜设置过大，避免上下文切换开销。
- `max-size`: 最大线程数；用于应对短期洪峰。超过此值的新任务会触发拒绝策略（默认 `CallerRunsPolicy`）。
- `keep-alive`: 空闲线程存活时间（秒），超过时间且线程数超出核心线程数时会回收。

调优建议：
- 当 `queue.capacity` 长期占满、日志延迟升高时，可考虑增加 `core-size` 或 `max-size`。
- 当线程数很高，但 CPU 使用率不高，反而可能增加调度开销，适当降低线程数。
- 如果写入目标是本地 SSD，通常 `core-size=2~4` 即可；若写入的是远程服务（如 MQ），可以适当提高核心线程数以提升并发度。
- 建议结合 `AsyncLogMetrics` 暴露的 `consumer pool active count`、`completed task count` 指标观察真实负载。

### 3.3 写入器 (`async.log.appender`)
- `type`: 落盘方式。
  - `file`（默认）：写入本地文件；配合 `async` 目录，可由 Filebeat 等工具收集。
  - 其他类型（如 `kafka`、`db`）可以通过自定义 `LogAppender` 实现实现。
- `file-path`: 文件目录。确保应用有写权限；生产环境最好放在独立分区或挂载点。
- `batch-size`: 批量写入的最大条数。批量写入可以减少磁盘 I/O，但会增加一点延迟。若业务要求实时性强，可调小；对吞吐量要求高可调大。
- `flush-interval`: （毫秒）若检查写入器支持定时 flush，可结合 `auto-flush` 使用；当前 `FileAppender` 中未使用该字段，可留作扩展。
- `auto-flush`（未配置时默认为 false）：如启用，在每次写入后立即 `flush`，实时性好但 I/O 次数大。

> **常见问题**：
> - 文件为空：说明未触发任何日志事件；需在业务中调用 `AsyncLogService.log(...)` 或触发带 `@OperationLog` 的接口。
> - 文件写失败：检查 `file-path` 是否存在、权限是否足够。生产环境建议定期归档、清理文件，避免磁盘占满。

#### 3.3.1 切换到 Kafka 写入示例

1. **实现写入器**（示例伪代码）：
   ```java
   @Component
   public class KafkaLogAppender extends AbstractLogAppender {
       private final KafkaProducer<String, String> producer;

       @Override
       protected boolean doInitialize() {
           Properties props = new Properties();
           props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
           // ... 其他参数
           producer = new KafkaProducer<>(props);
           return true;
       }

       @Override
       protected boolean doAppend(LogEvent event) {
           producer.send(new ProducerRecord<>(kafkaProperties.getTopic(), event.getLogId(), format(event)));
           return true;
       }

       @Override
       protected void doClose() {
           producer.flush();
           producer.close();
       }
   }
   ```

2. **扩展 `AppenderFactory`**：在 `createAppender()` 中识别 `type=kafka`，返回 `KafkaLogAppender`。

3. **YAML 配置**：
   ```yaml
   async:
     log:
       appender:
         type: kafka
         kafka:
           bootstrap-servers: kafka:9092
           topic: async-log
           acks: all
           retries: 3
           batch-size: 16384
   ```

4. **注意事项**：
   - 发送失败要有重试或降级方案，必要时写入本地 WAL；
   - 消费端要做好幂等处理，可使用 `logId` 作为 key；
   - 监控 MQ 指标（发送延迟、失败率），以及消费端积压情况。

#### 3.3.2 写入数据库示例

如果希望直接将日志写入表，可实现 `DatabaseLogAppender`，在 `doAppend()` 中调用 MyBatis/Repository 插入。注意：

- 日志量大时对 DB 压力较大，建议做好分库/分表或归档；
- 可复用现有的 `log_file` / `log_index` 设计，或另外建表；
- 结合事务和重试，保证插入失败时不会阻塞主流程。

---

## 4. Actuator 与 Logging

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    root: INFO
    com.asyncflow.log: DEBUG
  file:
    name: logs/asyncflow.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 4.1 Actuator 配置
- `management.endpoints.web.exposure.include`: 允许访问的 web 端点（健康检查、信息、指标等）。
- `management.endpoint.health.show-details`: 设置健康检查时显示详细信息，便于排查。

> 通过 `http://<host>:<port>/actuator/health` 可以检查服务健康状态；若数据库不可用，会在此返回 `DOWN` 并附带异常信息。

### 4.2 日志级别与输出
- `logging.level.root`: 根日志级别。
- `logging.level.com.asyncflow.log`: 项目包范围内的日志级别，默认 DEBUG，便于调试。
- `logging.file.name`: 主日志文件路径。AsyncFlowLog 的运行日志会写入这里。
- `logging.pattern.console/file`: 控制台和文件的日志格式。

> 若要减少日志噪音，可将 `com.asyncflow.log` 调整到 INFO；生产环境建议把 debug 调优日志关闭或定向输出。

#### 4.2.1 多环境配置示例

可以创建 `application-dev.yml`、`application-prod.yml` 等配置文件，按环境差异化设置参数：

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://dev-db:3307/dev_db
logging:
  level:
    com.asyncflow.log: DEBUG
async:
  log:
    queue:
      capacity: 5000

# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/prod_db
logging:
  level:
    com.asyncflow.log: INFO
async:
  log:
    queue:
      capacity: 20000
    consumer:
      core-size: 4
      max-size: 8
```

启动时通过 `--spring.profiles.active=dev` 或设置环境变量 `SPRING_PROFILES_ACTIVE=prod` 来选择配置文件。也可以在 `application.yml` 顶部增加 `spring.profiles.group` 来组合多个配置文件。

---

## 5. 覆盖配置的方式

1. **环境变量**：在运行前设置 `SPRING_DATASOURCE_URL`、`ASYNC_LOG_QUEUE_CAPACITY` 等环境变量，Spring Boot 会自动映射。
2. **命令行参数**：`java -jar app.jar --async.log.consumer.core-size=4`。
3. **配置文件**：可以创建 `application-dev.yml` 等多环境配置，结合 `--spring.profiles.active=dev` 切换。
4. **配置中心**：若接入 Spring Cloud Config/Nacos 等，可动态刷新配置。

---

## 6. 常见调优场景

| 场景 | 调整建议 |
| --- | --- |
| 队列长时间占满 | 增大 `async.log.queue.capacity`、增加消费者线程数、优化写入器（批量写或更快的存储）。 |
| 日志写入延迟大 | 减少 `batch-size`、开启 `auto-flush`、增加消费者线程、检查磁盘性能。 |
| 需要更可靠的存储 | 将 `async.log.appender.type` 换成自定义的 MQ / DB 写入器，或实现 WAL。 |
| 数据库健康检查超时 | 检查 JDBC URL、网络、账号权限；适当调整 Druid 超时配置。 |
| 日志文件过大 | 配合运维脚本或日志轮转工具（logrotate、Filebeat）定期归档。 |
| 低配服务器写入吃紧 | 降低 `core-size`、缩短 `batch-size`、将日志写入本地后统一异步上传。 |
| 需要多租户隔离 | 在 `LogEventFactory` 中添加租户标识字段，结合写入器按租户拆分目录/Topic。 |

---

## 7. 添加新配置项的建议

- 在 `application.yml` 中添加统一前缀，如 `async.log.retry.max-attempts`，再让对应 Bean（如 `LogEventHandler` 或 `LogAppender`）读取使用。
- 对于复杂配置，考虑定义 `@ConfigurationProperties` 类，便于校验与 IDE 补全。
- 记得在文档中更新说明，并提供默认值与调优建议。

---

> 参考：
> - `src/main/java/com/asyncflow/log/config/QueueConfig.java`
> - `src/main/java/com/asyncflow/log/config/ConsumerConfig.java`
> - `src/main/java/com/asyncflow/log/config/AppenderConfig.java`
> - `src/main/java/com/asyncflow/log/service/impl/AsyncLogServiceImpl.java`
>
> 这些类展示了配置如何被注入并应用，可根据需要自行扩展。
---

## 参数速查表

| 分类 | 参数名 | 默认值 | 说明 | 调优提示 |
| --- | --- | --- | --- | --- |
| 应用 | `spring.application.name` | `async-flow-log` | 应用名，用于日志/注册中心显示 | 多环境可追加后缀，如 `-dev`、`-prod` |
| 数据源 | `spring.datasource.url` | `jdbc:mysql://<host>:3307/<database>?...` | JDBC 连接串 | 建议改用环境变量 `DB_URL`，注意包含 `jdbc:mysql://` |
| 数据源 | `spring.datasource.username` | `your-username` | 数据库账号 | 通过 `DB_USERNAME` 注入，避免写入仓库 |
| 数据源 | `spring.datasource.password` | `your-password` | 数据库密码 | 使用 `DB_PASSWORD` 注入 |
| 数据源 | `spring.datasource.druid.initial-size` | `5` | 初始化连接数 | 并发高时可增大，但受限于数据库最大连接数 |
| 数据源 | `spring.datasource.druid.max-active` | `20` | 最大活动连接数 | 避免设置超过数据库上限，必要时做连接池隔离 |
| 队列 | `async.log.queue.type` | `linked` | 队列实现类型 | 可扩展为 `array`、`disruptor` 等自定义实现 |
| 队列 | `async.log.queue.capacity` | `10000` | 队列容量 | 洪峰大时增大容量，内存紧张时配合批量写减少积压 |
| 线程池 | `async.log.consumer.core-size` | `2` | 核心消费者线程数 | 写入慢或队列堆积时可提升至 4~8，注意 CPU 占用 |
| 线程池 | `async.log.consumer.max-size` | `4` | 最大线程数 | 应对突发流量，配合合理的拒绝策略 |
| 写入器 | `async.log.appender.type` | `file` | 日志目标类型 | 可扩展为 `kafka`、`db`、`es` 等 |
| 写入器 | `async.log.appender.file-path` | `logs/async` | 文件输出目录 | 确保写权限，建议结合日志采集组件 |
| 写入器 | `async.log.appender.batch-size` | `100` | 批量写入条数 | 实时性要求高时调低，吞吐优先场景调高 |
| 监控 | `management.endpoints.web.exposure.include` | `health,info,metrics` | Actuator 暴露端点 | 生产环境慎重开放，配合权限控制 |
| 日志 | `logging.level.com.asyncflow.log` | `DEBUG` | 项目包日志级别 | 调试阶段可留在 DEBUG，上线建议降为 INFO |

如需扩展新的参数，推荐使用同一命名空间（如 `async.log.retry.*`），并在文档中补充说明，上线前通过 Smoketest 或灰度验证配置有效性。

---

## 发行与集成指南

### 1. 打包与发布

1. **本地构建**：`mvn clean package -DskipTests`
2. **安装到本地仓库**（供本机其他项目引用）：`mvn clean install`
3. **发布到私服**：在 `pom.xml` 配置 `<distributionManagement>` 后执行 `mvn deploy`；如需更新版本可先运行 `mvn versions:set -DnewVersion=<new_version>`。

### 2. 业务项目引入依赖

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>asyncflowlog</artifactId>
    <version>1.0.0</version>
</dependency>
```

确保业务应用的 `@SpringBootApplication` 能扫描到 `com.asyncflow.log` 包（默认扫描子包时无需额外配置）。启动后在 Actuator `/actuator/health` 中可看到 `asynclog` 指标。

### 3. Maven 多模块场景

- 在父工程 `<modules>` 中添加 `asyncflowlog` 子模块，业务模块通过 `<dependency>` 引用；
- 或者保持仓库独立，通过 `mvn install`/私服提供 jar 依赖。

### 4. 验证步骤

1. 启动业务应用，确认 `AsyncLogService` 注入成功；
2. 触发一次日志事件（调用 `asyncLogService.info(...)` 或访问带 `@OperationLog` 的接口），查看 `logs/asyncflow.log` 或指定写入目标是否生成记录；
3. 观察队列、线程池与写入器指标（参考上文监控章节），确保配置达到预期效果。

> 建议在团队 README 或开发文档中记录依赖版本、打包命令与常用配置示例，方便团队成员复用。
