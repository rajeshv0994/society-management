package com.societyapp.controller.mapper;

import com.societyapp.application.model.dashboard.PlatformDashboardView;
import com.societyapp.application.model.dashboard.SocietyDashboardView;
import com.societyapp.controller.model.DashboardResponse;
import org.springframework.stereotype.Component;

@Component
public class DashboardHttpMapper {
    public DashboardResponse.Platform toResponse(PlatformDashboardView view) {
        return new DashboardResponse.Platform(
                view.societies(),
                view.pendingSocieties(),
                view.activeMembers(),
                view.monthlyPlanValue());
    }

    public DashboardResponse.Society toResponse(SocietyDashboardView view) {
        return new DashboardResponse.Society(
                view.spaces(),
                view.members(),
                view.openComplaints(),
                view.visitorsInside(),
                view.unpaidInvoices());
    }
}
