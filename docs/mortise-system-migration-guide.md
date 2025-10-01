# 🚀 mortise-system 模块迁移指南

## 📋 目标

将原始 `src/` 目录中的系统业务相关代码迁移到 `mortise-system` 模块。

---

## 🗂️ 迁移清单

### 📦 1. Entity (实体层)

从 `src/main/java/com/rymcu/mortise/entity/` 迁移到 `mortise-system/src/main/java/com/rymcu/mortise/system/entity/`

需要迁移的实体类：
- [ ] `User.java` - 用户实体
- [ ] `Role.java` - 角色实体
- [ ] `Permission.java` - 权限实体
- [ ] `Menu.java` - 菜单实体
- [ ] `Dict.java` - 字典数据实体
- [ ] `DictType.java` - 字典类型实体
- [ ] `UserRole.java` - 用户角色关联实体
- [ ] `RoleMenu.java` - 角色菜单关联实体
- [ ] `RolePermission.java` - 角色权限关联实体
- [ ] `Department.java` - 部门实体 (如果有)
- [ ] `UserDepartment.java` - 用户部门关联 (如果有)

**迁移步骤**：
```bash
# 1. 查看原始代码
ls src/main/java/com/rymcu/mortise/entity/

# 2. 创建目标目录
mkdir -p mortise-system/src/main/java/com/rymcu/mortise/system/entity

# 3. 复制文件
cp src/main/java/com/rymcu/mortise/entity/User.java \
   mortise-system/src/main/java/com/rymcu/mortise/system/entity/

# 4. 修改包名
# 将 package com.rymcu.mortise.entity;
# 改为 package com.rymcu.mortise.system.entity;
```

---

### 📦 2. Mapper (数据访问层)

从 `src/main/java/com/rymcu/mortise/mapper/` 迁移到 `mortise-system/src/main/java/com/rymcu/mortise/system/mapper/`

需要迁移的 Mapper 接口：
- [ ] `UserMapper.java` - 用户 Mapper
- [ ] `RoleMapper.java` - 角色 Mapper
- [ ] `PermissionMapper.java` - 权限 Mapper
- [ ] `MenuMapper.java` - 菜单 Mapper
- [ ] `DictMapper.java` - 字典数据 Mapper
- [ ] `DictTypeMapper.java` - 字典类型 Mapper
- [ ] `UserRoleMapper.java` - 用户角色 Mapper
- [ ] `RoleMenuMapper.java` - 角色菜单 Mapper
- [ ] `RolePermissionMapper.java` - 角色权限 Mapper
- [ ] `DepartmentMapper.java` - 部门 Mapper (如果有)

**迁移步骤**：
```bash
# 1. 创建目标目录
mkdir -p mortise-system/src/main/java/com/rymcu/mortise/system/mapper

# 2. 复制文件
cp src/main/java/com/rymcu/mortise/mapper/User*.java \
   mortise-system/src/main/java/com/rymcu/mortise/system/mapper/

# 3. 修改包名和导入
# package com.rymcu.mortise.mapper; 
# → package com.rymcu.mortise.system.mapper;
#
# import com.rymcu.mortise.entity.User;
# → import com.rymcu.mortise.system.entity.User;
```

**XML 映射文件** (如果有):
从 `src/main/resources/mapper/` 迁移到 `mortise-system/src/main/resources/mapper/`

---

### 📦 3. Model (DTO/VO 层)

从 `src/main/java/com/rymcu/mortise/model/` 迁移到 `mortise-system/src/main/java/com/rymcu/mortise/system/model/`

需要迁移的模型类：
- [ ] `UserInfo.java` - 用户信息 VO
- [ ] `UserDetail.java` - 用户详情 VO
- [ ] `UserSearch.java` - 用户搜索 DTO
- [ ] `RoleInfo.java` - 角色信息 VO
- [ ] `RoleSearch.java` - 角色搜索 DTO
- [ ] `MenuInfo.java` - 菜单信息 VO
- [ ] `MenuTree.java` - 菜单树 VO
- [ ] `DictInfo.java` - 字典信息 VO
- [ ] `DictTypeInfo.java` - 字典类型 VO
- [ ] `PermissionInfo.java` - 权限信息 VO
- [ ] 其他业务 DTO/VO...

**注意**：
- `BaseSearch.java` 应该迁移到 `mortise-core` 模块
- `BaseOption.java` 应该迁移到 `mortise-core` 模块

---

### 📦 4. Service (业务服务层)

#### 4.1 Service 接口

从 `src/main/java/com/rymcu/mortise/service/` 迁移到 `mortise-system/src/main/java/com/rymcu/mortise/system/service/`

