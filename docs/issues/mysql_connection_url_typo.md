# 数据源 JDBC URL 拼写错误导致启动失败

## 1. 问题概述
- 发生时间：2025-10-22 15:20 左右，应用在启动阶段持续报错并最终退出（`exit code 130`）。
- 日志位置：`codex-doc/20251022/problem/log--v1.txt`。
- 典型日志：
  ```
  java.sql.SQLException: connect error, url jdbc:mysql:mysql2.sqlpub.com:3307/cmyshorturl?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai, driverClass com.mysql.cj.jdbc.Driver
      at com.alibaba.druid.pool.DruidAbstractDataSource.createPhysicalConnection(DruidAbstractDataSource.java:1793)
  ```
- 影响范围：所有依赖默认数据源配置的环境均无法获取数据库连接，健康检查 (`/actuator/health`) 报错并触发 Spring 应用关闭钩子。

## 2. 根因分析
- `application.yml` 中的数据源默认值写成 `jdbc:mysql:mysql2.sqlpub.com...`，缺失了协议后的 `//`。
- MySQL 驱动会按 `jdbc:<subprotocol>:<subname>` 解析 URL；缺少 `//` 时整个 `mysql2.sqlpub.com:3307/...` 会被当作 `subname`，驱动拿不到主机/端口信息，最终在建立物理连接时抛出 `connect error`。
- Druid 连接池尝试初始化物理连接失败，连续重试触发大量 `create connection SQLException` 日志。
- 由于连接池无法就绪，`DataSourceHealthIndicator` 无法调用 JDBC，进而导致健康检查异常并终止应用。

## 3. 处理过程
1. 复查日志确认所有失败堆栈均指向同一个 JDBC URL。
2. 对照 MySQL JDBC 规范，修正默认配置并同步更新环境变量：
   ```properties
   datasource:${DB_URL:jdbc:mysql://mysql2.sqlpub.com:3307/cmyshorturl?characterEncoding=utf8&serverTimezone=UTC}
   ```
3. 重启应用，Druid 成功建立连接，健康检查恢复正常，日志不再出现 `connect error`。

## 4. 经验教训与预防
- 变更 JDBC 配置时务必校验协议前缀和查询参数，必要时借助 IDE 或单元测试预先验证。
- 建议为关键配置添加启动前自检（例如解析 URL 并尝试建立短连接），提前捕获格式错误。
- 对外部提供的环境变量加强约束，可在 CI/CD 中引入静态检查或模板化配置，避免因手工拼写导致的宕机。
