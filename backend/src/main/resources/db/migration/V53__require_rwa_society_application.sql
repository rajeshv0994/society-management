ALTER TABLE platform_services
  ADD CONSTRAINT chk_rwa_service_required
  CHECK (
    code <> 'RWA'
    OR (
      status = 'ACTIVE'
      AND availability_type = 'DEFAULT_FREE'
      AND price_per_space = 0
    )
  );

ALTER TABLE societies
  ADD CONSTRAINT chk_society_rwa_enabled
  CHECK (enabled_modules @> '["RWA"]'::jsonb);
