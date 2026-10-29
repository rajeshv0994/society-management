package com.societyapp.application.model;

import java.util.UUID;

public record SocietyLogoUpdateView(UUID societyId, String logoUrl, boolean updated) {}
