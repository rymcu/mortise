# Flyway 权限问题快速修复指南

## 🔴 错误信息

```
Caused by: org.springframework.beans.factory.BeanCreationException: 
Error creating bean with name 'flywayInitializer'

SQL State  : 42501
Error Code : 0
Message    : ERROR: permission denied for schema mortise
```

## 🎯 问题原因

PostgreSQL 数据库用户 `mortise` 没有在 schema `mortise` 上的操作权限。

Flyway 尝试在 schema 中创建表时被拒绝。

## ✅ 解决方案

### 方案1：使用自动修复脚本（推荐）

```powershell
# 运行权限修复脚本
.\fix-postgresql-permissions.ps1

# 脚本会提示输入 postgres 超级用户密码
# 然后自动授予必要的权限
```

### 方案2：手动执行 SQL（如果没有 psql 客户端）

1. **使用 DBeaver、pgAdmin 或其他数据库工具**连接到数据库

2. **以超级用户身份**（如 `postgres`）执行以下 SQL：

```sql
-- 授予数据库级别权限（关键！）
GRANT CREATE ON DATABASE postgres TO mortise;
GRANT CONNECT ON DATABASE postgres TO mortise;

-- 创建 schema（如果不存在）
CREATE SCHEMA IF NOT EXISTS mortise;

-- 设置 schema 所有者为应用用户
ALTER SCHEMA mortise OWNER TO mortise;

-- 授予基本权限
GRANT USAGE ON SCHEMA mortise TO mortise;
GRANT CREATE ON SCHEMA mortise TO mortise;

-- 授予对现有对象的权限（如果有）
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA mortise TO mortise;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA mortise TO mortise;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA mortise TO mortise;

-- 设置默认权限（重要！）
ALTER DEFAULT PRIVILEGES IN SCHEMA mortise 
    GRANT ALL PRIVILEGES ON TABLES TO mortise;
    
ALTER DEFAULT PRIVILEGES IN SCHEMA mortise 
    GRANT ALL PRIVILEGES ON SEQUENCES TO mortise;
    
ALTER DEFAULT PRIVILEGES IN SCHEMA mortise 
    GRANT ALL PRIVILEGES ON FUNCTIONS TO mortise;
```

### 方案3：使用 SQL 文件

```powershell
# 如果已安装 psql 客户端
psql -h 192.168.21.238 -p 5432 -U postgres -d postgres -f docs\fix-postgresql-permissions.sql
```

## 🔍 验证权限

执行以下 SQL 验证权限是否正确（PostgreSQL 17 兼容）：

```sql
-- 检查 schema 所有者
SELECT 
    nspname AS schema_name, 
    pg_catalog.pg_get_userbyid(nspowner) AS schema_owner
FROM pg_catalog.pg_namespace
WHERE nspname = 'mortise';

-- 预期结果：
-- schema_name | schema_owner
-- -----------+--------------
-- mortise     | mortise
```

```sql
-- 检查 schema 权限列表（ACL）
SELECT 
    nspname AS schema_name,
    pg_catalog.pg_get_userbyid(nspowner) AS owner,
    nspacl AS acl_list
FROM pg_catalog.pg_namespace
WHERE nspname = 'mortise';

-- 如果 schema_owner 是 mortise，说明权限配置正确
-- nspacl 列会显示额外的权限授予信息
```

```sql
-- 更详细的权限展开（PostgreSQL 17）
SELECT 
    nspname AS schema_name,
    (aclexplode(nspacl)).grantee::regrole AS grantee,
    (aclexplode(nspacl)).privilege_type AS privilege
FROM pg_catalog.pg_namespace
WHERE nspname = 'mortise';

-- 如果设置了所有者，这个查询可能返回空结果（正常）
-- 因为所有者自动拥有所有权限，不需要显式 ACL
```

## 🚀 重新启动应用

权限修复后，重新启动应用：

```powershell
mvn spring-boot:run
```

Flyway 应该能够成功执行迁移！

## 📋 预期成功日志

```
Flyway Community Edition 10.x.x by Redgate

Database: jdbc:postgresql://192.168.21.238:5432/postgres (PostgreSQL 16.x)
Successfully validated 1 migration (execution time 00:00.023s)
Creating Schema History table "mortise"."flyway_schema_history" ...
Current version of schema "mortise": << Empty Schema >>
Migrating schema "mortise" to version "1 - Create System Tables"
Successfully applied 1 migration to schema "mortise", now at version v1 (execution time 00:00.156s)
```

## 🛠️ 常见问题

### Q1: 为什么会出现这个权限问题？

**A:** 可能的原因：
1. Schema 是由超级用户创建的，应用用户没有权限
2. Schema 所有者不是应用用户
3. 首次创建 schema 时没有授予正确的权限

### Q2: 是否需要给应用用户超级用户权限？

**A:** 不需要！**不建议**给应用用户超级用户权限。只需要：
- Schema 的所有者权限，或
- USAGE + CREATE 权限

### Q3: 如何避免这个问题？

**A:** 最佳实践：
1. **由应用用户创建 schema**：
   ```sql
   CREATE SCHEMA mortise AUTHORIZATION mortise;
   ```

2. **或者创建后立即授权**：
   ```sql
   CREATE SCHEMA mortise;
   ALTER SCHEMA mortise OWNER TO mortise;
   ```

### Q4: 生产环境如何处理？

**A:** 生产环境建议：
1. 使用数据库迁移脚本管理权限
2. 在部署前由 DBA 预先创建 schema 并授权
3. 应用用户只需要必要的权限，不要超级用户权限
4. 使用最小权限原则

## 📝 权限说明

### 必需的权限

| 权限 | 说明 | 是否必需 |
|-----|------|---------|
| USAGE | 访问 schema | ✅ 必需 |
| CREATE | 在 schema 中创建对象 | ✅ 必需 |
| ALL ON TABLES | 表的所有操作 | ✅ 必需 |
| ALL ON SEQUENCES | 序列的所有操作 | ✅ 必需（如果使用序列） |
| ALL ON FUNCTIONS | 函数的所有操作 | ⚠️ 可选（如果使用函数） |

### 推荐的权限配置

**最安全的方式**：设置应用用户为 schema 所有者
```sql
ALTER SCHEMA mortise OWNER TO mortise;
```

**优点**：
- 自动拥有所有权限
- 创建的对象自动属于该用户
- 不需要额外的 GRANT 语句

## 📚 相关文档

- [PostgreSQL 权限管理官方文档](https://www.postgresql.org/docs/current/ddl-priv.html)
- [Flyway 数据库权限要求](https://documentation.red-gate.com/fd/database-permissions-138346987.html)

## ✨ 总结

1. ✅ 运行 `.\fix-postgresql-permissions.ps1` 自动修复
2. ✅ 或手动执行 SQL 授予权限
3. ✅ 验证 schema 所有者为 `mortise`
4. ✅ 重启应用

**问题将完全解决！** 🎉

---

**最后更新**: 2025-10-02  
**状态**: 测试通过 ✅
