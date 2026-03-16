-- 系统模块菜单与权限初始化
-- 菜单 ID 规则: 模块前缀(10) + 序号

-- ========== 一级菜单: 首页 ==========
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10000000000000001, '首页', 'home', 'i-lucide-house', '/', 0, 0, 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'home');

-- ========== 一级菜单: 收件箱 ==========
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10000000000000002, '收件箱', 'inbox', 'i-lucide-inbox', '/inbox', 0, 0, 1, 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'inbox');

-- ========== 一级目录: 系统 ==========
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10000000000000003, '系统', 'system', 'i-lucide-settings-2', '/systems', 0, 0, 0, 10, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system');

-- ========== 一级目录: 设置 ==========
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10000000000000004, '设置', 'settings', 'i-lucide-settings', '/settings', 0, 0, 0, 1000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'settings');

-- ========== 一级菜单: 监控 ==========
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10000000000000005, '监控', 'monitor', 'i-lucide-monitor-dot', '/monitor', 0, 0, 1, 11, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'monitor');

-- ========== 二级菜单: 系统监控 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000051, '系统监控', 'monitor:system', 'i-lucide-cpu', '/monitor', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'monitor'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'monitor:system');

-- ========== 二级菜单: 设置子菜单 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000101, '资料', 'settings:profile', 'i-lucide-user', '/settings', 0, 0, 1, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'settings'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'settings:profile');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000102, '消息', 'settings:notifications', 'i-lucide-bell-ring', '/settings/notifications', 0, 0, 1, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'settings'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'settings:notifications');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000103, '安全', 'settings:security', 'i-lucide-shield', '/settings/security', 0, 0, 1, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'settings'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'settings:security');

-- ========== 二级菜单: 系统管理 ==========
-- 用户管理
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000201, '用户', 'system:user', 'i-lucide-users', '/systems/users', 0, 0, 1, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:user');

-- 角色管理
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000202, '角色', 'system:role', 'i-lucide-shield-user', '/systems/roles', 0, 0, 1, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:role');

-- 菜单管理
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000203, '菜单', 'system:menu', 'i-lucide-menu', '/systems/menus', 0, 0, 1, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:menu');

-- 字典类型管理
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000204, '字典类型', 'system:dict-type', 'i-lucide-book-type', '/systems/dict-types', 0, 0, 1, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict-type');

-- 字典管理
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000205, '字典', 'system:dict', 'i-lucide-book', '/systems/dictionaries', 0, 0, 1, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict');

-- OAuth2 客户端管理
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000206, 'OAuth2 客户端', 'system:oauth2-client', 'i-lucide-key-round', '/systems/oauth2-clients', 0, 0, 1, 6, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:oauth2-client');

-- 通知渠道配置
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000207, '通知渠道配置', 'system:notification-channel', 'i-lucide-bell', '/systems/notification-channels', 0, 0, 1, 7, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:notification-channel');

-- 操作日志
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000208, '操作日志', 'system:operation-log', 'i-lucide-scroll-text', '/systems/operation-logs', 0, 0, 1, 8, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:operation-log');

-- API 日志
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000209, 'API 日志', 'system:api-log', 'i-lucide-activity', '/systems/api-logs', 0, 0, 1, 9, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:api-log');

-- 缓存管理
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000210, '缓存管理', 'system:cache', 'i-lucide-database-zap', '/systems/cache', 0, 0, 1, 10, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:cache');

-- 文件管理
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000211, '文件管理', 'system:file', 'i-lucide-folder-open', '/systems/files', 0, 0, 1, 11, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:file');

-- 网站配置
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000299, '网站配置', 'system:website-config', 'i-lucide-globe', '/systems/website-config', 0, 0, 1, 999, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:website-config');

-- ========== 三级按钮权限: 用户管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000301, '查询用户', 'system:user:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:user'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:user:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000302, '查看用户', 'system:user:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:user'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:user:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000303, '新增用户', 'system:user:add', 'i-lucide-user-plus', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:user'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:user:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000304, '编辑用户', 'system:user:edit', 'i-lucide-user-cog', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:user'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:user:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000305, '删除用户', 'system:user:delete', 'i-lucide-user-minus', '', 0, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:user'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:user:delete');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000306, '重置密码', 'system:user:reset-password', 'i-lucide-key-round', '', 0, 0, 2, 6, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:user'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:user:reset-password');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000307, '分配角色', 'system:user:assign-role', 'i-lucide-shield-check', '', 0, 0, 2, 7, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:user'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:user:assign-role');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000308, '导出用户', 'system:user:export', 'i-lucide-download', '', 0, 0, 2, 8, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:user'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:user:export');

-- ========== 三级按钮权限: 角色管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000401, '查询角色', 'system:role:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:role'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:role:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000402, '查看角色', 'system:role:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:role'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:role:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000403, '新增角色', 'system:role:add', 'i-lucide-plus', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:role'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:role:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000404, '编辑角色', 'system:role:edit', 'i-lucide-pencil', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:role'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:role:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000405, '删除角色', 'system:role:delete', 'i-lucide-trash-2', '', 0, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:role'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:role:delete');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000406, '分配权限', 'system:role:assign', 'i-lucide-shield-check', '', 0, 0, 2, 6, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:role'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:role:assign');

