-- ADMIN 角色与所有菜单的关联初始化
-- 幂等：NOT EXISTS 子查询防止重复插入

INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT r.id, m.id
FROM mortise.mortise_role r
CROSS JOIN mortise.mortise_menu m
WHERE r.permission = 'ADMIN'
  AND m.del_flag = 0
  AND NOT EXISTS (
    SELECT 1 FROM mortise.mortise_role_menu rm
    WHERE rm.id_mortise_role = r.id
      AND rm.id_mortise_menu = m.id
  );
