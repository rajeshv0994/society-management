package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.MemberChargeSetting;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMemberChargeSettingRepository
        extends JpaRepository<MemberChargeSetting, UUID>,
                com.societyapp.application.port.out.persistence.MemberChargeSettingRepository {
    List<MemberChargeSetting> findBySocietyIdOrderByName(UUID societyId);
}
