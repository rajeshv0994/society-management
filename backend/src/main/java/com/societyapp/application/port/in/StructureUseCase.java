package com.societyapp.application.port.in;

import com.societyapp.application.model.structure.StructureModels;
import java.util.UUID;

public interface StructureUseCase {
    StructureModels.View get(UUID societyId);

    StructureModels.Created addBlock(UUID societyId, StructureModels.BlockCommand command);

    StructureModels.BlockUpdated updateBlock(
            UUID societyId, UUID blockId, StructureModels.BlockCommand command);

    StructureModels.Deleted deleteBlock(UUID societyId, UUID blockId);

    StructureModels.Created addSection(
            UUID societyId, UUID blockId, StructureModels.SectionCommand command);

    StructureModels.SectionUpdated updateSection(
            UUID societyId, UUID sectionId, StructureModels.SectionCommand command);

    StructureModels.Deleted deleteSection(UUID societyId, UUID sectionId);

    StructureModels.Created addSpace(
            UUID societyId, UUID sectionId, StructureModels.SpaceCommand command);

    StructureModels.Created addSpace(UUID societyId, StructureModels.SpaceCommand command);

    StructureModels.SpaceUpdated updateSpace(
            UUID societyId, UUID spaceId, StructureModels.SpaceCommand command);

    StructureModels.Deleted deleteSpace(UUID societyId, UUID spaceId);
}
