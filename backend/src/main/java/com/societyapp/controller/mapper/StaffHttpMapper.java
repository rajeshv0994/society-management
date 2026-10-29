package com.societyapp.controller.mapper;

import com.societyapp.application.model.staff.StaffModels;
import com.societyapp.controller.model.StaffResponse;
import com.societyapp.domain.type.Gender;
import java.util.stream.Collectors;

public final class StaffHttpMapper {
    public StaffResponse.Staff toResponse(StaffModels.StaffView value) {
        return new StaffResponse.Staff(
                value.id(),
                value.fullName(),
                value.email(),
                value.mobile(),
                value.status().code(),
                value.permissions().stream()
                        .map(permission -> permission.code())
                        .collect(Collectors.toUnmodifiableSet()),
                value.staffType(),
                value.employeeCode(),
                value.dutyArea(),
                value.shiftName(),
                value.joinedOn(),
                value.dateOfBirth(),
                code(value.gender()),
                value.alternateMobile(),
                value.address(),
                value.emergencyContactName(),
                value.emergencyContactMobile(),
                value.idType(),
                value.idNumber(),
                value.monthlySalary(),
                value.bankAccountName(),
                value.bankAccountNumber(),
                value.bankIfsc(),
                value.upiId());
    }

    public StaffResponse.Attendance toResponse(StaffModels.AttendanceView value) {
        return new StaffResponse.Attendance(
                value.id(),
                value.attendanceDate(),
                value.status().code(),
                value.checkIn(),
                value.checkOut(),
                value.note(),
                value.markedBy(),
                value.updatedAt());
    }

    public StaffResponse.OwnAttendance toResponse(StaffModels.OwnAttendance value) {
        return new StaffResponse.OwnAttendance(
                value.today() == null ? null : toResponse(value.today()),
                value.history().stream().map(this::toResponse).toList(),
                summary(value.summary()));
    }

    public StaffResponse.Detail toResponse(StaffModels.Detail value) {
        return new StaffResponse.Detail(
                toResponse(value.staff()),
                value.attendance().stream().map(this::toResponse).toList(),
                value.tasks().stream().map(this::task).toList(),
                value.salaryPayments().stream().map(this::salary).toList(),
                summary(value.attendanceSummary()));
    }

    public StaffResponse.AccountCreated toResponse(StaffModels.AccountCreated value) {
        return new StaffResponse.AccountCreated(
                value.userId(), value.role().code(), value.status().code());
    }

    public StaffResponse.Updated toResponse(StaffModels.Updated value) {
        return new StaffResponse.Updated(value.id(), value.updated());
    }

    public StaffResponse.Status toResponse(StaffModels.Status value) {
        return new StaffResponse.Status(value.id(), value.status().code());
    }

    public StaffResponse.PasswordReset toResponse(StaffModels.PasswordReset value) {
        return new StaffResponse.PasswordReset(value.id(), value.passwordReset());
    }

    public StaffResponse.Permissions toResponse(StaffModels.Permissions value) {
        return new StaffResponse.Permissions(
                value.id(),
                value.permissions().stream()
                        .map(permission -> permission.code())
                        .collect(Collectors.toUnmodifiableSet()));
    }

    public StaffResponse.AttendanceMarked toResponse(StaffModels.AttendanceMarked value) {
        return new StaffResponse.AttendanceMarked(value.id(), value.status().code());
    }

    public StaffResponse.SalaryRecorded toResponse(StaffModels.SalaryRecorded value) {
        return new StaffResponse.SalaryRecorded(value.id());
    }

    private StaffResponse.AttendanceSummary summary(StaffModels.AttendanceSummary value) {
        return new StaffResponse.AttendanceSummary(
                value.present(), value.absent(), value.halfDay(), value.leave());
    }

    private StaffResponse.Task task(StaffModels.TaskView value) {
        return new StaffResponse.Task(
                value.id(),
                value.title(),
                value.description(),
                value.priority().code(),
                value.status().code(),
                value.dueAt(),
                value.completionNote(),
                value.completedAt(),
                value.createdAt());
    }

    private StaffResponse.SalaryPayment salary(StaffModels.SalaryPaymentView value) {
        return new StaffResponse.SalaryPayment(
                value.id(),
                value.salaryMonth(),
                value.amount(),
                value.paidOn(),
                value.paymentMethod().code(),
                value.referenceNumber(),
                value.note(),
                value.createdAt());
    }

    private String code(Gender value) {
        return value == null ? null : value.code();
    }
}
