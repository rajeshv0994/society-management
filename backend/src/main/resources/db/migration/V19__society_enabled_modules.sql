ALTER TABLE societies
ADD COLUMN enabled_modules jsonb NOT NULL DEFAULT '[]'::jsonb;
