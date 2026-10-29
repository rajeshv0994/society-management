package com.societyapp.application.model.document;

import com.societyapp.domain.type.VerificationStatus;
import java.util.UUID;

public record DocumentVerificationView(UUID documentId, VerificationStatus status) {}
