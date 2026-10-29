package com.societyapp.controller;

import com.societyapp.application.model.staff.StaffModels;
import com.societyapp.application.port.in.StaffUseCase;
import com.societyapp.controller.mapper.StaffHttpMapper;
import com.societyapp.controller.model.StaffResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/society/staff")
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class StaffController {
    private final StaffUseCase service;
    private final StaffHttpMapper mapper = new StaffHttpMapper();

    public StaffController(StaffUseCase service) {
        this.service = service;
    }

    public record CreateStaff(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank String mobile,
            @NotBlank @Size(min = 8) String temporaryPassword,
            @NotBlank String staffType,
            String employeeCode,
            String dutyArea,
            String shiftName) {}

    public record UpdateStaff(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank String mobile,
            @NotBlank String staffType,
            String employeeCode,
            String dutyArea,
            String shiftName) {}

    public record StaffStatus(@Pattern(regexp = "ACTIVE|INACTIVE") String status) {}

    public record ResetPassword(@NotBlank @Size(min = 8) String temporaryPassword) {}

    public record Permissions(@NotNull Set<String> permissions) {}

    public record Employment(
            @NotNull LocalDate joinedOn,
            LocalDate dateOfBirth,
            @Pattern(regexp = "MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY") String gender,
            @Size(max = 20) String alternateMobile,
            String address,
            String emergencyContactName,
            String emergencyContactMobile,
            String idType,
            String idNumber,
            @DecimalMin("0.00") BigDecimal monthlySalary,
            @Size(max = 150) String bankAccountName,
            @Size(max = 40) String bankAccountNumber,
            @Size(max = 20) String bankIfsc,
            @Size(max = 100) String upiId) {}

    public record Attendance(
            @NotNull LocalDate attendanceDate,
            @NotNull @Pattern(regexp = "PRESENT|ABSENT|HALF_DAY|LEAVE") String status,
            LocalTime checkIn,
            LocalTime checkOut,
            @Size(max = 250) String note) {}

    public record SalaryPayment(
            @NotNull LocalDate salaryMonth,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotNull LocalDate paidOn,
            @NotNull @Pattern(regexp = "CASH|BANK_TRANSFER|UPI|CHEQUE") String paymentMethod,
            String referenceNumber,
            @Size(max = 250) String note) {}

    @GetMapping
    public List<StaffResponse.Staff> list() {
        return service.list(CurrentUser.societyId()).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STAFF')")
    public StaffResponse.Detail ownProfile() {
        return mapper.toResponse(service.detail(CurrentUser.societyId(), CurrentUser.id()));
    }

    @GetMapping("/me/attendance")
    @PreAuthorize("hasRole('STAFF')")
    public StaffResponse.OwnAttendance ownAttendance() {
        return mapper.toResponse(service.ownAttendance(CurrentUser.societyId(), CurrentUser.id()));
    }

    @PostMapping("/me/attendance/clock-in")
    @PreAuthorize("hasRole('STAFF')")
    public StaffResponse.Attendance clockIn() {
        return mapper.toResponse(service.clockIn(CurrentUser.societyId(), CurrentUser.id()));
    }

    @PostMapping("/me/attendance/clock-out")
    @PreAuthorize("hasRole('STAFF')")
    public StaffResponse.Attendance clockOut() {
        return mapper.toResponse(service.clockOut(CurrentUser.societyId(), CurrentUser.id()));
    }

    @GetMapping("/{staffId}")
    public StaffResponse.Detail detail(@PathVariable UUID staffId) {
        return mapper.toResponse(service.detail(CurrentUser.societyId(), staffId));
    }

    @PostMapping
    public StaffResponse.AccountCreated create(@Valid @RequestBody CreateStaff request) {
        var command =
                new StaffModels.CreateCommand(
                        request.fullName(),
                        request.email(),
                        request.mobile(),
                        request.temporaryPassword(),
                        request.staffType(),
                        request.employeeCode(),
                        request.dutyArea(),
                        request.shiftName());
        return mapper.toResponse(service.create(CurrentUser.societyId(), command));
    }

    @PutMapping("/{staffId}")
    public StaffResponse.Updated update(
            @PathVariable UUID staffId, @Valid @RequestBody UpdateStaff request) {
        var command =
                new StaffModels.UpdateCommand(
                        request.fullName(),
                        request.email(),
                        request.mobile(),
                        request.staffType(),
                        request.employeeCode(),
                        request.dutyArea(),
                        request.shiftName());
        return mapper.toResponse(service.update(CurrentUser.societyId(), staffId, command));
    }

    @PatchMapping("/{staffId}/status")
    public StaffResponse.Status status(
            @PathVariable UUID staffId, @Valid @RequestBody StaffStatus request) {
        return mapper.toResponse(
                service.setStatus(
                        CurrentUser.societyId(), staffId, AccountStatus.from(request.status())));
    }

    @PostMapping("/{staffId}/reset-password")
    public StaffResponse.PasswordReset resetPassword(
            @PathVariable UUID staffId, @Valid @RequestBody ResetPassword request) {
        return mapper.toResponse(
                service.resetPassword(
                        CurrentUser.societyId(), staffId, request.temporaryPassword()));
    }

    @PutMapping("/{staffId}/permissions")
    public StaffResponse.Permissions permissions(
            @PathVariable UUID staffId, @Valid @RequestBody Permissions request) {
        var codes =
                request.permissions().stream()
                        .map(StaffPermissionCode::from)
                        .collect(java.util.stream.Collectors.toSet());
        return mapper.toResponse(
                service.setPermissions(CurrentUser.societyId(), CurrentUser.id(), staffId, codes));
    }

    @PutMapping("/{staffId}/employment")
    public StaffResponse.Updated employment(
            @PathVariable UUID staffId, @Valid @RequestBody Employment request) {
        var command =
                new StaffModels.EmploymentCommand(
                        request.joinedOn(),
                        request.dateOfBirth(),
                        request.gender() == null ? null : Gender.from(request.gender()),
                        request.alternateMobile(),
                        request.address(),
                        request.emergencyContactName(),
                        request.emergencyContactMobile(),
                        request.idType(),
                        request.idNumber(),
                        request.monthlySalary(),
                        request.bankAccountName(),
                        request.bankAccountNumber(),
                        request.bankIfsc(),
                        request.upiId());
        return mapper.toResponse(
                service.updateEmployment(CurrentUser.societyId(), staffId, command));
    }

    @PutMapping("/{staffId}/attendance")
    public StaffResponse.AttendanceMarked attendance(
            @PathVariable UUID staffId, @Valid @RequestBody Attendance request) {
        var command =
                new StaffModels.AttendanceCommand(
                        request.attendanceDate(),
                        AttendanceStatus.from(request.status()),
                        request.checkIn(),
                        request.checkOut(),
                        request.note());
        return mapper.toResponse(
                service.markAttendance(
                        CurrentUser.societyId(), CurrentUser.id(), staffId, command));
    }

    @PostMapping("/{staffId}/salary-payments")
    public StaffResponse.SalaryRecorded salaryPayment(
            @PathVariable UUID staffId, @Valid @RequestBody SalaryPayment request) {
        var command =
                new StaffModels.SalaryPaymentCommand(
                        request.salaryMonth(),
                        request.amount(),
                        request.paidOn(),
                        PaymentMethod.from(request.paymentMethod()),
                        request.referenceNumber(),
                        request.note());
        return mapper.toResponse(
                service.recordSalaryPayment(
                        CurrentUser.societyId(), CurrentUser.id(), staffId, command));
    }
}
