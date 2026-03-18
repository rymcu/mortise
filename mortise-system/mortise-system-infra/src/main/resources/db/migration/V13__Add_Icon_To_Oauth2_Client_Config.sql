-- 为 OAuth2 客户端配置表新增图标字段
-- icon 用于在登录页展示第三方平台的图标（存储图标 URL 或 CDN 地址）
ALTER TABLE mortise_oauth2_client_config
    ADD COLUMN icon VARCHAR(500) NULL COMMENT '客户端图标 URL';
