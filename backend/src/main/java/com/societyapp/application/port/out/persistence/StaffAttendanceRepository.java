package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.StaffAttendance;
import java.time.LocalDate;
import java.util.*;

public interface StaffAttendanceRepository extends CrudRepositoryPort<StaffAttendance, UUID> {
    List<StaffAttendance> findByStaffUserIdOrderByAttendanceDateDesc(UUID staffUserId);

    Optional<StaffAttendance> findByStaffUserIdAndAttendanceDate(UUID staffUserId, LocalDate date);
}
