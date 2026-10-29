ALTER TABLE requirement_requests
  ADD COLUMN estimated_staff integer;

ALTER TABLE requirement_requests
  ADD CONSTRAINT chk_requirement_estimated_staff
    CHECK (estimated_staff IS NULL OR estimated_staff >= 0);
