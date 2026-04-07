INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41400000000000015, '任务记录', 'voice:job', 'i-lucide-history', '/voices/jobs', 1, 0, 1, 5, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:job');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41400000000000151, '查询任务列表', 'voice:job:list', 'i-lucide-list', '', 1, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:job'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:job:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41400000000000152, '查询任务详情', 'voice:job:query', 'i-lucide-search', '', 1, 0, 2, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:job'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:job:query');

INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
JOIN mortise.mortise_menu menu ON menu.permission IN (
    'voice:job',
    'voice:job:list',
    'voice:job:query'
)
WHERE role.permission = 'ADMIN'
    AND NOT EXISTS (
        SELECT 1 FROM mortise.mortise_role_menu rm
        WHERE rm.id_mortise_role = role.id
            AND rm.id_mortise_menu = menu.id
    );