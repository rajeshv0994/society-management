ALTER TABLE platform_services
  ADD COLUMN availability_type varchar(24) NOT NULL DEFAULT 'OPTIONAL_FREE',
  ADD COLUMN price_per_space numeric(12,2) NOT NULL DEFAULT 0;

ALTER TABLE platform_services
  ADD CONSTRAINT chk_platform_service_availability
    CHECK (availability_type IN ('DEFAULT_FREE','OPTIONAL_FREE','PAID_PER_SPACE')),
  ADD CONSTRAINT chk_platform_service_space_price
    CHECK (
      (availability_type = 'PAID_PER_SPACE' AND price_per_space > 0)
      OR (availability_type IN ('DEFAULT_FREE','OPTIONAL_FREE') AND price_per_space = 0)
    );

-- These are foundational services. Existing selections remain unchanged; the
-- defaults are also guaranteed for every current and future society.
UPDATE platform_services
SET availability_type = 'DEFAULT_FREE', price_per_space = 0
WHERE code IN ('RESIDENTS','NOTICES') AND status = 'ACTIVE';

UPDATE societies
SET enabled_modules = (
  SELECT jsonb_agg(DISTINCT module ORDER BY module)
  FROM (
    SELECT jsonb_array_elements_text(societies.enabled_modules) AS module
    UNION ALL
    SELECT code FROM platform_services
    WHERE status = 'ACTIVE' AND availability_type = 'DEFAULT_FREE'
  ) selected
), updated_at = now();

ALTER TABLE subscription_orders
  ADD COLUMN pricing_breakdown jsonb NOT NULL DEFAULT '[]'::jsonb;

CREATE INDEX idx_platform_services_availability
  ON platform_services(status, availability_type, sort_order);
