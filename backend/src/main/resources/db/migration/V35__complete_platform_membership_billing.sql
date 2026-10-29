ALTER TABLE subscription_orders
  ADD COLUMN invoice_number varchar(50),
  ADD COLUMN billing_month date,
  ADD COLUMN due_date date,
  ADD COLUMN payment_method varchar(30),
  ADD COLUMN payment_note varchar(300),
  ADD COLUMN submitted_at timestamptz,
  ADD COLUMN reviewed_at timestamptz,
  ADD COLUMN reviewed_by uuid REFERENCES users(id),
  ADD COLUMN rejection_reason varchar(300);

UPDATE subscription_orders
SET invoice_number = 'SUB-' || to_char(created_at, 'YYYYMM') || '-' || upper(substr(replace(id::text, '-', ''), 1, 8)),
    billing_month = date_trunc('month', created_at)::date,
    due_date = (created_at::date + 15),
    payment_method = CASE WHEN status = 'PAID' THEN provider ELSE NULL END,
    submitted_at = CASE WHEN status = 'PAID' THEN paid_at ELSE NULL END,
    reviewed_at = CASE WHEN status = 'PAID' THEN paid_at ELSE NULL END,
    status = CASE WHEN status = 'CREATED' THEN 'DUE' ELSE status END;

ALTER TABLE subscription_orders
  ALTER COLUMN invoice_number SET NOT NULL,
  ALTER COLUMN billing_month SET NOT NULL,
  ALTER COLUMN due_date SET NOT NULL,
  ADD CONSTRAINT uq_subscription_invoice_number UNIQUE(invoice_number),
  ADD CONSTRAINT uq_subscription_society_month UNIQUE(society_id, billing_month),
  ADD CONSTRAINT chk_subscription_order_status
    CHECK(status IN ('DUE','SUBMITTED','PAID','REJECTED'));

CREATE INDEX idx_subscription_orders_due
ON subscription_orders(status, due_date);
