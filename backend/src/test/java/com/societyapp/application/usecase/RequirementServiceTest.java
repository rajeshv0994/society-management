package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.societyapp.application.mapper.RequirementViewMapper;
import com.societyapp.application.model.requirement.RequirementModels;
import com.societyapp.application.port.out.persistence.RequirementRequestRepository;
import com.societyapp.application.port.out.persistence.SocietyCustomPlanRepository;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.service.RequirementService;
import com.societyapp.domain.model.SocietyCustomPlan;
import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.CustomPlanStatus;
import com.societyapp.infrastructure.serialization.JacksonJsonCodec;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

class RequirementServiceTest {
    private final UUID planId = UUID.randomUUID();
    private final UUID societyId = UUID.randomUUID();
    private SocietyCustomPlanRepository customPlans;
    private RequirementService service;
    private SocietyCustomPlan plan;

    @BeforeEach
    void setUp() {
        customPlans = mock(SocietyCustomPlanRepository.class);
        service =
                new RequirementService(
                        mock(RequirementRequestRepository.class),
                        customPlans,
                        mock(SocietyRepository.class),
                        new JacksonJsonCodec(new ObjectMapper()),
                        mock(RequirementViewMapper.class));
        plan = new SocietyCustomPlan();
        ReflectionTestUtils.setField(plan, "id", planId);
        plan.setSocietyId(societyId);
        plan.setStatus("DRAFT");
        when(customPlans.findById(planId)).thenReturn(Optional.of(plan));
    }

    @Test
    void draftCanBeUpdatedAndActivatedWithFixedMonthlyPricing() {
        var previous = new SocietyCustomPlan();
        previous.setSocietyId(societyId);
        previous.setStatus("ACTIVE");
        when(customPlans.findAll()).thenReturn(List.of(plan, previous));

        updatePlan(
                planId,
                "Updated contract",
                "Fixed commercial agreement",
                List.of("RESIDENTS", "BILLING"),
                100,
                25,
                50,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("8500"),
                BigDecimal.ZERO,
                "MONTHLY",
                LocalDate.now(),
                null,
                "ACTIVE");

        assertThat(plan.getPlanName()).isEqualTo("Updated contract");
        assertThat(plan.getPriceMonthly()).isEqualByComparingTo("8500");
        assertThat(plan.getStatus()).isEqualTo("ACTIVE");
        assertThat(plan.getActivatedAt()).isNotNull();
        assertThat(previous.getStatus()).isEqualTo("REPLACED");
        verify(customPlans).save(previous);
        verify(customPlans).save(plan);
    }

    @Test
    void activePlanCannotBeReturnedToDraft() {
        plan.setStatus("ACTIVE");

        assertThatThrownBy(
                        () ->
                                updatePlan(
                                        planId,
                                        "Plan",
                                        null,
                                        List.of("RESIDENTS"),
                                        100,
                                        25,
                                        50,
                                        new BigDecimal("10"),
                                        new BigDecimal("50"),
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        "MONTHLY",
                                        LocalDate.now(),
                                        null,
                                        "DRAFT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot return to draft");
    }

    @Test
    void fixedAndUsagePricesCannotBeMixed() {
        assertThatThrownBy(
                        () ->
                                updatePlan(
                                        planId,
                                        "Plan",
                                        null,
                                        List.of("RESIDENTS"),
                                        100,
                                        25,
                                        50,
                                        BigDecimal.TEN,
                                        BigDecimal.ZERO,
                                        new BigDecimal("8500"),
                                        BigDecimal.ZERO,
                                        "MONTHLY",
                                        LocalDate.now(),
                                        null,
                                        "DRAFT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("either a fixed monthly price");
    }

    @Test
    void activePlanCanBeCancelledButRemainsAuditable() {
        var actorId = UUID.randomUUID();
        plan.setStatus("ACTIVE");

        service.cancelPlan(planId, actorId, "Commercial agreement ended");

        assertThat(plan.getStatus()).isEqualTo("CANCELLED");
        assertThat(plan.getCancelledBy()).isEqualTo(actorId);
        assertThat(plan.getCancelledAt()).isNotNull();
        assertThat(plan.getCancellationReason()).isEqualTo("Commercial agreement ended");
        verify(customPlans).save(plan);
    }

    private void updatePlan(
            UUID id,
            String name,
            String description,
            List<String> modules,
            Integer maxSpaces,
            Integer maxStaff,
            Integer maxStorage,
            BigDecimal pricePerSpace,
            BigDecimal pricePerStaff,
            BigDecimal fixedMonthly,
            BigDecimal setupFee,
            String billingCycle,
            LocalDate validFrom,
            LocalDate validUntil,
            String status) {
        service.updatePlan(
                id,
                new RequirementModels.PlanCommand(
                        null,
                        null,
                        name,
                        description,
                        modules,
                        maxSpaces,
                        maxStaff,
                        maxStorage,
                        pricePerSpace,
                        pricePerStaff,
                        fixedMonthly,
                        setupFee,
                        BillingCycle.from(billingCycle),
                        validFrom,
                        validUntil,
                        CustomPlanStatus.from(status)));
    }
}
