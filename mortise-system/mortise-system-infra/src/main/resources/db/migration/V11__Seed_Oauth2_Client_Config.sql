-- =============================================
-- Mortise System Module - OAuth2 客户端配置初始化
-- Description: 内置 OAuth2 登录提供商的占位配置
-- ID 规则: 10000000000009xxx（OAuth2 客户端配置段）
--
-- ⚠️  注意事项：
--   1. client_id / client_secret / *_uri 均为占位符，部署前必须替换为真实值。
--   2. 推荐通过管理后台 [系统管理 → OAuth2 客户端配置] 页面维护，
--      也可直接执行以下 UPDATE 语句覆盖。
--   3. 此脚本在 is_enabled = 0（关闭）状态下插入，填写真实参数后改为 1 方可生效。
-- =============================================

-- logto-admin：管理端 Logto OIDC 登录
INSERT INTO mortise.mortise_oauth2_client_config (
    id,
    registration_id,
    client_id,
    client_secret,
    client_name,
    scopes,
    redirect_uri_template,
    client_authentication_method,
    authorization_grant_type,
    authorization_uri,
    token_uri,
    user_info_uri,
    user_name_attribute,
    jwk_set_uri,
    is_enabled,
    status,
    del_flag,
    remark,
    created_time,
    updated_time
)
SELECT
    10000000000009001,
    'logto-admin',
    'REPLACE_WITH_LOGTO_APP_ID',
    'REPLACE_WITH_LOGTO_APP_SECRET',
    'Logto（管理端）',
    'openid,profile,email,offline_access',
    '{baseUrl}/login/oauth2/code/{registrationId}',
    'client_secret_basic',
    'authorization_code',
    'https://REPLACE_WITH_LOGTO_DOMAIN/oidc/auth',
    'https://REPLACE_WITH_LOGTO_DOMAIN/oidc/token',
    'https://REPLACE_WITH_LOGTO_DOMAIN/oidc/me',
    'sub',
    'https://REPLACE_WITH_LOGTO_DOMAIN/oidc/jwks',
    0,   -- is_enabled: 0=关闭，填写真实参数后改为 1
    0,
    0,
    '管理端 Logto 登录。部署前请替换 client_id、client_secret 及各 URI，然后将 is_enabled 改为 1。',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mortise.mortise_oauth2_client_config
    WHERE registration_id = 'logto-admin'
      AND del_flag = 0
);

-- github-app：GitHub OAuth2 登录（管理端）
INSERT INTO mortise.mortise_oauth2_client_config (
    id,
    registration_id,
    client_id,
    client_secret,
    client_name,
    scopes,
    redirect_uri_template,
    client_authentication_method,
    authorization_grant_type,
    authorization_uri,
    token_uri,
    user_info_uri,
    user_name_attribute,
    is_enabled,
    status,
    del_flag,
    remark,
    created_time,
    updated_time
)
SELECT
    10000000000009002,
    'github-app',
    'REPLACE_WITH_GITHUB_CLIENT_ID',
    'REPLACE_WITH_GITHUB_CLIENT_SECRET',
    'GitHub',
    'read:user,user:email',
    '{baseUrl}/login/oauth2/code/{registrationId}',
    'client_secret_basic',
    'authorization_code',
    'https://github.com/login/oauth/authorize',
    'https://github.com/login/oauth/access_token',
    'https://api.github.com/user',
    'login',
    0,   -- is_enabled: 0=关闭，填写真实参数后改为 1
    0,
    0,
    'GitHub OAuth2 登录。部署前请替换 client_id 与 client_secret，然后将 is_enabled 改为 1。',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mortise.mortise_oauth2_client_config
    WHERE registration_id = 'github-app'
      AND del_flag = 0
);
