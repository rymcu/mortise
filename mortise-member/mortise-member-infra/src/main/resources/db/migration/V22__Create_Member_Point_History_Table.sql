CREATE TABLE IF NOT EXISTS mortise.mortise_member_point_history
(
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES mortise.mortise_member (id),
    change_amount   INTEGER      NOT NULL,
    current_points  INTEGER      NOT NULL,
    biz_type        VARCHAR(50)  NOT NULL,
    biz_key         VARCHAR(120) NOT NULL,
    biz_id          BIGINT,
    reason          VARCHAR(255),
    created_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    del_flag        INTEGER      DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_member_point_history IS '会员积分历史表';
COMMENT ON COLUMN mortise.mortise_member_point_history.user_id IS '用户 ID';
COMMENT ON COLUMN mortise.mortise_member_point_history.change_amount IS '积分变更值';
COMMENT ON COLUMN mortise.mortise_member_point_history.current_points IS '变更后积分';
COMMENT ON COLUMN mortise.mortise_member_point_history.biz_type IS '业务类型';
COMMENT ON COLUMN mortise.mortise_member_point_history.biz_key IS '业务幂等键';
COMMENT ON COLUMN mortise.mortise_member_point_history.biz_id IS '业务 ID';
COMMENT ON COLUMN mortise.mortise_member_point_history.reason IS '变更原因';

CREATE INDEX IF NOT EXISTS idx_member_point_history_user_time
    ON mortise.mortise_member_point_history (user_id, created_time DESC);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_point_history_user_biz_key
    ON mortise.mortise_member_point_history (user_id, biz_key)
    WHERE del_flag = 0;
