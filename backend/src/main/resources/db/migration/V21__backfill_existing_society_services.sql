UPDATE societies
SET enabled_modules = (
  SELECT jsonb_agg(code ORDER BY sort_order, name)
  FROM platform_services
  WHERE status = 'ACTIVE'
)
WHERE enabled_modules = '[]'::jsonb;
