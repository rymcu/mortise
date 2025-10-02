-- =============================================
-- PostgreSQL 权限配置脚本
-- 用于解决 Flyway 执行时的权限问题
-- =============================================

-- 说明：
-- 错误信息：ERROR: permission denied for schema mortise
-- 原因：数据库用户 'mortise' 没有在 schema 'mortise' 上的操作权限
-- 解决：授予必要的权限

-- =============================================
-- 步骤 1: 以超级用户身份连接数据库
-- =============================================
-- psql -U postgres -d postgres

-- =============================================
-- 步骤 2: 授予数据库级别权限
-- =============================================

-- 授予在数据库中创建 schema 的权限（关键！）
GRANT CREATE ON DATABASE postgres TO mortise;

-- 授予连接数据库的权限
GRANT CONNECT ON DATABASE postgres TO mortise;

-- =============================================
-- 步骤 3: 创建 schema（如果不存在）
-- =============================================
CREATE SCHEMA IF NOT EXISTS mortise;

-- =============================================
-- 步骤 4: 授予用户对 schema 的所有权限
-- =============================================

-- 授予 USAGE 权限（允许访问 schema）
GRANT USAGE ON SCHEMA mortise TO mortise;

-- 授予 CREATE 权限（允许在 schema 中创建对象）
GRANT CREATE ON SCHEMA mortise TO mortise;

-- 授予 schema 所有权（推荐，最彻底的解决方案）
ALTER SCHEMA mortise OWNER TO mortise;

-- =============================================
-- 步骤 5: 授予对现有表的权限（如果有）
-- =============================================

-- 授予对所有现有表的所有权限
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA mortise TO mortise;

-- 授予对所有现有序列的所有权限
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA mortise TO mortise;

-- 授予对所有现有函数的所有权限
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA mortise TO mortise;

-- =============================================
-- 步骤 6: 设置默认权限（重要！）
-- =============================================

-- 为将来创建的表设置默认权限
ALTER DEFAULT PRIVILEGES IN SCHEMA mortise 
    GRANT ALL PRIVILEGES ON TABLES TO mortise;

-- 为将来创建的序列设置默认权限
ALTER DEFAULT PRIVILEGES IN SCHEMA mortise 
    GRANT ALL PRIVILEGES ON SEQUENCES TO mortise;

-- 为将来创建的函数设置默认权限
ALTER DEFAULT PRIVILEGES IN SCHEMA mortise 
    GRANT ALL PRIVILEGES ON FUNCTIONS TO mortise;

-- =============================================
-- 步骤 7: 验证权限（PostgreSQL 17 兼容）
-- =============================================

-- 查看 schema 的所有者
SELECT 
    nspname AS schema_name, 
    pg_catalog.pg_get_userbyid(nspowner) AS schema_owner
FROM pg_catalog.pg_namespace
WHERE nspname = 'mortise';

-- 查看 schema 的 ACL 权限（PostgreSQL 17 兼容方式）
SELECT 
    n.nspname AS schema_name,
    pg_catalog.pg_get_userbyid(n.nspowner) AS owner,
    n.nspacl AS acl_list
FROM pg_catalog.pg_namespace n
WHERE n.nspname = 'mortise';

-- 更详细的权限查询
SELECT 
    nspname AS schema_name,
    pg_catalog.pg_get_userbyid(nspowner) AS owner,
    (aclexplode(nspacl)).grantee::regrole AS grantee,
    (aclexplode(nspacl)).privilege_type AS privilege
FROM pg_catalog.pg_namespace
WHERE nspname = 'mortise';

-- =============================================
-- 完成提示
-- =============================================

-- 执行完成后，重新启动应用即可
-- Flyway 将能够成功创建表和执行迁移

-- 预期输出示例：
-- schema_name | schema_owner
-- -----------+--------------
-- mortise     | mortise

-- 或者至少应该看到 mortise 用户在 grantee 列表中
