package com.societyapp.application.model.storage;

import com.societyapp.domain.type.StorageStatus;
import java.util.UUID;

public record StorageDeletionView(UUID id, StorageStatus status, long freedBytes) {}
