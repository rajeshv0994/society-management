CREATE TABLE complaint_categories (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 name varchar(100) NOT NULL, sla_hours integer NOT NULL DEFAULT 48, active boolean NOT NULL DEFAULT true,
 UNIQUE(society_id,name)
);
CREATE TABLE complaints (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 raised_by uuid NOT NULL REFERENCES users(id), category_id uuid REFERENCES complaint_categories(id),
 title varchar(180) NOT NULL, description text NOT NULL, priority varchar(20) NOT NULL DEFAULT 'NORMAL',
 status varchar(30) NOT NULL DEFAULT 'OPEN', assigned_to uuid REFERENCES users(id), resolution text,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), closed_at timestamptz
);
CREATE TABLE notices (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 title varchar(180) NOT NULL, body text NOT NULL, audience varchar(30) NOT NULL DEFAULT 'ALL',
 published_by uuid NOT NULL REFERENCES users(id), published_at timestamptz NOT NULL DEFAULT now(), expires_at timestamptz
);
CREATE TABLE visitors (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 flat_id uuid NOT NULL REFERENCES flats(id), visitor_name varchar(150) NOT NULL, mobile varchar(20), purpose varchar(150),
 vehicle_number varchar(30), expected_at timestamptz, approved_by uuid REFERENCES users(id),
 checked_in_by uuid REFERENCES users(id), checked_in_at timestamptz, checked_out_at timestamptz,
 status varchar(20) NOT NULL DEFAULT 'EXPECTED', created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE amenities (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 name varchar(120) NOT NULL, description text, capacity integer, hourly_charge numeric(12,2) NOT NULL DEFAULT 0,
 opens_at time, closes_at time, active boolean NOT NULL DEFAULT true, UNIQUE(society_id,name)
);
CREATE TABLE amenity_bookings (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 amenity_id uuid NOT NULL REFERENCES amenities(id), booked_by uuid NOT NULL REFERENCES users(id),
 starts_at timestamptz NOT NULL, ends_at timestamptz NOT NULL, amount numeric(12,2) NOT NULL DEFAULT 0,
 status varchar(20) NOT NULL DEFAULT 'CONFIRMED', created_at timestamptz NOT NULL DEFAULT now(),
 CHECK(ends_at > starts_at)
);
CREATE TABLE maintenance_invoices (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 flat_id uuid NOT NULL REFERENCES flats(id), invoice_number varchar(50) NOT NULL, billing_month date NOT NULL,
 due_date date NOT NULL, subtotal numeric(12,2) NOT NULL CHECK(subtotal >= 0), late_fee numeric(12,2) NOT NULL DEFAULT 0,
 paid_amount numeric(12,2) NOT NULL DEFAULT 0, status varchar(20) NOT NULL DEFAULT 'DUE', created_at timestamptz NOT NULL DEFAULT now(),
 UNIQUE(society_id,invoice_number), UNIQUE(flat_id,billing_month)
);
CREATE TABLE payments (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 invoice_id uuid NOT NULL REFERENCES maintenance_invoices(id), paid_by uuid NOT NULL REFERENCES users(id),
 amount numeric(12,2) NOT NULL CHECK(amount > 0), payment_method varchar(30) NOT NULL,
 provider_reference varchar(120), status varchar(20) NOT NULL DEFAULT 'SUCCESS', paid_at timestamptz NOT NULL DEFAULT now(),
 UNIQUE(provider_reference)
);
CREATE TABLE staff_tasks (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 title varchar(180) NOT NULL, description text, assigned_to uuid NOT NULL REFERENCES users(id), assigned_by uuid NOT NULL REFERENCES users(id),
 due_at timestamptz, priority varchar(20) NOT NULL DEFAULT 'NORMAL', status varchar(20) NOT NULL DEFAULT 'OPEN',
 completion_note text, created_at timestamptz NOT NULL DEFAULT now(), completed_at timestamptz
);
CREATE TABLE household_members (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 primary_resident_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE, full_name varchar(150) NOT NULL,
 relationship varchar(40) NOT NULL, mobile varchar(20), date_of_birth date, active boolean NOT NULL DEFAULT true
);
CREATE INDEX idx_complaints_society_status ON complaints(society_id,status);
CREATE INDEX idx_visitors_society_status ON visitors(society_id,status);
CREATE INDEX idx_invoices_society_status ON maintenance_invoices(society_id,status);
CREATE INDEX idx_tasks_assignee_status ON staff_tasks(assigned_to,status);
CREATE INDEX idx_notices_society_published ON notices(society_id,published_at DESC);
