package application.controllers;



import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import application.models.inventory.InventoryItem;
import application.models.inventory.InventoryTransaction;
import application.models.inventory.PurchaseOrder;
import application.models.inventory.RoomAssignment;
import application.services.dao.inventory.InventoryDAO;
import application.services.dao.inventory.InventoryTransactionDAO;
import application.services.dao.inventory.PurchaseOrderDAO;
import application.services.dao.inventory.RoomAssignmentDAO;
import application.utils.inventory.AlertUtils;

public class InventoryController {

    // FXML Components
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter, statusFilter;
    
    // Statistics Labels
    @FXML private Label lblTotalItems, lblLowStock, lblOutOfStock, lblPendingOrders;
    
    // Main Inventory Table
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, Integer> colItemId;
    @FXML private TableColumn<InventoryItem, String> colItemName, colCategory, colLocation, colStatus;
    @FXML private TableColumn<InventoryItem, Integer> colAvailableQty, colReservedQty, colMinStock;
    @FXML private TableColumn<InventoryItem, BigDecimal> colUnitPrice, colTotalValue;
    @FXML private TableColumn<InventoryItem, LocalDateTime> colLastUpdated;
    @FXML private TableColumn<InventoryItem, Void> colActions;
    
    // Transaction Table
    @FXML private TableView<InventoryTransaction> transactionTable;
    @FXML private TableColumn<InventoryTransaction, LocalDateTime> colTransDate;
    @FXML private TableColumn<InventoryTransaction, String> colTransId, colTransItem, colTransType, colTransLocation, colTransReference, colTransUser, colTransNotes;
    @FXML private TableColumn<InventoryTransaction, Integer> colTransQty;
    
    // Assignment Table
    @FXML private TableView<RoomAssignment> assignmentTable;
    @FXML private TableColumn<RoomAssignment, String> colAssignRoom, colAssignItem, colAssignStatus, colAssignedBy;
    @FXML private TableColumn<RoomAssignment, Integer> colAssignQty;
    @FXML private TableColumn<RoomAssignment, LocalDateTime> colAssignDate, colReturnDate;
    @FXML private TableColumn<RoomAssignment, Void> colAssignActions;
    
    // Purchase Order Table
    @FXML private TableView<PurchaseOrder> purchaseOrderTable;
    @FXML private TableColumn<PurchaseOrder, String> colPONumber, colPOVendor, colPOStatus;
    @FXML private TableColumn<PurchaseOrder, LocalDateTime> colPODate, colPOExpected;
    @FXML private TableColumn<PurchaseOrder, Integer> colPOItems;
    @FXML private TableColumn<PurchaseOrder, BigDecimal> colPOAmount;
    @FXML private TableColumn<PurchaseOrder, Void> colPOActions;
    
    // Action Buttons
    @FXML private Button btnAddItem, btnBulkImport, btnGenerateReport, btnRefresh;
    @FXML private Button btnNewTransaction, btnExportHistory;
    @FXML private Button btnAssignToRoom, btnBulkAssign;
    @FXML private Button btnCreatePO, btnAutoGenerate;

    // Data and DAOs
    private InventoryDAO inventoryDAO;
    private InventoryTransactionDAO transactionDAO;
    private RoomAssignmentDAO assignmentDAO;
    private PurchaseOrderDAO purchaseOrderDAO;
    
    private ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();
    private FilteredList<InventoryItem> filteredItems;
    private SortedList<InventoryItem> sortedItems;
    
