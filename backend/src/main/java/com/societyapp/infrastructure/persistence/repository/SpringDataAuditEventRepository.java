package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.AuditEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataAuditEventRepository
        extends JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent> {
    List<AuditEvent> findBySocietyIdOrderByOccurredAtDesc(UUID societyId);
}
