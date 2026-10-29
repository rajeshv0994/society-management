CREATE TABLE support_tickets (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid REFERENCES societies(id) ON DELETE SET NULL,
 created_by uuid REFERENCES users(id) ON DELETE SET NULL, subject varchar(180) NOT NULL, description text NOT NULL,
 priority varchar(20) NOT NULL DEFAULT 'NORMAL', status varchar(20) NOT NULL DEFAULT 'OPEN',
 assigned_to uuid REFERENCES users(id) ON DELETE SET NULL, resolution text,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), closed_at timestamptz
);
CREATE INDEX idx_support_status_priority ON support_tickets(status,priority,created_at);

