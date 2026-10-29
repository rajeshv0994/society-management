package com.societyapp.application.port.in;

import com.societyapp.application.model.requirement.RequirementModels;
import java.util.List;
import java.util.UUID;

public interface RequirementUseCase {
    RequirementModels.Submitted submit(RequirementModels.SubmitCommand command);

    List<RequirementModels.RequirementView> list();

    RequirementModels.Status review(UUID id, RequirementModels.ReviewCommand command);

    RequirementModels.PlanResult createPlan(UUID creatorId, RequirementModels.PlanCommand command);

    RequirementModels.PlanResult updatePlan(UUID id, RequirementModels.PlanCommand command);

    RequirementModels.PlanResult cancelPlan(UUID id, UUID actorId, String reason);

    List<RequirementModels.PlanView> plans();
}
