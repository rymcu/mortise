-- =============================================
-- Mortise Member Module - Database Schema
-- PostgreSQL DDL Script
-- Author: ronger
-- Description: 会员模块核心表结构
-- 注意: 主键ID由应用层 MyBatis-Flex flexId 生成，不使用数据库自增
-- Version Range: Member V20-V39
-- =============================================

-- =============================================
-- 表: mortise_member (会员表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_member
(
    id                  BIGINT PRIMARY KEY,
    username            VARCHAR(50) UNIQUE,
    email               VARCHAR(100) UNIQUE,
    phone               VARCHAR(20) UNIQUE,
    password_hash       VARCHAR(255),
    name                VARCHAR(100),
    nickname            VARCHAR(50),
    avatar_url          VARCHAR(500),
    gender              VARCHAR(10),
    birth_date          DATE,
    status              INTEGER        DEFAULT 0,
    member_level        VARCHAR(20)    DEFAULT 'normal',
    points              INTEGER        DEFAULT 0,
    balance             DECIMAL(10, 2) DEFAULT 0.00,
    register_source     VARCHAR(50),
    referrer_id         BIGINT REFERENCES mortise.mortise_member (id),
    last_login_time     TIMESTAMP,
    email_verified_time TIMESTAMP,
    phone_verified_time TIMESTAMP,
    profile             JSONB,
    preferences         JSONB,
    created_time        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_time        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    del_flag            INTEGER        DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_member IS '会员表';
COMMENT ON COLUMN mortise.mortise_member.id IS '主键ID，由应用程序生成';
COMMENT ON COLUMN mortise.mortise_member.username IS '用户名';
COMMENT ON COLUMN mortise.mortise_member.email IS '邮箱';
COMMENT ON COLUMN mortise.mortise_member.phone IS '手机号';
COMMENT ON COLUMN mortise.mortise_member.password_hash IS '密码哈希';
COMMENT ON COLUMN mortise.mortise_member.name IS '真实姓名';
COMMENT ON COLUMN mortise.mortise_member.nickname IS '昵称';
COMMENT ON COLUMN mortise.mortise_member.avatar_url IS '头像URL';
COMMENT ON COLUMN mortise.mortise_member.gender IS '性别：male-男, female-女, other-其他';
COMMENT ON COLUMN mortise.mortise_member.birth_date IS '出生日期';
COMMENT ON COLUMN mortise.mortise_member.status IS '状态：0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_member.member_level IS '会员等级';
COMMENT ON COLUMN mortise.mortise_member.points IS '积分';
COMMENT ON COLUMN mortise.mortise_member.balance IS '账户余额';
COMMENT ON COLUMN mortise.mortise_member.register_source IS '注册来源';
COMMENT ON COLUMN mortise.mortise_member.referrer_id IS '推荐人ID';
COMMENT ON COLUMN mortise.mortise_member.last_login_time IS '最后登录时间';
COMMENT ON COLUMN mortise.mortise_member.email_verified_time IS '邮箱验证时间';
COMMENT ON COLUMN mortise.mortise_member.phone_verified_time IS '手机号验证时间';
COMMENT ON COLUMN mortise.mortise_member.profile IS '扩展资料';
COMMENT ON COLUMN mortise.mortise_member.preferences IS '个人偏好设置';
COMMENT ON COLUMN mortise.mortise_member.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_member.updated_time IS '更新时间';
COMMENT ON COLUMN mortise.mortise_member.del_flag IS '删除标记：0-未删除, 1-已删除';

CREATE INDEX IF NOT EXISTS idx_member_email ON mortise.mortise_member (email) WHERE del_flag = 0;
CREATE INDEX IF NOT EXISTS idx_member_phone ON mortise.mortise_member (phone) WHERE del_flag = 0;
CREATE INDEX IF NOT EXISTS idx_member_status ON mortise.mortise_member (status) WHERE del_flag = 0;
CREATE INDEX IF NOT EXISTS idx_member_level ON mortise.mortise_member (member_level);
CREATE INDEX IF NOT EXISTS idx_member_referrer ON mortise.mortise_member (referrer_id);
-- 复合索引用于会员查询
CREATE INDEX IF NOT EXISTS idx_member_status_level ON mortise.mortise_member (status, member_level, del_flag);
CREATE INDEX IF NOT EXISTS idx_member_created ON mortise.mortise_member (created_time DESC);
-- JSONB 字段 GIN 索引（用于 JSONB 查询）
CREATE INDEX IF NOT EXISTS idx_member_profile_gin ON mortise.mortise_member USING GIN (profile) WHERE profile IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_member_preferences_gin ON mortise.mortise_member USING GIN (preferences) WHERE preferences IS NOT NULL;

-- 更新时间触发器
DROP TRIGGER IF EXISTS update_mortise_member_updated_time ON mortise.mortise_member;
CREATE TRIGGER update_mortise_member_updated_time
    BEFORE UPDATE
    ON mortise.mortise_member
    FOR EACH ROW EXECUTE FUNCTION update_updated_time_column();
