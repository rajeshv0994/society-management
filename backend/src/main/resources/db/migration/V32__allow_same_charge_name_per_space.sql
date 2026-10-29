ALTER TABLE member_charge_settings
  DROP CONSTRAINT member_charge_settings_society_id_name_key;

CREATE UNIQUE INDEX uq_member_charge_settings_scope_name
ON member_charge_settings(
  society_id,
  name,
  COALESCE(flat_id, '00000000-0000-0000-0000-000000000000'::uuid)
);
