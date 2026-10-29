package com.societyapp.application.service;

import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.model.staff.StaffModels;
import com.societyapp.application.policy.StaffCapacityPolicy;
import com.societyapp.application.policy.StaffPermissionCatalog;
import com.societyapp.application.port.in.StaffUseCase;
import com.societyapp.application.port.out.PasswordCodec;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.StaffQueryCoordinator;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.AccountStatus;
import com.societyapp.domain.type.AttendanceStatus;
import com.societyapp.domain.type.StaffPermissionCode;
import com.societyapp.domain.type.UserRole;
import java.time.*;
import java.util.*;

public class StaffService implements StaffUseCase {
    private final UserAccountJpaRepository users;
    private final StaffProfileRepository profiles;
    private final StaffPermissionRepository permissions;
    private final PasswordCodec passwords;
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final SocietyCustomPlanRepository customPlans;
    private final StaffAttendanceRepository attendance;
    private final StaffSalaryPaymentRepository salaryPayments;
    private final StaffTaskRepository tasks;
    private final StaffQueryCoordinator queries;
    private final StaffCapacityPolicy capacity;

    public StaffService(
            UserAccountJpaRepository users,
            StaffProfileRepository profiles,
            StaffPermissionRepository permissions,
            PasswordCodec passwords,
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            SocietyCustomPlanRepository customPlans,
            StaffAttendanceRepository attendance,
            StaffSalaryPaymentRepository salaryPayments,
            StaffTaskRepository tasks) {
        this.users = users;
        this.profiles = profiles;
        this.permissions = permissions;
        this.passwords = passwords;
        this.societies = societies;
        this.plans = plans;
        this.customPlans = customPlans;
        this.attendance = attendance;
        this.salaryPayments = salaryPayments;
        this.tasks = tasks;
        this.queries =
                new StaffQueryCoordinator(
                        users, profiles, permissions, attendance, salaryPayments, tasks);
        this.capacity = new StaffCapacityPolicy(societies, plans, customPlans, users);
    }

    @Override
    public List<StaffModels.StaffView> list(UUID societyId) {
        return queries.list(societyId);
    }

    @Override
    public StaffModels.Detail detail(UUID societyId, UUID staffId) {
        return queries.detail(societyId, requireStaff(societyId, staffId));
    }

    @Override
    public StaffModels.Updated updateEmployment(
            UUID societyId, UUID staffId, StaffModels.EmploymentCommand command) {
        requireStaff(societyId, staffId);
        var profile = profiles.findById(staffId).orElseThrow();
        profile.setJoinedOn(command.joinedOn());
        profile.setDateOfBirth(command.dateOfBirth());
        profile.setGender(command.gender() == null ? null : command.gender().code());
        profile.setAlternateMobile(command.alternateMobile());
        profile.setAddress(command.address());
        profile.setEmergencyContactName(command.emergencyContactName());
        profile.setEmergencyContactMobile(command.emergencyContactMobile());
        profile.setIdType(command.idType());
        profile.setIdNumber(command.idNumber());
        profile.setMonthlySalary(command.monthlySalary());
        profile.setBankAccountName(command.bankAccountName());
        profile.setBankAccountNumber(command.bankAccountNumber());
        profile.setBankIfsc(command.bankIfsc());
        profile.setUpiId(command.upiId());
        profiles.save(profile);
        return new StaffModels.Updated(staffId, true);
    }

    @Override
    public StaffModels.AttendanceMarked markAttendance(
            UUID societyId, UUID markerId, UUID staffId, StaffModels.AttendanceCommand command) {
        requireStaff(societyId, staffId);
        if (command.checkIn() != null
                && command.checkOut() != null
                && command.checkOut().isBefore(command.checkIn())) {
            throw new IllegalArgumentException("Check-out cannot be before check-in");
        }
        var row =
                attendance
                        .findByStaffUserIdAndAttendanceDate(staffId, command.attendanceDate())
                        .orElseGet(StaffAttendance::new);
        if (row.getId() == null) {
            row.setSocietyId(societyId);
            row.setStaffUserId(staffId);
            row.setAttendanceDate(command.attendanceDate());
            row.setCreatedAt(OffsetDateTime.now());
        }
        row.setStatus(command.status().code());
        row.setCheckIn(command.checkIn());
        row.setCheckOut(command.checkOut());
        row.setNote(command.note());
        row.setMarkedBy(markerId);
        row.setUpdatedAt(OffsetDateTime.now());
        row = attendance.save(row);
        return new StaffModels.AttendanceMarked(row.getId(), command.status());
    }

