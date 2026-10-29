package com.societyapp.application.model;

public record SocietyProfileUpdate(
        String name,
        String description,
        String website,
        String contactPerson,
        String email,
        String mobile,
        String address,
        String city,
        String state,
        String pinCode) {}
