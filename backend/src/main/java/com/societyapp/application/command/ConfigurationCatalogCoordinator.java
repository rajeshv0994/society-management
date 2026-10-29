package com.societyapp.application.command;

import com.societyapp.application.exception.*;
import com.societyapp.application.policy.StaffPermissionCatalog;
import com.societyapp.application.port.out.*;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class ConfigurationCatalogCoordinator {
    private final VendorRepository vendors;
    private final ComplaintCategoryRepository categories;
    private final MaintenanceChargeRuleRepository charges;
    private final SocietyRepository societies;
    private final AuditEventRepository audits;
    private final JsonCodec json;

    public ConfigurationCatalogCoordinator(
            VendorRepository vendors,
            ComplaintCategoryRepository categories,
            MaintenanceChargeRuleRepository charges,
            SocietyRepository societies,
            AuditEventRepository audits,
            JsonCodec json) {
        this.vendors = vendors;
        this.categories = categories;
        this.charges = charges;
        this.societies = societies;
        this.audits = audits;
        this.json = json;
    }

    public Vendor addVendor(
            UUID society,
            UUID actor,
            String name,
            String type,
            String contact,
            String mobile,
            String email,
            String gst) {
        module(society, BuiltInServiceCode.VENDORS);
        name = required(name, "Vendor name");
        type = required(type, "Service type");
        unique(society, name, type, null);
        var now = OffsetDateTime.now();
        var item = new Vendor();
        item.setSocietyId(society);
        apply(item, name, type, contact, mobile, email, gst);
        item.setStatus(VendorStatus.ACTIVE.code());
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        item = vendors.save(item);
        audit(society, actor, "CONFIGURATION_CREATED", "VENDOR", item.getId(), Map.of());
        return item;
    }

    public Vendor updateVendor(
            UUID society,
            UUID actor,
            UUID id,
            String name,
            String type,
            String contact,
            String mobile,
            String email,
            String gst) {
        module(society, BuiltInServiceCode.VENDORS);
        var item = vendor(society, id);
        name = required(name, "Vendor name");
        type = required(type, "Service type");
        unique(society, name, type, id);
        apply(item, name, type, contact, mobile, email, gst);
        item.setUpdatedAt(OffsetDateTime.now());
        item = vendors.save(item);
        audit(society, actor, "CONFIGURATION_UPDATED", "VENDOR", id, Map.of());
        return item;
    }

    public Vendor vendorStatus(UUID society, UUID actor, UUID id, VendorStatus requested) {
        module(society, BuiltInServiceCode.VENDORS);
        var item = vendor(society, id);
        item.setStatus(requested.code());
        item.setUpdatedAt(OffsetDateTime.now());
        item = vendors.save(item);
        audit(
                society,
                actor,
                "VENDOR_" + requested.code(),
                "VENDOR",
                id,
                Map.of("status", requested.code()));
        return item;
    }

    public ComplaintCategory addCategory(UUID society, UUID actor, String name, int sla) {
        module(society, BuiltInServiceCode.COMPLAINTS);
        name = required(name, "Category name");
        sla(sla);
        if (categories.existsBySocietyIdAndNameIgnoreCase(society, name))
            throw new ConflictException("Complaint category already exists");
        var now = OffsetDateTime.now();
        var item = new ComplaintCategory();
        item.setSocietyId(society);
        item.setName(name);
        item.setSlaHours(sla);
        item.setActive(true);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        item = categories.save(item);
        audit(
                society,
                actor,
                "CONFIGURATION_CREATED",
                "COMPLAINT_CATEGORY",
                item.getId(),
                Map.of());
        return item;
    }

    public ComplaintCategory updateCategory(
            UUID society, UUID actor, UUID id, String name, int sla) {
        module(society, BuiltInServiceCode.COMPLAINTS);
        var item = category(society, id);
        name = required(name, "Category name");
        sla(sla);
        if (categories.existsBySocietyIdAndNameIgnoreCaseAndIdNot(society, name, id))
            throw new ConflictException("Complaint category already exists");
        item.setName(name);
        item.setSlaHours(sla);
        item.setUpdatedAt(OffsetDateTime.now());
        item = categories.save(item);
        audit(society, actor, "CONFIGURATION_UPDATED", "COMPLAINT_CATEGORY", id, Map.of());
        return item;
    }

    public ComplaintCategory categoryStatus(UUID society, UUID actor, UUID id, boolean active) {
        module(society, BuiltInServiceCode.COMPLAINTS);
        var item = category(society, id);
        item.setActive(active);
        item.setUpdatedAt(OffsetDateTime.now());
        item = categories.save(item);
        audit(
                society,
                actor,
                active ? "CATEGORY_ACTIVATED" : "CATEGORY_DEACTIVATED",
                "COMPLAINT_CATEGORY",
                id,
                Map.of("active", active));
        return item;
    }

    public MaintenanceChargeRule addCharge(
            UUID society,
            UUID actor,
            String name,
            ChargeCalculationType type,
            BigDecimal amount,
            ChargeFrequency frequency,
            LocalDate from,
            LocalDate to) {
        module(society, BuiltInServiceCode.BILLING);
        name = required(name, "Charge rule name");
        validate(type, amount, frequency, from, to);
        if (charges.existsBySocietyIdAndNameIgnoreCaseAndEffectiveFrom(society, name, from))
            throw new ConflictException("Charge rule already exists for this effective date");
        var now = OffsetDateTime.now();
        var item = new MaintenanceChargeRule();
        item.setSocietyId(society);
        apply(item, name, type, amount, frequency, from, to);
        item.setActive(true);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        item = charges.save(item);
        audit(society, actor, "CONFIGURATION_CREATED", "CHARGE_RULE", item.getId(), Map.of());
        return item;
    }

    public MaintenanceChargeRule updateCharge(
            UUID society,
            UUID actor,
            UUID id,
            String name,
            ChargeCalculationType type,
            BigDecimal amount,
            ChargeFrequency frequency,
            LocalDate from,
            LocalDate to) {
        module(society, BuiltInServiceCode.BILLING);
        var item = charge(society, id);
        name = required(name, "Charge rule name");
        validate(type, amount, frequency, from, to);
        if (charges.existsBySocietyIdAndNameIgnoreCaseAndEffectiveFromAndIdNot(
                society, name, from, id))
            throw new ConflictException("Charge rule already exists for this effective date");
        apply(item, name, type, amount, frequency, from, to);
        item.setUpdatedAt(OffsetDateTime.now());
        item = charges.save(item);
        audit(society, actor, "CONFIGURATION_UPDATED", "CHARGE_RULE", id, Map.of());
        return item;
    }

    public MaintenanceChargeRule chargeStatus(UUID society, UUID actor, UUID id, boolean active) {
        module(society, BuiltInServiceCode.BILLING);
        var item = charge(society, id);
        item.setActive(active);
        item.setUpdatedAt(OffsetDateTime.now());
        item = charges.save(item);
        audit(
                society,
                actor,
                active ? "CHARGE_RULE_ACTIVATED" : "CHARGE_RULE_DEACTIVATED",
                "CHARGE_RULE",
                id,
                Map.of("active", active));
        return item;
    }

    private void apply(
            Vendor x,
            String name,
            String type,
            String contact,
            String mobile,
            String email,
            String gst) {
        x.setName(name.trim());
        x.setServiceType(type.trim());
        x.setContactPerson(blank(contact));
        x.setMobile(required(mobile, "Vendor mobile"));
        x.setEmail(lower(email));
        x.setGstNumber(upper(gst));
    }

    private void unique(UUID society, String name, String type, UUID excluded) {
        boolean exists =
                excluded == null
                        ? vendors.existsBySocietyIdAndNameIgnoreCaseAndServiceTypeIgnoreCase(
                                society, name, type)
                        : vendors
                                .existsBySocietyIdAndNameIgnoreCaseAndServiceTypeIgnoreCaseAndIdNot(
                                        society, name, type, excluded);
        if (exists) throw new ConflictException("Vendor and service type already exist");
    }

    private void apply(
            MaintenanceChargeRule x,
            String name,
            ChargeCalculationType type,
            BigDecimal amount,
            ChargeFrequency frequency,
            LocalDate from,
            LocalDate to) {
        x.setName(name.trim());
        x.setCalculationType(type.code());
        x.setAmount(amount);
        x.setFrequency(frequency.code());
        x.setEffectiveFrom(from);
        x.setEffectiveTo(to);
    }

    private void validate(
            ChargeCalculationType calculation,
            BigDecimal amount,
            ChargeFrequency frequency,
            LocalDate from,
            LocalDate to) {
        if (amount == null || amount.signum() < 0)
            throw new IllegalArgumentException("Charge amount cannot be negative");
        if (calculation == ChargeCalculationType.PERCENTAGE
                && amount.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("Percentage charge cannot exceed 100");
        if (from == null) throw new IllegalArgumentException("Effective date is required");
        if (to != null && to.isBefore(from))
            throw new IllegalArgumentException("Effective end date cannot be before start date");
    }

    private void sla(int hours) {
        if (hours < 1 || hours > 8760)
            throw new IllegalArgumentException("SLA must be between 1 and 8760 hours");
    }

    private Vendor vendor(UUID society, UUID id) {
        return vendors.findByIdAndSocietyId(id, society)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
    }

    private ComplaintCategory category(UUID society, UUID id) {
        return categories
                .findByIdAndSocietyId(id, society)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint category not found"));
    }

    private MaintenanceChargeRule charge(UUID society, UUID id) {
        return charges.findByIdAndSocietyId(id, society)
                .orElseThrow(() -> new ResourceNotFoundException("Charge rule not found"));
    }

    private void module(UUID id, BuiltInServiceCode code) {
        var society =
                societies
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        if (!StaffPermissionCatalog.moduleEnabled(society.getEnabledModules(), code.code()))
            throw new IllegalStateException(
                    code.code().replace('_', ' ') + " module is not enabled");
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(label + " is required");
        return value.trim();
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String lower(String value) {
        value = blank(value);
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private String upper(String value) {
        value = blank(value);
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    private void audit(
            UUID society, UUID actor, String action, String type, UUID id, Map<String, ?> details) {
        var event = new AuditEvent();
        event.setSocietyId(society);
        event.setActorUserId(actor);
        event.setAction(action);
        event.setEntityType(type);
        event.setEntityId(id == null ? null : id.toString());
        try {
            event.setDetails(json.write(details));
        } catch (JsonCodecException e) {
            event.setDetails("{}");
        }
        event.setOccurredAt(OffsetDateTime.now());
        audits.save(event);
    }
}
