ALTER TABLE subscription_orders DROP CONSTRAINT chk_subscription_order_status;
ALTER TABLE subscription_orders
  ADD CONSTRAINT chk_subscription_order_status
  CHECK(status IN ('DUE','PAYMENT_PENDING','SUBMITTED','PAID','REJECTED','CANCELLED'));

ALTER TABLE subscription_orders DROP CONSTRAINT uq_subscription_society_month;
CREATE UNIQUE INDEX uq_subscription_society_month_open
ON subscription_orders(society_id, billing_month)
WHERE status <> 'CANCELLED';

ALTER TABLE society_custom_plans
  ADD COLUMN cancelled_at timestamptz,
  ADD COLUMN cancelled_by uuid REFERENCES users(id),
  ADD COLUMN cancellation_reason varchar(300);
