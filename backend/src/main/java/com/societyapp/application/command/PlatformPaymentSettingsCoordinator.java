package com.societyapp.application.command;

import com.societyapp.application.mapper.PlatformBillingViewMapper;
import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.application.port.out.persistence.PlatformPaymentSettingRepository;
import com.societyapp.domain.model.PlatformPaymentSetting;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class PlatformPaymentSettingsCoordinator {
    private final PlatformPaymentSettingRepository settings;
    private final PlatformBillingViewMapper views;

    public PlatformPaymentSettingsCoordinator(
            PlatformPaymentSettingRepository settings, PlatformBillingViewMapper views) {
        this.settings = settings;
        this.views = views;
    }

    public PlatformBillingModels.PaymentSettingView update(
            UUID actorId, PlatformBillingModels.PaymentSettingsCommand command) {
        var setting =
                settings.findFirstByOrderByUpdatedAtDesc().orElseGet(PlatformPaymentSetting::new);
        setting.setBeneficiaryName(command.beneficiaryName().trim());
        setting.setBankName(blank(command.bankName()));
        setting.setAccountNumber(blank(command.accountNumber()));
        setting.setIfscCode(
                command.ifscCode() == null ? null : command.ifscCode().trim().toUpperCase());
        setting.setUpiId(blank(command.upiId()));
        setting.setPaymentInstructions(blank(command.instructions()));
        setting.setUpdatedBy(actorId);
        setting.setUpdatedAt(OffsetDateTime.now());
        return views.paymentSetting(settings.save(setting));
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
