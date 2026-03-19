-- 产品编码字段：PRD-{ULID} 格式，留空由服务层自动生成
ALTER TABLE mortise.mortise_product
    ADD COLUMN IF NOT EXISTS product_code VARCHAR(30);

-- 为已有记录生成编码（PRD- + 大写 ID 的零填充，仅做占位，生产环境可按需调整）
UPDATE mortise.mortise_product
SET product_code = 'PRD-' || LPAD(id::TEXT, 26, '0')
WHERE product_code IS NULL;

-- 设为非空 + 唯一约束
ALTER TABLE mortise.mortise_product
    ALTER COLUMN product_code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_product_product_code
    ON mortise.mortise_product (product_code);

COMMENT ON COLUMN mortise.mortise_product.product_code IS '产品编码（PRD-{ULID} 格式，全局唯一）';
