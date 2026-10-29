package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.ComplaintUpdate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataComplaintUpdateRepository
        extends JpaRepository<ComplaintUpdate, UUID>,
                com.societyapp.application.port.out.persistence.ComplaintUpdateRepository {
    List<ComplaintUpdate> findByComplaintIdOrderByCreatedAt(UUID complaintId);
}
