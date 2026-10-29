package com.societyapp.controller.mapper;

import com.societyapp.application.model.platform.PlatformNotificationView;
import com.societyapp.application.model.platform.RetryNotificationResult;
import com.societyapp.controller.model.PlatformNotificationResponse;
import com.societyapp.controller.model.PlatformNotificationRetryResponse;
import org.springframework.stereotype.Component;

@Component
public class PlatformNotificationHttpMapper {
    public PlatformNotificationResponse toResponse(PlatformNotificationView view) {
        return new PlatformNotificationResponse(
                view.id(),
                view.societyId(),
                view.societyName(),
                view.channel().code(),
                view.destination(),
                view.templateCode(),
                view.payload(),
                view.status().code(),
                view.attempts(),
                view.createdAt(),
                view.sentAt(),
                view.lastError());
    }

    public PlatformNotificationRetryResponse toResponse(RetryNotificationResult result) {
        return new PlatformNotificationRetryResponse(result.id(), result.status().code());
    }
}
