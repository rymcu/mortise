INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000111, '查询 Provider 详情', 'voice:provider:query', 'i-lucide-search', '', 1, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:provider'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:provider:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000112, '新增 Provider', 'voice:provider:add', 'i-lucide-plus', '', 1, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:provider'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:provider:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000113, '编辑 Provider', 'voice:provider:edit', 'i-lucide-pencil', '', 1, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:provider'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:provider:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000114, '删除 Provider', 'voice:provider:delete', 'i-lucide-trash-2', '', 1, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:provider'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:provider:delete');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000121, '查询 Model 详情', 'voice:model:query', 'i-lucide-search', '', 1, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:model'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:model:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000122, '新增 Model', 'voice:model:add', 'i-lucide-plus', '', 1, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:model'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:model:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000123, '编辑 Model', 'voice:model:edit', 'i-lucide-pencil', '', 1, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:model'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:model:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000124, '删除 Model', 'voice:model:delete', 'i-lucide-trash-2', '', 1, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:model'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:model:delete');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000131, '查询 Profile 详情', 'voice:profile:query', 'i-lucide-search', '', 1, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:profile'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:profile:query');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000132, '新增 Profile', 'voice:profile:add', 'i-lucide-plus', '', 1, 0, 2, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:profile'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:profile:add');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000133, '编辑 Profile', 'voice:profile:edit', 'i-lucide-pencil', '', 1, 0, 2, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:profile'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:profile:edit');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41300000000000134, '删除 Profile', 'voice:profile:delete', 'i-lucide-trash-2', '', 1, 0, 2, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:profile'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:profile:delete');

INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
JOIN mortise.mortise_menu menu ON menu.permission IN (
    'voice:provider:query', 'voice:provider:add', 'voice:provider:edit', 'voice:provider:delete',
    'voice:model:query', 'voice:model:add', 'voice:model:edit', 'voice:model:delete',
    'voice:profile:query', 'voice:profile:add', 'voice:profile:edit', 'voice:profile:delete'
)
WHERE role.permission = 'ADMIN'
    AND NOT EXISTS (
        SELECT 1 FROM mortise.mortise_role_menu rm
        WHERE rm.id_mortise_role = role.id
            AND rm.id_mortise_menu = menu.id
    );