需要迁移的 Service 接口：
- [ ] `UserService.java` - 用户服务
- [ ] `RoleService.java` - 角色服务
- [ ] `PermissionService.java` - 权限服务
- [ ] `MenuService.java` - 菜单服务
- [ ] `DictService.java` - 字典服务
- [ ] `DictTypeService.java` - 字典类型服务
- [ ] 其他系统服务...

#### 4.2 Service 实现

从 `src/main/java/com/rymcu/mortise/service/impl/` 迁移到 `mortise-system/src/main/java/com/rymcu/mortise/system/service/impl/`

需要迁移的 Service 实现：
- [ ] `UserServiceImpl.java`
- [ ] `RoleServiceImpl.java`
- [ ] `PermissionServiceImpl.java`
- [ ] `MenuServiceImpl.java`
- [ ] `DictServiceImpl.java`
- [ ] `DictTypeServiceImpl.java`
- [ ] 其他实现类...

**迁移注意事项**：
1. 修改包名
2. 更新导入语句
3. 确保使用 `SystemCacheService` 而不是直接使用 `CacheService`
4. 确保使用 `SystemNotificationService` 而不是直接使用 `NotificationService`

---

### 📦 5. Controller (控制器层)

从 `src/main/java/com/rymcu/mortise/controller/` 迁移到 `mortise-system/src/main/java/com/rymcu/mortise/system/controller/`

需要迁移的 Controller：
- [ ] `UserController.java` - 用户管理接口
- [ ] `RoleController.java` - 角色管理接口
- [ ] `PermissionController.java` - 权限管理接口
- [ ] `MenuController.java` - 菜单管理接口
- [ ] `DictController.java` - 字典管理接口
- [ ] `DictTypeController.java` - 字典类型管理接口
- [ ] 其他系统 Controller...

**迁移注意事项**：
1. 添加 `@OperationLog` 注解记录操作日志
2. 添加 `@RateLimit` 注解进行限流
3. 使用 Swagger 注解（`@Tag`, `@Operation`）
4. 确保使用 `GlobalResult` 统一返回格式

---

### 📦 6. Handler (事件处理器)

从 `src/main/java/com/rymcu/mortise/handler/` 迁移到 `mortise-system/src/main/java/com/rymcu/mortise/system/handler/`

需要迁移的 Handler：
- [ ] `RegisterHandler.java` - 注册事件处理器
- [ ] `AccountHandler.java` - 账户事件处理器
- [ ] `ResetPasswordHandler.java` - 重置密码处理器
- [ ] `UserLoginEventHandler.java` - 用户登录事件处理器
- [ ] `OidcUserEventHandler.java` - OIDC 用户事件处理器

#### 事件定义

从 `src/main/java/com/rymcu/mortise/event/` 或 `handler/event/` 迁移到 `mortise-system/src/main/java/com/rymcu/mortise/system/handler/event/`

需要迁移的 Event：
- [ ] `RegisterEvent.java` - 注册事件
- [ ] `AccountEvent.java` - 账户事件
- [ ] `ResetPasswordEvent.java` - 重置密码事件
- [ ] `UserLoginEvent.java` - 用户登录事件
- [ ] `OidcUserEvent.java` - OIDC 用户事件

---

### 📦 7. Serializer (序列化器)

从 `src/main/java/com/rymcu/mortise/serializer/` 迁移到 `mortise-system/src/main/java/com/rymcu/mortise/system/serializer/`

需要迁移的 Serializer：
- [ ] `DictSerializer.java` - 字典序列化器

---

### 📦 8. Constant (常量)

从 `src/main/java/com/rymcu/mortise/constant/` 中筛选系统业务相关常量：

- [ ] 创建 `mortise-system/src/main/java/com/rymcu/mortise/system/constant/SystemConstant.java`
- [ ] 迁移系统业务相关的常量定义

**注意**：基础常量应该保留在 `mortise-common` 的 `ProjectConstant` 中

---

### 📦 9. Resources (资源文件)

#### 9.1 Mapper XML 文件

从 `src/main/resources/mapper/` 迁移到 `mortise-system/src/main/resources/mapper/`

```bash
mkdir -p mortise-system/src/main/resources/mapper
cp src/main/resources/mapper/*.xml \
   mortise-system/src/main/resources/mapper/
```

#### 9.2 配置文件

检查是否有模块专属配置需要迁移到 `mortise-system/src/main/resources/`

---

## 🔧 迁移后的调整

### 1. 包名调整

所有文件的包名需要调整为：
```java
// 原包名
package com.rymcu.mortise.entity;
package com.rymcu.mortise.mapper;
package com.rymcu.mortise.service;
package com.rymcu.mortise.controller;

// 新包名
package com.rymcu.mortise.system.entity;
package com.rymcu.mortise.system.mapper;
package com.rymcu.mortise.system.service;
package com.rymcu.mortise.system.controller;
```

