ALTER TABLE societies
  ADD COLUMN subscription_billing_cycle varchar(20) NOT NULL DEFAULT 'MONTHLY';

ALTER TABLE societies
  ADD CONSTRAINT chk_society_subscription_cycle
  CHECK(subscription_billing_cycle IN ('MONTHLY','QUARTERLY','YEARLY'));

ALTER TABLE subscription_orders
  ADD COLUMN billing_cycle varchar(20) NOT NULL DEFAULT 'MONTHLY',
  ADD COLUMN period_start date,
  ADD COLUMN period_end date;

UPDATE subscription_orders
SET period_start = billing_month,
    period_end = (billing_month + interval '1 month - 1 day')::date;

ALTER TABLE subscription_orders
  ALTER COLUMN period_start SET NOT NULL,
  ALTER COLUMN period_end SET NOT NULL,
  ADD CONSTRAINT chk_subscription_invoice_cycle
    CHECK(billing_cycle IN ('MONTHLY','QUARTERLY','YEARLY')),
  ADD CONSTRAINT chk_subscription_invoice_period CHECK(period_end >= period_start);
