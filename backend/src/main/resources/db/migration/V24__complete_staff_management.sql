ALTER TABLE staff_profiles
  ADD COLUMN address text,
  ADD COLUMN emergency_contact_name varchar(150),
  ADD COLUMN emergency_contact_mobile varchar(20),
  ADD COLUMN id_type varchar(40),
  ADD COLUMN id_number varchar(80),
  ADD COLUMN monthly_salary numeric(12,2) CHECK (monthly_salary IS NULL OR monthly_salary >= 0);

CREATE TABLE staff_attendance (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  staff_user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  attendance_date date NOT NULL,
  status varchar(20) NOT NULL CHECK (status IN ('PRESENT','ABSENT','HALF_DAY','LEAVE')),
  check_in time,
  check_out time,
  note varchar(250),
  marked_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(staff_user_id, attendance_date)
);

CREATE INDEX idx_staff_attendance_society_date
ON staff_attendance(society_id, attendance_date DESC);

CREATE TABLE staff_salary_payments (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  staff_user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  salary_month date NOT NULL,
  amount numeric(12,2) NOT NULL CHECK (amount > 0),
  paid_on date NOT NULL,
  payment_method varchar(30) NOT NULL,
  reference_number varchar(100),
  note varchar(250),
  recorded_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(staff_user_id, salary_month)
);

CREATE INDEX idx_staff_salary_society_month
ON staff_salary_payments(society_id, salary_month DESC);
