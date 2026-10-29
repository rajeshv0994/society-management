package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.societyapp.application.model.audit.AuditModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.port.out.persistence.AuditEventRepository.AuditPage;
import com.societyapp.application.service.AuditService;
import com.societyapp.domain.model.*;
import com.societyapp.infrastructure.serialization.JacksonJsonCodec;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class AuditServiceTest {
    private final AuditEventRepository events = mock(AuditEventRepository.class);
    private final SocietyRepository societies = mock(SocietyRepository.class);
    private final UserAccountJpaRepository users = mock(UserAccountJpaRepository.class);
    private final com.societyapp.application.port.out.JsonCodec json =
            new JacksonJsonCodec(new ObjectMapper());
    private final AuditService service = new AuditService(events, societies, users, json);

    @Test
    void recordsSafeStructuredAuditEvent() {
        when(events.save(any(AuditEvent.class))).thenAnswer(call -> call.getArgument(0));
        var societyId = UUID.randomUUID();
        var actorId = UUID.randomUUID();
        var actor = mock(UserAccount.class);
        when(actor.getFullName()).thenReturn("Original Admin");
        when(actor.getEmail()).thenReturn("original@example.com");
        when(users.findById(actorId)).thenReturn(Optional.of(actor));

        service.record(
                new AuditModels.RecordCommand(
                        societyId,
                        actorId,
                        "API_UPDATE_VENDOR",
                        "CONFIGURATION",
                        "vendor-1",
                        false,
                        new AuditModels.Details(
                                Map.of("route", "/api/vendors/{id}", "outcome", "SUCCEEDED"))));

        var captured = org.mockito.ArgumentCaptor.forClass(AuditEvent.class);
        verify(events).save(captured.capture());
        assertThat(captured.getValue().getSocietyId()).isEqualTo(societyId);
        assertThat(captured.getValue().getActorUserId()).isEqualTo(actorId);
        assertThat(captured.getValue().getOccurredAt()).isNotNull();
        assertThat(captured.getValue().getDetails()).contains("SUCCEEDED");
        assertThat(captured.getValue().getDetails())
                .contains("Original Admin", "original@example.com");
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchesWithBoundedPagingParsedDetailsAndBatchResolvedNames() {
        var societyId = UUID.randomUUID();
        var actorId = UUID.randomUUID();
        var event = new AuditEvent();
        event.setId(9L);
        event.setSocietyId(societyId);
        event.setActorUserId(actorId);
        event.setAction("API_UPDATE_VENDOR");
        event.setEntityType("CONFIGURATION");
        event.setEntityId("vendor-1");
        event.setSensitive(true);
        event.setDetails("{\"outcome\":\"SUCCEEDED\"}");
        event.setOccurredAt(OffsetDateTime.parse("2026-07-28T12:00:00Z"));
        when(events.search(any())).thenReturn(new AuditPage(List.of(event), 0, 100, 1, 1));
        var society = mock(Society.class);
        when(society.getId()).thenReturn(societyId);
        when(society.getName()).thenReturn("Green View");
        var actor = mock(UserAccount.class);
        when(actor.getId()).thenReturn(actorId);
        when(actor.getFullName()).thenReturn("Admin User");
        when(societies.findAllById(any())).thenReturn(List.of(society));
        when(users.findAllById(any())).thenReturn(List.of(actor));

        var result =
                service.search(
                        new AuditModels.SearchCriteria(
                                -5,
                                500,
                                "API_UPDATE_VENDOR",
                                "CONFIGURATION",
                                societyId,
                                actorId,
                                OffsetDateTime.parse("2026-07-01T00:00:00Z"),
                                OffsetDateTime.parse("2026-08-01T00:00:00Z"),
                                "vendor",
                                null));

        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(100);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().societyName()).isEqualTo("Green View");
        assertThat(result.items().getFirst().actorName()).isEqualTo("Admin User");
        assertThat(result.items().getFirst().sensitive()).isTrue();
        assertThat(result.items().getFirst().details().values().get("outcome"))
                .isEqualTo("SUCCEEDED");
    }
}
