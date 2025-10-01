# Mortise-System 模块重构报告

## 📋 **问题清单**

### **1. CacheService vs SystemCacheService 职能重复** ⚠️

**当前状况**：
- `CacheServiceImpl` (mortise-system) - 直接使用 Spring Cache
- `SystemCacheService` (mortise-system) - 业务层封装，但内部注入的是 `com.rymcu.mortise.cache.service.CacheService`
- 8个业务类都注入了 `CacheService`

**问题分析**：
```
错误的架构：
mortise-system (业务层)
    ├── CacheServiceImpl (直接使用 Spring Cache) ❌ 不应该存在
    ├── SystemCacheService (业务封装) ✅ 正确
    └── 8个业务类注入 CacheService ❌ 应该注入 SystemCacheService

正确的架构：
mortise-system (业务层)
    ├── SystemCacheService (业务封装)
    └── 业务类注入 SystemCacheService ✅
    
mortise-cache (基础设施层)
    └── CacheService (Redis/Caffeine操作) ✅
```

**解决方案**：
1. **删除** `mortise-system/service/CacheService.java` 和 `CacheServiceImpl.java`
2. **替换** 所有业务类中的 `CacheService` 为 `SystemCacheService`
3. **保留** `SystemCacheService` 和 `SystemCacheServiceImpl`

---

### **2. DictSerializer 已在 mortise-system 中** ✅

**当前状况**：
- ✅ 已存在：`mortise-system/serializer/DictSerializer.java`
- ✅ 包名正确：`com.rymcu.mortise.system.serializer`
- ✅ 依赖正确：使用 `DictService` (system模块)

**结论**：无需处理，已正确迁移

---

### **3. Model 类已在 mortise-system 中** ✅

**当前状况**：
- ✅ `BaseSearch.java` - 已在 `mortise-system/model/`
- ✅ `Link.java` - 已在 `mortise-system/model/`
- ✅ `LoginInfo.java` - 已在 `mortise-system/model/`

**结论**：无需处理，已正确迁移

---

### **4. Resilience4jRateLimit 应移至 mortise-web** ⚠️

**当前状况**：
- ❌ 当前位置：`mortise-system/annotation/Resilience4jRateLimit.java`
- ✅ 应该位置：`mortise-web/annotation/Resilience4jRateLimit.java`

**理由**：
1. Resilience4j限流是Web层的横切关注点，不是业务层功能
2. 限流注解应该在Web基础设施层，供所有业务模块使用
3. mortise-system不应该提供Web基础设施功能

**解决方案**：
1. 将 `Resilience4jRateLimit.java` 移动到 `mortise-web/annotation/`
2. 更新所有引用该注解的 import

---

## 🔧 **重构执行计划**

### **阶段 1：删除重复的 CacheService**

```powershell
# 1. 删除 CacheService 接口和实现
Remove-Item "mortise-system\src\main\java\com\rymcu\mortise\system\service\CacheService.java"
Remove-Item "mortise-system\src\main\java\com\rymcu\mortise\system\service\impl\CacheServiceImpl.java"
```

### **阶段 2：替换所有 CacheService 引用**

需要修改的文件（8个）：
1. `AuthServiceImpl.java`
2. `DictServiceImpl.java`
3. `DictTypeServiceImpl.java`
4. `JavaMailServiceImpl.java`
5. `UserCacheServiceImpl.java`
6. `UserServiceImpl.java`
7. `SystemCacheServiceImpl.java`

替换模式：
```java
// 旧代码
import com.rymcu.mortise.system.service.CacheService;
@Resource
private CacheService cacheService;

// 新代码
import com.rymcu.mortise.system.service.SystemCacheService;
@Resource
private SystemCacheService systemCacheService;
```

**特别注意**：
- `SystemCacheServiceImpl` 中的 `CacheService` 是 `com.rymcu.mortise.cache.service.CacheService`，不需要改
- 其他7个文件的 `CacheService` 都是 `com.rymcu.mortise.system.service.CacheService`，需要改

### **阶段 3：移动 Resilience4jRateLimit 到 mortise-web**

```powershell
# 1. 创建目录
New-Item -ItemType Directory -Path "mortise-web\src\main\java\com\rymcu\mortise\web\annotation" -Force

# 2. 移动文件
Move-Item "mortise-system\src\main\java\com\rymcu\mortise\system\annotation\Resilience4jRateLimit.java" `
          "mortise-web\src\main\java\com\rymcu\mortise\web\annotation\Resilience4jRateLimit.java"

# 3. 更新包名
# package com.rymcu.mortise.web.annotation;
```

### **阶段 4：修复 SystemCacheConstant**

当前 `CacheServiceImpl` 使用了 `SystemCacheConstant`，但该常量类在 `mortise-system/constant/`。

**问题**：
- `SystemCacheConstant` 定义了所有缓存名称
- 但这些缓存名称应该在基础设施层定义，而不是业务层

**解决方案**：
1. 检查 `SystemCacheConstant` 的内容
2. 将缓存名称常量移动到 `mortise-cache/constant/CacheConstant`
3. 或者在 `SystemCacheConstant` 中引用 `CacheConstant`

---

## 📊 **影响范围**

### **需要修改的文件**

| 类型 | 文件数 | 操作 |
|------|-------|------|
| 删除 | 2 | CacheService.java, CacheServiceImpl.java |
| 修改 import | 7 | 7个业务实现类 |
| 移动 | 1 | Resilience4jRateLimit.java |
| **总计** | **10** | |

### **不需要修改的文件**

- ✅ DictSerializer.java - 已正确位置
- ✅ BaseSearch.java - 已正确位置
- ✅ Link.java - 已正确位置
- ✅ LoginInfo.java - 已正确位置
- ✅ SystemCacheServiceImpl.java - 注入的是 mortise-cache 的 CacheService

---

## ✅ **执行检查清单**

### **阶段 1：删除重复服务**
- [ ] 删除 `CacheService.java` 接口
- [ ] 删除 `CacheServiceImpl.java` 实现

### **阶段 2：替换引用（7个文件）**
- [ ] `AuthServiceImpl.java` - CacheService → SystemCacheService
- [ ] `DictServiceImpl.java` - CacheService → SystemCacheService
- [ ] `DictTypeServiceImpl.java` - CacheService → SystemCacheService
- [ ] `JavaMailServiceImpl.java` - CacheService → SystemCacheService
- [ ] `UserCacheServiceImpl.java` - CacheService → SystemCacheService
- [ ] `UserServiceImpl.java` - CacheService → SystemCacheService
- [ ] 检查所有方法调用是否兼容

### **阶段 3：移动限流注解**
- [ ] 创建 `mortise-web/annotation/` 目录
- [ ] 移动 `Resilience4jRateLimit.java`
- [ ] 更新包名为 `com.rymcu.mortise.web.annotation`
- [ ] 更新所有引用该注解的 import

### **阶段 4：验证编译**
- [ ] `mvn clean compile -pl mortise-system -am`
- [ ] `mvn clean compile -pl mortise-web -am`
- [ ] `mvn clean compile`

---

## 🚀 **立即执行**

开始重构：
```powershell
# 执行删除
.\refactor-step1-delete-cache-service.ps1

# 执行替换
.\refactor-step2-replace-cache-service.ps1

# 执行移动
.\refactor-step3-move-ratelimit.ps1

# 验证
mvn clean compile
```

---

**重构清晰，职责分明！** 🔧
