ALTER TABLE amenities
  ADD COLUMN location varchar(180),
  ADD COLUMN booking_approval_required boolean NOT NULL DEFAULT false,
  ADD COLUMN slot_minutes integer NOT NULL DEFAULT 60,
  ADD COLUMN advance_booking_days integer NOT NULL DEFAULT 30,
  ADD COLUMN cancellation_hours integer NOT NULL DEFAULT 4,
  ADD COLUMN instructions varchar(500),
  ADD COLUMN created_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now(),
  ADD CONSTRAINT chk_amenity_slot_minutes CHECK(slot_minutes BETWEEN 15 AND 1440),
  ADD CONSTRAINT chk_amenity_advance_days CHECK(advance_booking_days BETWEEN 1 AND 365),
  ADD CONSTRAINT chk_amenity_cancellation_hours CHECK(cancellation_hours BETWEEN 0 AND 168);

ALTER TABLE amenity_bookings
  ADD COLUMN booking_number varchar(50),
  ADD COLUMN purpose varchar(240),
  ADD COLUMN attendees integer NOT NULL DEFAULT 1,
  ADD COLUMN payment_status varchar(30) NOT NULL DEFAULT 'FREE',
  ADD COLUMN payment_method varchar(40),
  ADD COLUMN payment_reference varchar(120),
  ADD COLUMN reviewed_by uuid REFERENCES users(id),
  ADD COLUMN reviewed_at timestamptz,
  ADD COLUMN rejection_reason varchar(300),
  ADD COLUMN cancelled_at timestamptz,
  ADD COLUMN checked_in_at timestamptz,
  ADD COLUMN checked_out_at timestamptz,
  ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();

UPDATE amenity_bookings
SET booking_number = 'AMB-' || to_char(created_at, 'YYYYMM') || '-' || upper(substr(replace(id::text, '-', ''), 1, 10)),
    payment_status = CASE WHEN amount > 0 THEN 'PAID' ELSE 'FREE' END;

ALTER TABLE amenity_bookings
  ALTER COLUMN booking_number SET NOT NULL,
  ADD CONSTRAINT uq_amenity_booking_number UNIQUE(society_id, booking_number),
  ADD CONSTRAINT chk_amenity_booking_attendees CHECK(attendees > 0),
  ADD CONSTRAINT chk_amenity_booking_payment_status
    CHECK(payment_status IN ('FREE','PENDING','PAID','REFUND_PENDING','REFUNDED'));

CREATE INDEX idx_amenity_booking_society_start
  ON amenity_bookings(society_id, starts_at DESC);
