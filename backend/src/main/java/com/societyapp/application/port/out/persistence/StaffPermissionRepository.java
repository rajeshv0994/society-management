package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.*;
import java.util.*;

public interface StaffPermissionRepository
        extends CrudRepositoryPort<StaffPermission, StaffPermissionId> {
    List<StaffPermission> findByStaffUserId(UUID staffUserId);

    boolean existsByStaffUserIdAndPermissionCode(UUID staffUserId, String permissionCode);

    void deleteByStaffUserId(UUID staffUserId);
}
