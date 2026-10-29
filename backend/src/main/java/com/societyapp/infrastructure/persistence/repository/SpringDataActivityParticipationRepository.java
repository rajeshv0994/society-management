package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.ActivityParticipation;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataActivityParticipationRepository
        extends JpaRepository<ActivityParticipation, UUID>,
                com.societyapp.application.port.out.persistence.ActivityParticipationRepository {
    List<ActivityParticipation> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<ActivityParticipation> findBySocietyIdAndParticipantIdOrderByCreatedAtDesc(
            UUID societyId, UUID participantId);

    List<ActivityParticipation> findByActivityId(UUID activityId);

    Optional<ActivityParticipation> findByIdAndSocietyId(UUID id, UUID societyId);

    Optional<ActivityParticipation> findByActivityIdAndParticipantId(
            UUID activityId, UUID participantId);

    long countBySocietyId(UUID societyId);
}
