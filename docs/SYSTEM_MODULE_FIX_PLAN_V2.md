# Mortise-System 模块代码梳理方案（修订版）

## 🎯 **核心问题分析**

### **UserUtils 的架构问题**

❌ **错误方案**：将 UserUtils 放到 mortise-auth  
✅ **正确方案**：将 UserUtils 保留在 mortise-system（或创建 system.util 子包）

**原因**：
1. UserUtils 依赖 `UserService`（业务层）
2. UserUtils 依赖 `User` 实体（system 模块的实体）
3. UserUtils 使用 `SpringContextHolder` 获取 Bean（运行时依赖）
4. 这是典型的业务工具类，不应该放在基础设施层（auth）

### **模块依赖关系**

```
mortise-system (业务层)
    ├── 依赖 mortise-auth (认证基础设施)
    ├── 依赖 mortise-web (Web 基础设施)
    ├── 依赖 mortise-cache (缓存基础设施)
    └── 包含 UserUtils (业务工具类，依赖 UserService)

mortise-auth (基础设施层)
    ├── 提供 JwtUtils, JwtConstants (纯工具)
    └── 不应该依赖业务层的 UserService
```

---

## 🔧 **修订后的修复方案**

### **方案 A：保留 UserUtils 在 mortise-system.util**

**优点**：
- 符合模块依赖原则（业务层可依赖基础设施层）
- 不引入循环依赖
- UserUtils 与 UserService 在同一模块

**实施步骤**：

1. **不需要迁移 UserUtils**，它已经在 `mortise-system/util/` 下
2. **只需修改 Controller 中的 import**：
   ```java
   // 保持为 system 内部引用
   import com.rymcu.mortise.system.util.UserUtils;
   ```

### **方案 B：创建 UserContext（推荐，解耦更好）**

**优点**：
- 更清晰的职责分离
- 使用 Spring Security 标准方式
- 减少对 UserUtils 的依赖

**实施步骤**：

1. 在 Controller 中直接使用 Spring Security：
   ```java
   @GetMapping
   public GlobalResult<Page<Dict>> listDict(
       @AuthenticationPrincipal User currentUser,
       @Valid DictSearch search) {
       // 使用 currentUser
   }
   ```

2. 或创建 `SecurityContextUtils`（放在 mortise-auth）：
   ```java
   public class SecurityContextUtils {
       public static String getCurrentUsername() {
           Authentication auth = SecurityContextHolder.getContext().getAuthentication();
           return auth.getName();
       }
   }
   ```

---

## 📝 **最终修复方案（混合方案）**

### **阶段 1：更新 pom.xml** ✅ (已完成)

```xml
<!-- 已添加 -->
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-auth</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
    <optional>true</optional>
</dependency>
```

### **阶段 2：迁移 BeanCopierUtil** ✅ (已完成)

已迁移到 `mortise-common/util/BeanCopierUtil.java`

### **阶段 3：检查 SpringContextHolder**

UserUtils 依赖 `SpringContextHolder.getBean()`，确保该类存在：

```powershell
# 检查是否存在
Get-ChildItem -Path "mortise-common" -Filter "SpringContextHolder.java" -Recurse
```

### **阶段 4：批量替换 Import 语句（修订版）**

使用 VS Code 全局替换（`Ctrl+Shift+H`），文件过滤：`mortise-system/**/*.java`，启用正则：

| # | 搜索（正则） | 替换为 | 说明 |
|---|-------------|--------|------|
| 1 | `import com\.rymcu\.mortise\.annotation\.` | `import com.rymcu.mortise.system.annotation.` | 注解类 |
| 2 | `import com\.rymcu\.mortise\.serializer\.` | `import com.rymcu.mortise.system.serializer.` | 序列化类 |
| 3 | `import com\.rymcu\.mortise\.handler\.event\.` | `import com.rymcu.mortise.system.handler.event.` | 事件类 |
| 4 | `import com\.rymcu\.mortise\.system\.util\.Utils;` | `import com.rymcu.mortise.common.util.Utils;` | Utils |
| 5 | `import com\.rymcu\.mortise\.system\.util\.BeanCopierUtil;` | `import com.rymcu.mortise.common.util.BeanCopierUtil;` | BeanCopierUtil |
| 6 | ❌ ~~UserUtils~~ | **保持不变** | UserUtils 保留在 system.util |

**关键变化**：**UserUtils 不需要替换**，保持为 `com.rymcu.mortise.system.util.UserUtils`

