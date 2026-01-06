-- =============================================
-- Mortise Log Module - Database Schema
-- PostgreSQL DDL Script
-- Author: ronger
-- Create Date: 2026-01-06
-- Description: 日志模块数据库表结构
-- =============================================

-- =============================================
-- 表1: mortise_operation_log (操作日志表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_operation_log
(
    id               BIGINT PRIMARY KEY,
    trace_id         VARCHAR(64),
    client_type      VARCHAR(20),
    module           VARCHAR(100),
    operation        VARCHAR(200),
    operator_id      BIGINT,
    operator_account VARCHAR(100),
    operate_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    method           VARCHAR(500),
    request_uri      VARCHAR(500),
    request_method   VARCHAR(10),
    params           TEXT,
    result           TEXT,
    ip               VARCHAR(50),
    user_agent       VARCHAR(500),
    duration         BIGINT,
    success          BOOLEAN   DEFAULT TRUE,
    error_msg        TEXT,
    created_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mortise.mortise_operation_log IS '操作日志表';
COMMENT ON COLUMN mortise.mortise_operation_log.id IS '日志ID (flexId)';
COMMENT ON COLUMN mortise.mortise_operation_log.trace_id IS '链路追踪ID';
COMMENT ON COLUMN mortise.mortise_operation_log.client_type IS '客户端类型: system-后台管理, app-App端, web-Web端, api-开放API';
COMMENT ON COLUMN mortise.mortise_operation_log.module IS '模块名称';
COMMENT ON COLUMN mortise.mortise_operation_log.operation IS '操作类型';
COMMENT ON COLUMN mortise.mortise_operation_log.operator_id IS '操作人ID';
COMMENT ON COLUMN mortise.mortise_operation_log.operator_account IS '操作人账号';
COMMENT ON COLUMN mortise.mortise_operation_log.operate_time IS '操作时间';
COMMENT ON COLUMN mortise.mortise_operation_log.method IS '请求方法签名';
COMMENT ON COLUMN mortise.mortise_operation_log.request_uri IS '请求URI';
COMMENT ON COLUMN mortise.mortise_operation_log.request_method IS '请求方式 (GET/POST/PUT/DELETE)';
COMMENT ON COLUMN mortise.mortise_operation_log.params IS '请求参数 (JSON)';
COMMENT ON COLUMN mortise.mortise_operation_log.result IS '返回结果 (JSON)';
COMMENT ON COLUMN mortise.mortise_operation_log.ip IS 'IP地址';
COMMENT ON COLUMN mortise.mortise_operation_log.user_agent IS '用户代理';
COMMENT ON COLUMN mortise.mortise_operation_log.duration IS '执行耗时 (毫秒)';
COMMENT ON COLUMN mortise.mortise_operation_log.success IS '是否成功';
COMMENT ON COLUMN mortise.mortise_operation_log.error_msg IS '错误信息';
COMMENT ON COLUMN mortise.mortise_operation_log.created_time IS '创建时间';

-- 创建索引
CREATE INDEX idx_operation_log_trace_id ON mortise.mortise_operation_log (trace_id);
CREATE INDEX idx_operation_log_client_type ON mortise.mortise_operation_log (client_type);
CREATE INDEX idx_operation_log_module ON mortise.mortise_operation_log (module);
CREATE INDEX idx_operation_log_operator_id ON mortise.mortise_operation_log (operator_id);
CREATE INDEX idx_operation_log_operator_account ON mortise.mortise_operation_log (operator_account);
CREATE INDEX idx_operation_log_operate_time ON mortise.mortise_operation_log (operate_time);
CREATE INDEX idx_operation_log_success ON mortise.mortise_operation_log (success);
CREATE INDEX idx_operation_log_created_time ON mortise.mortise_operation_log (created_time);

-- =============================================
-- 表2: mortise_api_log (API日志表)
-- =============================================
CREATE TABLE IF NOT EXISTS mortise.mortise_api_log
(
    id               BIGINT PRIMARY KEY,
    trace_id         VARCHAR(64),
    client_type      VARCHAR(20),
    api_description  VARCHAR(500),
    class_name       VARCHAR(300),
    method_name      VARCHAR(100),
    user_id          BIGINT,
    username         VARCHAR(100),
    request_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    request_uri      VARCHAR(500),
    request_method   VARCHAR(10),
    query_string     TEXT,
    request_headers  TEXT,
    request_body     TEXT,
    response_body    TEXT,
    http_status      INTEGER,
    client_ip        VARCHAR(50),
    user_agent       VARCHAR(500),
    duration         BIGINT,
    success          BOOLEAN   DEFAULT TRUE,
    error_msg        TEXT,
    created_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mortise.mortise_api_log IS 'API日志表';
COMMENT ON COLUMN mortise.mortise_api_log.id IS '日志ID (flexId)';
COMMENT ON COLUMN mortise.mortise_api_log.trace_id IS '链路追踪ID';
COMMENT ON COLUMN mortise.mortise_api_log.client_type IS '客户端类型: system-后台管理, app-App端, web-Web端, api-开放API';
COMMENT ON COLUMN mortise.mortise_api_log.api_description IS 'API描述';
COMMENT ON COLUMN mortise.mortise_api_log.class_name IS '类名';
COMMENT ON COLUMN mortise.mortise_api_log.method_name IS '方法名';
COMMENT ON COLUMN mortise.mortise_api_log.user_id IS '用户ID';
COMMENT ON COLUMN mortise.mortise_api_log.username IS '用户名';
COMMENT ON COLUMN mortise.mortise_api_log.request_time IS '请求时间';
COMMENT ON COLUMN mortise.mortise_api_log.request_uri IS '请求URI';
COMMENT ON COLUMN mortise.mortise_api_log.request_method IS '请求方式';
COMMENT ON COLUMN mortise.mortise_api_log.query_string IS '查询参数';
COMMENT ON COLUMN mortise.mortise_api_log.request_headers IS '请求头 (JSON)';
COMMENT ON COLUMN mortise.mortise_api_log.request_body IS '请求体 (JSON)';
COMMENT ON COLUMN mortise.mortise_api_log.response_body IS '响应体 (JSON)';
COMMENT ON COLUMN mortise.mortise_api_log.http_status IS 'HTTP状态码';
COMMENT ON COLUMN mortise.mortise_api_log.client_ip IS '客户端IP';
COMMENT ON COLUMN mortise.mortise_api_log.user_agent IS '用户代理';
COMMENT ON COLUMN mortise.mortise_api_log.duration IS '执行耗时 (毫秒)';
COMMENT ON COLUMN mortise.mortise_api_log.success IS '是否成功';
COMMENT ON COLUMN mortise.mortise_api_log.error_msg IS '错误信息';
COMMENT ON COLUMN mortise.mortise_api_log.created_time IS '创建时间';

-- 创建索引
CREATE INDEX idx_api_log_trace_id ON mortise.mortise_api_log (trace_id);
CREATE INDEX idx_api_log_client_type ON mortise.mortise_api_log (client_type);
CREATE INDEX idx_api_log_user_id ON mortise.mortise_api_log (user_id);
CREATE INDEX idx_api_log_username ON mortise.mortise_api_log (username);
CREATE INDEX idx_api_log_request_uri ON mortise.mortise_api_log (request_uri);
CREATE INDEX idx_api_log_request_time ON mortise.mortise_api_log (request_time);
CREATE INDEX idx_api_log_http_status ON mortise.mortise_api_log (http_status);
CREATE INDEX idx_api_log_success ON mortise.mortise_api_log (success);
CREATE INDEX idx_api_log_created_time ON mortise.mortise_api_log (created_time);

-- =============================================
-- 分区表建议（可选，用于大数据量场景）
-- 如果日志量较大，建议按时间分区：
-- CREATE TABLE mortise.mortise_operation_log (
--     ...
-- ) PARTITION BY RANGE (created_time);
--
-- CREATE TABLE mortise.mortise_operation_log_2026_01
--     PARTITION OF mortise.mortise_operation_log
--     FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
-- =============================================
