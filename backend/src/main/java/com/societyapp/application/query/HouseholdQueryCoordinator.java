package com.societyapp.application.query;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.HouseholdViewMapper;
import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.application.port.out.persistence.StaffPermissionRepository;
import com.societyapp.application.port.out.persistence.UserAccountJpaRepository;
import com.societyapp.domain.model.UserAccount;
import com.societyapp.domain.type.*;
import java.util.*;

public final class HouseholdQueryCoordinator {
    private final UserAccountJpaRepository users;
    private final StaffPermissionRepository permissions;
    private final HouseholdViewMapper views;

    public HouseholdQueryCoordinator(
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions,
            HouseholdViewMapper views) {
        this.users = users;
        this.permissions = permissions;
        this.views = views;
    }

    public HouseholdModels.Household profile(UUID societyId, UUID userId) {
        return views.household(requireResident(societyId, userId));
    }

    public HouseholdModels.Workspace workspace(UUID societyId, UUID actorId, UserRole role) {
        List<UserAccount> residents;
        if (role == UserRole.RESIDENT) residents = List.of(requireResident(societyId, actorId));
        else {
            requireManager(actorId, role);
            residents =
                    users
                            .findBySocietyIdAndRoleOrderByFullName(
                                    societyId, UserRole.RESIDENT.code())
                            .stream()
                            .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                            .toList();
        }
        var households = residents.stream().map(views::household).toList();
        int memberCount = households.stream().mapToInt(row -> row.household().size()).sum();
        int vehicleCount = households.stream().mapToInt(row -> row.vehicles().size()).sum();
        long pending =
                households.stream()
                        .flatMap(row -> row.household().stream())
                        .filter(row -> row.verificationStatus() == VerificationStatus.PENDING)
                        .count();
        pending +=
                households.stream()
                        .flatMap(row -> row.vehicles().stream())
                        .filter(row -> row.verificationStatus() == VerificationStatus.PENDING)
                        .count();
        return new HouseholdModels.Workspace(
                households, households.size(), memberCount, vehicleCount, pending);
    }

    private void requireManager(UUID actorId, UserRole role) {
        if (role == UserRole.SOCIETY_ADMIN) return;
        if (role == UserRole.STAFF
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actorId, StaffPermissionCode.HOUSEHOLDS.code())) return;
        throw new AuthorizationException("Household management permission is required");
    }

    private UserAccount requireResident(UUID societyId, UUID residentId) {
        return users.findById(residentId)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
    }
}