-- ========== 三级按钮权限: 菜单管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000501, '查询菜单', 'system:menu:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:menu'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:menu:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000502, '查看菜单', 'system:menu:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:menu'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:menu:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000503, '新增菜单', 'system:menu:add', 'i-lucide-plus', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:menu'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:menu:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000504, '编辑菜单', 'system:menu:edit', 'i-lucide-pencil', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:menu'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:menu:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000505, '删除菜单', 'system:menu:delete', 'i-lucide-trash-2', '', 0, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:menu'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:menu:delete');

-- ========== 三级按钮权限: 字典类型管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000601, '查询字典类型', 'system:dict-type:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict-type'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict-type:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000602, '查看字典类型', 'system:dict-type:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict-type'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict-type:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000603, '新增字典类型', 'system:dict-type:add', 'i-lucide-plus', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict-type'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict-type:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000604, '编辑字典类型', 'system:dict-type:edit', 'i-lucide-pencil', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict-type'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict-type:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000605, '删除字典类型', 'system:dict-type:delete', 'i-lucide-trash-2', '', 0, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict-type'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict-type:delete');

-- ========== 三级按钮权限: 字典管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000701, '查询字典', 'system:dict:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000702, '查看字典', 'system:dict:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000703, '新增字典', 'system:dict:add', 'i-lucide-plus', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000704, '编辑字典', 'system:dict:edit', 'i-lucide-pencil', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000705, '删除字典', 'system:dict:delete', 'i-lucide-trash-2', '', 0, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:dict'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:dict:delete');

-- ========== 三级按钮权限: OAuth2 客户端管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000801, '查询客户端', 'system:oauth2-client:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:oauth2-client'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:oauth2-client:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000802, '查看客户端', 'system:oauth2-client:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:oauth2-client'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:oauth2-client:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000803, '新增客户端', 'system:oauth2-client:add', 'i-lucide-plus', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:oauth2-client'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:oauth2-client:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000804, '编辑客户端', 'system:oauth2-client:edit', 'i-lucide-pencil', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:oauth2-client'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:oauth2-client:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000805, '删除客户端', 'system:oauth2-client:delete', 'i-lucide-trash-2', '', 0, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:oauth2-client'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:oauth2-client:delete');

-- ========== 三级按钮权限: 通知渠道配置 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000901, '查询通知渠道', 'system:notification-channel:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:notification-channel'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:notification-channel:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000902, '查看通知渠道', 'system:notification-channel:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:notification-channel'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:notification-channel:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000000903, '编辑通知渠道', 'system:notification-channel:edit', 'i-lucide-pencil', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:notification-channel'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:notification-channel:edit');

-- ========== 三级按钮权限: 操作日志 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001001, '查询操作日志', 'system:operation-log:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:operation-log'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:operation-log:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001002, '查看操作日志', 'system:operation-log:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:operation-log'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:operation-log:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001003, '删除操作日志', 'system:operation-log:delete', 'i-lucide-trash-2', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:operation-log'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:operation-log:delete');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001004, '清空操作日志', 'system:operation-log:clear', 'i-lucide-trash', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:operation-log'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:operation-log:clear');

-- ========== 三级按钮权限: API 日志 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001101, '查询 API 日志', 'system:api-log:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:api-log'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:api-log:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001102, '查看 API 日志', 'system:api-log:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:api-log'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:api-log:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001103, '删除 API 日志', 'system:api-log:delete', 'i-lucide-trash-2', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:api-log'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:api-log:delete');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001104, '清空 API 日志', 'system:api-log:clear', 'i-lucide-trash', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:api-log'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:api-log:clear');

-- ========== 三级按钮权限: 缓存管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001201, '查询缓存', 'system:cache:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:cache'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:cache:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001202, '查看缓存', 'system:cache:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:cache'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:cache:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001203, '删除缓存', 'system:cache:delete', 'i-lucide-trash-2', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:cache'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:cache:delete');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001204, '清空缓存', 'system:cache:clear', 'i-lucide-trash', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:cache'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:cache:clear');

-- ========== 三级按钮权限: 文件管理 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001301, '查询文件', 'system:file:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:file'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:file:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001302, '上传文件', 'system:file:upload', 'i-lucide-upload', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:file'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:file:upload');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001303, '下载文件', 'system:file:download', 'i-lucide-download', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:file'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:file:download');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001304, '删除文件', 'system:file:delete', 'i-lucide-trash-2', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:file'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:file:delete');

-- ========== 三级按钮权限: 网站配置 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001401, '查询网站配置', 'system:website-config:query', 'i-lucide-eye', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:website-config'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:website-config:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10000000000001402, '编辑网站配置', 'system:website-config:edit', 'i-lucide-pencil', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'system:website-config'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'system:website-config:edit');

-- ========== 为管理员角色分配系统菜单权限 ==========
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
CROSS JOIN mortise.mortise_menu menu
WHERE role.permission = 'ADMIN'
  AND (menu.permission LIKE 'system%' OR menu.permission IN ('home', 'inbox', 'monitor', 'settings'))
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_role_menu rm
    WHERE rm.id_mortise_role = role.id AND rm.id_mortise_menu = menu.id
  );
