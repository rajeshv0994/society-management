CREATE TABLE society_budget_additions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  budget_year integer NOT NULL CHECK (budget_year >= 2020),
  amount numeric(14,2) NOT NULL CHECK (amount > 0),
  note varchar(240),
  added_by uuid NOT NULL REFERENCES users(id),
  added_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO society_budget_additions(
  society_id, budget_year, amount, note, added_by, added_at
)
SELECT society_id, budget_year, amount, 'Opening manual budget', updated_by, updated_at
FROM society_budgets
WHERE amount > 0;

CREATE INDEX idx_society_budget_additions_year
ON society_budget_additions(society_id, budget_year, added_at DESC);
