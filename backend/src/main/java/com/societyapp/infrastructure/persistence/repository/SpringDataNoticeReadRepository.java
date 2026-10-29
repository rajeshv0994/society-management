package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNoticeReadRepository
        extends JpaRepository<NoticeRead, NoticeReadId>,
                com.societyapp.application.port.out.persistence.NoticeReadRepository {
    Optional<NoticeRead> findByNoticeIdAndUserId(UUID noticeId, UUID userId);

    long countByNoticeId(UUID noticeId);

    long countByNoticeIdAndAcknowledgedAtIsNotNull(UUID noticeId);
}
