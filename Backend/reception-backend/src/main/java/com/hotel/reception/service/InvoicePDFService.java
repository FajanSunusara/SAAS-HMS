package com.hotel.reception.service;

import com.hotel.reception.model.dto.response.InvoiceResponse;
import com.hotel.reception.model.dto.response.BookingResponse;
import com.hotel.reception.model.dto.response.GuestResponse;
import com.hotel.reception.model.dto.response.PaymentResponse;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class InvoicePDFService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, new BaseColor(26, 86, 219));
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
    private static final Font SECTION_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, new BaseColor(55, 65, 81));
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK);
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY);
    private static final Font TABLE_HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.WHITE);
    private static final Font TOTAL_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(220, 38, 38));

    private static final BaseColor PRIMARY_COLOR = new BaseColor(26, 86, 219);
    private static final BaseColor LIGHT_GRAY = new BaseColor(249, 250, 251);
    private static final BaseColor BORDER_COLOR = new BaseColor(229, 231, 235);
    private static final BaseColor SUCCESS_COLOR = new BaseColor(22, 163, 74);
    private static final BaseColor WARNING_COLOR = new BaseColor(234, 179, 8);

    public byte[] generateInvoicePDF(InvoiceResponse invoice) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Add modern styled content
            addModernHeader(document, invoice);
            addCompactBillingInfo(document, invoice);
            addCompactStayDetails(document, invoice);
            addModernItemsTable(document, invoice);
            addModernTotals(document, invoice);
            addCompactPaymentStatus(document, invoice);
            addModernFooter(document);
            
            document.close();
            
            log.info("Enhanced PDF generated successfully for invoice: {}", invoice.getInvoiceNumber());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating PDF for invoice: {}", invoice.getInvoiceNumber(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addModernHeader(Document document, InvoiceResponse invoice) throws DocumentException {
        // Header with colored background
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1.2f, 1f});
        headerTable.setSpacingAfter(12);
        
        // Left side - Hotel info with colored box
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingBottom(8);
        
        Paragraph hotelName = new Paragraph("HOTEL GRAND PLAZA", TITLE_FONT);
        leftCell.addElement(hotelName);
        
        Paragraph tagline = new Paragraph("Luxury & Comfort Redefined", SUBTITLE_FONT);
        tagline.setSpacingBefore(2);
        leftCell.addElement(tagline);
        
        Chunk spacer = new Chunk("\n");
        leftCell.addElement(new Paragraph(spacer));
        
        Paragraph hotelAddress = new Paragraph("123 Luxury Street, City, State 12345", new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY));
        leftCell.addElement(hotelAddress);
        
        Paragraph hotelContact = new Paragraph("☎ +1 (555) 123-4567  |  ✉ accounts@hotelgrandplaza.com", SMALL_FONT);
        hotelContact.setSpacingBefore(2);
        leftCell.addElement(hotelContact);
        
        headerTable.addCell(leftCell);
        
        // Right side - Invoice details in colored box
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBackgroundColor(LIGHT_GRAY);
        rightCell.setBorder(Rectangle.BOX);
        rightCell.setBorderColor(BORDER_COLOR);
        rightCell.setPadding(10);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        Paragraph invoiceTitle = new Paragraph("INVOICE", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, PRIMARY_COLOR));
        invoiceTitle.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(invoiceTitle);
        
        rightCell.addElement(new Paragraph(" ", SMALL_FONT));
        
        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setWidths(new float[]{1, 1.2f});
        
        addDetailRow(detailsTable, "Invoice #:", invoice.getInvoiceNumber(), true);
        addDetailRow(detailsTable, "Issue Date:", invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : "N/A", false);
        if (invoice.getDueDate() != null) {
            addDetailRow(detailsTable, "Due Date:", invoice.getDueDate().toString(), false);
        }
        
        rightCell.addElement(detailsTable);
        headerTable.addCell(rightCell);
        
        document.add(headerTable);
    }

    private void addDetailRow(PdfPTable table, String label, String value, boolean bold) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, SMALL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setPaddingBottom(3);
        table.addCell(labelCell);
        
        Font valueFont = bold ? new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK) : BOLD_FONT;
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingBottom(3);
        table.addCell(valueCell);
    }

    private void addCompactBillingInfo(Document document, InvoiceResponse invoice) throws DocumentException {
        PdfPTable billingTable = new PdfPTable(2);
        billingTable.setWidthPercentage(100);
        billingTable.setWidths(new float[]{1, 1});
        billingTable.setSpacingAfter(10);
        
        // Billed From
        PdfPCell fromCell = new PdfPCell();
        fromCell.setBackgroundColor(LIGHT_GRAY);
        fromCell.setBorder(Rectangle.BOX);
        fromCell.setBorderColor(BORDER_COLOR);
        fromCell.setPadding(8);
        
        Paragraph fromTitle = new Paragraph("BILLED FROM", SECTION_FONT);
        fromTitle.setSpacingAfter(4);
        fromCell.addElement(fromTitle);
        
        Paragraph hotelName = new Paragraph("Hotel Grand Plaza", BOLD_FONT);
        fromCell.addElement(hotelName);
        
        Paragraph hotelDetails = new Paragraph("123 Luxury Street\nCity, State 12345\nVAT: VAT123456789", SMALL_FONT);
        hotelDetails.setSpacingBefore(2);
        fromCell.addElement(hotelDetails);
        
        billingTable.addCell(fromCell);
        
        // Billed To
        PdfPCell toCell = new PdfPCell();
        toCell.setBackgroundColor(LIGHT_GRAY);
        toCell.setBorder(Rectangle.BOX);
        toCell.setBorderColor(BORDER_COLOR);
        toCell.setPadding(8);
        
        Paragraph toTitle = new Paragraph("BILLED TO", SECTION_FONT);
        toTitle.setSpacingAfter(4);
        toCell.addElement(toTitle);
        
        GuestResponse guest = invoice.getGuest();
        if (guest != null) {
            String guestName = guest.getFirstName() + " " + guest.getLastName();
            Paragraph guestNamePara = new Paragraph(guestName, BOLD_FONT);
            toCell.addElement(guestNamePara);
            
            StringBuilder guestInfo = new StringBuilder();
            if (guest.getCompany() != null && !guest.getCompany().isEmpty()) {
                guestInfo.append(guest.getCompany()).append("\n");
            }
            if (guest.getEmail() != null) {
                guestInfo.append("✉ ").append(guest.getEmail()).append("\n");
            }
            if (guest.getPhone() != null) {
                guestInfo.append("☎ ").append(guest.getPhone());
            }
            
            Paragraph guestDetails = new Paragraph(guestInfo.toString(), SMALL_FONT);
            guestDetails.setSpacingBefore(2);
            toCell.addElement(guestDetails);
        }
        
        billingTable.addCell(toCell);
        document.add(billingTable);
    }

    private void addCompactStayDetails(Document document, InvoiceResponse invoice) throws DocumentException {
        BookingResponse booking = invoice.getBooking();
        if (booking == null) return;
        
        PdfPTable stayTable = new PdfPTable(4);
        stayTable.setWidthPercentage(100);
        stayTable.setSpacingAfter(10);
        
        PdfPCell titleCell = new PdfPCell(new Phrase("STAY DETAILS", SECTION_FONT));
        titleCell.setColspan(4);
        titleCell.setBackgroundColor(new BaseColor(243, 244, 246));
        titleCell.setBorder(Rectangle.BOX);
        titleCell.setBorderColor(BORDER_COLOR);
        titleCell.setPadding(6);
        stayTable.addCell(titleCell);
        
        addStayCell(stayTable, "Check-in", booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "N/A");
        addStayCell(stayTable, "Check-out", booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : "N/A");
        addStayCell(stayTable, "Nights", String.valueOf(booking.getNights()));
        addStayCell(stayTable, "Status", booking.getStatus());
        
        document.add(stayTable);
    }

    private void addStayCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(6);
        cell.setBackgroundColor(BaseColor.WHITE);
        
        Paragraph labelPara = new Paragraph(label, SMALL_FONT);
        cell.addElement(labelPara);
        
        Paragraph valuePara = new Paragraph(value, BOLD_FONT);
        valuePara.setSpacingBefore(2);
        cell.addElement(valuePara);
        
        table.addCell(cell);
    }

    private void addModernItemsTable(Document document, InvoiceResponse invoice) throws DocumentException {
        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{3f, 1f, 1.2f, 1.2f});
        itemsTable.setSpacingAfter(10);
        
        // Modern header with gradient-like effect
        addModernTableHeader(itemsTable, "DESCRIPTION");
        addModernTableHeader(itemsTable, "QTY");
        addModernTableHeader(itemsTable, "RATE");
        addModernTableHeader(itemsTable, "AMOUNT");
        
        // Add charges with alternating row colors
        boolean alternate = false;
        if (invoice.getRoomCharges() != null && invoice.getRoomCharges().doubleValue() > 0) {
            addModernItemRow(itemsTable, "Room Charges", 
                          invoice.getBooking() != null ? String.valueOf(invoice.getBooking().getNights()) : "1",
                          formatCurrency(invoice.getRoomCharges().divide(invoice.getBooking() != null ? 
                              java.math.BigDecimal.valueOf(invoice.getBooking().getNights()) : java.math.BigDecimal.ONE)),
                          formatCurrency(invoice.getRoomCharges()), alternate);
            alternate = !alternate;
        }
        
        if (invoice.getServiceCharges() != null && invoice.getServiceCharges().doubleValue() > 0) {
            addModernItemRow(itemsTable, "Service Charges", "1",
                          formatCurrency(invoice.getServiceCharges()),
                          formatCurrency(invoice.getServiceCharges()), alternate);
            alternate = !alternate;
        }
        
        if (invoice.getFoodCharges() != null && invoice.getFoodCharges().doubleValue() > 0) {
            addModernItemRow(itemsTable, "Food & Beverage", "1",
                          formatCurrency(invoice.getFoodCharges()),
                          formatCurrency(invoice.getFoodCharges()), alternate);
            alternate = !alternate;
        }
        
        if (invoice.getOtherCharges() != null && invoice.getOtherCharges().doubleValue() > 0) {
            addModernItemRow(itemsTable, "Other Charges", "1",
                          formatCurrency(invoice.getOtherCharges()),
                          formatCurrency(invoice.getOtherCharges()), alternate);
        }
        
        document.add(itemsTable);
    }

    private void addModernTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addModernItemRow(PdfPTable table, String desc, String qty, String rate, String amount, boolean alternate) {
        BaseColor bgColor = alternate ? LIGHT_GRAY : BaseColor.WHITE;
        
        PdfPCell descCell = new PdfPCell(new Phrase(desc, NORMAL_FONT));
        descCell.setBackgroundColor(bgColor);
        descCell.setPadding(6);
        descCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(descCell);
        
        PdfPCell qtyCell = new PdfPCell(new Phrase(qty, NORMAL_FONT));
        qtyCell.setBackgroundColor(bgColor);
        qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qtyCell.setPadding(6);
        qtyCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(qtyCell);
        
        PdfPCell rateCell = new PdfPCell(new Phrase(rate, NORMAL_FONT));
        rateCell.setBackgroundColor(bgColor);
        rateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rateCell.setPadding(6);
        rateCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(rateCell);
        
        PdfPCell amountCell = new PdfPCell(new Phrase(amount, BOLD_FONT));
        amountCell.setBackgroundColor(bgColor);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountCell.setPadding(6);
        amountCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(amountCell);
    }

    private void addModernTotals(Document document, InvoiceResponse invoice) throws DocumentException {
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(45);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setWidths(new float[]{2f, 1.5f});
        totalsTable.setSpacingAfter(10);
        
        addTotalRow(totalsTable, "Subtotal:", formatCurrency(invoice.getSubtotal()), false, false);
        
        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().doubleValue() > 0) {
            addTotalRow(totalsTable, "Discount:", "-" + formatCurrency(invoice.getDiscountAmount()), false, false);
        }
        
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().doubleValue() > 0) {
            addTotalRow(totalsTable, "Tax:", formatCurrency(invoice.getTaxAmount()), false, false);
        }
        
        if (invoice.getAmountPaid() != null && invoice.getAmountPaid().doubleValue() > 0) {
            addTotalRow(totalsTable, "Amount Paid:", formatCurrency(invoice.getAmountPaid()), false, false);
        }
        
        // Balance Due with prominent styling
        addTotalRow(totalsTable, "BALANCE DUE:", formatCurrency(invoice.getBalanceDue()), true, true);
        
        document.add(totalsTable);
    }

    private void addTotalRow(PdfPTable table, String label, String amount, boolean isBold, boolean isTotal) {
        Font labelFont = isTotal ? TOTAL_FONT : (isBold ? HEADER_FONT : NORMAL_FONT);
        Font amountFont = isTotal ? TOTAL_FONT : (isBold ? HEADER_FONT : BOLD_FONT);
        BaseColor bgColor = isTotal ? LIGHT_GRAY : BaseColor.WHITE;
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(isTotal ? Rectangle.BOX : Rectangle.NO_BORDER);
        labelCell.setBorderColor(BORDER_COLOR);
        labelCell.setBackgroundColor(bgColor);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(isTotal ? 8 : 4);
        table.addCell(labelCell);
        
        PdfPCell amountCell = new PdfPCell(new Phrase(amount, amountFont));
        amountCell.setBorder(isTotal ? Rectangle.BOX : Rectangle.NO_BORDER);
        amountCell.setBorderColor(BORDER_COLOR);
        amountCell.setBackgroundColor(bgColor);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountCell.setPadding(isTotal ? 8 : 4);
        table.addCell(amountCell);
    }

    private void addCompactPaymentStatus(Document document, InvoiceResponse invoice) throws DocumentException {
        PdfPTable statusTable = new PdfPTable(1);
        statusTable.setWidthPercentage(100);
        statusTable.setSpacingAfter(10);
        
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(8);
        
        Paragraph statusTitle = new Paragraph("PAYMENT STATUS", SECTION_FONT);
        cell.addElement(statusTitle);
        
        Paragraph status = new Paragraph("Status: " + invoice.getStatus(), BOLD_FONT);
        status.setSpacingBefore(4);
        cell.addElement(status);
        
        if (invoice.getPayments() != null && !invoice.getPayments().isEmpty()) {
            PaymentResponse lastPayment = invoice.getPayments().get(invoice.getPayments().size() - 1);
            String paymentInfo = String.format("Last Payment: %s | Method: %s | Txn: %s",
                formatCurrency(lastPayment.getAmountPaid()),
                lastPayment.getPaymentMethod(),
                lastPayment.getTransactionId() != null ? lastPayment.getTransactionId() : "N/A");
            Paragraph payment = new Paragraph(paymentInfo, SMALL_FONT);
            payment.setSpacingBefore(2);
            cell.addElement(payment);
        }
        
        statusTable.addCell(cell);
        document.add(statusTable);
    }

    private void addModernFooter(Document document) throws DocumentException {
        // Thin separator line
        LineSeparator separator = new LineSeparator();
        separator.setLineColor(BORDER_COLOR);
        separator.setLineWidth(0.5f);
        document.add(new Chunk(separator));
        document.add(new Paragraph(" ", SMALL_FONT));
        
        Paragraph disclaimer = new Paragraph(
            "This is an official invoice. For any discrepancies, please contact our accounts department within 30 days.",
            SMALL_FONT
        );
        disclaimer.setAlignment(Element.ALIGN_CENTER);
        document.add(disclaimer);
        
        Paragraph footer = new Paragraph(
            "© " + new SimpleDateFormat("yyyy").format(new Date()) + " Hotel Grand Plaza. All rights reserved. | Generated: " + 
            new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date()),
            new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL, BaseColor.GRAY)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(3);
        document.add(footer);
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%.2f", amount);
    }
}