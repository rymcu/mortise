-- =============================================
-- 删除 mortise_wechat_account 表中冗余的 is_enabled 列
-- 与 Oauth2ClientConfig 保持一致：统一由 status（0=正常, 1=禁用）管理启用状态，
-- 与项目 User、Dict、Role 等系统模块的命名规范对齐。
-- =============================================

DROP INDEX IF EXISTS mortise.idx_wechat_account_enabled;

ALTER TABLE mortise.mortise_wechat_account
    DROP COLUMN IF EXISTS is_enabled;
