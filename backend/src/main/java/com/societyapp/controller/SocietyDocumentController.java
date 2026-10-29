package com.societyapp.controller;

import com.societyapp.application.port.in.SocietyDocumentUseCase;
import com.societyapp.controller.mapper.SocietyDocumentHttpMapper;
import com.societyapp.controller.model.SocietyDocumentResponse;
import com.societyapp.domain.type.SocietyDocumentType;
import com.societyapp.domain.type.VerificationStatus;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class SocietyDocumentController {
    private final SocietyDocumentUseCase service;
    private final SocietyDocumentHttpMapper mapper;

    public SocietyDocumentController(
            SocietyDocumentUseCase service, SocietyDocumentHttpMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public record VerificationRequest(@Pattern(regexp = "VERIFIED|REJECTED") String status) {}

    @PostMapping(value = "/public/societies/{id}/documents", consumes = "multipart/form-data")
    SocietyDocumentResponse.Created upload(
            @PathVariable UUID id,
            @RequestHeader("X-Registration-Token") String token,
            @RequestParam @Pattern(regexp = "REGISTRATION_CERTIFICATE|GST|PAN|ADDRESS_PROOF")
                    String documentType,
            @RequestPart MultipartFile file) {
        return mapper.toResponse(
                service.upload(
                        id,
                        token,
                        SocietyDocumentType.from(documentType),
                        new SpringUploadedFile(file)));
    }

    @GetMapping("/admin/societies/{id}/documents")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    List<SocietyDocumentResponse.Document> list(@PathVariable UUID id) {
        return service.list(id).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/society/documents")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    List<SocietyDocumentResponse.Document> societyDocuments() {
        return service.list(CurrentUser.societyId()).stream().map(mapper::toResponse).toList();
    }

    @PostMapping(value = "/society/documents", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    SocietyDocumentResponse.Created uploadSocietyDocument(
            @RequestParam @Pattern(regexp = "REGISTRATION_CERTIFICATE|GST|PAN|ADDRESS_PROOF|OTHER")
                    String documentType,
            @RequestPart MultipartFile file) {
        return mapper.toResponse(
                service.uploadAuthenticated(
                        CurrentUser.societyId(),
                        SocietyDocumentType.from(documentType),
                        new SpringUploadedFile(file)));
    }

    @PatchMapping("/admin/societies/{societyId}/documents/{documentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    SocietyDocumentResponse.Verification verify(
            @PathVariable UUID societyId,
            @PathVariable UUID documentId,
            @Valid @RequestBody VerificationRequest body) {
        return mapper.toResponse(
                service.verify(societyId, documentId, VerificationStatus.from(body.status())));
    }
}
