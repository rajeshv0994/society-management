package com.societyapp.application.model.platform;

import com.societyapp.domain.type.NotificationStatus;
import java.util.UUID;

public record RetryNotificationResult(UUID id, NotificationStatus status) {}
