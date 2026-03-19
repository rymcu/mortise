-- =============================================================================
-- V63: 统一 status 字段约定为 0=禁用, 1=启用（wechat 模块）
-- =============================================================================

BEGIN;

UPDATE mortise.mortise_wechat_account
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

UPDATE mortise.mortise_wechat_config
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

COMMENT ON COLUMN mortise.mortise_wechat_account.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN mortise.mortise_wechat_config.status IS '状态: 0-禁用, 1-启用';

ALTER TABLE mortise.mortise_wechat_account ALTER COLUMN status SET DEFAULT 1;
ALTER TABLE mortise.mortise_wechat_config ALTER COLUMN status SET DEFAULT 1;

COMMIT;
