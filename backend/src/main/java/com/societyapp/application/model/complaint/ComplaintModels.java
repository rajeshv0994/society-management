package com.societyapp.application.model.complaint;

import com.societyapp.domain.type.ComplaintStatus;
import com.societyapp.domain.type.UserRole;
import com.societyapp.domain.type.WorkPriority;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ComplaintModels {
    private ComplaintModels() {}

    public record Actor(UUID societyId, UUID userId, UserRole role) {}

    public record CreateCommand(
            UUID residentId,
            UUID spaceId,
            UUID categoryId,
            UUID assignedTo,
            String title,
            String description,
            WorkPriority priority) {}

    public record UpdateCommand(
            ComplaintStatus status, UUID assignedTo, String note, String resolution) {}

    public record Space(UUID spaceId, String spaceNumber, String block) {}

    public record ResidentOption(
            UUID residentId,
            String fullName,
            String mobile,
            UUID spaceId,
            String spaceNumber,
            String block) {}

    public record Category(UUID id, String name, int slaHours) {}

    public record Staff(UUID id, String fullName, String mobile) {}

    public record Options(
            UUID currentUserId,
            List<Category> categories,
            List<ResidentOption> residents,
            List<Staff> staff,
            List<Space> spaces) {}

    public record View(
            UUID id,
            String reference,
            UUID residentId,
            UUID spaceId,
            UUID categoryId,
            String title,
            String description,
            WorkPriority priority,
            ComplaintStatus status,
            UUID assignedTo,
            String resolution,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime dueAt,
            OffsetDateTime closedAt,
            boolean overdue,
            String residentName,
            String createdByName,
            String assignedToName,
            String categoryName,
            String spaceNumber,
            String block) {}

    public record TimelineEntry(
            UUID id,
            String eventType,
            ComplaintStatus fromStatus,
            ComplaintStatus toStatus,
            String note,
            OffsetDateTime createdAt,
            String actorName) {}

    public record Detail(View complaint, List<TimelineEntry> timeline) {
        public Detail {
            timeline = List.copyOf(timeline);
        }
    }

    public record Status(UUID id, ComplaintStatus status) {}

    public record Commented(UUID id, boolean commented) {}
}
