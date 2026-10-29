package com.societyapp.application.port.in;

import com.societyapp.application.model.audit.AuditModels;

public interface AuditUseCase {
    void record(AuditModels.RecordCommand command);

    AuditModels.Page search(AuditModels.SearchCriteria criteria);
}
