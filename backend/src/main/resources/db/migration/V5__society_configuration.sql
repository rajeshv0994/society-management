CREATE TABLE parking_slots (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 slot_number varchar(40) NOT NULL, slot_type varchar(20) NOT NULL, status varchar(20) NOT NULL DEFAULT 'AVAILABLE',
 flat_id uuid REFERENCES flats(id), vehicle_number varchar(30), UNIQUE(society_id,slot_number)
);
CREATE TABLE vendors (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 name varchar(150) NOT NULL, service_type varchar(80) NOT NULL, contact_person varchar(120), mobile varchar(20) NOT NULL,
 email varchar(180), gst_number varchar(30), status varchar(20) NOT NULL DEFAULT 'ACTIVE', created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE maintenance_charge_rules (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 name varchar(120) NOT NULL, calculation_type varchar(30) NOT NULL CHECK(calculation_type IN ('FIXED','PER_SQFT','PERCENTAGE')),
 amount numeric(12,2) NOT NULL CHECK(amount>=0), frequency varchar(20) NOT NULL CHECK(frequency IN ('MONTHLY','QUARTERLY','YEARLY','ONE_TIME')),
 effective_from date NOT NULL, effective_to date, active boolean NOT NULL DEFAULT true, UNIQUE(society_id,name,effective_from)
);
CREATE TABLE staff_permissions (
 staff_user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE, permission_code varchar(80) NOT NULL,
 granted_by uuid NOT NULL REFERENCES users(id), granted_at timestamptz NOT NULL DEFAULT now(), PRIMARY KEY(staff_user_id,permission_code)
);
ALTER TABLE flats ADD COLUMN area_sqft numeric(10,2);
ALTER TABLE societies ADD COLUMN setup_completed boolean NOT NULL DEFAULT false;
CREATE INDEX idx_parking_society_status ON parking_slots(society_id,status);
CREATE INDEX idx_vendors_society_status ON vendors(society_id,status);
