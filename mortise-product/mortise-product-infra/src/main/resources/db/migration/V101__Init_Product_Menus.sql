-- 产品模块菜单与权限初始化
-- 菜单 ID 规则: 模块前缀(101) + 序号

-- ========== 一级目录: 产品 ==========
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10100000000000001, '产品', 'product', 'i-lucide-package-2', '/products', 0, 0, 0, 7, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product');

-- ========== 二级菜单: 产品目录 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000011, '产品目录', 'product:catalog', 'i-lucide-package-search', '/products', 0, 0, 1, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:catalog');

-- ========== 二级权限资源: 产品分类 / SKU（仅用于权限树，不展示为侧栏菜单） ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000021, '产品分类', 'product:category', 'i-lucide-tags', '', 1, 0, 1, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:category');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000031, '产品 SKU', 'product:sku', 'i-lucide-boxes', '', 1, 0, 1, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku');

-- ========== 三级按钮权限: 产品目录 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000101, '查询产品', 'product:catalog:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:catalog'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:catalog:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000102, '查看产品', 'product:catalog:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:catalog'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:catalog:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000103, '新增产品', 'product:catalog:add', 'i-lucide-plus', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:catalog'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:catalog:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000104, '编辑产品', 'product:catalog:edit', 'i-lucide-pencil', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:catalog'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:catalog:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000105, '删除产品', 'product:catalog:delete', 'i-lucide-trash-2', '', 0, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:catalog'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:catalog:delete');

-- ========== 三级按钮权限: 产品分类 ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000201, '查询分类', 'product:category:list', 'i-lucide-list-tree', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:category'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:category:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000202, '查看分类', 'product:category:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:category'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:category:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000203, '新增分类', 'product:category:add', 'i-lucide-plus', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:category'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:category:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000204, '编辑分类', 'product:category:edit', 'i-lucide-pencil', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:category'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:category:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000205, '删除分类', 'product:category:delete', 'i-lucide-trash-2', '', 0, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:category'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:category:delete');

-- ========== 三级按钮权限: 产品 SKU ==========
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000301, '查询 SKU', 'product:sku:list', 'i-lucide-list', '', 0, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:sku'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000302, '查看 SKU', 'product:sku:query', 'i-lucide-eye', '', 0, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:sku'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000303, '新增 SKU', 'product:sku:add', 'i-lucide-plus', '', 0, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:sku'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000304, '编辑 SKU', 'product:sku:edit', 'i-lucide-pencil', '', 0, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:sku'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 10100000000000305, '删除 SKU', 'product:sku:delete', 'i-lucide-trash-2', '', 0, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent WHERE parent.permission = 'product:sku'
AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku:delete');

-- ========== 为管理员角色分配产品菜单权限 ==========
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
CROSS JOIN mortise.mortise_menu menu
WHERE role.permission = 'ADMIN'
  AND menu.permission LIKE 'product%'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_role_menu rm
    WHERE rm.id_mortise_role = role.id AND rm.id_mortise_menu = menu.id
  );
