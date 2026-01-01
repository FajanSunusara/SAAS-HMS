package application.services.impl;

import application.models.Invoice;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public void export(List<Invoice> invoices, File outputFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Invoices");

            // Header style
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Invoice No", "Guest Name", "Date", "Subtotal", "Tax %", "Tax Amount", "Total", "Status", "Payment Method"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Invoice invoice : invoices) {
                Row row = sheet.createRow(rowIdx);

                row.createCell(0).setCellValue(invoice.getInvoiceNumber());
                row.createCell(1).setCellValue(invoice.getGuestName());
                row.createCell(2).setCellValue(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FORMATTER) : "");
                row.createCell(3).setCellValue(invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0);
                row.createCell(4).setCellValue(invoice.getTaxPercentage());
                row.createCell(5).setCellValue(invoice.getGst() != null ? invoice.getGst().doubleValue() : 0);
                row.createCell(6).setCellValue(invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0);
                row.createCell(7).setCellValue(invoice.getStatus());
                row.createCell(8).setCellValue(invoice.getPaymentMethod());

                rowIdx++;
            }

            // Autosize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
    }
}
