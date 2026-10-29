package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Notice;
import java.util.*;

public interface NoticeRepository extends CrudRepositoryPort<Notice, UUID> {
    List<Notice> findBySocietyIdOrderByPublishedAtDesc(UUID societyId);
}
