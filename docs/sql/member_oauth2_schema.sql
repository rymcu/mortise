-- 创建会员表
CREATE TABLE IF NOT EXISTS mortise_member (
    id BIGINT PRIMARY KEY,
    account VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    nickname VARCHAR(50) NOT NULL,
    real_name VARCHAR(50),
    avatar VARCHAR(255),
    email VARCHAR(100),
    phone VARCHAR(20),
    gender INTEGER DEFAULT 0 COMMENT '0: 未知, 1: 男, 2: 女',
    status INTEGER DEFAULT 1 COMMENT '1: 正常, 0: 禁用',
    del_flag INTEGER DEFAULT 0 COMMENT '0: 未删除, 1: 已删除',
    last_login_time TIMESTAMP,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_online_time TIMESTAMP,
    UNIQUE KEY uk_account (account),
    KEY idx_email (email),
    KEY idx_phone (phone),
    KEY idx_status (status)
) COMMENT '会员用户表';

-- 创建会员 OAuth2 绑定表
CREATE TABLE IF NOT EXISTS mortise_member_oauth2_binding (
    id BIGINT PRIMARY KEY,
    member_id BIGINT NOT NULL COMMENT '会员 ID',
    provider VARCHAR(50) NOT NULL COMMENT 'OAuth2 提供商: github, google, wechat, logto',
    open_id VARCHAR(100) NOT NULL COMMENT 'OAuth2 提供商的用户唯一标识',
    union_id VARCHAR(100) COMMENT '微信 UnionID（仅微信开放平台）',
    nickname VARCHAR(100) COMMENT 'OAuth2 提供商返回的昵称',
    avatar VARCHAR(255) COMMENT 'OAuth2 提供商返回的头像',
    email VARCHAR(100) COMMENT 'OAuth2 提供商返回的邮箱',
    access_token TEXT COMMENT '访问令牌（可选）',
    refresh_token TEXT COMMENT '刷新令牌（可选）',
    expires_at TIMESTAMP COMMENT '令牌过期时间',
    raw_data TEXT COMMENT '原始用户数据 (JSON)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_provider_openid (provider, open_id),
    KEY idx_member_id (member_id),
    KEY idx_union_id (union_id),
    KEY idx_provider (provider),
    CONSTRAINT fk_member_oauth2_member FOREIGN KEY (member_id) REFERENCES mortise_member(id) ON DELETE CASCADE
) COMMENT '会员 OAuth2 绑定表';
