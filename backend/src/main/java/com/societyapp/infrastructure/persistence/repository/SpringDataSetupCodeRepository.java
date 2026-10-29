package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SetupCode;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSetupCodeRepository
        extends JpaRepository<SetupCode, UUID>,
                com.societyapp.application.port.out.persistence.SetupCodeRepository {
    List<SetupCode> findByStatusOrderByCreatedAtDesc(String status);
}
