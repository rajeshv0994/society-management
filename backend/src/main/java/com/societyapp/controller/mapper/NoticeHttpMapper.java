package com.societyapp.controller.mapper;

import com.societyapp.application.model.notice.NoticeModels;
import com.societyapp.controller.model.NoticeResponse;
import org.springframework.stereotype.Component;

@Component
public class NoticeHttpMapper {
    public NoticeResponse.Notice toResponse(NoticeModels.View v) {
        return new NoticeResponse.Notice(
                v.id(),
                v.title(),
                v.body(),
                v.audience().code(),
                v.priority().code(),
                v.pinned(),
                v.requiresAcknowledgement(),
                v.status().code(),
                v.publishedAt(),
                v.expiresAt(),
                v.updatedAt(),
                v.expired(),
                v.read(),
                v.readAt(),
                v.acknowledged(),
                v.acknowledgedAt(),
                v.publishedBy(),
                v.publishedByName(),
                v.readCount(),
                v.acknowledgementCount());
    }

    public NoticeResponse.Options toResponse(NoticeModels.Options v) {
        return new NoticeResponse.Options(
                v.canManage(),
                v.audiences().stream().map(x -> x.code()).toList(),
                v.priorities().stream().map(x -> x.code()).toList(),
                v.statuses().stream().map(x -> x.code()).toList());
    }

    public NoticeResponse.Status toResponse(NoticeModels.Status v) {
        return new NoticeResponse.Status(v.id(), v.status().code());
    }

    public NoticeResponse.Read toResponse(NoticeModels.Read v) {
        return new NoticeResponse.Read(v.id(), v.read());
    }

    public NoticeResponse.Acknowledgement toResponse(NoticeModels.Acknowledgement v) {
        return new NoticeResponse.Acknowledgement(v.id(), v.acknowledged());
    }
}
