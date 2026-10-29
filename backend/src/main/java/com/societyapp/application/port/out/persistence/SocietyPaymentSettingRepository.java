package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SocietyPaymentSetting;
import java.util.*;

public interface SocietyPaymentSettingRepository
        extends CrudRepositoryPort<SocietyPaymentSetting, UUID> {
    Optional<SocietyPaymentSetting> findBySocietyId(UUID societyId);
}
