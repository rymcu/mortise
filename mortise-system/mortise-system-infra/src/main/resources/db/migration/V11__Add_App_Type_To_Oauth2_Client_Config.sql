-- =============================================
-- Mortise System Module - OAuth2 客户端入口类型
-- Description: 为 OAuth2 客户端配置表添加 app_type 列，用于区分不同登录入口
--              admin = 管理端登录   site = 用户端登录
-- =============================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'mortise'
          AND table_name   = 'mortise_oauth2_client_config'
          AND column_name  = 'app_type'
    ) THEN
        ALTER TABLE mortise.mortise_oauth2_client_config
            ADD COLUMN app_type VARCHAR(20) NOT NULL DEFAULT 'admin';

        COMMENT ON COLUMN mortise.mortise_oauth2_client_config.app_type
            IS '登录入口类型：admin=管理端，site=用户端';
    END IF;
END;
$$;

-- 已有种子数据（logto-admin / github-app）均属于管理端，DEFAULT 已覆盖，无需额外 UPDATE
