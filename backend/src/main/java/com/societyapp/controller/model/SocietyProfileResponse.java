package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SocietyProfileResponse(
        UUID id,
        String name,
        @JsonProperty("registration_number") String registrationNumber,
        @JsonProperty("society_code") String societyCode,
        @JsonProperty("society_type") String societyType,
        @JsonProperty("total_blocks") int totalBlocks,
        @JsonProperty("total_flats") int totalFlats,
        @JsonProperty("total_spaces") int totalSpaces,
        @JsonProperty("planned_staff") int plannedStaff,
        @JsonProperty("approximate_residents") Integer approximateResidents,
        String address,
        String city,
        String state,
        @JsonProperty("pin_code") String pinCode,
        @JsonProperty("contact_person") String contactPerson,
        String email,
        String mobile,
        @JsonProperty("plan_code") String planCode,
        @JsonProperty("enabled_modules") List<String> enabledModules,
        String status,
        String description,
        String website,
        @JsonProperty("logo_url") String logoUrl,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("membership_access_allowed") Boolean membershipAccessAllowed,
        @JsonProperty("membership_access_reason") String membershipAccessReason) {}