    @Override
    public StaffModels.OwnAttendance ownAttendance(UUID societyId, UUID staffId) {
        requireStaff(societyId, staffId);
        return queries.ownAttendance(staffId);
    }

    @Override
    public StaffModels.AttendanceView clockIn(UUID societyId, UUID staffId) {
        requireStaff(societyId, staffId);
        LocalDate today = LocalDate.now();
        var row =
                attendance
                        .findByStaffUserIdAndAttendanceDate(staffId, today)
                        .orElseGet(StaffAttendance::new);
        if (row.getId() != null && row.getCheckIn() != null) {
            throw new ConflictException("You are already checked in today");
        }
        if (row.getId() != null && row.getCheckIn() == null && !staffId.equals(row.getMarkedBy())) {
            throw new ConflictException(
                    "Today's attendance was marked by the society admin; contact the admin for a correction");
        }
        if (row.getId() == null) {
            row.setSocietyId(societyId);
            row.setStaffUserId(staffId);
            row.setAttendanceDate(today);
            row.setCreatedAt(OffsetDateTime.now());
        }
        row.setStatus(AttendanceStatus.PRESENT.code());
        row.setCheckIn(LocalTime.now().withSecond(0).withNano(0));
        row.setCheckOut(null);
        row.setMarkedBy(staffId);
        row.setUpdatedAt(OffsetDateTime.now());
        row = attendance.save(row);
        return queries.attendance(row);
    }

    @Override
    public StaffModels.AttendanceView clockOut(UUID societyId, UUID staffId) {
        requireStaff(societyId, staffId);
        var row =
                attendance
                        .findByStaffUserIdAndAttendanceDate(staffId, LocalDate.now())
                        .orElseThrow(
                                () -> new IllegalStateException("Check in before checking out"));
        if (row.getCheckIn() == null)
            throw new IllegalStateException("Check in before checking out");
        if (row.getCheckOut() != null)
            throw new ConflictException("You are already checked out today");
        var checkOut = LocalTime.now().withSecond(0).withNano(0);
        if (checkOut.isBefore(row.getCheckIn())) {
            throw new IllegalStateException("Check-out cannot be before check-in");
        }
        row.setCheckOut(checkOut);
        row.setMarkedBy(staffId);
        row.setUpdatedAt(OffsetDateTime.now());
        row = attendance.save(row);
        return queries.attendance(row);
    }

    @Override
    public StaffModels.SalaryRecorded recordSalaryPayment(
            UUID societyId,
            UUID recorderId,
            UUID staffId,
            StaffModels.SalaryPaymentCommand command) {
        requireStaff(societyId, staffId);
        LocalDate salaryMonth = command.salaryMonth().withDayOfMonth(1);
        if (salaryPayments.existsByStaffUserIdAndSalaryMonth(staffId, salaryMonth)) {
            throw new ConflictException("Salary payment is already recorded for this month");
        }
        var payment = new StaffSalaryPayment();
        payment.setSocietyId(societyId);
        payment.setStaffUserId(staffId);
        payment.setSalaryMonth(salaryMonth);
        payment.setAmount(command.amount());
        payment.setPaidOn(command.paidOn());
        payment.setPaymentMethod(command.paymentMethod().code());
        payment.setReferenceNumber(command.referenceNumber());
        payment.setNote(command.note());
        payment.setRecordedBy(recorderId);
        payment.setCreatedAt(OffsetDateTime.now());
        payment = salaryPayments.save(payment);
        return new StaffModels.SalaryRecorded(payment.getId());
    }

