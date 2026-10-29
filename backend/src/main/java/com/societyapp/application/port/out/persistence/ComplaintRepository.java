package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Complaint;
import java.util.*;

public interface ComplaintRepository extends CrudRepositoryPort<Complaint, UUID> {
    List<Complaint> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<Complaint> findBySocietyIdAndRaisedByOrderByCreatedAtDesc(UUID societyId, UUID raisedBy);

    Optional<Complaint> findByIdAndSocietyId(UUID id, UUID societyId);
}
