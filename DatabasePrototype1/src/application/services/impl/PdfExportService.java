package application.services.impl;

import application.models.Invoice;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;


import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public void export(List<Invoice> invoices, File outputFile) throws IOException {
        PdfWriter writer = new PdfWriter(outputFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("THE GRAND HOTEL").setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Invoice Report").setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Generated on: " + java.time.LocalDate.now().format(DATE_FORMATTER))
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

        float[] columnWidths = {100, 150, 80, 80, 50, 80, 80, 80, 100};
        Table table = new Table(columnWidths);
        String[] headers = {"Invoice No", "Guest Name", "Date", "Subtotal", "Tax %", "Tax Amount", "Total", "Status", "Payment Method"};

        for (String h: headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        for (Invoice invoice : invoices) {
            table.addCell(new Cell().add(new Paragraph(invoice.getInvoiceNumber())));
            table.addCell(new Cell().add(new Paragraph(invoice.getGuestName())));
            table.addCell(new Cell().add(new Paragraph(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FORMATTER) : ""))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(String.format("₹%.2f", invoice.getSubtotal().doubleValue())))
                    .setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", invoice.getTaxPercentage())))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(String.format("₹%.2f", invoice.getGst().doubleValue())))
                    .setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(String.format("₹%.2f", invoice.getTotal().doubleValue())))
                    .setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(invoice.getStatus())).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(invoice.getPaymentMethod())).setTextAlignment(TextAlignment.CENTER));
        }

        document.add(table);
        document.close();
    }
}
