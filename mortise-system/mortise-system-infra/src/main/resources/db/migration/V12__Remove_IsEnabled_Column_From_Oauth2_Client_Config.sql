-- =============================================
-- 删除 mortise_oauth2_client_config 表中冗余的 is_enabled 列
-- 该列与 status 语义重叠，且语义方向与项目规范相反。
-- 启用/禁用状态统一由 status 字段管理（0=正常, 1=禁用），
-- 与项目其他模块（User、Dict、Role 等）保持一致。
-- =============================================

ALTER TABLE mortise.mortise_oauth2_client_config
    DROP COLUMN IF EXISTS is_enabled;
