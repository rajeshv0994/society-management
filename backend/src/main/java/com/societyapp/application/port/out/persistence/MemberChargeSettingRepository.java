package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.MemberChargeSetting;
import java.util.*;

public interface MemberChargeSettingRepository
        extends CrudRepositoryPort<MemberChargeSetting, UUID> {
    List<MemberChargeSetting> findBySocietyIdOrderByName(UUID societyId);
}
