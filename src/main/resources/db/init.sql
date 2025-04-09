-- 创建数据库
CREATE DATABASE IF NOT EXISTS async_log DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 使用数据库
USE async_log;

--  2.1 日志文件表 (log_file)
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


--  2.2 日志索引表 (log_index)
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


-- 2.3 日志归档表 (log_archive)
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

-- 添加外键约束到log_index表
ALTER TABLE log_index 
ADD CONSTRAINT fk_log_index_file_id 
FOREIGN KEY (file_id) REFERENCES log_file (id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 添加外键约束到log_archive表
ALTER TABLE log_archive 
ADD CONSTRAINT fk_log_archive_file_id 
FOREIGN KEY (file_id) REFERENCES log_file (id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 为log_index表添加组合索引，用于常见查询场景
CREATE INDEX idx_log_index_time_level ON log_index (log_time, level);