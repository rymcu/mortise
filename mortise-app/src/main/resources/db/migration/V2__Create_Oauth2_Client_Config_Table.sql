-- =============================================
-- 表: mortise_oauth2_client_config (OAuth2 客户端配置表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_oauth2_client_config
(
    id                          BIGINT PRIMARY KEY, -- 主键ID，由 MyBatis-Flex flexId 生成
    registration_id             VARCHAR(100)  NOT NULL, -- 客户端注册ID（唯一标识）
    client_id                   VARCHAR(200)  NOT NULL, -- OAuth2 客户端ID
    client_secret               VARCHAR(500),           -- OAuth2 客户端密钥
    client_name                 VARCHAR(100),           -- 客户端名称
    scopes                      VARCHAR(500),           -- 授权范围（逗号分隔）
    redirect_uri_template       VARCHAR(300),           -- 重定向URI模板
    client_authentication_method VARCHAR(50),           -- 客户端认证方法
    authorization_grant_type    VARCHAR(50),            -- 授权类型
    authorization_uri           VARCHAR(300),           -- 授权端点URI
    token_uri                   VARCHAR(300),           -- Token端点URI
    user_info_uri               VARCHAR(300),           -- 用户信息端点URI
    user_name_attribute         VARCHAR(100),           -- 用户名属性名称
    jwk_set_uri                 VARCHAR(300),           -- JWK Set URI
    is_enabled                  INTEGER     DEFAULT 1,  -- 是否启用
    status                      INTEGER     DEFAULT 0,  -- 状态：0-正常, 1-禁用
    del_flag                    INTEGER     DEFAULT 0,  -- 删除标记：0-未删除, 1-已删除
    remark                      VARCHAR(500),           -- 备注
    created_by                  BIGINT,                 -- 创建人ID
    created_time                TIMESTAMP   DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_by                  BIGINT,                 -- 更新人ID
    updated_time                TIMESTAMP   DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);

COMMENT ON TABLE mortise.mortise_oauth2_client_config IS 'OAuth2 客户端配置表';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.registration_id IS '客户端注册ID（唯一标识）';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.client_id IS 'OAuth2 客户端ID';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.client_secret IS 'OAuth2 客户端密钥';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.client_name IS '客户端名称';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.scopes IS '授权范围（逗号分隔）';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.redirect_uri_template IS '重定向URI模板';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.client_authentication_method IS '客户端认证方法';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.authorization_grant_type IS '授权类型';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.authorization_uri IS '授权端点URI';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.token_uri IS 'Token端点URI';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.user_info_uri IS '用户信息端点URI';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.user_name_attribute IS '用户名属性名称';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.jwk_set_uri IS 'JWK Set URI';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.is_enabled IS '是否启用';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.status IS '状态：0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.del_flag IS '删除标记：0-未删除, 1-已删除';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.remark IS '备注';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.created_by IS '创建人ID';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.updated_by IS '更新人ID';
COMMENT ON COLUMN mortise.mortise_oauth2_client_config.updated_time IS '更新时间';

-- 唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS uk_oauth2_client_registration_id ON mortise.mortise_oauth2_client_config(registration_id);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_oauth2_client_status ON mortise.mortise_oauth2_client_config(status);
CREATE INDEX IF NOT EXISTS idx_oauth2_client_del_flag ON mortise.mortise_oauth2_client_config(del_flag);