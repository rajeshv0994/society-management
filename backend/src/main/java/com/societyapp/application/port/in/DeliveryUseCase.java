package com.societyapp.application.port.in;

import com.societyapp.application.model.delivery.DeliveryModels;
import java.util.List;
import java.util.UUID;

public interface DeliveryUseCase {
    DeliveryModels.Workspace workspace(DeliveryModels.Actor actor);

    List<DeliveryModels.HistoryEntry> history(DeliveryModels.Actor actor, UUID deliveryId);

    DeliveryModels.View create(DeliveryModels.Actor actor, DeliveryModels.CreateCommand command);

    DeliveryModels.View action(
            DeliveryModels.Actor actor, UUID deliveryId, DeliveryModels.ActionCommand command);

    DeliveryModels.View checkout(
            DeliveryModels.Actor actor, UUID deliveryId, DeliveryModels.PaymentCommand command);

    DeliveryModels.View recordCod(
            DeliveryModels.Actor actor, UUID deliveryId, DeliveryModels.PaymentCommand command);
}
