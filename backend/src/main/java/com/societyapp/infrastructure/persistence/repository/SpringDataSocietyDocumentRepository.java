package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SocietyDocument;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSocietyDocumentRepository
        extends JpaRepository<SocietyDocument, UUID>,
                com.societyapp.application.port.out.persistence.SocietyDocumentRepository {
    List<SocietyDocument> findBySocietyId(UUID societyId);

    Optional<SocietyDocument> findByIdAndSocietyId(UUID id, UUID societyId);
}
