-- 为已有数据库补齐社区管理菜单与管理员角色授权

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT
    85000000000000001,
    '社区',
    'community',
    'i-lucide-panels-top-left',
    '/community',
    0,
    0,
    0,
    8,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mortise.mortise_menu WHERE permission = 'community'
);

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT
    85000000000000002,
    '社区概览',
    'community:dashboard',
    'i-lucide-layout-dashboard',
    '/community',
    0,
    0,
    1,
    1,
    parent.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'community'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_menu WHERE permission = 'community:dashboard'
);

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT
    85000000000000003,
    '文章管理',
    'community:article',
    'i-lucide-file-text',
    '/community/articles',
    0,
    0,
    1,
    2,
    parent.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'community'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_menu WHERE permission = 'community:article'
);

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT
    85000000000000004,
    '专题管理',
    'community:topic',
    'i-lucide-layout-grid',
    '/community/topics',
    0,
    0,
    1,
    3,
    parent.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'community'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_menu WHERE permission = 'community:topic'
);

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT
    85000000000000005,
    '标签管理',
    'community:tag',
    'i-lucide-tags',
    '/community/tags',
    0,
    0,
    1,
    4,
    parent.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'community'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_menu WHERE permission = 'community:tag'
);

INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT
    85000000000000006,
    '合集管理',
    'community:collection',
    'i-lucide-library',
    '/community/collections',
    0,
    0,
    1,
    5,
    parent.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'community'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_menu WHERE permission = 'community:collection'
);

INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
JOIN mortise.mortise_menu menu
  ON menu.permission IN (
      'community',
      'community:dashboard',
      'community:article',
      'community:topic',
      'community:tag',
      'community:collection'
  )
WHERE role.permission = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM mortise.mortise_role_menu role_menu
    WHERE role_menu.id_mortise_role = role.id
      AND role_menu.id_mortise_menu = menu.id
  );