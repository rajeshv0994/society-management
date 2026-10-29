package com.societyapp.controller.mapper;

import com.societyapp.application.model.billing.BillingModels;
import com.societyapp.controller.model.BillingResponse;
import java.util.stream.Collectors;

public final class BillingHttpMapper {
    public BillingResponse.Invoice toResponse(BillingModels.InvoiceView value) {
        var space = value.space();
        return new BillingResponse.Invoice(
                value.id(),
                value.societyId(),
                id(space),
                id(space),
                id(space),
                number(space),
                residentId(space),
                residentName(space),
                residentMobile(space),
                block(space),
                floor(space),
                value.invoiceNumber(),
                value.billingMonth(),
                value.dueDate(),
                value.subtotal(),
                value.membershipFee(),
                value.otherCharges(),
                value.otherChargeNote(),
                value.lateFee(),
                value.total(),
                value.paidAmount(),
                value.outstanding(),
                value.status().code(),
                value.overdue(),
                value.createdAt(),
                value.paymentCount());
    }

    public BillingResponse.Payment toResponse(BillingModels.PaymentView value) {
        var space = value.space();
        return new BillingResponse.Payment(
                value.id(),
                value.invoiceId(),
                value.invoiceNumber(),
                id(space),
                number(space),
                residentId(space),
                residentName(space),
                residentMobile(space),
                block(space),
                floor(space),
                value.amount(),
                paymentMethod(value),
                value.paymentChannel().code(),
                value.providerReference(),
                value.receiptNumber(),
                value.note(),
                value.status().code(),
                value.paidAt(),
                value.paidBy(),
                value.recordedBy(),
                value.payerName(),
                value.recordedByName());
    }

    public BillingResponse.Options toResponse(BillingModels.Options value) {
        return new BillingResponse.Options(
                value.currentUserId(),
                value.role().code(),
                value.paymentMethods().stream()
                        .map(method -> method.code())
                        .collect(Collectors.toUnmodifiableSet()),
                value.onlinePayment() == null ? null : toResponse(value.onlinePayment()),
                value.spaces().stream().map(this::toResponse).toList());
    }

    public BillingResponse.PaymentSetting toResponse(BillingModels.PaymentSettingView value) {
        return new BillingResponse.PaymentSetting(
                value.beneficiaryName(),
                value.bankName(),
                value.accountNumberMasked(),
                value.upiId(),
                value.razorpayKeyId(),
                value.testMode(),
                value.configured(),
                value.updatedAt(),
                value.accountNumber(),
                value.ifscCode());
    }

    public BillingResponse.InvoiceCreated toResponse(BillingModels.InvoiceCreated value) {
        return new BillingResponse.InvoiceCreated(
                value.id(), value.invoiceNumber(), value.status().code());
    }

    public BillingResponse.PaymentRecorded toResponse(BillingModels.PaymentRecorded value) {
        return new BillingResponse.PaymentRecorded(
                value.paymentId(),
                value.receiptNumber(),
                value.invoiceStatus().code(),
                value.paidAmount());
    }

    public BillingResponse.Receipt toResponse(BillingModels.Receipt value) {
        return new BillingResponse.Receipt(
                toResponse(value.payment()),
                value.societyName(),
                value.societyRegistration(),
                value.billingMonth(),
                value.dueDate(),
                value.membershipFee(),
                value.otherCharges(),
                value.otherChargeNote(),
                value.invoiceTotal(),
                value.invoicePaidAmount(),
                value.invoiceStatus().code());
    }

    private BillingResponse.Space toResponse(BillingModels.Space value) {
        return new BillingResponse.Space(
                value.id(),
                value.spaceNumber(),
                value.residentId(),
                value.residentName(),
                value.residentMobile(),
                value.block(),
                value.floorNumber());
    }

    private String paymentMethod(BillingModels.PaymentView value) {
        return value.providerReference() != null && value.providerReference().startsWith("pay_soc_")
                ? value.paymentMethod().razorpayCode()
                : value.paymentMethod().code();
    }

    private java.util.UUID id(BillingModels.Space value) {
        return value == null ? null : value.id();
    }

    private String number(BillingModels.Space value) {
        return value == null ? null : value.spaceNumber();
    }

    private java.util.UUID residentId(BillingModels.Space value) {
        return value == null ? null : value.residentId();
    }

    private String residentName(BillingModels.Space value) {
        return value == null ? null : value.residentName();
    }

    private String residentMobile(BillingModels.Space value) {
        return value == null ? null : value.residentMobile();
    }

    private String block(BillingModels.Space value) {
        return value == null ? null : value.block();
    }

    private String floor(BillingModels.Space value) {
        return value == null ? null : value.floorNumber();
    }
}
