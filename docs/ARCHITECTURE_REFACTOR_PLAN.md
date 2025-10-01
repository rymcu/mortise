# Mortise-System 模块架构重构分析

## 🏗️ **模块职责分析**

### **当前模块架构**

```
mortise-auth (认证基础设施)
├── 职责：认证、授权、JWT 处理
├── 依赖：mortise-core, mortise-common
└── 提供：认证服务、JWT 工具

mortise-common (通用工具)
├── 职责：通用工具类、常量、异常
├── 依赖：最小依赖
└── 提供：Utils, JasyptUtils, ProjectConstant

mortise-core (核心业务)
├── 职责：核心业务逻辑、结果封装
├── 依赖：mortise-common
└── 提供：GlobalResult, ResultCode

mortise-system (系统业务)
├── 职责：用户、角色、菜单、字典管理
├── 依赖：mortise-auth, mortise-core, mortise-common
└── 提供：User, Role, Menu 等业务实体
```

---

## 📦 **类的归属分析**

### **1. 认证相关类 → mortise-auth**

| 类名 | 当前位置 | 目标位置 | 理由 |
|------|----------|----------|------|
| `JwtUtils` | mortise-temp/auth | **mortise-auth** | ✅ 纯 JWT 工具，无业务逻辑 |
| `JwtConstants` | mortise-temp/auth | **mortise-auth** | ✅ JWT 常量定义 |
| `TokenModel` | mortise-temp/auth | **mortise-auth** | ✅ JWT Token 数据模型 |
| `TokenManager` | mortise-temp/auth | **mortise-auth** | ✅ Token 管理接口 |
| `CacheTokenManager` | mortise-temp/auth | **mortise-auth** | ✅ Token 缓存实现 |
| `JwtAuthenticationFilter` | mortise-temp/auth | **mortise-auth** | ✅ 认证过滤器 |
| `JwtAuthenticationEntryPoint` | mortise-temp/auth | **mortise-auth** | ✅ 认证入口点 |
| `JwtProperties` | mortise-temp/config | **mortise-auth** | ✅ JWT 配置属性 |

### **2. 异常类 → mortise-common**

| 类名 | 当前位置 | 目标位置 | 理由 |
|------|----------|----------|------|
| `AccountExistsException` | mortise-temp/core/exception | **mortise-common/exception** | ✅ 通用业务异常 |

### **3. 业务模型类 → mortise-system**

| 类名 | 当前位置 | 目标位置 | 理由 |
|------|----------|----------|------|
| `TokenUser` | mortise-temp/model | **mortise-system/model** | ✅ 用户相关的业务模型 |
| `UserUtils` | mortise-system/util | **mortise-system/util** | ✅ 依赖 UserService，属于业务层 |

### **4. 事件处理类 → mortise-system**

| 类名 | 当前位置 | 目标位置 | 理由 |
|------|----------|----------|------|
| `AccountEvent` | mortise-temp/handler/event | **mortise-system/handler/event** | ✅ 已迁移，用户账户事件 |
| `AccountHandler` | mortise-temp/handler | **mortise-system/handler** | ✅ 已迁移，业务事件处理 |

---

## 🔧 **修复方案**

### **阶段 1：迁移认证类到 mortise-auth**

```powershell
# 1. 创建目录结构
New-Item -ItemType Directory -Path "mortise-auth\src\main\java\com\rymcu\mortise\auth" -Force

# 2. 复制认证相关类
$authFiles = @(
    "JwtUtils.java",
    "JwtConstants.java", 
    "TokenModel.java",
    "TokenManager.java",
    "CacheTokenManager.java",
    "JwtAuthenticationFilter.java",
    "JwtAuthenticationEntryPoint.java"
)

foreach ($file in $authFiles) {
    Copy-Item "mortise-temp\src\main\java\com\rymcu\mortise\auth\$file" `
              "mortise-auth\src\main\java\com\rymcu\mortise\auth\$file" -Force
}

