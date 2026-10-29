package com.societyapp.application.service;

import com.societyapp.application.port.out.persistence.ComplaintUpdateRepository;
import com.societyapp.domain.model.ComplaintUpdate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ComplaintTimelineWriter {
    private final ComplaintUpdateRepository updates;

    public ComplaintTimelineWriter(ComplaintUpdateRepository updates) {
        this.updates = updates;
    }

    public void record(
            UUID complaintId, UUID actorId, String event, String from, String to, String note) {
        var update = new ComplaintUpdate();
        update.setComplaintId(complaintId);
        update.setActorUserId(actorId);
        update.setEventType(event);
        update.setFromStatus(from);
        update.setToStatus(to);
        update.setNote(note);
        update.setCreatedAt(OffsetDateTime.now());
        updates.save(update);
    }
}
