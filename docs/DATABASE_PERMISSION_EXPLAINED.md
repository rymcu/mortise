# Flyway 权限问题详解：数据库级别 vs Schema 级别

## 🔴 最新错误分析

### 错误信息
```
ERROR: permission denied for database postgres
Location: V1__Create_System_Tables.sql
Line: 11
Statement: CREATE SCHEMA IF NOT EXISTS mortise;
```

### 问题根源

这是一个 **两层权限问题**：

1. ❌ **第一层：数据库级别** - 用户没有在数据库上创建 schema 的权限
2. ❌ **第二层：Schema 级别** - 用户没有在 schema 中创建对象的权限

Flyway 脚本在第 11 行尝试 `CREATE SCHEMA`，但用户 `mortise` 没有数据库的 `CREATE` 权限。

## 🎯 PostgreSQL 权限层级

```
Database (postgres)
  ↓ 需要 CREATE 权限才能创建 Schema
  ├─ Schema (mortise)
  │   ↓ 需要 CREATE 权限才能创建表
  │   ├─ Table (mortise_user)
  │   ├─ Table (mortise_role)
  │   └─ ...
  └─ Schema (public)
```

### 权限要求

| 操作 | 需要的权限 | 级别 |
|------|-----------|------|
| 连接数据库 | `CONNECT` | Database |
| **创建 Schema** | **`CREATE`** | **Database** ⚠️ |
| 访问 Schema | `USAGE` | Schema |
| 在 Schema 中创建表 | `CREATE` | Schema |

## ✅ 完整的权限配置

### 方案 1：授予必要权限（推荐）

```sql
-- 1. 数据库级别权限（关键！）
GRANT CREATE ON DATABASE postgres TO mortise;
GRANT CONNECT ON DATABASE postgres TO mortise;

-- 2. 创建 schema
CREATE SCHEMA IF NOT EXISTS mortise;

-- 3. Schema 级别权限
ALTER SCHEMA mortise OWNER TO mortise;
GRANT ALL PRIVILEGES ON SCHEMA mortise TO mortise;
```

### 方案 2：超级用户预创建（生产环境推荐）

```sql
-- 由 DBA 以超级用户身份预先创建
CREATE SCHEMA mortise AUTHORIZATION mortise;

-- 应用用户只需要 CONNECT 权限
GRANT CONNECT ON DATABASE postgres TO mortise;
```

**优点：**
- 应用用户不需要 DATABASE CREATE 权限（更安全）
- Schema 已存在，Flyway 只需创建表
- 符合生产环境最小权限原则

## 🔧 快速修复

### 自动修复（已更新）

```powershell
# 运行修复脚本（已包含数据库权限）
.\fix-postgresql-permissions.ps1
```

脚本会自动执行：
1. ✅ `GRANT CREATE ON DATABASE postgres TO mortise;`
2. ✅ `CREATE SCHEMA IF NOT EXISTS mortise;`
3. ✅ `ALTER SCHEMA mortise OWNER TO mortise;`
4. ✅ 其他必要权限

### 手动修复

**最简单的方式（一条命令）：**

```sql
-- 以 postgres 超级用户身份执行
GRANT CREATE ON DATABASE postgres TO mortise;
```

然后重启应用，Flyway 就能成功创建 schema 和表了。

**完整方式：**

```sql
-- 以 postgres 超级用户身份执行
GRANT CREATE ON DATABASE postgres TO mortise;
GRANT CONNECT ON DATABASE postgres TO mortise;

CREATE SCHEMA IF NOT EXISTS mortise;
ALTER SCHEMA mortise OWNER TO mortise;
```

## 📊 权限检查清单

### 检查数据库权限

```sql
-- PostgreSQL 17 兼容
SELECT 
    datname AS database_name,
    (aclexplode(datacl)).grantee::regrole AS grantee,
    (aclexplode(datacl)).privilege_type AS privilege
FROM pg_database
WHERE datname = 'postgres' 
  AND (aclexplode(datacl)).grantee::regrole::text = 'mortise';
```

**预期输出应包含：**
- `CONNECT`
- `CREATE` ⚠️ **关键权限**

### 检查 Schema 权限

```sql
SELECT 
    nspname AS schema_name,
    pg_get_userbyid(nspowner) AS owner
FROM pg_namespace
WHERE nspname = 'mortise';
```

**预期输出：**
```
 schema_name | owner
-------------+--------
 mortise     | mortise
```

