package com.societyapp.controller.mapper;

import com.societyapp.application.model.document.*;
import com.societyapp.controller.model.SocietyDocumentResponse;
import org.springframework.stereotype.Component;

@Component
public class SocietyDocumentHttpMapper {
    public SocietyDocumentResponse.Created toResponse(DocumentCreatedView v) {
        return new SocietyDocumentResponse.Created(
                v.documentId(), v.documentType().code(), v.status().code());
    }

    public SocietyDocumentResponse.Document toResponse(SocietyDocumentView v) {
        return new SocietyDocumentResponse.Document(
                v.id(), v.documentType().code(), v.storageKey(), v.verificationStatus().code());
    }

    public SocietyDocumentResponse.Verification toResponse(DocumentVerificationView v) {
        return new SocietyDocumentResponse.Verification(v.documentId(), v.status().code());
    }
}
