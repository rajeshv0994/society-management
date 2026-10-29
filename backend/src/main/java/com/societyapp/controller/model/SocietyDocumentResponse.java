package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public final class SocietyDocumentResponse {
    private SocietyDocumentResponse() {}

    public record Created(UUID documentId, String documentType, String status) {}

    public record Document(
            UUID id,
            @JsonProperty("document_type") String documentType,
            @JsonProperty("storage_key") String storageKey,
            @JsonProperty("verification_status") String verificationStatus) {}

    public record Verification(UUID documentId, String status) {}
}