## 🎓 最佳实践

### 开发环境

```sql
-- 简单粗暴：给应用用户足够的权限
GRANT CREATE ON DATABASE postgres TO mortise;
CREATE SCHEMA mortise AUTHORIZATION mortise;
```

### 生产环境

```sql
-- 最小权限原则
-- 1. DBA 预先创建 schema
CREATE SCHEMA mortise AUTHORIZATION mortise;

-- 2. 应用用户只需要连接权限
GRANT CONNECT ON DATABASE postgres TO mortise;

-- 3. Schema 所有者自动拥有所有 schema 权限
-- 无需额外 GRANT
```

## 🔍 为什么会出现这个问题？

### 常见原因

1. **数据库是空的**
   - Flyway 脚本需要创建 schema
   - 但用户没有 DATABASE CREATE 权限

2. **Schema 不存在**
   - 应用启动时尝试创建
   - 失败：permission denied for database

3. **权限配置不完整**
   - 只配置了 schema 权限
   - 忘记了数据库权限

### 解决思路

**选项 A：给予权限** ⭐ 开发环境推荐
```sql
GRANT CREATE ON DATABASE postgres TO mortise;
```

**选项 B：预创建** ⭐ 生产环境推荐
```sql
CREATE SCHEMA mortise AUTHORIZATION mortise;
```

## 📝 Flyway 配置优化建议

### 当前配置（需要 DATABASE CREATE 权限）

```yaml
spring:
  flyway:
    schemas: mortise
    # Flyway 会尝试创建 schema
```

SQL 脚本：
```sql
CREATE SCHEMA IF NOT EXISTS mortise;  -- 需要 DATABASE CREATE 权限
```

### 优化配置（不需要 DATABASE CREATE 权限）

**方式 1：修改 SQL 脚本**

```sql
-- 移除 CREATE SCHEMA 语句
-- 假设 schema 已由 DBA 创建

-- 直接创建表
CREATE TABLE IF NOT EXISTS mortise.mortise_user (...);
```

**方式 2：使用 Flyway 的 createSchemas 配置**

```yaml
spring:
  flyway:
    schemas: mortise
    create-schemas: false  # 不尝试创建 schema
```

然后由 DBA 预先创建：
```sql
CREATE SCHEMA mortise AUTHORIZATION mortise;
```

## ⚠️ 安全警告

### DATABASE CREATE 权限的影响

授予 `CREATE ON DATABASE` 意味着用户可以：
- ✅ 创建 schema
- ⚠️ 创建任意名称的 schema
- ⚠️ 在数据库中创建其他对象

### 生产环境建议

**不要在生产环境授予 DATABASE CREATE 权限！**

应该：
1. 由 DBA 预先创建 schema
2. 应用只需要 schema 级别权限
3. 遵循最小权限原则

```sql
-- ❌ 不推荐（生产环境）
GRANT CREATE ON DATABASE postgres TO mortise;

-- ✅ 推荐（生产环境）
CREATE SCHEMA mortise AUTHORIZATION mortise;
GRANT CONNECT ON DATABASE postgres TO mortise;
```

## 🚀 现在就修复

### 最快的方式（10 秒）

```powershell
# 运行这个脚本
.\fix-postgresql-permissions.ps1
```

### 手动方式（30 秒）

打开你的数据库工具，以 `postgres` 用户执行：

```sql
GRANT CREATE ON DATABASE postgres TO mortise;
```

重启应用，完成！

## 📚 相关文档

- [PostgreSQL 数据库权限文档](https://www.postgresql.org/docs/current/ddl-priv.html)
- [GRANT 命令参考](https://www.postgresql.org/docs/current/sql-grant.html)
- [Flyway 权限要求](https://documentation.red-gate.com/fd/database-permissions-138346987.html)

## ✨ 总结

### 问题
用户缺少 **DATABASE CREATE** 权限，无法创建 schema。

### 解决方案
```sql
GRANT CREATE ON DATABASE postgres TO mortise;
```

### 已更新
所有修复脚本已更新，包含数据库级别权限配置。

### 状态
✅ 问题已识别  
✅ 修复脚本已更新  
✅ 文档已完善  
🎯 **直接运行修复脚本即可！**

---

**更新日期**: 2025-10-02  
**问题**: permission denied for database postgres  
**解决**: 授予 DATABASE CREATE 权限  
**影响**: 所有用户  
**优先级**: 🔴 高
