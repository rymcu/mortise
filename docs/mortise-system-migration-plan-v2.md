# 🚀 mortise-system 模块迁移计划 V2（基于实际代码）

## 📋 迁移概述

基于 GitHub 仓库 `rymcu/mortise` 的实际代码结构，本文档提供详细的迁移指南。

**原始代码位置**: `src/main/java/com/rymcu/mortise/`  
**目标位置**: `mortise-system/src/main/java/com/rymcu/mortise/system/`

---

## 📦 实际代码结构分析

### 1. Entity 层（实体类）

**位置**: `src/main/java/com/rymcu/mortise/entity/`

已确认的实体类：
- ✅ `User.java` - 用户实体
- ✅ `Role.java` - 角色实体
- ✅ `Menu.java` - 菜单实体
- ✅ `Dict.java` - 字典数据实体
- ✅ `DictType.java` - 字典类型实体
- ✅ `UserRole.java` - 用户角色关联实体
- ✅ `RoleMenu.java` - 角色菜单关联实体

**迁移目标**: `mortise-system/src/main/java/com/rymcu/mortise/system/entity/`

---

### 2. Mapper 层（数据访问）

**位置**: `src/main/java/com/rymcu/mortise/mapper/`

已确认的 Mapper 接口：
- ✅ `UserMapper.java` - 用户 Mapper (继承 BaseMapper<User>)
- ✅ `RoleMapper.java` - 角色 Mapper (继承 BaseMapper<Role>)
- ✅ `MenuMapper.java` - 菜单 Mapper (继承 BaseMapper<Menu>)
- ✅ `DictMapper.java` - 字典 Mapper (继承 BaseMapper<Dict>)
- ✅ `DictTypeMapper.java` - 字典类型 Mapper (继承 BaseMapper<DictType>)

**特点**: 
- 所有 Mapper 都使用 MyBatis-Flex 的 `BaseMapper`
- 使用 `@Mapper` 注解
- 无需 XML 文件（使用注解方式）

**迁移目标**: `mortise-system/src/main/java/com/rymcu/mortise/system/mapper/`

---

### 3. Model 层（DTO/VO）

**位置**: `src/main/java/com/rymcu/mortise/model/`

已确认的 Model 类：
- ✅ `UserSearch.java` - 用户搜索 DTO
- ✅ `RoleSearch.java` - 角色搜索 DTO
- ✅ `MenuSearch.java` - 菜单搜索 DTO
- ✅ `DictSearch.java` - 字典搜索 DTO
- ✅ `DictTypeSearch.java` - 字典类型搜索 DTO
- ✅ `BindRoleMenuInfo.java` - 角色菜单绑定 DTO
- ✅ `Link.java` - 菜单树节点 VO
- ✅ `BaseOption.java` - 基础选项 VO（应该在 mortise-core）
- ✅ `DictInfo.java` - 字典信息 VO
- ✅ `BatchUpdateInfo.java` - 批量更新 DTO

**迁移目标**: `mortise-system/src/main/java/com/rymcu/mortise/system/model/`

**注意**: `BaseOption` 应迁移到 `mortise-core` 模块

---

### 4. Service 层（业务服务）

**位置**: 
- 接口: `src/main/java/com/rymcu/mortise/service/`
- 实现: `src/main/java/com/rymcu/mortise/service/impl/`

#### 4.1 Service 接口

已确认的 Service 接口：
- ✅ `UserService.java` - 用户服务
- ✅ `RoleService.java` - 角色服务
- ✅ `MenuService.java` - 菜单服务（继承 IService<Menu>）
- ✅ `DictService.java` - 字典服务（继承 IService<Dict>）
- ✅ `DictTypeService.java` - 字典类型服务（继承 IService<DictType>）

#### 4.2 Service 实现

已确认的 Service 实现：
- ✅ `UserServiceImpl.java` - 用户服务实现
- ✅ `RoleServiceImpl.java` - 角色服务实现（继承 ServiceImpl<RoleMapper, Role>）
- ✅ `MenuServiceImpl.java` - 菜单服务实现（继承 ServiceImpl<MenuMapper, Menu>）
- ✅ `DictServiceImpl.java` - 字典服务实现（继承 ServiceImpl<DictMapper, Dict>）
- ✅ `DictTypeServiceImpl.java` - 字典类型服务实现（继承 ServiceImpl<DictTypeMapper, DictType>）

**特点**:
- 使用 MyBatis-Flex 的 `ServiceImpl` 基类
- 使用 `@Service` 注解
- 使用 `@Transactional` 进行事务管理
- **直接注入了 `CacheService`**（需要改为 `SystemCacheService`）

