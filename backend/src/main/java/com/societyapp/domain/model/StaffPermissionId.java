package com.societyapp.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class StaffPermissionId implements Serializable {
    public UUID staffUserId;
    public String permissionCode;

    public StaffPermissionId() {}

    public StaffPermissionId(UUID staffUserId, String permissionCode) {
        this.staffUserId = staffUserId;
        this.permissionCode = permissionCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof StaffPermissionId that)) return false;
        return Objects.equals(staffUserId, that.staffUserId)
                && Objects.equals(permissionCode, that.permissionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffUserId, permissionCode);
    }
}
