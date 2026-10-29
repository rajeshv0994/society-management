package com.societyapp.application.port.in;

import com.societyapp.application.model.resident.ResidentModels;
import java.util.List;
import java.util.UUID;

public interface ResidentUseCase {
    List<ResidentModels.SpaceOption> spaces(UUID societyId);

    ResidentModels.RegistrationResult register(ResidentModels.RegistrationCommand command);

    List<ResidentModels.RegistrationView> pending(UUID societyId);

    List<ResidentModels.Summary> residents(UUID societyId);

    ResidentModels.CreatedResident createResident(ResidentModels.AdminCreateCommand command);

    ResidentModels.Detail resident(UUID societyId, UUID residentId);

    ResidentModels.EntityResult addHouseholdMember(
            UUID societyId, UUID residentId, ResidentModels.HouseholdCommand command);

    ResidentModels.EntityResult removeHouseholdMember(
            UUID societyId, UUID residentId, UUID memberId);

    ResidentModels.ReviewResult review(
            UUID societyId, UUID registrationId, ResidentModels.ReviewCommand command);
}
