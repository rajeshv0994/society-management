package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.societyapp.application.model.storage.StorageModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.StorageSourceType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StorageMeterServiceTest {
    private final StoragePolicyRepository policies = mock(StoragePolicyRepository.class);
    private final SocietyStorageAccountRepository accounts =
            mock(SocietyStorageAccountRepository.class);
    private final StorageObjectRepository objects = mock(StorageObjectRepository.class);
    private final StorageAddonPurchaseRepository purchases =
            mock(StorageAddonPurchaseRepository.class);
    private final SocietyRepository societies = mock(SocietyRepository.class);
    private final StorageMeterService service =
            new StorageMeterService(policies, accounts, objects, purchases, societies);
    private final UUID societyId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();
    private StoragePolicy policy;
    private SocietyStorageAccount account;

    @BeforeEach
    void setUp() {
        policy = new StoragePolicy();
        policy.setId((short) 1);
        policy.setDefaultFreeGb(1);
        policy.setPurchaseBlockGb(5);
        policy.setPricePerBlockMonthly(new BigDecimal("199"));
        policy.setUpdatedAt(OffsetDateTime.now());
        account = new SocietyStorageAccount();
        account.setSocietyId(societyId);
        account.setPurchasedBytes(0);
        account.setUpdatedAt(OffsetDateTime.now());
        when(policies.findById((short) 1)).thenReturn(Optional.of(policy));
        when(accounts.findForUpdate(societyId)).thenReturn(Optional.of(account));
        when(objects.findByStorageKey(any())).thenReturn(Optional.empty());
        when(objects.save(any())).thenAnswer(call -> call.getArgument(0));
        when(purchases.save(any())).thenAnswer(call -> call.getArgument(0));
        when(accounts.save(any())).thenAnswer(call -> call.getArgument(0));
        when(societies.findById(societyId)).thenReturn(Optional.of(new Society()));
    }

    @Test
    void rejectsUploadThatWouldExceedAllowance() {
        when(objects.usedBytes(societyId)).thenReturn(StorageMeterService.GB);

        assertThatThrownBy(
                        () ->
                                service.register(
                                        new StorageModels.RegisterCommand(
                                                societyId,
                                                "key.jpg",
                                                "images",
                                                "photo.jpg",
                                                "image/jpeg",
                                                1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Storage limit reached");
        verify(objects, never()).save(any());
    }

    @Test
    void purchaseAddsCapacityAndSnapshotsMonthlyPrice() {
        var result = service.purchase(societyId, actorId, 2);

        assertThat(account.getPurchasedBytes()).isEqualTo(10 * StorageMeterService.GB);
        assertThat(result.blocks()).isEqualTo(2);
        assertThat(result.additionalBytes()).isEqualTo(10 * StorageMeterService.GB);
        assertThat(result.monthlyAmount()).isEqualByComparingTo(new BigDecimal("398"));
    }

    @Test
    void registersAndAttachesManagedFileMetadata() {
        service.register(
                new StorageModels.RegisterCommand(
                        societyId, "key.jpg", "images", "photo.jpg", "image/jpeg", 4096));
        var captured = org.mockito.ArgumentCaptor.forClass(StorageObject.class);
        verify(objects).save(captured.capture());
        var stored = captured.getValue();
        assertThat(stored.getSizeBytes()).isEqualTo(4096);
        assertThat(stored.getCategory()).isEqualTo("IMAGES");

        when(objects.findByStorageKey("key.jpg")).thenReturn(Optional.of(stored));
        var sourceId = UUID.randomUUID();
        service.attach(
                new StorageModels.AttachCommand(
                        "key.jpg", StorageSourceType.ACTIVITY_PHOTO, sourceId, "Annual meeting"));
        assertThat(stored.getSourceType()).isEqualTo("ACTIVITY_PHOTO");
        assertThat(stored.getSourceId()).isEqualTo(sourceId);
        assertThat(stored.getDisplayName()).isEqualTo("Annual meeting");
    }
}
