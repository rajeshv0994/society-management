ALTER TABLE amenities
  ADD COLUMN amenity_type varchar(40) NOT NULL DEFAULT 'OTHER',
  ADD COLUMN amenity_code varchar(40),
  ADD COLUMN pricing_type varchar(20) NOT NULL DEFAULT 'HOURLY',
  ADD COLUMN booking_charge numeric(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN refundable_deposit numeric(12,2) NOT NULL DEFAULT 0;

UPDATE amenities
SET amenity_code = upper(substr(regexp_replace(name, '[^A-Za-z0-9]+', '', 'g'), 1, 24))
                   || '-' || upper(substr(replace(id::text, '-', ''), 1, 6)),
    amenity_type = CASE
      WHEN lower(name) LIKE '%gym%' THEN 'GYM'
      WHEN lower(name) LIKE '%pool%' OR lower(name) LIKE '%swim%' THEN 'SWIMMING_POOL'
      WHEN lower(name) LIKE '%hall%' OR lower(name) LIKE '%community%' THEN 'COMMUNITY_CENTER'
      WHEN lower(name) LIKE '%court%' OR lower(name) LIKE '%sport%' THEN 'SPORTS_COURT'
      WHEN lower(name) LIKE '%guest%' THEN 'GUEST_ROOM'
      ELSE 'OTHER'
    END;

ALTER TABLE amenities
  ALTER COLUMN amenity_code SET NOT NULL,
  ADD CONSTRAINT uq_amenity_instance_code UNIQUE(society_id, amenity_code),
  ADD CONSTRAINT chk_amenity_type CHECK(amenity_type IN (
    'COMMUNITY_CENTER','GYM','SWIMMING_POOL','SPORTS_COURT','GUEST_ROOM','PLAY_AREA','OTHER'
  )),
  ADD CONSTRAINT chk_amenity_pricing_type CHECK(pricing_type IN ('FREE','HOURLY','PER_SLOT','FIXED')),
  ADD CONSTRAINT chk_amenity_booking_charge CHECK(booking_charge >= 0),
  ADD CONSTRAINT chk_amenity_deposit CHECK(refundable_deposit >= 0);

ALTER TABLE amenity_bookings
  ADD COLUMN base_charge numeric(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN deposit_amount numeric(12,2) NOT NULL DEFAULT 0;

UPDATE amenity_bookings SET base_charge = amount;

ALTER TABLE amenity_bookings
  ADD CONSTRAINT chk_amenity_booking_base_charge CHECK(base_charge >= 0),
  ADD CONSTRAINT chk_amenity_booking_deposit CHECK(deposit_amount >= 0);
