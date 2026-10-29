ALTER TABLE staff_profiles
  ADD COLUMN date_of_birth date,
  ADD COLUMN gender varchar(20),
  ADD COLUMN alternate_mobile varchar(20),
  ADD COLUMN bank_account_name varchar(150),
  ADD COLUMN bank_account_number varchar(40),
  ADD COLUMN bank_ifsc varchar(20),
  ADD COLUMN upi_id varchar(100);

ALTER TABLE staff_profiles
  ADD CONSTRAINT chk_staff_gender
  CHECK (gender IS NULL OR gender IN ('MALE','FEMALE','OTHER','PREFER_NOT_TO_SAY'));
