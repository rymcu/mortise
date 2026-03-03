-- 系统全局配置表
-- 按组（config_group）存放各类网站信息配置，如 site（基本信息）、seo（SEO 设置）、footer（页脚配置）等。
-- schema（字段定义）由代码维护，不入库；本表仅存储各字段实际值。
CREATE TABLE IF NOT EXISTS mortise.mortise_system_config
(
    id           BIGINT       NOT NULL PRIMARY KEY,
    config_group VARCHAR(50)  NOT NULL DEFAULT 'site',
    config_key   VARCHAR(100) NOT NULL,
    config_value TEXT,
    created_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_system_config_key UNIQUE (config_key)
);

COMMENT ON TABLE mortise.mortise_system_config IS '系统全局配置表（网站信息、SEO、页脚等）';
COMMENT ON COLUMN mortise.mortise_system_config.config_group IS '配置分组：site=基本信息，seo=SEO 设置，footer=页脚配置';
COMMENT ON COLUMN mortise.mortise_system_config.config_key IS '配置项唯一标识，如 site.name、footer.icp';
COMMENT ON COLUMN mortise.mortise_system_config.config_value IS '配置项值（图片类存储文件相对路径）';

-- 初始化默认配置项（值为空，由管理员填写）
INSERT INTO mortise.mortise_system_config (id, config_group, config_key, config_value)
VALUES (1000000000000001, 'site',   'site.name',            'Mortise'),
       (1000000000000002, 'site',   'site.logo',            null),
       (1000000000000003, 'site',   'site.favicon',         null),
       (1000000000000004, 'site',   'site.description',     null),
       (1000000000000005, 'seo',    'seo.title_template',   '{page} - {site}'),
       (1000000000000006, 'seo',    'seo.keywords',         null),
       (1000000000000007, 'footer', 'footer.copyright',     null),
       (1000000000000008, 'footer', 'footer.icp',           null),
       (1000000000000009, 'footer', 'footer.icp_link',      null),
       (1000000000000010, 'footer', 'footer.gov_beian',     null),
       (1000000000000011, 'footer', 'footer.gov_link',      null),
       (1000000000000012, 'footer', 'footer.telecom',       null),
       (1000000000000013, 'footer', 'footer.telecom_link',       null)
ON CONFLICT (config_key) DO NOTHING;
