-- =============================================================================
-- V14: 统一 status 字段约定为 0=禁用, 1=启用（system 模块）
--
-- 历史约定: 0=正常/启用, 1=禁用（沿用 Java 枚举 ordinal）
-- 新约定:   0=禁用, 1=启用（行业惯例，与 aiot/community 模块一致）
-- =============================================================================

BEGIN;

-- ---------- 数据翻转：0↔1 ----------

UPDATE mortise.mortise_user
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

UPDATE mortise.mortise_role
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

UPDATE mortise.mortise_menu
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

UPDATE mortise.mortise_dict_type
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

UPDATE mortise.mortise_dict
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

UPDATE mortise.mortise_oauth2_client_config
SET    status = CASE WHEN status = 0 THEN 1 WHEN status = 1 THEN 0 ELSE status END;

-- ---------- 更新字典条目（Status 字典的 value 和 label） ----------
-- 使用 CASE 一次性翻转，避免顺序 UPDATE 互相影响

UPDATE mortise.mortise_dict
SET    label   = CASE WHEN value = '0' THEN '启用' WHEN value = '1' THEN '禁用' ELSE label END,
       sort_no = CASE WHEN value = '0' THEN 1      WHEN value = '1' THEN 2      ELSE sort_no END,
       value   = CASE WHEN value = '0' THEN '1'    WHEN value = '1' THEN '0'    ELSE value END
WHERE  dict_type_code = 'Status' AND value IN ('0', '1');

-- ---------- 更新列注释 ----------

COMMENT ON COLUMN mortise.mortise_user.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN mortise.mortise_role.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN mortise.mortise_menu.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN mortise.mortise_dict_type.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN mortise.mortise_dict.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.status IS '状态: 0-禁用, 1-启用';

-- ---------- 更新默认值 ----------

ALTER TABLE mortise.mortise_user ALTER COLUMN status SET DEFAULT 1;
ALTER TABLE mortise.mortise_role ALTER COLUMN status SET DEFAULT 1;
ALTER TABLE mortise.mortise_menu ALTER COLUMN status SET DEFAULT 1;
ALTER TABLE mortise.mortise_dict_type ALTER COLUMN status SET DEFAULT 1;
ALTER TABLE mortise.mortise_dict ALTER COLUMN status SET DEFAULT 1;
ALTER TABLE mortise.mortise_oauth2_client_config ALTER COLUMN status SET DEFAULT 1;

COMMIT;
