# 🎯 mortise-system 迁移快速参考

## 📌 一句话总结

**从 GitHub 克隆原始代码 → 复制到 mortise-system → 批量替换包名和导入 → 编译验证**

---

## 🚀 三步快速开始

### 第一步: 获取原始代码

```powershell
# 在项目根目录执行
git clone https://github.com/rymcu/mortise.git mortise-temp
```

### 第二步: 批量复制文件

```powershell
# 执行迁移脚本
.\migrate-system-from-github.ps1
```

### 第三步: VS Code 批量替换

打开 VS Code → Ctrl+Shift+H → 启用正则表达式 → 按照下表依次替换：

| 序号 | 查找内容 | 替换内容 | 文件范围 |
|------|----------|----------|----------|
| 1 | `import com\.rymcu\.mortise\.entity\.` | `import com.rymcu.mortise.system.entity.` | `mortise-system/**/*.java` |
| 2 | `import com\.rymcu\.mortise\.mapper\.` | `import com.rymcu.mortise.system.mapper.` | `mortise-system/**/*.java` |
| 3 | `import com\.rymcu\.mortise\.model\.` | `import com.rymcu.mortise.system.model.` | `mortise-system/**/*.java` |
| 4 | `import com\.rymcu\.mortise\.service\.` | `import com.rymcu.mortise.system.service.` | `mortise-system/**/*.java` |
| 5 | `import com\.rymcu\.mortise\.util\.` | `import com.rymcu.mortise.common.util.` | `mortise-system/**/*.java` |
| 6 | `import com\.rymcu\.mortise\.result\.` | `import com.rymcu.mortise.core.result.` | `mortise-system/**/*.java` |
| 7 | `import com\.rymcu\.mortise\.exception\.` | `import com.rymcu.mortise.common.exception.` | `mortise-system/**/*.java` |

---

## 📋 需要迁移的文件清单

### ✅ 核心业务文件（必须迁移）

**Entity (7 个)**:
```
User.java, Role.java, Menu.java, Dict.java, DictType.java
UserRole.java, RoleMenu.java
```

**Mapper (5 个)**:
```
UserMapper.java, RoleMapper.java, MenuMapper.java
DictMapper.java, DictTypeMapper.java
```

**Service (10 个)**:
```
接口: UserService, RoleService, MenuService, DictService, DictTypeService
实现: UserServiceImpl, RoleServiceImpl, MenuServiceImpl, DictServiceImpl, DictTypeServiceImpl
```

**Controller (5 个)**:
```
UserController.java, RoleController.java, MenuController.java
DictController.java, DictTypeController.java
```

**Model (9+ 个)**:
```
UserSearch, RoleSearch, MenuSearch, DictSearch, DictTypeSearch
BindRoleMenuInfo, Link, DictInfo, BatchUpdateInfo
```

### ⚠️ 需要特殊处理

**UserUtils.java**:
- 位置: `src/main/java/com/rymcu/mortise/util/`
- 问题: Controller 中使用 `UserUtils.getCurrentUserByToken()`
- 解决: 迁移到 `mortise-app/util/` 或 `mortise-common/util/`

**DictFormat.java** (注解):
- 位置: `src/main/java/com/rymcu/mortise/annotation/`
- 问题: Entity 中使用 `@DictFormat`
- 解决: 迁移到 `mortise-common/annotation/`

**CacheService**:
- 问题: Service 实现中直接注入 `CacheService`
- 解决: 改用 `SystemCacheService` 或保持不变（已有依赖）

---

## 🔧 关键修改点

### 1. Service 实现中的缓存

**查找这些文件**:
- `DictServiceImpl.java`
- `DictTypeServiceImpl.java`

**修改**:
```java
// 可选：将 CacheService 改为 SystemCacheService
@Autowired
private SystemCacheService systemCacheService;
```

### 2. Controller 路径

**原路径**: `src/main/java/com/rymcu/mortise/web/admin/`  
**新路径**: `mortise-system/src/main/java/com/rymcu/mortise/system/controller/`

**修改包名**:
```java
// 原包名
package com.rymcu.mortise.web.admin;

// 新包名
package com.rymcu.mortise.system.controller;
```

---

## ✅ 验证命令

```bash
# 1. 编译验证
mvn clean compile -pl mortise-system -am

# 2. 运行验证脚本
.\verify-system.ps1

# 3. 完整编译
mvn clean compile
```

---

## 📊 预计工作量

| 阶段 | 任务 | 预计时间 |
|------|------|----------|
| 1️⃣ | 获取原始代码 | 5分钟 |
| 2️⃣ | 批量复制文件 | 10分钟 |
| 3️⃣ | 批量替换（VS Code） | 15分钟 |
| 4️⃣ | 手动调整特殊文件 | 20分钟 |
| 5️⃣ | 编译验证和修复 | 30分钟 |
| **总计** | | **~1.5小时** |

---

## 🎁 提供的工具

1. **migrate-system-from-github.ps1** - 自动从 GitHub 克隆并复制文件
2. **verify-system.ps1** - 验证迁移质量
3. **vscode-replace-config.json** - VS Code 批量替换配置
4. **mortise-system-migration-checklist.md** - 详细检查清单

---

## 🆘 遇到问题？

### 编译错误: "cannot find symbol"
→ 检查导入语句是否正确替换

### 编译错误: "package does not exist"
→ 检查依赖模块是否已编译 (`mvn compile -pl mortise-common,mortise-core -am`)

### Controller 中找不到 UserUtils
→ 暂时注释掉或使用 SecurityContextHolder

---

## 📚 详细文档

完整指南请参考：
- 📖 `docs/mortise-system-migration-plan-v2.md` - 详细迁移计划
- 📋 `docs/mortise-system-migration-checklist.md` - 检查清单
- 🔧 `docs/vscode-replace-config.json` - VS Code 配置

---

**准备好了吗？开始迁移吧！** 🚀

```powershell
# 第一步：克隆原始代码
git clone https://github.com/rymcu/mortise.git mortise-temp

# 第二步：运行迁移脚本
.\migrate-system-from-github.ps1

# 第三步：打开 VS Code 批量替换
code .
```
