package com.societyapp.application.usecase;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.storage.StorageDeletionView;
import com.societyapp.application.port.in.StorageCleanupUseCase;
import com.societyapp.application.port.in.StorageMeterUseCase;
import com.societyapp.application.port.out.DocumentStorage;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.type.StorageStatus;
import java.util.UUID;

public class StorageCleanupService implements StorageCleanupUseCase {
    private final StorageMeterUseCase meter;
    private final DocumentStorage storage;
    private final SocietyDocumentRepository documents;
    private final VisitorPhotoRepository visitorPhotos;
    private final VisitorRepository visitors;
    private final ActivityGalleryPhotoRepository activityPhotos;
    private final SocietyRepository societies;

    public StorageCleanupService(
            StorageMeterUseCase meter,
            DocumentStorage storage,
            SocietyDocumentRepository documents,
            VisitorPhotoRepository visitorPhotos,
            VisitorRepository visitors,
            ActivityGalleryPhotoRepository activityPhotos,
            SocietyRepository societies) {
        this.meter = meter;
        this.storage = storage;
        this.documents = documents;
        this.visitorPhotos = visitorPhotos;
        this.visitors = visitors;
        this.activityPhotos = activityPhotos;
        this.societies = societies;
    }

    public StorageDeletionView delete(UUID societyId, UUID actorId, UUID objectId) {
        var object = meter.activeObject(societyId, objectId);
        if (object.sourceId() == null) {
            throw new IllegalStateException("This legacy file cannot be safely removed here");
        }
        switch (object.sourceType()) {
            case SOCIETY_DOCUMENT ->
                    documents
                            .findByIdAndSocietyId(object.sourceId(), societyId)
                            .ifPresent(documents::delete);
            case VISITOR_PHOTO -> {
                var photo =
                        visitorPhotos
                                .findById(object.sourceId())
                                .orElseThrow(
                                        () ->
                                                new ResourceNotFoundException(
                                                        "Visitor photo not found"));
                visitors.findByIdAndSocietyId(photo.getVisitorId(), societyId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Visitor photo not found"));
                visitorPhotos.delete(photo);
            }
            case ACTIVITY_PHOTO ->
                    activityPhotos
                            .findByIdAndSocietyId(object.sourceId(), societyId)
                            .ifPresent(activityPhotos::delete);
            case SOCIETY_LOGO -> {
                var society =
                        societies
                                .findById(societyId)
                                .orElseThrow(
                                        () -> new ResourceNotFoundException("Society not found"));
                if (object.storageKey().equals(society.getLogoStorageKey())) {
                    society.setLogoStorageKey(null);
                    society.setLogoContentType(null);
                    societies.save(society);
                }
            }
        }
        storage.delete(object.storageKey());
        meter.markDeleted(societyId, objectId, actorId);
        return new StorageDeletionView(objectId, StorageStatus.DELETED, object.sizeBytes());
    }
}
