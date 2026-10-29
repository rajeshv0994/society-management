ALTER TABLE notices
  ADD COLUMN status varchar(20) NOT NULL DEFAULT 'PUBLISHED',
  ADD COLUMN priority varchar(20) NOT NULL DEFAULT 'NORMAL',
  ADD COLUMN pinned boolean NOT NULL DEFAULT false,
  ADD COLUMN requires_acknowledgement boolean NOT NULL DEFAULT false,
  ADD COLUMN updated_at timestamptz;

UPDATE notices SET updated_at = published_at WHERE updated_at IS NULL;

ALTER TABLE notices
  ALTER COLUMN published_at DROP NOT NULL,
  ALTER COLUMN updated_at SET NOT NULL,
  ADD CONSTRAINT chk_notice_status CHECK(status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
  ADD CONSTRAINT chk_notice_priority CHECK(priority IN ('NORMAL', 'IMPORTANT', 'URGENT'));

CREATE TABLE notice_reads (
  notice_id uuid NOT NULL REFERENCES notices(id) ON DELETE CASCADE,
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  read_at timestamptz NOT NULL DEFAULT now(),
  acknowledged_at timestamptz,
  PRIMARY KEY(notice_id, user_id)
);

CREATE INDEX idx_notice_reads_user
ON notice_reads(user_id, read_at DESC);

CREATE INDEX idx_notices_society_lifecycle
ON notices(society_id, status, pinned DESC, published_at DESC);