**迁移目标**: 
- 接口: `mortise-system/src/main/java/com/rymcu/mortise/system/service/`
- 实现: `mortise-system/src/main/java/com/rymcu/mortise/system/service/impl/`

---

### 5. Controller 层（REST API）

**位置**: `src/main/java/com/rymcu/mortise/web/admin/`

已确认的 Controller：
- ✅ `UserController.java` - 用户管理接口
- ✅ `RoleController.java` - 角色管理接口
- ✅ `MenuController.java` - 菜单管理接口
- ✅ `DictController.java` - 字典管理接口
- ✅ `DictTypeController.java` - 字典类型管理接口

**特点**:
- 使用 `@RestController` + `@RequestMapping`
- 已经使用了 `@Tag` (Swagger 文档)
- 已经使用了 `@Operation` + `@ApiResponses`
- 使用 `@PreAuthorize("hasRole('admin')")` 权限控制
- 使用 `UserUtils.getCurrentUserByToken()` 获取当前用户
- 返回 `GlobalResult<T>` 统一响应格式

**迁移目标**: `mortise-system/src/main/java/com/rymcu/mortise/system/controller/`

---

### 6. Handler 层（事件处理）

**位置**: `src/main/java/com/rymcu/mortise/handler/` 或 `src/main/java/com/rymcu/mortise/event/`

需要查找的 Handler：
- `RegisterHandler.java`
- `AccountHandler.java`
- `ResetPasswordHandler.java`
- `UserLoginEventHandler.java`
- `OidcUserEventHandler.java`

**迁移目标**: `mortise-system/src/main/java/com/rymcu/mortise/system/handler/`

---

### 7. Serializer 层（序列化器）

**位置**: `src/main/java/com/rymcu/mortise/serializer/`

需要迁移：
- `DictSerializer.java` - 字典序列化器

**迁移目标**: `mortise-system/src/main/java/com/rymcu/mortise/system/serializer/`

---

## 🔧 关键修改点

### 1. Service 实现中的缓存服务

**原代码** (DictServiceImpl.java, DictTypeServiceImpl.java):
```java
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Resource
    private CacheService cacheService;  // ❌ 直接使用基础设施服务
    
    // ...
}
```

**需要修改为**:
```java
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Autowired
    private SystemCacheService systemCacheService;  // ✅ 使用业务封装层
    
    // 或者保留 CacheService，但建议升级到 SystemCacheService
}
```

### 2. Controller 中的工具类

**原代码**:
```java
import com.rymcu.mortise.util.UserUtils;

User user = UserUtils.getCurrentUserByToken();
```

**需要修改导入**:
```java
import com.rymcu.mortise.app.util.UserUtils;  // 或保留在合适位置
```

### 3. 实体类中的注解

**原代码** (Dict.java, Menu.java):
```java
import com.rymcu.mortise.annotation.DictFormat;

@DictFormat(value = "Status")
private Integer status;
```

**需要确认** `DictFormat` 注解的位置：
- 如果在 `src/main/java/com/rymcu/mortise/annotation/` → 需要迁移到合适模块
- 建议迁移到 `mortise-system` 或 `mortise-common`

---

## 📝 详细迁移步骤

### 步骤 1: 创建目录结构

```powershell
$baseDir = "mortise-system/src/main/java/com/rymcu/mortise/system"

# 创建 Java 源码目录
New-Item -ItemType Directory -Force -Path "$baseDir/entity"
New-Item -ItemType Directory -Force -Path "$baseDir/mapper"
New-Item -ItemType Directory -Force -Path "$baseDir/model"
New-Item -ItemType Directory -Force -Path "$baseDir/service"
New-Item -ItemType Directory -Force -Path "$baseDir/service/impl"
New-Item -ItemType Directory -Force -Path "$baseDir/controller"
New-Item -ItemType Directory -Force -Path "$baseDir/handler"
New-Item -ItemType Directory -Force -Path "$baseDir/handler/event"
New-Item -ItemType Directory -Force -Path "$baseDir/serializer"
```

### 步骤 2: 复制文件（从 GitHub 或本地 target/classes）

由于本地 `src/` 目录已被删除，您需要从以下来源之一获取代码：

**选项 A: 从 GitHub 克隆原始代码**
```bash
# 克隆到临时目录
git clone https://github.com/rymcu/mortise.git mortise-original

# 复制需要的文件
Copy-Item "mortise-original/src/main/java/com/rymcu/mortise/entity/*.java" `
          "mortise-system/src/main/java/com/rymcu/mortise/system/entity/"
# ... 其他文件
```

**选项 B: 从编译后的 class 文件反编译**
```bash
# 使用 IntelliJ IDEA 的反编译功能
# 或使用 JD-GUI 等工具
```

**选项 C: 从备份恢复**
```bash
# 如果您有 src-old-backup 目录
Copy-Item "src-old-backup/main/java/com/rymcu/mortise/entity/*.java" `
          "mortise-system/src/main/java/com/rymcu/mortise/system/entity/"
```

