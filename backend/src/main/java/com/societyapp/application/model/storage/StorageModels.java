package com.societyapp.application.model.storage;

import com.societyapp.domain.type.StorageSourceType;
import java.util.UUID;

public final class StorageModels {
    private StorageModels() {}

    public record RegisterCommand(
            UUID societyId,
            String storageKey,
            String category,
            String originalFilename,
            String contentType,
            long sizeBytes) {}

    public record AttachCommand(
            String storageKey, StorageSourceType sourceType, UUID sourceId, String displayName) {}

    public record RegisterExistingCommand(
            UUID societyId,
            String storageKey,
            String category,
            String contentType,
            long sizeBytes) {}

    public record ManagedObject(
            UUID id,
            UUID societyId,
            String storageKey,
            long sizeBytes,
            StorageSourceType sourceType,
            UUID sourceId) {}
}
