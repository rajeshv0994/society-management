ALTER TABLE member_charge_settings
  ADD COLUMN flat_id uuid REFERENCES flats(id);

CREATE INDEX idx_member_charge_settings_flat
ON member_charge_settings(society_id, flat_id, active);

CREATE TABLE society_budgets (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  budget_year integer NOT NULL CHECK (budget_year >= 2020),
  amount numeric(14,2) NOT NULL CHECK (amount >= 0),
  updated_by uuid NOT NULL REFERENCES users(id),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(society_id, budget_year)
);
