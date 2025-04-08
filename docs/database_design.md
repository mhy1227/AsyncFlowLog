# 数据库设计文档

## 1. 设计原则

1. **日志存储原则**
   - 主要存储：文件系统（日志文件）
   - 数据库存储：仅存储日志元数据和索引信息
   - 内存队列：用于异步处理

2. **数据库角色**
   - 存储日志文件信息
   - 提供日志查询接口
   - 管理日志生命周期
   - 支持日志归档

## 2. 表结构设计

### 2.1 日志文件表 (log_file)
```sql
CREATE TABLE IF NOT EXISTS log_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_name VARCHAR(100) NOT NULL COMMENT '文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    level VARCHAR(10) NOT NULL COMMENT '日志级别',
    status VARCHAR(20) NOT NULL COMMENT '状态(ACTIVE/ARCHIVED/DELETED)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_file_path (file_path),
    INDEX idx_level (level),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志文件表';
```

### 2.2 日志索引表 (log_index)
```sql
CREATE TABLE IF NOT EXISTS log_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    file_id BIGINT NOT NULL COMMENT '文件ID',
    line_number INT NOT NULL COMMENT '行号',
    log_time DATETIME NOT NULL COMMENT '日志时间',
    level VARCHAR(10) NOT NULL COMMENT '日志级别',
    keyword VARCHAR(100) COMMENT '关键词',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_file_id (file_id),
    INDEX idx_log_time (log_time),
    INDEX idx_level (level),
    INDEX idx_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志索引表';
```

### 2.3 日志归档表 (log_archive)
```sql
CREATE TABLE IF NOT EXISTS log_archive (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    file_id BIGINT NOT NULL COMMENT '原文件ID',
    archive_path VARCHAR(500) NOT NULL COMMENT '归档路径',
    archive_time DATETIME NOT NULL COMMENT '归档时间',
    archive_reason VARCHAR(100) COMMENT '归档原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_file_id (file_id),
    INDEX idx_archive_time (archive_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志归档表';
```

## 3. 字段说明

### 3.1 日志文件表 (log_file)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| file_path | VARCHAR(500) | 文件完整路径 |
| file_name | VARCHAR(100) | 文件名 |
| file_size | BIGINT | 文件大小(字节) |
| start_time | DATETIME | 文件开始时间 |
| end_time | DATETIME | 文件结束时间 |
| level | VARCHAR(10) | 日志级别 |
| status | VARCHAR(20) | 状态(ACTIVE/ARCHIVED/DELETED) |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 3.2 日志索引表 (log_index)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| file_id | BIGINT | 关联的文件ID |
| line_number | INT | 日志行号 |
| log_time | DATETIME | 日志时间 |
| level | VARCHAR(10) | 日志级别 |
| keyword | VARCHAR(100) | 关键词 |
| create_time | DATETIME | 创建时间 |

### 3.3 日志归档表 (log_archive)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| file_id | BIGINT | 原文件ID |
| archive_path | VARCHAR(500) | 归档路径 |
| archive_time | DATETIME | 归档时间 |
| archive_reason | VARCHAR(100) | 归档原因 |
| create_time | DATETIME | 创建时间 |

## 4. 索引设计

### 4.1 日志文件表索引
- 主键索引：id
- 唯一索引：file_path
- 普通索引：level, status, create_time

### 4.2 日志索引表索引
- 主键索引：id
- 普通索引：file_id, log_time, level, keyword

### 4.3 日志归档表索引
- 主键索引：id
- 普通索引：file_id, archive_time

## 5. 工作流程

### 5.1 日志写入流程
1. 日志写入文件系统
2. 更新日志文件表信息
3. 创建日志索引记录

```
   业务系统 -> 内存队列 -> 消费者线程池 -> 文件写入器 -> 日志文件
   ```
### 5.2 日志查询流程
1. 通过索引表定位日志文件
2. 根据文件路径和行号读取具体日志内容
3. 返回查询结果

```
   日志文件 -> 文件扫描器 -> 数据库记录 -> 查询接口
   ```
### 5.3 日志归档流程
1. 扫描需要归档的日志文件
2. 将文件移动到归档目录
3. 更新日志文件表状态
4. 创建归档记录
```   日志文件 -> 归档任务 -> 归档存储 -> 数据库记录
```

## 6. 注意事项

1. **性能考虑**
   - 索引表只存储关键信息，避免过大
   - 定期清理过期的索引记录
   - 使用分区表处理大量数据

2. **存储考虑**
   - 日志文件按日期和级别分目录存储
   - 定期归档旧日志文件
   - 设置合理的文件大小限制

3. **查询考虑**
   - 支持按时间范围查询
   - 支持按级别查询
   - 支持关键词搜索

4. **维护考虑**
   - 定期备份数据库
   - 监控表空间使用情况
   - 优化查询性能 

## 7. 进阶优化方向

### 7.1 日志文件管理
- **文件命名规范**：`{应用名}_{日期}_{级别}_{序号}.log`
- **目录结构**：按应用/级别/年/月/日分层存储
- **文件大小控制**：单个文件限制（如100MB），按日期自动切分
- **日志格式**：JSON格式，支持结构化日志

### 7.2 日志生命周期
- **清理策略**：
  - 按时间清理（如保留30天）
  - 按大小清理（如总大小超过10GB）
  - 按级别清理（如INFO保留7天，ERROR保留90天）
- **压缩策略**：归档日志压缩，支持多种压缩格式
- **备份策略**：定期备份，支持快速恢复

### 7.3 安全与监控
- **安全措施**：
  - 敏感信息脱敏
  - 访问权限控制
  - 日志加密存储
- **监控告警**：
  - 写入异常告警
  - 存储空间告警
  - 日志量异常告警

### 7.4 分布式支持
- **多节点日志收集**
- **日志聚合**
- **负载均衡**
- **高可用设计**

### 7.5 性能优化
- **批量写入**
- **异步索引**
- **缓存机制**
- **定期合并小文件**
- **查询优化**：
  - 全文检索
  - 模糊查询
  - 多条件组合
  - 结果缓存

### 7.6 运维支持
- **日志备份恢复**
- **日志迁移**
- **日志分析工具**
- **运维管理界面**

### 7.7 扩展性设计
- **自定义日志格式**
- **自定义存储策略**
- **自定义清理策略**
- **插件化扩展**

### 7.8 兼容性支持
- **多种日志框架**（Log4j、Logback等）
- **多种操作系统**
- **多种数据库**

### 7.9 测试策略
- **性能测试**
- **可靠性测试**
- **并发测试**
- **异常场景测试**

## 8. 实施建议

1. **第一阶段**：实现基础功能
   - 完成核心表结构
   - 实现基本日志写入
   - 实现简单查询功能

2. **第二阶段**：完善基础功能
   - 优化性能
   - 添加基本监控
   - 实现日志归档

3. **第三阶段**：添加高级特性
   - 实现分布式支持
   - 添加安全特性
   - 完善运维工具

4. **第四阶段**：优化和扩展
   - 性能优化
   - 功能扩展
   - 运维支持 