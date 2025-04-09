-- 创建操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id VARCHAR(64) COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    module VARCHAR(50) COMMENT '操作模块',
    operation_type VARCHAR(50) COMMENT '操作类型',
    description VARCHAR(200) COMMENT '操作描述',
    method VARCHAR(100) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    ip VARCHAR(50) COMMENT '请求IP',
    request_params TEXT COMMENT '请求参数（JSON格式）',
    result TEXT COMMENT '返回结果（JSON格式）',
    duration BIGINT COMMENT '执行时长（毫秒）',
    status TINYINT COMMENT '操作状态（0成功 1失败）',
    error_message TEXT COMMENT '异常信息',
    operation_time TIMESTAMP COMMENT '操作时间'
);

-- 添加索引
CREATE INDEX idx_user_id ON operation_log(user_id);
CREATE INDEX idx_operation_type ON operation_log(operation_type);
CREATE INDEX idx_operation_time ON operation_log(operation_time); 