package com.societyapp.application.port.in;

import com.societyapp.application.model.platform.PlatformNotificationView;
import com.societyapp.application.model.platform.RetryNotificationResult;
import com.societyapp.domain.type.NotificationStatus;
import java.util.List;
import java.util.UUID;

public interface PlatformNotificationUseCase {
    List<PlatformNotificationView> find(NotificationStatus status);

    List<PlatformNotificationView> findBySociety(UUID societyId);

    long failedCount();

    RetryNotificationResult retry(UUID notificationId);
}
