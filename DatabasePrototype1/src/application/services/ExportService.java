package application.services;

import application.models.Invoice;
import java.io.File;
import java.util.List;

public interface ExportService {
    void exportToExcel(List<Invoice> invoices, File file) throws Exception;
    void exportToPdf(List<Invoice> invoices, File file) throws Exception;
    void exportInvoiceToPdf(Invoice invoice, File file) throws Exception;
}
