package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SocietyDocument;
import java.util.*;

public interface SocietyDocumentRepository extends CrudRepositoryPort<SocietyDocument, UUID> {
    List<SocietyDocument> findBySocietyId(UUID societyId);

    Optional<SocietyDocument> findByIdAndSocietyId(UUID id, UUID societyId);
}
