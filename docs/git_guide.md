# AsyncFlowLog Git 使用指南

## 1. 仓库初始化

```bash
# 初始化本地仓库
git init

# 添加远程仓库
git remote add origin https://github.com/你的用户名/AsyncFlowLog.git

# 创建 .gitignore 文件
echo "# Java
*.class
*.jar
*.war
*.ear
*.zip
*.tar.gz
*.rar

# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties

# IDE
.idea/
*.iml
*.iws
*.ipr
.settings/
.classpath
.project
.factorypath

# Logs
logs/
*.log

# System
.DS_Store
Thumbs.db" > .gitignore

# 添加所有文件
git add .

# 提交初始版本
git commit -m "feat: 初始化项目结构"

# 推送到远程仓库
git push -u origin main
```

## 2. 分支管理策略

### 2.1 分支命名规范
- `main`: 主分支，用于生产环境
- `develop`: 开发分支，用于日常开发
- `feature/*`: 功能分支，用于新功能开发
- `bugfix/*`: 修复分支，用于问题修复
- `release/*`: 发布分支，用于版本发布
- `hotfix/*`: 紧急修复分支，用于生产环境紧急修复

### 2.2 分支创建示例
```bash
# 创建功能分支
git checkout -b feature/async-log-core

# 创建修复分支
git checkout -b bugfix/log-index-issue

# 创建发布分支
git checkout -b release/v1.0.0
```

## 3. 提交规范

### 3.1 提交信息格式
```
<type>(<scope>): <subject>

<body>

<footer>
```

### 3.2 提交类型
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

### 3.3 提交示例
```bash
# 新功能提交
git commit -m "feat(core): 实现异步日志核心功能

- 添加日志事件队列
- 实现消费者线程池
- 完成文件写入器"

# Bug修复提交
git commit -m "fix(index): 修复日志索引问题

- 修复索引表查询性能问题
- 优化索引创建逻辑"
```

## 4. 开发流程

### 4.1 新功能开发
```bash
# 1. 从develop分支创建功能分支
git checkout develop
git pull
git checkout -b feature/new-feature

# 2. 开发并提交代码
git add .
git commit -m "feat: 实现新功能"

# 3. 推送到远程
git push origin feature/new-feature

# 4. 创建Pull Request
# 在GitHub上创建PR，请求合并到develop分支
```

### 4.2 Bug修复
```bash
# 1. 从develop分支创建修复分支
git checkout develop
git pull
git checkout -b bugfix/issue-123

# 2. 修复并提交代码
git add .
git commit -m "fix: 修复问题描述"

# 3. 推送到远程
git push origin bugfix/issue-123

# 4. 创建Pull Request
# 在GitHub上创建PR，请求合并到develop分支
```

## 5. 版本发布

### 5.1 创建发布分支
```bash
# 1. 从develop分支创建发布分支
git checkout develop
git pull
git checkout -b release/v1.0.0

# 2. 更新版本号
# 修改pom.xml中的版本号

# 3. 提交版本更新
git add pom.xml
git commit -m "chore: 更新版本号到1.0.0"

# 4. 合并到main分支
git checkout main
git merge release/v1.0.0

# 5. 打标签
git tag -a v1.0.0 -m "版本1.0.0"

# 6. 推送到远程
git push origin main
git push origin v1.0.0
```

## 6. 日常操作

### 6.1 更新代码
```bash
# 拉取最新代码
git pull

# 如果本地有修改，先暂存
git stash
git pull
git stash pop
```

### 6.2 查看状态
```bash
# 查看当前状态
git status

# 查看提交历史
git log

# 查看分支
git branch -a
```

### 6.3 撤销修改
```bash
# 撤销工作区修改
git checkout -- <file>

# 撤销暂存区修改
git reset HEAD <file>

# 撤销最近一次提交
git reset --soft HEAD^
```

## 7. 注意事项

1. **提交前检查**
   - 运行测试
   - 检查代码格式
   - 确保没有敏感信息

2. **分支管理**
   - 及时删除已合并的分支
   - 保持分支命名规范
   - 定期同步主分支

3. **代码审查**
   - 所有PR必须经过审查
   - 确保代码质量
   - 遵循项目规范

4. **版本控制**
   - 遵循语义化版本
   - 及时打标签
   - 保持版本历史清晰 