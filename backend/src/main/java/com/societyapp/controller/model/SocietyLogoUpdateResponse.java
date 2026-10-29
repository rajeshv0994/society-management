package com.societyapp.controller.model;

import java.util.UUID;

public record SocietyLogoUpdateResponse(UUID societyId, String logoUrl, boolean updated) {}
