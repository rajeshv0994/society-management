CREATE TABLE society_payment_settings (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL UNIQUE REFERENCES societies(id) ON DELETE CASCADE,
  beneficiary_name varchar(150) NOT NULL,
  bank_name varchar(150),
  account_number varchar(60),
  ifsc_code varchar(20),
  upi_id varchar(100),
  razorpay_key_id varchar(100) NOT NULL,
  test_mode boolean NOT NULL DEFAULT true,
  updated_by uuid REFERENCES users(id),
  updated_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO society_payment_settings(
  society_id, beneficiary_name, bank_name, account_number, ifsc_code,
  upi_id, razorpay_key_id, test_mode
)
SELECT
  id, name, 'Test settlement bank', '0000000000', 'TEST0000000',
  'collections@societyhub', 'rzp_test_societyhub', true
FROM societies;
