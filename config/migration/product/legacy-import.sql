-- 商品域旧库导入脚本
-- 用途：
--   1. 从旧库 staging 数据库导入分类、产品、SKU 与 SKU 目标映射
--   2. 只负责搬运旧值，不做状态翻转
--   3. 状态语义修正请继续执行 legacy-status-remap.sql

BEGIN;

CREATE EXTENSION IF NOT EXISTS dblink;

WITH legacy_category AS (
    SELECT *
    FROM dblink(:'source_conn', $db$
        SELECT id,
               name,
               slug,
               description,
               parent_id,
               image_url,
               sort_no,
               is_active,
               status,
               metadata,
               created_time,
               updated_time,
               del_flag
        FROM mortise.mortise_product_category
    $db$) AS t(
        id BIGINT,
        name VARCHAR(100),
        slug VARCHAR(100),
        description TEXT,
        parent_id BIGINT,
        image_url VARCHAR(500),
        sort_no INTEGER,
        is_active BOOLEAN,
        status INTEGER,
        metadata JSONB,
        created_time TIMESTAMP,
        updated_time TIMESTAMP,
        del_flag INTEGER
    )
)
INSERT INTO mortise.mortise_product_category (
    id,
    name,
    slug,
    description,
    parent_id,
    image_url,
    sort_no,
    is_active,
    status,
    metadata,
    created_time,
    updated_time,
    del_flag
)
SELECT id,
       name,
       slug,
       description,
       parent_id,
       image_url,
       COALESCE(sort_no, 0),
       COALESCE(is_active, FALSE),
       COALESCE(status, 0),
       metadata,
       COALESCE(created_time, CURRENT_TIMESTAMP),
       COALESCE(updated_time, COALESCE(created_time, CURRENT_TIMESTAMP)),
       COALESCE(del_flag, 0)
FROM legacy_category
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    slug = EXCLUDED.slug,
    description = EXCLUDED.description,
    parent_id = EXCLUDED.parent_id,
    image_url = EXCLUDED.image_url,
    sort_no = EXCLUDED.sort_no,
    is_active = EXCLUDED.is_active,
    status = EXCLUDED.status,
    metadata = EXCLUDED.metadata,
    created_time = EXCLUDED.created_time,
    updated_time = EXCLUDED.updated_time,
    del_flag = EXCLUDED.del_flag;

WITH legacy_product AS (
    SELECT *
    FROM dblink(:'source_conn', $db$
        SELECT id,
               title,
               subtitle,
               description,
               short_description,
               cover_image_url,
               gallery_images,
               product_type,
               category_id,
               tags,
               features,
               specifications,
               seo_title,
               seo_description,
               seo_keywords,
               status,
               is_featured,
               is_digital,
               requires_shipping,
               weight_grams,
               dimensions,
               sort_no,
               view_count,
               sale_count,
               rating_average,
               rating_count,
               created_by,
               created_time,
               updated_time,
               published_time,
               del_flag
        FROM mortise.mortise_product
    $db$) AS t(
        id BIGINT,
        title VARCHAR(200),
        subtitle VARCHAR(500),
        description TEXT,
        short_description VARCHAR(1000),
        cover_image_url VARCHAR(500),
        gallery_images TEXT[],
        product_type VARCHAR(20),
        category_id BIGINT,
        tags VARCHAR(100)[],
        features JSONB,
        specifications JSONB,
        seo_title VARCHAR(200),
        seo_description VARCHAR(500),
        seo_keywords VARCHAR(500),
        status INTEGER,
        is_featured BOOLEAN,
        is_digital BOOLEAN,
        requires_shipping BOOLEAN,
        weight_grams INTEGER,
        dimensions JSONB,
        sort_no INTEGER,
        view_count INTEGER,
        sale_count INTEGER,
        rating_average NUMERIC(3, 2),
        rating_count INTEGER,
        created_by BIGINT,
        created_time TIMESTAMP,
        updated_time TIMESTAMP,
        published_time TIMESTAMP,
        del_flag INTEGER
    )
)
INSERT INTO mortise.mortise_product (
    id,
    title,
    subtitle,
    description,
    short_description,
    cover_image_url,
    gallery_images,
    product_type,
    category_id,
    tags,
    features,
    specifications,
    seo_title,
    seo_description,
    seo_keywords,
    status,
    is_featured,
    sort_no,
    created_by,
    created_time,
    updated_time,
    published_time,
    del_flag,
    product_code
)
SELECT id,
       title,
       subtitle,
       description,
       short_description,
       cover_image_url,
       gallery_images,
       product_type,
       category_id,
       tags,
       features,
       CASE
           WHEN is_digital IS NULL
               AND requires_shipping IS NULL
               AND weight_grams IS NULL
               AND dimensions IS NULL
               AND view_count IS NULL
               AND sale_count IS NULL
               AND rating_average IS NULL
               AND rating_count IS NULL
               THEN specifications
           ELSE COALESCE(specifications, '{}'::jsonb) ||
                jsonb_build_object(
                    '_legacySalesFields',
                    jsonb_strip_nulls(
                        jsonb_build_object(
                            'isDigital', is_digital,
                            'requiresShipping', requires_shipping,
                            'weightGrams', weight_grams,
                            'dimensions', dimensions,
                            'viewCount', view_count,
                            'saleCount', sale_count,
                            'ratingAverage', rating_average,
                            'ratingCount', rating_count
                        )
                    )
                )
           END,
       seo_title,
       seo_description,
       seo_keywords,
       COALESCE(status, 0),
       COALESCE(is_featured, FALSE),
       COALESCE(sort_no, 0),
       created_by,
       COALESCE(created_time, CURRENT_TIMESTAMP),
       COALESCE(updated_time, COALESCE(created_time, CURRENT_TIMESTAMP)),
       published_time,
       COALESCE(del_flag, 0),
       CONCAT('LEG-', id::text)
