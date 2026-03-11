CREATE TABLE IF NOT EXISTS mortise.mortise_badge
(
    id               BIGINT PRIMARY KEY,
    code             VARCHAR(100) NOT NULL,
    name             VARCHAR(100) NOT NULL,
    icon             VARCHAR(120),
    description      VARCHAR(255),
    condition_type   VARCHAR(100),
    condition_value  VARCHAR(100),
    created_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag         INTEGER   DEFAULT 0
);

CREATE TABLE IF NOT EXISTS mortise.mortise_user_badge
(
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT    NOT NULL REFERENCES mortise.mortise_member (id),
    badge_id      BIGINT    NOT NULL REFERENCES mortise.mortise_badge (id),
    source_type   VARCHAR(100),
    source_id     BIGINT,
    earned_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag      INTEGER   DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_badge IS '徽章定义表';
COMMENT ON TABLE mortise.mortise_user_badge IS '用户徽章表';

CREATE UNIQUE INDEX IF NOT EXISTS uk_mortise_badge_code
    ON mortise.mortise_badge (code)
    WHERE del_flag = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_mortise_user_badge_user_badge
    ON mortise.mortise_user_badge (user_id, badge_id)
    WHERE del_flag = 0;

CREATE INDEX IF NOT EXISTS idx_mortise_user_badge_user_time
    ON mortise.mortise_user_badge (user_id, earned_time DESC);

INSERT INTO mortise.mortise_badge
    (id, code, name, icon, description, condition_type, condition_value, created_time, updated_time, del_flag)
VALUES
    (900001, 'community_first_article', '首篇文章', 'i-lucide-file-pen-line', '发布第一篇社区文章。', 'article_publish_count', '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (900002, 'community_five_articles', '持续创作', 'i-lucide-notebook-pen', '累计发布 5 篇社区文章。', 'article_publish_count', '5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (900003, 'community_first_comment', '首条评论', 'i-lucide-message-circle-more', '在社区发表第一条评论。', 'comment_publish_count', '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (900004, 'community_ten_comments', '对话参与者', 'i-lucide-messages-square', '累计发表 10 条社区评论。', 'comment_publish_count', '10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (900005, 'community_article_ten_likes', '10赞达人', 'i-lucide-heart', '单篇社区文章累计获得 10 个赞。', 'article_like_count', '10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (900006, 'community_author_hundred_likes', '百赞作者', 'i-lucide-sparkles', '社区文章累计获得 100 个赞。', 'author_total_likes', '100', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;
