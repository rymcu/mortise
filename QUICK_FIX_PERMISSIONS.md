# ⚠️ Flyway 权限问题 - 立即修复

## 🔴 您可能遇到的错误

### 错误 1：Schema 权限
```
ERROR: permission denied for schema mortise
```

### 错误 2：数据库权限（更常见）
```
ERROR: permission denied for database postgres
Line: 11
Statement: CREATE SCHEMA IF NOT EXISTS mortise
```

## ✅ 3 步快速修复

### 第 1 步：运行修复脚本（推荐）

```powershell
.\fix-postgresql-permissions.ps1
```

脚本会提示输入 `postgres` 用户密码，然后自动修复：
- ✅ 数据库 CREATE 权限（允许创建 schema）
- ✅ Schema 所有权
- ✅ 所有必要权限

---

### 第 2 步：重启应用

```powershell
mvn spring-boot:run
```

---

### 第 3 步：完成！

Flyway 应该能够成功创建表了 ✅

---

## 🔧 如果没有 psql 客户端

使用 **DBeaver**、**pgAdmin** 或其他数据库工具，以 **postgres** 用户身份执行：

```sql
-- 授予数据库权限（关键！）
GRANT CREATE ON DATABASE postgres TO mortise;

-- 创建并设置 schema
CREATE SCHEMA IF NOT EXISTS mortise;
ALTER SCHEMA mortise OWNER TO mortise;
```

然后重启应用。

---

## 📖 详细文档

- [完整修复指南](./docs/FLYWAY_PERMISSION_FIX.md)
- [SQL 修复脚本](./docs/fix-postgresql-permissions.sql)
- [PostgreSQL 17 兼容性说明](./docs/POSTGRESQL_17_COMPATIBILITY.md)

---

**问题原因**：应用用户 `mortise` 没有权限在 schema `mortise` 中创建表。

**解决方法**：授予用户必要的权限或设置为 schema 所有者。

**PostgreSQL 版本**：✅ 已兼容 PostgreSQL 17+

**耗时**：< 1 分钟

**难度**：⭐ 简单
