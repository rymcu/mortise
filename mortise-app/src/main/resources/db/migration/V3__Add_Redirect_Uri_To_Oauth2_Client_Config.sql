DO $$
BEGIN
    -- 检查 'redirect_uri' 列是否存在于 'mortise.mortise_oauth2_client_config' 表中
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'mortise'
        AND table_name = 'mortise_oauth2_client_config'
        AND column_name = 'redirect_uri'
    ) THEN
-- 如果列不存在，则添加该列
ALTER TABLE mortise.mortise_oauth2_client_config
    ADD COLUMN redirect_uri VARCHAR(300);

-- 并为新列添加注释
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.redirect_uri IS '重定向URI';
END IF;
END;
$$;
