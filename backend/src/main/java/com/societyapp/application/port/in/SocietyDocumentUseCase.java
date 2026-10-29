package com.societyapp.application.port.in;

import com.societyapp.application.model.document.*;
import com.societyapp.application.port.out.UploadedFile;
import com.societyapp.domain.type.SocietyDocumentType;
import com.societyapp.domain.type.VerificationStatus;
import java.util.List;
import java.util.UUID;

public interface SocietyDocumentUseCase {
    DocumentCreatedView upload(
            UUID societyId, String token, SocietyDocumentType type, UploadedFile file);

    DocumentCreatedView uploadAuthenticated(
            UUID societyId, SocietyDocumentType type, UploadedFile file);

    List<SocietyDocumentView> list(UUID societyId);

    DocumentVerificationView verify(UUID societyId, UUID documentId, VerificationStatus status);
}
