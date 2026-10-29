package com.societyapp.infrastructure.documents;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.societyapp.application.model.billing.BillingModels;
import com.societyapp.application.port.out.ReceiptRenderer;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Component;

@Component
public final class ReceiptPdfRenderer implements ReceiptRenderer {
    @Override
    public byte[] render(BillingModels.Receipt receipt) {
        var output = new ByteArrayOutputStream();
        var document = new Document(PageSize.A4, 44, 44, 44, 44);
        try {
            PdfWriter.getInstance(document, output);
            document.open();

            var brand = new Paragraph("SocietyHub", new Font(Font.HELVETICA, 22, Font.BOLD));
            brand.setAlignment(Element.ALIGN_RIGHT);
            document.add(brand);

            var title =
                    new Paragraph(
                            "PAYMENT RECEIPT",
                            new Font(Font.HELVETICA, 17, Font.BOLD, new Color(30, 64, 175)));
            title.setSpacingBefore(16);
            title.setSpacingAfter(8);
            document.add(title);
            document.add(
                    new Paragraph(
                            receipt.societyName() + registration(receipt.societyRegistration()),
                            new Font(Font.HELVETICA, 11)));

            var amount =
                    new Paragraph(
                            "PAYMENT RECEIVED: INR " + receipt.payment().amount(),
                            new Font(Font.HELVETICA, 15, Font.BOLD, new Color(35, 135, 88)));
            amount.setSpacingBefore(18);
            amount.setSpacingAfter(14);
            document.add(amount);

            var facts = new PdfPTable(new float[] {2, 3});
            facts.setWidthPercentage(100);
            var payment = receipt.payment();
            row(facts, "Receipt number", payment.receiptNumber());
            row(facts, "Invoice", payment.invoiceNumber());
            row(facts, "Resident", payment.payerName());
            row(facts, "Space", space(payment.space()));
            row(facts, "Billing month", receipt.billingMonth());
            row(facts, "Payment method", label(payment.paymentMethod()));
            row(facts, "Channel", label(payment.paymentChannel()));
            row(facts, "Transaction reference", payment.providerReference());
            row(facts, "Paid on", payment.paidAt());
            row(facts, "Recorded by", payment.recordedByName());
            if (payment.note() != null) row(facts, "Note", payment.note());
            document.add(facts);

            var footer =
                    new Paragraph(
                            "This is a system-generated payment receipt.",
                            new Font(Font.HELVETICA, 9, Font.ITALIC, Color.DARK_GRAY));
            footer.setSpacingBefore(24);
            document.add(footer);
        } catch (DocumentException exception) {
            throw new IllegalStateException("Unable to generate payment receipt", exception);
        } finally {
            document.close();
        }
        return output.toByteArray();
    }

    private static void row(PdfPTable table, String label, Object value) {
        table.addCell(cell(label, true));
        table.addCell(cell(value == null ? "-" : String.valueOf(value), false));
    }

    private static PdfPCell cell(String value, boolean label) {
        var cell =
                new PdfPCell(
                        new Phrase(
                                value,
                                new Font(Font.HELVETICA, 10, label ? Font.BOLD : Font.NORMAL)));
        cell.setPadding(9);
        cell.setBorderColor(new Color(220, 225, 235));
        if (label) cell.setBackgroundColor(new Color(244, 246, 252));
        return cell;
    }

    private static String registration(Object value) {
        return value == null ? "" : "\nRegistration: " + value;
    }

    private static String space(BillingModels.Space space) {
        if (space == null) return "-";
        return java.util.stream.Stream.of(space.block(), space.floorNumber(), space.spaceNumber())
                .filter(java.util.Objects::nonNull)
                .map(String::valueOf)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" / "));
    }

    private static String label(Object value) {
        if (value == null) return "-";
        var normalized = String.valueOf(value).toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return java.util.Arrays.stream(normalized.split(" "))
                .filter(word -> !word.isBlank())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
