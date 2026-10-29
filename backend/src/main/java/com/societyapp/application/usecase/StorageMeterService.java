package com.societyapp.application.usecase;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.storage.StorageModels;
import com.societyapp.application.model.storage.StorageViews;
import com.societyapp.application.port.in.StorageMeterUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.StorageSourceType;
import com.societyapp.domain.type.StorageStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public class StorageMeterService implements StorageMeterUseCase {
    public static final long GB = 1024L * 1024L * 1024L;
    private final StoragePolicyRepository policies;
    private final SocietyStorageAccountRepository accounts;
    private final StorageObjectRepository objects;
    private final StorageAddonPurchaseRepository purchases;
    private final SocietyRepository societies;

    public StorageMeterService(
            StoragePolicyRepository policies,
            SocietyStorageAccountRepository accounts,
            StorageObjectRepository objects,
            StorageAddonPurchaseRepository purchases,
            SocietyRepository societies) {
        this.policies = policies;
        this.accounts = accounts;
        this.objects = objects;
        this.purchases = purchases;
        this.societies = societies;
    }

    public void register(StorageModels.RegisterCommand command) {
        var societyId = command.societyId();
        var storageKey = command.storageKey();
        var category = command.category();
        var originalFilename = command.originalFilename();
        var contentType = command.contentType();
        var sizeBytes = command.sizeBytes();
        if (objects.findByStorageKey(storageKey).isPresent()) return;
        var policy = requirePolicy();
        var account = lockedAccount(societyId);
        long used = objects.usedBytes(societyId);
        long allowance = bytes(policy.getDefaultFreeGb()) + account.getPurchasedBytes();
        if (sizeBytes > allowance - used) {
            throw new IllegalStateException(
                    "Storage limit reached. Delete files or purchase more storage before uploading.");
        }
        var object = new StorageObject();
        object.setSocietyId(societyId);
        object.setStorageKey(storageKey);
        object.setCategory(category.toUpperCase(Locale.ROOT));
        object.setOriginalFilename(trim(originalFilename, 255));
        object.setContentType(trim(contentType, 100));
        object.setSizeBytes(sizeBytes);
        object.setSourceType(StorageSourceType.UPLOAD.code());
        object.setStatus(StorageStatus.ACTIVE.code());
        object.setCreatedAt(OffsetDateTime.now());
        objects.save(object);
    }

    public void attach(StorageModels.AttachCommand command) {
        objects.findByStorageKey(command.storageKey())
                .ifPresent(
                        object -> {
                            object.setSourceType(command.sourceType().code());
                            object.setSourceId(command.sourceId());
                            object.setDisplayName(trim(command.displayName(), 255));
                            objects.save(object);
                        });
    }

    public void registerExisting(StorageModels.RegisterExistingCommand command) {
        var societyId = command.societyId();
        var storageKey = command.storageKey();
        var category = command.category();
        var contentType = command.contentType();
        var sizeBytes = command.sizeBytes();
        if (!societies.existsById(societyId) || objects.findByStorageKey(storageKey).isPresent()) {
            return;
        }
        var object = new StorageObject();
        object.setSocietyId(societyId);
        object.setStorageKey(storageKey);
        object.setCategory(category.toUpperCase(Locale.ROOT));
        object.setContentType(trim(contentType, 100));
        object.setSizeBytes(sizeBytes);
        object.setSourceType(StorageSourceType.UPLOAD.code());
        object.setDisplayName("Existing " + category.toLowerCase(Locale.ROOT) + " file");
        object.setStatus(StorageStatus.ACTIVE.code());
        object.setCreatedAt(OffsetDateTime.now());
        objects.save(object);
    }

    public void markDeleted(UUID societyId, UUID objectId, UUID actorId) {
        var object = requireActive(societyId, objectId);
        object.setStatus(StorageStatus.DELETED.code());
        object.setDeletedAt(OffsetDateTime.now());
        object.setDeletedBy(actorId);
        objects.save(object);
    }

    public StorageModels.ManagedObject activeObject(UUID societyId, UUID objectId) {
        var object = requireActive(societyId, objectId);
        return new StorageModels.ManagedObject(
                object.getId(),
                object.getSocietyId(),
                object.getStorageKey(),
                object.getSizeBytes(),
                StorageSourceType.from(object.getSourceType()),
                object.getSourceId());
    }

    public StorageViews.Policy updatePolicy(
            UUID actorId, int defaultFreeGb, int purchaseBlockGb, BigDecimal price) {
        if (defaultFreeGb < 0 || purchaseBlockGb < 1 || price.signum() < 0) {
            throw new IllegalArgumentException("Storage policy values are invalid");
        }
        var policy = requirePolicy();
        policy.setDefaultFreeGb(defaultFreeGb);
        policy.setPurchaseBlockGb(purchaseBlockGb);
        policy.setPricePerBlockMonthly(price);
        policy.setUpdatedBy(actorId);
        policy.setUpdatedAt(OffsetDateTime.now());
        policies.save(policy);
        return policyView(policy);
    }

    public StorageViews.Purchase purchase(UUID societyId, UUID actorId, int blocks) {
        if (blocks < 1 || blocks > 100) {
            throw new IllegalArgumentException("Purchase must contain between 1 and 100 blocks");
        }
        societies
                .findById(societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        var policy = requirePolicy();
        var account = lockedAccount(societyId);
        long additional = Math.multiplyExact(bytes(policy.getPurchaseBlockGb()), blocks);
        var purchase = new StorageAddonPurchase();
        purchase.setSocietyId(societyId);
        purchase.setPurchasedBy(actorId);
        purchase.setBlocks(blocks);
        purchase.setBlockSizeGb(policy.getPurchaseBlockGb());
        purchase.setAdditionalBytes(additional);
        purchase.setPricePerBlockMonthly(policy.getPricePerBlockMonthly());
        purchase.setMonthlyAmount(
                policy.getPricePerBlockMonthly().multiply(BigDecimal.valueOf(blocks)));
        purchase.setStatus(StorageStatus.ACTIVE.code());
        purchase.setCreatedAt(OffsetDateTime.now());
        purchase = purchases.save(purchase);
        account.setPurchasedBytes(Math.addExact(account.getPurchasedBytes(), additional));
        account.setUpdatedAt(OffsetDateTime.now());
        accounts.save(account);
        return purchaseView(purchase);
    }

    public StorageViews.SocietyDashboard societyDashboard(UUID societyId) {
        var society =
                societies
                        .findById(societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        var policy = requirePolicy();
        var account = accounts.findById(societyId).orElseGet(() -> emptyAccount(societyId));
        long used = objects.usedBytes(societyId);
        long free = bytes(policy.getDefaultFreeGb());
        long total = free + account.getPurchasedBytes();
        return new StorageViews.SocietyDashboard(
                societyId,
                society.getName(),
                used,
                free,
                account.getPurchasedBytes(),
                total,
                Math.max(0, total - used),
                total == 0 ? 100 : Math.min(100, used * 100.0 / total),
                policyView(policy),
                monthlyAmount(societyId),
                categoryUsage(societyId),
                objects
                        .findBySocietyIdAndStatusOrderByCreatedAtDesc(
                                societyId, StorageStatus.ACTIVE.code())
                        .stream()
                        .map(this::objectView)
                        .toList(),
                purchases.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                        .map(this::purchaseView)
                        .toList());
    }

    public StorageViews.AdminDashboard adminDashboard() {
        return new StorageViews.AdminDashboard(
                policyView(requirePolicy()),
                societies.findAll().stream()
                        .map(
                                society -> {
                                    var account =
                                            accounts.findById(society.getId())
                                                    .orElseGet(() -> emptyAccount(society.getId()));
                                    long used = objects.usedBytes(society.getId());
                                    return new StorageViews.AdminSociety(
                                            society.getId(),
                                            society.getName(),
                                            used,
                                            account.getPurchasedBytes(),
                                            monthlyAmount(society.getId()));
                                })
                        .toList());
    }

    public BigDecimal monthlyAmount(UUID societyId) {
        return purchases.findBySocietyIdAndStatus(societyId, StorageStatus.ACTIVE.code()).stream()
                .map(StorageAddonPurchase::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<StorageViews.CategoryUsage> categoryUsage(UUID societyId) {
        return objects.usageByCategory(societyId).stream()
                .map(
                        row ->
                                new StorageViews.CategoryUsage(
                                        Objects.toString(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }

    private StorageViews.File objectView(StorageObject object) {
        return new StorageViews.File(
                object.getId(),
                object.getCategory(),
                object.getDisplayName() != null
                        ? object.getDisplayName()
                        : object.getOriginalFilename(),
                object.getContentType(),
                object.getSizeBytes(),
                StorageSourceType.from(object.getSourceType()),
                object.getCreatedAt(),
                object.getSourceId() != null);
    }

    private StorageViews.Purchase purchaseView(StorageAddonPurchase purchase) {
        return new StorageViews.Purchase(
                purchase.getId(),
                purchase.getBlocks(),
                purchase.getBlockSizeGb(),
                purchase.getAdditionalBytes(),
                purchase.getMonthlyAmount(),
                StorageStatus.from(purchase.getStatus()),
                purchase.getCreatedAt());
    }

    private StorageViews.Policy policyView(StoragePolicy policy) {
        return new StorageViews.Policy(
                policy.getDefaultFreeGb(),
                policy.getPurchaseBlockGb(),
                policy.getPricePerBlockMonthly(),
                policy.getUpdatedAt());
    }

    private StoragePolicy requirePolicy() {
        return policies.findById((short) 1)
                .orElseThrow(() -> new IllegalStateException("Storage policy is not configured"));
    }

    private StorageObject requireActive(UUID societyId, UUID objectId) {
        return objects.findByIdAndSocietyIdAndStatus(
                        objectId, societyId, StorageStatus.ACTIVE.code())
                .orElseThrow(() -> new ResourceNotFoundException("Stored file not found"));
    }

    private SocietyStorageAccount lockedAccount(UUID societyId) {
        return accounts.findForUpdate(societyId)
                .orElseGet(
                        () -> {
                            var account = emptyAccount(societyId);
                            return accounts.saveAndFlush(account);
                        });
    }

    private SocietyStorageAccount emptyAccount(UUID societyId) {
        var account = new SocietyStorageAccount();
        account.setSocietyId(societyId);
        account.setPurchasedBytes(0);
        account.setUpdatedAt(OffsetDateTime.now());
        return account;
    }

    private long bytes(int gigabytes) {
        return Math.multiplyExact((long) gigabytes, GB);
    }

    private String trim(String value, int length) {
        if (value == null || value.isBlank()) return null;
        return value.length() <= length ? value : value.substring(0, length);
    }
}
