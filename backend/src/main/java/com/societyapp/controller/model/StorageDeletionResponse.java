package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record StorageDeletionResponse(
        UUID id, String status, @JsonProperty("freed_bytes") long freedBytes) {}
