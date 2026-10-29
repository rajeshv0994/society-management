package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class TaskResponse {
    private TaskResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record View(
            UUID id,
            String reference,
            String title,
            String description,
            @JsonProperty("assigned_to") UUID assignedTo,
            @JsonProperty("assigned_by") UUID assignedBy,
            @JsonProperty("due_at") OffsetDateTime dueAt,
            String priority,
            String status,
            @JsonProperty("completion_note") String completionNote,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("started_at") OffsetDateTime startedAt,
            @JsonProperty("completed_at") OffsetDateTime completedAt,
            boolean overdue,
            @JsonProperty("assigned_to_name") String assignedToName,
            @JsonProperty("assigned_by_name") String assignedByName) {}

    public record Detail(
            UUID id,
            String reference,
            String title,
            String description,
            @JsonProperty("assigned_to") UUID assignedTo,
            @JsonProperty("assigned_by") UUID assignedBy,
            @JsonProperty("due_at") OffsetDateTime dueAt,
            String priority,
            String status,
            @JsonProperty("completion_note") String completionNote,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("started_at") OffsetDateTime startedAt,
            @JsonProperty("completed_at") OffsetDateTime completedAt,
            boolean overdue,
            @JsonProperty("assigned_to_name") String assignedToName,
            @JsonProperty("assigned_by_name") String assignedByName,
            List<TimelineEntry> timeline) {}

    public record TimelineEntry(
            UUID id,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("from_status") String fromStatus,
            @JsonProperty("to_status") String toStatus,
            String note,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("actor_name") String actorName) {}

    public record StaffOption(UUID id, @JsonProperty("full_name") String fullName) {}

    public record Options(
            @JsonProperty("current_user_id") UUID currentUserId, List<StaffOption> staff) {}

    public record Status(UUID id, String status) {}

    public record Commented(UUID id, boolean commented) {}
}
