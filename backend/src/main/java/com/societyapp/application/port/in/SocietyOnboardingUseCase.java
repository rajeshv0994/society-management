package com.societyapp.application.port.in;

import com.societyapp.application.model.onboarding.SocietyOnboardingModels;
import java.util.List;
import java.util.UUID;

public interface SocietyOnboardingUseCase {
    List<SocietyOnboardingModels.Plan> plans();

    List<SocietyOnboardingModels.ServiceModule> services();

    List<SocietyOnboardingModels.ActiveSociety> activeSocieties();

    SocietyOnboardingModels.RegistrationResult register(
            SocietyOnboardingModels.RegistrationCommand command);

    List<SocietyOnboardingModels.PendingSociety> pending();

    SocietyOnboardingModels.Approved approve(UUID societyId);

    SocietyOnboardingModels.Rejected reject(UUID societyId, String reason);

    SocietyOnboardingModels.Activated activate(SocietyOnboardingModels.ActivationCommand command);
}
