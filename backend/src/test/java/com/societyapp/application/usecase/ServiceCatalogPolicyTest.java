package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.application.port.out.persistence.PlatformServiceModuleRepository;
import com.societyapp.domain.model.PlatformServiceModule;
import com.societyapp.domain.model.Society;
import com.societyapp.infrastructure.serialization.JacksonJsonCodec;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ServiceCatalogPolicyTest {
    private PlatformServiceModuleRepository repository;
    private ServiceCatalogPolicy policy;
    private List<PlatformServiceModule> catalog;

    @BeforeEach
    void setUp() {
        repository = mock(PlatformServiceModuleRepository.class);
        policy = new ServiceCatalogPolicy(repository, new JacksonJsonCodec(new ObjectMapper()));
        catalog =
                List.of(
                        service(
                                "RESIDENTS",
                                "Residents",
                                "DEFAULT_FREE",
                                BigDecimal.ZERO,
                                "ACTIVE"),
                        service("NOTICES", "Notices", "OPTIONAL_FREE", BigDecimal.ZERO, "ACTIVE"),
                        service(
                                "AMENITIES",
                                "Amenities",
                                "PAID_PER_SPACE",
                                new BigDecimal("3.50"),
                                "ACTIVE"),
                        service(
                                "FUTURE",
                                "Future",
                                "OPTIONAL_FREE",
                                BigDecimal.ZERO,
                                "COMING_SOON"));
        when(repository.findAllByOrderBySortOrderAscNameAsc()).thenReturn(catalog);
        when(repository.findByStatusOrderBySortOrderAscNameAsc("ACTIVE"))
                .thenReturn(catalog.subList(0, 3));
    }

    @Test
    void defaultsAreAlwaysIncludedAndOrderedByCatalog() {
        assertThat(policy.normalizeSelection(List.of("AMENITIES")))
                .containsExactly("RESIDENTS", "AMENITIES");
        assertThat(policy.normalizeSelection(List.of("amenities", "RESIDENTS", "amenities")))
                .containsExactly("RESIDENTS", "AMENITIES");
    }

    @Test
    void optionalFreeCanBeSelectedWithoutACharge() {
        var society = society("[\"NOTICES\"]", 20);

        var pricing = policy.servicePricing(society);

        assertThat(pricing.monthlyAmount()).isEqualByComparingTo("0");
        assertThat(pricing.lines()).extracting("code").containsExactly("RESIDENTS", "NOTICES");
    }

    @Test
    void paidServiceChargesItsRateForEverySelectedSpace() {
        var pricing = policy.servicePricing(society("[\"AMENITIES\"]", 20));

        assertThat(pricing.monthlyAmount()).isEqualByComparingTo("70.00");
        var amenityLine =
                pricing.lines().stream()
                        .filter(line -> "AMENITIES".equals(line.code()))
                        .findFirst()
                        .orElseThrow();
        assertThat(amenityLine.spaces()).isEqualTo(20);
        assertThat(amenityLine.monthlyAmount()).isEqualByComparingTo("70.00");
    }

    @Test
    void unselectedPaidAndInactiveServicesAreNotCharged() {
        assertThat(policy.servicePricing(society("[\"NOTICES\",\"FUTURE\"]", 100)).monthlyAmount())
                .isEqualByComparingTo("0");
    }

    @Test
    void unknownAndUnavailableSelectionsAreRejected() {
        assertThatThrownBy(() -> policy.normalizeSelection(List.of("UNKNOWN")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNKNOWN");
        assertThatThrownBy(() -> policy.normalizeSelection(List.of("FUTURE")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void societyCatalogMarksDefaultsLockedAndShowsPaidEstimate() {
        var rows = policy.catalogFor(society("[\"AMENITIES\"]", 10));

        var resident =
                rows.stream()
                        .filter(row -> "RESIDENTS".equals(row.code()))
                        .findFirst()
                        .orElseThrow();
        var amenity =
                rows.stream()
                        .filter(row -> "AMENITIES".equals(row.code()))
                        .findFirst()
                        .orElseThrow();
        assertThat(resident.selected()).isTrue();
        assertThat(resident.required()).isTrue();
        assertThat(amenity.monthlyAmount()).isEqualByComparingTo("35.00");
        assertThat(rows).extracting("code").doesNotContain("FUTURE");
    }

    @Test
    void invalidStoredSelectionFailsClearly() {
        assertThatThrownBy(() -> policy.selected(society("not-json", 10)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invalid");
    }

    private PlatformServiceModule service(
            String code, String name, String type, BigDecimal price, String status) {
        var service = new PlatformServiceModule();
        service.setCode(code);
        service.setName(name);
        service.setAvailabilityType(type);
        service.setPricePerSpace(price);
        service.setStatus(status);
        return service;
    }

    private Society society(String modules, int spaces) {
        var society = new Society();
        society.setEnabledModules(modules);
        society.setTotalFlats(spaces);
        return society;
    }
}
