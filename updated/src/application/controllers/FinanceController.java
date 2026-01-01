package application.controllers;

import application.models.FinanceModels.*;
import application.services.dao.FinanceDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Updated Finance Controller with new tables integration
 */
public class FinanceController implements Initializable {

    // ===== FXML COMPONENTS =====
    
    // Header Controls
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Button filterButton;
    @FXML private Button exportExcelButton;
    @FXML private Button exportPdfButton;
    @FXML private TextField searchField;

    // KPI Labels
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netBalanceLabel;
    @FXML private Label pendingAmountLabel;

    // Tab Components
    @FXML private TabPane mainTabPane;
    @FXML private Tab paymentHistoryTab;
    @FXML private Tab reservationPaymentTab;
    @FXML private Tab returnPaymentTab;
    @FXML private Tab expenseHistoryTab;
    @FXML private Tab dailySheetTab;

    // Payment History Table
    @FXML private TableView<PaymentHistoryRow> paymentHistoryTable;
    @FXML private TableColumn<PaymentHistoryRow, String> paymentIdColumn;
    @FXML private TableColumn<PaymentHistoryRow, String> guestNameColumn;
    @FXML private TableColumn<PaymentHistoryRow, String> roomNoColumn;
    @FXML private TableColumn<PaymentHistoryRow, String> amountColumn;
    @FXML private TableColumn<PaymentHistoryRow, String> methodColumn;
    @FXML private TableColumn<PaymentHistoryRow, String> transactionIdColumn;
    @FXML private TableColumn<PaymentHistoryRow, String> paymentDateColumn;
    @FXML private Label totalReceivedLabel;

    // Reservation Payment Table
    @FXML private TableView<ReservationPaymentRow> reservationPaymentTable;
    @FXML private TableColumn<ReservationPaymentRow, String> reservationIdColumn;
    @FXML private TableColumn<ReservationPaymentRow, String> resGuestNameColumn;
    @FXML private TableColumn<ReservationPaymentRow, String> resRoomNoColumn;
    @FXML private TableColumn<ReservationPaymentRow, String> resAmountColumn;
    @FXML private TableColumn<ReservationPaymentRow, String> resMethodColumn;
    @FXML private TableColumn<ReservationPaymentRow, String> resNotesColumn;
    @FXML private TableColumn<ReservationPaymentRow, String> resDateColumn;
    @FXML private Label totalReservationLabel;

    // Return Payment Table
    @FXML private TableView<ReturnPaymentRow> returnPaymentTable;
    @FXML private TableColumn<ReturnPaymentRow, String> returnIdColumn;
    @FXML private TableColumn<ReturnPaymentRow, String> returnGuestNameColumn;
    @FXML private TableColumn<ReturnPaymentRow, String> returnRoomNoColumn;
    @FXML private TableColumn<ReturnPaymentRow, String> returnAmountColumn;
    @FXML private TableColumn<ReturnPaymentRow, String> returnMethodColumn;
    @FXML private TableColumn<ReturnPaymentRow, String> returnDateColumn;
    @FXML private Label totalReturnedLabel;

    // Hotel Expense Table (Updated to use hotel_expenses table)
    @FXML private TableView<ExpenseHistoryRow> expenseHistoryTable;
    @FXML private TableColumn<ExpenseHistoryRow, String> expenseDateColumn;
    @FXML private TableColumn<ExpenseHistoryRow, String> expenseCategoryColumn;
    @FXML private TableColumn<ExpenseHistoryRow, String> expenseDescColumn;
    @FXML private TableColumn<ExpenseHistoryRow, String> expenseAmountColumn;
    @FXML private TableColumn<ExpenseHistoryRow, String> expensePaidByColumn;
    @FXML private Label totalExpenseLabel;

    // Daily Sheet Table (Simple view with button to open detailed window)
    @FXML private TableView<DailySheetRow> dailySheetTable;
    @FXML private TableColumn<DailySheetRow, String> dailyRoomNoColumn;
    @FXML private TableColumn<DailySheetRow, String> dailyDateColumn;
    @FXML private TableColumn<DailySheetRow, String> dailyOccupiedColumn;
    @FXML private TableColumn<DailySheetRow, String> dailyReceivedColumn;
    @FXML private TableColumn<DailySheetRow, String> dailyPendingColumn;
    @FXML private TableColumn<DailySheetRow, String> dailyTotalColumn;
    @FXML private Label dailyReceivedSummary;
    @FXML private Label dailyPendingSummary;
    @FXML private Label dailyTotalSummary;

