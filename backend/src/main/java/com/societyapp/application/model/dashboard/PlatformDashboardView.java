package com.societyapp.application.model.dashboard;

import java.math.BigDecimal;

public record PlatformDashboardView(
        int societies, long pendingSocieties, long activeMembers, BigDecimal monthlyPlanValue) {}
