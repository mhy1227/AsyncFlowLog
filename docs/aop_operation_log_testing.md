# AOP操作日志模块测试指南

本文档提供了测试异步日志系统中的AOP操作日志模块的方法和步骤。

## 测试准备

1. **确保数据库表已创建**：
   - 检查数据库中是否存在`operation_log`表
   - 如果不存在，执行`V1_2__Create_Operation_Log_Table.sql`脚本创建

2. **确保所有依赖服务可用**：
   - Spring AOP功能已启用
   - 异步线程池配置正确
   - JsonUtils工具类可用

## 自动化测试

项目包含了一套完整的单元测试，用于验证AOP操作日志功能：

```bash
# 运行所有测试
mvn test

# 只运行AOP操作日志相关测试
mvn test -Dtest=OperationLogAspectTest
```

## 手动测试步骤

### 1. 使用Postman或curl测试API接口

#### 测试GET请求操作日志：

```bash
curl -X GET "http://localhost:8080/api/test/log?param=testValue" -H "accept: application/json"
```

预期结果：
- 接口返回成功响应
- 数据库中新增一条操作日志记录，描述为"测试GET操作"

#### 测试POST请求操作日志：

```bash
curl -X POST "http://localhost:8080/api/test/log" \
     -H "Content-Type: application/json" \
     -d '{"name":"测试名称","value":123}'
```

预期结果：
- 接口返回成功响应
- 数据库中新增一条操作日志记录，描述为"测试POST操作"
- 日志中包含请求参数和返回结果

#### 测试异常情况操作日志：

```bash
curl -X GET "http://localhost:8080/api/test/error" -H "accept: application/json"
```

预期结果：
- 接口返回500错误
- 数据库中新增一条操作日志记录，描述为"测试异常操作"
- 日志状态为失败(1)，并包含异常信息

### 2. 查询数据库验证结果

```sql
-- 查看所有操作日志
SELECT * FROM operation_log ORDER BY operation_time DESC;

-- 查看特定类型的操作日志
SELECT * FROM operation_log WHERE operation_type = 'QUERY';

-- 查看异常操作日志
SELECT * FROM operation_log WHERE status = 1;
```

### 3. 性能测试

使用JMeter或其他工具进行并发请求测试：

1. 创建一个包含100个并发用户的测试计划
2. 每个用户连续发送10个请求
3. 监控系统性能和数据库写入情况
4. 验证所有操作都被正确记录

## 常见问题排查

1. **日志未记录**
   - 检查AOP配置是否正确
   - 确认方法上是否有@OperationLog注解
   - 验证切面是否正常工作

2. **请求参数/返回结果未记录**
   - 检查@OperationLog注解中recordParams和recordResult设置
   - 确认JsonUtils能正确处理复杂对象

3. **异步保存失败**
   - 检查线程池配置
   - 确认数据库连接正常

4. **性能问题**
   - 检查索引使用情况
   - 确认日志表是否过大需要归档
   - 评估是否需要批量写入优化

## 测试报告模板

```
# AOP操作日志测试报告

## 测试环境
- 测试日期: YYYY-MM-DD
- 测试人员: XXX
- 系统版本: X.X.X
- 数据库版本: MySQL X.X

## 测试结果汇总
- 单元测试: 通过/失败
- 功能测试: 通过/失败
- 性能测试: 通过/失败

## 详细测试结果
1. GET请求测试: 通过/失败
2. POST请求测试: 通过/失败
3. 异常处理测试: 通过/失败
4. 并发测试: 通过/失败

## 发现的问题
1. ...
2. ...

## 建议改进
1. ...
2. ...
```

## 结论

通过上述测试步骤，可以全面验证AOP操作日志模块的功能正确性、可靠性和性能。测试覆盖了正常请求、异常情况和高并发场景，确保系统在各种条件下都能正确记录操作日志。 