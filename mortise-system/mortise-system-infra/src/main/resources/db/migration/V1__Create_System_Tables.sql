-- =============================================
-- Mortise System Module - Database Schema
-- PostgreSQL DDL Script
-- Author: ronger
-- Create Date: 2025-10-02
-- Description: 系统模块数据库表结构（用户、角色、菜单、字典、OAuth2绑定）
-- 注意: 主键ID由应用层 MyBatis-Flex flexId 生成，不使用数据库自增
-- Version Range: System V1-V19
-- =============================================

-- =============================================
-- 表1: mortise_user (用户表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_user
(
    id               BIGINT PRIMARY KEY, -- 由 MyBatis-Flex flexId 生成
    account          VARCHAR(50) NOT NULL UNIQUE,
    password         VARCHAR(255),
    nickname         VARCHAR(100),
    real_name        VARCHAR(100),
    avatar           VARCHAR(500),
    email            VARCHAR(100),
    phone            VARCHAR(20),
    status           INTEGER   DEFAULT 0,
    del_flag         INTEGER   DEFAULT 0,
    last_login_time  TIMESTAMP,
    created_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_online_time TIMESTAMP,
    CONSTRAINT uk_mortise_user_account UNIQUE (account)
);

COMMENT ON TABLE mortise.mortise_user IS '用户表';
COMMENT ON COLUMN mortise.mortise_user.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_user.account IS '登录账号';
COMMENT ON COLUMN mortise.mortise_user.password IS '密码';
COMMENT ON COLUMN mortise.mortise_user.nickname IS '昵称';
COMMENT ON COLUMN mortise.mortise_user.real_name IS '真实姓名';
COMMENT ON COLUMN mortise.mortise_user.avatar IS '头像路径';
COMMENT ON COLUMN mortise.mortise_user.email IS '邮箱地址';
COMMENT ON COLUMN mortise.mortise_user.phone IS '手机号码';
COMMENT ON COLUMN mortise.mortise_user.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_user.del_flag IS '删除标记: 0-未删除, 1-已删除';
COMMENT ON COLUMN mortise.mortise_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN mortise.mortise_user.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_user.last_online_time IS '最后在线时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_mortise_user_account ON mortise.mortise_user (account);
CREATE INDEX IF NOT EXISTS idx_mortise_user_email ON mortise.mortise_user (email);
CREATE INDEX IF NOT EXISTS idx_mortise_user_status ON mortise.mortise_user (status);
CREATE INDEX IF NOT EXISTS idx_mortise_user_del_flag ON mortise.mortise_user (del_flag);

-- =============================================
-- 表2: mortise_role (角色表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_role
(
    id           BIGINT PRIMARY KEY,
    label        VARCHAR(100) NOT NULL,
    permission   VARCHAR(200) NOT NULL UNIQUE,
    status       INTEGER   DEFAULT 0,
    is_default   INTEGER   DEFAULT 0,
    del_flag     INTEGER   DEFAULT 0,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_mortise_role_permission UNIQUE (permission)
);

COMMENT ON TABLE mortise.mortise_role IS '角色表';
COMMENT ON COLUMN mortise.mortise_role.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_role.label IS '角色名称';
COMMENT ON COLUMN mortise.mortise_role.permission IS '角色权限标识';
COMMENT ON COLUMN mortise.mortise_role.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_role.is_default IS '是否为默认角色: 0-否, 1-是';
COMMENT ON COLUMN mortise.mortise_role.del_flag IS '删除标记: 0-未删除, 1-已删除';
COMMENT ON COLUMN mortise.mortise_role.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_role.updated_time IS '更新时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_mortise_role_permission ON mortise.mortise_role (permission);
CREATE INDEX IF NOT EXISTS idx_mortise_role_status ON mortise.mortise_role (status);
CREATE INDEX IF NOT EXISTS idx_mortise_role_del_flag ON mortise.mortise_role (del_flag);

-- =============================================
-- 表3: mortise_menu (菜单表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_menu
(
    id           BIGINT PRIMARY KEY,
    label        VARCHAR(100) NOT NULL,
    permission   VARCHAR(200),
    icon         VARCHAR(100),
    href         VARCHAR(500),
    status       INTEGER   DEFAULT 0,
    del_flag     INTEGER   DEFAULT 0,
    menu_type    INTEGER   DEFAULT 0,
    sort_no      INTEGER   DEFAULT 0,
    parent_id    BIGINT    DEFAULT 0,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mortise.mortise_menu IS '菜单表';
COMMENT ON COLUMN mortise.mortise_menu.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_menu.label IS '菜单名称';
COMMENT ON COLUMN mortise.mortise_menu.permission IS '菜单权限标识';
COMMENT ON COLUMN mortise.mortise_menu.icon IS '菜单图标';
COMMENT ON COLUMN mortise.mortise_menu.href IS '菜单链接';
COMMENT ON COLUMN mortise.mortise_menu.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_menu.del_flag IS '删除标记: 0-未删除, 1-已删除';
COMMENT ON COLUMN mortise.mortise_menu.menu_type IS '菜单类型: 0-目录, 1-菜单, 2-按钮';
COMMENT ON COLUMN mortise.mortise_menu.sort_no IS '排序号';
COMMENT ON COLUMN mortise.mortise_menu.parent_id IS '父级菜单ID';
COMMENT ON COLUMN mortise.mortise_menu.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_menu.updated_time IS '更新时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_mortise_menu_parent_id ON mortise.mortise_menu (parent_id);
CREATE INDEX IF NOT EXISTS idx_mortise_menu_status ON mortise.mortise_menu (status);
CREATE INDEX IF NOT EXISTS idx_mortise_menu_del_flag ON mortise.mortise_menu (del_flag);
CREATE INDEX IF NOT EXISTS idx_mortise_menu_sort_no ON mortise.mortise_menu (sort_no);

-- =============================================
-- 表4: mortise_user_role (用户角色关联表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_user_role
(
    id_mortise_user BIGINT NOT NULL,
    id_mortise_role BIGINT NOT NULL,
    PRIMARY KEY (id_mortise_user, id_mortise_role),
    CONSTRAINT fk_user_role_user FOREIGN KEY (id_mortise_user) REFERENCES mortise.mortise_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (id_mortise_role) REFERENCES mortise.mortise_role (id) ON DELETE CASCADE
);

COMMENT ON TABLE mortise.mortise_user_role IS '用户角色关联表';
COMMENT ON COLUMN mortise.mortise_user_role.id_mortise_user IS '用户ID';
COMMENT ON COLUMN mortise.mortise_user_role.id_mortise_role IS '角色ID';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_mortise_user_role_user ON mortise.mortise_user_role (id_mortise_user);
CREATE INDEX IF NOT EXISTS idx_mortise_user_role_role ON mortise.mortise_user_role (id_mortise_role);

-- =============================================
-- 表5: mortise_role_menu (角色菜单关联表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_role_menu
(
    id_mortise_role BIGINT NOT NULL,
    id_mortise_menu BIGINT NOT NULL,
    PRIMARY KEY (id_mortise_role, id_mortise_menu),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (id_mortise_role) REFERENCES mortise.mortise_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (id_mortise_menu) REFERENCES mortise.mortise_menu (id) ON DELETE CASCADE
);

COMMENT ON TABLE mortise.mortise_role_menu IS '角色菜单关联表';
COMMENT ON COLUMN mortise.mortise_role_menu.id_mortise_role IS '角色ID';
COMMENT ON COLUMN mortise.mortise_role_menu.id_mortise_menu IS '菜单ID';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_mortise_role_menu_role ON mortise.mortise_role_menu (id_mortise_role);
CREATE INDEX IF NOT EXISTS idx_mortise_role_menu_menu ON mortise.mortise_role_menu (id_mortise_menu);

-- =============================================
-- 表6: mortise_dict_type (字典类型表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_dict_type
(
    id           BIGINT PRIMARY KEY,
    label        VARCHAR(100) NOT NULL,
    type_code    VARCHAR(50)  NOT NULL UNIQUE,
    description  VARCHAR(500),
    sort_no      INTEGER   DEFAULT 0,
    status       INTEGER   DEFAULT 0,
    del_flag     INTEGER   DEFAULT 0,
    created_by   BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by   BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_mortise_dict_type_code UNIQUE (type_code)
);

COMMENT ON TABLE mortise.mortise_dict_type IS '字典类型表';
COMMENT ON COLUMN mortise.mortise_dict_type.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_dict_type.label IS '类型名称';
COMMENT ON COLUMN mortise.mortise_dict_type.type_code IS '类型编码';
COMMENT ON COLUMN mortise.mortise_dict_type.description IS '描述';
COMMENT ON COLUMN mortise.mortise_dict_type.sort_no IS '排序号';
COMMENT ON COLUMN mortise.mortise_dict_type.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_dict_type.del_flag IS '删除标记: 0-未删除, 1-已删除';
COMMENT ON COLUMN mortise.mortise_dict_type.created_by IS '创建人ID';
COMMENT ON COLUMN mortise.mortise_dict_type.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_dict_type.updated_by IS '更新人ID';
COMMENT ON COLUMN mortise.mortise_dict_type.updated_time IS '更新时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_mortise_dict_type_code ON mortise.mortise_dict_type (type_code);
CREATE INDEX IF NOT EXISTS idx_mortise_dict_type_status ON mortise.mortise_dict_type (status);
CREATE INDEX IF NOT EXISTS idx_mortise_dict_type_del_flag ON mortise.mortise_dict_type (del_flag);

-- =============================================
-- 表7: mortise_dict (字典数据表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_dict
(
    id             BIGINT PRIMARY KEY,
    dict_type_code VARCHAR(50)  NOT NULL,
    label          VARCHAR(100) NOT NULL,
    value          VARCHAR(100) NOT NULL,
    sort_no        INTEGER   DEFAULT 0,
    status         INTEGER   DEFAULT 0,
    del_flag       INTEGER   DEFAULT 0,
    created_by     BIGINT,
    created_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by     BIGINT,
    updated_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    icon           VARCHAR(100),
    image          VARCHAR(500),
    color          VARCHAR(50),
    CONSTRAINT fk_dict_type_code FOREIGN KEY (dict_type_code) REFERENCES mortise.mortise_dict_type (type_code) ON DELETE CASCADE
);

COMMENT ON TABLE mortise.mortise_dict IS '字典数据表';
COMMENT ON COLUMN mortise.mortise_dict.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_dict.dict_type_code IS '字典类型编码';
COMMENT ON COLUMN mortise.mortise_dict.label IS '字典标签';
COMMENT ON COLUMN mortise.mortise_dict.value IS '字典值';
COMMENT ON COLUMN mortise.mortise_dict.sort_no IS '排序号';
COMMENT ON COLUMN mortise.mortise_dict.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN mortise.mortise_dict.del_flag IS '删除标记: 0-未删除, 1-已删除';
COMMENT ON COLUMN mortise.mortise_dict.created_by IS '创建人ID';
COMMENT ON COLUMN mortise.mortise_dict.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_dict.updated_by IS '更新人ID';
COMMENT ON COLUMN mortise.mortise_dict.updated_time IS '更新时间';
COMMENT ON COLUMN mortise.mortise_dict.icon IS '图标';
COMMENT ON COLUMN mortise.mortise_dict.image IS '图片';
COMMENT ON COLUMN mortise.mortise_dict.color IS 'Chip颜色';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_mortise_dict_dict_type_code ON mortise.mortise_dict (dict_type_code);
CREATE INDEX IF NOT EXISTS idx_mortise_dict_status ON mortise.mortise_dict (status);
CREATE INDEX IF NOT EXISTS idx_mortise_dict_del_flag ON mortise.mortise_dict (del_flag);
CREATE INDEX IF NOT EXISTS idx_mortise_dict_sort_no ON mortise.mortise_dict (sort_no);

-- =============================================
-- 表8: mortise_user_oauth2_binding (用户OAuth2绑定表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_user_oauth2_binding
(
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    provider      VARCHAR(50)  NOT NULL,
    open_id       VARCHAR(255) NOT NULL,
    union_id      VARCHAR(255),
    nickname      VARCHAR(100),
    avatar        VARCHAR(500),
    email         VARCHAR(100),
    access_token  TEXT,
    refresh_token TEXT,
    expires_at    TIMESTAMP,
    raw_data      TEXT,
    created_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_provider_openid UNIQUE (provider, open_id)
);

COMMENT ON TABLE mortise.mortise_user_oauth2_binding IS '用户OAuth2绑定表';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.id IS '主键ID';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.user_id IS '用户ID';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.provider IS 'OAuth2 提供商 (github, google, wechat, logto)';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.open_id IS 'OAuth2 提供商的用户唯一标识';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.union_id IS '微信 UnionID（仅微信开放平台）';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.nickname IS 'OAuth2 提供商返回的昵称';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.avatar IS 'OAuth2 提供商返回的头像';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.email IS 'OAuth2 提供商返回的邮箱';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.access_token IS '访问令牌';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.refresh_token IS '刷新令牌';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.expires_at IS '令牌过期时间';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.raw_data IS '原始用户数据 (JSON)';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_user_oauth2_binding.updated_time IS '更新时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_id ON mortise.mortise_user_oauth2_binding (user_id);
CREATE INDEX IF NOT EXISTS idx_provider ON mortise.mortise_user_oauth2_binding (provider);
CREATE INDEX IF NOT EXISTS idx_provider_unionid ON mortise.mortise_user_oauth2_binding (provider, union_id);

-- =============================================
-- 创建更新时间触发器函数（全局共用）
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_time_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 创建触发器
DROP TRIGGER IF EXISTS update_mortise_user_oauth2_binding_updated_time ON mortise.mortise_user_oauth2_binding;
CREATE TRIGGER update_mortise_user_oauth2_binding_updated_time
    BEFORE UPDATE
    ON mortise.mortise_user_oauth2_binding
    FOR EACH ROW EXECUTE FUNCTION update_updated_time_column();
