package com.societyapp.application.port.in;

import com.societyapp.application.model.notice.NoticeModels;
import com.societyapp.domain.type.UserRole;
import java.util.List;
import java.util.UUID;

public interface NoticeUseCase {
    List<NoticeModels.View> list(UUID societyId, UUID userId, UserRole role);

    NoticeModels.Options options(UUID userId, UserRole role);

    NoticeModels.Status create(
            UUID societyId, UUID publisherId, UserRole role, NoticeModels.SaveCommand command);

    NoticeModels.Status update(
            UUID societyId,
            UUID actorId,
            UserRole role,
            UUID noticeId,
            NoticeModels.SaveCommand command);

    NoticeModels.Read markRead(UUID societyId, UUID userId, UserRole role, UUID noticeId);

    NoticeModels.Acknowledgement acknowledge(
            UUID societyId, UUID userId, UserRole role, UUID noticeId);
}
