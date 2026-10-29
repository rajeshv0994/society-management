CREATE INDEX idx_audit_occurred_at ON audit_events(occurred_at DESC);
CREATE INDEX idx_audit_action_time ON audit_events(action, occurred_at DESC);
CREATE INDEX idx_audit_entity_time ON audit_events(entity_type, occurred_at DESC);
CREATE INDEX idx_audit_actor_time ON audit_events(actor_user_id, occurred_at DESC);
