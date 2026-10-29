UPDATE societies society
SET total_flats = GREATEST(
  society.total_flats,
  (
    SELECT COUNT(*)::integer
    FROM flats space
    JOIN floors section ON section.id = space.floor_id
    JOIN blocks property_group ON property_group.id = section.block_id
    WHERE property_group.society_id = society.id
  )
);

UPDATE societies society
SET planned_staff = GREATEST(
  society.planned_staff,
  (
    SELECT COUNT(*)::integer
    FROM users staff
    WHERE staff.society_id = society.id
      AND staff.role = 'STAFF'
      AND staff.status = 'ACTIVE'
  )
);
