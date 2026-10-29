ALTER TABLE societies ADD COLUMN society_code varchar(30);

UPDATE societies
SET society_code = 'SOC-' || upper(substr(replace(id::text, '-', ''), 1, 8));

UPDATE societies
SET society_code = 'GREEN-VALLEY'
WHERE registration_number = 'MH/PUN/2024/1001';

UPDATE societies
SET society_code = 'SUNRISE'
WHERE registration_number = 'KA/BLR/2023/2042';

ALTER TABLE societies
  ALTER COLUMN society_code SET NOT NULL,
  ADD CONSTRAINT uq_societies_society_code UNIQUE (society_code);
