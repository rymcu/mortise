-- 会员域旧库导入后状态修复脚本
-- 用途：
--   1. 把 old-codes 旧语义 0=正常,1=禁用 的会员状态翻转为新语义 0=禁用,1=启用
--   2. 同步修正 OAuth2 绑定状态，避免导入后账号可用性失真
--
-- 注意：
--   1. 该脚本不是 Flyway，不接入应用启动流程
--   2. 该脚本按“旧值尚未翻转”假设编写，重复执行会再次翻转 0/1
--   3. 执行前请先备份测试环境数据库

BEGIN;

-- 会员状态：旧语义 0=正常,1=禁用 -> 新语义 0=禁用,1=启用
UPDATE mortise.mortise_member
SET status = CASE status
                 WHEN 0 THEN 1
                 WHEN 1 THEN 0
                 ELSE status
    END,
    updated_time = CURRENT_TIMESTAMP
WHERE status IN (0, 1);

-- OAuth2 绑定状态：旧语义 0=正常,1=禁用 -> 新语义 0=禁用,1=启用
UPDATE mortise.mortise_member_oauth2_binding
SET status = CASE status
                 WHEN 0 THEN 1
                 WHEN 1 THEN 0
                 ELSE status
    END,
    updated_time = CURRENT_TIMESTAMP
WHERE status IN (0, 1);

COMMIT;
