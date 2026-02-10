-- =============================================
-- Mortise Log Module - Migrate ID to BIGINT (flexId)
-- PostgreSQL Flyway Migration
--
-- Strategy:
-- 1) Drop existing log tables (including any *_ulid leftovers)
-- 2) Create new tables with BIGINT primary keys (to match KeyGenerators.flexId)
--
-- Notes:
-- - This migration is destructive: it does NOT preserve existing log data.
-- =============================================

-- Drop tables first (CASCADE will drop related indexes)
DROP TABLE IF EXISTS mortise.mortise_operation_log_ulid CASCADE;
DROP TABLE IF EXISTS mortise.mortise_api_log_ulid CASCADE;

DROP TABLE IF EXISTS mortise.mortise_operation_log CASCADE;
DROP TABLE IF EXISTS mortise.mortise_api_log CASCADE;

-- =============================================
-- 表1: mortise_operation_log (操作日志表) - BIGINT ID
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

-- Indexes
CREATE INDEX IF NOT EXISTS idx_operation_log_trace_id ON mortise.mortise_operation_log (trace_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_client_type ON mortise.mortise_operation_log (client_type);
CREATE INDEX IF NOT EXISTS idx_operation_log_module ON mortise.mortise_operation_log (module);
CREATE INDEX IF NOT EXISTS idx_operation_log_operator_id ON mortise.mortise_operation_log (operator_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_operator_account ON mortise.mortise_operation_log (operator_account);
CREATE INDEX IF NOT EXISTS idx_operation_log_operate_time ON mortise.mortise_operation_log (operate_time);
CREATE INDEX IF NOT EXISTS idx_operation_log_success ON mortise.mortise_operation_log (success);
CREATE INDEX IF NOT EXISTS idx_operation_log_created_time ON mortise.mortise_operation_log (created_time);

-- =============================================
-- 表2: mortise_api_log (API日志表) - BIGINT ID
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

-- Indexes
CREATE INDEX IF NOT EXISTS idx_api_log_trace_id ON mortise.mortise_api_log (trace_id);
CREATE INDEX IF NOT EXISTS idx_api_log_client_type ON mortise.mortise_api_log (client_type);
CREATE INDEX IF NOT EXISTS idx_api_log_user_id ON mortise.mortise_api_log (user_id);
CREATE INDEX IF NOT EXISTS idx_api_log_username ON mortise.mortise_api_log (username);
CREATE INDEX IF NOT EXISTS idx_api_log_request_uri ON mortise.mortise_api_log (request_uri);
CREATE INDEX IF NOT EXISTS idx_api_log_request_time ON mortise.mortise_api_log (request_time);
CREATE INDEX IF NOT EXISTS idx_api_log_http_status ON mortise.mortise_api_log (http_status);
CREATE INDEX IF NOT EXISTS idx_api_log_success ON mortise.mortise_api_log (success);
CREATE INDEX IF NOT EXISTS idx_api_log_created_time ON mortise.mortise_api_log (created_time);
