package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Complaint;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataComplaintRepository
        extends JpaRepository<Complaint, UUID>,
                com.societyapp.application.port.out.persistence.ComplaintRepository {
    List<Complaint> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<Complaint> findBySocietyIdAndRaisedByOrderByCreatedAtDesc(UUID societyId, UUID raisedBy);

    Optional<Complaint> findByIdAndSocietyId(UUID id, UUID societyId);
}
