-- 会员域旧库导入脚本
-- 用途：
--   1. 从旧库 staging 数据库导入会员与 OAuth2 绑定数据
--   2. 只负责搬运旧值，不做状态翻转
--   3. 状态语义修正请继续执行 legacy-status-remap.sql
--
-- 运行前提：
--   1. 当前连接数据库应为新架构目标库
--   2. 通过 psql 变量 source_conn 传入 dblink 连接串
--   3. 目标库已完成 Flyway 初始化

BEGIN;

CREATE EXTENSION IF NOT EXISTS dblink;

WITH legacy_member AS (
    SELECT *
    FROM dblink(:'source_conn', $db$
        SELECT id,
               username,
               email,
               phone,
               password_hash,
               name,
               nickname,
               avatar_url,
               gender,
               birth_date,
               status,
               member_level,
               points,
               balance,
               register_source,
               referrer_id,
               last_login_time,
               email_verified_time,
               phone_verified_time,
               profile,
               preferences,
               created_time,
               updated_time,
               del_flag
        FROM mortise.mortise_member
    $db$) AS t(
        id BIGINT,
        username VARCHAR(50),
        email VARCHAR(100),
        phone VARCHAR(20),
        password_hash VARCHAR(255),
        name VARCHAR(100),
        nickname VARCHAR(50),
        avatar_url VARCHAR(500),
        gender VARCHAR(10),
        birth_date DATE,
        status INTEGER,
        member_level VARCHAR(20),
        points INTEGER,
        balance NUMERIC(10, 2),
        register_source VARCHAR(50),
        referrer_id BIGINT,
        last_login_time TIMESTAMP,
        email_verified_time TIMESTAMP,
        phone_verified_time TIMESTAMP,
        profile JSONB,
        preferences JSONB,
        created_time TIMESTAMP,
        updated_time TIMESTAMP,
        del_flag INTEGER
    )
)
INSERT INTO mortise.mortise_member (
    id,
    username,
    email,
    phone,
    password_hash,
    name,
    nickname,
    avatar_url,
    gender,
    birth_date,
    status,
    member_level,
    points,
    balance,
    register_source,
    referrer_id,
    last_login_time,
    email_verified_time,
    phone_verified_time,
    profile,
    preferences,
    created_time,
    updated_time,
    del_flag,
    current_family_id
)
SELECT id,
       username,
       email,
       phone,
       password_hash,
       name,
       nickname,
       avatar_url,
       gender,
       birth_date,
       COALESCE(status, 0),
       COALESCE(member_level, 'normal'),
       COALESCE(points, 0),
       COALESCE(balance, 0.00),
       register_source,
       referrer_id,
       last_login_time,
       email_verified_time,
       phone_verified_time,
       profile,
       preferences,
       COALESCE(created_time, CURRENT_TIMESTAMP),
       COALESCE(updated_time, COALESCE(created_time, CURRENT_TIMESTAMP)),
       COALESCE(del_flag, 0),
       NULL::BIGINT
FROM legacy_member
ON CONFLICT (id) DO UPDATE
SET username = EXCLUDED.username,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    password_hash = EXCLUDED.password_hash,
    name = EXCLUDED.name,
    nickname = EXCLUDED.nickname,
    avatar_url = EXCLUDED.avatar_url,
    gender = EXCLUDED.gender,
    birth_date = EXCLUDED.birth_date,
    status = EXCLUDED.status,
    member_level = EXCLUDED.member_level,
    points = EXCLUDED.points,
    balance = EXCLUDED.balance,
    register_source = EXCLUDED.register_source,
    referrer_id = EXCLUDED.referrer_id,
    last_login_time = EXCLUDED.last_login_time,
    email_verified_time = EXCLUDED.email_verified_time,
    phone_verified_time = EXCLUDED.phone_verified_time,
    profile = EXCLUDED.profile,
    preferences = EXCLUDED.preferences,
    created_time = EXCLUDED.created_time,
    updated_time = EXCLUDED.updated_time,
    del_flag = EXCLUDED.del_flag;

WITH legacy_binding AS (
    SELECT *
    FROM dblink(:'source_conn', $db$
        SELECT id,
               member_id,
               provider,
               open_id,
               union_id,
               nickname,
               avatar,
               email,
               phone,
               gender,
               country,
               province,
               city,
               access_token,
               refresh_token,
               expires_at,
               raw_data,
               status,
               del_flag,
               created_time,
               updated_time
        FROM mortise.mortise_member_oauth2_binding
    $db$) AS t(
        id BIGINT,
        member_id BIGINT,
        provider VARCHAR(50),
        open_id VARCHAR(255),
        union_id VARCHAR(255),
        nickname VARCHAR(100),
        avatar VARCHAR(500),
        email VARCHAR(100),
        phone VARCHAR(20),
        gender VARCHAR(10),
        country VARCHAR(50),
        province VARCHAR(50),
        city VARCHAR(50),
        access_token TEXT,
        refresh_token TEXT,
        expires_at TIMESTAMP,
        raw_data TEXT,
        status INTEGER,
        del_flag INTEGER,
        created_time TIMESTAMP,
        updated_time TIMESTAMP
    )
)
INSERT INTO mortise.mortise_member_oauth2_binding (
    id,
    member_id,
    provider,
    open_id,
    union_id,
    nickname,
    avatar,
    email,
    phone,
    gender,
    country,
    province,
    city,
    access_token,
    refresh_token,
    expires_at,
    raw_data,
    status,
    del_flag,
    created_time,
    updated_time
)
SELECT b.id,
       b.member_id,
       b.provider,
       b.open_id,
       b.union_id,
       b.nickname,
       b.avatar,
       b.email,
       b.phone,
       b.gender,
       b.country,
       b.province,
       b.city,
       b.access_token,
       b.refresh_token,
       b.expires_at,
       b.raw_data,
       COALESCE(b.status, 0),
       COALESCE(b.del_flag, 0),
       COALESCE(b.created_time, CURRENT_TIMESTAMP),
       COALESCE(b.updated_time, COALESCE(b.created_time, CURRENT_TIMESTAMP))
FROM legacy_binding b
WHERE EXISTS (
    SELECT 1
    FROM mortise.mortise_member m
    WHERE m.id = b.member_id
)
ON CONFLICT (id) DO UPDATE
SET member_id = EXCLUDED.member_id,
    provider = EXCLUDED.provider,
    open_id = EXCLUDED.open_id,
    union_id = EXCLUDED.union_id,
    nickname = EXCLUDED.nickname,
    avatar = EXCLUDED.avatar,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    gender = EXCLUDED.gender,
    country = EXCLUDED.country,
    province = EXCLUDED.province,
    city = EXCLUDED.city,
    access_token = EXCLUDED.access_token,
    refresh_token = EXCLUDED.refresh_token,
    expires_at = EXCLUDED.expires_at,
    raw_data = EXCLUDED.raw_data,
    status = EXCLUDED.status,
    del_flag = EXCLUDED.del_flag,
    created_time = EXCLUDED.created_time,
    updated_time = EXCLUDED.updated_time;

COMMIT;

SELECT 'mortise_member' AS table_name, count(*) AS row_count
FROM mortise.mortise_member
UNION ALL
SELECT 'mortise_member_oauth2_binding' AS table_name, count(*) AS row_count
FROM mortise.mortise_member_oauth2_binding
ORDER BY table_name;