# 3. 复制 JWT 配置
Copy-Item "mortise-temp\src\main\java\com\rymcu\mortise\config\JwtProperties.java" `
          "mortise-auth\src\main\java\com\rymcu\mortise\auth\JwtProperties.java" -Force
```

### **阶段 2：迁移异常类到 mortise-common**

```powershell
# 1. 创建异常目录
New-Item -ItemType Directory -Path "mortise-common\src\main\java\com\rymcu\mortise\common\exception" -Force

# 2. 复制异常类
Copy-Item "mortise-temp\src\main\java\com\rymcu\mortise\core\exception\AccountExistsException.java" `
          "mortise-common\src\main\java\com\rymcu\mortise\common\exception\AccountExistsException.java" -Force
```

### **阶段 3：迁移业务模型到 mortise-system**

```powershell
# TokenUser 已经在 mortise-system 中，只需要检查包名
```

### **阶段 4：更新 pom.xml 依赖**

#### **mortise-auth/pom.xml 添加依赖**

```xml
<!-- JWT 相关 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 (optional) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
    <optional>true</optional>
</dependency>
```

### **阶段 5：更新所有 import 语句**

使用 VS Code 全局替换（`Ctrl+Shift+H`），启用正则：

| 搜索模式 | 替换为 | 文件过滤 |
|----------|--------|----------|
| `import com\.rymcu\.mortise\.auth\.Jwt` | `import com.rymcu.mortise.auth.Jwt` | `**/*.java` |
| `import com\.rymcu\.mortise\.auth\.Token` | `import com.rymcu.mortise.auth.Token` | `**/*.java` |
| `import com\.rymcu\.mortise\.core\.exception\.AccountExistsException` | `import com.rymcu.mortise.common.exception.AccountExistsException` | `**/*.java` |
| `import com\.rymcu\.mortise\.config\.JwtProperties` | `import com.rymcu.mortise.auth.config.JwtProperties` | `**/*.java` |

---

## 📊 **依赖关系图**

```
mortise-system (业务层)
├── UserService (依赖 User 实体)
├── UserUtils (依赖 UserService + JwtUtils)
├── TokenUser (业务模型)
└── AccountHandler (业务事件处理)
    ↓ 依赖
mortise-auth (认证基础设施)
├── JwtUtils (纯工具，依赖 JwtProperties)
├── JwtConstants (常量)
├── TokenModel (数据模型)
├── TokenManager (接口)
└── CacheTokenManager (实现，依赖 mortise-cache)
    ↓ 依赖
mortise-core (核心)
├── GlobalResult (结果封装)
└── ResultCode (结果码)
    ↓ 依赖
mortise-common (通用)
├── Utils (工具类)
├── JasyptUtils (加密工具)
├── ProjectConstant (项目常量)
└── AccountExistsException (业务异常)
```

---

## ✅ **执行检查清单**

### **mortise-auth 模块**
- [ ] 复制所有 JWT 相关类
- [ ] 更新 pom.xml 添加 JWT 和 Security 依赖
- [ ] 修复 import 语句
- [ ] 编译通过

### **mortise-common 模块**
- [ ] 复制 AccountExistsException
- [ ] 更新包名为 common.exception
- [ ] 编译通过

### **mortise-system 模块**
- [ ] 确保 TokenUser 包名正确
- [ ] 确保 UserUtils 使用正确的 auth 模块类
- [ ] 更新所有 import 语句
- [ ] 编译通过

### **全项目验证**
- [ ] 所有模块独立编译成功
- [ ] 完整项目编译成功
- [ ] 依赖关系清晰，无循环依赖

---

## 🚀 **立即执行**

**优先级**：
1. **高优先级**：迁移 JWT 类到 mortise-auth（解决 UserUtils 编译问题）
2. **中优先级**：迁移异常类到 mortise-common
3. **低优先级**：清理和优化

开始执行：
```powershell
# 执行阶段 1：认证类迁移
.\migrate-auth-classes.ps1

# 执行阶段 2：异常类迁移
.\migrate-exception-classes.ps1

# 执行阶段 3：编译验证
mvn clean compile
```

---

**架构清晰，职责分明！** 🏗️
