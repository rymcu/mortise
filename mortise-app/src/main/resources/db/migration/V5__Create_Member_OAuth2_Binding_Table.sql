-- =============================================
-- Mortise Member Module - OAuth2 Binding Schema
-- PostgreSQL DDL Script
-- Author: ronger
-- Create Date: 2025-12-15
-- Description: 会员 OAuth2 绑定表结构
-- 注意: 主键ID由应用层 MyBatis-Flex flexId 生成，不使用数据库自增
-- =============================================

-- =============================================
-- 表: mortise_member_oauth2_binding (会员OAuth2绑定表)
-- 参考 mortise_user_oauth2_binding 表结构
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_member_oauth2_binding
(
    id            BIGINT PRIMARY KEY,
    member_id     BIGINT       NOT NULL,
    provider      VARCHAR(50)  NOT NULL,
    open_id       VARCHAR(255) NOT NULL,
    union_id      VARCHAR(255),
    nickname      VARCHAR(100),
    avatar        VARCHAR(500),
    email         VARCHAR(100),
    phone         VARCHAR(20),
    gender        VARCHAR(10),
    country       VARCHAR(50),
    province      VARCHAR(50),
    city          VARCHAR(50),
    access_token  TEXT,
    refresh_token TEXT,
    expires_at    TIMESTAMP,
    raw_data      TEXT,
    status        INTEGER   DEFAULT 0,
    del_flag      INTEGER   DEFAULT 0,
    created_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_member_provider_openid UNIQUE (provider, open_id),
    CONSTRAINT fk_member_oauth2_member FOREIGN KEY (member_id) 
        REFERENCES mortise.mortise_member (id) ON DELETE CASCADE
);

-- 添加表注释
COMMENT ON TABLE mortise.mortise_member_oauth2_binding IS '会员OAuth2绑定表';

-- 添加字段注释
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.member_id IS '会员ID';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.provider IS 'OAuth2 提供商 (wechat, wechat_mp, wechat_miniapp, qq, weibo)';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.open_id IS 'OAuth2 提供商的用户唯一标识';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.union_id IS '微信 UnionID（仅微信开放平台）';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.nickname IS 'OAuth2 提供商返回的昵称';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.avatar IS 'OAuth2 提供商返回的头像';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.email IS 'OAuth2 提供商返回的邮箱';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.phone IS 'OAuth2 提供商返回的手机号';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.gender IS '性别: male-男, female-女, other-其他';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.country IS '国家';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.province IS '省份';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.city IS '城市';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.access_token IS '访问令牌';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.refresh_token IS '刷新令牌';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.expires_at IS '令牌过期时间';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.raw_data IS '原始用户数据 (JSON)';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.del_flag IS '删除标记: 0-未删除, 1-已删除';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_member_oauth2_binding.updated_time IS '更新时间';

-- 创建索引
CREATE INDEX idx_member_oauth2_member_id ON mortise.mortise_member_oauth2_binding (member_id);
CREATE INDEX idx_member_oauth2_provider ON mortise.mortise_member_oauth2_binding (provider);
CREATE INDEX idx_member_oauth2_provider_unionid ON mortise.mortise_member_oauth2_binding (provider, union_id);
CREATE INDEX idx_member_oauth2_status ON mortise.mortise_member_oauth2_binding (status);
CREATE INDEX idx_member_oauth2_del_flag ON mortise.mortise_member_oauth2_binding (del_flag);

-- 创建触发器（实现 ON UPDATE CURRENT_TIMESTAMP 功能）
CREATE TRIGGER update_mortise_member_oauth2_binding_updated_time
    BEFORE UPDATE
    ON mortise.mortise_member_oauth2_binding
    FOR EACH ROW EXECUTE FUNCTION update_updated_time_column();
