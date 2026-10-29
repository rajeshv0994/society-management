ALTER TABLE visitors
  ALTER COLUMN flat_id DROP NOT NULL,
  ADD COLUMN target_type varchar(30) NOT NULL DEFAULT 'RESIDENT',
  ADD COLUMN target_user_id uuid REFERENCES users(id),
  ADD COLUMN target_name varchar(150),
  ADD COLUMN staff_approved_by uuid REFERENCES users(id),
  ADD COLUMN approval_method varchar(30),
  ADD COLUMN approval_note varchar(250);

UPDATE visitors
SET target_user_id = host_resident_id
WHERE host_resident_id IS NOT NULL;

CREATE TABLE visitor_photos (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  visitor_id uuid NOT NULL REFERENCES visitors(id) ON DELETE CASCADE,
  storage_key text NOT NULL,
  content_type varchar(50),
  created_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO visitor_photos(visitor_id, storage_key, content_type)
SELECT id, photo_storage_key, photo_content_type
FROM visitors
WHERE photo_storage_key IS NOT NULL;

CREATE INDEX idx_visitor_photos_visitor ON visitor_photos(visitor_id, created_at);
