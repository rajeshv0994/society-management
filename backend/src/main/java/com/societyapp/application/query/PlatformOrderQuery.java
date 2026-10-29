package com.societyapp.application.query;

import com.societyapp.application.mapper.PlatformBillingViewMapper;
import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.port.out.persistence.SubscriptionOrderRepository;
import java.util.List;
import java.util.UUID;

public final class PlatformOrderQuery {
    private final SubscriptionOrderRepository orders;
    private final SocietyRepository societies;
    private final PlatformBillingViewMapper views;

    public PlatformOrderQuery(
            SubscriptionOrderRepository orders,
            SocietyRepository societies,
            PlatformBillingViewMapper views) {
        this.orders = orders;
        this.societies = societies;
        this.views = views;
    }

    public List<PlatformBillingModels.OrderView> forSociety(UUID societyId) {
        return orders.findBySocietyIdOrderByBillingMonthDesc(societyId).stream()
                .map(views::order)
                .toList();
    }

    public List<PlatformBillingModels.OrderView> all() {
        return orders.findAllByOrderByCreatedAtDesc().stream()
                .map(order -> views.order(order, society(order.getSocietyId())))
                .toList();
    }

    private PlatformBillingModels.SocietySummary society(UUID societyId) {
        return societies
                .findById(societyId)
                .map(
                        society ->
                                new PlatformBillingModels.SocietySummary(
                                        society.getName(),
                                        society.getEmail(),
                                        society.getMobile(),
                                        society.getSocietyCode()))
                .orElse(new PlatformBillingModels.SocietySummary("Society", null, null, null));
    }
}
