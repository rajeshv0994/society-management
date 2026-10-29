package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserAccount extends BaseUuidEntity {
    private UUID societyId;
    private String fullName;
    private String email;
    private String mobile;
    private String passwordHash;
    private String role;
    private String status;
    private boolean emailVerified;
    private boolean mobileVerified;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getFullName() {
        return this.fullName;
    }

    @java.lang.SuppressWarnings("all")
    public String getEmail() {
        return this.email;
    }

    @java.lang.SuppressWarnings("all")
    public String getMobile() {
        return this.mobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getPasswordHash() {
        return this.passwordHash;
    }

    @java.lang.SuppressWarnings("all")
    public String getRole() {
        return this.role;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isEmailVerified() {
        return this.emailVerified;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isMobileVerified() {
        return this.mobileVerified;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmail(final String email) {
        this.email = email;
    }

    @java.lang.SuppressWarnings("all")
    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @java.lang.SuppressWarnings("all")
    public void setRole(final String role) {
        this.role = role;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmailVerified(final boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    @java.lang.SuppressWarnings("all")
    public void setMobileVerified(final boolean mobileVerified) {
        this.mobileVerified = mobileVerified;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public UserAccount() {}
}
