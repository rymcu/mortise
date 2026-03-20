-- 微信模块菜单与权限初始化
-- 菜单 ID 规则: 模块前缀(61) + 序号

-- ========== 一级目录: 微信 ==========
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 61000000000000001, '微信', 'wechat', 'i-lucide-message-circle', '/wechat', 1, 0, 0, 9, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'wechat');

-- ========== 二级菜单: 微信账户 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 61000000000000011, '微信账户', 'wechat:account', 'i-lucide-contact', '/systems/wechat-accounts', 1, 0, 1, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'wechat'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'wechat:account');

-- ========== 三级按钮权限: 微信账户 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 61000000000000101, '查询微信账户', 'wechat:account:list', 'i-lucide-list', '', 1, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'wechat:account'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'wechat:account:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 61000000000000102, '查看微信账户', 'wechat:account:query', 'i-lucide-eye', '', 1, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'wechat:account'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'wechat:account:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 61000000000000103, '新增账户', 'wechat:account:add', 'i-lucide-plus', '', 1, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'wechat:account'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'wechat:account:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 61000000000000104, '编辑账户', 'wechat:account:edit', 'i-lucide-pencil', '', 1, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'wechat:account'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'wechat:account:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 61000000000000105, '删除账户', 'wechat:account:delete', 'i-lucide-trash-2', '', 1, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'wechat:account'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'wechat:account:delete');

-- ========== 为管理员角色分配微信菜单权限 ==========
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
CROSS JOIN mortise.mortise_menu menu
WHERE role.permission = 'ADMIN'
  AND menu.permission LIKE 'wechat%'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_role_menu rm
    WHERE rm.id_mortise_role = role.id AND rm.id_mortise_menu = menu.id
  );
