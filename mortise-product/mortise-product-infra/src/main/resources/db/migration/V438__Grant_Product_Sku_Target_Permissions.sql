-- 为 SKU 目标映射管理补齐权限树与系统管理员授权

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10100000000000306, '查询 SKU 目标映射', 'product:sku-target:list', 'i-lucide-list', '', 1, 0, 2, 6, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'product:sku'
  AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku-target:list');

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10100000000000307, '查看 SKU 目标映射', 'product:sku-target:query', 'i-lucide-eye', '', 1, 0, 2, 7, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'product:sku'
  AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku-target:query');

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10100000000000308, '新增 SKU 目标映射', 'product:sku-target:add', 'i-lucide-plus', '', 1, 0, 2, 8, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'product:sku'
  AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku-target:add');

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10100000000000309, '编辑 SKU 目标映射', 'product:sku-target:edit', 'i-lucide-pencil', '', 1, 0, 2, 9, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'product:sku'
  AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku-target:edit');

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 10100000000000310, '删除 SKU 目标映射', 'product:sku-target:delete', 'i-lucide-trash-2', '', 1, 0, 2, 10, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'product:sku'
  AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'product:sku-target:delete');

INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
CROSS JOIN mortise.mortise_menu menu
WHERE role.permission = 'ADMIN'
  AND menu.permission LIKE 'product:sku-target:%'
  AND NOT EXISTS (
    SELECT 1
    FROM mortise.mortise_role_menu rm
    WHERE rm.id_mortise_role = role.id
      AND rm.id_mortise_menu = menu.id
  );
