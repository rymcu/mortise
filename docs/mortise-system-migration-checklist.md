# mortise-system 迁移检查清单

## ✅ 迁移前检查

- [ ] 确认原始代码在 `src/` 目录中
- [ ] 备份原始代码 (可选但推荐)
- [ ] 确认 `mortise-system/pom.xml` 存在
- [ ] 确认其他依赖模块已完成 (common, core, cache, auth 等)

---

## 📋 迁移步骤

### 阶段 1: 自动迁移 (运行脚本)

- [ ] 运行 PowerShell 脚本: `.\migrate-system.ps1`
- [ ] 检查迁移文件数量是否正确
- [ ] 确认目录结构已创建

### 阶段 2: 包名和导入语句替换

使用 VS Code 的全局搜索替换 (Ctrl+Shift+H):

#### 包名替换 (已由脚本完成)
- [x] `package com.rymcu.mortise.entity;` → `package com.rymcu.mortise.system.entity;`
- [x] `package com.rymcu.mortise.mapper;` → `package com.rymcu.mortise.system.mapper;`
- [x] `package com.rymcu.mortise.service;` → `package com.rymcu.mortise.system.service;`
- [x] `package com.rymcu.mortise.controller;` → `package com.rymcu.mortise.system.controller;`

#### 导入语句替换 (需手动完成)

**实体类导入**:
- [ ] `import com.rymcu.mortise.entity.` → `import com.rymcu.mortise.system.entity.`
- [ ] `import com.rymcu.mortise.mapper.` → `import com.rymcu.mortise.system.mapper.`
- [ ] `import com.rymcu.mortise.model.` → `import com.rymcu.mortise.system.model.`
- [ ] `import com.rymcu.mortise.service.` → `import com.rymcu.mortise.system.service.`

**公共类导入**:
- [ ] `import com.rymcu.mortise.util.` → `import com.rymcu.mortise.common.util.`
- [ ] `import com.rymcu.mortise.constant.ProjectConstant` → `import com.rymcu.mortise.common.constant.ProjectConstant`
- [ ] `import com.rymcu.mortise.enumerate.` → `import com.rymcu.mortise.common.enumerate.`
- [ ] `import com.rymcu.mortise.exception.` → `import com.rymcu.mortise.common.exception.`

**核心类导入**:
- [ ] `import com.rymcu.mortise.result.` → `import com.rymcu.mortise.core.result.`

**特殊导入** (如果使用了直接基础设施):
- [ ] `CacheService` → 改用 `SystemCacheService`
- [ ] `NotificationService` → 改用 `SystemNotificationService`

### 阶段 3: 代码调整

#### 3.1 Service 实现调整

检查所有 `*ServiceImpl.java` 文件:

- [ ] 确认使用 `SystemCacheService` 而不是 `CacheService`
  ```java
  // ❌ 不推荐
  @Autowired
  private CacheService cacheService;
  
  // ✅ 推荐
  @Autowired
  private SystemCacheService systemCacheService;
  ```

- [ ] 确认使用 `SystemNotificationService` 而不是 `NotificationService`
  ```java
  // ❌ 不推荐
  @Autowired
  private NotificationService notificationService;
  
  // ✅ 推荐
  @Autowired
  private SystemNotificationService systemNotificationService;
  ```

#### 3.2 Controller 调整

检查所有 `*Controller.java` 文件:

- [ ] 添加 `@Tag` 注解 (Swagger 文档)
  ```java
  @Tag(name = "用户管理", description = "用户管理接口")
  public class UserController { }
  ```

- [ ] 为关键操作添加 `@OperationLog` 注解
  ```java
  @PostMapping
  @OperationLog(module = "用户管理", operation = "创建用户")
  public GlobalResult<User> create(@RequestBody User user) { }
  ```

- [ ] 为高频接口添加 `@RateLimit` 注解
  ```java
  @GetMapping("/list")
  @RateLimit(key = "user:list", limit = 100, period = 60)
  public GlobalResult<List<User>> list() { }
  ```

- [ ] 使用 `@Operation` 注解描述接口
  ```java
  @Operation(summary = "获取用户列表", description = "分页查询用户列表")
  @GetMapping("/list")
  public GlobalResult<Page<User>> list() { }
  ```

