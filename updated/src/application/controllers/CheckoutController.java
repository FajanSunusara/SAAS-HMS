package application.controllers;

import application.models.ScheduledCheckout;
import application.services.dao.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CheckoutController implements Initializable {

    // Table and columns
    @FXML private TableView<ScheduledCheckout> scheduledCheckOutsTable;
    @FXML private TableColumn<ScheduledCheckout, String> bookingIdCol;
    @FXML private TableColumn<ScheduledCheckout, String> roomNoCol;
    @FXML private TableColumn<ScheduledCheckout, String> guestNameCol;
    @FXML private TableColumn<ScheduledCheckout, String> pendingPaymentCol;
    @FXML private TableColumn<ScheduledCheckout, String> statusCol;

    // Statistics Labels
    @FXML private Label availableRoomsLabel;
    @FXML private Label totalPendingAmountLabel;
    @FXML private Label totalCheckoutsLabel;
    @FXML private Label occupancyRateLabel;

    // Icons
    @FXML private ImageView notificationIconView;
    @FXML private ImageView userIconView;

    private ObservableList<ScheduledCheckout> checkoutsData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadIcons();
        loadData();
        
        scheduledCheckOutsTable.setItems(checkoutsData);
        scheduledCheckOutsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    showBookingBillingSummaryPopup(newValue);
                }
            }
        );
    }

    private void setupTableColumns() {
        bookingIdCol.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty());
        roomNoCol.setCellValueFactory(cellData -> cellData.getValue().roomNoProperty());
        guestNameCol.setCellValueFactory(cellData -> cellData.getValue().guestNameProperty());
        pendingPaymentCol.setCellValueFactory(cellData -> cellData.getValue().pendingPaymentProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void loadData() {
        loadScheduledCheckOuts();
        loadStatistics();
    }

    private void loadScheduledCheckOuts() {
        checkoutsData.clear();
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        String sql = """
            SELECT b.id as booking_id, b.room_no, g.name AS guest_name,
                   COALESCE(i.due_amount, 0) AS pending_payment,
                   b.status, b.check_out_date
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            LEFT JOIN invoices i ON b.id = i.booking_id
            WHERE b.status NOT IN ('Checked-out', 'Completed', 'Cancelled')
                  AND b.check_out_date BETWEEN ? AND ?
                  AND b.id = (
                      SELECT MAX(b2.id) 
                      FROM bookings b2 
                      WHERE b2.room_no = b.room_no 
                        AND b2.status NOT IN ('Checked-out', 'Completed', 'Cancelled')
                        AND b2.check_out_date BETWEEN ? AND ?
                  )
            ORDER BY b.check_out_date ASC, b.room_no
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Set parameters for both BETWEEN clauses
            ps.setDate(1, Date.valueOf(yesterday));  // Start date
            ps.setDate(2, Date.valueOf(today));      // End date
            ps.setDate(3, Date.valueOf(yesterday));  // Start date for subquery
            ps.setDate(4, Date.valueOf(today));      // End date for subquery
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String bookingId = String.valueOf(rs.getLong("booking_id"));
                    String roomNo = rs.getString("room_no");
                    String guestName = rs.getString("guest_name");
                    double pendingPayment = rs.getDouble("pending_payment");
                    String pendingPaymentStr = String.format("₹%.2f", pendingPayment);
                    String status = rs.getString("status");
                    LocalDate checkoutDate = rs.getDate("check_out_date").toLocalDate();

                    // Fetch additional details using the actual checkout date
                    String roomTypeNo = fetchRoomTypeAndNo(conn, roomNo);
                    String durationOfStay = fetchDurationOfStay(conn, roomNo, checkoutDate);
                    String totalAmount = fetchInvoiceTotal(conn, roomNo, checkoutDate);
                    String amountPaid = fetchInvoiceAmountPaid(conn, roomNo, checkoutDate);
                    String gstDiscounts = "GST Included";

                    ScheduledCheckout checkout = new ScheduledCheckout(
                        bookingId, roomNo, guestName, pendingPaymentStr, status,
                        roomTypeNo, durationOfStay, totalAmount, gstDiscounts,
                        amountPaid, ""
                    );

                    checkoutsData.add(checkout);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load checkout data: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try (Connection conn = DatabaseManager.getConnection()) {
            loadAvailableRoomsCount(conn);
            loadTotalPendingAmount(conn);
            loadTotalCheckoutsCount(conn);
            loadOccupancyRate(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load statistics: " + e.getMessage());
        }
    }

    private void loadAvailableRoomsCount(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE status = 'Available'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int availableRooms = rs.getInt(1);
                if (availableRoomsLabel != null) {
                    availableRoomsLabel.setText(availableRooms + " Available");
                }
            }
        }
    }

    private void loadTotalPendingAmount(Connection conn) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(i.due_amount), 0) as total_pending
            FROM bookings b
            JOIN invoices i ON b.id = i.booking_id
            WHERE b.check_out_date = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double totalPending = rs.getDouble("total_pending");
                    if (totalPendingAmountLabel != null) {
                        totalPendingAmountLabel.setText(String.format("₹%.2f Pending", totalPending));
                    }
                }
            }
        }
    }

    private void loadTotalCheckoutsCount(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE check_out_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int totalCheckouts = rs.getInt(1);
                    if (totalCheckoutsLabel != null) {
                        totalCheckoutsLabel.setText(totalCheckouts + " Checkouts");
                    }
                }
            }
        }
    }

    private void loadOccupancyRate(Connection conn) throws SQLException {
        String sqlTotal = "SELECT COUNT(*) FROM rooms";
        String sqlOccupied = "SELECT COUNT(*) FROM rooms WHERE status IN ('Occupied', 'Cleaning')";
        
        int totalRooms = 0;
        int occupiedRooms = 0;
        
        try (PreparedStatement psTotal = conn.prepareStatement(sqlTotal);
             ResultSet rsTotal = psTotal.executeQuery()) {
            if (rsTotal.next()) {
                totalRooms = rsTotal.getInt(1);
            }
        }
        
        try (PreparedStatement psOccupied = conn.prepareStatement(sqlOccupied);
             ResultSet rsOccupied = psOccupied.executeQuery()) {
            if (rsOccupied.next()) {
                occupiedRooms = rsOccupied.getInt(1);
            }
        }
        
        if (totalRooms > 0) {
            double occupancyRate = (double) occupiedRooms / totalRooms * 100;
            if (occupancyRateLabel != null) {
                occupancyRateLabel.setText(String.format("%.1f%% Occupied", occupancyRate));
            }
        }
    }

    private String fetchRoomTypeAndNo(Connection conn, String roomNo) throws SQLException {
        String sql = "SELECT room_type FROM rooms WHERE room_no = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("room_type") + "/" + roomNo;
                }
            }
        }
        return roomNo;
    }

    private String fetchDurationOfStay(Connection conn, String roomNo, LocalDate checkOutDate) throws SQLException {
        String sql = """
            SELECT check_in_date, check_out_date
            FROM bookings
            WHERE room_no = ? AND check_out_date = ?
            ORDER BY check_in_date DESC
            LIMIT 1
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNo);
            ps.setDate(2, Date.valueOf(checkOutDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                    LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();
                    long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
                    return nights + " Night" + (nights > 1 ? "s" : "") + " / " + checkOut.toString();
                }
            }
        }
        return "-";
    }

    private String fetchInvoiceTotal(Connection conn, String roomNo, LocalDate checkOutDate) throws SQLException {
        String sql = """
            SELECT i.total
            FROM bookings b
            JOIN invoices i ON b.id = i.booking_id
            WHERE b.room_no = ? AND b.check_out_date = ?
            ORDER BY b.check_in_date DESC
            LIMIT 1
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNo);
            ps.setDate(2, Date.valueOf(checkOutDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    return String.format("₹%.2f", total);
                }
            }
        }
        return "₹0.00";
    }

    private String fetchInvoiceAmountPaid(Connection conn, String roomNo, LocalDate checkOutDate) throws SQLException {
        String sql = """
            SELECT i.paid_amount
            FROM bookings b
            JOIN invoices i ON b.id = i.booking_id
            WHERE b.room_no = ? AND b.check_out_date = ?
            ORDER BY b.check_in_date DESC
            LIMIT 1
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNo);
            ps.setDate(2, Date.valueOf(checkOutDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double paid = rs.getDouble("paid_amount");
                    return String.format("₹%.2f", paid);
                }
            }
        }
        return "₹0.00";
    }

    private void showBookingBillingSummaryPopup(ScheduledCheckout checkout) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/SummaryPopup.fxml"));
            Parent root = loader.load();
            SummaryPopupController controller = loader.getController();
            controller.setCheckoutData(checkout);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UTILITY);
            popupStage.setTitle("Booking & Billing Summary - " + checkout.getGuestName());
            popupStage.setScene(new Scene(root));
            controller.setPopupStage(popupStage);

            try {
                root.getScene().getStylesheets().add(getClass().getResource("/css/checkout.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Could not load stylesheet: " + e.getMessage());
            }

            popupStage.showAndWait();
            
            // Refresh data after popup closes
            loadData();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load summary popup: " + e.getMessage());
        }
    }

    private void loadIcons() {
        loadIcon(notificationIconView, "/icons/bell_white.png");
        loadIcon(userIconView, "/icons/user_white.png");
    }

    private void loadIcon(ImageView imageView, String path) {
        if (imageView == null) return;
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                imageView.setImage(new Image(url.toExternalForm(), true));
            } else {
                System.err.println("Icon not found: " + path);
                imageView.setImage(createPlaceholderImage());
            }
        } catch (Exception ex) {
            System.err.println("Failed to load icon " + path + ": " + ex.getMessage());
            imageView.setImage(createPlaceholderImage());
        }
    }

    private Image createPlaceholderImage() {
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH5QgCDCYF2rJc5gAAAB10RVh0Q29tbWVudABDcmVhdGVkIHdpdGggVGhlIEdJTVAwLjE4LjYAAADeSURBVEjH7dVBCsMgEADgX83//2h9u7O6iI92h+lZ91t58sL5fE0wAAAAAACAeQ4E/AfgTwB+BOBPAH4E4E8A/gTgTwD+BOBPAH4E4E8A/gTgTwB+BOBPAH4E4E8A/gTgTwB+BOBPAH4E4E8A/gTgTwB+BOBPAH4E4E8A/gTgTwB+BOBPAH4E4E8A/gTgTwB+BOBPAH4E4E8A/gTgoA9L0tH1xV53LAAAAABJRU5ErkJggg==");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshData() {
        loadData();
    }
}
