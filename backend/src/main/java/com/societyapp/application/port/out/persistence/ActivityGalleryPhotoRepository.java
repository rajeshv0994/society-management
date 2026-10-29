package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.ActivityGalleryPhoto;
import java.util.*;

public interface ActivityGalleryPhotoRepository
        extends CrudRepositoryPort<ActivityGalleryPhoto, UUID> {
    List<ActivityGalleryPhoto> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<ActivityGalleryPhoto> findByActivityIdOrderByFeaturedDescCreatedAtDesc(UUID activityId);

    Optional<ActivityGalleryPhoto> findByIdAndSocietyId(UUID id, UUID societyId);

    long countBySocietyIdAndModerationStatus(UUID societyId, String status);
}
