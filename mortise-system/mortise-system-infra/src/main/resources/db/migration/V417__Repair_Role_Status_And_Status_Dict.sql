-- 修复角色状态语义与通用状态字典

ALTER TABLE mortise.mortise_role
    ALTER COLUMN status SET DEFAULT 1;

COMMENT ON COLUMN mortise.mortise_role.status IS '状态: 0-禁用, 1-启用';

-- 修复系统内置角色状态
UPDATE mortise.mortise_role
SET status = 1,
    updated_time = CURRENT_TIMESTAMP
WHERE permission IN ('ADMIN', 'USER')
  AND COALESCE(status, 0) <> 1;

-- 修复通用状态字典语义
UPDATE mortise.mortise_dict
SET label = '禁用',
    sort_no = 2,
    icon = 'i-lucide-square-x',
    color = 'error',
    status = 1,
    updated_time = CURRENT_TIMESTAMP
WHERE dict_type_code = 'Status'
  AND value = '0';

UPDATE mortise.mortise_dict
SET label = '启用',
    sort_no = 1,
    icon = 'i-lucide-square-check',
    color = 'success',
    status = 1,
    updated_time = CURRENT_TIMESTAMP
WHERE dict_type_code = 'Status'
  AND value = '1';
