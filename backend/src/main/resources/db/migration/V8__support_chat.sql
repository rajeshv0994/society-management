CREATE TABLE support_ticket_messages (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), ticket_id uuid NOT NULL REFERENCES support_tickets(id) ON DELETE CASCADE,
 sender_id uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT, message text NOT NULL,
 message_type varchar(20) NOT NULL DEFAULT 'TEXT', attachment_key text,
 created_at timestamptz NOT NULL DEFAULT now(), read_at timestamptz
);
CREATE INDEX idx_support_messages_ticket_time ON support_ticket_messages(ticket_id,created_at);

