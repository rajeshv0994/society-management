ALTER TABLE subscription_plans
  ADD COLUMN price_per_space numeric(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN trial_duration_months integer NOT NULL DEFAULT 1;

ALTER TABLE subscription_plans
  ADD CONSTRAINT chk_subscription_plan_price_per_space
    CHECK (price_per_space >= 0),
  ADD CONSTRAINT chk_subscription_plan_trial_duration
    CHECK (trial_duration_months > 0);

UPDATE subscription_plans
SET price_per_space = CASE code
  WHEN 'BASIC' THEN 10
  WHEN 'STANDARD' THEN 8
  WHEN 'PREMIUM' THEN 6
  ELSE 0
END;

UPDATE subscription_plans
SET max_flats = 10,
    trial_duration_months = 1
WHERE code = 'TRIAL';
