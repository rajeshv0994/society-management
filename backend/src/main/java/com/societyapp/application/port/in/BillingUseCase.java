package com.societyapp.application.port.in;

import com.societyapp.application.model.billing.BillingModels;
import java.util.List;
import java.util.UUID;

public interface BillingUseCase {
    List<BillingModels.InvoiceView> list(UUID societyId, BillingModels.Actor actor);

    List<BillingModels.PaymentView> payments(UUID societyId, BillingModels.Actor actor);

    List<BillingModels.PaymentView> invoicePayments(
            UUID societyId, BillingModels.Actor actor, UUID invoiceId);

    BillingModels.Options options(UUID societyId, BillingModels.Actor actor);

    BillingModels.InvoiceCreated create(UUID societyId, BillingModels.InvoiceCommand command);

    BillingModels.PaymentRecorded pay(
            UUID societyId,
            BillingModels.Actor actor,
            UUID invoiceId,
            BillingModels.PaymentCommand command);

    BillingModels.PaymentRecorded checkout(
            UUID societyId,
            BillingModels.Actor actor,
            UUID invoiceId,
            BillingModels.CheckoutCommand command);

    BillingModels.PaymentSettingView updatePaymentSettings(
            UUID societyId, UUID actorId, BillingModels.PaymentSettingsCommand command);

    BillingModels.Receipt receipt(UUID societyId, BillingModels.Actor actor, UUID paymentId);

    byte[] receiptPdf(UUID societyId, BillingModels.Actor actor, UUID paymentId);
}