FROM legacy_product
ON CONFLICT (id) DO UPDATE
SET title = EXCLUDED.title,
    subtitle = EXCLUDED.subtitle,
    description = EXCLUDED.description,
    short_description = EXCLUDED.short_description,
    cover_image_url = EXCLUDED.cover_image_url,
    gallery_images = EXCLUDED.gallery_images,
    product_type = EXCLUDED.product_type,
    category_id = EXCLUDED.category_id,
    tags = EXCLUDED.tags,
    features = EXCLUDED.features,
    specifications = EXCLUDED.specifications,
    seo_title = EXCLUDED.seo_title,
    seo_description = EXCLUDED.seo_description,
    seo_keywords = EXCLUDED.seo_keywords,
    status = EXCLUDED.status,
    is_featured = EXCLUDED.is_featured,
    sort_no = EXCLUDED.sort_no,
    created_by = EXCLUDED.created_by,
    created_time = EXCLUDED.created_time,
    updated_time = EXCLUDED.updated_time,
    published_time = EXCLUDED.published_time,
    del_flag = EXCLUDED.del_flag,
    product_code = EXCLUDED.product_code;

WITH legacy_sku AS (
    SELECT *
    FROM dblink(:'source_conn', $db$
        SELECT id,
               product_id,
               sku_code,
               name,
               description,
               attributes,
               original_price,
               current_price,
               cost_price,
               currency,
               inventory_type,
               stock_quantity,
               reserved_quantity,
               low_stock_threshold,
               sell_start_time,
               sell_end_time,
               max_purchase_quantity,
               min_purchase_quantity,
               status,
               is_default,
               weight_grams,
               dimensions,
               shipping_template_id,
               sale_count,
               view_count,
               metadata,
               created_time,
               updated_time,
               del_flag
        FROM mortise.mortise_product_sku
    $db$) AS t(
        id BIGINT,
        product_id BIGINT,
        sku_code VARCHAR(100),
        name VARCHAR(200),
        description TEXT,
        attributes JSONB,
        original_price NUMERIC(10, 2),
        current_price NUMERIC(10, 2),
        cost_price NUMERIC(10, 2),
        currency VARCHAR(10),
        inventory_type VARCHAR(20),
        stock_quantity INTEGER,
        reserved_quantity INTEGER,
        low_stock_threshold INTEGER,
        sell_start_time TIMESTAMP,
        sell_end_time TIMESTAMP,
        max_purchase_quantity INTEGER,
        min_purchase_quantity INTEGER,
        status VARCHAR(20),
        is_default BOOLEAN,
        weight_grams INTEGER,
        dimensions JSONB,
        shipping_template_id BIGINT,
        sale_count INTEGER,
        view_count INTEGER,
        metadata JSONB,
        created_time TIMESTAMP,
        updated_time TIMESTAMP,
        del_flag INTEGER
    )
)
INSERT INTO mortise.mortise_product_sku (
    id,
    product_id,
    sku_code,
    name,
    description,
    attributes,
    status,
    is_default,
    metadata,
    created_time,
    updated_time,
    del_flag
)
SELECT s.id,
       s.product_id,
       s.sku_code,
       s.name,
       s.description,
       s.attributes,
       COALESCE(s.status, 'active'),
       COALESCE(s.is_default, FALSE),
       COALESCE(s.metadata, '{}'::jsonb) ||
       jsonb_build_object(
           '_legacySalesFields',
           jsonb_strip_nulls(
               jsonb_build_object(
                   'originalPrice', s.original_price,
                   'currentPrice', s.current_price,
                   'costPrice', s.cost_price,
                   'currency', s.currency,
                   'inventoryType', s.inventory_type,
                   'stockQuantity', s.stock_quantity,
                   'reservedQuantity', s.reserved_quantity,
                   'lowStockThreshold', s.low_stock_threshold,
                   'sellStartTime', s.sell_start_time,
                   'sellEndTime', s.sell_end_time,
                   'maxPurchaseQuantity', s.max_purchase_quantity,
                   'minPurchaseQuantity', s.min_purchase_quantity,
                   'weightGrams', s.weight_grams,
                   'dimensions', s.dimensions,
                   'shippingTemplateId', s.shipping_template_id,
                   'saleCount', s.sale_count,
                   'viewCount', s.view_count
               )
           )
       ),
       COALESCE(s.created_time, CURRENT_TIMESTAMP),
       COALESCE(s.updated_time, COALESCE(s.created_time, CURRENT_TIMESTAMP)),
       COALESCE(s.del_flag, 0)
