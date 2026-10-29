package com.societyapp.application.port.in;

import com.societyapp.application.model.SocietyLogoUpdateView;
import com.societyapp.application.model.SocietyProfileUpdate;
import com.societyapp.application.model.SocietyProfileView;
import com.societyapp.application.port.out.UploadedFile;
import java.util.UUID;

public interface SocietyProfileUseCase {
    record LogoContent(
            com.societyapp.application.model.BinaryContent content, String contentType) {}

    SocietyProfileView get(UUID societyId);

    SocietyProfileView update(UUID societyId, SocietyProfileUpdate update);

    SocietyLogoUpdateView uploadLogo(UUID societyId, UploadedFile file);

    LogoContent getLogo(UUID societyId);
}
