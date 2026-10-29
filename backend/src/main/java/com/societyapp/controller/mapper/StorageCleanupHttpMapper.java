package com.societyapp.controller.mapper;

import com.societyapp.application.model.storage.StorageDeletionView;
import com.societyapp.controller.model.StorageDeletionResponse;
import org.springframework.stereotype.Component;

@Component
public class StorageCleanupHttpMapper {
    public StorageDeletionResponse toResponse(StorageDeletionView view) {
        return new StorageDeletionResponse(view.id(), view.status().code(), view.freedBytes());
    }
}
