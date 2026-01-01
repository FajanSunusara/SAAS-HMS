package application.controllers;

import application.models.FinanceModels.*;
import application.services.dao.FinanceDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for Daily Sheet Detail Window
 */
public class DailySheetDetailController {

    // ===== FXML COMPONENTS =====
    
    @FXML private DatePicker selectedDatePicker;
    @FXML private Button loadDataButton;
    @FXML private Button exportPdfButton;
    @FXML private Button exportExcelButton;
    @FXML private Label dateRangeLabel;

    // Table and Columns
    @FXML private TableView<DailySheetDetailRow> dailySheetDetailTable;
    @FXML private TableColumn<DailySheetDetailRow, String> roomNoColumn;
    @FXML private TableColumn<DailySheetDetailRow, String> roomStatusColumn;
    @FXML private TableColumn<DailySheetDetailRow, String> guestNameColumn;
    @FXML private TableColumn<DailySheetDetailRow, String> checkInColumn;
    @FXML private TableColumn<DailySheetDetailRow, String> checkOutColumn;
    @FXML private TableColumn<DailySheetDetailRow, String> todayAmountColumn;
    @FXML private TableColumn<DailySheetDetailRow, String> todayPaymentColumn;
    @FXML private TableColumn<DailySheetDetailRow, String> pendingAmountColumn;
    @FXML private TableColumn<DailySheetDetailRow, String> totalAmountColumn;
    @FXML private TableColumn<DailySheetDetailRow, String> paymentMethodsColumn; 

    // Summary Labels
    @FXML private Label occupiedRoomsLabel;
    @FXML private Label todayCollectionLabel;
    @FXML private Label totalPendingLabel;
    @FXML private Label totalRevenueLabel;

    // ===== DATA =====
    
    private ObservableList<DailySheetDetailRow> dailyDetailData = FXCollections.observableArrayList();
    private FinanceDAO financeDAO = new FinanceDAO();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    // ===== INITIALIZATION =====
    
    public void initialize() {
        initializeDatePicker();
        initializeTableColumns();
        loadTodayData();
    }

    private void initializeDatePicker() {
        selectedDatePicker.setValue(LocalDate.now());
        updateDateRangeLabel();
    }
    private void initializeTableColumns() {
        roomNoColumn.setCellValueFactory(data -> data.getValue().roomNoProperty());
        roomStatusColumn.setCellValueFactory(data -> data.getValue().roomStatusProperty());
        guestNameColumn.setCellValueFactory(data -> data.getValue().guestNameProperty());
        checkInColumn.setCellValueFactory(data -> data.getValue().checkInProperty());
        checkOutColumn.setCellValueFactory(data -> data.getValue().checkOutProperty());
        todayAmountColumn.setCellValueFactory(data -> data.getValue().todayAmountProperty());
        todayPaymentColumn.setCellValueFactory(data -> data.getValue().todayPaymentProperty());
        paymentMethodsColumn.setCellValueFactory(data -> data.getValue().paymentMethodsProperty()); // NEW
        pendingAmountColumn.setCellValueFactory(data -> data.getValue().pendingAmountProperty());
        totalAmountColumn.setCellValueFactory(data -> data.getValue().totalAmountProperty());

        dailySheetDetailTable.setItems(dailyDetailData);
    }

    // ===== DATA LOADING =====
    
    private void loadTodayData() {
        LocalDate selectedDate = selectedDatePicker.getValue();
        loadDataForDate(selectedDate);
    }

    private void loadDataForDate(LocalDate date) {
        Task<List<DailySheetDetailData>> loadTask = new Task<List<DailySheetDetailData>>() {
            @Override
            protected List<DailySheetDetailData> call() throws Exception {
                return financeDAO.getDailySheetDetailData(date);
            }

            @Override
            protected void succeeded() {
                List<DailySheetDetailData> data = getValue();
                Platform.runLater(() -> {
                    updateTableData(data);
                    updateSummary(data);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    getException().printStackTrace();
                    showErrorAlert("Data Loading Error", "Failed to load daily sheet data: " + getException().getMessage());
                });
            }
        };
        
        executorService.submit(loadTask);
    }

    private void updateTableData(List<DailySheetDetailData> data) {
        dailyDetailData.clear();
        
        for (DailySheetDetailData item : data) {
            DailySheetDetailRow row = new DailySheetDetailRow(
                item.getRoomNo(),
                item.getRoomStatus(),
                item.getGuestName() != null ? item.getGuestName() : "Not-Available",
                item.getCheckInDate() != null ? item.getCheckInDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-",
                item.getCheckOutDate() != null ? item.getCheckOutDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-",
                "₹" + String.format("%.2f", item.getTodayAmount().doubleValue()),
                "₹" + String.format("%.2f", item.getTodayPaymentReceived().doubleValue()),
                "₹" + String.format("%.2f", item.getPendingAmount().doubleValue()),
                "₹" + String.format("%.2f", item.getTotalAmount().doubleValue()),
                item.getPaymentMethods() != null ? item.getPaymentMethods() : "N/A" // NEW
            );
            dailyDetailData.add(row);
        }
    }

    private void updateSummary(List<DailySheetDetailData> data) {
        int occupiedRooms = (int) data.stream().filter(d -> "Occupied".equals(d.getRoomStatus())).count();
        
        BigDecimal todayCollection = data.stream()
            .map(DailySheetDetailData::getTodayPaymentReceived)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPending = data.stream()
            .map(DailySheetDetailData::getPendingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalRevenue = data.stream()
            .map(DailySheetDetailData::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        occupiedRoomsLabel.setText(String.valueOf(occupiedRooms));
        todayCollectionLabel.setText("₹" + String.format("%,.2f", todayCollection.doubleValue()));
        totalPendingLabel.setText("₹" + String.format("%,.2f", totalPending.doubleValue()));
        totalRevenueLabel.setText("₹" + String.format("%,.2f", totalRevenue.doubleValue()));
    }

    // ===== EVENT HANDLERS =====
    
    @FXML
    private void handleLoadData(ActionEvent event) {
        LocalDate selectedDate = selectedDatePicker.getValue();
        updateDateRangeLabel();
        loadDataForDate(selectedDate);
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        showInfoAlert("Export PDF", "PDF export functionality will be implemented here.");
    }

    @FXML
    private void handleExportExcel(ActionEvent event) {
        showInfoAlert("Export Excel", "Excel export functionality will be implemented here.");
    }

    // ===== UTILITY METHODS =====
    
    private void updateDateRangeLabel() {
        LocalDate selectedDate = selectedDatePicker.getValue();
        if (selectedDate.equals(LocalDate.now())) {
            dateRangeLabel.setText("Today - " + selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        } else {
            dateRangeLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
