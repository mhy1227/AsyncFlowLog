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
    operation_time DATETIME COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录表';

-- 添加表说明
ALTER TABLE operation_log COMMENT '系统操作日志记录表，存储用户操作轨迹';

-- 添加初始测试数据（可选）
-- INSERT INTO operation_log (user_id, username, module, operation_type, description, method, request_url, ip, request_params, status, operation_time)
-- VALUES ('1', 'admin', '用户管理', 'LOGIN', '用户登录', 'com.asyncflow.log.controller.UserController.login', '/api/user/login', '127.0.0.1', '{"username": "admin", "password": "******"}', 0, NOW()); 