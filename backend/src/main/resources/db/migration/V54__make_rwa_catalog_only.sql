ALTER TABLE societies
  DROP CONSTRAINT IF EXISTS chk_society_rwa_enabled;

ALTER TABLE platform_services
  DROP CONSTRAINT IF EXISTS chk_rwa_service_required;

UPDATE platform_services
SET availability_type = 'OPTIONAL_FREE',
    price_per_space = 0,
    updated_at = now()
WHERE code = 'RWA';

UPDATE societies
SET enabled_modules = enabled_modules - 'RWA',
    updated_at = now()
WHERE enabled_modules @> '["RWA"]'::jsonb;
