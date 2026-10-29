package com.societyapp.application.query;

import com.societyapp.application.model.staff.StaffModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StaffQueryCoordinator {
    private final UserAccountJpaRepository users;
    private final StaffProfileRepository profiles;
    private final StaffPermissionRepository permissions;
    private final StaffAttendanceRepository attendance;
    private final StaffSalaryPaymentRepository salaryPayments;
    private final StaffTaskRepository tasks;

    public StaffQueryCoordinator(
            UserAccountJpaRepository users,
            StaffProfileRepository profiles,
            StaffPermissionRepository permissions,
            StaffAttendanceRepository attendance,
            StaffSalaryPaymentRepository salaryPayments,
            StaffTaskRepository tasks) {
        this.users = users;
        this.profiles = profiles;
        this.permissions = permissions;
        this.attendance = attendance;
        this.salaryPayments = salaryPayments;
        this.tasks = tasks;
    }

    public List<StaffModels.StaffView> list(UUID societyId) {
        return users
                .findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.STAFF.code())
                .stream()
                .map(this::staff)
                .toList();
    }

    public StaffModels.Detail detail(UUID societyId, UserAccount user) {
        var attendanceRows =
                attendance.findByStaffUserIdOrderByAttendanceDateDesc(user.getId()).stream()
                        .map(this::attendance)
                        .toList();
        var taskRows =
                tasks.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                        .filter(task -> user.getId().equals(task.getAssignedTo()))
                        .map(this::task)
                        .toList();
        var salaries =
                salaryPayments.findByStaffUserIdOrderBySalaryMonthDesc(user.getId()).stream()
                        .map(this::salary)
                        .toList();
        return new StaffModels.Detail(
                staff(user), attendanceRows, taskRows, salaries, summary(attendanceRows));
    }

    public StaffModels.OwnAttendance ownAttendance(UUID staffId) {
        var history =
                attendance.findByStaffUserIdOrderByAttendanceDateDesc(staffId).stream()
                        .map(this::attendance)
                        .toList();
        return new StaffModels.OwnAttendance(
                history.stream()
                        .filter(row -> row.attendanceDate().equals(LocalDate.now()))
                        .findFirst()
                        .orElse(null),
                history,
                summary(history));
    }

    public StaffModels.StaffView staff(UserAccount user) {
        var profile = profiles.findById(user.getId()).orElse(null);
        return new StaffModels.StaffView(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobile(),
                AccountStatus.from(user.getStatus()),
                permissions.findByStaffUserId(user.getId()).stream()
                        .map(StaffPermission::getPermissionCode)
                        .map(StaffPermissionCode::from)
                        .collect(Collectors.toUnmodifiableSet()),
                profile == null ? null : profile.getStaffType(),
                profile == null ? null : profile.getEmployeeCode(),
                profile == null ? null : profile.getDutyArea(),
                profile == null ? null : profile.getShiftName(),
                profile == null ? null : profile.getJoinedOn(),
                profile == null ? null : profile.getDateOfBirth(),
                profile == null || profile.getGender() == null
                        ? null
                        : Gender.from(profile.getGender()),
                profile == null ? null : profile.getAlternateMobile(),
                profile == null ? null : profile.getAddress(),
                profile == null ? null : profile.getEmergencyContactName(),
                profile == null ? null : profile.getEmergencyContactMobile(),
                profile == null ? null : profile.getIdType(),
                profile == null ? null : profile.getIdNumber(),
                profile == null ? null : profile.getMonthlySalary(),
                profile == null ? null : profile.getBankAccountName(),
                profile == null ? null : profile.getBankAccountNumber(),
                profile == null ? null : profile.getBankIfsc(),
                profile == null ? null : profile.getUpiId());
    }

    public StaffModels.AttendanceView attendance(StaffAttendance row) {
        return new StaffModels.AttendanceView(
                row.getId(),
                row.getAttendanceDate(),
                AttendanceStatus.from(row.getStatus()),
                row.getCheckIn(),
                row.getCheckOut(),
                row.getNote(),
                row.getMarkedBy(),
                row.getUpdatedAt());
    }

    private StaffModels.AttendanceSummary summary(List<StaffModels.AttendanceView> rows) {
        return new StaffModels.AttendanceSummary(
                count(rows, AttendanceStatus.PRESENT), count(rows, AttendanceStatus.ABSENT),
                count(rows, AttendanceStatus.HALF_DAY), count(rows, AttendanceStatus.LEAVE));
    }

    private long count(List<StaffModels.AttendanceView> rows, AttendanceStatus status) {
        return rows.stream().filter(row -> row.status() == status).count();
    }

    private StaffModels.TaskView task(StaffTask row) {
        return new StaffModels.TaskView(
                row.getId(),
                row.getTitle(),
                row.getDescription(),
                WorkPriority.from(row.getPriority()),
                TaskStatus.from(row.getStatus()),
                row.getDueAt(),
                row.getCompletionNote(),
                row.getCompletedAt(),
                row.getCreatedAt());
    }

    private StaffModels.SalaryPaymentView salary(StaffSalaryPayment row) {
        return new StaffModels.SalaryPaymentView(
                row.getId(),
                row.getSalaryMonth(),
                row.getAmount(),
                row.getPaidOn(),
                PaymentMethod.from(row.getPaymentMethod()),
                row.getReferenceNumber(),
                row.getNote(),
                row.getCreatedAt());
    }
}
