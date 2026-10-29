ALTER TABLE audit_events
    ADD COLUMN sensitive boolean NOT NULL DEFAULT false;

UPDATE audit_events
SET sensitive = true
WHERE society_id IS NOT NULL;

CREATE INDEX idx_audit_society_sensitive_time
    ON audit_events(society_id, sensitive, occurred_at DESC);
