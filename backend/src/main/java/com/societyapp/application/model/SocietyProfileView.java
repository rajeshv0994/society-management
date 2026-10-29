package com.societyapp.application.model;

import com.societyapp.domain.type.SocietyStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SocietyProfileView(
        UUID id,
        String name,
        String registrationNumber,
        String societyCode,
        String societyType,
        int totalBlocks,
        int totalFlats,
        int plannedStaff,
        Integer approximateResidents,
        String address,
        String city,
        String state,
        String pinCode,
        String contactPerson,
        String email,
        String mobile,
        String planCode,
        List<String> enabledModules,
        SocietyStatus status,
        String description,
        String website,
        String logoUrl,
        OffsetDateTime updatedAt) {}
