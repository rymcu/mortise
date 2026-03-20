-- AI Agent 模块菜单与权限初始化
-- 菜单 ID 规则: 模块前缀(300) + 序号

-- ========== 一级菜单: AI 配置 ==========
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 300000000000000001, 'AI 配置', 'agent', 'i-lucide-bot', '/agent', 1, 0, 1, 8, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent');

-- ========== 二级菜单: AI 提供商管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000011, 'AI 提供商', 'agent:provider', 'i-lucide-server', '/agent/providers', 1, 0, 1, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:provider');

-- ========== 三级按钮权限: 提供商 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000021, '查询提供商', 'agent:provider:list', 'i-lucide-list', '', 1, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:provider'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:provider:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000022, '查看提供商', 'agent:provider:query', 'i-lucide-eye', '', 1, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:provider'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:provider:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000023, '新增提供商', 'agent:provider:add', 'i-lucide-plus', '', 1, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:provider'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:provider:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000024, '编辑提供商', 'agent:provider:edit', 'i-lucide-pencil', '', 1, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:provider'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:provider:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000025, '删除提供商', 'agent:provider:delete', 'i-lucide-trash', '', 1, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:provider'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:provider:delete');

-- ========== 二级菜单: AI 模型管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000012, 'AI 模型', 'agent:model', 'i-lucide-brain', '/agent/models', 1, 0, 1, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:model');

-- ========== 三级按钮权限: 模型 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000031, '查询模型', 'agent:model:list', 'i-lucide-list', '', 1, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:model'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:model:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000032, '查看模型', 'agent:model:query', 'i-lucide-eye', '', 1, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:model'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:model:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000033, '新增模型', 'agent:model:add', 'i-lucide-plus', '', 1, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:model'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:model:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000034, '编辑模型', 'agent:model:edit', 'i-lucide-pencil', '', 1, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:model'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:model:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 300000000000000035, '删除模型', 'agent:model:delete', 'i-lucide-trash', '', 1, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'agent:model'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'agent:model:delete');

-- ========== 为管理员角色分配 Agent 菜单权限 ==========
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
CROSS JOIN mortise.mortise_menu menu
WHERE role.permission = 'ADMIN'
  AND menu.permission LIKE 'agent%'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_role_menu rm
    WHERE rm.id_mortise_role = role.id AND rm.id_mortise_menu = menu.id
  );
