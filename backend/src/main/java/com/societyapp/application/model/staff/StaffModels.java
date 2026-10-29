package com.societyapp.application.model.staff;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class StaffModels {
    private StaffModels() {}

    public record CreateCommand(
            String fullName,
            String email,
            String mobile,
            String temporaryPassword,
            String staffType,
            String employeeCode,
            String dutyArea,
            String shiftName) {}

    public record UpdateCommand(
            String fullName,
            String email,
            String mobile,
            String staffType,
            String employeeCode,
            String dutyArea,
            String shiftName) {}

    public record EmploymentCommand(
            LocalDate joinedOn,
            LocalDate dateOfBirth,
            Gender gender,
            String alternateMobile,
            String address,
            String emergencyContactName,
            String emergencyContactMobile,
            String idType,
            String idNumber,
            BigDecimal monthlySalary,
            String bankAccountName,
            String bankAccountNumber,
            String bankIfsc,
            String upiId) {}

    public record AttendanceCommand(
            LocalDate attendanceDate,
            AttendanceStatus status,
            LocalTime checkIn,
            LocalTime checkOut,
            String note) {}

    public record SalaryPaymentCommand(
            LocalDate salaryMonth,
            BigDecimal amount,
            LocalDate paidOn,
            PaymentMethod paymentMethod,
            String referenceNumber,
            String note) {}

    public record StaffView(
            UUID id,
            String fullName,
            String email,
            String mobile,
            AccountStatus status,
            Set<StaffPermissionCode> permissions,
            String staffType,
            String employeeCode,
            String dutyArea,
            String shiftName,
            LocalDate joinedOn,
            LocalDate dateOfBirth,
            Gender gender,
            String alternateMobile,
            String address,
            String emergencyContactName,
            String emergencyContactMobile,
            String idType,
            String idNumber,
            BigDecimal monthlySalary,
            String bankAccountName,
            String bankAccountNumber,
            String bankIfsc,
            String upiId) {}

    public record AttendanceView(
            UUID id,
            LocalDate attendanceDate,
            AttendanceStatus status,
            LocalTime checkIn,
            LocalTime checkOut,
            String note,
            UUID markedBy,
            OffsetDateTime updatedAt) {}

    public record AttendanceSummary(long present, long absent, long halfDay, long leave) {}

    public record OwnAttendance(
            AttendanceView today, List<AttendanceView> history, AttendanceSummary summary) {}

    public record TaskView(
            UUID id,
            String title,
            String description,
            WorkPriority priority,
            TaskStatus status,
            OffsetDateTime dueAt,
            String completionNote,
            OffsetDateTime completedAt,
            OffsetDateTime createdAt) {}

    public record SalaryPaymentView(
            UUID id,
            LocalDate salaryMonth,
            BigDecimal amount,
            LocalDate paidOn,
            PaymentMethod paymentMethod,
            String referenceNumber,
            String note,
            OffsetDateTime createdAt) {}

    public record Detail(
            StaffView staff,
            List<AttendanceView> attendance,
            List<TaskView> tasks,
            List<SalaryPaymentView> salaryPayments,
            AttendanceSummary attendanceSummary) {}

    public record AccountCreated(UUID userId, UserRole role, AccountStatus status) {}

    public record Updated(UUID id, boolean updated) {}

    public record Status(UUID id, AccountStatus status) {}

    public record PasswordReset(UUID id, boolean passwordReset) {}

    public record Permissions(UUID id, Set<StaffPermissionCode> permissions) {}

    public record AttendanceMarked(UUID id, AttendanceStatus status) {}

    public record SalaryRecorded(UUID id) {}
}
