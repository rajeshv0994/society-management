ALTER TABLE subscription_orders DROP CONSTRAINT chk_subscription_order_status;
ALTER TABLE subscription_orders
  ADD CONSTRAINT chk_subscription_order_status
  CHECK(status IN ('DUE','PAYMENT_PENDING','SUBMITTED','PAID','REJECTED'));

CREATE TABLE platform_payment_attempts (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  subscription_order_id uuid NOT NULL REFERENCES subscription_orders(id) ON DELETE CASCADE,
  provider_order_id varchar(120) NOT NULL UNIQUE,
  provider_payment_id varchar(120) UNIQUE,
  amount numeric(12,2) NOT NULL,
  currency varchar(3) NOT NULL,
  requested_method varchar(30),
  status varchar(20) NOT NULL,
  failure_code varchar(100),
  failure_description varchar(500),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT chk_platform_payment_attempt_status
    CHECK(status IN ('CREATED','AUTHORIZED','CAPTURED','FAILED'))
);

CREATE INDEX idx_platform_payment_attempt_order
ON platform_payment_attempts(subscription_order_id, created_at DESC);

CREATE TABLE razorpay_webhook_events (
  event_id varchar(150) PRIMARY KEY,
  event_type varchar(80) NOT NULL,
  received_at timestamptz NOT NULL DEFAULT now(),
  processed_at timestamptz
);
