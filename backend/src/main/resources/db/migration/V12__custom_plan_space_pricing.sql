ALTER TABLE society_custom_plans
  ADD COLUMN price_per_space numeric(12,2) NOT NULL DEFAULT 0;

ALTER TABLE society_custom_plans
  ADD CONSTRAINT chk_custom_plan_price_per_space
    CHECK (price_per_space >= 0);
