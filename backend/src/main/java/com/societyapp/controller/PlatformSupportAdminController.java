package com.societyapp.controller;

import com.societyapp.application.model.audit.AuditModels;
import com.societyapp.application.port.in.AuditUseCase;
import com.societyapp.application.port.in.PlatformAdminUseCase;
import com.societyapp.controller.mapper.AuditHttpMapper;
import com.societyapp.controller.mapper.PlatformAdminHttpMapper;
import com.societyapp.controller.model.AuditPageResponse;
import com.societyapp.controller.model.PlatformAdminResponse;
import com.societyapp.domain.type.SupportTicketStatus;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/platform")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformSupportAdminController {
    private final PlatformAdminUseCase service;
    private final AuditUseCase audits;
    private final AuditHttpMapper auditMapper = new AuditHttpMapper();
    private final PlatformAdminHttpMapper mapper = new PlatformAdminHttpMapper();

    public PlatformSupportAdminController(PlatformAdminUseCase service, AuditUseCase audits) {
        this.service = service;
        this.audits = audits;
    }

    public record TicketUpdate(
            @Pattern(regexp = "OPEN|IN_PROGRESS|RESOLVED|CLOSED") String status,
            String resolution) {}

    @GetMapping("/audit")
    AuditPageResponse audit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID societyId,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime to,
            @RequestParam(required = false) String search) {
        return auditMapper.toResponse(
                audits.search(
                        new AuditModels.SearchCriteria(
                                page,
                                size,
                                action,
                                entityType,
                                societyId,
                                actorId,
                                from,
                                to,
                                search,
                                null)));
    }

    @GetMapping("/support")
    List<PlatformAdminResponse.Ticket> tickets() {
        return service.tickets().stream().map(mapper::toResponse).toList();
    }

    @PatchMapping("/support/{id}")
    PlatformAdminResponse.TicketUpdated ticket(
            @PathVariable UUID id, @Valid @RequestBody TicketUpdate request) {
        return mapper.toResponse(
                service.updateTicket(
                        CurrentUser.id(),
                        id,
                        SupportTicketStatus.from(request.status()),
                        request.resolution()));
    }
}