    @Override
    public StaffModels.AccountCreated create(UUID societyId, StaffModels.CreateCommand command) {
        capacity.ensureAvailable(societyId);
        var user = new UserAccount();
        user.setSocietyId(societyId);
        user.setFullName(command.fullName());
        user.setEmail(command.email());
        user.setMobile(command.mobile());
        user.setPasswordHash(passwords.encode(command.temporaryPassword()));
        user.setRole(UserRole.STAFF.code());
        user.setStatus(AccountStatus.ACTIVE.code());
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        user.setCreatedAt(OffsetDateTime.now());
        user = users.save(user);
        var profile = new StaffProfile();
        profile.setUserId(user.getId());
        profile.setStaffType(command.staffType());
        profile.setEmployeeCode(command.employeeCode());
        profile.setDutyArea(command.dutyArea());
        profile.setShiftName(command.shiftName());
        profile.setJoinedOn(LocalDate.now());
        profiles.save(profile);
        return new StaffModels.AccountCreated(user.getId(), UserRole.STAFF, AccountStatus.ACTIVE);
    }

    @Override
    public StaffModels.Updated update(
            UUID societyId, UUID staffId, StaffModels.UpdateCommand command) {
        var user = requireStaff(societyId, staffId);
        user.setFullName(command.fullName().trim());
        user.setEmail(command.email().trim().toLowerCase());
        user.setMobile(command.mobile().trim());
        users.save(user);
        var profile = profiles.findById(staffId).orElseGet(StaffProfile::new);
        profile.setUserId(staffId);
        profile.setStaffType(command.staffType());
        profile.setEmployeeCode(command.employeeCode());
        profile.setDutyArea(command.dutyArea());
        profile.setShiftName(command.shiftName());
        if (profile.getJoinedOn() == null) profile.setJoinedOn(LocalDate.now());
        profiles.save(profile);
        return new StaffModels.Updated(staffId, true);
    }

    @Override
    public StaffModels.Status setStatus(UUID societyId, UUID staffId, AccountStatus status) {
        var user = requireStaff(societyId, staffId);
        user.setStatus(status.code());
        users.save(user);
        return new StaffModels.Status(staffId, status);
    }

    @Override
    public StaffModels.PasswordReset resetPassword(
            UUID societyId, UUID staffId, String temporaryPassword) {
        var user = requireStaff(societyId, staffId);
        user.setPasswordHash(passwords.encode(temporaryPassword));
        users.save(user);
        return new StaffModels.PasswordReset(staffId, true);
    }

    @Override
    public StaffModels.Permissions setPermissions(
            UUID societyId, UUID granterId, UUID staffId, Set<StaffPermissionCode> requested) {
        requireStaff(societyId, staffId);
        var society =
                societies
                        .findById(societyId)
                        .orElseThrow(
                                () ->
                                        new com.societyapp.application.exception
                                                .ResourceNotFoundException("Society not found"));
        var codes =
                StaffPermissionCatalog.validate(
                        requested.stream()
                                .map(StaffPermissionCode::code)
                                .collect(java.util.stream.Collectors.toSet()),
                        society.getEnabledModules());
        permissions.deleteByStaffUserId(staffId);
        permissions.flush();
        for (var code : codes) {
            var permission = new StaffPermission();
            permission.setStaffUserId(staffId);
            permission.setPermissionCode(code);
            permission.setGrantedBy(granterId);
            permission.setGrantedAt(OffsetDateTime.now());
            permissions.save(permission);
        }
        return new StaffModels.Permissions(
                staffId,
                codes.stream()
                        .map(StaffPermissionCode::from)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }

    private UserAccount requireStaff(UUID societyId, UUID staffId) {
        return users.findById(staffId)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.STAFF.matches(user.getRole()))
                .orElseThrow(
                        () ->
                                new com.societyapp.application.exception.ResourceNotFoundException(
                                        "Staff member not found"));
    }
}
