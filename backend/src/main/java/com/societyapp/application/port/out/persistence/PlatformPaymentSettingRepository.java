package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.PlatformPaymentSetting;
import java.util.*;

public interface PlatformPaymentSettingRepository
        extends CrudRepositoryPort<PlatformPaymentSetting, UUID> {
    Optional<PlatformPaymentSetting> findFirstByOrderByUpdatedAtDesc();
}
