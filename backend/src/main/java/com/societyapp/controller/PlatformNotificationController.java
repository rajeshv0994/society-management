package com.societyapp.controller;

import com.societyapp.application.port.in.PlatformNotificationUseCase;
import com.societyapp.controller.mapper.PlatformNotificationHttpMapper;
import com.societyapp.controller.model.PlatformNotificationResponse;
import com.societyapp.controller.model.PlatformNotificationRetryResponse;
import com.societyapp.domain.type.NotificationStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/platform/notifications")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformNotificationController {
    private final PlatformNotificationUseCase notifications;
    private final PlatformNotificationHttpMapper mapper;

    public PlatformNotificationController(
            PlatformNotificationUseCase notifications, PlatformNotificationHttpMapper mapper) {
        this.notifications = notifications;
        this.mapper = mapper;
    }

    @GetMapping
    List<PlatformNotificationResponse> find(
            @RequestParam(name = "status", required = false) String status) {
        return notifications.find(status == null ? null : NotificationStatus.from(status)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping("/{id}/retry")
    PlatformNotificationRetryResponse retry(@PathVariable UUID id) {
        return mapper.toResponse(notifications.retry(id));
    }
}
