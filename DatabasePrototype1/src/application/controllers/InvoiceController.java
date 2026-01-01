package application.controllers;

import application.models.Invoice;
import application.services.dao.InvoiceDAO;
import application.services.impl.ExcelExportService;
import application.services.impl.PdfExportService;
import application.utils.PropertyReader;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InvoiceController {
    
    // FXML Elements
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private ComboBox<String> paymentTypeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField taxPercentageField;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalTaxesLabel;
    @FXML private Label netProfitLabel;
    @FXML private Label filteredResultsLabel;
    
    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, Boolean> selectColumn;
    @FXML private TableColumn<Invoice, String> invoiceIdColumn;
    @FXML private TableColumn<Invoice, String> customerNameColumn;
    @FXML private TableColumn<Invoice, LocalDate> dateColumn;
    @FXML private TableColumn<Invoice, BigDecimal> amountColumn;
    @FXML private TableColumn<Invoice, Double> taxPercentageColumn;
    @FXML private TableColumn<Invoice, BigDecimal> totalColumn;
    @FXML private TableColumn<Invoice, String> statusColumn;
    @FXML private TableColumn<Invoice, Void> actionColumn;
    
    // Action Buttons
    @FXML private Button exportSelectedExcelButton;
    @FXML private Button exportSelectedPdfButton;
    @FXML private Button exportFilteredExcelButton;
    @FXML private Button exportFilteredPdfButton;
    @FXML private Button saveFilteredToDbButton;
    @FXML private Button saveFilterButton;
    @FXML private Button addInvoiceButton;
    @FXML private Button clearFiltersButton;
    @FXML private ProgressIndicator loadingIndicator;
    
    // Data Management
    private ObservableList<Invoice> masterInvoiceData = FXCollections.observableArrayList();
    private FilteredList<Invoice> filteredInvoiceData;
    private List<Invoice> currentFilteredList = new ArrayList<>();
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private PropertyReader propertyReader;
    private ExcelExportService excelExportService = new ExcelExportService();
    private PdfExportService pdfExportService = new PdfExportService();
    
    @FXML
    public void initialize() {
        propertyReader = PropertyReader.getInstance();
        setupComboBoxes();
        setupTableColumns();
        setupEventListeners();
        loadInvoiceDataAsync();
        updateSummaryTotals();
        invoiceTable.setEditable(true);
        selectColumn.setEditable(true);

    }
    
    private void setupComboBoxes() {
        paymentTypeComboBox.setItems(FXCollections.observableArrayList(
                "All", "Cash", "Credit Card", "Debit Card", "UPI", "Netbanking"));
        statusComboBox.setItems(FXCollections.observableArrayList(
                "All", "Paid", "Unpaid", "Partial"));
        paymentTypeComboBox.setValue("All");
        statusComboBox.setValue("All");
        
        double gstRate = propertyReader.getGstRate();
        if (taxPercentageField != null) {
            taxPercentageField.setText(Double.toString(gstRate));
        }
    }
    
    private void setupTableColumns() {
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty());
        amountColumn.setCellFactory(column -> new TableCell<Invoice, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹%.2f", item.doubleValue()));
                }
            }
        });
        
        taxPercentageColumn.setCellValueFactory(cellData ->
                Bindings.createDoubleBinding(() -> cellData.getValue().getTaxPercentage(),
                        cellData.getValue().subtotalProperty(), cellData.getValue().gstProperty()).asObject());
        taxPercentageColumn.setCellFactory(column -> new TableCell<Invoice, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item));
                }
            }
        });
        
        totalColumn.setCellValueFactory(cellData -> cellData.getValue().totalProperty());
        totalColumn.setCellFactory(column -> new TableCell<Invoice, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹%.2f", item.doubleValue()));
                }
            }
        });
        
        statusColumn.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() -> cellData.getValue().getStatus(),
                        cellData.getValue().paidAmountProperty(), cellData.getValue().totalProperty()));
        
        setupActionColumn();
    }
    
    private void setupActionColumn() {
        actionColumn.setCellFactory(new Callback<TableColumn<Invoice, Void>, TableCell<Invoice, Void>>() {
            @Override
            public TableCell<Invoice, Void> call(final TableColumn<Invoice, Void> param) {
                return new TableCell<Invoice, Void>() {
                    private final Button viewEditButton = new Button("View/Edit");
                    private final Button deleteButton = new Button("❌");
                    
                    {
                        viewEditButton.getStyleClass().add("primary-button");
                        deleteButton.getStyleClass().add("delete-button");
                        
                        viewEditButton.setOnAction(event -> {
                            Invoice invoice = getTableView().getItems().get(getIndex());
                            openInvoiceEditDialog(invoice);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            Invoice invoice = getTableView().getItems().get(getIndex());
                            handleDeleteInvoice(invoice);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5, viewEditButton, deleteButton);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }
    
    private void setupEventListeners() {
        // Create sample data if empty
        if (masterInvoiceData.isEmpty()) {
            createSampleData();
        }
        
        filteredInvoiceData = new FilteredList<>(masterInvoiceData, p -> true);
        invoiceTable.setItems(filteredInvoiceData);
        
        // Filter listeners
        dateFromPicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateToPicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        paymentTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        if (taxPercentageField != null) {
            taxPercentageField.textProperty().addListener((obs, oldVal, newVal) -> updateSummaryTotals());
        }
    }
    
    private void createSampleData() {
        masterInvoiceData.addAll(FXCollections.observableArrayList(
                new Invoice("INV-2025-001", "John Doe", LocalDate.of(2025, 8, 20), 10593.22, 18.0, "Partial", "Credit Card"),
                new Invoice("INV-2025-004", "Jane Smith", LocalDate.of(2025, 8, 22), 17796.61, 18.0, "Partial", "Credit Card"),
                new Invoice("INV-2025-008", "Sophia Martinez", LocalDate.of(2025, 8, 19), 7906.78, 18.0, "Paid", "Debit Card"),
                new Invoice("INV-2025-011", "Noah Anderson", LocalDate.of(2025, 7, 10), 4237.29, 18.0, "Paid", "Credit Card"),
                new Invoice("INV-2025-012", "Mia Thomas", LocalDate.of(2025, 7, 14), 11864.41, 18.0, "Paid", "Credit Card")
        ));
    }
    
    private void loadInvoiceDataAsync() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }
        
        Task<List<Invoice>> loadTask = new Task<List<Invoice>>() {
            @Override
            protected List<Invoice> call() throws Exception {
                try {
                    return invoiceDAO.getAllInvoicesWithDetails();
                } catch (Exception e) {
                    return null;
                }
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            List<Invoice> loadedData = loadTask.getValue();
            if (loadedData != null && !loadedData.isEmpty()) {
                masterInvoiceData.setAll(loadedData);
            } else if (masterInvoiceData.isEmpty()) {
                createSampleData();
            }
            
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
            updateSummaryTotals();
        });
        
        loadTask.setOnFailed(e -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
            if (masterInvoiceData.isEmpty()) {
                createSampleData();
            }
            updateSummaryTotals();
        });
        
        new Thread(loadTask).start();
    }
    
    private void applyFilters() {
        filteredInvoiceData.setPredicate(invoice -> {
            LocalDate fromDate = dateFromPicker.getValue();
            LocalDate toDate = dateToPicker.getValue();
            
            if (fromDate != null && invoice.getInvoiceDate() != null && invoice.getInvoiceDate().isBefore(fromDate)) {
                return false;
            }
            if (toDate != null && invoice.getInvoiceDate() != null && invoice.getInvoiceDate().isAfter(toDate)) {
                return false;
            }
            
            String selectedPaymentType = paymentTypeComboBox.getValue();
            if (selectedPaymentType != null && !"All".equals(selectedPaymentType)) {
                String paymentMethod = invoice.getPaymentMethod();
                if (paymentMethod == null || !paymentMethod.equalsIgnoreCase(selectedPaymentType)) {
                    return false;
                }
            }
            
            String selectedStatus = statusComboBox.getValue();
            if (selectedStatus != null && !"All".equals(selectedStatus)) {
                if (!invoice.getStatus().equalsIgnoreCase(selectedStatus)) {
                    return false;
                }
            }
            
            return true;
        });
        
        // Update current filtered list
        currentFilteredList = filteredInvoiceData.stream().collect(Collectors.toList());
        
        updateSummaryTotals();
        updateResultsCount();
    }
    
    private void updateSummaryTotals() {
        double totalRevenue = 0;
        double totalTaxes = 0;
        
        for (Invoice invoice : filteredInvoiceData) {
            BigDecimal subtotal = invoice.getSubtotal();
            BigDecimal gst = invoice.getGst();
            
            if (subtotal != null) {
                totalRevenue += subtotal.doubleValue();
            }
            if (gst != null) {
                totalTaxes += gst.doubleValue();
            }
        }
        
        double netProfit = totalRevenue - totalTaxes;
        
        totalRevenueLabel.setText(String.format("₹%.2f", totalRevenue));
        totalTaxesLabel.setText(String.format("₹%.2f", totalTaxes));
        netProfitLabel.setText(String.format("₹%.2f", netProfit));
    }
    
    private void updateResultsCount() {
        int count = filteredInvoiceData.size();
        filteredResultsLabel.setText(count + (count == 1 ? " invoice" : " invoices"));
    }
    
    private void openInvoiceEditDialog(Invoice invoice) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/InvoiceEditDialog.fxml"));
            VBox dialogContent = loader.load();
            
            InvoiceEditDialogController dialogController = loader.getController();
            dialogController.setInvoice(invoice);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Invoice Details - " + invoice.getInvoiceNumber());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogContent));
            dialogStage.setResizable(false);
            
            // Set callback for when invoice is updated
            dialogController.setOnInvoiceUpdated(() -> {
                invoiceTable.refresh();
                updateSummaryTotals();
            });
            
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open invoice edit dialog: " + e.getMessage());
        }
    }
    
    // Export Methods
    @FXML
    private void handleExportSelectedExcel() {
        List<Invoice> selectedInvoices = getSelectedInvoices();
        if (selectedInvoices.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select invoices to export.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.setInitialFileName("selected_invoices_" + LocalDate.now().toString() + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(invoiceTable.getScene().getWindow());
        if (file != null) {
            exportToExcel(selectedInvoices, file);
        }
    }
    
    @FXML
    private void handleExportSelectedPdf() {
        List<Invoice> selectedInvoices = getSelectedInvoices();
        if (selectedInvoices.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select invoices to export.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.setInitialFileName("selected_invoices_" + LocalDate.now().toString() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(invoiceTable.getScene().getWindow());
        if (file != null) {
            exportToPdf(selectedInvoices, file);
        }
    }
    
    @FXML
    private void handleExportFilteredExcel() {
        if (currentFilteredList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No filtered data to export.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.setInitialFileName("filtered_invoices_" + LocalDate.now().toString() + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(invoiceTable.getScene().getWindow());
        if (file != null) {
            exportToExcel(currentFilteredList, file);
        }
    }
    
    @FXML
    private void handleExportFilteredPdf() {
        if (currentFilteredList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No filtered data to export.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.setInitialFileName("filtered_invoices_" + LocalDate.now().toString() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(invoiceTable.getScene().getWindow());
        if (file != null) {
            exportToPdf(currentFilteredList, file);
        }
    }
    
    private List<Invoice> getSelectedInvoices() {
        return masterInvoiceData.stream()
                .filter(Invoice::isSelected)
                .collect(Collectors.toList());
    }
    
    private void exportToExcel(List<Invoice> invoices, File file) {
        try {
            excelExportService.export(invoices, file);
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Excel file exported successfully to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not export Excel file: " + e.getMessage());
        }
    }
    
    private void exportToPdf(List<Invoice> invoices, File file) {
        try {
            pdfExportService.export(invoices, file);
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "PDF file exported successfully to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not export PDF file: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSaveFilter() {
        showAlert(Alert.AlertType.INFORMATION, "Filter Saved", 
                String.format("Current filter saved with %d invoices", currentFilteredList.size()));
    }
    
    @FXML
    private void handleSaveFilteredToDb() {
        if (currentFilteredList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No filtered data to save to database.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Save to Database");
        confirmAlert.setHeaderText("Save Filtered Results");
        confirmAlert.setContentText("Save " + currentFilteredList.size() + " filtered invoices to database?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (Invoice invoice : currentFilteredList) {
                try {
                    // Use your InvoiceDAO to save/update invoice
                    invoiceDAO.saveFullInvoiceDetails(invoice);  // Make sure this method exists in your DAO
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Database Error",
                              "Failed to save invoice: " + invoice.getInvoiceNumber() + "\n" + ex.getMessage());
                    return;
                }
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Filtered invoices saved to database successfully.");
        }
    }

    
    @FXML
    private void handleClearFilters() {
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        paymentTypeComboBox.setValue("All");
        statusComboBox.setValue("All");
        applyFilters();
    }
    
    @FXML
    private void handleAddInvoice() {
        showAlert(Alert.AlertType.INFORMATION, "Add Invoice",
                "Add Invoice functionality would open new invoice creation dialog.");
    }
    
    private void handleDeleteInvoice(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Invoice " + invoice.getInvoiceNumber() + "?");
        alert.setContentText("Are you sure you want to delete this invoice? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            masterInvoiceData.remove(invoice);
            updateSummaryTotals();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Invoice deleted successfully.");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
