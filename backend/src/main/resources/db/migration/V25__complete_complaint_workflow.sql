ALTER TABLE complaints
  ADD COLUMN resident_id uuid REFERENCES users(id),
  ADD COLUMN flat_id uuid REFERENCES flats(id),
  ADD COLUMN created_by uuid REFERENCES users(id),
  ADD COLUMN due_at timestamptz;

UPDATE complaints SET created_by = raised_by WHERE created_by IS NULL;
UPDATE complaints c
SET resident_id = c.raised_by
WHERE EXISTS (
  SELECT 1 FROM users u WHERE u.id = c.raised_by AND u.role = 'RESIDENT'
);

ALTER TABLE complaints ALTER COLUMN created_by SET NOT NULL;

CREATE TABLE complaint_updates (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  complaint_id uuid NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
  actor_user_id uuid NOT NULL REFERENCES users(id),
  event_type varchar(30) NOT NULL,
  from_status varchar(30),
  to_status varchar(30),
  note text,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_complaint_updates_complaint
ON complaint_updates(complaint_id, created_at);

CREATE INDEX idx_complaints_resident_updated
ON complaints(society_id, resident_id, updated_at DESC);

CREATE INDEX idx_complaints_assignee_updated
ON complaints(society_id, assigned_to, updated_at DESC);
