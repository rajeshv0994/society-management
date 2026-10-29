package com.societyapp.domain.type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DomainCodeTest {
    @Test
    void parsesBoundaryValuesOnceAndNormalizesCaseAndWhitespace() {
        assertThat(UserRole.from(" resident ")).isEqualTo(UserRole.RESIDENT);
        assertThat(PaymentMethod.from("bank_transfer")).isEqualTo(PaymentMethod.BANK_TRANSFER);
        assertThat(AmenityBookingAction.from("check_in")).isEqualTo(AmenityBookingAction.CHECK_IN);
    }

    @Test
    void rejectsMissingAndUnknownBusinessCodes() {
        assertThatThrownBy(() -> UserRole.from(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user role is required");
        assertThatThrownBy(() -> IncidentSeverity.from("extreme"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid incident severity: extreme");
    }
}
