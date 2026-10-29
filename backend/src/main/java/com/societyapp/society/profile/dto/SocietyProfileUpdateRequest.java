package com.societyapp.society.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SocietyProfileUpdateRequest(
        @NotBlank String name,
        String description,
        String website,
        @NotBlank String contactPerson,
        @Email String email,
        @NotBlank String mobile,
        @NotBlank String address,
        @NotBlank String city,
        @NotBlank String state,
        @Pattern(regexp = "[0-9]{6}") String pinCode) {}
