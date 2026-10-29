package com.societyapp.application.query;

import com.societyapp.application.mapper.PlatformSocietyViewMapper;
import com.societyapp.application.model.platformadmin.PlatformAdminModels;
import com.societyapp.application.port.in.SubscriptionQuantityUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.util.*;

public final class PlatformSocietyListQuery {
    private final SocietyRepository societies;
    private final SocietyDocumentRepository documents;
    private final BlockRepository blocks;
    private final FloorRepository floors;
    private final FlatRepository flats;
    private final UserAccountJpaRepository users;
    private final SubscriptionQuantityUseCase subscriptionQuantities;
    private final PlatformSocietyViewMapper mapper;

    public PlatformSocietyListQuery(
            SocietyRepository societies,
            SocietyDocumentRepository documents,
            BlockRepository blocks,
            FloorRepository floors,
            FlatRepository flats,
            UserAccountJpaRepository users,
            SubscriptionQuantityUseCase subscriptionQuantities,
            PlatformSocietyViewMapper mapper) {
        this.societies = societies;
        this.documents = documents;
        this.blocks = blocks;
        this.floors = floors;
        this.flats = flats;
        this.users = users;
        this.subscriptionQuantities = subscriptionQuantities;
        this.mapper = mapper;
    }

    public List<PlatformAdminModels.SocietySummary> get(
            SocietyStatus requestedStatus, String search) {
        String term = search == null ? null : search.toLowerCase(Locale.ROOT);
        return societies.findAll().stream()
                .filter(
                        item ->
                                requestedStatus == null
                                        || requestedStatus.matches(item.getStatus()))
                .filter(
                        item ->
                                term == null
                                        || item.getName().toLowerCase(Locale.ROOT).contains(term)
                                        || item.getRegistrationNumber()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(term))
                .sorted(Comparator.comparing(Society::getCreatedAt).reversed())
                .map(this::societySummary)
                .toList();
    }

    private PlatformAdminModels.SocietySummary societySummary(Society society) {
        long configured =
                blocks.findBySocietyIdOrderByName(society.getId()).stream()
                        .flatMap(
                                block ->
                                        floors
                                                .findByBlockIdOrderByFloorNumber(block.getId())
                                                .stream())
                        .mapToLong(
                                floor -> flats.findByFloorIdOrderByFlatNumber(floor.getId()).size())
                        .sum();
        long activeMembers =
                users.findAll().stream()
                        .filter(user -> society.getId().equals(user.getSocietyId()))
                        .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                        .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                        .count();
        var subscription = subscriptionQuantities.get(society.getId());
        return new PlatformAdminModels.SocietySummary(
                mapper.society(society),
                documents.findBySocietyId(society.getId()).size(),
                configured,
                activeMembers,
                subscription.estimatedMonthlyPrice(),
                subscription.serviceMonthlyPrice());
    }
}
