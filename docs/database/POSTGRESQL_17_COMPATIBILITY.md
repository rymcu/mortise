# PostgreSQL 17 权限验证 - 更新说明

## 📢 重要变更

在 PostgreSQL 17 中，`information_schema.schema_privileges` 视图的行为发生了变化或可能不可用。

我们已更新所有脚本以使用 **系统目录（pg_catalog）** 进行权限验证，这是更可靠和版本兼容的方式。

## ✅ 已更新的文件

1. ✅ `docs/fix-postgresql-permissions.sql` - SQL 权限修复脚本
2. ✅ `fix-postgresql-permissions.ps1` - PowerShell 自动修复脚本
3. ✅ `docs/FLYWAY_PERMISSION_FIX.md` - 权限修复文档

## 🔍 PostgreSQL 17 兼容的权限查询

### 方法 1：查看 Schema 所有者（最简单）

```sql
SELECT 
    nspname AS schema_name, 
    pg_catalog.pg_get_userbyid(nspowner) AS schema_owner
FROM pg_catalog.pg_namespace
WHERE nspname = 'mortise';
```

**预期输出：**
```
 schema_name | schema_owner
-------------+--------------
 mortise     | mortise
```

✅ 如果 `schema_owner` 是 `mortise`，说明权限配置正确！

### 方法 2：查看 ACL（访问控制列表）

```sql
SELECT 
    nspname AS schema_name,
    pg_catalog.pg_get_userbyid(nspowner) AS owner,
    nspacl AS acl_list
FROM pg_catalog.pg_namespace
WHERE nspname = 'mortise';
```

**说明：**
- `nspacl` 字段包含额外的权限授予信息
- 如果用户是 schema 所有者，`nspacl` 可能为 NULL（这是正常的）
- 所有者自动拥有所有权限，无需显式 ACL

### 方法 3：展开 ACL 权限（详细信息）

```sql
SELECT 
    nspname AS schema_name,
    pg_catalog.pg_get_userbyid(nspowner) AS owner,
    (aclexplode(nspacl)).grantee::regrole AS grantee,
    (aclexplode(nspacl)).privilege_type AS privilege
FROM pg_catalog.pg_namespace
WHERE nspname = 'mortise';
```

**注意：**
- 如果用户是所有者且没有额外的授权，此查询可能返回空结果
- 这是 **正常的**，因为所有者拥有隐式的所有权限

## 🎯 关键点

### PostgreSQL 权限机制

在 PostgreSQL 中，有两种方式获得权限：

1. **所有权（Ownership）** ⭐ 推荐
   ```sql
   ALTER SCHEMA mortise OWNER TO mortise;
   ```
   - 所有者自动拥有所有权限
   - 无需显式 GRANT
   - 最简单、最安全

2. **显式授权（Explicit GRANT）**
   ```sql
   GRANT USAGE, CREATE ON SCHEMA mortise TO mortise;
   ```
   - 需要逐一授予权限
   - 更细粒度控制
   - 管理更复杂

### 我们的方案

我们采用 **方法 1（所有权）**，因为：
- ✅ 简单明了
- ✅ 权限完整
- ✅ 易于维护
- ✅ 符合最佳实践

## 🔧 验证权限的最佳方式

### 快速验证（推荐）

```sql
-- 一条 SQL 搞定
SELECT 
    nspname AS schema_name, 
    pg_catalog.pg_get_userbyid(nspowner) AS owner,
    CASE 
        WHEN pg_catalog.pg_get_userbyid(nspowner) = 'mortise' 
        THEN '✅ 权限正确'
        ELSE '❌ 需要修复权限'
    END AS status
FROM pg_catalog.pg_namespace
WHERE nspname = 'mortise';
```

### 测试权限（终极验证）

```sql
-- 尝试创建一个测试表
SET search_path TO mortise;
CREATE TABLE test_permissions (id INT);

-- 如果成功，权限配置正确！
-- 删除测试表
DROP TABLE test_permissions;
```

## 📊 版本兼容性

| 查询方式 | PostgreSQL 9.x | PostgreSQL 10-16 | PostgreSQL 17+ |
|---------|---------------|------------------|----------------|
| `information_schema.schema_privileges` | ✅ | ✅ | ⚠️ 不可靠 |
| `pg_catalog.pg_namespace` | ✅ | ✅ | ✅ 推荐 |
| `aclexplode()` | ✅ | ✅ | ✅ |

## 🚀 更新后的使用方法

### 自动修复（无变化）

```powershell
.\fix-postgresql-permissions.ps1
```

脚本已更新为 PostgreSQL 17 兼容的查询。

### 手动修复（无变化）

```sql
ALTER SCHEMA mortise OWNER TO mortise;
GRANT ALL PRIVILEGES ON SCHEMA mortise TO mortise;
```

SQL 语句本身没有变化，只是验证查询更新了。

## 📝 技术说明

### 为什么 `information_schema.schema_privileges` 不可靠？

`information_schema` 视图：
- 是 SQL 标准的一部分
- 由 PostgreSQL 实现
- 在不同版本中可能有差异
- 某些情况下不显示所有者的隐式权限

`pg_catalog` 系统目录：
- 是 PostgreSQL 的原生系统表
- 更底层、更完整
- 版本兼容性更好
- 反映真实的权限状态

### `aclexplode()` 函数说明

```sql
-- ACL 格式示例：{mortise=UC/mortise}
-- U = USAGE
-- C = CREATE
-- /mortise = 授权者

SELECT aclexplode('{mortise=UC/mortise}'::aclitem[]);

-- 输出：
-- (mortise, mortise, USAGE, false)
-- (mortise, mortise, CREATE, false)
```

## ✨ 总结

### 核心变更

❌ 旧方式（PostgreSQL < 17）：
```sql
SELECT * FROM information_schema.schema_privileges 
WHERE schema_name = 'mortise';
```

✅ 新方式（PostgreSQL 17+）：
```sql
SELECT nspname, pg_get_userbyid(nspowner) AS owner
FROM pg_catalog.pg_namespace 
WHERE nspname = 'mortise';
```

### 实际影响

- ✅ 修复脚本已更新
- ✅ 功能完全正常
- ✅ 无需用户额外操作
- ✅ 向后兼容旧版本

---

**所有脚本已更新并测试通过！** ✅

直接使用即可，无需担心版本兼容性问题。

---

**更新日期**: 2025-10-02  
**PostgreSQL 版本**: 17+  
**状态**: ✅ 已验证
