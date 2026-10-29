CREATE TABLE storage_policies (
  id smallint PRIMARY KEY CHECK (id = 1),
  default_free_gb integer NOT NULL CHECK (default_free_gb >= 0),
  purchase_block_gb integer NOT NULL CHECK (purchase_block_gb > 0),
  price_per_block_monthly numeric(12,2) NOT NULL CHECK (price_per_block_monthly >= 0),
  updated_by uuid REFERENCES users(id) ON DELETE SET NULL,
  updated_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO storage_policies(id, default_free_gb, purchase_block_gb, price_per_block_monthly)
VALUES (1, 5, 5, 199.00);

CREATE TABLE society_storage_accounts (
  society_id uuid PRIMARY KEY REFERENCES societies(id) ON DELETE CASCADE,
  purchased_bytes bigint NOT NULL DEFAULT 0 CHECK (purchased_bytes >= 0),
  updated_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO society_storage_accounts(society_id)
SELECT id FROM societies;

CREATE TABLE storage_objects (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  storage_key text NOT NULL UNIQUE,
  category varchar(30) NOT NULL,
  original_filename varchar(255),
  content_type varchar(100),
  size_bytes bigint NOT NULL CHECK (size_bytes >= 0),
  source_type varchar(40) NOT NULL DEFAULT 'UPLOAD',
  source_id uuid,
  display_name varchar(255),
  status varchar(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','DELETED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz,
  deleted_by uuid REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_storage_objects_society_status
ON storage_objects(society_id, status, created_at DESC);

CREATE TABLE storage_addon_purchases (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  society_id uuid NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
  purchased_by uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  blocks integer NOT NULL CHECK (blocks > 0),
  block_size_gb integer NOT NULL CHECK (block_size_gb > 0),
  additional_bytes bigint NOT NULL CHECK (additional_bytes > 0),
  price_per_block_monthly numeric(12,2) NOT NULL CHECK (price_per_block_monthly >= 0),
  monthly_amount numeric(12,2) NOT NULL CHECK (monthly_amount >= 0),
  status varchar(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CANCELLED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  cancelled_at timestamptz
);

CREATE INDEX idx_storage_purchases_society_status
ON storage_addon_purchases(society_id, status, created_at DESC);
