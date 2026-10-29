package com.societyapp.domain.model;

import java.util.UUID;

public class Block extends BaseUuidEntity {
    private UUID societyId;
    private String name;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setName(final String name) {
        this.name = name;
    }

    @java.lang.SuppressWarnings("all")
    public Block() {}
}
