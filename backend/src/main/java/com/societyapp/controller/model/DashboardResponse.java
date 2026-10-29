package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public final class DashboardResponse {
    private DashboardResponse() {}

    public record Platform(
            int societies,
            @JsonProperty("pending_societies") long pendingSocieties,
            @JsonProperty("active_members") long activeMembers,
            @JsonProperty("monthly_plan_value") BigDecimal monthlyPlanValue) {}

    public record Society(
            long spaces,
            long members,
            @JsonProperty("open_complaints") long openComplaints,
            @JsonProperty("visitors_inside") long visitorsInside,
            @JsonProperty("unpaid_invoices") long unpaidInvoices) {}
}
