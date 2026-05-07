-- 会员域旧库导入后校验 SQL
-- 用途：
--   1. 对比会员与 OAuth2 绑定导入后的行数
--   2. 检查 status 翻转后是否仍存在异常值或关联脏数据
--   3. 为认证链路回归提供基础验收数据

SELECT CURRENT_TIMESTAMP AS validation_time;

-- 1. 行数概览
SELECT 'row_count' AS check_type,
       'mortise_member' AS target_table,
       COUNT(*) AS total_rows,
       COUNT(*) FILTER (WHERE del_flag = 0) AS active_rows
FROM mortise.mortise_member
UNION ALL
SELECT 'row_count', 'mortise_member_oauth2_binding', COUNT(*), COUNT(*) FILTER (WHERE del_flag = 0)
FROM mortise.mortise_member_oauth2_binding
ORDER BY target_table;

-- 2. status 分布
SELECT 'status_distribution' AS check_type,
       'mortise_member' AS target_table,
       status::TEXT AS status_value,
       COUNT(*) AS row_count
FROM mortise.mortise_member
GROUP BY status
UNION ALL
SELECT 'status_distribution', 'mortise_member_oauth2_binding', status::TEXT, COUNT(*)
FROM mortise.mortise_member_oauth2_binding
GROUP BY status
ORDER BY target_table, status_value;

-- 3. 结构与语义校验
SELECT 'member_invalid_status' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_member
WHERE status NOT IN (0, 1)
   OR status IS NULL;

SELECT 'member_oauth2_binding_invalid_status' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_member_oauth2_binding
WHERE status NOT IN (0, 1)
   OR status IS NULL;

SELECT 'oauth2_binding_orphan_member' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_member_oauth2_binding binding
LEFT JOIN mortise.mortise_member member ON member.id = binding.member_id
WHERE member.id IS NULL;

SELECT 'enabled_binding_linked_disabled_member' AS check_name,
       COUNT(*) AS issue_count
FROM mortise.mortise_member_oauth2_binding binding
JOIN mortise.mortise_member member ON member.id = binding.member_id
WHERE binding.status = 1
  AND member.status = 0;

SELECT 'duplicate_provider_open_id' AS check_name,
       COUNT(*) AS issue_count
FROM (
         SELECT provider, open_id
         FROM mortise.mortise_member_oauth2_binding
         WHERE del_flag = 0
         GROUP BY provider, open_id
         HAVING COUNT(*) > 1
     ) duplicates;

SELECT 'member_missing_login_identifier' AS check_name,
       COUNT(*) AS review_count
FROM mortise.mortise_member
WHERE del_flag = 0
  AND NULLIF(BTRIM(COALESCE(username, '')), '') IS NULL
  AND NULLIF(BTRIM(COALESCE(email, '')), '') IS NULL
  AND NULLIF(BTRIM(COALESCE(phone, '')), '') IS NULL;
