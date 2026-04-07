CREATE TABLE IF NOT EXISTS mortise.mortise_voice_provider (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(128) NOT NULL,
    provider_type VARCHAR(64) NOT NULL,
    status INTEGER NOT NULL DEFAULT 1,
    sort_no INTEGER NOT NULL DEFAULT 0,
    default_config TEXT,
    remark VARCHAR(500),
    del_flag INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_voice_provider_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_voice_provider_type_status
    ON mortise.mortise_voice_provider(provider_type, status);

CREATE TABLE IF NOT EXISTS mortise.mortise_voice_model (
    id BIGINT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(128) NOT NULL,
    capability VARCHAR(32) NOT NULL,
    model_type VARCHAR(64) NOT NULL,
    runtime_name VARCHAR(255),
    version VARCHAR(64),
    language VARCHAR(32),
    concurrency_limit INTEGER,
    is_default_model BOOLEAN NOT NULL DEFAULT FALSE,
    status INTEGER NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    del_flag INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_voice_model_code UNIQUE (code),
    CONSTRAINT fk_voice_model_provider FOREIGN KEY (provider_id)
        REFERENCES mortise.mortise_voice_provider(id)
);

CREATE INDEX IF NOT EXISTS idx_voice_model_provider_status
    ON mortise.mortise_voice_model(provider_id, status);

CREATE TABLE IF NOT EXISTS mortise.mortise_voice_profile (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(128) NOT NULL,
    language VARCHAR(32),
    asr_provider_id BIGINT,
    asr_model_id BIGINT,
    vad_provider_id BIGINT,
    vad_model_id BIGINT,
    tts_provider_id BIGINT,
    tts_model_id BIGINT,
    default_params TEXT,
    status INTEGER NOT NULL DEFAULT 1,
    sort_no INTEGER NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    del_flag INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_voice_profile_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_voice_profile_status_sort
    ON mortise.mortise_voice_profile(status, sort_no);

CREATE TABLE IF NOT EXISTS mortise.mortise_voice_job (
    id BIGINT PRIMARY KEY,
    job_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    profile_id BIGINT,
    user_id BIGINT,
    source_module VARCHAR(128),
    duration_millis BIGINT,
    result_summary TEXT,
    error_message TEXT,
    del_flag INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_voice_job_profile_status
    ON mortise.mortise_voice_job(profile_id, status);

CREATE TABLE IF NOT EXISTS mortise.mortise_voice_artifact (
    id BIGINT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    file_id BIGINT,
    artifact_type VARCHAR(32) NOT NULL,
    content_type VARCHAR(128),
    bucket VARCHAR(128),
    object_key VARCHAR(512),
    del_flag INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_voice_artifact_job FOREIGN KEY (job_id)
        REFERENCES mortise.mortise_voice_job(id)
);

CREATE INDEX IF NOT EXISTS idx_voice_artifact_job_type
    ON mortise.mortise_voice_artifact(job_id, artifact_type);

CREATE TABLE IF NOT EXISTS mortise.mortise_voice_session (
    id BIGINT PRIMARY KEY,
    session_code VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    user_id BIGINT,
    profile_id BIGINT,
    input_frame_count INTEGER NOT NULL DEFAULT 0,
    final_segment_count INTEGER NOT NULL DEFAULT 0,
    started_time TIMESTAMP,
    ended_time TIMESTAMP,
    del_flag INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_voice_session_code UNIQUE (session_code)
);

CREATE INDEX IF NOT EXISTS idx_voice_session_profile_status
    ON mortise.mortise_voice_session(profile_id, status);

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41200000000000001, '语音', 'voice', 'i-lucide-audio-lines', '/voices', 1, 0, 0, 55, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41200000000000011, 'Provider 管理', 'voice:provider', 'i-lucide-server', '/voices/providers', 1, 0, 1, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:provider');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41200000000000012, 'Model 管理', 'voice:model', 'i-lucide-cpu', '/voices/models', 1, 0, 1, 2, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:model');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41200000000000013, 'Profile 管理', 'voice:profile', 'i-lucide-sliders-horizontal', '/voices/profiles', 1, 0, 1, 3, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:profile');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41200000000000014, '运行时节点', 'voice:runtime', 'i-lucide-activity', '/voices/runtime', 1, 0, 1, 4, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:runtime');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41200000000000101, '查询 Provider', 'voice:provider:list', 'i-lucide-list', '', 1, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:provider'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:provider:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41200000000000102, '查询 Model', 'voice:model:list', 'i-lucide-list', '', 1, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:model'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:model:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41200000000000103, '查询 Profile', 'voice:profile:list', 'i-lucide-list', '', 1, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:profile'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:profile:list');

INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, del_flag, menu_type, sort_no, parent_id, created_time, updated_time)
SELECT 41200000000000104, '查询运行时节点', 'voice:runtime:list', 'i-lucide-list', '', 1, 0, 2, 1, parent.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mortise.mortise_menu parent
WHERE parent.permission = 'voice:runtime'
    AND NOT EXISTS (SELECT 1 FROM mortise.mortise_menu WHERE permission = 'voice:runtime:list');

INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu)
SELECT role.id, menu.id
FROM mortise.mortise_role role
CROSS JOIN mortise.mortise_menu menu
WHERE role.permission = 'ADMIN'
    AND menu.permission LIKE 'voice%'
    AND NOT EXISTS (
        SELECT 1 FROM mortise.mortise_role_menu rm
        WHERE rm.id_mortise_role = role.id
            AND rm.id_mortise_menu = menu.id
    );