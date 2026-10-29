package com.societyapp.application.port.in;

import com.societyapp.application.model.platformadmin.PlatformAdminModels;
import com.societyapp.application.model.platformadmin.PlatformSocietyModels;
import com.societyapp.domain.type.*;
import java.util.List;
import java.util.UUID;

public interface PlatformAdminUseCase {
    PlatformAdminModels.Dashboard dashboard();

    List<PlatformAdminModels.SocietySummary> societies(SocietyStatus status, String search);

    PlatformAdminModels.SocietyCreated createSociety(
            UUID actorId, PlatformAdminModels.CreateSocietyCommand command);

    PlatformSocietyModels.Details society(UUID societyId);

    PlatformAdminModels.SocietyUpdated updateSociety(
            UUID actorId, UUID societyId, PlatformAdminModels.UpdateSocietyCommand command);

    List<PlatformAdminModels.PlanView> plans();

    List<PlatformAdminModels.ServiceView> services();

    PlatformAdminModels.ServiceView createService(
            UUID actorId, PlatformAdminModels.ServiceCommand command);

    PlatformAdminModels.ServiceView updateService(
            UUID actorId, String code, PlatformAdminModels.ServiceCommand command);

    PlatformAdminModels.PlanUpdated updatePlan(
            UUID actorId, SubscriptionPlanCode code, PlatformAdminModels.PlanCommand command);

    List<PlatformAdminModels.TicketView> tickets();

    PlatformAdminModels.TicketUpdated updateTicket(
            UUID actorId, UUID ticketId, SupportTicketStatus status, String resolution);
}
