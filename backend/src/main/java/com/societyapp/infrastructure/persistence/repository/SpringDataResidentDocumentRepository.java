package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.ResidentDocument;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataResidentDocumentRepository
        extends JpaRepository<ResidentDocument, UUID>,
                com.societyapp.application.port.out.persistence.ResidentDocumentRepository {
    List<ResidentDocument> findByRegistrationId(UUID registrationId);
}
