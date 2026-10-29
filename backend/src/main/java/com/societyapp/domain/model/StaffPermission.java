package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class StaffPermission {
    private UUID staffUserId;
    private String permissionCode;
    private UUID grantedBy;
    private OffsetDateTime grantedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getStaffUserId() {
        return this.staffUserId;
    }

    @java.lang.SuppressWarnings("all")
    public String getPermissionCode() {
        return this.permissionCode;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getGrantedBy() {
        return this.grantedBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getGrantedAt() {
        return this.grantedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setStaffUserId(final UUID staffUserId) {
        this.staffUserId = staffUserId;
    }

    @java.lang.SuppressWarnings("all")
    public void setPermissionCode(final String permissionCode) {
        this.permissionCode = permissionCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setGrantedBy(final UUID grantedBy) {
        this.grantedBy = grantedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setGrantedAt(final OffsetDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    @java.lang.SuppressWarnings("all")
    public StaffPermission() {}
}
