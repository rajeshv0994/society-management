package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.societyapp.application.model.storage.StorageModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.documents.LocalDocumentStorage;
import com.societyapp.domain.model.*;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StorageCleanupServiceTest {
    private final StorageMeterService meter = mock(StorageMeterService.class);
    private final LocalDocumentStorage storage = mock(LocalDocumentStorage.class);
    private final SocietyDocumentRepository documents = mock(SocietyDocumentRepository.class);
    private final VisitorPhotoRepository visitorPhotos = mock(VisitorPhotoRepository.class);
    private final VisitorRepository visitors = mock(VisitorRepository.class);
    private final ActivityGalleryPhotoRepository activityPhotos =
            mock(ActivityGalleryPhotoRepository.class);
    private final SocietyRepository societies = mock(SocietyRepository.class);
    private final StorageCleanupService service =
            new StorageCleanupService(
                    meter, storage, documents, visitorPhotos, visitors, activityPhotos, societies);

    @Test
    void deletesActivityMetadataAndBytesBeforeReleasingQuota() {
        var societyId = UUID.randomUUID();
        var actorId = UUID.randomUUID();
        var objectId = UUID.randomUUID();
        var sourceId = UUID.randomUUID();
        var object =
                new StorageModels.ManagedObject(
                        objectId,
                        societyId,
                        "society/images/photo.jpg",
                        2048,
                        com.societyapp.domain.type.StorageSourceType.ACTIVITY_PHOTO,
                        sourceId);
        var photo = new ActivityGalleryPhoto();
        when(meter.activeObject(societyId, objectId)).thenReturn(object);
        when(activityPhotos.findByIdAndSocietyId(sourceId, societyId))
                .thenReturn(Optional.of(photo));

        var result = service.delete(societyId, actorId, objectId);

        verify(activityPhotos).delete(photo);
        verify(storage).delete("society/images/photo.jpg");
        verify(meter).markDeleted(societyId, objectId, actorId);
        assertThat(result.freedBytes()).isEqualTo(2048L);
        assertThat(result.status()).isEqualTo(com.societyapp.domain.type.StorageStatus.DELETED);
    }
}
