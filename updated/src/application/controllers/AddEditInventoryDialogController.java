package application.controllers;

import application.controllers.InventoryController;
import application.models.inventory.InventoryItem;
import application.models.inventory.Vendor;
import application.services.dao.inventory.InventoryDAO;
import application.services.dao.inventory.VendorDAO;
import application.utils.inventory.AlertUtils;
import application.utils.inventory.ValidationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AddEditInventoryDialogController {

    // Header Controls
    @FXML private Label lblDialogTitle;
    @FXML private Label lblItemId;

    // Basic Information
    @FXML private TextField txtItemName;
    @FXML private TextField txtSKU;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbLocation;
    @FXML private ComboBox<String> cmbUnit;
    @FXML private TextField txtBarcode;
    @FXML private TextArea txtDescription;

    // Stock Information
    @FXML private TextField txtAvailableQty;
    @FXML private TextField txtReservedQty;
    @FXML private TextField txtMinStock;
    @FXML private TextField txtMaxStock;
    @FXML private Label lblStockStatus;
    @FXML private Label lblStockStatusValue;
    @FXML private Label lblTotalValue;

    // Pricing Information
    @FXML private TextField txtUnitPrice;
    @FXML private ComboBox<String> cmbCurrency;

    // Supplier Information
    @FXML private ComboBox<String> cmbSupplier;
    @FXML private TextField txtSupplierContact;

    // System Information
    @FXML private VBox systemInfoSection;
    @FXML private Label lblLastUpdated;
    @FXML private Label lblUpdatedBy;
    @FXML private Label lblCreatedDate;
    @FXML private CheckBox chkActive;

    // Action Buttons
    @FXML private Button btnGenerateSKU;
    @FXML private Button btnClear;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    // Status
    @FXML private Label lblValidationStatus;

    // Data and Controllers
    private InventoryItem currentItem;
    private InventoryController parentController;
    private InventoryDAO inventoryDAO;
    private VendorDAO vendorDAO;
    private boolean isEditMode = false;
    private Stage dialogStage;

    // Observable Lists
    private ObservableList<String> categories = FXCollections.observableArrayList();
    private ObservableList<String> locations = FXCollections.observableArrayList();
    private ObservableList<String> units = FXCollections.observableArrayList();
    private ObservableList<String> suppliers = FXCollections.observableArrayList();
    private ObservableList<String> currencies = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initializeDAOs();
        setupComboBoxes();
        setupFieldListeners();
        setupValidation();
        loadInitialData();
    }

    private void initializeDAOs() {
        try {
            inventoryDAO = new InventoryDAO();
            vendorDAO = new VendorDAO();
        } catch (Exception e) {
            AlertUtils.showError("Initialization Error", "Failed to initialize database connections: " + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        // Categories
        categories.addAll("Linen", "Toiletries", "Furniture", "Electronics", 
                         "Cleaning Supplies", "Food & Beverage", "Maintenance", 
                         "Office Supplies", "Safety & Security", "Guest Amenities");
        cmbCategory.setItems(categories);

        // Locations
        locations.addAll("Main Store", "Floor 1 Storage", "Floor 2 Storage", "Floor 3 Storage",
                        "Basement Storage", "Laundry Room", "Kitchen Storage", "Reception Desk");
        cmbLocation.setItems(locations);

        // Units
        units.addAll("pieces", "sets", "kg", "grams", "liters", "ml", "meters", "cm", 
                    "boxes", "packs", "bottles", "rolls", "pairs");
        cmbUnit.setItems(units);
        cmbUnit.setValue("pieces");

        // Currencies
        currencies.addAll("INR", "USD", "EUR", "GBP");
        cmbCurrency.setItems(currencies);
        cmbCurrency.setValue("INR");

        // Make location and unit editable
        cmbLocation.setEditable(true);
        cmbUnit.setEditable(true);
        cmbSupplier.setEditable(true);
    }

    private void setupFieldListeners() {
        // Numeric fields - allow only numbers
        addNumericValidation(txtAvailableQty);
        addNumericValidation(txtReservedQty);
        addNumericValidation(txtMinStock);
        addNumericValidation(txtMaxStock);
        addDecimalValidation(txtUnitPrice);

        // Auto-update total value when quantity or price changes
        txtAvailableQty.textProperty().addListener((obs, old, newVal) -> updateCalculatedFields());
        txtUnitPrice.textProperty().addListener((obs, old, newVal) -> updateCalculatedFields());
        txtMinStock.textProperty().addListener((obs, old, newVal) -> updateStockStatus());

        // Auto-generate SKU when item name or category changes
        txtItemName.textProperty().addListener((obs, old, newVal) -> {
            if (safeTrim(txtSKU.getText()).isEmpty() || !isEditMode) {
                generateSKUAuto();
            }
        });
        
        cmbCategory.valueProperty().addListener((obs, old, newVal) -> {
            if (safeTrim(txtSKU.getText()).isEmpty() || !isEditMode) {
                generateSKUAuto();
            }
        });

        // Load supplier contact when supplier is selected
        cmbSupplier.valueProperty().addListener((obs, old, newVal) -> loadSupplierContact(newVal));

        // Real-time validation feedback
        txtItemName.textProperty().addListener((obs, old, newVal) -> validateField(txtItemName, "Item Name", true));
        txtUnitPrice.textProperty().addListener((obs, old, newVal) -> validateField(txtUnitPrice, "Unit Price", true));
        txtMinStock.textProperty().addListener((obs, old, newVal) -> validateField(txtMinStock, "Minimum Stock", true));
    }

    private void setupValidation() {
        lblValidationStatus.setText("Ready to add new item");
    }

    private void loadInitialData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load existing categories and locations from database
                List<String> dbCategories = inventoryDAO.getAllCategories();
                List<String> dbLocations = inventoryDAO.getAllLocations();
                List<Vendor> vendors = vendorDAO.getAllVendors();
                
                Platform.runLater(() -> {
                    // Add database categories if not already present
                    for (String category : dbCategories) {
                        if (!categories.contains(category)) {
                            categories.add(category);
                        }
                    }
                    
                    // Add database locations if not already present
                    for (String location : dbLocations) {
                        if (!locations.contains(location)) {
                            locations.add(location);
                        }
                    }
                    
                    // Load suppliers
                    suppliers.clear();
                    for (Vendor vendor : vendors) {
                        suppliers.add(vendor.getVendorName());
                    }
                    cmbSupplier.setItems(suppliers);
                });
                
                return null;
            }
        };
        
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            AlertUtils.showError("Data Loading Error", 
                "Failed to load initial data: " + (exception != null ? exception.getMessage() : "Unknown error"));
        });
        
        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    public void setInventoryItem(InventoryItem item) {
        this.currentItem = item;
        this.isEditMode = (item != null && item.getItemId() > 0);
        
        updateDialogTitle();
        
        if (isEditMode) {
            populateFields();
            systemInfoSection.setVisible(true);
        } else {
            clearForm();
            systemInfoSection.setVisible(false);
        }
    }

    public void setParentController(InventoryController parentController) {
        this.parentController = parentController;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void updateDialogTitle() {
        if (isEditMode) {
            lblDialogTitle.setText("Edit Inventory Item");
            lblItemId.setText("ID: " + currentItem.getItemId());
            lblItemId.setVisible(true);
            btnSave.setText("Update Item");
        } else {
            lblDialogTitle.setText("Add New Inventory Item");
            lblItemId.setVisible(false);
            btnSave.setText("Save Item");
        }
    }

    private void populateFields() {
        if (currentItem == null) return;
        
        txtItemName.setText(safeTrim(currentItem.getItemName()));
        txtSKU.setText(safeTrim(currentItem.getSku()));
        cmbCategory.setValue(currentItem.getCategory());
        cmbLocation.setValue(currentItem.getLocation());
        txtDescription.setText(safeTrim(currentItem.getDescription()));
        
        txtAvailableQty.setText(String.valueOf(currentItem.getAvailableQuantity()));
        txtReservedQty.setText(String.valueOf(currentItem.getReservedQuantity()));
        txtMinStock.setText(String.valueOf(currentItem.getMinStockLevel()));
        txtMaxStock.setText(String.valueOf(currentItem.getMaxStockLevel()));
        
        if (currentItem.getUnitPrice() != null) {
            txtUnitPrice.setText(currentItem.getUnitPrice().toString());
        }
        cmbUnit.setValue(currentItem.getUnit() != null ? currentItem.getUnit() : "pieces");
        
        cmbSupplier.setValue(currentItem.getSupplier());
        txtSupplierContact.setText(safeTrim(currentItem.getSupplierContact()));
        txtBarcode.setText(safeTrim(currentItem.getBarcode()));
        
        // System information
        if (currentItem.getLastUpdated() != null) {
            lblLastUpdated.setText(currentItem.getLastUpdated().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        }
        lblUpdatedBy.setText(safeTrim(currentItem.getUpdatedBy()));
        
        if (currentItem.getCreatedAt() != null) {
            lblCreatedDate.setText(currentItem.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        }
        
        chkActive.setSelected(currentItem.isActive());
        
        updateCalculatedFields();
        updateStockStatus();
    }

    @FXML
    private void handleGenerateSKU(ActionEvent event) {
        generateSKU();
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        if (AlertUtils.showConfirmation("Clear Form", "Are you sure you want to clear all fields?")) {
            clearForm();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (hasUnsavedChanges()) {
            if (!AlertUtils.showConfirmation("Unsaved Changes", 
                    "You have unsaved changes. Are you sure you want to close?")) {
                return;
            }
        }
        closeDialog();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (validateForm()) {
            saveItem();
        }
    }

    private void generateSKUAuto() {
        if (safeTrim(txtItemName.getText()).isEmpty()) return;
        
        String itemName = safeTrim(txtItemName.getText());
        String category = cmbCategory.getValue();
        
        String sku = generateSKUFromNameAndCategory(itemName, category);
        txtSKU.setText(sku);
    }

    private void generateSKU() {
        String itemName = safeTrim(txtItemName.getText());
        String category = cmbCategory.getValue();
        
        if (itemName.isEmpty()) {
            AlertUtils.showWarning("Generate SKU", "Please enter an item name first.");
            txtItemName.requestFocus();
            return;
        }
        
        String sku = generateSKUFromNameAndCategory(itemName, category);
        txtSKU.setText(sku);
        AlertUtils.showInfo("SKU Generated", "SKU has been generated: " + sku);
    }

    private String generateSKUFromNameAndCategory(String itemName, String category) {
        String categoryPrefix = "GEN";
        if (category != null && !category.isEmpty()) {
            switch (category) {
                case "Linen": categoryPrefix = "LIN"; break;
                case "Toiletries": categoryPrefix = "TOI"; break;
                case "Furniture": categoryPrefix = "FUR"; break;
                case "Electronics": categoryPrefix = "ELE"; break;
                case "Cleaning Supplies": categoryPrefix = "CLE"; break;
                case "Food & Beverage": categoryPrefix = "F&B"; break;
                case "Maintenance": categoryPrefix = "MAI"; break;
                case "Office Supplies": categoryPrefix = "OFF"; break;
                case "Safety & Security": categoryPrefix = "SAF"; break;
                case "Guest Amenities": categoryPrefix = "AME"; break;
                default: categoryPrefix = category.substring(0, Math.min(3, category.length())).toUpperCase(); break;
            }
        }
        
        String cleanName = itemName.replaceAll("[^A-Za-z0-9]", "");
        String namePrefix = cleanName.length() > 0 ? 
            cleanName.toUpperCase().substring(0, Math.min(3, cleanName.length())) : "ITM";
        
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000);
        return categoryPrefix + namePrefix + String.format("%03d", Integer.parseInt(timestamp));
    }

    private void loadSupplierContact(String supplierName) {
        if (supplierName == null || supplierName.trim().isEmpty()) {
            txtSupplierContact.setText("");
            return;
        }
        
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                Vendor vendor = vendorDAO.getVendorByName(supplierName);
                return vendor != null ? safeTrim(vendor.getPhone()) : "";
            }
        };
        
        task.setOnSucceeded(e -> txtSupplierContact.setText(task.getValue()));
        task.setOnFailed(e -> txtSupplierContact.setText(""));
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateCalculatedFields() {
        try {
            int availableQty = parseIntegerSafe(txtAvailableQty.getText());
            BigDecimal unitPrice = parseDecimalSafe(txtUnitPrice.getText());
            
            BigDecimal totalValue = unitPrice.multiply(BigDecimal.valueOf(availableQty)).setScale(2, RoundingMode.HALF_UP);
            lblTotalValue.setText("Total Value: ₹" + totalValue.toString());
            
        } catch (Exception e) {
            lblTotalValue.setText("Total Value: ₹0.00");
        }
        
        updateStockStatus();
    }

    private void updateStockStatus() {
        try {
            int availableQty = parseIntegerSafe(txtAvailableQty.getText());
            int minStock = parseIntegerSafe(txtMinStock.getText());
            
            if (availableQty == 0) {
                lblStockStatusValue.setText("Out of Stock");
                lblStockStatusValue.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else if (availableQty <= minStock) {
                lblStockStatusValue.setText("Low Stock");
                lblStockStatusValue.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            } else {
                lblStockStatusValue.setText("Good");
                lblStockStatusValue.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            lblStockStatusValue.setText("Unknown");
            lblStockStatusValue.setStyle("-fx-text-fill: #7f8c8d;");
        }
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();
        
        // Basic validation
        ValidationUtils.validateRequired(txtItemName, "Item Name", 2, errors);
        ValidationUtils.validateSelection(cmbCategory, "Category", errors);
        ValidationUtils.validateRequired(txtUnitPrice, "Unit Price", errors);
        ValidationUtils.validateRequired(txtMinStock, "Minimum Stock Level", errors);
        
        // Numeric validation
        ValidationUtils.validatePositiveInteger(txtAvailableQty, "Available Quantity", false, errors);
        ValidationUtils.validatePositiveDecimal(txtUnitPrice, "Unit Price", true, errors);
        ValidationUtils.validatePositiveInteger(txtMinStock, "Minimum Stock Level", true, errors);
        
        if (!safeTrim(txtMaxStock.getText()).isEmpty()) {
            Integer maxStock = ValidationUtils.validatePositiveInteger(txtMaxStock, "Maximum Stock Level", false, errors);
            Integer minStock = ValidationUtils.validatePositiveInteger(txtMinStock, "Minimum Stock Level", true, errors);
            
            if (maxStock != null && minStock != null && maxStock <= minStock) {
                errors.add("Maximum stock level must be greater than minimum stock level");
                ValidationUtils.addErrorStyle(txtMaxStock);
            }
        }
        
        // SKU validation
        String sku = safeTrim(txtSKU.getText());
        if (!sku.isEmpty() && !isValidSKU(sku)) {
            errors.add("SKU must be 3-20 characters and contain only letters and numbers");
            ValidationUtils.addErrorStyle(txtSKU);
        }
        
        if (errors.isEmpty()) {
            lblValidationStatus.setText("Validation passed");
            lblValidationStatus.setStyle("-fx-text-fill: #27ae60;");
            return true;
        } else {
            lblValidationStatus.setText(errors.size() + " validation error(s)");
            lblValidationStatus.setStyle("-fx-text-fill: #e74c3c;");
            AlertUtils.showValidationErrors(errors);
            return false;
        }
    }

    private void saveItem() {
        try {
            if (currentItem == null) {
                currentItem = new InventoryItem();
            }
            
            // Populate item from form with null-safe operations
            currentItem.setItemName(safeTrim(txtItemName.getText()));
            currentItem.setSku(safeTrim(txtSKU.getText()));
            currentItem.setCategory(cmbCategory.getValue());
            currentItem.setLocation(cmbLocation.getValue());
            currentItem.setDescription(safeTrim(txtDescription.getText()));
            
            currentItem.setAvailableQuantity(parseIntegerSafe(txtAvailableQty.getText()));
            currentItem.setReservedQuantity(parseIntegerSafe(txtReservedQty.getText()));
            currentItem.setMinStockLevel(parseIntegerSafe(txtMinStock.getText()));
            currentItem.setMaxStockLevel(parseIntegerSafe(txtMaxStock.getText(), 1000));
            
            currentItem.setUnitPrice(parseDecimalSafe(txtUnitPrice.getText()));
            currentItem.setUnit(cmbUnit.getValue() != null ? cmbUnit.getValue() : "pieces");
            currentItem.setSupplier(cmbSupplier.getValue());
            currentItem.setSupplierContact(safeTrim(txtSupplierContact.getText()));
            currentItem.setBarcode(safeTrim(txtBarcode.getText()));
            
            currentItem.setLastUpdated(LocalDateTime.now());
            currentItem.setUpdatedBy("current_user"); // Replace with actual logged-in user
            
            if (isEditMode) {
                currentItem.setActive(chkActive.isSelected());
            } else {
                currentItem.setActive(true);
            }
            
            // Save to database
            Task<Integer> saveTask = new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    return inventoryDAO.saveItem(currentItem);
                }
            };
            
            saveTask.setOnSucceeded(e -> {
                int itemId = saveTask.getValue();
                if (!isEditMode) {
                    currentItem.setItemId(itemId);
                }
                
                AlertUtils.showSaveSuccess("Inventory Item");
                
                // Refresh parent controller
                if (parentController != null) {
                    parentController.refreshInventoryData();
                }
                
                closeDialog();
            });
            
            saveTask.setOnFailed(e -> {
                Throwable exception = saveTask.getException();
                String message = exception instanceof SQLException ? 
                    exception.getMessage() : "An unexpected error occurred";
                AlertUtils.showSaveError("Inventory Item", message);
            });
            
            Thread thread = new Thread(saveTask);
            thread.setDaemon(true);
            thread.start();
            
        } catch (Exception e) {
            AlertUtils.showSaveError("Inventory Item", e.getMessage());
        }
    }

    private void clearForm() {
        txtItemName.clear();
        txtSKU.clear();
        cmbCategory.getSelectionModel().clearSelection();
        cmbLocation.getSelectionModel().clearSelection();
        txtDescription.clear();
        
        txtAvailableQty.clear();
        txtReservedQty.setText("0");
        txtMinStock.clear();
        txtMaxStock.clear();
        
        txtUnitPrice.clear();
        cmbUnit.setValue("pieces");
        cmbCurrency.setValue("INR");
        
        cmbSupplier.getSelectionModel().clearSelection();
        txtSupplierContact.clear();
        txtBarcode.clear();
        
        chkActive.setSelected(true);
        
        lblValidationStatus.setText("Ready to add new item");
        lblValidationStatus.setStyle("");
        updateCalculatedFields();
        
        // Clear error styles
        List<javafx.scene.Node> fields = List.of(txtItemName, txtUnitPrice, txtMinStock, txtMaxStock, txtSKU);
        ValidationUtils.clearErrorStyles(fields);
    }

    private boolean hasUnsavedChanges() {
        return !safeTrim(txtItemName.getText()).isEmpty() || 
               !safeTrim(txtUnitPrice.getText()).isEmpty() ||
               cmbCategory.getValue() != null;
    }

    private void validateField(TextField field, String fieldName, boolean required) {
        List<String> errors = new ArrayList<>();
        
        if (field == txtItemName) {
            ValidationUtils.validateRequired(field, fieldName, 2, errors);
        } else if (field == txtUnitPrice) {
            ValidationUtils.validatePositiveDecimal(field, fieldName, required, errors);
        } else if (field == txtMinStock) {
            ValidationUtils.validatePositiveInteger(field, fieldName, required, errors);
        }
    }

    private void addNumericValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void addDecimalValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                textField.setText(oldValue);
            }
        });
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    // Utility methods for null-safe operations
    private String safeTrim(String input) {
        return input != null ? input.trim() : "";
    }

    private int parseIntegerSafe(String input) {
        return parseIntegerSafe(input, 0);
    }

    private int parseIntegerSafe(String input, int defaultValue) {
        try {
            String trimmed = safeTrim(input);
            return trimmed.isEmpty() ? defaultValue : Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private BigDecimal parseDecimalSafe(String input) {
        try {
            String trimmed = safeTrim(input);
            return trimmed.isEmpty() ? BigDecimal.ZERO : new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean isValidSKU(String sku) {
        return sku != null && sku.matches("^[A-Z0-9]{3,20}$");
    }
}
