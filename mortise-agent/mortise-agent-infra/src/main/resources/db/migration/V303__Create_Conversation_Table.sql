-- 会话表：记录用户与 Agent 的对话会话
CREATE TABLE IF NOT EXISTS mortise.mortise_conversation
(
    id           BIGINT       NOT NULL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    title        VARCHAR(200),
    status       SMALLINT     NOT NULL DEFAULT 1,
    del_flag     SMALLINT     NOT NULL DEFAULT 0,
    created_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  mortise.mortise_conversation              IS 'Agent 会话表';
COMMENT ON COLUMN mortise.mortise_conversation.id           IS '会话 ID';
COMMENT ON COLUMN mortise.mortise_conversation.user_id      IS '所属用户 ID';
COMMENT ON COLUMN mortise.mortise_conversation.title        IS '会话标题';
COMMENT ON COLUMN mortise.mortise_conversation.status       IS '状态：0=归档, 1=活跃';
COMMENT ON COLUMN mortise.mortise_conversation.del_flag     IS '逻辑删除：0=正常, 1=已删除';
COMMENT ON COLUMN mortise.mortise_conversation.created_time IS '创建时间';
COMMENT ON COLUMN mortise.mortise_conversation.updated_time IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_conversation_user_id ON mortise.mortise_conversation (user_id);
