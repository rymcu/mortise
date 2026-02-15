-- =============================================
-- Mortise Wechat Module - Database Schema
-- PostgreSQL DDL Script
-- Author: ronger
-- Create Date: 2025-10-02
-- Description: 微信模块数据库表结构（微信账号、微信配置）
-- 注意: 主键ID由应用层 MyBatis-Flex flexId 生成，不使用数据库自增
-- Version Range: Wechat V60-V79
-- =============================================

-- =============================================
-- 表: mortise_wechat_account (微信账号表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_wechat_account
(
    id           BIGINT PRIMARY KEY,
    account_type VARCHAR(20)  NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    app_id       VARCHAR(100) NOT NULL,
    app_secret   TEXT,
    is_default   INTEGER   DEFAULT 0,
    is_enabled   INTEGER   DEFAULT 1,
    status       INTEGER   DEFAULT 0,
    del_flag     INTEGER   DEFAULT 0,
    remark       VARCHAR(500),
    created_by   BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by   BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_wechat_account_appid UNIQUE (app_id)
);

COMMENT ON TABLE mortise.mortise_wechat_account IS '微信账号表（支持多账号）';
COMMENT ON COLUMN mortise.mortise_wechat_account.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_wechat_account.account_type IS '账号类型: mp-公众号, open-开放平台, miniapp-小程序';
COMMENT ON COLUMN mortise.mortise_wechat_account.account_name IS '账号名称（便于识别，如：RYMCU公众号）';
COMMENT ON COLUMN mortise.mortise_wechat_account.app_id IS '微信 AppID';
COMMENT ON COLUMN mortise.mortise_wechat_account.app_secret IS 'AppSecret（加密存储）';
COMMENT ON COLUMN mortise.mortise_wechat_account.is_default IS '是否为默认账号: 0-否, 1-是（同一类型只能有一个默认）';
COMMENT ON COLUMN mortise.mortise_wechat_account.is_enabled IS '是否启用: 0-禁用, 1-启用';
COMMENT ON COLUMN mortise.mortise_wechat_account.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_wechat_account.del_flag IS '删除标记: 0-未删除, 1-已删除';
COMMENT ON COLUMN mortise.mortise_wechat_account.remark IS '备注说明';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_wechat_account_type ON mortise.mortise_wechat_account(account_type);
CREATE INDEX IF NOT EXISTS idx_wechat_account_default ON mortise.mortise_wechat_account(account_type, is_default);
CREATE INDEX IF NOT EXISTS idx_wechat_account_enabled ON mortise.mortise_wechat_account(is_enabled);
CREATE INDEX IF NOT EXISTS idx_wechat_account_status ON mortise.mortise_wechat_account(status);
CREATE INDEX IF NOT EXISTS idx_wechat_account_del_flag ON mortise.mortise_wechat_account(del_flag);

-- =============================================
-- 表: mortise_wechat_config (微信账号配置表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_wechat_config
(
    id           BIGINT PRIMARY KEY,
    account_id   BIGINT      NOT NULL,
    config_key   VARCHAR(50) NOT NULL,
    config_value TEXT,
    config_label VARCHAR(100),
    is_encrypted INTEGER   DEFAULT 0,
    sort_no      INTEGER   DEFAULT 0,
    status       INTEGER   DEFAULT 0,
    del_flag     INTEGER   DEFAULT 0,
    remark       VARCHAR(500),
    created_by   BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by   BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wechat_config_account FOREIGN KEY (account_id)
        REFERENCES mortise.mortise_wechat_account (id) ON DELETE CASCADE,
    CONSTRAINT uk_wechat_config_key UNIQUE (account_id, config_key)
);

COMMENT ON TABLE mortise.mortise_wechat_config IS '微信账号配置表';
COMMENT ON COLUMN mortise.mortise_wechat_config.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_wechat_config.account_id IS '关联的账号ID';
COMMENT ON COLUMN mortise.mortise_wechat_config.config_key IS '配置项键名';
COMMENT ON COLUMN mortise.mortise_wechat_config.config_value IS '配置项值';
COMMENT ON COLUMN mortise.mortise_wechat_config.config_label IS '配置项描述';
COMMENT ON COLUMN mortise.mortise_wechat_config.is_encrypted IS '是否加密: 0-否, 1-是';
COMMENT ON COLUMN mortise.mortise_wechat_config.sort_no IS '排序号';
COMMENT ON COLUMN mortise.mortise_wechat_config.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_wechat_config.del_flag IS '删除标记: 0-未删除, 1-已删除';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_wechat_config_account ON mortise.mortise_wechat_config(account_id);
CREATE INDEX IF NOT EXISTS idx_wechat_config_status ON mortise.mortise_wechat_config(status);
CREATE INDEX IF NOT EXISTS idx_wechat_config_del_flag ON mortise.mortise_wechat_config(del_flag);
