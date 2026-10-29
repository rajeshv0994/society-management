package com.societyapp.application.model.support;

import java.util.UUID;

public record SupportMessageSent(UUID messageId, boolean created) {}
