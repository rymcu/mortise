-- Flyway Migration for File Module
-- 创建文件记录表和文件分片表

-- 文件记录表
CREATE TABLE IF NOT EXISTS mortise_file_detail
(
    id                  BIGINT PRIMARY KEY,
    url                 VARCHAR(512)  NOT NULL,
    size                BIGINT,
    filename            VARCHAR(256),
    original_filename   VARCHAR(256),
    base_path           VARCHAR(256),
    path                VARCHAR(256),
    ext                 VARCHAR(32),
    content_type        VARCHAR(128),
    platform            VARCHAR(32),
    th_url              VARCHAR(512),
    th_filename         VARCHAR(256),
    th_size             BIGINT,
    th_content_type     VARCHAR(128),
    object_id           VARCHAR(64),
    object_type         VARCHAR(64),
    metadata            TEXT,
    user_metadata       TEXT,
    th_metadata         TEXT,
    th_user_metadata    TEXT,
    attr                TEXT,
    file_acl            VARCHAR(32),
    th_file_acl         VARCHAR(32),
    hash_info           TEXT,
    upload_id           VARCHAR(128),
    upload_status       INT,
    create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_url ON mortise_file_detail (url);

COMMENT ON TABLE mortise_file_detail IS '文件记录表';
COMMENT ON COLUMN mortise_file_detail.id IS '主键';
COMMENT ON COLUMN mortise_file_detail.url IS '文件访问地址';
COMMENT ON COLUMN mortise_file_detail.size IS '文件大小，单位字节';
COMMENT ON COLUMN mortise_file_detail.filename IS '文件名称';
COMMENT ON COLUMN mortise_file_detail.original_filename IS '原始文件名';
COMMENT ON COLUMN mortise_file_detail.base_path IS '基础存储路径';
COMMENT ON COLUMN mortise_file_detail.path IS '存储路径';
COMMENT ON COLUMN mortise_file_detail.ext IS '文件扩展名';
COMMENT ON COLUMN mortise_file_detail.content_type IS 'MIME类型';
COMMENT ON COLUMN mortise_file_detail.platform IS '存储平台';
COMMENT ON COLUMN mortise_file_detail.th_url IS '缩略图访问路径';
COMMENT ON COLUMN mortise_file_detail.th_filename IS '缩略图名称';
COMMENT ON COLUMN mortise_file_detail.th_size IS '缩略图大小，单位字节';
COMMENT ON COLUMN mortise_file_detail.th_content_type IS '缩略图MIME类型';
COMMENT ON COLUMN mortise_file_detail.object_id IS '文件所属对象id';
COMMENT ON COLUMN mortise_file_detail.object_type IS '文件所属对象类型';
COMMENT ON COLUMN mortise_file_detail.metadata IS '文件元数据';
COMMENT ON COLUMN mortise_file_detail.user_metadata IS '文件用户元数据';
COMMENT ON COLUMN mortise_file_detail.th_metadata IS '缩略图元数据';
COMMENT ON COLUMN mortise_file_detail.th_user_metadata IS '缩略图用户元数据';
COMMENT ON COLUMN mortise_file_detail.attr IS '附加属性';
COMMENT ON COLUMN mortise_file_detail.file_acl IS '文件ACL';
COMMENT ON COLUMN mortise_file_detail.th_file_acl IS '缩略图文件ACL';
COMMENT ON COLUMN mortise_file_detail.hash_info IS '哈希信息';
COMMENT ON COLUMN mortise_file_detail.upload_id IS '上传ID，仅在手动分片上传时使用';
COMMENT ON COLUMN mortise_file_detail.upload_status IS '上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成';
COMMENT ON COLUMN mortise_file_detail.create_time IS '创建时间';

-- 文件分片信息表
CREATE TABLE IF NOT EXISTS mortise_file_part_detail
(
    id           BIGINT PRIMARY KEY,
    platform     VARCHAR(32),
    upload_id    VARCHAR(128),
    e_tag        VARCHAR(256),
    part_number  INT,
    part_size    BIGINT,
    hash_info    TEXT,
    create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_upload_id ON mortise_file_part_detail (upload_id);

COMMENT ON TABLE mortise_file_part_detail IS '文件分片信息表，仅在手动分片上传时使用';
COMMENT ON COLUMN mortise_file_part_detail.id IS '主键';
COMMENT ON COLUMN mortise_file_part_detail.platform IS '存储平台';
COMMENT ON COLUMN mortise_file_part_detail.upload_id IS '上传ID';
COMMENT ON COLUMN mortise_file_part_detail.e_tag IS '分片 ETag';
COMMENT ON COLUMN mortise_file_part_detail.part_number IS '分片号';
COMMENT ON COLUMN mortise_file_part_detail.part_size IS '文件大小，单位字节';
COMMENT ON COLUMN mortise_file_part_detail.hash_info IS '哈希信息';
COMMENT ON COLUMN mortise_file_part_detail.create_time IS '创建时间';
