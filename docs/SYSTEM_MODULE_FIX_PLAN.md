# Mortise-System 模块代码梳理报告

## 📋 **问题分析**

### **编译错误分类**

根据编译错误，主要有以下几类问题：

#### **1. Import 语句未更新** (高优先级)
- ❌ `import com.rymcu.mortise.annotation.*` → 应为 `import com.rymcu.mortise.system.annotation.*`
- ❌ `import com.rymcu.mortise.serializer.*` → 应为 `import com.rymcu.mortise.system.serializer.*`
- ❌ `import com.rymcu.mortise.handler.event.*` → 应为 `import com.rymcu.mortise.system.handler.event.*`
- ❌ `import com.rymcu.mortise.system.util.UserUtils` → 应为 `import com.rymcu.mortise.auth.util.UserUtils` (需从GitHub复制)
- ❌ `import com.rymcu.mortise.system.util.Utils` → 应为 `import com.rymcu.mortise.common.util.Utils` (已存在)
- ❌ `import com.rymcu.mortise.system.util.BeanCopierUtil` → 应为 `import com.rymcu.mortise.common.util.BeanCopierUtil` (需迁移)

#### **2. 缺少 Spring Security 依赖** (高优先级)
- ❌ Controller 中使用 `@PreAuthorize` 但缺少依赖
- ❌ 缺少 `spring-boot-starter-security`

#### **3. 缺少 OAuth2 依赖** (中优先级)
- ❌ `OidcUserEventHandler` 使用 `org.springframework.security.oauth2.core.oidc.user.*`
- ❌ 需要添加 `spring-boot-starter-oauth2-client`

#### **4. 工具类缺失** (中优先级)
- ❌ `UserUtils` - 需要从 mortise-auth 或 GitHub 复制
- ❌ `BeanCopierUtil` - 需要从 mortise-temp 迁移到 mortise-common

---

## 🔧 **修复方案**

### **阶段 1：更新 pom.xml 依赖**

需要添加以下依赖：

```xml
<!-- Spring Security (for @PreAuthorize) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Client (for OidcUser) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
    <optional>true</optional>
</dependency>

<!-- Mortise Auth Module (for UserUtils) -->
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-auth</artifactId>
</dependency>
```

### **阶段 2：批量替换 Import 语句**

使用 VS Code 全局替换（`Ctrl+Shift+H`），文件过滤：`mortise-system/**/*.java`，启用正则：

| 顺序 | 搜索（正则） | 替换为 | 说明 |
|------|-------------|--------|------|
| 1 | `import com\.rymcu\.mortise\.annotation\.` | `import com.rymcu.mortise.system.annotation.` | 注解类 |
| 2 | `import com\.rymcu\.mortise\.serializer\.` | `import com.rymcu.mortise.system.serializer.` | 序列化类 |
| 3 | `import com\.rymcu\.mortise\.handler\.event\.` | `import com.rymcu.mortise.system.handler.event.` | 事件类 |
| 4 | `import com\.rymcu\.mortise\.system\.util\.UserUtils;` | `import com.rymcu.mortise.auth.util.UserUtils;` | UserUtils |
| 5 | `import com\.rymcu\.mortise\.system\.util\.Utils;` | `import com.rymcu.mortise.common.util.Utils;` | Utils |
| 6 | `import com\.rymcu\.mortise\.system\.util\.BeanCopierUtil;` | `import com.rymcu.mortise.common.util.BeanCopierUtil;` | BeanCopierUtil |

### **阶段 3：迁移缺失的工具类**

#### **3.1 迁移 BeanCopierUtil**

```powershell
# 从 mortise-temp 复制到 mortise-common
Copy-Item "mortise-temp\src\main\java\com\rymcu\mortise\util\BeanCopierUtil.java" `
          "mortise-common\src\main\java\com\rymcu\mortise\common\util\BeanCopierUtil.java"
```

然后更新包名：
```java
package com.rymcu.mortise.common.util;
```

#### **3.2 确认 UserUtils 位置**

检查 mortise-auth 中是否有 UserUtils：
```powershell
Get-ChildItem -Path "mortise-auth" -Filter "UserUtils.java" -Recurse
```

如果没有，从 GitHub 或 mortise-temp 复制：
```powershell
# 方案 A: 从 mortise-temp 复制到 mortise-auth
Copy-Item "mortise-temp\src\main\java\com\rymcu\mortise\util\UserUtils.java" `
          "mortise-auth\src\main\java\com\rymcu\mortise\auth\util\UserUtils.java"

# 方案 B: 从 GitHub 克隆到 mortise-auth
# (已在 migrate-system-from-github.ps1 中)
```

### **阶段 4：验证和编译**

```powershell
# 1. 重新编译 mortise-common (如果添加了 BeanCopierUtil)
mvn clean compile -pl mortise-common -am

# 2. 重新编译 mortise-auth (如果添加了 UserUtils)
mvn clean compile -pl mortise-auth -am

# 3. 编译 mortise-system
mvn clean compile -pl mortise-system -am

# 4. 完整编译
mvn clean compile
```

