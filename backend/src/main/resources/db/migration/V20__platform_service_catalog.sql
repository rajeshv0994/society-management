CREATE TABLE platform_services (
  code varchar(40) PRIMARY KEY,
  name varchar(100) NOT NULL,
  description text,
  status varchar(20) NOT NULL CHECK (status IN ('ACTIVE', 'COMING_SOON', 'RETIRED')),
  sort_order integer NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO platform_services(code, name, description, status, sort_order) VALUES
 ('RESIDENTS', 'Residents', 'Resident records, households and approvals', 'ACTIVE', 10),
 ('STAFF', 'Staff', 'Staff profiles, permissions and tasks', 'ACTIVE', 20),
 ('VISITORS', 'Visitors', 'Visitor approvals and gate activity', 'ACTIVE', 30),
 ('COMPLAINTS', 'Complaints', 'Complaint tracking and resolution', 'ACTIVE', 40),
 ('BILLING', 'Billing', 'Maintenance billing and invoices', 'ACTIVE', 50),
 ('PAYMENTS', 'Payments', 'Payment records and reconciliation', 'ACTIVE', 60),
 ('AMENITIES', 'Amenities', 'Amenity setup and bookings', 'ACTIVE', 70),
 ('NOTICES', 'Notices', 'Society notices and announcements', 'ACTIVE', 80),
 ('DOCUMENTS', 'Documents', 'Society and resident documents', 'ACTIVE', 90),
 ('PARKING', 'Parking', 'Parking slot configuration', 'ACTIVE', 100),
 ('VENDORS', 'Vendors', 'Vendor directory and contacts', 'ACTIVE', 110),
 ('DELIVERIES', 'Deliveries', 'Delivery tracking and resident notifications', 'COMING_SOON', 120),
 ('EMERGENCY_INCIDENTS', 'Emergency & incidents', 'Emergency reporting and incident workflows', 'COMING_SOON', 130);
