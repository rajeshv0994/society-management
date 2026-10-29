ALTER TABLE subscription_plans
  ADD COLUMN max_staff integer,
  ADD COLUMN price_per_staff numeric(12,2) NOT NULL DEFAULT 0;

ALTER TABLE subscription_plans
  ADD CONSTRAINT chk_subscription_plan_max_staff CHECK (max_staff IS NULL OR max_staff > 0),
  ADD CONSTRAINT chk_subscription_plan_price_per_staff CHECK (price_per_staff >= 0);

UPDATE subscription_plans
SET max_staff = CASE code
      WHEN 'TRIAL' THEN 2
      WHEN 'BASIC' THEN 5
      WHEN 'STANDARD' THEN 15
      WHEN 'PREMIUM' THEN 50
      ELSE NULL
    END,
    price_per_staff = CASE code
      WHEN 'BASIC' THEN 100
      WHEN 'STANDARD' THEN 80
      WHEN 'PREMIUM' THEN 60
      ELSE 0
    END;

ALTER TABLE societies
  ADD COLUMN planned_staff integer NOT NULL DEFAULT 0,
  ADD CONSTRAINT chk_society_planned_staff CHECK (planned_staff >= 0);

UPDATE societies s
SET planned_staff = (
  SELECT count(*) FROM users u
  WHERE u.society_id = s.id AND u.role = 'STAFF'
);

ALTER TABLE society_custom_plans
  ADD COLUMN price_per_staff numeric(12,2) NOT NULL DEFAULT 0,
  ADD CONSTRAINT chk_custom_plan_price_per_staff CHECK (price_per_staff >= 0);
