package com.societyapp.application.port.in;

import com.societyapp.application.model.staff.StaffModels;
import com.societyapp.domain.type.AccountStatus;
import com.societyapp.domain.type.StaffPermissionCode;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface StaffUseCase {
    List<StaffModels.StaffView> list(UUID societyId);

    StaffModels.Detail detail(UUID societyId, UUID staffId);

    StaffModels.Updated updateEmployment(
            UUID societyId, UUID staffId, StaffModels.EmploymentCommand command);

    StaffModels.AttendanceMarked markAttendance(
            UUID societyId, UUID markerId, UUID staffId, StaffModels.AttendanceCommand command);

    StaffModels.OwnAttendance ownAttendance(UUID societyId, UUID staffId);

    StaffModels.AttendanceView clockIn(UUID societyId, UUID staffId);

    StaffModels.AttendanceView clockOut(UUID societyId, UUID staffId);

    StaffModels.SalaryRecorded recordSalaryPayment(
            UUID societyId,
            UUID recorderId,
            UUID staffId,
            StaffModels.SalaryPaymentCommand command);

    StaffModels.AccountCreated create(UUID societyId, StaffModels.CreateCommand command);

    StaffModels.Updated update(UUID societyId, UUID staffId, StaffModels.UpdateCommand command);

    StaffModels.Status setStatus(UUID societyId, UUID staffId, AccountStatus status);

    StaffModels.PasswordReset resetPassword(UUID societyId, UUID staffId, String temporaryPassword);

    StaffModels.Permissions setPermissions(
            UUID societyId, UUID granterId, UUID staffId, Set<StaffPermissionCode> permissions);
}
