-- =============================================================================
-- V91: 统一 status 字段约定为 0=禁用, 1=启用（member 模块）
-- =============================================================================

BEGIN;

UPDATE mortise.mortise_member
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

COMMENT ON COLUMN mortise.mortise_member.status IS '状态: 0-禁用, 1-启用';

ALTER TABLE mortise.mortise_member ALTER COLUMN status SET DEFAULT 1;

COMMIT;
