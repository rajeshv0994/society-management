package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.domain.model.Society;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ServiceEntitlementServiceTest {
    private final SocietyRepository societies = mock(SocietyRepository.class);
    private final ServiceEntitlementService useCase = new ServiceEntitlementService(societies);

    @Test
    void reportsAnEnabledServiceFromTheSocietyConfiguration() {
        var societyId = UUID.randomUUID();
        var society = new Society();
        society.setEnabledModules("[\"RESIDENTS\",\"PAYMENTS\"]");
        when(societies.findById(societyId)).thenReturn(Optional.of(society));

        assertThat(
                        useCase.isEnabled(
                                societyId, com.societyapp.domain.type.BuiltInServiceCode.PAYMENTS))
                .isTrue();
        assertThat(
                        useCase.isEnabled(
                                societyId, com.societyapp.domain.type.BuiltInServiceCode.AMENITIES))
                .isFalse();
    }

    @Test
    void deniesAccessWhenTheSocietyDoesNotExist() {
        var societyId = UUID.randomUUID();
        when(societies.findById(societyId)).thenReturn(Optional.empty());

        assertThat(
                        useCase.isEnabled(
                                societyId, com.societyapp.domain.type.BuiltInServiceCode.PAYMENTS))
                .isFalse();
    }
}
