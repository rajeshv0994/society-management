package com.societyapp.application.port.out.persistence;

import com.societyapp.application.model.SocietyLogoReference;
import com.societyapp.application.model.SocietyProfileUpdate;
import com.societyapp.application.model.SocietyProfileView;
import java.util.UUID;

public interface SocietyProfileStore {
    SocietyProfileView findById(UUID id);

    void update(UUID id, SocietyProfileUpdate update);

    boolean exists(UUID id);

    void updateLogo(UUID id, String storageKey, String contentType);

    java.util.Optional<SocietyLogoReference> findLogo(UUID id);
}