    private ObservableList<InventoryTransaction> transactions = FXCollections.observableArrayList();
    private ObservableList<RoomAssignment> assignments = FXCollections.observableArrayList();
    private ObservableList<PurchaseOrder> purchaseOrders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initializeDAOs();
        setupTableColumns();
        setupFiltersAndSearch();
        setupButtonActions();
        loadData();
        updateStatistics();
    }

    private void initializeDAOs() {
        try {
            inventoryDAO = new InventoryDAO();
            transactionDAO = new InventoryTransactionDAO();
            assignmentDAO = new RoomAssignmentDAO();
            purchaseOrderDAO = new PurchaseOrderDAO();
        } catch (Exception e) {
            AlertUtils.showError("Database Error", "Failed to initialize database connections: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        // Inventory Table Columns
        colItemId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAvailableQty.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        colReservedQty.setCellValueFactory(new PropertyValueFactory<>("reservedQuantity"));
        colMinStock.setCellValueFactory(new PropertyValueFactory<>("minStockLevel"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        
        // Custom cell factories for formatted display
        colUnitPrice.setCellFactory(column -> new TableCell<InventoryItem, BigDecimal>() {
            private DecimalFormat format = new DecimalFormat("₹#,##0.00");
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : format.format(item));
            }
        });
        
        colTotalValue.setCellValueFactory(cellData -> {
            InventoryItem item = cellData.getValue();
            BigDecimal total = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getAvailableQuantity()));
            return Bindings.createObjectBinding(() -> total);
        });
        
        colTotalValue.setCellFactory(column -> new TableCell<InventoryItem, BigDecimal>() {
            private DecimalFormat format = new DecimalFormat("₹#,##0.00");
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : format.format(item));
            }
        });
        
        colLastUpdated.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));
        colLastUpdated.setCellFactory(column -> new TableCell<InventoryItem, LocalDateTime>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
        
        colStatus.setCellValueFactory(cellData -> {
            InventoryItem item = cellData.getValue();
            String status;
            if (item.getAvailableQuantity() == 0) {
                status = "Out of Stock";
            } else if (item.getAvailableQuantity() <= item.getMinStockLevel()) {
                status = "Low Stock";
            } else {
                status = "Available";
            }
            return new SimpleStringProperty(status);
        });
        
        // Actions column for inventory
        setupInventoryActionsColumn();
        
        // Transaction Table Columns
        colTransDate.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        colTransDate.setCellFactory(column -> new TableCell<InventoryTransaction, LocalDateTime>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
        
        colTransId.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colTransItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colTransType.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        colTransQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTransLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colTransReference.setCellValueFactory(new PropertyValueFactory<>("referenceNumber"));
        colTransUser.setCellValueFactory(new PropertyValueFactory<>("handledBy"));
        colTransNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        
        // Assignment Table Columns
        colAssignRoom.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        colAssignItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colAssignQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAssignDate.setCellValueFactory(new PropertyValueFactory<>("assignedDate"));
        colAssignStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAssignedBy.setCellValueFactory(new PropertyValueFactory<>("assignedBy"));
        colReturnDate.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        
        // Purchase Order Table Columns
        colPONumber.setCellValueFactory(new PropertyValueFactory<>("poNumber"));
        colPOVendor.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        colPODate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colPOExpected.setCellValueFactory(new PropertyValueFactory<>("expectedDate"));
        colPOItems.setCellValueFactory(new PropertyValueFactory<>("totalItems"));
        colPOAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colPOStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupInventoryActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<InventoryItem, Void>() {
            private final HBox actionBox = new HBox(5);
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final Button btnAdjust = new Button("Adjust");

            {
                btnEdit.getStyleClass().addAll("action-btn", "edit-btn");
                btnDelete.getStyleClass().addAll("action-btn", "delete-btn");
                btnAdjust.getStyleClass().addAll("action-btn", "adjust-btn");
                
                btnEdit.setOnAction(e -> editInventoryItem(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> deleteInventoryItem(getTableView().getItems().get(getIndex())));
                btnAdjust.setOnAction(e -> adjustInventoryItem(getTableView().getItems().get(getIndex())));
                
                actionBox.getChildren().addAll(btnEdit, btnAdjust, btnDelete);
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void setupFiltersAndSearch() {
        // Initialize filter lists
        filteredItems = new FilteredList<>(inventoryItems, p -> true);
        sortedItems = new SortedList<>(filteredItems);
        sortedItems.comparatorProperty().bind(inventoryTable.comparatorProperty());
        inventoryTable.setItems(sortedItems);
        
        // Setup category filter
        categoryFilter.setItems(FXCollections.observableArrayList(
            "All Categories", "Linen", "Toiletries", "Furniture", "Electronics", 
            "Cleaning Supplies", "Food & Beverage", "Maintenance", "Office Supplies"
        ));
        categoryFilter.setValue("All Categories");
        
        // Setup status filter
        statusFilter.setItems(FXCollections.observableArrayList(
            "All Status", "Available", "Low Stock", "Out of Stock"
        ));
        statusFilter.setValue("All Status");
        
        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        filteredItems.setPredicate(item -> {
            // Search filter
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchText = searchField.getText().toLowerCase();
                if (!item.getItemName().toLowerCase().contains(searchText) &&
                    !item.getCategory().toLowerCase().contains(searchText)) {
                    return false;
                }
            }
            
            // Category filter
            if (categoryFilter.getValue() != null && !categoryFilter.getValue().equals("All Categories")) {
                if (!item.getCategory().equals(categoryFilter.getValue())) {
                    return false;
                }
            }
            
            // Status filter
            if (statusFilter.getValue() != null && !statusFilter.getValue().equals("All Status")) {
                String status = getItemStatus(item);
                if (!status.equals(statusFilter.getValue())) {
                    return false;
                }
            }
            
            return true;
        });
        
        updateStatistics();
    }

    private String getItemStatus(InventoryItem item) {
        if (item.getAvailableQuantity() == 0) {
            return "Out of Stock";
        } else if (item.getAvailableQuantity() <= item.getMinStockLevel()) {
            return "Low Stock";
        } else {
            return "Available";
        }
    }

    private void setupButtonActions() {
        btnAddItem.setOnAction(e -> showAddEditItemDialog(null));
        btnBulkImport.setOnAction(e -> showBulkImportDialog());
        btnGenerateReport.setOnAction(e -> generateInventoryReport());
        btnRefresh.setOnAction(e -> refreshData());
        
        btnNewTransaction.setOnAction(e -> showNewTransactionDialog());
        btnExportHistory.setOnAction(e -> exportTransactionHistory());
        
        btnAssignToRoom.setOnAction(e -> showRoomAssignmentDialog());
        btnBulkAssign.setOnAction(e -> showBulkAssignmentDialog());
        
        btnCreatePO.setOnAction(e -> showCreatePurchaseOrderDialog());
        btnAutoGenerate.setOnAction(e -> autoGeneratePurchaseOrders());
    }

    private void loadData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load inventory items
                List<InventoryItem> items = inventoryDAO.getAllItems();
                Platform.runLater(() -> {
                    inventoryItems.clear();
                    inventoryItems.addAll(items);
                });
                
                // Load transactions
                List<InventoryTransaction> transactionList = transactionDAO.getRecentTransactions(100);
                Platform.runLater(() -> {
                    transactions.clear();
                    transactions.addAll(transactionList);
                    transactionTable.setItems(transactions);
                });
                
                // Load assignments
                List<RoomAssignment> assignmentList = assignmentDAO.getActiveAssignments();
                Platform.runLater(() -> {
                    assignments.clear();
                    assignments.addAll(assignmentList);
                    assignmentTable.setItems(assignments);
                });
                
                // Load purchase orders
                List<PurchaseOrder> poList = purchaseOrderDAO.getRecentPurchaseOrders();
                Platform.runLater(() -> {
                    purchaseOrders.clear();
                    purchaseOrders.addAll(poList);
                    purchaseOrderTable.setItems(purchaseOrders);
                });
                
                return null;
            }
        };
        
        loadTask.setOnSucceeded(e -> updateStatistics());
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            AlertUtils.showError("Data Loading Error", "Failed to load inventory data: " + 
                (exception != null ? exception.getMessage() : "Unknown error"));
        });
        
        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateStatistics() {
        int totalItems = inventoryItems.size();
        int lowStock = (int) inventoryItems.stream()
            .filter(item -> item.getAvailableQuantity() <= item.getMinStockLevel() && item.getAvailableQuantity() > 0)
            .count();
        int outOfStock = (int) inventoryItems.stream()
            .filter(item -> item.getAvailableQuantity() == 0)
            .count();
        int pendingOrders = (int) purchaseOrders.stream()
            .filter(po -> "Pending".equals(po.getStatus()) || "Ordered".equals(po.getStatus()))
            .count();
        
        lblTotalItems.setText(String.valueOf(totalItems));
        lblLowStock.setText(String.valueOf(lowStock));
        lblOutOfStock.setText(String.valueOf(outOfStock));
        lblPendingOrders.setText(String.valueOf(pendingOrders));
    }

    private void refreshData() {
        loadData();
        AlertUtils.showInfo("Data Refreshed", "Inventory data has been refreshed successfully.");
    }

    // Dialog Methods
    private void showAddEditItemDialog(InventoryItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/AddEditInventoryDialog.fxml"));
            VBox dialog = loader.load();
            
            AddEditInventoryDialogController controller = loader.getController();
            controller.setInventoryItem(item);
            controller.setParentController(this);
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(item == null ? "Add New Item" : "Edit Item");
            dialogStage.setScene(new Scene(dialog));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            AlertUtils.showError("Dialog Error", "Failed to open item dialog: " + e.getMessage());
        }
    }

    private void editInventoryItem(InventoryItem item) {
        showAddEditItemDialog(item);
    }

    private void deleteInventoryItem(InventoryItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Inventory Item");
        alert.setContentText("Are you sure you want to delete '" + item.getItemName() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                inventoryDAO.deleteItem(item.getItemId());
                inventoryItems.remove(item);
                updateStatistics();
                AlertUtils.showSuccess("Item Deleted", "Item has been deleted successfully.");
            } catch (SQLException e) {
                AlertUtils.showError("Delete Error", "Failed to delete item: " + e.getMessage());
            }
        }
    }

    private void adjustInventoryItem(InventoryItem item) {
        // Show stock adjustment dialog
        showStockAdjustmentDialog(item);
    }

    private void showStockAdjustmentDialog(InventoryItem item) {
        // Implementation for stock adjustment dialog
    }

    private void showNewTransactionDialog() {
        // Implementation for new transaction dialog
    }

    private void showRoomAssignmentDialog() {
        // Implementation for room assignment dialog
    }

    private void showBulkAssignmentDialog() {
        // Implementation for bulk assignment dialog
    }

    private void showCreatePurchaseOrderDialog() {
        // Implementation for purchase order dialog
    }

    private void showBulkImportDialog() {
        // Implementation for bulk import dialog
    }

    private void generateInventoryReport() {
        // Implementation for report generation
    }

    private void exportTransactionHistory() {
        // Implementation for transaction history export
    }

    private void autoGeneratePurchaseOrders() {
        // Implementation for auto PO generation based on low stock
    }

    // Public methods for dialog controllers to refresh data
    public void refreshInventoryData() {
        loadData();
    }

    public InventoryDAO getInventoryDAO() {
        return inventoryDAO;
    }

    public InventoryTransactionDAO getTransactionDAO() {
        return transactionDAO;
    }
}
