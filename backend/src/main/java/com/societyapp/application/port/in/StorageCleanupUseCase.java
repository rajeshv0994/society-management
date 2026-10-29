package com.societyapp.application.port.in;

import com.societyapp.application.model.storage.StorageDeletionView;
import java.util.UUID;

public interface StorageCleanupUseCase {
    StorageDeletionView delete(UUID societyId, UUID actorId, UUID objectId);
}
