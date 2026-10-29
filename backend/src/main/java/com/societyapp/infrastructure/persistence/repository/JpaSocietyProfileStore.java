package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.application.model.SocietyLogoReference;
import com.societyapp.application.model.SocietyProfileUpdate;
import com.societyapp.application.model.SocietyProfileView;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.persistence.SocietyProfileStore;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.domain.type.SocietyStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSocietyProfileStore implements SocietyProfileStore {
    private final SocietyRepository societies;
    private final JsonCodec json;

    public JpaSocietyProfileStore(SocietyRepository societies, JsonCodec json) {
        this.societies = societies;
        this.json = json;
    }

    public SocietyProfileView findById(UUID id) {
        var society =
                societies
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Society not found"));
        return new SocietyProfileView(
                society.getId(),
                society.getName(),
                society.getRegistrationNumber(),
                society.getSocietyCode(),
                society.getSocietyType(),
                society.getTotalBlocks(),
                society.getTotalFlats(),
                society.getPlannedStaff(),
                society.getApproximateResidents(),
                society.getAddress(),
                society.getCity(),
                society.getState(),
                society.getPinCode(),
                society.getContactPerson(),
                society.getEmail(),
                society.getMobile(),
                society.getPlanCode(),
                json.readStrings(society.getEnabledModules()),
                SocietyStatus.from(society.getStatus()),
                society.getDescription(),
                society.getWebsite(),
                society.getLogoStorageKey() == null
                        ? null
                        : "/api/public/societies/" + society.getId() + "/logo",
                society.getUpdatedAt());
    }

    public void update(UUID id, SocietyProfileUpdate request) {
        var society =
                societies
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Society not found"));
        society.setName(request.name());
        society.setDescription(request.description());
        society.setWebsite(request.website());
        society.setContactPerson(request.contactPerson());
        society.setEmail(request.email());
        society.setMobile(request.mobile());
        society.setAddress(request.address());
        society.setCity(request.city());
        society.setState(request.state());
        society.setPinCode(request.pinCode());
        society.setUpdatedAt(java.time.OffsetDateTime.now());
        societies.save(society);
    }

    public boolean exists(UUID id) {
        return societies.existsById(id);
    }

    public void updateLogo(UUID id, String storageKey, String contentType) {
        var society =
                societies
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Society not found"));
        society.setLogoStorageKey(storageKey);
        society.setLogoContentType(contentType);
        society.setUpdatedAt(java.time.OffsetDateTime.now());
        societies.save(society);
    }

    public Optional<SocietyLogoReference> findLogo(UUID id) {
        return societies
                .findById(id)
                .filter(society -> society.getLogoStorageKey() != null)
                .map(
                        society ->
                                new SocietyLogoReference(
                                        society.getLogoStorageKey(), society.getLogoContentType()));
    }
}
