ALTER TABLE household_members
  ADD COLUMN member_code varchar(30),
  ADD COLUMN email varchar(180),
  ADD COLUMN gender varchar(20),
  ADD COLUMN blood_group varchar(10),
  ADD COLUMN occupation varchar(120),
  ADD COLUMN identity_type varchar(30),
  ADD COLUMN identity_last4 varchar(4),
  ADD COLUMN emergency_contact_name varchar(150),
  ADD COLUMN emergency_contact_mobile varchar(20),
  ADD COLUMN access_enabled boolean NOT NULL DEFAULT false,
  ADD COLUMN verification_status varchar(20) NOT NULL DEFAULT 'PENDING',
  ADD COLUMN verification_note varchar(300),
  ADD COLUMN reviewed_by uuid REFERENCES users(id),
  ADD COLUMN reviewed_at timestamptz,
  ADD COLUMN created_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();

UPDATE household_members
SET member_code = 'HM-' || upper(substr(replace(id::text, '-', ''), 1, 10))
WHERE member_code IS NULL;

ALTER TABLE household_members
  ALTER COLUMN member_code SET NOT NULL,
  ADD CONSTRAINT uq_household_member_code UNIQUE(society_id, member_code),
  ADD CONSTRAINT chk_household_member_verification CHECK(verification_status IN ('PENDING','VERIFIED','REJECTED')),
  ADD CONSTRAINT chk_household_member_identity_last4 CHECK(identity_last4 IS NULL OR identity_last4 ~ '^[A-Za-z0-9]{4}$');

CREATE INDEX idx_household_members_resident_active
  ON household_members(society_id, primary_resident_id, active);

CREATE TABLE household_vehicles (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  primary_resident_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  vehicle_type varchar(20) NOT NULL,
  registration_number varchar(20) NOT NULL,
  make varchar(80),
  model varchar(80),
  color varchar(40),
  parking_slot varchar(40),
  sticker_number varchar(40),
  verification_status varchar(20) NOT NULL DEFAULT 'PENDING',
  verification_note varchar(300),
  reviewed_by uuid REFERENCES users(id),
  reviewed_at timestamptz,
  active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT chk_household_vehicle_type CHECK(vehicle_type IN ('CAR','MOTORCYCLE','SCOOTER','BICYCLE','OTHER')),
  CONSTRAINT chk_household_vehicle_verification CHECK(verification_status IN ('PENDING','VERIFIED','REJECTED'))
);

CREATE UNIQUE INDEX uq_household_active_vehicle_registration
  ON household_vehicles(society_id, upper(registration_number)) WHERE active;
CREATE INDEX idx_household_vehicles_resident_active
  ON household_vehicles(society_id, primary_resident_id, active);

CREATE TABLE household_emergency_contacts (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  primary_resident_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  full_name varchar(150) NOT NULL,
  relationship varchar(40) NOT NULL,
  mobile varchar(20) NOT NULL,
  alternate_mobile varchar(20),
  priority integer NOT NULL DEFAULT 1,
  active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT chk_household_contact_priority CHECK(priority BETWEEN 1 AND 3)
);

CREATE INDEX idx_household_contacts_resident_active
  ON household_emergency_contacts(society_id, primary_resident_id, active, priority);
