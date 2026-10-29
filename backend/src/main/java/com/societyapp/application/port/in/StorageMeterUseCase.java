package com.societyapp.application.port.in;

import com.societyapp.application.model.storage.StorageModels;
import com.societyapp.application.model.storage.StorageViews;
import java.math.BigDecimal;
import java.util.UUID;

public interface StorageMeterUseCase {
    void register(StorageModels.RegisterCommand command);

    void attach(StorageModels.AttachCommand command);

    void registerExisting(StorageModels.RegisterExistingCommand command);

    void markDeleted(UUID societyId, UUID objectId, UUID actorId);

    StorageModels.ManagedObject activeObject(UUID societyId, UUID objectId);

    StorageViews.Policy updatePolicy(
            UUID actorId, int defaultFreeGb, int purchaseBlockGb, BigDecimal price);

    StorageViews.Purchase purchase(UUID societyId, UUID actorId, int blocks);

    StorageViews.SocietyDashboard societyDashboard(UUID societyId);

    StorageViews.AdminDashboard adminDashboard();

    BigDecimal monthlyAmount(UUID societyId);
}
