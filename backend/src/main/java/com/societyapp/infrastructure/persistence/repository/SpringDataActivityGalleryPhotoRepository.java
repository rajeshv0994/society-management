package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.ActivityGalleryPhoto;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataActivityGalleryPhotoRepository
        extends JpaRepository<ActivityGalleryPhoto, UUID>,
                com.societyapp.application.port.out.persistence.ActivityGalleryPhotoRepository {
    List<ActivityGalleryPhoto> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<ActivityGalleryPhoto> findByActivityIdOrderByFeaturedDescCreatedAtDesc(UUID activityId);

    Optional<ActivityGalleryPhoto> findByIdAndSocietyId(UUID id, UUID societyId);

    long countBySocietyIdAndModerationStatus(UUID societyId, String status);
}
