package com.societyapp.application.port.in;

import com.societyapp.application.model.subscription.SubscriptionQuantityModels;
import java.util.UUID;

public interface SubscriptionQuantityUseCase {
    SubscriptionQuantityModels.View get(UUID societyId);

    SubscriptionQuantityModels.View update(
            UUID societyId, SubscriptionQuantityModels.UpdateCommand command);
}
