-- ====================================================
-- 角色表添加默认角色字段迁移脚本
-- 数据库：PostgreSQL
-- 版本：v1.x.x
-- 创建日期：2025-10-02
-- ====================================================

-- ====================================================
-- 第一步：添加字段
-- ====================================================
ALTER TABLE mortise.mortise_role 
ADD COLUMN is_default INTEGER DEFAULT 0;

COMMENT ON COLUMN mortise.mortise_role.is_default IS '是否为默认角色（注册时自动分配）0-否，1-是';

-- ====================================================
-- 第二步：设置现有的 user 角色为默认角色
-- 注意：使用 DefaultFlag.YES.getValue() = 1
-- ====================================================
UPDATE mortise.mortise_role 
SET is_default = 1  -- DefaultFlag.YES
WHERE permission = 'user' 
  AND del_flag = 0
  AND status = 0;

-- ====================================================
-- 第三步：创建部分唯一索引（强烈推荐）
-- 确保系统中只有一个默认角色
-- 注意：使用 DefaultFlag.YES.getValue() = 1
-- ====================================================
CREATE UNIQUE INDEX idx_role_is_default 
ON mortise.mortise_role (is_default) 
WHERE is_default = 1 AND del_flag = 0;  -- DefaultFlag.YES

-- ====================================================
-- 验证步骤
-- ====================================================

-- 验证 1: 检查字段是否添加成功
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_schema = 'mortise' 
  AND table_name = 'mortise_role' 
  AND column_name = 'is_default';

-- 验证 2: 检查默认角色是否设置成功
SELECT id, label, permission, is_default, status, del_flag
FROM mortise.mortise_role
WHERE is_default = 1;
-- 应该返回 1 条记录

-- 验证 3: 检查索引是否创建成功
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE schemaname = 'mortise'
  AND tablename = 'mortise_role' 
  AND indexname = 'idx_role_is_default';

-- 验证 4: 测试唯一约束（此步骤应该失败）
-- 如果成功说明索引未生效，需要检查索引创建
-- 注释掉此测试，仅用于手动验证
/*
INSERT INTO mortise.mortise_role (label, permission, is_default, status, del_flag, created_time, updated_time)
VALUES ('测试重复默认角色', 'test_duplicate', 1, 0, 0, NOW(), NOW());
-- 预期结果：ERROR: duplicate key value violates unique constraint "idx_role_is_default"
*/

-- ====================================================
-- 回滚脚本（如需回滚，请执行以下语句）
-- ====================================================
/*
-- 删除索引
DROP INDEX IF EXISTS mortise.idx_role_is_default;

-- 删除字段
ALTER TABLE mortise.mortise_role DROP COLUMN IF EXISTS is_default;
*/

-- ====================================================
-- 执行说明
-- ====================================================
/*
1. 执行顺序：
   - 在生产环境执行前，请先在测试环境验证
   - 先执行迁移脚本，再部署新代码
   
2. 注意事项：
   - 如果数据库中不存在 permission = 'user' 的角色，需要先创建或手动设置默认角色
   - 执行前请备份数据库
   - 建议在维护窗口执行，避免影响线上用户
   
3. 执行时间：
   - 预计执行时间：< 1 秒（取决于表大小）
   - 锁表时间：极短（仅 ALTER TABLE 期间）
   
4. 验证清单：
   ✅ 字段添加成功
   ✅ 默认角色设置成功
   ✅ 唯一索引创建成功
   ✅ 唯一约束生效
*/
