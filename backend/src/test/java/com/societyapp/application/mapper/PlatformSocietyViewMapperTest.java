package com.societyapp.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.domain.model.SubscriptionOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlatformSocietyViewMapperTest {
    @ParameterizedTest
    @CsvSource({"MOCK,MOCK", "RAZORPAY,RAZORPAY_UPI", "BANK_TRANSFER,BANK_TRANSFER"})
    void preservesRecordedPaymentCodesInSocietyHistory(String provider, String method) {
        var order = new SubscriptionOrder();
        order.setProvider(provider);
        order.setPaymentMethod(method);
        order.setStatus("PAID");
        var mapped = new PlatformSocietyViewMapper(mock(JsonCodec.class)).order(order);
        assertThat(mapped.provider()).isEqualTo(provider);
        assertThat(mapped.paymentMethod()).isEqualTo(method);
    }
}
