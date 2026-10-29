package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SocietyPaymentSetting;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSocietyPaymentSettingRepository
        extends JpaRepository<SocietyPaymentSetting, UUID>,
                com.societyapp.application.port.out.persistence.SocietyPaymentSettingRepository {
    Optional<SocietyPaymentSetting> findBySocietyId(UUID societyId);
}
