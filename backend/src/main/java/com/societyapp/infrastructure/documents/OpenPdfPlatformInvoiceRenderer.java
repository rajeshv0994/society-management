package com.societyapp.infrastructure.documents;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.application.port.out.PlatformInvoiceRenderer;
import com.societyapp.domain.model.Society;
import com.societyapp.domain.model.SubscriptionOrder;
import com.societyapp.domain.type.BillingCycle;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class OpenPdfPlatformInvoiceRenderer implements PlatformInvoiceRenderer {
    @Override
    public byte[] render(
            SubscriptionOrder order,
            Society society,
            java.util.List<PlatformBillingModels.PricingLine> pricingBreakdown) {
        var output = new ByteArrayOutputStream();
        var document = new Document(PageSize.A4, 44, 44, 44, 44);
        try {
            PdfWriter.getInstance(document, output);
            document.open();
            var title =
                    new Paragraph(
                            "SocietyHub",
                            new Font(Font.HELVETICA, 22, Font.BOLD, new Color(30, 64, 175)));
            title.setAlignment(Element.ALIGN_RIGHT);
            document.add(title);
            var invoiceTitle =
                    new Paragraph(
                            "PLATFORM MEMBERSHIP INVOICE", new Font(Font.HELVETICA, 16, Font.BOLD));
            invoiceTitle.setSpacingBefore(18);
            invoiceTitle.setSpacingAfter(16);
            document.add(invoiceTitle);

            var parties = new PdfPTable(new float[] {1, 1});
            parties.setWidthPercentage(100);
            parties.addCell(cell("FROM\nSocietyHub Platform\nPlatform membership services"));
            parties.addCell(
                    cell(
                            "BILL TO\n"
                                    + society.getName()
                                    + "\n"
                                    + safe(society.getAddress())
                                    + "\n"
                                    + safe(society.getCity())
                                    + ", "
                                    + safe(society.getState())));
            document.add(parties);

            var facts = new PdfPTable(new float[] {1, 1, 1, 1});
            facts.setWidthPercentage(100);
            facts.setSpacingBefore(16);
            facts.addCell(cell("Invoice number\n" + order.getInvoiceNumber()));
            facts.addCell(cell("Invoice date\n" + order.getCreatedAt().toLocalDate()));
            facts.addCell(cell("Due date\n" + order.getDueDate()));
            facts.addCell(cell("Status\n" + order.getStatus()));
            document.add(facts);

            var lines = new PdfPTable(new float[] {4, 2, 2});
            lines.setWidthPercentage(100);
            lines.setSpacingBefore(20);
            lines.addCell(header("Description"));
            lines.addCell(header("Coverage"));
            lines.addCell(header("Amount"));
            var billable =
                    pricingBreakdown.stream()
                            .filter(
                                    line ->
                                            line.monthlyAmount() != null
                                                    && line.monthlyAmount().signum() > 0)
                            .toList();
            if (billable.isEmpty()) {
                lines.addCell(
                        cell(
                                "SocietyHub "
                                        + order.getPlanCode()
                                        + " platform membership\n"
                                        + label(order.getBillingCycle())
                                        + " billing"));
                lines.addCell(cell(order.getPeriodStart() + "\nto\n" + order.getPeriodEnd()));
                lines.addCell(cell("INR " + order.getAmount().setScale(2)));
            } else {
                for (var line : billable) {
                    var monthly = line.monthlyAmount();
                    var amount = monthly.multiply(BigDecimal.valueOf(cycleMonths(order)));
                    lines.addCell(
                            cell(
                                    line.name()
                                            + "\n"
                                            + (line.quantity() == null
                                                    ? line.spaces()
                                                    : line.quantity())
                                            + " × INR "
                                            + (line.rate() == null
                                                    ? line.pricePerSpace()
                                                    : line.rate())));
                    lines.addCell(cell(order.getPeriodStart() + "\nto\n" + order.getPeriodEnd()));
                    lines.addCell(cell("INR " + amount.setScale(2)));
                }
            }
            document.add(lines);

            var total =
                    new Paragraph(
                            "TOTAL: INR " + order.getAmount().setScale(2),
                            new Font(Font.HELVETICA, 14, Font.BOLD));
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(14);
            document.add(total);
            if (order.getProviderReference() != null) {
                var payment =
                        new Paragraph(
                                "Payment method: "
                                        + label(order.getPaymentMethod())
                                        + "\nPayment reference: "
                                        + order.getProviderReference()
                                        + "\nPaid at: "
                                        + (order.getPaidAt() == null
                                                ? "Pending verification"
                                                : order.getPaidAt()),
                                new Font(Font.HELVETICA, 10));
                payment.setSpacingBefore(22);
                document.add(payment);
            }
            var footer =
                    new Paragraph(
                            "This invoice was generated electronically by SocietyHub.",
                            new Font(Font.HELVETICA, 9, Font.ITALIC, Color.DARK_GRAY));
            footer.setSpacingBefore(28);
            document.add(footer);
        } catch (DocumentException exception) {
            throw new IllegalStateException("Unable to generate subscription invoice", exception);
        } finally {
            document.close();
        }
        return output.toByteArray();
    }

    private PdfPCell cell(String value) {
        var cell = new PdfPCell(new Phrase(value, new Font(Font.HELVETICA, 10)));
        cell.setPadding(10);
        cell.setBorderColor(new Color(220, 225, 235));
        return cell;
    }

    private PdfPCell header(String value) {
        var cell =
                new PdfPCell(
                        new Phrase(value, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
        cell.setPadding(9);
        cell.setBackgroundColor(new Color(30, 64, 175));
        return cell;
    }

    private int cycleMonths(SubscriptionOrder order) {
        return BillingCycle.YEARLY.matches(order.getBillingCycle()) ? 12 : 1;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String label(String value) {
        if (value == null) return "-";
        return Arrays.stream(value.toLowerCase(Locale.ROOT).replace('_', ' ').split(" "))
                .filter(word -> !word.isBlank())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
