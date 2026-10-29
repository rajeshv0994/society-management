package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.ResidentDocument;
import java.util.*;

public interface ResidentDocumentRepository extends CrudRepositoryPort<ResidentDocument, UUID> {
    List<ResidentDocument> findByRegistrationId(UUID registrationId);
}
