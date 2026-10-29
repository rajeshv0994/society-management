package com.societyapp.application.usecase;

import com.societyapp.application.model.*;
import com.societyapp.application.port.in.SocietyProfileUseCase;
import com.societyapp.application.port.out.DocumentStorage;
import com.societyapp.application.port.out.UploadedFile;
import com.societyapp.application.port.out.persistence.SocietyProfileStore;
import java.util.UUID;

public class SocietyProfileService implements SocietyProfileUseCase {
    private final SocietyProfileStore profiles;
    private final DocumentStorage storage;

    public SocietyProfileService(SocietyProfileStore profiles, DocumentStorage storage) {
        this.profiles = profiles;
        this.storage = storage;
    }

    public SocietyProfileView get(UUID societyId) {
        return profiles.findById(societyId);
    }

    public SocietyProfileView update(UUID societyId, SocietyProfileUpdate update) {
        profiles.update(societyId, update);
        return profiles.findById(societyId);
    }

    public SocietyLogoUpdateView uploadLogo(UUID societyId, UploadedFile file) {
        if (!profiles.exists(societyId)) throw new IllegalArgumentException("Society not found");
        String storageKey = storage.storeImage(societyId, file);
        profiles.updateLogo(societyId, storageKey, file.contentType());
        storage.attach(storageKey, "SOCIETY_LOGO", societyId, "Society logo");
        return new SocietyLogoUpdateView(
                societyId, "/api/public/societies/" + societyId + "/logo", true);
    }

    public LogoContent getLogo(UUID societyId) {
        return profiles.findLogo(societyId)
                .map(logo -> new LogoContent(storage.load(logo.storageKey()), logo.contentType()))
                .orElse(null);
    }
}
