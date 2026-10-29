package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.PlatformPaymentSetting;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPlatformPaymentSettingRepository
        extends JpaRepository<PlatformPaymentSetting, UUID>,
                com.societyapp.application.port.out.persistence.PlatformPaymentSettingRepository {
    Optional<PlatformPaymentSetting> findFirstByOrderByUpdatedAtDesc();
}
