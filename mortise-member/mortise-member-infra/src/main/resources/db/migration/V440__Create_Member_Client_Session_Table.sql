CREATE TABLE IF NOT EXISTS mortise.mortise_member_client_session
(
    id                      BIGINT PRIMARY KEY,
    member_id               BIGINT       NOT NULL REFERENCES mortise.mortise_member (id),
    client_id               VARCHAR(64)  NOT NULL,
    device_name             VARCHAR(120),
    device_fingerprint_hash VARCHAR(128),
    status                  VARCHAR(20)  NOT NULL DEFAULT 'active',
    last_active_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at              TIMESTAMP,
    created_time            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag                INTEGER      NOT NULL DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_member_client_session IS '会员客户端会话表';
COMMENT ON COLUMN mortise.mortise_member_client_session.member_id IS '会员ID';
COMMENT ON COLUMN mortise.mortise_member_client_session.client_id IS '客户端ID，如 rodak-desktop';
COMMENT ON COLUMN mortise.mortise_member_client_session.device_name IS '客户端设备名称';
COMMENT ON COLUMN mortise.mortise_member_client_session.device_fingerprint_hash IS '设备指纹哈希';
COMMENT ON COLUMN mortise.mortise_member_client_session.status IS '会话状态：active/revoked';
COMMENT ON COLUMN mortise.mortise_member_client_session.last_active_at IS '最近活跃时间';
COMMENT ON COLUMN mortise.mortise_member_client_session.revoked_at IS '撤销时间';
COMMENT ON COLUMN mortise.mortise_member_client_session.del_flag IS '删除标记：0-未删除, 1-已删除';

CREATE INDEX IF NOT EXISTS idx_member_client_session_member
    ON mortise.mortise_member_client_session (member_id, status)
    WHERE del_flag = 0;

CREATE INDEX IF NOT EXISTS idx_member_client_session_client
    ON mortise.mortise_member_client_session (client_id, status)
    WHERE del_flag = 0;

DROP TRIGGER IF EXISTS update_mortise_member_client_session_updated_time
    ON mortise.mortise_member_client_session;
CREATE TRIGGER update_mortise_member_client_session_updated_time
    BEFORE UPDATE
    ON mortise.mortise_member_client_session
    FOR EACH ROW EXECUTE FUNCTION update_updated_time_column();