FROM legacy_sku s
WHERE EXISTS (
    SELECT 1
    FROM mortise.mortise_product p
    WHERE p.id = s.product_id
)
ON CONFLICT (id) DO UPDATE
SET product_id = EXCLUDED.product_id,
    sku_code = EXCLUDED.sku_code,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    attributes = EXCLUDED.attributes,
    status = EXCLUDED.status,
    is_default = EXCLUDED.is_default,
    metadata = EXCLUDED.metadata,
    created_time = EXCLUDED.created_time,
    updated_time = EXCLUDED.updated_time,
    del_flag = EXCLUDED.del_flag;

WITH legacy_sku_target AS (
    SELECT *
    FROM dblink(:'source_conn', $db$
        SELECT id,
               product_sku_id,
               target_type,
               target_id,
               quantity,
               validity_days,
               access_level,
               conditions,
               metadata,
               status,
               created_time,
               updated_time,
               del_flag
        FROM mortise.mortise_product_sku_target
    $db$) AS t(
        id BIGINT,
        product_sku_id BIGINT,
        target_type VARCHAR(30),
        target_id BIGINT,
        quantity INTEGER,
        validity_days INTEGER,
        access_level VARCHAR(20),
        conditions JSONB,
        metadata JSONB,
        status INTEGER,
        created_time TIMESTAMP,
        updated_time TIMESTAMP,
        del_flag INTEGER
    )
)
INSERT INTO mortise.mortise_product_sku_target (
    id,
    product_sku_id,
    target_type,
    target_id,
    quantity,
    validity_days,
    access_level,
    conditions,
    metadata,
    status,
    created_time,
    updated_time,
    del_flag
)
SELECT st.id,
       st.product_sku_id,
       st.target_type,
       st.target_id,
       COALESCE(st.quantity, 1),
       st.validity_days,
       COALESCE(st.access_level, 'full'),
       st.conditions,
       st.metadata,
       COALESCE(st.status, 0),
       COALESCE(st.created_time, CURRENT_TIMESTAMP),
       COALESCE(st.updated_time, COALESCE(st.created_time, CURRENT_TIMESTAMP)),
       COALESCE(st.del_flag, 0)
FROM legacy_sku_target st
WHERE EXISTS (
    SELECT 1
    FROM mortise.mortise_product_sku sku
    WHERE sku.id = st.product_sku_id
)
ON CONFLICT (id) DO UPDATE
SET product_sku_id = EXCLUDED.product_sku_id,
    target_type = EXCLUDED.target_type,
    target_id = EXCLUDED.target_id,
    quantity = EXCLUDED.quantity,
    validity_days = EXCLUDED.validity_days,
    access_level = EXCLUDED.access_level,
    conditions = EXCLUDED.conditions,
    metadata = EXCLUDED.metadata,
    status = EXCLUDED.status,
    created_time = EXCLUDED.created_time,
    updated_time = EXCLUDED.updated_time,
    del_flag = EXCLUDED.del_flag;

COMMIT;

SELECT 'mortise_product_category' AS table_name, count(*) AS row_count
FROM mortise.mortise_product_category
UNION ALL
SELECT 'mortise_product' AS table_name, count(*) AS row_count
FROM mortise.mortise_product
UNION ALL
SELECT 'mortise_product_sku' AS table_name, count(*) AS row_count
FROM mortise.mortise_product_sku
UNION ALL
SELECT 'mortise_product_sku_target' AS table_name, count(*) AS row_count
FROM mortise.mortise_product_sku_target
ORDER BY table_name;
