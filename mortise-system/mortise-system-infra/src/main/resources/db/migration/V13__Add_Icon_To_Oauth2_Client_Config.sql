-- =============================================
-- Mortise System Module - Add Icon Column
-- Description: 为 OAuth2 客户端配置表添加 icon 列
-- =============================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'mortise'
        AND table_name = 'mortise_oauth2_client_config'
        AND column_name = 'icon'
    ) THEN
        ALTER TABLE mortise.mortise_oauth2_client_config
            ADD COLUMN icon VARCHAR(500);

        COMMENT ON COLUMN mortise.mortise_oauth2_client_config.icon IS '客户端图标，存储 Iconify 图标类名，如 i-simple-icons-github';
    END IF;
END;
$$;
