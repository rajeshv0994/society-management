package com.societyapp.controller;

import com.societyapp.application.port.in.SupportUseCase;
import com.societyapp.controller.mapper.SupportHttpMapper;
import com.societyapp.controller.model.SupportResponse;
import com.societyapp.domain.type.SupportPriority;
import com.societyapp.domain.type.UserRole;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN')")
public class SupportChatController {
    private final SupportUseCase service;
    private final SupportHttpMapper mapper;

    public SupportChatController(SupportUseCase service, SupportHttpMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public record CreateTicket(
            @NotBlank String subject,
            @NotBlank String description,
            @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT") String priority) {}

    public record Send(@NotBlank @Size(max = 5000) String message) {}

    @GetMapping
    List<SupportResponse.Ticket> tickets() {
        return service.tickets(CurrentUser.societyId()).stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    SupportResponse.Created create(@Valid @RequestBody CreateTicket r) {
        return mapper.toResponse(
                service.create(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        r.subject(),
                        r.description(),
                        SupportPriority.from(r.priority())));
    }

    @GetMapping("/{ticketId}/messages")
    List<SupportResponse.Message> messages(@PathVariable UUID ticketId) {
        return service
                .messages(
                        ticketId,
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        UserRole.from(CurrentUser.role()))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping("/{ticketId}/messages")
    SupportResponse.Sent send(@PathVariable UUID ticketId, @Valid @RequestBody Send r) {
        return mapper.toResponse(
                service.send(
                        ticketId,
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        UserRole.from(CurrentUser.role()),
                        r.message()));
    }
}
