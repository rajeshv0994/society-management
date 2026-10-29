package com.societyapp.application.policy;

import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.JsonCodecException;
import com.societyapp.application.port.out.persistence.PlatformServiceModuleRepository;
import com.societyapp.domain.model.PlatformServiceModule;
import com.societyapp.domain.model.Society;
import com.societyapp.domain.type.PlatformServiceStatus;
import com.societyapp.domain.type.ServiceAvailabilityType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class ServiceCatalogPolicy {
    private final PlatformServiceModuleRepository services;
    private final JsonCodec json;

    public ServiceCatalogPolicy(PlatformServiceModuleRepository services, JsonCodec json) {
        this.services = services;
        this.json = json;
    }

    public List<String> normalizeSelection(List<String> requested) {
        var active =
                services.findByStatusOrderBySortOrderAscNameAsc(
                        PlatformServiceStatus.ACTIVE.code());
        var activeCodes = new LinkedHashSet<String>();
        var defaults = new LinkedHashSet<String>();
        for (var service : active) {
            activeCodes.add(service.getCode());
            if (ServiceAvailabilityType.DEFAULT_FREE.matches(service.getAvailabilityType())) {
                defaults.add(service.getCode());
            }
        }
        var normalized = new LinkedHashSet<String>(defaults);
        if (requested != null) {
            for (var code : requested) {
                if (code == null || code.isBlank()) continue;
                var value = code.trim().toUpperCase(Locale.ROOT);
                if (!activeCodes.contains(value)) {
                    throw new IllegalArgumentException(
                            "Service " + value + " is unavailable or invalid");
                }
                normalized.add(value);
            }
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Select at least one service");
        }
        return active.stream()
                .map(PlatformServiceModule::getCode)
                .filter(normalized::contains)
                .toList();
    }

    public List<String> selected(Society society) {
        try {
            var values = json.readStrings(society.getEnabledModules());
            return normalizeExisting(values);
        } catch (JsonCodecException exception) {
            throw new IllegalStateException("Society service selection is invalid", exception);
        }
    }

    public String writeSelection(List<String> values) {
        try {
            return json.write(values);
        } catch (JsonCodecException exception) {
            throw new IllegalStateException("Could not save selected services", exception);
        }
    }

    public Pricing servicePricing(Society society) {
        var selected = new LinkedHashSet<>(selected(society));
        var lines = new ArrayList<PricingLine>();
        var monthly = BigDecimal.ZERO;
        for (var service : services.findAllByOrderBySortOrderAscNameAsc()) {
            if (!selected.contains(service.getCode())) continue;
            var amount = BigDecimal.ZERO;
            if (PlatformServiceStatus.ACTIVE.matches(service.getStatus())
                    && ServiceAvailabilityType.PAID_PER_SPACE.matches(
                            service.getAvailabilityType())) {
                amount =
                        service.getPricePerSpace()
                                .multiply(BigDecimal.valueOf(society.getTotalFlats()));
                monthly = monthly.add(amount);
            }
            lines.add(
                    new PricingLine(
                            service.getCode(),
                            service.getName(),
                            ServiceAvailabilityType.from(service.getAvailabilityType()),
                            service.getPricePerSpace(),
                            society.getTotalFlats(),
                            amount));
        }
        return new Pricing(monthly, List.copyOf(lines));
    }

    public List<CatalogItem> catalogFor(Society society) {
        var selected = new LinkedHashSet<>(selected(society));
        return services.findAllByOrderBySortOrderAscNameAsc().stream()
                .filter(service -> PlatformServiceStatus.ACTIVE.matches(service.getStatus()))
                .map(
                        service ->
                                new CatalogItem(
                                        service.getCode(),
                                        service.getName(),
                                        service.getDescription(),
                                        PlatformServiceStatus.from(service.getStatus()),
                                        ServiceAvailabilityType.from(service.getAvailabilityType()),
                                        service.getPricePerSpace(),
                                        selected.contains(service.getCode()),
                                        ServiceAvailabilityType.DEFAULT_FREE.matches(
                                                        service.getAvailabilityType())
                                                && PlatformServiceStatus.ACTIVE.matches(
                                                        service.getStatus()),
                                        selected.contains(service.getCode())
                                                        && PlatformServiceStatus.ACTIVE.matches(
                                                                service.getStatus())
                                                        && ServiceAvailabilityType.PAID_PER_SPACE
                                                                .matches(
                                                                        service
                                                                                .getAvailabilityType())
                                                ? service.getPricePerSpace()
                                                        .multiply(
                                                                BigDecimal.valueOf(
                                                                        society.getTotalFlats()))
                                                : BigDecimal.ZERO))
                .toList();
    }

    private List<String> normalizeExisting(List<String> values) {
        var result = new LinkedHashSet<String>();
        if (values != null) {
            values.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(value -> value.trim().toUpperCase(Locale.ROOT))
                    .forEach(result::add);
        }
        services
                .findByStatusOrderBySortOrderAscNameAsc(PlatformServiceStatus.ACTIVE.code())
                .stream()
                .filter(
                        service ->
                                ServiceAvailabilityType.DEFAULT_FREE.matches(
                                        service.getAvailabilityType()))
                .map(PlatformServiceModule::getCode)
                .forEach(result::add);
        return services.findAllByOrderBySortOrderAscNameAsc().stream()
                .filter(service -> PlatformServiceStatus.ACTIVE.matches(service.getStatus()))
                .map(PlatformServiceModule::getCode)
                .filter(result::contains)
                .toList();
    }

    public record PricingLine(
            String code,
            String name,
            ServiceAvailabilityType availabilityType,
            BigDecimal pricePerSpace,
            int spaces,
            BigDecimal monthlyAmount) {}

    public record CatalogItem(
            String code,
            String name,
            String description,
            PlatformServiceStatus status,
            ServiceAvailabilityType availabilityType,
            BigDecimal pricePerSpace,
            boolean selected,
            boolean required,
            BigDecimal monthlyAmount) {}

    public record Pricing(BigDecimal monthlyAmount, List<PricingLine> lines) {
        public Pricing {
            lines = List.copyOf(lines);
        }
    }
}
