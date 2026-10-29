package com.societyapp.application.model.storage;

import com.societyapp.domain.type.StorageSourceType;
import com.societyapp.domain.type.StorageStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class StorageViews {
    private StorageViews() {}

    public record Policy(
            int defaultFreeGb,
            int purchaseBlockGb,
            BigDecimal pricePerBlockMonthly,
            OffsetDateTime updatedAt) {}

    public record Purchase(
            UUID id,
            int blocks,
            int blockSizeGb,
            long additionalBytes,
            BigDecimal monthlyAmount,
            StorageStatus status,
            OffsetDateTime createdAt) {}

    public record File(
            UUID id,
            String category,
            String name,
            String contentType,
            long sizeBytes,
            StorageSourceType sourceType,
            OffsetDateTime createdAt,
            boolean cleanupEligible) {}

    public record CategoryUsage(String category, long usedBytes) {}

    public record SocietyDashboard(
            UUID societyId,
            String societyName,
            long usedBytes,
            long freeBytes,
            long purchasedBytes,
            long totalBytes,
            long availableBytes,
            double usagePercent,
            Policy policy,
            BigDecimal monthlyAddonAmount,
            List<CategoryUsage> usageByCategory,
            List<File> files,
            List<Purchase> purchases) {}

    public record AdminSociety(
            UUID societyId,
            String societyName,
            long usedBytes,
            long purchasedBytes,
            BigDecimal monthlyAddonAmount) {}

    public record AdminDashboard(Policy policy, List<AdminSociety> societies) {}
}
