ALTER TABLE visitors
  ALTER COLUMN status TYPE varchar(30),
  ADD COLUMN host_resident_id uuid REFERENCES users(id),
  ADD COLUMN accompanying_members integer NOT NULL DEFAULT 0 CHECK (accompanying_members >= 0),
  ADD COLUMN photo_storage_key text,
  ADD COLUMN photo_content_type varchar(50),
  ADD COLUMN resident_decision_at timestamptz;

UPDATE visitors
SET host_resident_id = approved_by
WHERE approved_by IN (SELECT id FROM users WHERE role = 'RESIDENT');

CREATE INDEX idx_visitors_host_status
ON visitors(host_resident_id, status, created_at DESC);
