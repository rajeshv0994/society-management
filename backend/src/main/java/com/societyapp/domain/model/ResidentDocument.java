package com.societyapp.domain.model;

import java.util.UUID;

public class ResidentDocument extends BaseUuidEntity {
    private UUID registrationId;
    private String documentType;
    private String storageKey;
    private String verificationStatus;

    @java.lang.SuppressWarnings("all")
    public UUID getRegistrationId() {
        return this.registrationId;
    }

    @java.lang.SuppressWarnings("all")
    public String getDocumentType() {
        return this.documentType;
    }

    @java.lang.SuppressWarnings("all")
    public String getStorageKey() {
        return this.storageKey;
    }

    @java.lang.SuppressWarnings("all")
    public String getVerificationStatus() {
        return this.verificationStatus;
    }

    @java.lang.SuppressWarnings("all")
    public void setRegistrationId(final UUID registrationId) {
        this.registrationId = registrationId;
    }

    @java.lang.SuppressWarnings("all")
    public void setDocumentType(final String documentType) {
        this.documentType = documentType;
    }

    @java.lang.SuppressWarnings("all")
    public void setStorageKey(final String storageKey) {
        this.storageKey = storageKey;
    }

    @java.lang.SuppressWarnings("all")
    public void setVerificationStatus(final String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    @java.lang.SuppressWarnings("all")
    public ResidentDocument() {}
}