#### 3.3 Mapper XML 调整

检查 `src/main/resources/mapper/*.xml` 文件:

- [ ] 确认 `namespace` 路径正确
  ```xml
  <!-- 原路径 -->
  <mapper namespace="com.rymcu.mortise.mapper.UserMapper">
  
  <!-- 新路径 -->
  <mapper namespace="com.rymcu.mortise.system.mapper.UserMapper">
  ```

- [ ] 确认 `resultType` 路径正确
  ```xml
  <!-- 原路径 -->
  <select id="findById" resultType="com.rymcu.mortise.entity.User">
  
  <!-- 新路径 -->
  <select id="findById" resultType="com.rymcu.mortise.system.entity.User">
  ```

### 阶段 4: 编译验证

- [ ] 清理编译缓存: `mvn clean`
- [ ] 编译 mortise-system 模块: `mvn compile -pl mortise-system -am`
- [ ] 修复编译错误 (如果有)
- [ ] 编译整个项目: `mvn clean compile`

### 阶段 5: 测试验证

- [ ] 运行单元测试 (如果有): `mvn test -pl mortise-system`
- [ ] 启动应用: `mvn spring-boot:run -pl mortise-app`
- [ ] 测试 REST API 接口
- [ ] 验证数据库操作
- [ ] 验证缓存功能
- [ ] 验证日志记录
- [ ] 验证通知发送

---

## 🔍 常见问题

### 问题 1: 找不到类

**症状**: 编译错误 "cannot find symbol"

**解决方案**:
1. 检查导入语句是否正确
2. 检查包名是否正确
3. 确认依赖的模块已编译

### 问题 2: 循环依赖

**症状**: "Circular dependency detected"

**解决方案**:
1. 检查 `pom.xml` 依赖关系
2. 移除不必要的依赖
3. 参考依赖关系图 (REFACTORING_PLAN.md)

### 问题 3: Mapper 找不到

**症状**: "Invalid bound statement (not found)"

**解决方案**:
1. 检查 Mapper XML 的 `namespace` 是否正确
2. 检查方法名是否匹配
3. 确认 XML 文件在 `src/main/resources/mapper/` 下

### 问题 4: 缓存不生效

**症状**: 数据没有缓存

**解决方案**:
1. 确认使用 `SystemCacheService` 而不是 `CacheService`
2. 检查 `SystemCacheConfigurer` 是否正确配置
3. 查看日志确认缓存配置是否加载

---

## 📊 进度统计

### 迁移文件统计

| 类型 | 预计数量 | 已迁移 | 待迁移 |
|------|----------|--------|--------|
| Entity | ~ | 0 | ~ |
| Mapper | ~ | 0 | ~ |
| Model | ~ | 0 | ~ |
| Service | ~ | 0 | ~ |
| Service Impl | ~ | 0 | ~ |
| Controller | ~ | 0 | ~ |
| Handler | ~ | 0 | ~ |
| Event | ~ | 0 | ~ |
| Serializer | ~ | 0 | ~ |
| Mapper XML | ~ | 0 | ~ |
| **总计** | **~** | **0** | **~** |

### 调整任务统计

| 任务 | 状态 |
|------|------|
| 包名替换 | ⏳ 待开始 |
| 导入语句替换 | ⏳ 待开始 |
| Service 层调整 | ⏳ 待开始 |
| Controller 层调整 | ⏳ 待开始 |
| Mapper XML 调整 | ⏳ 待开始 |
| 编译验证 | ⏳ 待开始 |
| 功能测试 | ⏳ 待开始 |

---

## 💡 最佳实践

1. **分层迁移**: 建议按 Entity → Mapper → Model → Service → Controller 的顺序迁移
2. **逐步验证**: 每迁移一层就编译验证一次
3. **保留备份**: 迁移完成并验证无误后再删除原文件
4. **使用版本控制**: 每完成一个阶段就提交一次 Git
5. **文档优先**: 遇到问题先查看迁移指南文档

---

**迁移开始时间**: _______________  
**迁移完成时间**: _______________  
**迁移耗时**: _______________  
**遇到的主要问题**: _______________

---

**准备好开始了吗？运行 `.\migrate-system.ps1` 开始自动迁移！** 🚀
