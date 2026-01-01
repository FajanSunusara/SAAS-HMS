package application.services.impl;

import application.models.Invoice;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SimpleExcelExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    
    // Export as CSV (which Excel can open)
    public void exportInvoicesToCsv(List<Invoice> invoices, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write CSV header
            writer.append("Invoice Number,Guest Name,Date,Subtotal,Tax %,Tax Amount,Total,Status,Payment Method\n");
            
            // Write data rows
            for (Invoice invoice : invoices) {
                writer.append(escape(invoice.getInvoiceNumber())).append(",");
                writer.append(escape(invoice.getGuestName())).append(",");
                writer.append(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FORMATTER) : "").append(",");
                writer.append(String.format("%.2f", invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0.0)).append(",");
                writer.append(String.format("%.1f%%", invoice.getTaxPercentage())).append(",");
                writer.append(String.format("%.2f", invoice.getGst() != null ? invoice.getGst().doubleValue() : 0.0)).append(",");
                writer.append(String.format("%.2f", invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0.0)).append(",");
                writer.append(escape(invoice.getStatus())).append(",");
                writer.append(escape(invoice.getPaymentMethod())).append("\n");
            }
            
            // Add summary section
            double totalRevenue = invoices.stream().mapToDouble(i -> i.getSubtotal() != null ? i.getSubtotal().doubleValue() : 0.0).sum();
            double totalTaxes = invoices.stream().mapToDouble(i -> i.getGst() != null ? i.getGst().doubleValue() : 0.0).sum();
            
            writer.append("\nSUMMARY\n");
            writer.append("Total Records,").append(String.valueOf(invoices.size())).append("\n");
            writer.append("Total Revenue,").append(String.format("%.2f", totalRevenue)).append("\n");
            writer.append("Total Taxes,").append(String.format("%.2f", totalTaxes)).append("\n");
            writer.append("Net Profit,").append(String.format("%.2f", totalRevenue - totalTaxes)).append("\n");
        }
    }
    
    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
