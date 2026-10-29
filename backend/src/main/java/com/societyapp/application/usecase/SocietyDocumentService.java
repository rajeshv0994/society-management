package com.societyapp.application.usecase;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.document.*;
import com.societyapp.application.port.in.SocietyDocumentUseCase;
import com.societyapp.application.port.out.DocumentStorage;
import com.societyapp.application.port.out.PasswordCodec;
import com.societyapp.application.port.out.UploadedFile;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.SocietyDocument;
import com.societyapp.domain.type.SocietyDocumentType;
import com.societyapp.domain.type.VerificationStatus;
import java.util.*;

public class SocietyDocumentService implements SocietyDocumentUseCase {
    private final SocietyRepository societies;
    private final SocietyDocumentRepository documents;
    private final DocumentStorage storage;
    private final PasswordCodec passwords;

    public SocietyDocumentService(
            SocietyRepository societies,
            SocietyDocumentRepository documents,
            DocumentStorage storage,
            PasswordCodec passwords) {
        this.societies = societies;
        this.documents = documents;
        this.storage = storage;
        this.passwords = passwords;
    }

    public DocumentCreatedView upload(
            UUID societyId, String token, SocietyDocumentType type, UploadedFile file) {
        var society =
                societies
                        .findById(societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        if (society.getRegistrationAccessHash() == null
                || !passwords.matches(token, society.getRegistrationAccessHash())) {
            throw new IllegalArgumentException("Invalid registration token");
        }
        var document = new SocietyDocument();
        document.setSocietyId(societyId);
        document.setDocumentType(type.code());
        document.setStorageKey(storage.store(societyId, file));
        document.setVerificationStatus(VerificationStatus.PENDING.code());
        document = documents.save(document);
        storage.attach(
                document.getStorageKey(),
                "SOCIETY_DOCUMENT",
                document.getId(),
                type.code() + " document");
        return new DocumentCreatedView(document.getId(), type, VerificationStatus.PENDING);
    }

    public DocumentCreatedView uploadAuthenticated(
            UUID societyId, SocietyDocumentType type, UploadedFile file) {
        if (!societies.existsById(societyId)) {
            throw new ResourceNotFoundException("Society not found");
        }
        var document = new SocietyDocument();
        document.setSocietyId(societyId);
        document.setDocumentType(type.code());
        document.setStorageKey(storage.store(societyId, file));
        document.setVerificationStatus(VerificationStatus.PENDING.code());
        document = documents.save(document);
        storage.attach(
                document.getStorageKey(),
                "SOCIETY_DOCUMENT",
                document.getId(),
                type.code() + " document");
        return new DocumentCreatedView(document.getId(), type, VerificationStatus.PENDING);
    }

    public List<SocietyDocumentView> list(UUID societyId) {
        return documents.findBySocietyId(societyId).stream()
                .sorted(Comparator.comparing(SocietyDocument::getDocumentType))
                .map(
                        document ->
                                new SocietyDocumentView(
                                        document.getId(),
                                        SocietyDocumentType.from(document.getDocumentType()),
                                        document.getStorageKey(),
                                        VerificationStatus.from(document.getVerificationStatus())))
                .toList();
    }

    public DocumentVerificationView verify(
            UUID societyId, UUID documentId, VerificationStatus status) {
        if (status == VerificationStatus.PENDING) {
            throw new IllegalArgumentException("Status must be VERIFIED or REJECTED");
        }
        var document =
                documents
                        .findByIdAndSocietyId(documentId, societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        document.setVerificationStatus(status.code());
        documents.save(document);
        return new DocumentVerificationView(documentId, status);
    }
}
