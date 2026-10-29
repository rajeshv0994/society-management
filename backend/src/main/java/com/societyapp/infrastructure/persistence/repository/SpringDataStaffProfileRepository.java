package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.StaffProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStaffProfileRepository
        extends JpaRepository<StaffProfile, UUID>,
                com.societyapp.application.port.out.persistence.StaffProfileRepository {}