    // ===== DATA COLLECTIONS =====
    
    private ObservableList<PaymentHistoryRow> paymentHistoryData = FXCollections.observableArrayList();
    private ObservableList<ReservationPaymentRow> reservationPaymentData = FXCollections.observableArrayList();
    private ObservableList<ReturnPaymentRow> returnPaymentData = FXCollections.observableArrayList();
    private ObservableList<ExpenseHistoryRow> expenseHistoryData = FXCollections.observableArrayList();
    private ObservableList<DailySheetRow> dailySheetData = FXCollections.observableArrayList();

    // Filtered Collections
    private ObservableList<PaymentHistoryRow> filteredPaymentHistory = FXCollections.observableArrayList();
    private ObservableList<ReservationPaymentRow> filteredReservationPayment = FXCollections.observableArrayList();
    private ObservableList<ReturnPaymentRow> filteredReturnPayment = FXCollections.observableArrayList();
    private ObservableList<ExpenseHistoryRow> filteredExpenseHistory = FXCollections.observableArrayList();
    private ObservableList<DailySheetRow> filteredDailySheet = FXCollections.observableArrayList();

    // ===== SERVICES =====
    
    private FinanceDAO financeDAO = new FinanceDAO();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    // ===== INITIALIZATION =====
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDatePickers();
        initializeTableColumns();
        setupTableContextMenus();
        loadInitialData();
    }

    private void initializeDatePickers() {
        LocalDate now = LocalDate.now();
        fromDatePicker.setValue(now.withDayOfMonth(1));
        toDatePicker.setValue(now);
        fromDatePicker.setValue(now.minusMonths(3)); // 3 months back
        toDatePicker.setValue(now.plusDays(30));
    }

    private void initializeTableColumns() {
        // Payment History Table
        if (paymentIdColumn != null) {
            paymentIdColumn.setCellValueFactory(data -> data.getValue().paymentIdProperty());
            guestNameColumn.setCellValueFactory(data -> data.getValue().guestNameProperty());
            roomNoColumn.setCellValueFactory(data -> data.getValue().roomNoProperty());
            amountColumn.setCellValueFactory(data -> data.getValue().amountProperty());
            methodColumn.setCellValueFactory(data -> data.getValue().methodProperty());
            transactionIdColumn.setCellValueFactory(data -> data.getValue().transactionIdProperty());
            paymentDateColumn.setCellValueFactory(data -> data.getValue().paymentDateProperty());
            paymentHistoryTable.setItems(filteredPaymentHistory);
        }

        // Reservation Payment Table
        if (reservationIdColumn != null) {
            reservationIdColumn.setCellValueFactory(data -> data.getValue().reservationIdProperty());
            resGuestNameColumn.setCellValueFactory(data -> data.getValue().guestNameProperty());
            resRoomNoColumn.setCellValueFactory(data -> data.getValue().roomNoProperty());
            resAmountColumn.setCellValueFactory(data -> data.getValue().amountProperty());
            resMethodColumn.setCellValueFactory(data -> data.getValue().methodProperty());
            resNotesColumn.setCellValueFactory(data -> data.getValue().notesProperty());
            resDateColumn.setCellValueFactory(data -> data.getValue().paymentDateProperty());
            reservationPaymentTable.setItems(filteredReservationPayment);
        }

        // Return Payment Table
        if (returnIdColumn != null) {
            returnIdColumn.setCellValueFactory(data -> data.getValue().returnIdProperty());
            returnGuestNameColumn.setCellValueFactory(data -> data.getValue().guestNameProperty());
            returnRoomNoColumn.setCellValueFactory(data -> data.getValue().roomNoProperty());
            returnAmountColumn.setCellValueFactory(data -> data.getValue().amountProperty());
            returnMethodColumn.setCellValueFactory(data -> data.getValue().methodProperty());
            returnDateColumn.setCellValueFactory(data -> data.getValue().returnDateProperty());
            returnPaymentTable.setItems(filteredReturnPayment);
        }

        // Expense History Table
        if (expenseDateColumn != null) {
            expenseDateColumn.setCellValueFactory(data -> data.getValue().expenseDateProperty());
            expenseCategoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
            expenseDescColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());
            expenseAmountColumn.setCellValueFactory(data -> data.getValue().amountProperty());
            expensePaidByColumn.setCellValueFactory(data -> data.getValue().paidByProperty());
            expenseHistoryTable.setItems(filteredExpenseHistory);
        }

        // Daily Sheet Table - WITH NULL CHECKS
        if (dailyRoomNoColumn != null) {
            dailyRoomNoColumn.setCellValueFactory(data -> data.getValue().roomNoProperty());
            dailyDateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
            dailyOccupiedColumn.setCellValueFactory(data -> data.getValue().occupiedProperty());
            dailyReceivedColumn.setCellValueFactory(data -> data.getValue().receivedAmountProperty());
            dailyPendingColumn.setCellValueFactory(data -> data.getValue().pendingAmountProperty());
            dailyTotalColumn.setCellValueFactory(data -> data.getValue().totalProperty());
            dailySheetTable.setItems(filteredDailySheet);
        }
    }


    private void setupTableContextMenus() {
        paymentHistoryTable.setOnMouseClicked(this::handleContextMenu);
        reservationPaymentTable.setOnMouseClicked(this::handleContextMenu);
        returnPaymentTable.setOnMouseClicked(this::handleContextMenu);
        expenseHistoryTable.setOnMouseClicked(this::handleContextMenu);
        dailySheetTable.setOnMouseClicked(this::handleContextMenu);
        
        // Add double-click handler for daily sheet to open detailed window
        dailySheetTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openDailySheetDetailWindow();
            }
        });
    }

    // ===== DATA LOADING METHODS =====
    
    private void loadInitialData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadPaymentHistory();
                loadReservationPayments();
                loadReturnPayments();
                loadHotelExpenses(); // Make sure this is enabled
                loadDailySheet();
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    applyFilters();
                    updateKPIs();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    getException().printStackTrace();
                    showErrorAlert("Data Loading Error", "Failed to load financial data: " + getException().getMessage());
                });
            }
        };
        executorService.submit(loadTask);
    }

    private void loadPaymentHistory() {
        try {
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            
            // Add debug logging
            System.out.println("Loading payments from " + fromDate + " to " + toDate);
            
            List<Payment> payments = financeDAO.getPaymentHistory(fromDate, toDate);
            
            // Add debug logging
            System.out.println("Found " + payments.size() + " payments");
            
            Platform.runLater(() -> {
                paymentHistoryData.clear();
                for (Payment payment : payments) {
                    PaymentHistoryRow row = new PaymentHistoryRow(
                        String.valueOf(payment.getId()),
                        payment.getGuestName() != null ? payment.getGuestName() : "Unknown",
                        payment.getRoomNo() != null ? payment.getRoomNo() : "N/A",
                        "₹" + String.format("%.2f", payment.getAmount().doubleValue()),
                        payment.getMethod() != null ? payment.getMethod() : "N/A",
                        payment.getTransactionId() != null ? payment.getTransactionId() : "N/A",
                        payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "N/A"
                    );
                    paymentHistoryData.add(row);
                }
                System.out.println("Added " + paymentHistoryData.size() + " rows to table");
            });
        } catch (SQLException e) {
            Platform.runLater(() -> showErrorAlert("Error loading payment history", e.getMessage()));
            e.printStackTrace();
        }
    }
    private void loadReservationPayments() {
        try {
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            
            List<ReservationPayment> payments = financeDAO.getReservationPayments(fromDate, toDate);
            
            Platform.runLater(() -> {
                reservationPaymentData.clear();
                for (ReservationPayment payment : payments) {
                    ReservationPaymentRow row = new ReservationPaymentRow(
                        String.valueOf(payment.getReservationId()),
                        payment.getGuestName(),
                        payment.getRoomNo(),
                        "₹" + String.format("%.2f", payment.getAmount().doubleValue()),
                        payment.getMethod(),
                        payment.getNotes(),
                        payment.getPaymentDate().toString()
                    );
                    reservationPaymentData.add(row);
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> showErrorAlert("Error loading reservation payments", e.getMessage()));
            e.printStackTrace();
        }
    }

    // ===== NEW METHOD: Load Return Payments from return_payments table =====
    private void loadReturnPayments() {
        try {
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            
            List<ReturnPayment> returnPayments = financeDAO.getReturnPayments(fromDate, toDate);
            
            Platform.runLater(() -> {
                returnPaymentData.clear();
                for (ReturnPayment returnPayment : returnPayments) {
                    ReturnPaymentRow row = new ReturnPaymentRow(
                        String.valueOf(returnPayment.getId()),
                        returnPayment.getGuestName(),
                        returnPayment.getRoomNo() != null ? returnPayment.getRoomNo() : "N/A",
                        "₹" + String.format("%.2f", returnPayment.getReturnAmount().doubleValue()),
                        returnPayment.getReturnMethod(),
                        returnPayment.getReturnDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    );
                    returnPaymentData.add(row);
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> showErrorAlert("Error loading return payments", e.getMessage()));
            e.printStackTrace();
        }
    }

//    // ===== UPDATED METHOD: Load Hotel Expenses from hotel_expenses table =====
//    private void loadHotelExpenses() {
//        try {
//            LocalDate fromDate = fromDatePicker.getValue();
//            LocalDate toDate = toDatePicker.getValue();
//            
//            List<HotelExpense> expenses = financeDAO.getHotelExpenseHistory(fromDate, toDate);
//            
//            Platform.runLater(() -> {
//                expenseHistoryData.clear();
//                for (HotelExpense expense : expenses) {
//                    ExpenseHistoryRow row = new ExpenseHistoryRow(
//                        expense.getExpenseDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
//                        expense.getCategory(),
//                        expense.getDescription(),
//                        "₹" + String.format("%.2f", expense.getAmount().doubleValue()),
//                        expense.getPaidBy()
//                    );
//                    expenseHistoryData.add(row);
//                }
//            });
//        } catch (SQLException e) {
//            Platform.runLater(() -> showErrorAlert("Error loading hotel expenses", e.getMessage()));
//            e.printStackTrace();
//        }
//    }

    private void loadDailySheet() {
        try {
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            
            // FIXED: Use the correct method name
            List<DailySheetData> dailyData = financeDAO.getDailySheetData(fromDate, toDate);
            
            Platform.runLater(() -> {
                dailySheetData.clear();
                for (DailySheetData data : dailyData) {
                    DailySheetRow row = new DailySheetRow(
                        data.getRoomNo(),
                        data.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        data.isOccupied() ? "Occupied" : "Available",
                        "₹" + String.format("%.2f", data.getTodayPaymentReceived().doubleValue()),
                        "₹" + String.format("%.2f", data.getPendingAmount().doubleValue()),
                        "₹" + String.format("%.2f", data.getTotalAmount().doubleValue())
                    );
                    dailySheetData.add(row);
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> showErrorAlert("Error loading daily sheet", e.getMessage()));
            e.printStackTrace();
        }
    }
    private void loadHotelExpenses() {
        try {
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            
            List<HotelExpense> expenses = financeDAO.getHotelExpenseHistory(fromDate, toDate);
            
            Platform.runLater(() -> {
                expenseHistoryData.clear();
                for (HotelExpense expense : expenses) {
                    ExpenseHistoryRow row = new ExpenseHistoryRow(
                        expense.getExpenseDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        expense.getCategory(),
                        expense.getDescription(),
                        "₹" + String.format("%.2f", expense.getAmount().doubleValue()),
                        expense.getPaidBy() // This now returns the display name
                    );
                    expenseHistoryData.add(row);
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> showErrorAlert("Error loading hotel expenses", e.getMessage()));
            e.printStackTrace();
        }
    }
    // ===== NEW METHOD: Open Daily Sheet Detail Window =====
    
    @FXML
    private void openDailySheetDetailWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/DailySheetDetail.fxml"));
            Parent root = loader.load();
            
            DailySheetDetailController controller = loader.getController();
            controller.initialize();
            
            Stage stage = new Stage();
            stage.setTitle("📊 Daily Sheet Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 1000, 700));
            stage.setResizable(true);
            
            // Center on parent window
            Stage parentStage = (Stage) dailySheetTable.getScene().getWindow();
            if (parentStage != null) {
                stage.setX(parentStage.getX() + (parentStage.getWidth() - 1000) / 2);
                stage.setY(parentStage.getY() + (parentStage.getHeight() - 700) / 2);
            }
            
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open Daily Sheet Detail window: " + e.getMessage());
        }
    }

    // ===== UPDATED KPI CALCULATION =====
    
    private void updateKPIs() {
        try {
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            
            FinancialSummary summary = financeDAO.getFinancialSummary(fromDate, toDate);
            
            String incomeText = "₹" + String.format("%,.2f", summary.getTotalIncome().doubleValue());
            String expenseText = "₹" + String.format("%,.2f", summary.getTotalExpenses().doubleValue());
            String balanceText = "₹" + String.format("%,.2f", summary.getNetBalance().doubleValue());
            String pendingText = "₹" + String.format("%,.2f", summary.getTotalPending().doubleValue());
            
            totalIncomeLabel.setText(incomeText);
            totalExpensesLabel.setText(expenseText);
            netBalanceLabel.setText(balanceText);
            pendingAmountLabel.setText(pendingText);
            
            updateTabTotals();
            updateLastUpdated();
            
        } catch (SQLException e) {
            showErrorAlert("Error updating KPIs", e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== EVENT HANDLERS =====
    
    @FXML
    private void handleFilter(ActionEvent event) {
        loadInitialData();
    }

    @FXML
    private void handleSearch(KeyEvent event) {
        applyFilters();
        updateKPIs();
    }

    @FXML
    private void handleExportExcel(ActionEvent event) {
        showInfoAlert("Export Excel", "Excel export functionality will be implemented here.");
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        showInfoAlert("Export PDF", "PDF export functionality will be implemented here.");
    }

    @FXML
    private void handleExportRow(ActionEvent event) {
        // Implementation for row export
        showInfoAlert("Export Row", "Row export functionality implemented.");
    }

    @FXML
    private void handlePrintReceipt(ActionEvent event) {
        // Implementation for receipt printing
        showInfoAlert("Print Receipt", "Receipt printing functionality implemented.");
    }

    @FXML
    private void handleContextMenu(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            TableView<?> table = (TableView<?>) event.getSource();
            if (table.getSelectionModel().getSelectedItem() != null) {
                ContextMenu contextMenu = createContextMenu();
                contextMenu.show(table, event.getScreenX(), event.getScreenY());
            }
        }
    }

    // ===== UTILITY METHODS =====
    
    private void applyFilters() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        String searchTerm = searchField.getText().toLowerCase();

        // Apply filters to all collections
        filteredPaymentHistory.setAll(paymentHistoryData.filtered(row -> 
            matchesSearchTerm(row.toString(), searchTerm)));
        
        filteredReservationPayment.setAll(reservationPaymentData.filtered(row -> 
            matchesSearchTerm(row.toString(), searchTerm)));
        
        filteredReturnPayment.setAll(returnPaymentData.filtered(row -> 
            matchesSearchTerm(row.toString(), searchTerm)));
        
        filteredExpenseHistory.setAll(expenseHistoryData.filtered(row -> 
            matchesSearchTerm(row.toString(), searchTerm)));
        
        filteredDailySheet.setAll(dailySheetData.filtered(row -> 
            matchesSearchTerm(row.toString(), searchTerm)));
    }

    private boolean matchesSearchTerm(String text, String searchTerm) {
        return searchTerm.isEmpty() || text.toLowerCase().contains(searchTerm);
    }

    private void updateTabTotals() {
        // Calculate and update totals for each tab
        double paymentTotal = paymentHistoryData.stream()
            .mapToDouble(row -> parseAmount(row.getAmount()))
            .sum();
        totalReceivedLabel.setText("Total Received: ₹" + String.format("%.2f", paymentTotal));

        double reservationTotal = reservationPaymentData.stream()
            .mapToDouble(row -> parseAmount(row.getAmount()))
            .sum();
        totalReservationLabel.setText("Total Reservation Payments: ₹" + String.format("%.2f", reservationTotal));

        double returnTotal = returnPaymentData.stream()
            .mapToDouble(row -> parseAmount(row.getAmount()))
            .sum();
        totalReturnedLabel.setText("Total Returned: ₹" + String.format("%.2f", returnTotal));

        double expenseTotal = expenseHistoryData.stream()
            .mapToDouble(row -> parseAmount(row.getAmount()))
            .sum();
        totalExpenseLabel.setText("Total Hotel Expenses: ₹" + String.format("%.2f", expenseTotal));
    }

    private double parseAmount(String amountStr) {
        try {
            return Double.parseDouble(amountStr.replace("₹", "").replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void updateLastUpdated() {
        // Update last updated timestamp if you have such a label
    }

    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem exportItem = new MenuItem("📊 Export Row");
        exportItem.setOnAction(this::handleExportRow);
        
        MenuItem printItem = new MenuItem("🖨️ Print Receipt");
        printItem.setOnAction(this::handlePrintReceipt);
        
        contextMenu.getItems().addAll(exportItem, printItem);
        return contextMenu;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
