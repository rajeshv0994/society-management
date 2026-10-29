package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class StaffResponse {
    private StaffResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Staff(
            UUID id,
            @JsonProperty("full_name") String fullName,
            String email,
            String mobile,
            String status,
            Set<String> permissions,
            @JsonProperty("staff_type") String staffType,
            @JsonProperty("employee_code") String employeeCode,
            @JsonProperty("duty_area") String dutyArea,
            @JsonProperty("shift_name") String shiftName,
            @JsonProperty("joined_on") LocalDate joinedOn,
            @JsonProperty("date_of_birth") LocalDate dateOfBirth,
            String gender,
            @JsonProperty("alternate_mobile") String alternateMobile,
            String address,
            @JsonProperty("emergency_contact_name") String emergencyContactName,
            @JsonProperty("emergency_contact_mobile") String emergencyContactMobile,
            @JsonProperty("id_type") String idType,
            @JsonProperty("id_number") String idNumber,
            @JsonProperty("monthly_salary") BigDecimal monthlySalary,
            @JsonProperty("bank_account_name") String bankAccountName,
            @JsonProperty("bank_account_number") String bankAccountNumber,
            @JsonProperty("bank_ifsc") String bankIfsc,
            @JsonProperty("upi_id") String upiId) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Attendance(
            UUID id,
            @JsonProperty("attendance_date") LocalDate attendanceDate,
            String status,
            @JsonProperty("check_in") LocalTime checkIn,
            @JsonProperty("check_out") LocalTime checkOut,
            String note,
            @JsonProperty("marked_by") UUID markedBy,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    public record AttendanceSummary(long present, long absent, long halfDay, long leave) {}

    public record OwnAttendance(
            Attendance today, List<Attendance> history, AttendanceSummary summary) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Task(
            UUID id,
            String title,
            String description,
            String priority,
            String status,
            @JsonProperty("due_at") OffsetDateTime dueAt,
            @JsonProperty("completion_note") String completionNote,
            @JsonProperty("completed_at") OffsetDateTime completedAt,
            @JsonProperty("created_at") OffsetDateTime createdAt) {}

    public record SalaryPayment(
            UUID id,
            @JsonProperty("salary_month") LocalDate salaryMonth,
            BigDecimal amount,
            @JsonProperty("paid_on") LocalDate paidOn,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("reference_number") String referenceNumber,
            String note,
            @JsonProperty("created_at") OffsetDateTime createdAt) {}

    public record Detail(
            Staff staff,
            List<Attendance> attendance,
            List<Task> tasks,
            @JsonProperty("salary_payments") List<SalaryPayment> salaryPayments,
            @JsonProperty("attendance_summary") AttendanceSummary attendanceSummary) {}

    public record AccountCreated(UUID userId, String role, String status) {}

    public record Updated(UUID id, boolean updated) {}

    public record Status(UUID id, String status) {}

    public record PasswordReset(UUID id, boolean passwordReset) {}

    public record Permissions(UUID id, Set<String> permissions) {}

    public record AttendanceMarked(UUID id, String status) {}

    public record SalaryRecorded(UUID id) {}
}
