-- 修复用户状态语义、默认值与初始化管理员首登阻塞问题

ALTER TABLE mortise.mortise_user
    ALTER COLUMN status SET DEFAULT 1;

COMMENT ON COLUMN mortise.mortise_user.status IS '状态: 0-禁用, 1-启用';

-- 修复系统初始化流程创建但尚未登录的管理员账号：
-- 1. 拥有 ADMIN 角色
-- 2. 从未登录
-- 3. 初始化流程未设置头像（avatar 为空）
-- 4. 当前仍处于错误的禁用状态
UPDATE mortise.mortise_user AS u
SET status = 1
FROM mortise.mortise_user_role AS ur
JOIN mortise.mortise_role AS r
  ON r.id = ur.id_mortise_role
WHERE u.id = ur.id_mortise_user
  AND r.permission = 'ADMIN'
  AND COALESCE(u.status, 0) = 0
  AND u.last_login_time IS NULL
  AND (u.avatar IS NULL OR btrim(u.avatar) = '');
