UPDATE platform_services
SET status = 'ACTIVE',
    description = 'SOS reporting, coordinated response, emergency contacts and incident audit workflows'
WHERE code = 'EMERGENCY_INCIDENTS';

UPDATE societies
SET enabled_modules = enabled_modules || '["EMERGENCY_INCIDENTS"]'::jsonb,
    updated_at = now()
WHERE NOT enabled_modules @> '["EMERGENCY_INCIDENTS"]'::jsonb;

CREATE TABLE emergency_directory_contacts (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id),
  contact_type varchar(30) NOT NULL CHECK (contact_type IN ('AMBULANCE','HOSPITAL','POLICE','FIRE','SOCIETY_SECURITY','DISASTER_CONTROL','OTHER')),
  name varchar(150) NOT NULL,
  phone varchar(20) NOT NULL,
  alternate_phone varchar(20),
  address varchar(300),
  available_24x7 boolean NOT NULL DEFAULT false,
  priority int NOT NULL DEFAULT 1 CHECK (priority BETWEEN 1 AND 99),
  active boolean NOT NULL DEFAULT true,
  created_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(society_id, contact_type, phone)
);

CREATE INDEX idx_emergency_directory_society
  ON emergency_directory_contacts(society_id, active, contact_type, priority);

CREATE TABLE emergency_incidents (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id),
  incident_number varchar(40) NOT NULL UNIQUE,
  reporter_id uuid NOT NULL REFERENCES users(id),
  reporter_role varchar(30) NOT NULL,
  resident_id uuid REFERENCES users(id),
  flat_id uuid REFERENCES flats(id),
  incident_type varchar(30) NOT NULL CHECK (incident_type IN ('MEDICAL','ACCIDENT','FIRE','SECURITY','GAS_LEAK','ELECTRICAL','ELEVATOR','NATURAL_DISASTER','OTHER')),
  severity varchar(20) NOT NULL CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
  title varchar(180) NOT NULL,
  description varchar(1500) NOT NULL,
  location_details varchar(300) NOT NULL,
  latitude numeric(10,7),
  longitude numeric(10,7),
  people_affected int NOT NULL DEFAULT 1 CHECK (people_affected BETWEEN 0 AND 999),
  injured_person_name varchar(150),
  injured_person_mobile varchar(20),
  injury_details varchar(800),
  status varchar(30) NOT NULL DEFAULT 'REPORTED' CHECK (status IN ('REPORTED','ACKNOWLEDGED','RESPONDING','ON_SCENE','STABILIZED','RESOLVED','CLOSED','CANCELLED','FALSE_ALARM')),
  assigned_staff_id uuid REFERENCES users(id),
  emergency_contact_notified boolean NOT NULL DEFAULT false,
  ambulance_requested boolean NOT NULL DEFAULT false,
  police_requested boolean NOT NULL DEFAULT false,
  fire_service_requested boolean NOT NULL DEFAULT false,
  hospital_name varchar(180),
  external_reference varchar(120),
  acknowledged_at timestamptz,
  response_started_at timestamptz,
  arrived_at timestamptz,
  stabilized_at timestamptz,
  resolved_at timestamptz,
  closed_at timestamptz,
  resolution_summary varchar(1000),
  closed_reason varchar(500),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_emergency_incident_society_status
  ON emergency_incidents(society_id, status, severity, created_at DESC);
CREATE INDEX idx_emergency_incident_resident
  ON emergency_incidents(society_id, resident_id, created_at DESC);
CREATE INDEX idx_emergency_incident_assignee
  ON emergency_incidents(society_id, assigned_staff_id, status);

CREATE TABLE emergency_incident_events (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_id uuid NOT NULL REFERENCES emergency_incidents(id) ON DELETE CASCADE,
  society_id uuid NOT NULL REFERENCES societies(id),
  event_type varchar(40) NOT NULL,
  from_status varchar(30),
  to_status varchar(30),
  note varchar(1000),
  actor_id uuid NOT NULL REFERENCES users(id),
  actor_role varchar(30) NOT NULL,
  occurred_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_emergency_event_incident
  ON emergency_incident_events(incident_id, occurred_at);
