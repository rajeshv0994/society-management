ALTER TABLE societies ADD COLUMN registration_access_hash varchar(255);
ALTER TABLE societies ADD COLUMN subscription_status varchar(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE societies ADD COLUMN subscription_started_at timestamptz;
ALTER TABLE societies ADD COLUMN subscription_expires_at timestamptz;

CREATE TABLE subscription_orders (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
 plan_code varchar(20) NOT NULL REFERENCES subscription_plans(code), amount numeric(12,2) NOT NULL,
 currency varchar(3) NOT NULL DEFAULT 'INR', provider varchar(30) NOT NULL DEFAULT 'MOCK',
 provider_reference varchar(120), status varchar(20) NOT NULL DEFAULT 'CREATED',
 created_at timestamptz NOT NULL DEFAULT now(), paid_at timestamptz, UNIQUE(provider,provider_reference)
);

CREATE TABLE verification_challenges (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 channel varchar(10) NOT NULL CHECK(channel IN ('EMAIL','MOBILE')), destination varchar(180) NOT NULL,
 code_hash varchar(255) NOT NULL, expires_at timestamptz NOT NULL, attempts integer NOT NULL DEFAULT 0,
 verified_at timestamptz, created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE notification_outbox (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), society_id uuid REFERENCES societies(id) ON DELETE CASCADE,
 channel varchar(20) NOT NULL CHECK(channel IN ('EMAIL','SMS','WHATSAPP','PUSH')),
 destination varchar(180) NOT NULL, template_code varchar(80) NOT NULL, payload jsonb NOT NULL DEFAULT '{}'::jsonb,
 status varchar(20) NOT NULL DEFAULT 'PENDING', attempts integer NOT NULL DEFAULT 0,
 created_at timestamptz NOT NULL DEFAULT now(), sent_at timestamptz, last_error text
);

CREATE TABLE audit_events (
 id bigserial PRIMARY KEY, society_id uuid REFERENCES societies(id) ON DELETE SET NULL,
 actor_user_id uuid REFERENCES users(id) ON DELETE SET NULL, action varchar(100) NOT NULL,
 entity_type varchar(80) NOT NULL, entity_id varchar(100), details jsonb NOT NULL DEFAULT '{}'::jsonb,
 occurred_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_society_status ON subscription_orders(society_id,status);
CREATE INDEX idx_challenges_user_channel ON verification_challenges(user_id,channel,created_at DESC);
CREATE INDEX idx_outbox_status_created ON notification_outbox(status,created_at);
CREATE INDEX idx_audit_society_time ON audit_events(society_id,occurred_at DESC);
