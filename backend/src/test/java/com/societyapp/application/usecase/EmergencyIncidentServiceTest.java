package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.model.emergency.EmergencyModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.service.EmergencyIncidentService;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmergencyIncidentServiceTest {
    private final EmergencyIncidentRepository incidents = mock(EmergencyIncidentRepository.class);
    private final EmergencyIncidentEventRepository events =
            mock(EmergencyIncidentEventRepository.class);
    private final EmergencyDirectoryContactRepository directory =
            mock(EmergencyDirectoryContactRepository.class);
    private final UserAccountJpaRepository users = mock(UserAccountJpaRepository.class);
    private final FlatRepository flats = mock(FlatRepository.class);
    private final FloorRepository floors = mock(FloorRepository.class);
    private final BlockRepository blocks = mock(BlockRepository.class);
    private final StructureAccessService access = mock(StructureAccessService.class);
    private final StaffPermissionRepository permissions = mock(StaffPermissionRepository.class);
    private final HouseholdEmergencyContactRepository householdContacts =
            mock(HouseholdEmergencyContactRepository.class);
    private final NotificationOutboxRepository notifications =
            mock(NotificationOutboxRepository.class);
    private final EmergencyIncidentService typedService =
            new EmergencyIncidentService(
                    incidents,
                    events,
                    directory,
                    users,
                    flats,
                    floors,
                    blocks,
                    access,
                    permissions,
                    householdContacts,
                    notifications);
    private final EmergencyTestFacade service = new EmergencyTestFacade(typedService);

    private final UUID societyId = UUID.randomUUID();
    private final UUID residentId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();
    private final UUID flatId = UUID.randomUUID();
    private final UUID incidentId = UUID.randomUUID();

    @BeforeEach
    void defaults() {
        when(incidents.save(any(EmergencyIncident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(events.save(any(EmergencyIncidentEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(directory.save(any(EmergencyDirectoryContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notifications.save(any(NotificationOutbox.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(users.findById(residentId)).thenReturn(Optional.of(resident()));
        when(access.occupiedBy(flatId, residentId)).thenReturn(true);
        when(flats.findById(flatId)).thenReturn(Optional.empty());
        when(directory.findBySocietyIdAndActiveTrueOrderByContactTypeAscPriorityAsc(societyId))
                .thenReturn(List.of());
    }

    @Test
    void residentWorkspaceIsScopedAndIncludesOperationalMetrics() {
        when(incidents.findBySocietyIdAndResidentIdOrderByCreatedAtDesc(societyId, residentId))
                .thenReturn(
                        List.of(incident("REPORTED", "CRITICAL"), incident("RESOLVED", "HIGH")));

        var result = service.workspace(societyId, residentId, "RESIDENT");

        assertThat(result)
                .containsEntry("total", 2)
                .containsEntry("open", 1L)
                .containsEntry("critical", 1L)
                .containsEntry("resolved", 1L);
    }

    @Test
    void staffRequiresEmergencyPermission() {
        assertThatThrownBy(() -> service.workspace(societyId, staffId, "STAFF"))
                .isInstanceOf(AuthorizationException.class);

        allowStaff();
        when(incidents.findBySocietyIdOrderByCreatedAtDesc(societyId)).thenReturn(List.of());
        assertThat(service.workspace(societyId, staffId, "STAFF")).containsEntry("total", 0);
    }

    @Test
    void residentCanReportCriticalMedicalEmergencyForOwnSpace() {
        var result =
                createAsResident(
                        "CRITICAL", BigDecimal.valueOf(18.5204), BigDecimal.valueOf(73.8567));

        assertThat(result)
                .containsEntry("status", "REPORTED")
                .containsEntry("severity", "CRITICAL")
                .containsEntry("incident_type", "MEDICAL")
                .containsEntry("people_affected", 1);
        verify(events).save(any(EmergencyIncidentEvent.class));
    }

    @Test
    void residentCannotReportAgainstAnotherSpace() {
        when(access.occupiedBy(flatId, residentId)).thenReturn(false);

        assertThatThrownBy(() -> createAsResident("HIGH", null, null))
                .hasMessageContaining("do not occupy");
    }

    @Test
    void coordinatesAreValidatedAtServiceBoundary() {
        assertThatThrownBy(() -> createAsResident("HIGH", BigDecimal.valueOf(91), BigDecimal.ZERO))
                .hasMessageContaining("Latitude");
        assertThatThrownBy(() -> createAsResident("HIGH", BigDecimal.ZERO, BigDecimal.valueOf(181)))
                .hasMessageContaining("Longitude");
    }

    @Test
    void managerCanReportCommonAreaIncidentWithoutResident() {
        var result =
                service.create(
                        societyId,
                        staffId,
                        "SOCIETY_ADMIN",
                        null,
                        null,
                        "FIRE",
                        "CRITICAL",
                        "Smoke in electrical room",
                        "Smoke visible near panel",
                        "Tower A electrical room",
                        null,
                        null,
                        0,
                        null,
                        null,
                        null);

        assertThat(result)
                .containsEntry("status", "REPORTED")
                .containsEntry("resident_id", null)
                .containsEntry("flat_id", null);
    }

    @Test
    void responseLifecycleCapturesAcknowledgementArrivalAndStabilization() {
        var item = incident("REPORTED", "HIGH");
        stub(item);

        assertThat(action("ACKNOWLEDGE", null)).containsEntry("status", "ACKNOWLEDGED");
        assertThat(item.getAcknowledgedAt()).isNotNull();
        assertThat(action("START_RESPONSE", null)).containsEntry("status", "RESPONDING");
        assertThat(action("ARRIVE", null)).containsEntry("status", "ON_SCENE");
        assertThat(action("STABILIZE", "First aid completed"))
                .containsEntry("status", "STABILIZED");
        assertThat(item.getStabilizedAt()).isNotNull();
    }

    @Test
    void externalServiceRecordsRequireCallDetails() {
        var item = incident("RESPONDING", "CRITICAL");
        stub(item);

        assertThatThrownBy(() -> action("REQUEST_AMBULANCE", " "))
                .hasMessageContaining("Dispatch details");
        var result =
                service.action(
                        societyId,
                        staffId,
                        "SOCIETY_ADMIN",
                        incidentId,
                        "REQUEST_AMBULANCE",
                        "Called city ambulance control",
                        null,
                        "City Hospital",
                        "CALL-100",
                        null);
        assertThat(result)
                .containsEntry("ambulance_requested", true)
                .containsEntry("hospital_name", "City Hospital")
                .containsEntry("external_reference", "CALL-100");
    }

    @Test
    void severityCanOnlyEscalate() {
        var item = incident("ACKNOWLEDGED", "MEDIUM");
        stub(item);

        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        incidentId,
                                        "ESCALATE",
                                        null,
                                        null,
                                        null,
                                        null,
                                        "MEDIUM"))
                .hasMessageContaining("must be higher");
        assertThat(
                        service.action(
                                societyId,
                                staffId,
                                "SOCIETY_ADMIN",
                                incidentId,
                                "ESCALATE",
                                null,
                                null,
                                null,
                                null,
                                "CRITICAL"))
                .containsEntry("severity", "CRITICAL");
    }

    @Test
    void resolveCloseAndReopenFollowControlledLifecycle() {
        var item = incident("STABILIZED", "HIGH");
        stub(item);

        assertThat(action("RESOLVE", "Patient transferred safely"))
                .containsEntry("status", "RESOLVED");
        assertThat(action("CLOSE", "Post incident review complete"))
                .containsEntry("status", "CLOSED");
        assertThat(action("REOPEN", "Hospital requested follow-up support"))
                .containsEntry("status", "RESPONDING");
        assertThat(item.getResolvedAt()).isNull();
        assertThat(item.getClosedAt()).isNull();
    }

    @Test
    void residentCanAddUpdateAndCancelBeforeResponse() {
        var item = incident("REPORTED", "HIGH");
        stub(item);

        assertThat(
                        service.action(
                                societyId,
                                residentId,
                                "RESIDENT",
                                incidentId,
                                "ADD_UPDATE",
                                "Person is conscious",
                                null,
                                null,
                                null,
                                null))
                .containsEntry("status", "REPORTED");
        assertThat(
                        service.action(
                                societyId,
                                residentId,
                                "RESIDENT",
                                incidentId,
                                "CANCEL",
                                "Submitted twice",
                                null,
                                null,
                                null,
                                null))
                .containsEntry("status", "CANCELLED");
    }

    @Test
    void residentCannotCancelAnotherResidentsIncidentOrActiveResponse() {
        var item = incident("REPORTED", "HIGH");
        stub(item);
        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        UUID.randomUUID(),
                                        "RESIDENT",
                                        incidentId,
                                        "CANCEL",
                                        "Mistake",
                                        null,
                                        null,
                                        null,
                                        null))
                .isInstanceOf(AuthorizationException.class);

        item.setStatus("RESPONDING");
        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        residentId,
                                        "RESIDENT",
                                        incidentId,
                                        "CANCEL",
                                        "No longer needed",
                                        null,
                                        null,
                                        null,
                                        null))
                .hasMessageContaining("already started");
    }

    @Test
    void managerCanMarkFalseAlarmButReasonIsRequired() {
        var item = incident("ACKNOWLEDGED", "MEDIUM");
        stub(item);

        assertThatThrownBy(() -> action("FALSE_ALARM", null))
                .hasMessageContaining("False alarm reason");
        assertThat(action("FALSE_ALARM", "Alarm sensor malfunction"))
                .containsEntry("status", "FALSE_ALARM");
    }

    @Test
    void adminCanMaintainDirectoryButStaffCannot() {
        var result =
                service.saveDirectory(
                        societyId,
                        staffId,
                        "SOCIETY_ADMIN",
                        null,
                        "AMBULANCE",
                        "City Ambulance",
                        "1080000000",
                        null,
                        "Central station",
                        true,
                        1);
        assertThat(result)
                .containsEntry("contact_type", "AMBULANCE")
                .containsEntry("available_24x7", true);

        assertThatThrownBy(
                        () ->
                                service.saveDirectory(
                                        societyId,
                                        staffId,
                                        "STAFF",
                                        null,
                                        "POLICE",
                                        "Police",
                                        "1000000000",
                                        null,
                                        null,
                                        true,
                                        1))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void assignmentRequiresEligibleSameSocietyResponder() {
        var item = incident("REPORTED", "HIGH");
        stub(item);
        when(users.findById(staffId)).thenReturn(Optional.of(staff()));

        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        incidentId,
                                        "ASSIGN",
                                        null,
                                        staffId,
                                        null,
                                        null,
                                        null))
                .hasMessageContaining("Eligible emergency responder");

        allowStaff();
        assertThat(
                        service.action(
                                societyId,
                                staffId,
                                "SOCIETY_ADMIN",
                                incidentId,
                                "ASSIGN",
                                null,
                                staffId,
                                null,
                                null,
                                null))
                .containsEntry("status", "ACKNOWLEDGED");
        assertThat(item.getAssignedStaffId()).isEqualTo(staffId);
    }

    @Test
    void eventHistoryIsTenantAndResidentScoped() {
        var item = incident("RESPONDING", "CRITICAL");
        stub(item);
        var event = new EmergencyIncidentEvent();
        event.setEventType("START_RESPONSE");
        event.setActorId(staffId);
        event.setActorRole("STAFF");
        event.setOccurredAt(OffsetDateTime.now());
        when(events.findByIncidentIdOrderByOccurredAt(incidentId)).thenReturn(List.of(event));

        assertThat(service.history(societyId, residentId, "RESIDENT", incidentId))
                .singleElement()
                .satisfies(row -> assertThat(row).containsEntry("event_type", "START_RESPONSE"));
        assertThatThrownBy(
                        () -> service.history(societyId, UUID.randomUUID(), "RESIDENT", incidentId))
                .isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(
                        () ->
                                service.history(
                                        UUID.randomUUID(), staffId, "SOCIETY_ADMIN", incidentId))
                .hasMessageContaining("not found");
    }

    private Map<String, Object> createAsResident(
            String severity, BigDecimal latitude, BigDecimal longitude) {
        return service.create(
                societyId,
                residentId,
                "RESIDENT",
                null,
                flatId,
                "MEDICAL",
                severity,
                "Resident collapsed",
                "Resident is dizzy and requires assistance",
                "Tower A lobby",
                latitude,
                longitude,
                1,
                "Resident",
                "9000000000",
                "Conscious but weak");
    }

    private Map<String, Object> action(String action, String note) {
        return service.action(
                societyId,
                staffId,
                "SOCIETY_ADMIN",
                incidentId,
                action,
                note,
                null,
                null,
                null,
                null);
    }

    private void allowStaff() {
        when(permissions.existsByStaffUserIdAndPermissionCode(staffId, "EMERGENCY_INCIDENTS"))
                .thenReturn(true);
    }

    private void stub(EmergencyIncident item) {
        when(incidents.findByIdAndSocietyId(incidentId, societyId)).thenReturn(Optional.of(item));
    }

    private EmergencyIncident incident(String status, String severity) {
        var item = new EmergencyIncident();
        item.setSocietyId(societyId);
        item.setIncidentNumber("EMG-TEST-1");
        item.setReporterId(residentId);
        item.setReporterRole("RESIDENT");
        item.setResidentId(residentId);
        item.setFlatId(flatId);
        item.setIncidentType("MEDICAL");
        item.setSeverity(severity);
        item.setTitle("Medical emergency");
        item.setDescription("Resident needs assistance");
        item.setLocationDetails("Tower A lobby");
        item.setPeopleAffected(1);
        item.setStatus(status);
        item.setCreatedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        return item;
    }

    private UserAccount resident() {
        var user = new UserAccount();
        user.setSocietyId(societyId);
        user.setFullName("Resident");
        user.setEmail("resident@example.com");
        user.setMobile("9000000000");
        user.setRole("RESIDENT");
        user.setStatus("ACTIVE");
        return user;
    }

    private UserAccount staff() {
        var user = new UserAccount();
        user.setSocietyId(societyId);
        user.setFullName("Security Staff");
        user.setEmail("staff@example.com");
        user.setMobile("9000000001");
        user.setRole("STAFF");
        user.setStatus("ACTIVE");
        return user;
    }

    private static final class EmergencyTestFacade {
        private final EmergencyIncidentService delegate;

        private EmergencyTestFacade(EmergencyIncidentService delegate) {
            this.delegate = delegate;
        }

        Map<String, Object> workspace(UUID societyId, UUID userId, String role) {
            var value = delegate.workspace(actor(societyId, userId, role));
            return Map.of(
                    "total", value.total(),
                    "open", value.open(),
                    "critical", value.critical(),
                    "responding", value.responding(),
                    "resolved", value.resolved());
        }

        List<Map<String, Object>> history(
                UUID societyId, UUID userId, String role, UUID incidentId) {
            return delegate.history(actor(societyId, userId, role), incidentId).stream()
                    .map(item -> Map.<String, Object>of("event_type", item.eventType().code()))
                    .toList();
        }

        Map<String, Object> create(
                UUID societyId,
                UUID userId,
                String role,
                UUID residentId,
                UUID flatId,
                String incidentType,
                String severity,
                String title,
                String description,
                String location,
                BigDecimal latitude,
                BigDecimal longitude,
                int peopleAffected,
                String injuredName,
                String injuredMobile,
                String injuryDetails) {
            return incident(
                    delegate.create(
                            actor(societyId, userId, role),
                            new EmergencyModels.CreateCommand(
                                    residentId,
                                    flatId,
                                    EmergencyIncidentType.from(incidentType),
                                    IncidentSeverity.from(severity),
                                    title,
                                    description,
                                    location,
                                    latitude,
                                    longitude,
                                    peopleAffected,
                                    injuredName,
                                    injuredMobile,
                                    injuryDetails)));
        }

        Map<String, Object> action(
                UUID societyId,
                UUID userId,
                String role,
                UUID incidentId,
                String action,
                String note,
                UUID assignedStaffId,
                String hospitalName,
                String externalReference,
                String severity) {
            return incident(
                    delegate.action(
                            actor(societyId, userId, role),
                            incidentId,
                            new EmergencyModels.ActionCommand(
                                    EmergencyAction.from(action),
                                    note,
                                    assignedStaffId,
                                    hospitalName,
                                    externalReference,
                                    severity == null ? null : IncidentSeverity.from(severity))));
        }

        Map<String, Object> saveDirectory(
                UUID societyId,
                UUID userId,
                String role,
                UUID id,
                String type,
                String name,
                String phone,
                String alternatePhone,
                String address,
                boolean available24x7,
                int priority) {
            var value =
                    delegate.saveDirectory(
                            actor(societyId, userId, role),
                            id,
                            new EmergencyModels.DirectoryCommand(
                                    EmergencyContactType.from(type),
                                    name,
                                    phone,
                                    alternatePhone,
                                    address,
                                    available24x7,
                                    priority));
            return Map.of(
                    "contact_type", value.contactType().code(),
                    "available_24x7", value.available24x7());
        }

        private EmergencyModels.Actor actor(UUID societyId, UUID userId, String role) {
            return new EmergencyModels.Actor(societyId, userId, UserRole.from(role));
        }

        private Map<String, Object> incident(EmergencyModels.IncidentView value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("status", value.status().code());
            result.put("severity", value.severity().code());
            result.put("incident_type", value.incidentType().code());
            result.put("people_affected", value.peopleAffected());
            result.put("resident_id", value.residentId());
            result.put("flat_id", value.spaceId());
            result.put("ambulance_requested", value.ambulanceRequested());
            result.put("hospital_name", value.hospitalName());
            result.put("external_reference", value.externalReference());
            return result;
        }
    }
}
