CREATE TABLE platform_payment_settings (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  beneficiary_name varchar(150) NOT NULL,
  bank_name varchar(150),
  account_number varchar(60),
  ifsc_code varchar(20),
  upi_id varchar(100),
  payment_instructions varchar(500),
  updated_by uuid REFERENCES users(id),
  updated_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO platform_payment_settings(
  beneficiary_name, bank_name, account_number, ifsc_code, upi_id,
  payment_instructions
) VALUES (
  'SocietyHub Platform', 'Configure bank name', 'Configure account number',
  'CONFIGURE', 'payments@societyhub',
  'Super Admin must configure the platform receiving account before accepting live payments.'
);
