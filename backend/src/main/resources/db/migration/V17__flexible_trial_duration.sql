ALTER TABLE subscription_plans
  ADD COLUMN trial_duration_value integer,
  ADD COLUMN trial_duration_unit varchar(10);

UPDATE subscription_plans
SET trial_duration_value = trial_duration_months,
    trial_duration_unit = 'MONTHS';

ALTER TABLE subscription_plans
  ALTER COLUMN trial_duration_value SET NOT NULL,
  ALTER COLUMN trial_duration_unit SET NOT NULL,
  ADD CONSTRAINT chk_trial_duration_value CHECK (trial_duration_value > 0),
  ADD CONSTRAINT chk_trial_duration_unit CHECK (trial_duration_unit IN ('DAYS', 'MONTHS'));
