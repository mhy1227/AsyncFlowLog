# 异步日志系统中的AOP操作日志模块说明文档

## 概述

异步日志系统中的AOP操作日志模块是一个基于面向切面编程（Aspect-Oriented Programming）的功能组件，用于自动收集和记录系统中的用户操作信息。通过简单的注解方式，可以轻松地为任何方法添加操作日志记录能力，无需修改原有业务代码。

## 核心组件

该AOP操作日志模块包含以下核心组件：

1. **OperationLog注解**：用于标记需要记录操作日志的方法
2. **OperationLogAspect切面**：拦截带有OperationLog注解的方法调用，收集操作信息
3. **OperationLogRecord实体**：存储操作日志的详细信息
4. **OperationLogMapper接口**：提供操作日志的数据库访问功能
5. **OperationLogService接口**：提供操作日志的服务层功能

## 功能特点

1. **注解驱动**：通过简单的注解即可启用日志记录
2. **自动收集上下文**：自动获取请求IP、URL等信息
3. **灵活配置**：可配置是否记录请求参数、返回结果、异常信息
4. **性能优化**：采用异步方式记录日志，不影响主业务流程
5. **与现有日志系统集成**：利用AsyncLogService记录操作日志的处理状态

## 使用方法

### 1. 为方法添加注解

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @OperationLog(
        description = "用户登录", 
        operationType = "LOGIN", 
        recordParams = true,
        recordResult = false
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDTO>> login(@RequestBody LoginDTO loginDTO) {
        // 业务逻辑
        return ResponseEntity.ok(ApiResponse.success(userService.login(loginDTO)));
    }
}
```

### 2. 注解参数说明

- **description**: 操作描述，说明该操作的用途
- **operationType**: 操作类型，如查询、新增、修改、删除等
- **recordParams**: 是否记录请求参数，默认为true
- **recordResult**: 是否记录返回结果，默认为false（避免记录过多数据）
- **recordException**: 是否记录异常信息，默认为true

## 实现原理

### 1. 切面定义与执行流程

```java
@Aspect
@Component
public class OperationLogAspect {
    
    @Pointcut("@annotation(com.asyncflow.log.annotation.OperationLog)")
    public void operationLogPointcut() {
    }
    
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 准备日志记录
        // 2. 执行原方法
        // 3. 记录执行结果
        // 4. 异步保存日志
    }
}
```

切面执行流程：
1. 拦截带有@OperationLog注解的方法调用
2. 收集方法调用前的信息（请求参数、IP地址等）
3. 执行原始方法
4. 收集方法执行后的信息（执行结果、耗时等）
5. 如发生异常，记录异常信息
6. 异步保存操作日志记录

### 2. 数据存储

操作日志被封装为OperationLogRecord对象，包含以下主要字段：
- 用户ID和用户名
- 操作模块和操作类型
- 操作描述
- 请求方法、URL和IP
- 请求参数和返回结果
- 执行时长
- 操作状态和异常信息
- 操作时间

### 3. 异步处理

为避免影响业务性能，操作日志的保存采用异步方式：

```java
@Async
public void asyncSave(OperationLogRecord logRecord) {
    boolean result = save(logRecord);
    
    // 使用现有的异步日志系统记录日志
    Map<String, String> context = new HashMap<>();
    // 设置上下文信息
    
    if (result) {
        asyncLogService.log("INFO", "操作日志已保存", context);
    } else {
        asyncLogService.log("ERROR", "操作日志保存失败", context);
    }
}
```

## 与AsyncLogService的集成

OperationLogService实现类使用AsyncLogService来记录操作日志的处理状态，形成两级日志记录：
1. **操作日志**：记录用户的具体操作（如登录、修改资料等）
2. **系统日志**：记录系统行为（如"操作日志保存成功/失败"）

这样的设计使得系统日志和操作日志分离，便于管理和查询。

## 安全性考虑

1. **敏感信息处理**：可以在记录前对敏感信息进行脱敏处理
2. **数据量控制**：默认不记录返回结果，避免日志数据过大
3. **异常处理**：对JSON转换等操作进行异常捕获，确保主流程不受影响

## 扩展建议

1. **添加日志审计功能**：基于已记录的操作日志实现审计功能
2. **添加实时监控**：对敏感操作进行实时监控和告警
3. **数据分析**：对操作日志进行统计分析，生成用户行为报告
4. **日志轮转**：实现操作日志的定期归档和清理机制

## 数据库设计

操作日志记录表（operation_log）设计如下：

| 字段名         | 类型          | 说明                   |
|--------------|--------------|------------------------|
| id           | bigint       | 主键，自增               |
| user_id      | varchar(64)  | 用户ID                 |
| username     | varchar(50)  | 用户名                  |
| module       | varchar(50)  | 操作模块                |
| operation_type| varchar(50)  | 操作类型                |
| description  | varchar(200) | 操作描述                |
| method       | varchar(100) | 请求方法                |
| request_url  | varchar(255) | 请求URL                |
| ip           | varchar(50)  | 请求IP                 |
| request_params| text         | 请求参数（JSON格式）     |
| result       | text         | 返回结果（JSON格式）     |
| duration     | bigint       | 执行时长（毫秒）         |
| status       | tinyint      | 操作状态（0成功 1失败）   |
| error_message| text         | 异常信息                |
| operation_time| datetime     | 操作时间                |

## 总结

AOP操作日志模块通过面向切面编程的方式，提供了一种非侵入式的操作日志记录方案。结合异步日志系统，既能满足业务审计需求，又不会影响系统性能，是一种理想的系统日志解决方案。

## 代码结构

```
src/main/java/com/asyncflow/log/
├── annotation/
│   └── OperationLog.java           # 操作日志注解
├── aspect/
│   └── OperationLogAspect.java     # 操作日志切面
├── model/entity/
│   └── OperationLogRecord.java     # 操作日志记录实体
├── mapper/
│   └── OperationLogMapper.java     # 操作日志Mapper接口
├── service/
│   ├── OperationLogService.java    # 操作日志服务接口
│   └── impl/
│       └── OperationLogServiceImpl.java # 操作日志服务实现
``` 