### 步骤 3: 批量替换包名

使用 PowerShell 脚本或 VS Code 全局替换：

```powershell
# 替换包名
(Get-Content $file.FullName -Raw) `
    -replace 'package com\.rymcu\.mortise\.entity;', 'package com.rymcu.mortise.system.entity;' `
    -replace 'package com\.rymcu\.mortise\.mapper;', 'package com.rymcu.mortise.system.mapper;' `
    -replace 'package com\.rymcu\.mortise\.service;', 'package com.rymcu.mortise.system.service;' `
    | Set-Content $file.FullName
```

### 步骤 4: 批量替换导入语句

使用 VS Code 全局搜索替换（Ctrl+Shift+H）:

1. **实体导入**:
   - 查找: `import com\.rymcu\.mortise\.entity\.`
   - 替换: `import com.rymcu.mortise.system.entity.`

2. **公共类导入**:
   - 查找: `import com\.rymcu\.mortise\.util\.`
   - 替换: `import com.rymcu.mortise.common.util.`

3. **结果类导入**:
   - 查找: `import com\.rymcu\.mortise\.result\.`
   - 替换: `import com.rymcu.mortise.core.result.`

4. **异常类导入**:
   - 查找: `import com\.rymcu\.mortise\.exception\.`
   - 替换: `import com.rymcu.mortise.common.exception.`

### 步骤 5: 修改 Service 实现中的缓存服务

手动检查以下文件并修改：
- `DictServiceImpl.java`
- `DictTypeServiceImpl.java`
- 其他使用了 `CacheService` 的 Service 实现

```java
// 原代码
@Resource
private CacheService cacheService;

// 修改为（推荐）
@Autowired
private SystemCacheService systemCacheService;
```

### 步骤 6: 编译验证

```bash
# 编译 mortise-system 模块
mvn clean compile -pl mortise-system -am

# 如果有错误，根据提示调整
```

---

## ✅ 迁移检查清单

### Entity 层
- [ ] User.java
- [ ] Role.java
- [ ] Menu.java
- [ ] Dict.java
- [ ] DictType.java
- [ ] UserRole.java
- [ ] RoleMenu.java

### Mapper 层
- [ ] UserMapper.java
- [ ] RoleMapper.java
- [ ] MenuMapper.java
- [ ] DictMapper.java
- [ ] DictTypeMapper.java

### Model 层
- [ ] UserSearch.java
- [ ] RoleSearch.java
- [ ] MenuSearch.java
- [ ] DictSearch.java
- [ ] DictTypeSearch.java
- [ ] BindRoleMenuInfo.java
- [ ] Link.java
- [ ] DictInfo.java
- [ ] BatchUpdateInfo.java

### Service 层
- [ ] UserService.java + UserServiceImpl.java
- [ ] RoleService.java + RoleServiceImpl.java
- [ ] MenuService.java + MenuServiceImpl.java
- [ ] DictService.java + DictServiceImpl.java
- [ ] DictTypeService.java + DictTypeServiceImpl.java

### Controller 层
- [ ] UserController.java
- [ ] RoleController.java
- [ ] MenuController.java
- [ ] DictController.java
- [ ] DictTypeController.java

### 其他
- [ ] DictSerializer.java
- [ ] Handler 相关类

---

## 🚨 常见问题处理

### 问题 1: 找不到 `UserUtils`

**解决方案**: `UserUtils` 可能在 `mortise-app` 模块，需要：
- 选项 A: 将其移到 `mortise-common`
- 选项 B: 在 `mortise-system` 中添加对 `mortise-app` 的依赖（不推荐）
- 选项 C: 在 Controller 中直接使用 Spring Security 的 `SecurityContextHolder`

### 问题 2: `@DictFormat` 注解找不到

**解决方案**: 
- 查找 `DictFormat` 注解的原始位置
- 迁移到 `mortise-common` 或 `mortise-system`

### 问题 3: `CacheService` vs `SystemCacheService`

**解决方案**:
- 推荐使用 `SystemCacheService` 业务封装层
- 或者在 `mortise-system/pom.xml` 中添加 `mortise-cache` 依赖（已添加）

---

## 📚 参考文档

- 迁移指南: `docs/mortise-system-migration-guide.md`
- 检查清单: `docs/mortise-system-migration-checklist.md`
- VS Code 替换配置: `docs/vscode-replace-config.json`
- 重构计划: `REFACTORING_PLAN.md`

---

**最后更新**: 2025-10-01  
**基于**: GitHub 仓库 `rymcu/mortise` 实际代码结构
