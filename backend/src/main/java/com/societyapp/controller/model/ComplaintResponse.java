package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ComplaintResponse {
    private ComplaintResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record View(
            UUID id,
            String reference,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("category_id") UUID categoryId,
            String title,
            String description,
            String priority,
            String status,
            @JsonProperty("assigned_to") UUID assignedTo,
            String resolution,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("due_at") OffsetDateTime dueAt,
            @JsonProperty("closed_at") OffsetDateTime closedAt,
            boolean overdue,
            @JsonProperty("resident_name") String residentName,
            @JsonProperty("created_by_name") String createdByName,
            @JsonProperty("assigned_to_name") String assignedToName,
            @JsonProperty("category_name") String categoryName,
            @JsonProperty("space_number") String spaceNumber,
            String block) {}

    public record Detail(
            UUID id,
            String reference,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("category_id") UUID categoryId,
            String title,
            String description,
            String priority,
            String status,
            @JsonProperty("assigned_to") UUID assignedTo,
            String resolution,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("due_at") OffsetDateTime dueAt,
            @JsonProperty("closed_at") OffsetDateTime closedAt,
            boolean overdue,
            @JsonProperty("resident_name") String residentName,
            @JsonProperty("created_by_name") String createdByName,
            @JsonProperty("assigned_to_name") String assignedToName,
            @JsonProperty("category_name") String categoryName,
            @JsonProperty("space_number") String spaceNumber,
            String block,
            List<TimelineEntry> timeline) {}

    public record TimelineEntry(
            UUID id,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("from_status") String fromStatus,
            @JsonProperty("to_status") String toStatus,
            String note,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("actor_name") String actorName) {}

    public record Options(
            @JsonProperty("current_user_id") UUID currentUserId,
            List<Category> categories,
            List<Resident> residents,
            List<Staff> staff,
            List<Space> spaces) {}

    public record Category(UUID id, String name, @JsonProperty("sla_hours") int slaHours) {}

    public record Resident(
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("full_name") String fullName,
            String mobile,
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("space_number") String spaceNumber,
            String block) {}

    public record Staff(UUID id, @JsonProperty("full_name") String fullName, String mobile) {}

    public record Space(
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("space_number") String spaceNumber,
            String block) {}

    public record Status(UUID id, String status) {}

    public record Commented(UUID id, boolean commented) {}
}
