package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.societyapp.application.model.billing.BillingModels;
import com.societyapp.domain.type.*;
import com.societyapp.infrastructure.documents.ReceiptPdfRenderer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReceiptPdfRendererTest {
    @Test
    void rendersAValidPaymentReceiptPdf() {
        var space =
                new BillingModels.Space(
                        UUID.randomUUID(),
                        "201",
                        UUID.randomUUID(),
                        "Resident One",
                        null,
                        "A",
                        "2");
        var payment =
                new BillingModels.PaymentView(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "INV-2026-0001",
                        space,
                        new BigDecimal("1250.00"),
                        PaymentMethod.NET_BANKING,
                        PaymentChannel.ONLINE,
                        "PAY-101",
                        "RCP-2026-0001",
                        null,
                        PaymentStatus.SUCCESS,
                        OffsetDateTime.parse("2026-07-28T10:00:00+05:30"),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Resident One",
                        "Resident One");
        var receipt =
                new BillingModels.Receipt(
                        payment,
                        "Green Valley Society",
                        "REG-101",
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 10),
                        new BigDecimal("1250.00"),
                        BigDecimal.ZERO,
                        null,
                        new BigDecimal("1250.00"),
                        new BigDecimal("1250.00"),
                        PaymentStatus.PAID);

        var pdf = new ReceiptPdfRenderer().render(receipt);

        assertThat(pdf).hasSizeGreaterThan(1_000);
        assertThat(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII))
                .isEqualTo("%PDF");
    }
}
