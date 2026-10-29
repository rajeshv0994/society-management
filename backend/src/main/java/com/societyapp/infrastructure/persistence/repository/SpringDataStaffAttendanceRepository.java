package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.StaffAttendance;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStaffAttendanceRepository
        extends JpaRepository<StaffAttendance, UUID>,
                com.societyapp.application.port.out.persistence.StaffAttendanceRepository {
    List<StaffAttendance> findByStaffUserIdOrderByAttendanceDateDesc(UUID staffUserId);

    Optional<StaffAttendance> findByStaffUserIdAndAttendanceDate(UUID staffUserId, LocalDate date);
}