### 2. 导入语句调整

更新所有导入语句：
```java
// 实体
import com.rymcu.mortise.entity.User;
→ import com.rymcu.mortise.system.entity.User;

// 公共类
import com.rymcu.mortise.util.Utils;
→ import com.rymcu.mortise.common.util.Utils;

// 结果类
import com.rymcu.mortise.result.GlobalResult;
→ import com.rymcu.mortise.core.result.GlobalResult;

// 异常
import com.rymcu.mortise.exception.BusinessException;
→ import com.rymcu.mortise.common.exception.BusinessException;
```

### 3. 业务封装层使用

在 Service 实现中：
```java
// ❌ 不推荐
@Autowired
private CacheService cacheService;

public void updateUser(User user) {
    // ...
    cacheService.set("user:info:" + user.getId(), userInfo, Duration.ofHours(1));
}

// ✅ 推荐
@Autowired
private SystemCacheService systemCacheService;

public void updateUser(User user) {
    // ...
    systemCacheService.cacheUserInfo(user.getId(), userInfo);
}
```

### 4. 注解添加

#### Controller 层：
```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户管理接口")
public class UserController {
    
    @PostMapping
    @OperationLog(module = "用户管理", operation = "创建用户")
    @RateLimit(key = "user:create", limit = 10, period = 60)
    @Operation(summary = "创建用户")
    public GlobalResult<UserInfo> createUser(@RequestBody UserDTO dto) {
        // ...
    }
}
```

#### Service 层：
```java
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    // 使用业务封装层
    @Autowired
    private SystemCacheService systemCacheService;
    
    @Autowired
    private SystemNotificationService systemNotificationService;
}
```

---

## 📝 验证清单

迁移完成后，请验证：

- [ ] 所有文件的包名正确
- [ ] 所有导入语句正确
- [ ] Maven 编译通过 (`mvn clean compile -pl mortise-system -am`)
- [ ] 单元测试通过 (如果有)
- [ ] Controller 接口可以正常访问
- [ ] 数据库操作正常
- [ ] 缓存操作正常
- [ ] 日志记录正常
- [ ] 通知发送正常

---

## 🚀 快速开始

### 步骤 1: 检查原始代码结构

```bash
# 查看原始 entity
ls -la src/main/java/com/rymcu/mortise/entity/

# 查看原始 mapper
ls -la src/main/java/com/rymcu/mortise/mapper/

# 查看原始 service
ls -la src/main/java/com/rymcu/mortise/service/

# 查看原始 controller
ls -la src/main/java/com/rymcu/mortise/controller/
```

### 步骤 2: 批量迁移脚本 (PowerShell)

```powershell
# 创建目标目录
$baseDir = "mortise-system/src/main/java/com/rymcu/mortise/system"
New-Item -ItemType Directory -Force -Path "$baseDir/entity"
New-Item -ItemType Directory -Force -Path "$baseDir/mapper"
New-Item -ItemType Directory -Force -Path "$baseDir/model"
New-Item -ItemType Directory -Force -Path "$baseDir/service"
New-Item -ItemType Directory -Force -Path "$baseDir/service/impl"
New-Item -ItemType Directory -Force -Path "$baseDir/controller"
New-Item -ItemType Directory -Force -Path "$baseDir/handler"
New-Item -ItemType Directory -Force -Path "$baseDir/handler/event"
New-Item -ItemType Directory -Force -Path "$baseDir/serializer"
New-Item -ItemType Directory -Force -Path "$baseDir/constant"

# 复制文件（示例）
Copy-Item "src/main/java/com/rymcu/mortise/entity/*.java" "$baseDir/entity/" -Force
Copy-Item "src/main/java/com/rymcu/mortise/mapper/*.java" "$baseDir/mapper/" -Force
# ... 其他复制操作
```

### 步骤 3: 批量替换包名

使用 VS Code 的全局搜索替换：
1. 打开 `mortise-system` 文件夹
2. Ctrl+Shift+H 打开全局替换
3. 查找: `package com.rymcu.mortise.entity;`
4. 替换为: `package com.rymcu.mortise.system.entity;`
5. 替换全部

重复以上步骤替换其他包名。

### 步骤 4: 编译验证

```bash
# 编译 mortise-system 模块
mvn clean compile -pl mortise-system -am

# 如果有错误，根据错误提示调整导入语句
```

---

## 💡 提示

1. **分批迁移**: 建议按层次分批迁移，先 entity → mapper → model → service → controller
2. **逐步验证**: 每迁移一层就编译验证一次
3. **保留原文件**: 迁移完成并验证无误后再删除原文件
4. **使用 Git**: 每完成一层迁移就提交一次，方便回滚

---

**准备好开始迁移了吗？** 🚀

建议从 Entity 层开始！
