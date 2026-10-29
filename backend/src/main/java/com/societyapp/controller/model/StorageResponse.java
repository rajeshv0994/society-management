package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class StorageResponse {
    private StorageResponse() {}

    public record Policy(
            @JsonProperty("default_free_gb") int defaultFreeGb,
            @JsonProperty("purchase_block_gb") int purchaseBlockGb,
            @JsonProperty("price_per_block_monthly") BigDecimal pricePerBlockMonthly,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    public record Purchase(
            UUID id,
            int blocks,
            @JsonProperty("block_size_gb") int blockSizeGb,
            @JsonProperty("additional_bytes") long additionalBytes,
            @JsonProperty("monthly_amount") BigDecimal monthlyAmount,
            String status,
            @JsonProperty("created_at") OffsetDateTime createdAt) {}

    public record File(
            UUID id,
            String category,
            String name,
            @JsonProperty("content_type") String contentType,
            @JsonProperty("size_bytes") long sizeBytes,
            @JsonProperty("source_type") String sourceType,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("cleanup_eligible") boolean cleanupEligible) {}

    public record CategoryUsage(String category, @JsonProperty("used_bytes") long usedBytes) {}

    public record SocietyDashboard(
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("society_name") String societyName,
            @JsonProperty("used_bytes") long usedBytes,
            @JsonProperty("free_bytes") long freeBytes,
            @JsonProperty("purchased_bytes") long purchasedBytes,
            @JsonProperty("total_bytes") long totalBytes,
            @JsonProperty("available_bytes") long availableBytes,
            @JsonProperty("usage_percent") double usagePercent,
            Policy policy,
            @JsonProperty("monthly_addon_amount") BigDecimal monthlyAddonAmount,
            @JsonProperty("usage_by_category") List<CategoryUsage> usageByCategory,
            List<File> files,
            List<Purchase> purchases) {}

    public record AdminSociety(
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("society_name") String societyName,
            @JsonProperty("used_bytes") long usedBytes,
            @JsonProperty("purchased_bytes") long purchasedBytes,
            @JsonProperty("monthly_addon_amount") BigDecimal monthlyAddonAmount) {}

    public record AdminDashboard(Policy policy, List<AdminSociety> societies) {}
}
