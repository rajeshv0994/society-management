package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class VisitorResponse {
    private VisitorResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Visitor(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("unit_id") UUID unitId,
            @JsonProperty("flat_id") UUID flatId,
            @JsonProperty("visitor_name") String visitorName,
            String mobile,
            String purpose,
            @JsonProperty("vehicle_number") String vehicleNumber,
            @JsonProperty("expected_at") OffsetDateTime expectedAt,
            @JsonProperty("approved_by") UUID approvedBy,
            @JsonProperty("host_resident_id") UUID hostResidentId,
            @JsonProperty("host_resident_name") String hostResidentName,
            @JsonProperty("target_type") String targetType,
            @JsonProperty("target_user_id") UUID targetUserId,
            @JsonProperty("target_name") String targetName,
            @JsonProperty("target_mobile") String targetMobile,
            @JsonProperty("accompanying_members") int accompanyingMembers,
            @JsonProperty("total_people") int totalPeople,
            @JsonProperty("photo_urls") List<String> photoUrls,
            @JsonProperty("photo_url") String photoUrl,
            @JsonProperty("photo_count") int photoCount,
            @JsonProperty("resident_decision_at") OffsetDateTime residentDecisionAt,
            @JsonProperty("staff_approved_by") UUID staffApprovedBy,
            @JsonProperty("approval_method") String approvalMethod,
            @JsonProperty("approval_note") String approvalNote,
            @JsonProperty("checked_in_by") UUID checkedInBy,
            @JsonProperty("checked_in_at") OffsetDateTime checkedInAt,
            @JsonProperty("checked_out_at") OffsetDateTime checkedOutAt,
            String status,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("unit_number") String unitNumber,
            @JsonProperty("flat_number") String flatNumber,
            String block) {}

    public record Permission(
            @JsonProperty("user_id") UUID userId,
            @JsonProperty("can_manage_gate") boolean canManageGate) {}

    public record Space(
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("space_number") String spaceNumber,
            String block) {}

    public record MemberOptions(@JsonProperty("resident_id") UUID residentId, List<Space> spaces) {}

    public record Host(
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("full_name") String fullName,
            String mobile,
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("space_number") String spaceNumber,
            String block) {}

    public record Target(
            @JsonProperty("user_id") UUID userId,
            @JsonProperty("full_name") String fullName,
            String role,
            String mobile) {}

    public record Mutation(UUID id, String status) {}

    public record Photo(
            UUID id,
            @JsonProperty("visitorId") UUID visitorId,
            @JsonProperty("photoUrl") String photoUrl) {}
}
