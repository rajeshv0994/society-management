package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataStaffPermissionRepository
        extends JpaRepository<StaffPermission, StaffPermissionId>,
                com.societyapp.application.port.out.persistence.StaffPermissionRepository {
    @Query(
            value = "select * from staff_permissions where staff_user_id = :staffId",
            nativeQuery = true)
    List<StaffPermission> findByStaffUserId(@Param("staffId") UUID staffUserId);

    boolean existsByStaffUserIdAndPermissionCode(UUID staffUserId, String permissionCode);

    @Modifying
    @Query(
            value = "delete from staff_permissions where staff_user_id = :staffId",
            nativeQuery = true)
    void deleteByStaffUserId(@Param("staffId") UUID staffUserId);
}
