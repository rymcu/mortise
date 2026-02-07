-- 会员表
CREATE TABLE IF NOT EXISTS mortise_member
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
    referrer_id         BIGINT REFERENCES mortise_member (id),
    last_login_time     TIMESTAMP,
    email_verified_time TIMESTAMP,
    phone_verified_time TIMESTAMP,
    profile             JSONB,
    preferences         JSONB,
    created_time        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_time        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    del_flag            INTEGER        DEFAULT 0
);

COMMENT ON TABLE mortise_member IS '会员表';
COMMENT ON COLUMN mortise_member.id IS '主键ID，由应用程序生成';
COMMENT ON COLUMN mortise_member.username IS '用户名';
COMMENT ON COLUMN mortise_member.email IS '邮箱';
COMMENT ON COLUMN mortise_member.phone IS '手机号';
COMMENT ON COLUMN mortise_member.password_hash IS '密码哈希';
COMMENT ON COLUMN mortise_member.name IS '真实姓名';
COMMENT ON COLUMN mortise_member.nickname IS '昵称';
COMMENT ON COLUMN mortise_member.avatar_url IS '头像URL';
COMMENT ON COLUMN mortise_member.gender IS '性别：male-男, female-女, other-其他';
COMMENT ON COLUMN mortise_member.birth_date IS '出生日期';
COMMENT ON COLUMN mortise_member.status IS '状态：0-正常, 1-禁用';
COMMENT ON COLUMN mortise_member.member_level IS '会员等级';
COMMENT ON COLUMN mortise_member.points IS '积分';
COMMENT ON COLUMN mortise_member.balance IS '账户余额';
COMMENT ON COLUMN mortise_member.register_source IS '注册来源';
COMMENT ON COLUMN mortise_member.referrer_id IS '推荐人ID';
COMMENT ON COLUMN mortise_member.last_login_time IS '最后登录时间';
COMMENT ON COLUMN mortise_member.email_verified_time IS '邮箱验证时间';
COMMENT ON COLUMN mortise_member.phone_verified_time IS '手机号验证时间';
COMMENT ON COLUMN mortise_member.profile IS '扩展资料';
COMMENT ON COLUMN mortise_member.preferences IS '个人偏好设置';
COMMENT ON COLUMN mortise_member.created_time IS '创建时间';
COMMENT ON COLUMN mortise_member.updated_time IS '更新时间';
COMMENT ON COLUMN mortise_member.del_flag IS '删除标记：0-未删除, 1-已删除';

CREATE INDEX idx_member_email ON mortise_member (email) WHERE del_flag = 0;
CREATE INDEX idx_member_phone ON mortise_member (phone) WHERE del_flag = 0;
CREATE INDEX idx_member_status ON mortise_member (status) WHERE del_flag = 0;
CREATE INDEX idx_member_level ON mortise_member (member_level);
CREATE INDEX idx_member_referrer ON mortise_member (referrer_id);
-- 复合索引用于会员查询
CREATE INDEX idx_member_status_level ON mortise_member (status, member_level, del_flag);
CREATE INDEX idx_member_created ON mortise_member (created_time DESC);
-- JSONB 字段 GIN 索引（用于 JSONB 查询）
CREATE INDEX idx_member_profile_gin ON mortise_member USING GIN (profile) WHERE profile IS NOT NULL;
CREATE INDEX idx_member_preferences_gin ON mortise_member USING GIN (preferences) WHERE preferences IS NOT NULL;
