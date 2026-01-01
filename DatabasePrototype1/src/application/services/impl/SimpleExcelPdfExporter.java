package application.services.impl;

import application.models.Invoice;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SimpleExcelPdfExporter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    
    // Export as Excel-compatible XML format (opens in Excel perfectly)
    public void exportToExcel(List<Invoice> invoices, File file) throws IOException {
        System.out.println("Creating Excel XML file...");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write Excel XML header
            writer.println("<?xml version=\"1.0\"?>");
            writer.println("<?mso-application progid=\"Excel.Sheet\"?>");
            writer.println("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
            writer.println(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"");
            writer.println(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"");
            writer.println(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"");
            writer.println(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
            
            // Define styles
            writer.println("<Styles>");
            writer.println("<Style ss:ID=\"Header\">");
            writer.println(" <Font ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/>");
            writer.println(" <Interior ss:Color=\"#4F81BD\" ss:Pattern=\"Solid\"/>");
            writer.println(" <Borders>");
            writer.println("  <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println("  <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println("  <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println("  <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println(" </Borders>");
            writer.println("</Style>");
            writer.println("<Style ss:ID=\"Data\">");
            writer.println(" <Borders>");
            writer.println("  <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println("  <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println("  <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println("  <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println(" </Borders>");
            writer.println("</Style>");
            writer.println("<Style ss:ID=\"Currency\">");
            writer.println(" <NumberFormat ss:Format=\"&quot;₹&quot;#,##0.00\"/>");
            writer.println(" <Borders>");
            writer.println("  <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println("  <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println("  <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println("  <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>");
            writer.println(" </Borders>");
            writer.println("</Style>");
            writer.println("<Style ss:ID=\"Title\">");
            writer.println(" <Font ss:Bold=\"1\" ss:Size=\"16\" ss:Color=\"#1F497D\"/>");
            writer.println(" <Alignment ss:Horizontal=\"Center\"/>");
            writer.println("</Style>");
            writer.println("</Styles>");
            
            // Start worksheet
            writer.println("<Worksheet ss:Name=\"Invoice Report\">");
            writer.println("<Table>");
            
            // Title row
            writer.println("<Row>");
            writer.println(" <Cell ss:MergeAcross=\"8\" ss:StyleID=\"Title\">");
            writer.println("  <Data ss:Type=\"String\">THE GRAND HOTEL - INVOICE REPORT</Data>");
            writer.println(" </Cell>");
            writer.println("</Row>");
            
            // Date row
            writer.println("<Row>");
            writer.println(" <Cell ss:MergeAcross=\"8\">");
            writer.println("  <Data ss:Type=\"String\">Generated on: " + java.time.LocalDate.now().format(DATE_FORMATTER) + "</Data>");
            writer.println(" </Cell>");
            writer.println("</Row>");
            
            // Empty row
            writer.println("<Row></Row>");
            
            // Header row
            writer.println("<Row>");
            String[] headers = {"Invoice Number", "Guest Name", "Date", "Check-in", "Check-out", 
                               "Room No.", "Subtotal", "Tax %", "Tax Amount", "Total", "Status", "Payment Method"};
            for (String header : headers) {
                writer.println(" <Cell ss:StyleID=\"Header\">");
                writer.println("  <Data ss:Type=\"String\">" + escapeXml(header) + "</Data>");
                writer.println(" </Cell>");
            }
            writer.println("</Row>");
            
            // Data rows
            double totalRevenue = 0;
            double totalTaxes = 0;
            double grandTotal = 0;
            
            for (Invoice invoice : invoices) {
                writer.println("<Row>");
                
                // Invoice Number
                writer.println(" <Cell ss:StyleID=\"Data\">");
                writer.println("  <Data ss:Type=\"String\">" + escapeXml(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "") + "</Data>");
                writer.println(" </Cell>");
                
                // Guest Name
                writer.println(" <Cell ss:StyleID=\"Data\">");
                writer.println("  <Data ss:Type=\"String\">" + escapeXml(invoice.getGuestName() != null ? invoice.getGuestName() : "") + "</Data>");
                writer.println(" </Cell>");
                
                // Invoice Date
                writer.println(" <Cell ss:StyleID=\"Data\">");
                writer.println("  <Data ss:Type=\"String\">" + (invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FORMATTER) : "") + "</Data>");
                writer.println(" </Cell>");
                
                // Check-in Date
                writer.println(" <Cell ss:StyleID=\"Data\">");
                writer.println("  <Data ss:Type=\"String\">" + (invoice.getCheckInDate() != null ? invoice.getCheckInDate().format(DATE_FORMATTER) : "") + "</Data>");
                writer.println(" </Cell>");
                
                // Check-out Date
                writer.println(" <Cell ss:StyleID=\"Data\">");
                writer.println("  <Data ss:Type=\"String\">" + (invoice.getCheckOutDate() != null ? invoice.getCheckOutDate().format(DATE_FORMATTER) : "") + "</Data>");
                writer.println(" </Cell>");
                
                // Room Number
                writer.println(" <Cell ss:StyleID=\"Data\">");
                writer.println("  <Data ss:Type=\"String\">" + escapeXml(invoice.getRoomNo() != null ? invoice.getRoomNo() : "") + "</Data>");
                writer.println(" </Cell>");
                
                // Subtotal
                double subtotal = invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0.0;
                writer.println(" <Cell ss:StyleID=\"Currency\">");
                writer.println("  <Data ss:Type=\"Number\">" + subtotal + "</Data>");
                writer.println(" </Cell>");
                
                // Tax Percentage
                writer.println(" <Cell ss:StyleID=\"Data\">");
                writer.println("  <Data ss:Type=\"String\">" + String.format("%.1f%%", invoice.getTaxPercentage()) + "</Data>");
                writer.println(" </Cell>");
                
                // Tax Amount
                double tax = invoice.getGst() != null ? invoice.getGst().doubleValue() : 0.0;
                writer.println(" <Cell ss:StyleID=\"Currency\">");
                writer.println("  <Data ss:Type=\"Number\">" + tax + "</Data>");
                writer.println(" </Cell>");
                
                // Total
                double total = invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0.0;
                writer.println(" <Cell ss:StyleID=\"Currency\">");
                writer.println("  <Data ss:Type=\"Number\">" + total + "</Data>");
                writer.println(" </Cell>");
                
                // Status
                writer.println(" <Cell ss:StyleID=\"Data\">");
                writer.println("  <Data ss:Type=\"String\">" + escapeXml(invoice.getStatus() != null ? invoice.getStatus() : "") + "</Data>");
                writer.println(" </Cell>");
                
                // Payment Method
                writer.println(" <Cell ss:StyleID=\"Data\">");
                writer.println("  <Data ss:Type=\"String\">" + escapeXml(invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "") + "</Data>");
                writer.println(" </Cell>");
                
                writer.println("</Row>");
                
                totalRevenue += subtotal;
                totalTaxes += tax;
                grandTotal += total;
            }
            
            // Empty row before summary
            writer.println("<Row></Row>");
            
            // Summary rows
            addSummaryRow(writer, "Total Records:", String.valueOf(invoices.size()));
            addSummaryRow(writer, "Total Revenue:", String.format("₹%.2f", totalRevenue));
            addSummaryRow(writer, "Total Taxes:", String.format("₹%.2f", totalTaxes));
            addSummaryRow(writer, "Grand Total:", String.format("₹%.2f", grandTotal));
            addSummaryRow(writer, "Net Profit:", String.format("₹%.2f", totalRevenue - totalTaxes));
            
            writer.println("</Table>");
            writer.println("</Worksheet>");
            writer.println("</Workbook>");
        }
        
        System.out.println("Excel XML file created successfully!");
    }
    
    private void addSummaryRow(PrintWriter writer, String label, String value) {
        writer.println("<Row>");
        // Empty cells to align with data
        for (int i = 0; i < 9; i++) {
            writer.println(" <Cell></Cell>");
        }
        writer.println(" <Cell ss:StyleID=\"Header\">");
        writer.println("  <Data ss:Type=\"String\">" + escapeXml(label) + "</Data>");
        writer.println(" </Cell>");
        writer.println(" <Cell ss:StyleID=\"Header\">");
        writer.println("  <Data ss:Type=\"String\">" + escapeXml(value) + "</Data>");
        writer.println(" </Cell>");
        writer.println("</Row>");
    }
    
    // Export as HTML that looks like PDF (can be printed to PDF from browser)
    public void exportToPdf(List<Invoice> invoices, File file) throws IOException {
        System.out.println("Creating PDF-style HTML file...");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Invoice Report - The Grand Hotel</title>");
            writer.println("<style>");
            writer.println("@page { size: A4; margin: 0.5in; }");
            writer.println("body { font-family: Arial, sans-serif; margin: 0; padding: 0; font-size: 12px; }");
            writer.println(".header { text-align: center; margin-bottom: 30px; page-break-after: avoid; }");
            writer.println(".hotel-name { font-size: 24px; font-weight: bold; color: #1f497d; margin-bottom: 5px; }");
            writer.println(".report-title { font-size: 18px; font-weight: bold; margin-bottom: 10px; }");
            writer.println(".date-info { font-size: 12px; color: #666; margin-bottom: 20px; }");
            writer.println("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
            writer.println("th { background-color: #4F81BD; color: white; padding: 8px; text-align: center; font-size: 10px; border: 1px solid #000; }");
            writer.println("td { padding: 6px; text-align: left; font-size: 9px; border: 1px solid #ccc; }");
            writer.println(".number { text-align: right; }");
            writer.println(".center { text-align: center; }");
            writer.println(".summary { margin-top: 20px; float: right; }");
            writer.println(".summary table { width: 300px; }");
            writer.println(".summary th { background-color: #f2f2f2; color: #333; }");
            writer.println("@media print { ");
            writer.println("  body { print-color-adjust: exact; }");
            writer.println("  .no-print { display: none; }");
            writer.println("}");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            
            // Header
            writer.println("<div class='header'>");
            writer.println("<div class='hotel-name'>THE GRAND HOTEL</div>");
            writer.println("<div class='report-title'>INVOICE REPORT</div>");
            writer.println("<div class='date-info'>Generated on: " + java.time.LocalDate.now().format(DATE_FORMATTER) + "</div>");
            writer.println("</div>");
            
            // Print instructions
            writer.println("<div class='no-print' style='background: #ffffcc; padding: 10px; margin-bottom: 20px; border: 1px solid #ffeb3b;'>");
            writer.println("<strong>Instructions:</strong> Press Ctrl+P to print this report to PDF. Make sure to select 'Save as PDF' in the print dialog.");
            writer.println("</div>");
            
            // Main table
            writer.println("<table>");
            writer.println("<thead><tr>");
            writer.println("<th>Invoice #</th><th>Guest Name</th><th>Date</th><th>Check-in</th><th>Check-out</th>");
            writer.println("<th>Room</th><th>Subtotal</th><th>Tax %</th><th>Tax Amt</th><th>Total</th><th>Status</th><th>Payment</th>");
            writer.println("</tr></thead>");
            writer.println("<tbody>");
            
            double totalRevenue = 0;
            double totalTaxes = 0;
            double grandTotal = 0;
            
            for (Invoice invoice : invoices) {
                writer.println("<tr>");
                
                writer.println("<td class='center'>" + escapeHtml(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "") + "</td>");
                writer.println("<td>" + escapeHtml(invoice.getGuestName() != null ? invoice.getGuestName() : "") + "</td>");
                writer.println("<td class='center'>" + (invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FORMATTER) : "") + "</td>");
                writer.println("<td class='center'>" + (invoice.getCheckInDate() != null ? invoice.getCheckInDate().format(DATE_FORMATTER) : "") + "</td>");
                writer.println("<td class='center'>" + (invoice.getCheckOutDate() != null ? invoice.getCheckOutDate().format(DATE_FORMATTER) : "") + "</td>");
                writer.println("<td class='center'>" + escapeHtml(invoice.getRoomNo() != null ? invoice.getRoomNo() : "") + "</td>");
                
                double subtotal = invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0.0;
                double tax = invoice.getGst() != null ? invoice.getGst().doubleValue() : 0.0;
                double total = invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0.0;
                
                writer.println("<td class='number'>₹" + String.format("%.2f", subtotal) + "</td>");
                writer.println("<td class='center'>" + String.format("%.1f%%", invoice.getTaxPercentage()) + "</td>");
                writer.println("<td class='number'>₹" + String.format("%.2f", tax) + "</td>");
                writer.println("<td class='number'><strong>₹" + String.format("%.2f", total) + "</strong></td>");
                writer.println("<td class='center'>" + escapeHtml(invoice.getStatus() != null ? invoice.getStatus() : "") + "</td>");
                writer.println("<td class='center'>" + escapeHtml(invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "") + "</td>");
                
                writer.println("</tr>");
                
                totalRevenue += subtotal;
                totalTaxes += tax;
                grandTotal += total;
            }
            
            writer.println("</tbody></table>");
            
            // Summary
            writer.println("<div class='summary'>");
            writer.println("<table>");
            writer.println("<tr><th colspan='2' style='text-align: center; background-color: #1f497d; color: white;'>SUMMARY</th></tr>");
            writer.println("<tr><th>Total Records:</th><td class='number'>" + invoices.size() + "</td></tr>");
            writer.println("<tr><th>Total Revenue:</th><td class='number'>₹" + String.format("%.2f", totalRevenue) + "</td></tr>");
            writer.println("<tr><th>Total Taxes:</th><td class='number'>₹" + String.format("%.2f", totalTaxes) + "</td></tr>");
            writer.println("<tr><th>Grand Total:</th><td class='number'><strong>₹" + String.format("%.2f", grandTotal) + "</strong></td></tr>");
            writer.println("<tr><th>Net Profit:</th><td class='number'><strong>₹" + String.format("%.2f", totalRevenue - totalTaxes) + "</strong></td></tr>");
            writer.println("</table>");
            writer.println("</div>");
            
            writer.println("<div style='clear: both;'></div>");
            writer.println("</body></html>");
        }
        
        System.out.println("PDF-style HTML file created successfully!");
    }
    
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;");
    }
}
