package com.societyapp.controller.mapper;

import com.societyapp.application.model.SocietyLogoUpdateView;
import com.societyapp.application.model.SocietyProfileView;
import com.societyapp.controller.model.SocietyLogoUpdateResponse;
import com.societyapp.controller.model.SocietyProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class SocietyProfileHttpMapper {
    public SocietyProfileResponse toResponse(
            SocietyProfileView view, Boolean accessAllowed, String accessReason) {
        return new SocietyProfileResponse(
                view.id(),
                view.name(),
                view.registrationNumber(),
                view.societyCode(),
                view.societyType(),
                view.totalBlocks(),
                view.totalFlats(),
                view.totalFlats(),
                view.plannedStaff(),
                view.approximateResidents(),
                view.address(),
                view.city(),
                view.state(),
                view.pinCode(),
                view.contactPerson(),
                view.email(),
                view.mobile(),
                view.planCode(),
                view.enabledModules(),
                view.status().code(),
                view.description(),
                view.website(),
                view.logoUrl(),
                view.updatedAt(),
                accessAllowed,
                accessReason);
    }

    public SocietyLogoUpdateResponse toResponse(SocietyLogoUpdateView view) {
        return new SocietyLogoUpdateResponse(view.societyId(), view.logoUrl(), view.updated());
    }
}
