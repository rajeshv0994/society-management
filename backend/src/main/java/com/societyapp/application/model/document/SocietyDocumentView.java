package com.societyapp.application.model.document;

import com.societyapp.domain.type.SocietyDocumentType;
import com.societyapp.domain.type.VerificationStatus;
import java.util.UUID;

public record SocietyDocumentView(
        UUID id,
        SocietyDocumentType documentType,
        String storageKey,
        VerificationStatus verificationStatus) {}