---

## 📊 **影响文件清单**

### **需要修改 Import 的文件**

| 文件 | 问题 Import | 修复后 Import |
|------|-------------|--------------|
| `entity/Dict.java` | `com.rymcu.mortise.annotation.DictFormat` | `com.rymcu.mortise.system.annotation.DictFormat` |
| `entity/DictType.java` | `com.rymcu.mortise.annotation.DictFormat` | `com.rymcu.mortise.system.annotation.DictFormat` |
| `entity/Menu.java` | `com.rymcu.mortise.annotation.DictFormat` | `com.rymcu.mortise.system.annotation.DictFormat` |
| `entity/Role.java` | `com.rymcu.mortise.annotation.DictFormat` | `com.rymcu.mortise.system.annotation.DictFormat` |
| `entity/User.java` | `com.rymcu.mortise.annotation.DictFormat` | `com.rymcu.mortise.system.annotation.DictFormat` |
| `model/UserInfo.java` | `com.rymcu.mortise.annotation.DictFormat` | `com.rymcu.mortise.system.annotation.DictFormat` |
| `annotation/DictAnnotationIntrospector.java` | `com.rymcu.mortise.serializer.DictSerializer` | `com.rymcu.mortise.system.serializer.DictSerializer` |
| `handler/AccountHandler.java` | `com.rymcu.mortise.handler.event.AccountEvent` | `com.rymcu.mortise.system.handler.event.AccountEvent` |
| `handler/RegisterHandler.java` | `com.rymcu.mortise.handler.event.RegisterEvent` | `com.rymcu.mortise.system.handler.event.RegisterEvent` |
| `handler/ResetPasswordHandler.java` | `com.rymcu.mortise.handler.event.ResetPasswordEvent` | `com.rymcu.mortise.system.handler.event.ResetPasswordEvent` |
| `handler/OidcUserEventHandler.java` | `com.rymcu.mortise.handler.event.OidcUserEvent` | `com.rymcu.mortise.system.handler.event.OidcUserEvent` |
| `handler/UserLoginEventHandler.java` | `com.rymcu.mortise.handler.event.AccountEvent` | `com.rymcu.mortise.system.handler.event.AccountEvent` |
| `service/impl/UserServiceImpl.java` | `com.rymcu.mortise.handler.event.*` | `com.rymcu.mortise.system.handler.event.*` |
| `service/impl/UserServiceImpl.java` | `com.rymcu.mortise.system.util.Utils` | `com.rymcu.mortise.common.util.Utils` |
| `service/impl/JavaMailServiceImpl.java` | `com.rymcu.mortise.system.util.Utils` | `com.rymcu.mortise.common.util.Utils` |
| `service/impl/UserCacheServiceImpl.java` | `com.rymcu.mortise.system.util.BeanCopierUtil` | `com.rymcu.mortise.common.util.BeanCopierUtil` |
| `controller/DictController.java` | `com.rymcu.mortise.system.util.UserUtils` | `com.rymcu.mortise.auth.util.UserUtils` |
| `controller/DictTypeController.java` | `com.rymcu.mortise.system.util.UserUtils` | `com.rymcu.mortise.auth.util.UserUtils` |

### **需要添加依赖的文件**

- `pom.xml` - 添加 Spring Security、OAuth2 Client、mortise-auth

### **需要迁移的工具类**

- `BeanCopierUtil.java` - 从 mortise-temp 迁移到 mortise-common
- `UserUtils.java` - 确认在 mortise-auth 中存在

---

## ⏱️ **执行时间估算**

- 阶段 1 (更新 pom.xml): 5 分钟
- 阶段 2 (批量替换 Import): 10 分钟
- 阶段 3 (迁移工具类): 10 分钟
- 阶段 4 (验证编译): 15 分钟
- **总计: 约 40 分钟**

---

## ✅ **成功标准**

- [ ] pom.xml 添加了所有必需依赖
- [ ] 所有 Import 语句已更新为正确的包名
- [ ] BeanCopierUtil 已迁移到 mortise-common
- [ ] UserUtils 在 mortise-auth 中可用
- [ ] `mvn clean compile -pl mortise-system -am` 成功
- [ ] 无编译错误
- [ ] 完整项目编译成功

---

## 🚀 **立即执行**

执行以下 PowerShell 脚本开始修复：

```powershell
# 1. 更新 pom.xml (手动或使用脚本)
code mortise-system\pom.xml

# 2. 迁移 BeanCopierUtil
.\scripts\migrate-bean-copier.ps1

# 3. 批量替换 Import (使用 VS Code)
# Ctrl+Shift+H → 启用正则 → files to include: mortise-system/**/*.java
# 按照上表执行 6 次替换

# 4. 编译验证
mvn clean compile -pl mortise-system -am
```

---

**准备就绪，开始修复！** 🔧
