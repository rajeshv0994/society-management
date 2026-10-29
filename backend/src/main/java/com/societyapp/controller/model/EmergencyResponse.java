package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class EmergencyResponse {
    private EmergencyResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Incident(
            UUID id,
            @JsonProperty("incident_number") String incidentNumber,
            @JsonProperty("incident_type") String incidentType,
            String severity,
            String title,
            String description,
            @JsonProperty("location_details") String locationDetails,
            BigDecimal latitude,
            BigDecimal longitude,
            @JsonProperty("people_affected") int peopleAffected,
            @JsonProperty("injured_person_name") String injuredPersonName,
            @JsonProperty("injured_person_mobile") String injuredPersonMobile,
            @JsonProperty("injury_details") String injuryDetails,
            String status,
            @JsonProperty("emergency_contact_notified") boolean emergencyContactNotified,
            @JsonProperty("ambulance_requested") boolean ambulanceRequested,
            @JsonProperty("police_requested") boolean policeRequested,
            @JsonProperty("fire_service_requested") boolean fireServiceRequested,
            @JsonProperty("hospital_name") String hospitalName,
            @JsonProperty("external_reference") String externalReference,
            @JsonProperty("acknowledged_at") OffsetDateTime acknowledgedAt,
            @JsonProperty("response_started_at") OffsetDateTime responseStartedAt,
            @JsonProperty("arrived_at") OffsetDateTime arrivedAt,
            @JsonProperty("stabilized_at") OffsetDateTime stabilizedAt,
            @JsonProperty("resolved_at") OffsetDateTime resolvedAt,
            @JsonProperty("closed_at") OffsetDateTime closedAt,
            @JsonProperty("resolution_summary") String resolutionSummary,
            @JsonProperty("closed_reason") String closedReason,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("reporter_id") UUID reporterId,
            @JsonProperty("reporter_name") String reporterName,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("resident_name") String residentName,
            @JsonProperty("resident_mobile") String residentMobile,
            @JsonProperty("assigned_staff_id") UUID assignedStaffId,
            @JsonProperty("assigned_staff_name") String assignedStaffName,
            @JsonProperty("assigned_staff_mobile") String assignedStaffMobile,
            @JsonProperty("flat_id") UUID flatId,
            @JsonProperty("space_number") String spaceNumber,
            String block) {}

    public record DirectoryContact(
            UUID id,
            @JsonProperty("contact_type") String contactType,
            String name,
            String phone,
            @JsonProperty("alternate_phone") String alternatePhone,
            String address,
            @JsonProperty("available_24x7") boolean available24x7,
            int priority) {}

    public record HouseholdContact(String name, String relationship, String mobile, int priority) {}

    public record ResidentSpace(
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("resident_name") String residentName,
            @JsonProperty("resident_mobile") String residentMobile,
            @JsonProperty("flat_id") UUID flatId,
            @JsonProperty("space_number") String spaceNumber,
            String block,
            @JsonProperty("emergency_contacts") List<HouseholdContact> emergencyContacts) {}

    public record Responder(UUID id, String name, String mobile) {}

    public record Workspace(
            List<Incident> incidents,
            List<DirectoryContact> directory,
            @JsonProperty("resident_options") List<ResidentSpace> residentOptions,
            @JsonProperty("responder_options") List<Responder> responderOptions,
            int total,
            long open,
            long critical,
            long responding,
            long resolved) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HistoryEntry(
            UUID id,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("from_status") String fromStatus,
            @JsonProperty("to_status") String toStatus,
            String note,
            @JsonProperty("actor_role") String actorRole,
            @JsonProperty("occurred_at") OffsetDateTime occurredAt,
            @JsonProperty("actor_name") String actorName) {}
}
