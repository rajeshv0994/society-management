package com.societyapp.application.port.in;

import com.societyapp.application.model.emergency.EmergencyModels;
import java.util.List;
import java.util.UUID;

public interface EmergencyIncidentUseCase {
    EmergencyModels.Workspace workspace(EmergencyModels.Actor actor);

    List<EmergencyModels.HistoryEntry> history(EmergencyModels.Actor actor, UUID incidentId);

    EmergencyModels.IncidentView create(
            EmergencyModels.Actor actor, EmergencyModels.CreateCommand command);

    EmergencyModels.IncidentView action(
            EmergencyModels.Actor actor, UUID incidentId, EmergencyModels.ActionCommand command);

    EmergencyModels.DirectoryContact saveDirectory(
            EmergencyModels.Actor actor, UUID id, EmergencyModels.DirectoryCommand command);

    void removeDirectory(EmergencyModels.Actor actor, UUID id);
}
