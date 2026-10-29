CREATE TABLE requirement_requests (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid REFERENCES societies(id) ON DELETE SET NULL,
 society_name varchar(180) NOT NULL, contact_person varchar(150) NOT NULL, email varchar(180) NOT NULL,
 mobile varchar(20) NOT NULL, city varchar(100), estimated_flats integer,
 requested_modules jsonb NOT NULL DEFAULT '[]'::jsonb, requirement_details text NOT NULL,
 budget_monthly numeric(12,2), status varchar(30) NOT NULL DEFAULT 'NEW',
 admin_notes text, assigned_to uuid REFERENCES users(id) ON DELETE SET NULL,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), closed_at timestamptz
);

CREATE TABLE society_custom_plans (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 requirement_id uuid REFERENCES requirement_requests(id) ON DELETE SET NULL,
 plan_name varchar(120) NOT NULL, description text, enabled_modules jsonb NOT NULL DEFAULT '[]'::jsonb,
 max_flats integer, max_staff integer, max_storage_gb integer,
 price_monthly numeric(12,2) NOT NULL CHECK(price_monthly>=0), setup_fee numeric(12,2) NOT NULL DEFAULT 0,
 billing_cycle varchar(20) NOT NULL DEFAULT 'MONTHLY', valid_from date NOT NULL,
 valid_until date, status varchar(20) NOT NULL DEFAULT 'DRAFT', created_by uuid NOT NULL REFERENCES users(id),
 created_at timestamptz NOT NULL DEFAULT now(), activated_at timestamptz
);

CREATE INDEX idx_requirements_status_created ON requirement_requests(status,created_at DESC);
CREATE INDEX idx_custom_plans_society_status ON society_custom_plans(society_id,status);
