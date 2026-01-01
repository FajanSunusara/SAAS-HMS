package application.controllers;

import application.models.Invoice;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceEditDialogController {
    
    @FXML private Label dialogTitleLabel;
    @FXML private Label invoiceNumberLabel;
    @FXML private DatePicker invoiceDatePicker;
    @FXML private TextField guestNameField;
    @FXML private TextField guestEmailField;
    @FXML private TextField guestPhoneField;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private TextField roomNumberField;
    @FXML private TextField roomRateField;
    @FXML private TextField subtotalField;
    @FXML private TextField taxPercentageDialogField;
    @FXML private Label taxAmountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private ComboBox<String> paymentStatusComboBox;
    @FXML private Button printInvoiceButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    
    private Invoice currentInvoice;
    private Runnable onInvoiceUpdatedCallback;
    
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupCalculationListeners();
    }
    
    private void setupComboBoxes() {
        paymentMethodComboBox.getItems().addAll("Cash", "Credit Card", "Debit Card", "UPI", "Net Banking");
        paymentStatusComboBox.getItems().addAll("Paid", "Unpaid", "Partial");
    }
    
    private void setupCalculationListeners() {
        // Auto-calculate tax and total when subtotal or tax percentage changes
        subtotalField.textProperty().addListener((obs, oldVal, newVal) -> calculateTotals());
        taxPercentageDialogField.textProperty().addListener((obs, oldVal, newVal) -> calculateTotals());
    }
    
    public void setInvoice(Invoice invoice) {
        this.currentInvoice = invoice;
        populateFields();
    }
    
    public void setOnInvoiceUpdated(Runnable callback) {
        this.onInvoiceUpdatedCallback = callback;
    }
    
    private void populateFields() {
        if (currentInvoice == null) return;
        
        // Basic info
        invoiceNumberLabel.setText(currentInvoice.getInvoiceNumber() != null ? currentInvoice.getInvoiceNumber() : "N/A");
        invoiceDatePicker.setValue(currentInvoice.getInvoiceDate());
        
        // Guest information
        guestNameField.setText(currentInvoice.getGuestName() != null ? currentInvoice.getGuestName() : "");
        guestEmailField.setText(currentInvoice.getGuestEmail() != null ? currentInvoice.getGuestEmail() : "");
        guestPhoneField.setText(currentInvoice.getGuestPhone() != null ? currentInvoice.getGuestPhone() : "");
        
        // Room details
        checkInDatePicker.setValue(currentInvoice.getCheckInDate());
        checkOutDatePicker.setValue(currentInvoice.getCheckOutDate());
        roomNumberField.setText(currentInvoice.getRoomNo() != null ? currentInvoice.getRoomNo() : "");
        
        // Financial details
        BigDecimal subtotal = currentInvoice.getSubtotal();
        if (subtotal != null) {
            subtotalField.setText(String.valueOf(subtotal.doubleValue()));
            roomRateField.setText(String.valueOf(subtotal.doubleValue()));
        }
        
        taxPercentageDialogField.setText(String.valueOf(currentInvoice.getTaxPercentage()));
        
        // Payment details
        paymentMethodComboBox.setValue(currentInvoice.getPaymentMethod());
        paymentStatusComboBox.setValue(currentInvoice.getStatus());
        
        calculateTotals();
    }
    
    private void calculateTotals() {
        try {
            double subtotal = Double.parseDouble(subtotalField.getText().trim());
            double taxPercentage = Double.parseDouble(taxPercentageDialogField.getText().trim());
            
            double taxAmount = subtotal * (taxPercentage / 100.0);
            double total = subtotal + taxAmount;
            
            taxAmountLabel.setText(String.format("₹%.2f", taxAmount));
            totalAmountLabel.setText(String.format("₹%.2f", total));
            
        } catch (NumberFormatException e) {
            taxAmountLabel.setText("₹0.00");
            totalAmountLabel.setText("₹0.00");
        }
    }
    
    @FXML
    private void handleSave() {
        if (currentInvoice == null) return;
        
        try {
            // Update invoice with form data
            currentInvoice.setInvoiceDate(invoiceDatePicker.getValue());
            currentInvoice.setGuestName(guestNameField.getText().trim());
            currentInvoice.setGuestEmail(guestEmailField.getText().trim());
            currentInvoice.setGuestPhone(guestPhoneField.getText().trim());
            currentInvoice.setCheckInDate(checkInDatePicker.getValue());
            currentInvoice.setCheckOutDate(checkOutDatePicker.getValue());
            currentInvoice.setRoomNo(roomNumberField.getText().trim());
            currentInvoice.setPaymentMethod(paymentMethodComboBox.getValue());
            
            // Update financial data
            double subtotal = Double.parseDouble(subtotalField.getText().trim());
            double taxPercentage = Double.parseDouble(taxPercentageDialogField.getText().trim());
            double taxAmount = subtotal * (taxPercentage / 100.0);
            double total = subtotal + taxAmount;
            
            currentInvoice.setSubtotal(BigDecimal.valueOf(subtotal));
            currentInvoice.setGst(BigDecimal.valueOf(taxAmount));
            currentInvoice.setTotal(BigDecimal.valueOf(total));
            
            // Update payment status
            String status = paymentStatusComboBox.getValue();
            if ("Paid".equals(status)) {
                currentInvoice.setPaidAmount(BigDecimal.valueOf(total));
                currentInvoice.setDueAmount(BigDecimal.ZERO);
            } else if ("Unpaid".equals(status)) {
                currentInvoice.setPaidAmount(BigDecimal.ZERO);
                currentInvoice.setDueAmount(BigDecimal.valueOf(total));
            }
            
            // Notify parent controller
            if (onInvoiceUpdatedCallback != null) {
                onInvoiceUpdatedCallback.run();
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Invoice updated successfully!");
            closeDialog();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numeric values for amounts.");
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    @FXML
    private void handlePrintInvoice() {
        showAlert(Alert.AlertType.INFORMATION, "Print", "Invoice printing functionality would be implemented here.");
    }
    
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
