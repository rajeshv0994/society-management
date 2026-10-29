CREATE TABLE society_expenses (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  category varchar(40) NOT NULL,
  vendor varchar(150),
  description varchar(300) NOT NULL,
  amount numeric(12,2) NOT NULL CHECK (amount > 0),
  expense_date date NOT NULL,
  payment_method varchar(30) NOT NULL,
  payment_reference varchar(120),
  created_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_society_expenses_date
ON society_expenses(society_id, expense_date DESC);

CREATE TABLE member_charge_settings (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  name varchar(120) NOT NULL,
  charge_type varchar(20) NOT NULL CHECK (charge_type IN ('MEMBERSHIP','OTHER')),
  amount numeric(12,2) NOT NULL CHECK (amount > 0),
  frequency varchar(20) NOT NULL CHECK (frequency IN ('MONTHLY','QUARTERLY','ANNUAL','ONE_TIME')),
  description varchar(240),
  active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(society_id, name)
);

CREATE INDEX idx_member_charge_settings_society
ON member_charge_settings(society_id, active, name);
