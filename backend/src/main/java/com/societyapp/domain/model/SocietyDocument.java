package com.societyapp.domain.model;

import java.util.UUID;

public class SocietyDocument extends BaseUuidEntity {
    private UUID societyId;
    private String documentType;
    private String storageKey;
    private String verificationStatus;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
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
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
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
    public SocietyDocument() {}
}
