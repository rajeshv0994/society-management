INSERT INTO platform_services(code, name, description, status, sort_order)
VALUES ('ACTIVITIES_GALLERIES', 'Activities & galleries', 'Free and paid society activities, participation, attendance and moderated photo galleries', 'ACTIVE', 140)
ON CONFLICT (code) DO UPDATE
SET name = excluded.name,
    description = excluded.description,
    status = 'ACTIVE',
    sort_order = excluded.sort_order,
    updated_at = now();

UPDATE societies
SET enabled_modules = enabled_modules || '["ACTIVITIES_GALLERIES"]'::jsonb,
    updated_at = now()
WHERE NOT enabled_modules @> '["ACTIVITIES_GALLERIES"]'::jsonb;

CREATE TABLE society_activities (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id),
  activity_code varchar(40) NOT NULL,
  title varchar(180) NOT NULL,
  category varchar(30) NOT NULL CHECK (category IN ('SPORTS','CULTURAL','EDUCATIONAL','HEALTH','SOCIAL','RELIGIOUS','CHILDREN','SENIOR_CITIZEN','VOLUNTEERING','OTHER')),
  description varchar(2000) NOT NULL,
  venue varchar(300) NOT NULL,
  starts_at timestamptz NOT NULL,
  ends_at timestamptz NOT NULL,
  registration_opens_at timestamptz NOT NULL,
  registration_closes_at timestamptz NOT NULL,
  capacity int CHECK (capacity IS NULL OR capacity > 0),
  fee_per_person numeric(12,2) NOT NULL DEFAULT 0 CHECK (fee_per_person >= 0),
  approval_required boolean NOT NULL DEFAULT false,
  cancellation_hours int NOT NULL DEFAULT 0 CHECK (cancellation_hours BETWEEN 0 AND 720),
  audience varchar(30) NOT NULL DEFAULT 'ALL_MEMBERS' CHECK (audience IN ('ALL_MEMBERS','ADULTS','CHILDREN','SENIOR_CITIZENS','WOMEN','MEN','OTHER')),
  organizer_name varchar(150),
  organizer_mobile varchar(20),
  instructions varchar(1000),
  status varchar(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED','CANCELLED','COMPLETED','ARCHIVED')),
  cancellation_reason varchar(500),
  created_by uuid NOT NULL REFERENCES users(id),
  published_at timestamptz,
  completed_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(society_id, activity_code),
  CHECK (ends_at > starts_at),
  CHECK (registration_closes_at >= registration_opens_at),
  CHECK (registration_closes_at <= starts_at)
);

CREATE INDEX idx_society_activity_schedule
  ON society_activities(society_id, status, starts_at);

CREATE TABLE activity_participations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id),
  activity_id uuid NOT NULL REFERENCES society_activities(id),
  participant_id uuid NOT NULL REFERENCES users(id),
  participant_count int NOT NULL DEFAULT 1 CHECK (participant_count BETWEEN 1 AND 50),
  participant_names varchar(1000),
  contact_mobile varchar(20),
  notes varchar(500),
  amount numeric(12,2) NOT NULL DEFAULT 0 CHECK (amount >= 0),
  payment_status varchar(30) NOT NULL CHECK (payment_status IN ('FREE','PENDING','PAID','REFUND_PENDING','REFUNDED')),
  payment_method varchar(30),
  payment_reference varchar(120),
  paid_at timestamptz,
  status varchar(30) NOT NULL CHECK (status IN ('PENDING_PAYMENT','PENDING_APPROVAL','CONFIRMED','CHECKED_IN','ATTENDED','CANCELLED','REJECTED','NO_SHOW')),
  reviewed_by uuid REFERENCES users(id),
  reviewed_at timestamptz,
  rejection_reason varchar(500),
  checked_in_at timestamptz,
  attended_at timestamptz,
  cancelled_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(activity_id, participant_id)
);

CREATE INDEX idx_activity_participation_activity
  ON activity_participations(activity_id, status, created_at);
CREATE INDEX idx_activity_participation_member
  ON activity_participations(society_id, participant_id, created_at DESC);

CREATE TABLE activity_gallery_photos (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id),
  activity_id uuid NOT NULL REFERENCES society_activities(id),
  submitted_by uuid NOT NULL REFERENCES users(id),
  storage_key varchar(500) NOT NULL,
  content_type varchar(100) NOT NULL,
  caption varchar(500),
  moderation_status varchar(20) NOT NULL CHECK (moderation_status IN ('PENDING','PUBLISHED','REJECTED','REMOVED')),
  featured boolean NOT NULL DEFAULT false,
  reviewed_by uuid REFERENCES users(id),
  reviewed_at timestamptz,
  rejection_reason varchar(500),
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_gallery_activity
  ON activity_gallery_photos(activity_id, moderation_status, featured DESC, created_at DESC);

CREATE TABLE activity_events (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id),
  activity_id uuid NOT NULL REFERENCES society_activities(id) ON DELETE CASCADE,
  participation_id uuid REFERENCES activity_participations(id) ON DELETE CASCADE,
  event_type varchar(50) NOT NULL,
  note varchar(1000),
  actor_id uuid NOT NULL REFERENCES users(id),
  actor_role varchar(30) NOT NULL,
  occurred_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_event_timeline
  ON activity_events(activity_id, occurred_at);
