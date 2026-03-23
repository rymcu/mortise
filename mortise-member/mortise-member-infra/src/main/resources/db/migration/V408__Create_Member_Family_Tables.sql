CREATE TABLE IF NOT EXISTS mortise.mortise_member_family
(
    id              BIGINT PRIMARY KEY,
    family_name     VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    owner_member_id BIGINT       NOT NULL REFERENCES mortise.mortise_member (id),
    created_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag        INTEGER      NOT NULL DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_member_family IS '会员家庭表';
COMMENT ON COLUMN mortise.mortise_member_family.family_name IS '家庭名称';
COMMENT ON COLUMN mortise.mortise_member_family.description IS '家庭描述';
COMMENT ON COLUMN mortise.mortise_member_family.owner_member_id IS '家庭创建者/管理员会员ID';

CREATE INDEX IF NOT EXISTS idx_member_family_owner
    ON mortise.mortise_member_family (owner_member_id)
    WHERE del_flag = 0;

CREATE INDEX IF NOT EXISTS idx_member_family_name
    ON mortise.mortise_member_family (family_name)
    WHERE del_flag = 0;

DROP TRIGGER IF EXISTS update_mortise_member_family_updated_time ON mortise.mortise_member_family;
CREATE TRIGGER update_mortise_member_family_updated_time
    BEFORE UPDATE
    ON mortise.mortise_member_family
    FOR EACH ROW EXECUTE FUNCTION update_updated_time_column();

CREATE TABLE IF NOT EXISTS mortise.mortise_member_family_member
(
    id           BIGINT PRIMARY KEY,
    family_id    BIGINT      NOT NULL REFERENCES mortise.mortise_member_family (id),
    member_id    BIGINT      NOT NULL REFERENCES mortise.mortise_member (id),
    role_code    VARCHAR(20) NOT NULL,
    joined_time  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag     INTEGER     NOT NULL DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_member_family_member IS '家庭成员关系表';
COMMENT ON COLUMN mortise.mortise_member_family_member.role_code IS '家庭角色编码：admin/member';

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_family_member_active
    ON mortise.mortise_member_family_member (family_id, member_id)
    WHERE del_flag = 0;

CREATE INDEX IF NOT EXISTS idx_member_family_member_member
    ON mortise.mortise_member_family_member (member_id)
    WHERE del_flag = 0;

DROP TRIGGER IF EXISTS update_mortise_member_family_member_updated_time ON mortise.mortise_member_family_member;
CREATE TRIGGER update_mortise_member_family_member_updated_time
    BEFORE UPDATE
    ON mortise.mortise_member_family_member
    FOR EACH ROW EXECUTE FUNCTION update_updated_time_column();

CREATE TABLE IF NOT EXISTS mortise.mortise_member_family_invitation
(
    id                BIGINT PRIMARY KEY,
    family_id         BIGINT       NOT NULL REFERENCES mortise.mortise_member_family (id),
    inviter_member_id BIGINT       NOT NULL REFERENCES mortise.mortise_member (id),
    invitee_member_id BIGINT       NOT NULL REFERENCES mortise.mortise_member (id),
    status_code       VARCHAR(20)  NOT NULL,
    invite_message    VARCHAR(255),
    expires_time      TIMESTAMP    NOT NULL,
    replied_time      TIMESTAMP,
    created_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag          INTEGER      NOT NULL DEFAULT 0
);

COMMENT ON TABLE mortise.mortise_member_family_invitation IS '家庭邀请表';
COMMENT ON COLUMN mortise.mortise_member_family_invitation.status_code IS '邀请状态：pending/accepted/rejected/cancelled/expired';

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_family_invitation_pending
    ON mortise.mortise_member_family_invitation (family_id, invitee_member_id)
    WHERE del_flag = 0 AND status_code = 'pending';

CREATE INDEX IF NOT EXISTS idx_member_family_invitation_invitee
    ON mortise.mortise_member_family_invitation (invitee_member_id, status_code)
    WHERE del_flag = 0;

DROP TRIGGER IF EXISTS update_mortise_member_family_invitation_updated_time ON mortise.mortise_member_family_invitation;
CREATE TRIGGER update_mortise_member_family_invitation_updated_time
    BEFORE UPDATE
    ON mortise.mortise_member_family_invitation
    FOR EACH ROW EXECUTE FUNCTION update_updated_time_column();

ALTER TABLE mortise.mortise_member
    ADD COLUMN IF NOT EXISTS current_family_id BIGINT REFERENCES mortise.mortise_member_family (id);

COMMENT ON COLUMN mortise.mortise_member.current_family_id IS '当前家庭ID';

CREATE INDEX IF NOT EXISTS idx_member_current_family
    ON mortise.mortise_member (current_family_id)
    WHERE del_flag = 0;
