package com.societyapp.domain.model;

import java.util.UUID;

public abstract class BaseUuidEntity {
    private UUID id;

    @java.lang.SuppressWarnings("all")
    public UUID getId() {
        return this.id;
    }
}
