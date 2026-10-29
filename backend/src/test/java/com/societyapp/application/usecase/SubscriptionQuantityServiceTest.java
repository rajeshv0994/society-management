package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.model.subscription.SubscriptionQuantityModels;
import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.service.SubscriptionQuantityService;
import com.societyapp.domain.model.Block;
import com.societyapp.domain.model.Flat;
import com.societyapp.domain.model.Floor;
import com.societyapp.domain.model.Society;
import com.societyapp.domain.model.SocietyCustomPlan;
import com.societyapp.domain.model.SubscriptionPlan;
import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.PricingModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubscriptionQuantityServiceTest {
    private final UUID societyId = UUID.randomUUID();
    private SocietyRepository societies;
    private SubscriptionPlanRepository plans;
    private SocietyCustomPlanRepository customPlans;
    private BlockRepository blocks;
    private FloorRepository floors;
    private FlatRepository spaces;
    private UserAccountJpaRepository users;
    private ServiceCatalogPolicy catalog;
    private SubscriptionQuantityService service;
    private Society society;

    @BeforeEach
    void setUp() {
        societies = mock(SocietyRepository.class);
        plans = mock(SubscriptionPlanRepository.class);
        customPlans = mock(SocietyCustomPlanRepository.class);
        blocks = mock(BlockRepository.class);
        floors = mock(FloorRepository.class);
        spaces = mock(FlatRepository.class);
        users = mock(UserAccountJpaRepository.class);
        catalog = mock(ServiceCatalogPolicy.class);
        service =
                new SubscriptionQuantityService(
                        societies, plans, customPlans, blocks, floors, spaces, users, catalog);
        society = new Society();
        society.setPlanCode("STANDARD");
        society.setTotalFlats(10);
        society.setPlannedStaff(2);
        society.setSubscriptionBillingCycle("QUARTERLY");
        society.setSubscriptionStatus("ACTIVE");
        society.setEnabledModules("[\"RESIDENTS\",\"AMENITIES\"]");
        var plan = new SubscriptionPlan();
        plan.setCode("STANDARD");
        plan.setPricePerSpace(new BigDecimal("10"));
        plan.setPricePerStaff(new BigDecimal("50"));
        plan.setMaxFlats(100);
        plan.setMaxStaff(20);
        when(societies.findById(societyId)).thenReturn(Optional.of(society));
        when(plans.findById("STANDARD")).thenReturn(Optional.of(plan));
        when(customPlans.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(blocks.findBySocietyIdOrderByName(societyId)).thenReturn(List.of());
        when(users.findBySocietyIdAndRoleOrderByFullName(societyId, "STAFF")).thenReturn(List.of());
        when(catalog.selected(society)).thenReturn(List.of("RESIDENTS", "AMENITIES"));
        when(catalog.catalogFor(society)).thenReturn(List.of());
        when(catalog.servicePricing(society))
                .thenReturn(
                        new ServiceCatalogPolicy.Pricing(
                                new BigDecimal("25"),
                                List.of(
                                        new ServiceCatalogPolicy.PricingLine(
                                                "AMENITIES",
                                                "Amenities",
                                                com.societyapp.domain.type.ServiceAvailabilityType
                                                        .PAID_PER_SPACE,
                                                new BigDecimal("2.50"),
                                                10,
                                                new BigDecimal("25")))));
    }

    @Test
    void totalSeparatesBaseAndPaidServicesAndAppliesBillingCycle() {
        var result = service.get(societyId);

        assertThat(result.baseMonthlyPrice()).isEqualTo(new BigDecimal("200"));
        assertThat(result.serviceMonthlyPrice()).isEqualTo(new BigDecimal("25"));
        assertThat(result.estimatedMonthlyPrice()).isEqualTo(new BigDecimal("225"));
        assertThat(result.billingCycleAmount()).isEqualTo(new BigDecimal("675"));
        assertThat(result.pricingBreakdown()).hasSize(3);
    }

    @Test
    void legacyFixedPriceCustomPlanIsNotReducedToZero() {
        var custom = new SocietyCustomPlan();
        custom.setSocietyId(societyId);
        custom.setStatus("ACTIVE");
        custom.setPriceMonthly(new BigDecimal("8500"));
        custom.setPricePerSpace(BigDecimal.ZERO);
        custom.setPricePerStaff(BigDecimal.ZERO);
        custom.setMaxFlats(100);
        custom.setMaxStaff(25);
        when(customPlans.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(custom));

        var result = service.get(societyId);

        assertThat(result.pricingModel()).isEqualTo(PricingModel.FIXED_CUSTOM);
        assertThat(result.fixedMonthlyPrice()).isEqualTo(new BigDecimal("8500"));
        assertThat(result.baseMonthlyPrice()).isEqualTo(new BigDecimal("8500"));
        assertThat(result.estimatedMonthlyPrice()).isEqualTo(new BigDecimal("8525"));
        assertThat(result.billingCycleAmount()).isEqualTo(new BigDecimal("25575"));
        assertThat(result.pricingBreakdown()).hasSize(4);
    }

    @Test
    void updateNormalizesAndPersistsSelectedServices() {
        when(catalog.normalizeSelection(List.of("AMENITIES")))
                .thenReturn(List.of("RESIDENTS", "AMENITIES"));
        when(catalog.writeSelection(List.of("RESIDENTS", "AMENITIES")))
                .thenReturn("[\"RESIDENTS\",\"AMENITIES\"]");

        service.update(
                societyId,
                new SubscriptionQuantityModels.UpdateCommand(
                        12, 3, BillingCycle.YEARLY, List.of("AMENITIES")));

        assertThat(society.getTotalFlats()).isEqualTo(12);
        assertThat(society.getPlannedStaff()).isEqualTo(3);
        assertThat(society.getSubscriptionBillingCycle()).isEqualTo("YEARLY");
        assertThat(society.getEnabledModules()).contains("RESIDENTS", "AMENITIES");
        verify(societies).save(society);
    }

    @Test
    void quantityOnlyUpdateKeepsExistingServiceSelection() {
        service.update(
                societyId,
                new SubscriptionQuantityModels.UpdateCommand(11, 2, BillingCycle.MONTHLY, null));

        verify(catalog, never()).normalizeSelection(any());
        assertThat(society.getEnabledModules()).isEqualTo("[\"RESIDENTS\",\"AMENITIES\"]");
    }

    @Test
    void cannotReduceBelowConfiguredSpaces() {
        var block = mock(Block.class);
        var blockId = UUID.randomUUID();
        when(block.getId()).thenReturn(blockId);
        var floor = mock(Floor.class);
        var floorId = UUID.randomUUID();
        when(floor.getId()).thenReturn(floorId);
        when(blocks.findBySocietyIdOrderByName(societyId)).thenReturn(List.of(block));
        when(floors.findByBlockIdOrderByFloorNumber(blockId)).thenReturn(List.of(floor));
        when(spaces.findByFloorIdOrderByFlatNumber(floorId))
                .thenReturn(List.of(new Flat(), new Flat(), new Flat()));

        assertThatThrownBy(
                        () ->
                                service.update(
                                        societyId,
                                        new SubscriptionQuantityModels.UpdateCommand(
                                                2, 2, BillingCycle.MONTHLY, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("3 spaces currently configured");
    }

    @Test
    void planMaximumStillAppliesWithServicePricing() {
        assertThatThrownBy(
                        () ->
                                service.update(
                                        societyId,
                                        new SubscriptionQuantityModels.UpdateCommand(
                                                101, 2, BillingCycle.MONTHLY, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("maximum of 100 spaces");
        assertThatThrownBy(
                        () ->
                                service.update(
                                        societyId,
                                        new SubscriptionQuantityModels.UpdateCommand(
                                                10, 21, BillingCycle.MONTHLY, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("maximum of 20 staff");
    }
}
