INSERT INTO platform_services(
  code,
  name,
  description,
  status,
  sort_order,
  availability_type,
  price_per_space
) VALUES (
  'RWA',
  'Society Application',
  'Resident Welfare Association society management application',
  'ACTIVE',
  5,
  'DEFAULT_FREE',
  0
)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    sort_order = EXCLUDED.sort_order,
    availability_type = EXCLUDED.availability_type,
    price_per_space = EXCLUDED.price_per_space,
    updated_at = now();

UPDATE societies
SET enabled_modules = (
      SELECT jsonb_agg(module ORDER BY module)
      FROM (
        SELECT jsonb_array_elements_text(
          COALESCE(societies.enabled_modules, '[]'::jsonb)
        ) AS module
        UNION
        SELECT 'RWA'
      ) selected
    ),
    updated_at = now();
