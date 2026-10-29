package com.societyapp.application.model.document;

import com.societyapp.domain.type.SocietyDocumentType;
import com.societyapp.domain.type.VerificationStatus;
import java.util.UUID;

public record DocumentCreatedView(
        UUID documentId, SocietyDocumentType documentType, VerificationStatus status) {}
