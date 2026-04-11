-- 系统角色种子数据
-- ID 规则: 10000000000002xxx（紧接菜单序列 10000000000001xxx）
-- 幂等：使用 WHERE NOT EXISTS 避免重复插入

INSERT INTO mortise.mortise_role (id, label, permission, status, is_default, del_flag, created_time, updated_time)
SELECT 10000000000002001, '超级管理员', 'ADMIN', 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_role WHERE permission = 'ADMIN');

INSERT INTO mortise.mortise_role (id, label, permission, status, is_default, del_flag, created_time, updated_time)
SELECT 10000000000002002, '普通用户', 'USER', 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_role WHERE permission = 'USER');
