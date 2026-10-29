package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.*;
import java.util.*;

public interface NoticeReadRepository extends CrudRepositoryPort<NoticeRead, NoticeReadId> {
    Optional<NoticeRead> findByNoticeIdAndUserId(UUID noticeId, UUID userId);

    long countByNoticeId(UUID noticeId);

    long countByNoticeIdAndAcknowledgedAtIsNotNull(UUID noticeId);
}
