-- AI 提供商与模型配置表
-- 模块前缀: 300

-- ========== AI 提供商表 ==========
CREATE TABLE IF NOT EXISTS mortise.mortise_ai_provider (
    id              BIGINT          PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    code            VARCHAR(50)     NOT NULL,
    api_key         VARCHAR(500),
    base_url        VARCHAR(500),
    default_model_name VARCHAR(200),
    status          INTEGER         NOT NULL DEFAULT 1,
    sort_no         INTEGER         NOT NULL DEFAULT 0,
    remark          VARCHAR(500),
    del_flag        INTEGER         NOT NULL DEFAULT 0,
    created_time    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_provider_code
    ON mortise.mortise_ai_provider (code) WHERE del_flag = 0;

COMMENT ON TABLE  mortise.mortise_ai_provider IS 'AI 提供商配置';
COMMENT ON COLUMN mortise.mortise_ai_provider.code IS '提供商编码，对应 ModelType 枚举的 code';
COMMENT ON COLUMN mortise.mortise_ai_provider.api_key IS 'API 密钥（建议加密存储）';
COMMENT ON COLUMN mortise.mortise_ai_provider.base_url IS '自定义 API 地址';
COMMENT ON COLUMN mortise.mortise_ai_provider.default_model_name IS '该提供商的默认模型名称';
COMMENT ON COLUMN mortise.mortise_ai_provider.status IS '0=禁用, 1=启用';

-- ========== AI 模型表 ==========
CREATE TABLE IF NOT EXISTS mortise.mortise_ai_model (
    id              BIGINT          PRIMARY KEY,
    provider_id     BIGINT          NOT NULL,
    model_name      VARCHAR(200)    NOT NULL,
    display_name    VARCHAR(200),
    status          INTEGER         NOT NULL DEFAULT 1,
    sort_no         INTEGER         NOT NULL DEFAULT 0,
    remark          VARCHAR(500),
    del_flag        INTEGER         NOT NULL DEFAULT 0,
    created_time    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_model_provider_id
    ON mortise.mortise_ai_model (provider_id) WHERE del_flag = 0;

COMMENT ON TABLE  mortise.mortise_ai_model IS 'AI 模型配置';
COMMENT ON COLUMN mortise.mortise_ai_model.provider_id IS '所属提供商 ID';
COMMENT ON COLUMN mortise.mortise_ai_model.model_name IS '模型标识（API 调用时使用）';
COMMENT ON COLUMN mortise.mortise_ai_model.display_name IS '显示名称';
COMMENT ON COLUMN mortise.mortise_ai_model.status IS '0=禁用, 1=启用';
