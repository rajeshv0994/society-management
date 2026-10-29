package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.ActivityParticipation;
import java.util.*;

public interface ActivityParticipationRepository
        extends CrudRepositoryPort<ActivityParticipation, UUID> {
    List<ActivityParticipation> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<ActivityParticipation> findBySocietyIdAndParticipantIdOrderByCreatedAtDesc(
            UUID societyId, UUID participantId);

    List<ActivityParticipation> findByActivityId(UUID activityId);

    Optional<ActivityParticipation> findByIdAndSocietyId(UUID id, UUID societyId);

    Optional<ActivityParticipation> findByActivityIdAndParticipantId(
            UUID activityId, UUID participantId);

    long countBySocietyId(UUID societyId);
}
