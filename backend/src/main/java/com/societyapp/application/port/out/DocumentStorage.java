package com.societyapp.application.port.out;

import com.societyapp.application.model.BinaryContent;
import java.util.UUID;

public interface DocumentStorage {
    String store(UUID scope, UploadedFile file);

    String storeImage(UUID scope, UploadedFile file);

    String storeMedia(UUID scope, UploadedFile file);

    BinaryContent load(String key);

    void attach(String key, String sourceType, UUID sourceId, String displayName);

    void delete(String key);
}
