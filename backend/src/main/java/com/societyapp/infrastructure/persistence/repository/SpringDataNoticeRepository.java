package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Notice;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNoticeRepository
        extends JpaRepository<Notice, UUID>,
                com.societyapp.application.port.out.persistence.NoticeRepository {
    List<Notice> findBySocietyIdOrderByPublishedAtDesc(UUID societyId);
}
