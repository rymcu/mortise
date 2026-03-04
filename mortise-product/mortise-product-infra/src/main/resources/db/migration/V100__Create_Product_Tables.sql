-- =============================================
-- Mortise Product Module - 通用产品目录表结构
-- PostgreSQL DDL Script
-- Author: ronger
-- Create Date: 2026-02-15
-- Description: 通用产品目录模块表结构（免费模块），不含电商交易属性
-- Version Range: V100-V119 reserved for product module
-- 注意: 主键ID由应用层 MyBatis-Flex flexId 生成，不使用数据库自增
-- =============================================

-- =============================================
-- 表: mortise_product_category (产品分类表)
-- 说明: Product 实体引用 categoryId，分类表作为基础参照
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_product_category
(
    id           BIGINT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    slug         VARCHAR(100) NOT NULL UNIQUE,
    description  TEXT,
    parent_id    BIGINT,
    image_url    VARCHAR(500),
    sort_no      INTEGER   DEFAULT 0,
    is_active    BOOLEAN   DEFAULT TRUE,
    status       INTEGER   DEFAULT 0,
    metadata     JSONB,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag     INTEGER   DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_product_category IS '产品分类表';
COMMENT ON COLUMN mortise.mortise_product_category.id IS '主键ID，由应用程序生成';
COMMENT ON COLUMN mortise.mortise_product_category.name IS '分类名称';
COMMENT ON COLUMN mortise.mortise_product_category.slug IS '分类别名';
COMMENT ON COLUMN mortise.mortise_product_category.description IS '分类描述';
COMMENT ON COLUMN mortise.mortise_product_category.parent_id IS '父分类ID';
COMMENT ON COLUMN mortise.mortise_product_category.image_url IS '分类图片URL';
COMMENT ON COLUMN mortise.mortise_product_category.sort_no IS '排序号';
COMMENT ON COLUMN mortise.mortise_product_category.is_active IS '是否激活';
COMMENT ON COLUMN mortise.mortise_product_category.status IS '状态：0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_product_category.metadata IS '扩展元数据';
COMMENT ON COLUMN mortise.mortise_product_category.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_product_category.updated_time IS '更新时间';
COMMENT ON COLUMN mortise.mortise_product_category.del_flag IS '删除标记：0-未删除, 1-已删除';

CREATE INDEX IF NOT EXISTS idx_product_category_parent ON mortise.mortise_product_category (parent_id);
CREATE INDEX IF NOT EXISTS idx_product_category_slug ON mortise.mortise_product_category (slug);


-- =============================================
-- 表: mortise_product (通用产品目录表)
-- 对应实体: com.rymcu.mortise.product.entity.Product
-- 说明: 不含电商交易属性，电商扩展见 commerce 模块的 mortise_product_commerce_ext
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_product
(
    id                BIGINT PRIMARY KEY,
    title             VARCHAR(200) NOT NULL,
    subtitle          VARCHAR(500),
    description       TEXT,
    short_description VARCHAR(1000),
    cover_image_url   VARCHAR(500),
    gallery_images    TEXT[],
    product_type      VARCHAR(20)  NOT NULL,
    category_id       BIGINT,
    tags              VARCHAR(100)[],
    features          JSONB,
    specifications    JSONB,
    seo_title         VARCHAR(200),
    seo_description   VARCHAR(500),
    seo_keywords      VARCHAR(500),
    status            INTEGER   DEFAULT 0,
    is_featured       BOOLEAN   DEFAULT FALSE,
    sort_no           INTEGER   DEFAULT 0,
    created_by        BIGINT,
    created_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_time    TIMESTAMP,
    del_flag          INTEGER   DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_product IS '通用产品目录表 — 不含电商交易属性';
COMMENT ON COLUMN mortise.mortise_product.id IS '主键ID，由应用程序生成';
COMMENT ON COLUMN mortise.mortise_product.title IS '产品标题';
COMMENT ON COLUMN mortise.mortise_product.subtitle IS '产品副标题';
COMMENT ON COLUMN mortise.mortise_product.description IS '产品描述';
COMMENT ON COLUMN mortise.mortise_product.short_description IS '简短描述';
COMMENT ON COLUMN mortise.mortise_product.cover_image_url IS '封面图片URL';
COMMENT ON COLUMN mortise.mortise_product.gallery_images IS '产品图片数组';
COMMENT ON COLUMN mortise.mortise_product.product_type IS '产品类型编码，内置 standard/digital/service/bundle，可通过 SPI 扩展';
COMMENT ON COLUMN mortise.mortise_product.category_id IS '分类ID';
COMMENT ON COLUMN mortise.mortise_product.tags IS '标签数组';
COMMENT ON COLUMN mortise.mortise_product.features IS '产品特性';
COMMENT ON COLUMN mortise.mortise_product.specifications IS '规格参数';
COMMENT ON COLUMN mortise.mortise_product.seo_title IS 'SEO标题';
COMMENT ON COLUMN mortise.mortise_product.seo_description IS 'SEO描述';
COMMENT ON COLUMN mortise.mortise_product.seo_keywords IS 'SEO关键词';
COMMENT ON COLUMN mortise.mortise_product.status IS '状态：0-草稿, 1-上架, 2-下架, 3-停产';
COMMENT ON COLUMN mortise.mortise_product.is_featured IS '是否推荐产品';
COMMENT ON COLUMN mortise.mortise_product.sort_no IS '排序号';
COMMENT ON COLUMN mortise.mortise_product.created_by IS '创建人ID';
COMMENT ON COLUMN mortise.mortise_product.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_product.updated_time IS '更新时间';
COMMENT ON COLUMN mortise.mortise_product.published_time IS '发布时间';
COMMENT ON COLUMN mortise.mortise_product.del_flag IS '删除标记：0-未删除, 1-已删除';

CREATE INDEX IF NOT EXISTS idx_product_type ON mortise.mortise_product (product_type);
CREATE INDEX IF NOT EXISTS idx_product_category ON mortise.mortise_product (category_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON mortise.mortise_product (status) WHERE del_flag = 0;
CREATE INDEX IF NOT EXISTS idx_product_featured ON mortise.mortise_product (is_featured) WHERE is_featured = TRUE;
CREATE INDEX IF NOT EXISTS idx_product_published ON mortise.mortise_product (published_time) WHERE published_time IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_product_tags ON mortise.mortise_product USING GIN (tags);


-- =============================================
-- 表: mortise_product_sku (通用产品 SKU 表)
-- 对应实体: com.rymcu.mortise.product.entity.ProductSku
-- 说明: 仅保留规格骨架，定价/库存/物流见 commerce 模块的 mortise_sku_pricing
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_product_sku
(
    id           BIGINT PRIMARY KEY,
    product_id   BIGINT       NOT NULL,
    sku_code     VARCHAR(100) NOT NULL,
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    attributes   JSONB,
    status       VARCHAR(20)  NOT NULL DEFAULT 'active',
    is_default   BOOLEAN               DEFAULT FALSE,
    metadata     JSONB,
    created_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag     INTEGER               DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_product_sku IS '通用产品 SKU 表 — 仅保留规格骨架，不含定价/库存';
COMMENT ON COLUMN mortise.mortise_product_sku.id IS '主键ID，由应用程序生成';
COMMENT ON COLUMN mortise.mortise_product_sku.product_id IS '产品ID';
COMMENT ON COLUMN mortise.mortise_product_sku.sku_code IS 'SKU编码';
COMMENT ON COLUMN mortise.mortise_product_sku.name IS 'SKU名称';
COMMENT ON COLUMN mortise.mortise_product_sku.description IS 'SKU描述';
COMMENT ON COLUMN mortise.mortise_product_sku.attributes IS 'SKU属性(如规格、颜色等)';
COMMENT ON COLUMN mortise.mortise_product_sku.status IS '状态：active-上架, inactive-下架, discontinued-停产';
COMMENT ON COLUMN mortise.mortise_product_sku.is_default IS '是否默认SKU';
COMMENT ON COLUMN mortise.mortise_product_sku.metadata IS '扩展元数据';
COMMENT ON COLUMN mortise.mortise_product_sku.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_product_sku.updated_time IS '更新时间';
COMMENT ON COLUMN mortise.mortise_product_sku.del_flag IS '删除标记：0-未删除, 1-已删除';

CREATE UNIQUE INDEX IF NOT EXISTS idx_sku_code ON mortise.mortise_product_sku (sku_code) WHERE del_flag = 0;
CREATE INDEX IF NOT EXISTS idx_sku_product ON mortise.mortise_product_sku (product_id);
CREATE INDEX IF NOT EXISTS idx_sku_status ON mortise.mortise_product_sku (status) WHERE del_flag = 0;


-- =============================================
-- 表: mortise_product_sku_target (产品SKU目标关联表)
-- 对应实体: com.rymcu.mortise.product.entity.ProductSkuTarget
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_product_sku_target
(
    id             BIGINT PRIMARY KEY,
    product_sku_id BIGINT      NOT NULL,
    target_type    VARCHAR(30) NOT NULL,
    target_id      BIGINT      NOT NULL,
    quantity       INTEGER     NOT NULL DEFAULT 1,
    validity_days  INTEGER,
    access_level   VARCHAR(20)          DEFAULT 'full',
    conditions     JSONB,
    metadata       JSONB,
    status         INTEGER              DEFAULT 0,
    created_time   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_time   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    del_flag       INTEGER              DEFAULT 0,
    UNIQUE (product_sku_id, target_type, target_id)
);

COMMENT ON TABLE mortise.mortise_product_sku_target IS '产品SKU目标关联表';
COMMENT ON COLUMN mortise.mortise_product_sku_target.id IS '主键ID，由应用程序生成';
COMMENT ON COLUMN mortise.mortise_product_sku_target.product_sku_id IS '产品SKU ID';
COMMENT ON COLUMN mortise.mortise_product_sku_target.target_type IS '目标类型，可通过 SPI 扩展';
COMMENT ON COLUMN mortise.mortise_product_sku_target.target_id IS '关联目标实体ID';
COMMENT ON COLUMN mortise.mortise_product_sku_target.quantity IS '发放数量';
COMMENT ON COLUMN mortise.mortise_product_sku_target.validity_days IS '有效天数';
COMMENT ON COLUMN mortise.mortise_product_sku_target.access_level IS '访问级别';
COMMENT ON COLUMN mortise.mortise_product_sku_target.conditions IS '发放条件';
COMMENT ON COLUMN mortise.mortise_product_sku_target.metadata IS '扩展元数据';
COMMENT ON COLUMN mortise.mortise_product_sku_target.status IS '状态：0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_product_sku_target.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_product_sku_target.updated_time IS '更新时间';
COMMENT ON COLUMN mortise.mortise_product_sku_target.del_flag IS '删除标记：0-未删除, 1-已删除';

CREATE INDEX IF NOT EXISTS idx_sku_target_sku ON mortise.mortise_product_sku_target (product_sku_id);
CREATE INDEX IF NOT EXISTS idx_sku_target_target ON mortise.mortise_product_sku_target (target_type, target_id);
