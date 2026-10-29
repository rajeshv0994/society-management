package com.societyapp.controller.mapper;

import com.societyapp.application.model.storage.StorageViews;
import com.societyapp.controller.model.StorageResponse;
import org.springframework.stereotype.Component;

@Component
public class StorageHttpMapper {
    public StorageResponse.Policy toResponse(StorageViews.Policy v) {
        return new StorageResponse.Policy(
                v.defaultFreeGb(), v.purchaseBlockGb(), v.pricePerBlockMonthly(), v.updatedAt());
    }

    public StorageResponse.Purchase toResponse(StorageViews.Purchase v) {
        return new StorageResponse.Purchase(
                v.id(),
                v.blocks(),
                v.blockSizeGb(),
                v.additionalBytes(),
                v.monthlyAmount(),
                v.status().code(),
                v.createdAt());
    }

    public StorageResponse.File toResponse(StorageViews.File v) {
        return new StorageResponse.File(
                v.id(),
                v.category(),
                v.name(),
                v.contentType(),
                v.sizeBytes(),
                v.sourceType().code(),
                v.createdAt(),
                v.cleanupEligible());
    }

    public StorageResponse.CategoryUsage toResponse(StorageViews.CategoryUsage v) {
        return new StorageResponse.CategoryUsage(v.category(), v.usedBytes());
    }

    public StorageResponse.SocietyDashboard toResponse(StorageViews.SocietyDashboard v) {
        return new StorageResponse.SocietyDashboard(
                v.societyId(),
                v.societyName(),
                v.usedBytes(),
                v.freeBytes(),
                v.purchasedBytes(),
                v.totalBytes(),
                v.availableBytes(),
                v.usagePercent(),
                toResponse(v.policy()),
                v.monthlyAddonAmount(),
                v.usageByCategory().stream().map(this::toResponse).toList(),
                v.files().stream().map(this::toResponse).toList(),
                v.purchases().stream().map(this::toResponse).toList());
    }

    public StorageResponse.AdminSociety toResponse(StorageViews.AdminSociety v) {
        return new StorageResponse.AdminSociety(
                v.societyId(),
                v.societyName(),
                v.usedBytes(),
                v.purchasedBytes(),
                v.monthlyAddonAmount());
    }

    public StorageResponse.AdminDashboard toResponse(StorageViews.AdminDashboard v) {
        return new StorageResponse.AdminDashboard(
                toResponse(v.policy()), v.societies().stream().map(this::toResponse).toList());
    }
}
