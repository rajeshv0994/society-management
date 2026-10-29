package com.societyapp.controller;

import com.societyapp.application.model.audit.AuditModels;
import com.societyapp.application.port.in.AuditUseCase;
import com.societyapp.controller.mapper.AuditHttpMapper;
import com.societyapp.controller.model.AuditPageResponse;
import com.societyapp.security.CurrentUser;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/society/audit")
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class SocietyAuditController {
    private final AuditUseCase audits;
    private final AuditHttpMapper mapper = new AuditHttpMapper();

    public SocietyAuditController(AuditUseCase audits) {
        this.audits = audits;
    }

    @GetMapping
    AuditPageResponse audit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "true") boolean sensitiveOnly,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime to,
            @RequestParam(required = false) String search) {
        var criteria =
                new AuditModels.SearchCriteria(
                        page,
                        size,
                        action,
                        entityType,
                        CurrentUser.societyId(),
                        actorId,
                        from,
                        to,
                        search,
                        sensitiveOnly ? true : null);
        return mapper.toResponse(audits.search(criteria));
    }
}
