CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE subscription_plans (
  code varchar(20) PRIMARY KEY,
  name varchar(50) NOT NULL,
  approval_mode varchar(10) NOT NULL CHECK (approval_mode IN ('AUTO','MANUAL')),
  max_flats integer,
  price_monthly numeric(12,2) NOT NULL DEFAULT 0
);

INSERT INTO subscription_plans(code,name,approval_mode,max_flats,price_monthly) VALUES
 ('TRIAL','Trial','AUTO',25,0), ('BASIC','Basic','AUTO',100,999),
 ('STANDARD','Standard','MANUAL',300,2499), ('PREMIUM','Premium','MANUAL',1000,5999),
 ('ENTERPRISE','Enterprise','MANUAL',NULL,0);

CREATE TABLE societies (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name varchar(180) NOT NULL,
  registration_number varchar(80) UNIQUE NOT NULL,
  society_type varchar(40) NOT NULL,
  total_blocks integer NOT NULL CHECK (total_blocks > 0),
  total_flats integer NOT NULL CHECK (total_flats > 0),
  approximate_residents integer,
  address text NOT NULL, city varchar(80) NOT NULL, state varchar(80) NOT NULL, pin_code varchar(10) NOT NULL,
  contact_person varchar(120) NOT NULL, email varchar(180) NOT NULL, mobile varchar(20) NOT NULL,
  plan_code varchar(20) NOT NULL REFERENCES subscription_plans(code),
  status varchar(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
  rejection_reason text, created_at timestamptz NOT NULL DEFAULT now(), approved_at timestamptz
);

CREATE TABLE society_documents (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 document_type varchar(40) NOT NULL, storage_key text NOT NULL, verification_status varchar(20) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE setup_codes (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id),
 code_hash varchar(255) NOT NULL, code_hint varchar(40) NOT NULL, expires_at timestamptz NOT NULL,
 max_activations integer NOT NULL DEFAULT 1, activation_count integer NOT NULL DEFAULT 0,
 status varchar(20) NOT NULL DEFAULT 'ACTIVE', created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE users (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid REFERENCES societies(id),
 full_name varchar(150) NOT NULL, email varchar(180) UNIQUE NOT NULL, mobile varchar(20),
 password_hash varchar(255) NOT NULL, role varchar(30) NOT NULL, status varchar(20) NOT NULL DEFAULT 'ACTIVE',
 email_verified boolean NOT NULL DEFAULT false, mobile_verified boolean NOT NULL DEFAULT false,
 created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE blocks (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE, name varchar(60) NOT NULL, UNIQUE(society_id,name));
CREATE TABLE floors (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), block_id uuid NOT NULL REFERENCES blocks(id) ON DELETE CASCADE, name varchar(60) NOT NULL, floor_number integer NOT NULL, UNIQUE(block_id,floor_number));
CREATE TABLE flats (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), floor_id uuid NOT NULL REFERENCES floors(id) ON DELETE CASCADE,
 flat_number varchar(30) NOT NULL, occupancy_status varchar(30) NOT NULL DEFAULT 'VACANT',
 active_owner_id uuid REFERENCES users(id), active_tenant_id uuid REFERENCES users(id), UNIQUE(floor_id,flat_number)
);

CREATE TABLE resident_registrations (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id), flat_id uuid NOT NULL REFERENCES flats(id),
 resident_type varchar(10) NOT NULL CHECK(resident_type IN ('OWNER','TENANT')), full_name varchar(150) NOT NULL,
 mobile varchar(20) NOT NULL, email varchar(180) NOT NULL, dob date, gender varchar(20), emergency_contact varchar(20),
 move_in_date date, move_out_date date, status varchar(30) NOT NULL DEFAULT 'PENDING_APPROVAL', rejection_reason text,
 invited boolean NOT NULL DEFAULT false, invitation_token_hash varchar(255), invitation_expires_at timestamptz,
 submitted_at timestamptz NOT NULL DEFAULT now(), reviewed_by uuid REFERENCES users(id), reviewed_at timestamptz
);

CREATE TABLE resident_documents (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), registration_id uuid NOT NULL REFERENCES resident_registrations(id) ON DELETE CASCADE,
 document_type varchar(40) NOT NULL, storage_key text NOT NULL, verification_status varchar(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_societies_status ON societies(status);
CREATE INDEX idx_registrations_society_status ON resident_registrations(society_id,status);
CREATE INDEX idx_flats_floor ON flats(floor_id);
