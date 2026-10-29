ALTER TABLE parking_slots
  ADD COLUMN created_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();

ALTER TABLE vendors
  ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();

ALTER TABLE complaint_categories
  ADD COLUMN created_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();

ALTER TABLE maintenance_charge_rules
  ADD COLUMN created_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();

ALTER TABLE parking_slots
  ADD CONSTRAINT chk_parking_slot_type
    CHECK (slot_type IN ('CAR','BIKE','VISITOR','ACCESSIBLE')),
  ADD CONSTRAINT chk_parking_status
    CHECK (status IN ('AVAILABLE','ALLOCATED','RESERVED','MAINTENANCE','INACTIVE')),
  ADD CONSTRAINT chk_parking_allocation
    CHECK (
      (status = 'ALLOCATED' AND flat_id IS NOT NULL AND vehicle_number IS NOT NULL)
      OR (status <> 'ALLOCATED')
    );

ALTER TABLE vendors
  ADD CONSTRAINT chk_vendor_status CHECK (status IN ('ACTIVE','INACTIVE'));

ALTER TABLE complaint_categories
  ADD CONSTRAINT chk_complaint_category_sla CHECK (sla_hours BETWEEN 1 AND 8760);

ALTER TABLE maintenance_charge_rules
  ADD CONSTRAINT chk_charge_effective_dates
    CHECK (effective_to IS NULL OR effective_to >= effective_from);

CREATE UNIQUE INDEX uq_parking_society_number_ci
  ON parking_slots(society_id, lower(slot_number));

CREATE UNIQUE INDEX uq_vendor_society_name_service_ci
  ON vendors(society_id, lower(name), lower(service_type));

CREATE UNIQUE INDEX uq_complaint_category_society_name_ci
  ON complaint_categories(society_id, lower(name));

CREATE INDEX idx_charge_rules_society_active_dates
  ON maintenance_charge_rules(society_id, active, effective_from, effective_to);
