package com.societyapp.controller;

import com.societyapp.application.model.SocietyProfileUpdate;
import com.societyapp.application.port.in.SocietyProfileUseCase;
import com.societyapp.application.port.in.SubscriptionAccessUseCase;
import com.societyapp.controller.mapper.SocietyProfileHttpMapper;
import com.societyapp.controller.model.SocietyLogoUpdateResponse;
import com.societyapp.controller.model.SocietyProfileResponse;
import com.societyapp.security.CurrentUser;
import com.societyapp.society.profile.dto.SocietyProfileUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class SocietyProfileController {
    private final SocietyProfileUseCase service;
    private final SubscriptionAccessUseCase subscriptions;
    private final SocietyProfileHttpMapper mapper;

    public SocietyProfileController(
            SocietyProfileUseCase service,
            SubscriptionAccessUseCase subscriptions,
            SocietyProfileHttpMapper mapper) {
        this.service = service;
        this.subscriptions = subscriptions;
        this.mapper = mapper;
    }

    @GetMapping("/society/profile")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    SocietyProfileResponse ownProfile() {
        var profile = service.get(CurrentUser.societyId());
        var access = subscriptions.evaluate(CurrentUser.societyId());
        return mapper.toResponse(profile, access.allowed(), access.reason());
    }

    @PutMapping("/society/profile")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    SocietyProfileResponse updateOwnProfile(
            @Valid @RequestBody SocietyProfileUpdateRequest request) {
        return mapper.toResponse(
                service.update(CurrentUser.societyId(), update(request)), null, null);
    }

    @PostMapping(value = "/society/profile/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    SocietyLogoUpdateResponse uploadOwnLogo(@RequestPart("file") MultipartFile file) {
        return mapper.toResponse(
                service.uploadLogo(CurrentUser.societyId(), new SpringUploadedFile(file)));
    }

    @PostMapping(
            value = "/admin/platform/societies/{id}/logo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    SocietyLogoUpdateResponse uploadLogoAsSuperAdmin(
            @PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return mapper.toResponse(service.uploadLogo(id, new SpringUploadedFile(file)));
    }

    @GetMapping("/public/societies/{id}/logo")
    ResponseEntity<Resource> logo(@PathVariable UUID id) {
        SocietyProfileUseCase.LogoContent logo = service.getLogo(id);
        if (logo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(logo.contentType()))
                .contentLength(logo.content().length())
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(new org.springframework.core.io.InputStreamResource(logo.content().stream()));
    }

    private SocietyProfileUpdate update(SocietyProfileUpdateRequest request) {
        return new SocietyProfileUpdate(
                request.name(),
                request.description(),
                request.website(),
                request.contactPerson(),
                request.email(),
                request.mobile(),
                request.address(),
                request.city(),
                request.state(),
                request.pinCode());
    }
}
