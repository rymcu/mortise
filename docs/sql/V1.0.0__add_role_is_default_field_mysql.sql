-- ====================================================
-- 角色表添加默认角色字段迁移脚本
-- 数据库：MySQL
-- 版本：v1.x.x
-- 创建日期：2025-10-02
-- ====================================================

-- ====================================================
-- 第一步：添加字段
-- ====================================================
ALTER TABLE mortise.mortise_role 
ADD COLUMN is_default INT DEFAULT 0 COMMENT '是否为默认角色（注册时自动分配）0-否，1-是';

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
-- 第三步：创建唯一约束（根据 MySQL 版本选择）
-- ====================================================

-- 方案 A：MySQL 8.0.13+ 使用函数索引（推荐）
-- 注意：仅 MySQL 8.0.13 及以上版本支持
-- CREATE UNIQUE INDEX idx_role_is_default 
-- ON mortise.mortise_role ((CASE WHEN is_default = 1 AND del_flag = 0 THEN 1 ELSE NULL END));

-- 方案 B：使用触发器（兼容所有 MySQL 版本）
DELIMITER $$

-- 删除已存在的触发器（如果有）
DROP TRIGGER IF EXISTS trg_role_default_insert$$
DROP TRIGGER IF EXISTS trg_role_default_update$$

-- 创建插入触发器
CREATE TRIGGER trg_role_default_insert
BEFORE INSERT ON mortise.mortise_role
FOR EACH ROW
BEGIN
    -- 如果新插入的记录是默认角色，则将其他默认角色设为非默认
    IF NEW.is_default = 1 AND NEW.del_flag = 0 THEN
        UPDATE mortise.mortise_role 
        SET is_default = 0 
        WHERE is_default = 1 
          AND del_flag = 0;
    END IF;
END$$

-- 创建更新触发器
CREATE TRIGGER trg_role_default_update
BEFORE UPDATE ON mortise.mortise_role
FOR EACH ROW
BEGIN
    -- 如果将角色更新为默认角色，则将其他默认角色设为非默认
    IF NEW.is_default = 1 
       AND NEW.del_flag = 0
       AND (OLD.is_default = 0 OR OLD.is_default IS NULL) THEN
        UPDATE mortise.mortise_role 
        SET is_default = 0 
        WHERE id != NEW.id 
          AND is_default = 1 
          AND del_flag = 0;
    END IF;
END$$

DELIMITER ;

-- ====================================================
-- 验证步骤
-- ====================================================

-- 验证 1: 检查字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_DEFAULT, IS_NULLABLE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mortise' 
  AND TABLE_NAME = 'mortise_role' 
  AND COLUMN_NAME = 'is_default';

-- 验证 2: 检查默认角色是否设置成功
SELECT id, label, permission, is_default, status, del_flag
FROM mortise.mortise_role
WHERE is_default = 1;
-- 应该返回 1 条记录

-- 验证 3: 检查触发器是否创建成功
SHOW TRIGGERS FROM mortise WHERE `Table` = 'mortise_role';

-- 验证 4: 测试触发器逻辑
-- 尝试插入第二个默认角色，检查是否自动将第一个设为非默认
/*
INSERT INTO mortise.mortise_role (label, permission, is_default, status, del_flag, created_time, updated_time)
VALUES ('测试默认角色2', 'test_default2', 1, 0, 0, NOW(), NOW());

-- 再次检查，应该只有一个默认角色
SELECT id, label, permission, is_default FROM mortise.mortise_role WHERE is_default = 1;

-- 清理测试数据
DELETE FROM mortise.mortise_role WHERE permission = 'test_default2';
*/

-- ====================================================
-- 回滚脚本（如需回滚，请执行以下语句）
-- ====================================================
/*
-- 删除触发器
DROP TRIGGER IF EXISTS trg_role_default_insert;
DROP TRIGGER IF EXISTS trg_role_default_update;

-- 删除函数索引（如果使用了方案 A）
-- DROP INDEX idx_role_is_default ON mortise.mortise_role;

-- 删除字段
ALTER TABLE mortise.mortise_role DROP COLUMN is_default;
*/

-- ====================================================
-- MySQL 8.0.13+ 方案说明
-- ====================================================
/*
如果你的 MySQL 版本 >= 8.0.13，推荐使用函数索引方案：

1. 注释掉触发器部分（方案 B）
2. 取消方案 A 的注释
3. 执行迁移

函数索引优点：
- 性能更好
- 维护更简单
- 并发安全性更高

触发器方案优点：
- 兼容性好，支持 MySQL 5.x
- 自动处理逻辑
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
   - 触发器会在每次插入/更新时执行，可能有轻微性能影响
   
3. 执行时间：
   - 预计执行时间：< 1 秒（取决于表大小）
   - 锁表时间：极短（仅 ALTER TABLE 期间）
   
4. 版本选择：
   - MySQL 8.0.13+：推荐使用函数索引（方案 A）
   - MySQL 5.x - 8.0.12：使用触发器（方案 B）
   
5. 验证清单：
   ✅ 字段添加成功
   ✅ 默认角色设置成功
   ✅ 触发器/索引创建成功
   ✅ 唯一性约束生效
*/
