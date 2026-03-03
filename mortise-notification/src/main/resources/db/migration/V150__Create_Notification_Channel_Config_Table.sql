-- 通知渠道配置表
-- 每行对应一个渠道（channel）下的一个配置项（config_key → config_value）
-- API 层负责将同一渠道的多行聚合为整体 VO 返回给前端
CREATE TABLE IF NOT EXISTS mortise.mortise_notification_channel_config
(
    id           BIGINT       PRIMARY KEY,
    channel      VARCHAR(32)  NOT NULL,
    config_key   VARCHAR(64)  NOT NULL,
    config_value TEXT,
    created_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP,
    CONSTRAINT uq_notification_channel_config UNIQUE (channel, config_key)
);

COMMENT ON TABLE  mortise.mortise_notification_channel_config               IS '通知渠道配置';
COMMENT ON COLUMN mortise.mortise_notification_channel_config.channel      IS '渠道标识，对应 NotificationType.code，如 email、sms、wechat';
COMMENT ON COLUMN mortise.mortise_notification_channel_config.config_key   IS '配置项 key，如 host、port、username、password、ssl、from_name、enabled';
COMMENT ON COLUMN mortise.mortise_notification_channel_config.config_value IS '配置项值（密码类字段明文存储，接口层脱敏输出）';

CREATE INDEX IF NOT EXISTS idx_notification_channel_config_channel
    ON mortise.mortise_notification_channel_config (channel);
