package com.societyapp.application.port.in;

import com.societyapp.application.model.dashboard.PlatformDashboardView;
import com.societyapp.application.model.dashboard.SocietyDashboardView;
import java.util.UUID;

public interface DashboardUseCase {
    PlatformDashboardView platform();

    SocietyDashboardView society(UUID societyId);
}
