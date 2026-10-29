ALTER TABLE maintenance_invoices
  ADD COLUMN membership_fee numeric(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN other_charges numeric(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN other_charge_note varchar(240);

UPDATE maintenance_invoices
SET membership_fee = subtotal
WHERE membership_fee = 0 AND other_charges = 0;

ALTER TABLE maintenance_invoices
  ADD CONSTRAINT chk_invoices_membership_fee CHECK (membership_fee >= 0),
  ADD CONSTRAINT chk_invoices_other_charges CHECK (other_charges >= 0);
