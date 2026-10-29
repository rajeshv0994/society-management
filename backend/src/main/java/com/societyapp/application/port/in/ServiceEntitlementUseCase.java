package com.societyapp.application.port.in;

import com.societyapp.domain.type.BuiltInServiceCode;
import java.util.UUID;

/** Checks whether a society can access a product feature. */
public interface ServiceEntitlementUseCase {
    boolean isEnabled(UUID societyId, BuiltInServiceCode serviceCode);
}
