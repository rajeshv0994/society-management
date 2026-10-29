ALTER TABLE payments
  ADD COLUMN receipt_number varchar(50),
  ADD COLUMN payment_channel varchar(20),
  ADD COLUMN recorded_by uuid REFERENCES users(id),
  ADD COLUMN note text;

UPDATE payments
SET receipt_number = 'RCT-LEGACY-' || upper(substr(replace(id::text, '-', ''), 1, 12)),
    payment_channel = CASE
      WHEN payment_method IN ('CASH', 'CHEQUE', 'BANK_TRANSFER') THEN 'OFFLINE'
      ELSE 'ONLINE'
    END,
    recorded_by = paid_by
WHERE receipt_number IS NULL;

ALTER TABLE payments
  ALTER COLUMN receipt_number SET NOT NULL,
  ALTER COLUMN payment_channel SET NOT NULL,
  ALTER COLUMN recorded_by SET NOT NULL,
  ADD CONSTRAINT uq_payments_receipt_number UNIQUE(receipt_number),
  ADD CONSTRAINT chk_payments_channel CHECK(payment_channel IN ('ONLINE', 'OFFLINE'));

CREATE INDEX idx_payments_invoice_paid_at
ON payments(invoice_id, paid_at DESC);
