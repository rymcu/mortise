-- 为微信消息发送接口补齐独立按钮/API 权限
INSERT INTO mortise.mortise_menu (
    id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time
)
SELECT 61000000000000106, '发送微信消息', 'wechat:message:send', 'i-lucide-send', '', 1, 0, 2, 6, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'wechat:account'
  AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'wechat:message:send');

INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
CROSS JOIN mortise.mortise_menu menu
WHERE role.permission = 'ADMIN'
  AND menu.permission = 'wechat:message:send'
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_role_menu rm
    WHERE rm.id_mortise_role = role.id AND rm.id_mortise_menu = menu.id
  );
