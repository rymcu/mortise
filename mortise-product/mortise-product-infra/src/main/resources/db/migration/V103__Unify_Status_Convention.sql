-- =============================================================================
-- V103: 统一 status 字段约定为 0=禁用, 1=启用（product 模块）
--
-- 注意：mortise_product.status 是多值状态（0-草稿, 1-上架, 2-下架, 3-停产），
-- 不属于启用/禁用语义，不在此次翻转范围内。
-- =============================================================================

BEGIN;

UPDATE mortise.mortise_product_category
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

UPDATE mortise.mortise_product_sku_target
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

COMMENT ON COLUMN mortise.mortise_product_category.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN mortise.mortise_product_sku_target.status IS '状态: 0-禁用, 1-启用';

ALTER TABLE mortise.mortise_product_category ALTER COLUMN status SET DEFAULT 1;
ALTER TABLE mortise.mortise_product_sku_target ALTER COLUMN status SET DEFAULT 1;

COMMIT;
