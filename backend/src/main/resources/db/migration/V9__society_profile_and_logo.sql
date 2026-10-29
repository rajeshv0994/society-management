ALTER TABLE societies
  ADD COLUMN description text,
  ADD COLUMN website varchar(255),
  ADD COLUMN logo_storage_key text,
  ADD COLUMN logo_content_type varchar(50),
  ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();
