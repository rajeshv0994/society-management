CREATE TABLE staff_profiles (
  user_id uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  staff_type varchar(40) NOT NULL,
  employee_code varchar(40),
  duty_area varchar(100),
  shift_name varchar(60),
  joined_on date NOT NULL DEFAULT current_date,
  UNIQUE (employee_code, user_id)
);

CREATE INDEX idx_staff_society ON users(society_id) WHERE role = 'STAFF';