### **阶段 5：处理 UserUtils 依赖**

检查 UserUtils 所需的其他类：

```java
// UserUtils 依赖：
import com.rymcu.mortise.auth.constant.JwtConstants;        // ✅ mortise-auth 提供
import com.rymcu.mortise.auth.JwtUtils;            // ✅ mortise-auth 提供
import com.rymcu.mortise.auth.model.TokenModel;          // ✅ mortise-auth 提供
import com.rymcu.mortise.core.result.ResultCode;   // ✅ mortise-core 提供
import com.rymcu.mortise.entity.User;              // ❌ 需要改为 system.entity.User
import com.rymcu.mortise.model.TokenUser;          // ❌ 需要改为 system.model.TokenUser
import com.rymcu.mortise.service.UserService;      // ❌ 需要改为 system.service.UserService
```

需要更新 UserUtils 中的 import：

1. 如果 UserUtils 已经在 mortise-system 中，检查其 import 语句
2. 确保引用的是 `com.rymcu.mortise.system.*` 包

---

## 🚀 **执行步骤**

### **步骤 1：检查 SpringContextHolder**

```powershell
Get-ChildItem -Path "mortise-common" -Filter "SpringContextHolder.java" -Recurse
```

如果不存在，从 mortise-temp 复制：
```powershell
Copy-Item "mortise-temp\src\main\java\com\rymcu\mortise\util\SpringContextHolder.java" `
          "mortise-common\src\main\java\com\rymcu\mortise\common\util\SpringContextHolder.java"
```

### **步骤 2：批量替换 Import（5次替换）**

VS Code: `Ctrl+Shift+H` → 启用正则 → `files to include: mortise-system/**/*.java`

执行以下 5 次替换（不包括 UserUtils）：

1. `import com\.rymcu\.mortise\.annotation\.` → `import com.rymcu.mortise.system.annotation.`
2. `import com\.rymcu\.mortise\.serializer\.` → `import com.rymcu.mortise.system.serializer.`
3. `import com\.rymcu\.mortise\.handler\.event\.` → `import com.rymcu.mortise.system.handler.event.`
4. `import com\.rymcu\.mortise\.system\.util\.Utils;` → `import com.rymcu.mortise.common.util.Utils;`
5. `import com\.rymcu\.mortise\.system\.util\.BeanCopierUtil;` → `import com.rymcu.mortise.common.util.BeanCopierUtil;`

### **步骤 3：检查 UserUtils 的 import**

```powershell
Get-Content "mortise-system\src\main\java\com\rymcu\mortise\system\util\UserUtils.java" | Select-String "import"
```

如果有旧包名，手动更新为：
- `import com.rymcu.mortise.system.entity.User;`
- `import com.rymcu.mortise.system.model.auth.TokenUser;`
- `import com.rymcu.mortise.system.service.UserService;`

### **步骤 4：编译验证**

```powershell
# 编译 mortise-common (BeanCopierUtil)
mvn clean compile -pl mortise-common -am

# 编译 mortise-system
mvn clean compile -pl mortise-system -am

# 完整编译
mvn clean compile
```

---

## ✅ **成功标准**

- [x] pom.xml 添加了 mortise-auth、Spring Security、OAuth2 依赖
- [x] BeanCopierUtil 已迁移到 mortise-common
- [ ] SpringContextHolder 在 mortise-common 中可用
- [ ] UserUtils 保留在 mortise-system.util，import 已更新
- [ ] 所有其他 Import 语句已更新为正确的包名
- [ ] `mvn clean compile -pl mortise-system -am` 成功
- [ ] 无编译错误

---

## 📊 **架构图**

```
mortise-system (业务层)
  └── util/
      └── UserUtils.java (业务工具，依赖 UserService)
          ├── import com.rymcu.mortise.auth.JwtUtils (基础设施)
          ├── import com.rymcu.mortise.system.entity.User (本模块)
          ├── import com.rymcu.mortise.system.service.UserService (本模块)
          └── import com.rymcu.mortise.common.util.SpringContextHolder (通用工具)

mortise-auth (基础设施层)
  └── 提供 JwtUtils, JwtConstants (无业务逻辑)

mortise-common (通用层)
  └── util/
      ├── BeanCopierUtil.java (通用工具)
      └── SpringContextHolder.java (Spring 上下文工具)
```

---

**准备执行修复！** 🔧
