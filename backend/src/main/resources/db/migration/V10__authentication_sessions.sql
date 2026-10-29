CREATE TABLE auth_refresh_sessions (
  id uuid PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  family_id uuid NOT NULL,
  token_hash varchar(64) NOT NULL UNIQUE,
  expires_at timestamptz NOT NULL,
  created_at timestamptz NOT NULL,
  last_used_at timestamptz,
  revoked_at timestamptz,
  replaced_by_id uuid REFERENCES auth_refresh_sessions(id)
);

CREATE INDEX idx_auth_sessions_user_active
  ON auth_refresh_sessions(user_id, expires_at)
  WHERE revoked_at IS NULL;

CREATE INDEX idx_auth_sessions_family
  ON auth_refresh_sessions(family_id);
