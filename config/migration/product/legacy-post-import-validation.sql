-- 商品域旧库导入后校验 SQL
-- 用途：
--   1. 对比商品分类、目录、SKU 与 SKU 目标映射的导入行数
--   2. 检查 ProductCategory / ProductSkuTarget 的状态翻转结果
--   3. 校验 SKU 默认项与目标映射关系是否完整

SELECT CURRENT_TIMESTAMP AS validation_time;

-- 1. 行数概览
SELECT 'row_count' AS check_type,
       'mortise_product_category' AS target_table,
       COUNT(*) AS total_rows,
       COUNT(*) FILTER (WHERE del_flag = 0) AS active_rows
FROM mortise.mortise_product_category
UNION ALL
SELECT 'row_count', 'mortise_product', COUNT(*), COUNT(*) FILTER (WHERE del_flag = 0)
FROM mortise.mortise_product
UNION ALL
SELECT 'row_count', 'mortise_product_sku', COUNT(*), COUNT(*) FILTER (WHERE del_flag = 0)
FROM mortise.mortise_product_sku
UNION ALL
SELECT 'row_count', 'mortise_product_sku_target', COUNT(*), COUNT(*) FILTER (WHERE del_flag = 0)
FROM mortise.mortise_product_sku_target
ORDER BY target_table;

-- 2. status / is_default 分布
SELECT 'status_distribution' AS check_type,
       'mortise_product_category' AS target_table,
       status::TEXT AS status_value,
       COUNT(*) AS row_count
FROM mortise.mortise_product_category
GROUP BY status
UNION ALL
SELECT 'status_distribution', 'mortise_product', status::TEXT, COUNT(*)
FROM mortise.mortise_product
GROUP BY status
UNION ALL
SELECT 'status_distribution', 'mortise_product_sku_target', status::TEXT, COUNT(*)
FROM mortise.mortise_product_sku_target
GROUP BY status
UNION ALL
SELECT 'is_default_distribution', 'mortise_product_sku', COALESCE(is_default::TEXT, 'null'), COUNT(*)
FROM mortise.mortise_product_sku
GROUP BY is_default
ORDER BY target_table, status_value;

-- 3. 结构与语义校验
SELECT 'product_category_status_active_mismatch' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_product_category
WHERE status IN (0, 1)
  AND (
      (status = 1 AND COALESCE(is_active, FALSE) = FALSE) OR
      (status = 0 AND COALESCE(is_active, FALSE) = TRUE)
  );

SELECT 'product_category_invalid_status' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_product_category
WHERE status NOT IN (0, 1)
   OR status IS NULL;

SELECT 'product_sku_target_invalid_status' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_product_sku_target
WHERE status NOT IN (0, 1)
   OR status IS NULL;

SELECT 'product_missing_category' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_product product
LEFT JOIN mortise.mortise_product_category category ON category.id = product.category_id
WHERE product.category_id IS NOT NULL
  AND category.id IS NULL;

SELECT 'sku_orphan_product' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_product_sku sku
LEFT JOIN mortise.mortise_product product ON product.id = sku.product_id
WHERE product.id IS NULL;

SELECT 'sku_target_orphan_sku' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_product_sku_target target
LEFT JOIN mortise.mortise_product_sku sku ON sku.id = target.product_sku_id
WHERE sku.id IS NULL;

SELECT 'multi_default_sku_per_product' AS check_name,
       COUNT(*) AS issue_count
FROM (
         SELECT product_id
         FROM mortise.mortise_product_sku
         WHERE del_flag = 0
           AND is_default = TRUE
         GROUP BY product_id
         HAVING COUNT(*) > 1
     ) duplicates;

SELECT 'sku_target_missing_target_type' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_product_sku_target
WHERE NULLIF(BTRIM(COALESCE(target_type, '')), '') IS NULL;
