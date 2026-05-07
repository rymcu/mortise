-- 为全新库初始化提供兼容引导：
-- community 的 V80 会先引用 mortise_product，产品完整建表位于 V100。
-- 这里先把被外键依赖的产品主表提前建出来，后续 V100 继续补齐分类、SKU 与索引。

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
