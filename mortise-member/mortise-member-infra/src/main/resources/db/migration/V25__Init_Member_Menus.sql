-- 会员模块菜单与权限初始化
-- 菜单 ID 规则: 模块前缀(25) + 序号

-- ========== 一级菜单: 会员 ==========
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 25000000000000001, '会员', 'member', 'i-lucide-users', '/members', 1, 0, 1, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'member');

-- ========== 二级按钮权限 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 25000000000000011, '查询会员', 'member:list', 'i-lucide-list', '', 1, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'member'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'member:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 25000000000000012, '查看会员', 'member:query', 'i-lucide-eye', '', 1, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'member'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'member:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 25000000000000013, '编辑会员', 'member:edit', 'i-lucide-pencil', '', 1, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'member'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'member:edit');

-- ========== 为管理员角色分配会员菜单权限 ==========
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
CROSS JOIN mortise.mortise_menu menu
WHERE role.permission = 'ADMIN'
  AND menu.permission LIKE 'member%'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_role_menu rm
    WHERE rm.id_mortise_role = role.id AND rm.id_mortise_menu = menu.id
  );
