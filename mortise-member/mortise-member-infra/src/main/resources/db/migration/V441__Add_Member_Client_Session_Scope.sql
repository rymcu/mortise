ALTER TABLE mortise.mortise_member_client_session
    ADD COLUMN IF NOT EXISTS scope VARCHAR(200) NOT NULL DEFAULT 'profile';

COMMENT ON COLUMN mortise.mortise_member_client_session.scope IS '授权作用域';
