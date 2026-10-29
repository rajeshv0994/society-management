UPDATE platform_services
SET status = 'ACTIVE', updated_at = now()
WHERE code = 'DELIVERIES';

UPDATE societies
SET enabled_modules = enabled_modules || '["DELIVERIES"]'::jsonb,
    updated_at = now()
WHERE NOT enabled_modules @> '["DELIVERIES"]'::jsonb;

CREATE TABLE deliveries (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  flat_id uuid NOT NULL REFERENCES flats(id),
  resident_id uuid NOT NULL REFERENCES users(id),
  delivery_number varchar(40) NOT NULL,
  tracking_number varchar(100),
  provider varchar(80) NOT NULL,
  sender_name varchar(150),
  delivery_person_name varchar(150),
  delivery_person_mobile varchar(20),
  parcel_type varchar(30) NOT NULL,
  package_count integer NOT NULL DEFAULT 1,
  parcel_size varchar(20) NOT NULL DEFAULT 'MEDIUM',
  fragile boolean NOT NULL DEFAULT false,
  storage_location varchar(80),
  expected_at timestamptz,
  notes varchar(500),
  status varchar(30) NOT NULL,
  resident_decision varchar(20),
  resident_decision_at timestamptz,
  authorized_collector varchar(150),
  pickup_code varchar(6),
  cod_amount numeric(12,2) NOT NULL DEFAULT 0,
  cod_payment_status varchar(20) NOT NULL DEFAULT 'NOT_REQUIRED',
  cod_payment_method varchar(30),
  cod_payment_reference varchar(120),
  cod_paid_at timestamptz,
  received_by uuid REFERENCES users(id),
  received_at timestamptz,
  collected_by_name varchar(150),
  handed_over_by uuid REFERENCES users(id),
  collected_at timestamptz,
  delivered_at timestamptz,
  returned_at timestamptz,
  closed_reason varchar(300),
  created_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT uq_delivery_number UNIQUE(society_id, delivery_number),
  CONSTRAINT chk_delivery_status CHECK(status IN (
    'EXPECTED','RECEIVED','READY_FOR_PICKUP','OUT_FOR_DELIVERY','COLLECTED','DELIVERED',
    'REJECTED','RETURNED','CANCELLED','DAMAGED','LOST'
  )),
  CONSTRAINT chk_delivery_decision CHECK(resident_decision IS NULL OR resident_decision IN ('ACCEPTED','REJECTED')),
  CONSTRAINT chk_delivery_parcel_type CHECK(parcel_type IN ('DOCUMENT','PACKAGE','FOOD','GROCERY','MEDICINE','OTHER')),
  CONSTRAINT chk_delivery_parcel_size CHECK(parcel_size IN ('SMALL','MEDIUM','LARGE','OVERSIZED')),
  CONSTRAINT chk_delivery_count CHECK(package_count BETWEEN 1 AND 100),
  CONSTRAINT chk_delivery_cod_amount CHECK(cod_amount >= 0),
  CONSTRAINT chk_delivery_cod_status CHECK(cod_payment_status IN ('NOT_REQUIRED','PENDING','PAID','WAIVED','REFUNDED')),
  CONSTRAINT chk_delivery_pickup_code CHECK(pickup_code IS NULL OR pickup_code ~ '^[0-9]{6}$')
);

CREATE UNIQUE INDEX uq_delivery_tracking_active
  ON deliveries(society_id, upper(tracking_number))
  WHERE tracking_number IS NOT NULL AND status NOT IN ('RETURNED','CANCELLED','COLLECTED','DELIVERED');
CREATE INDEX idx_deliveries_society_status ON deliveries(society_id, status, created_at DESC);
CREATE INDEX idx_deliveries_resident ON deliveries(society_id, resident_id, created_at DESC);
CREATE INDEX idx_deliveries_flat ON deliveries(society_id, flat_id, created_at DESC);

CREATE TABLE delivery_events (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  delivery_id uuid NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  event_type varchar(40) NOT NULL,
  from_status varchar(30),
  to_status varchar(30),
  note varchar(500),
  actor_id uuid NOT NULL REFERENCES users(id),
  actor_role varchar(30) NOT NULL,
  occurred_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_delivery_events_delivery ON delivery_events(delivery_id, occurred_at);
