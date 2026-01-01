package application.controllers;

import application.models.Guest;
import application.models.PaymentHistoryEntry;
import application.models.ScheduledCheckout;
import application.services.dao.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuestDetailController {
    private static final Logger LOGGER = Logger.getLogger(GuestDetailController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CURRENCY_FORMAT = "₹%.2f";

    @FXML private Label guestNameLabel;
    @FXML private Label roomNoLabel;
    @FXML private Label bookingDateLabel;
    @FXML private Label checkoutDateLabel;
    @FXML private Label peopleCountLabel;
    @FXML private Label pendingAmountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label roomRateLabel;
    @FXML private Label stayingAmountLabel;
    @FXML private Label AmountPaid;
    @FXML private TableView<PaymentHistoryEntry> paymentHistoryTable;
    @FXML private TableColumn<PaymentHistoryEntry, String> paymentDateCol;
    @FXML private TableColumn<PaymentHistoryEntry, Double> paidAmountCol;
    @FXML private TableColumn<PaymentHistoryEntry, String> descriptionCol;

    private Stage dialogStage;
    private Guest currentGuest;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setGuestData(Guest guest) {
        this.currentGuest = guest;
        if (guest != null) {
            populateGuestData();
        }
    }

    /**
     * Populate UI with current guest data
     */
    private void populateGuestData() {
        if (currentGuest == null) return;
        
        guestNameLabel.setText("Guest Name: " + currentGuest.getName());
        roomNoLabel.setText("Room No: " + currentGuest.getRoomNumber());
        bookingDateLabel.setText("Check-in Date: " + currentGuest.getCheckInDate());
        checkoutDateLabel.setText("Check-out Date: " + currentGuest.getCheckOutDate());
        peopleCountLabel.setText("No. of People: " + currentGuest.getNumberOfPeople());
        pendingAmountLabel.setText(String.format("Pending Amount: " + CURRENCY_FORMAT, currentGuest.getPendingAmount()));
        totalAmountLabel.setText(String.format("Total Amount: " + CURRENCY_FORMAT, currentGuest.getTotalAmount()));
        
        AmountPaid.setText(String.format("Amount Paid: " + CURRENCY_FORMAT, currentGuest.getAdvancePaid()));
        stayingAmountLabel.setText(String.format("Stay Amount: " + CURRENCY_FORMAT, currentGuest.getStayAmount()));
        

        if (paymentHistoryTable != null) {
            paymentDateCol.setCellValueFactory(cd -> cd.getValue().dateProperty());
            paidAmountCol.setCellValueFactory(cd -> cd.getValue().amountProperty().asObject());
            descriptionCol.setCellValueFactory(cd -> cd.getValue().descriptionProperty());
            if (currentGuest.getPaymentHistory() != null) {
                paymentHistoryTable.setItems(currentGuest.getPaymentHistory());
            }
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) dialogStage.close();
    }

    /**
     * Opens the Payment page with current guest's booking context
     */
    @FXML
    private void handlePayment() {
        if (currentGuest == null) {
            showAlert(Alert.AlertType.WARNING, "Payment", "No guest selected.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Payment.fxml"));
            Parent root = loader.load();
            PaymentController paymentController = loader.getController();

            Long bookingId = fetchBookingIdForGuest(currentGuest.getId());
            if (bookingId == null) {
                showAlert(Alert.AlertType.ERROR, "Payment Error", "No active booking found for guest.");
                return;
            }

            paymentController.setBookingContext(bookingId, currentGuest.getId());

            Stage stage = new Stage();
            stage.setTitle("Payment - " + currentGuest.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (dialogStage != null) stage.initOwner(dialogStage);
            stage.showAndWait();

            // Refresh guest data after payment
            refreshGuestDataFromDatabase();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error opening payment window", ex);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open payment window: " + ex.getMessage());
        }
    }

    /**
     * ENHANCED ROOM CHANGE FUNCTIONALITY with Financial Calculations
     */
    @FXML
    private void handleRoomChange() {
        if (currentGuest == null) {
            showAlert(Alert.AlertType.WARNING, "Room Change", "No guest selected.");
            return;
        }

        try {
            openRoomChangeDialogWithFinancialCalculation();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in room change", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to process room change: " + e.getMessage());
        }
    }

    /**
     * Comprehensive Room Change Dialog with Financial Calculations
     */
    private void openRoomChangeDialogWithFinancialCalculation() throws Exception {
        Dialog<RoomChangeResult> dialog = new Dialog<>();
        dialog.setTitle("Room Change - " + currentGuest.getName());
        dialog.setHeaderText("Select new room and review financial adjustments");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Current room information
        Label currentRoomLabel = new Label("Current Room: " + currentGuest.getRoomNumber());
        currentRoomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        double currentRoomRate = getRoomRateByNumber(currentGuest.getRoomNumber());
        Label currentRateLabel = new Label(String.format("Current Rate: ₹%.2f per night", currentRoomRate));

        // Available rooms dropdown
        Label newRoomLabel = new Label("Select New Room:");
        ComboBox<String> roomComboBox = new ComboBox<>();
        
        // Load available rooms with rates
        loadAvailableRoomsForChange(roomComboBox, currentGuest);

        // Financial calculation labels
        Label newRateLabel = new Label("New Rate: ₹0.00 per night");
        Label rateDifferenceLabel = new Label("Rate Difference: ₹0.00");
        Label remainingNightsLabel = new Label("Remaining Nights: 0");
        Label adjustmentAmountLabel = new Label("Total Adjustment: ₹0.00");
        Label newTotalLabel = new Label("New Total Amount: ₹0.00");
        Label newPendingLabel = new Label("New Pending Amount: ₹0.00");

        // Style labels
        rateDifferenceLabel.setStyle("-fx-font-weight: bold;");
        adjustmentAmountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Reason for change
        Label reasonLabel = new Label("Reason for Change:");
        ComboBox<String> reasonCombo = new ComboBox<>();
        reasonCombo.getItems().addAll("Guest Request - Upgrade", "Guest Request - Downgrade", 
                                     "Maintenance Issue", "Room Defect", "Overbooking", "Other");
        reasonCombo.setValue("Guest Request - Upgrade");

        // Update calculations when room selection changes
        roomComboBox.setOnAction(e -> {
            String selectedRoom = roomComboBox.getValue();
            if (selectedRoom != null) {
                calculateRoomChangeAdjustment(selectedRoom, currentRoomRate,
                    newRateLabel, rateDifferenceLabel, remainingNightsLabel,
                    adjustmentAmountLabel, newTotalLabel, newPendingLabel);
            }
        });

        content.getChildren().addAll(
            currentRoomLabel, currentRateLabel, new Separator(),
            newRoomLabel, roomComboBox, newRateLabel,
            new Separator(),
            remainingNightsLabel, rateDifferenceLabel, adjustmentAmountLabel,
            new Separator(),
            newTotalLabel, newPendingLabel,
            new Separator(),
            reasonLabel, reasonCombo
        );

        dialog.getDialogPane().setContent(content);

        // Buttons
        ButtonType changeButtonType = new ButtonType("Change Room", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, cancelButtonType);

        Button changeButton = (Button) dialog.getDialogPane().lookupButton(changeButtonType);
        changeButton.setDisable(true);
        
        roomComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            changeButton.setDisable(newVal == null);
        });

        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                return new RoomChangeResult(
                    roomComboBox.getValue(),
                    reasonCombo.getValue(),
                    Double.parseDouble(newRateLabel.getText().replaceAll("[^\\d.]", "")),
                    Double.parseDouble(adjustmentAmountLabel.getText().replaceAll("[^\\d.-]", ""))
                );
            }
            return null;
        });

        Optional<RoomChangeResult> result = dialog.showAndWait();
        result.ifPresent(changeResult -> {
            try {
                processRoomChangeWithFinancialAdjustment(changeResult);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing room change", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to change room: " + e.getMessage());
            }
        });
    }

    /**
     * Room Change Result Class
     */
    private static class RoomChangeResult {
        private final String newRoomNo;
        private final String reason;
        private final double newRate;
        private final double adjustmentAmount;
        
        public RoomChangeResult(String newRoomNo, String reason, double newRate, double adjustmentAmount) {
            this.newRoomNo = newRoomNo;
            this.reason = reason;
            this.newRate = newRate;
            this.adjustmentAmount = adjustmentAmount;
        }
        
        public String getNewRoomNo() { return newRoomNo; }
        public String getReason() { return reason; }
        public double getNewRate() { return newRate; }
        public double getAdjustmentAmount() { return adjustmentAmount; }
    }

    /**
     * Calculate Room Change Financial Adjustment
     */
    private void calculateRoomChangeAdjustment(String newRoomNo, double currentRate,
                                             Label newRateLabel, Label rateDifferenceLabel, 
                                             Label remainingNightsLabel, Label adjustmentAmountLabel,
                                             Label newTotalLabel, Label newPendingLabel) {
        try {
            double newRate = getRoomRateByNumber(newRoomNo);
            double rateDifference = newRate - currentRate;
            
            // Calculate remaining nights from today to checkout
            LocalDate today = LocalDate.now();
            LocalDate checkOut = currentGuest.getCheckOutDate();
            long remainingNights = ChronoUnit.DAYS.between(today, checkOut);
            remainingNights = Math.max(0, remainingNights);
            
            // Calculate total adjustment
            double totalAdjustment = rateDifference * remainingNights;
            
            // Calculate new totals
            double currentTotal = currentGuest.getTotalAmount();
            double newTotal = currentTotal + totalAdjustment;
            double currentPaid = currentTotal - currentGuest.getPendingAmount();
            double newPending = Math.max(0, newTotal - currentPaid);
            
            // Update labels
            newRateLabel.setText(String.format("New Rate: ₹%.2f per night", newRate));
            remainingNightsLabel.setText("Remaining Nights: " + remainingNights);
            
            if (rateDifference > 0) {
                rateDifferenceLabel.setText(String.format("Rate Difference: +₹%.2f (UPGRADE)", rateDifference));
                rateDifferenceLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                adjustmentAmountLabel.setText(String.format("Additional Amount: ₹%.2f", Math.abs(totalAdjustment)));
                adjustmentAmountLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else if (rateDifference < 0) {
                rateDifferenceLabel.setText(String.format("Rate Difference: ₹%.2f (DOWNGRADE)", rateDifference));
                rateDifferenceLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                adjustmentAmountLabel.setText(String.format("Credit Amount: ₹%.2f", Math.abs(totalAdjustment)));
                adjustmentAmountLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else {
                rateDifferenceLabel.setText("Rate Difference: ₹0.00 (SAME RATE)");
                rateDifferenceLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-weight: bold;");
                adjustmentAmountLabel.setText("No Adjustment Required");
                adjustmentAmountLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
            
            newTotalLabel.setText(String.format("New Total Amount: ₹%.2f", newTotal));
            newPendingLabel.setText(String.format("New Pending Amount: ₹%.2f", newPending));
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error calculating room change adjustment", e);
            newRateLabel.setText("New Rate: Error");
            adjustmentAmountLabel.setText("Adjustment: Error calculating");
        }
    }

    /**
     * Load Available Rooms for Change
     */
    private void loadAvailableRoomsForChange(ComboBox<String> roomComboBox, Guest guest) throws SQLException {
        roomComboBox.getItems().clear();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = """
                SELECT r.room_no, r.room_type, r.price, r.status
                FROM rooms r
                WHERE r.room_no != ? 
                AND (r.status = 'Available' OR r.status = 'Cleaning')
                AND r.room_no NOT IN (
                    SELECT b.room_no FROM bookings b 
                    WHERE b.status IN ('Checked-in', 'Confirmed')
                    AND (? < b.check_out_date AND ? > b.check_in_date)
                    AND b.room_no != ?
                )
                ORDER BY r.room_type, r.room_no
                """;
                
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, guest.getRoomNumber());
                ps.setDate(2, Date.valueOf(guest.getCheckOutDate()));
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.setString(4, guest.getRoomNumber());
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String roomInfo = String.format("%s (%s - ₹%.2f)", 
                            rs.getString("room_no"),
                            rs.getString("room_type"),
                            rs.getDouble("price"));
                        roomComboBox.getItems().add(rs.getString("room_no"));
                    }
                }
            }
        }
    }

    /**
     * Process Room Change with Financial Adjustment
     */
    private void processRoomChangeWithFinancialAdjustment(RoomChangeResult changeResult) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                Long bookingId = fetchBookingIdForGuest(currentGuest.getId());
                if (bookingId == null) {
                    throw new SQLException("No active booking found for guest.");
                }
                
                String oldRoomNo = currentGuest.getRoomNumber();
                String newRoomNo = changeResult.getNewRoomNo();
                double adjustmentAmount = changeResult.getAdjustmentAmount();
                
                // Check new room availability
                if (!isRoomAvailableForChange(conn, newRoomNo, currentGuest.getCheckInDate(), 
                                            currentGuest.getCheckOutDate(), bookingId)) {
                    throw new SQLException("Selected room is not available for the booking dates.");
                }
                
                // Update booking room number and total amount
                updateBookingForRoomChange(conn, bookingId, newRoomNo, adjustmentAmount);
                
                // Update room statuses
                updateRoomStatusAfterChange(conn, oldRoomNo, newRoomNo);
                
                // Update invoice with room change details
                updateInvoiceForRoomChange(conn, bookingId, adjustmentAmount, oldRoomNo, newRoomNo);
                
                // Log room change history
                logRoomChangeHistory(conn, bookingId, oldRoomNo, newRoomNo, 
                                   changeResult.getReason(), adjustmentAmount);
                
                conn.commit();
                
                // Update UI
                currentGuest.setRoomNumber(newRoomNo);
                currentGuest.setTotalAmount(currentGuest.getTotalAmount() + adjustmentAmount);
                currentGuest.setPendingAmount(Math.max(0, currentGuest.getPendingAmount() + adjustmentAmount));
                populateGuestData();
                
                // Handle payment for adjustment
                if (adjustmentAmount > 0.01) {
                    showRoomChangePaymentOption(adjustmentAmount, newRoomNo);
                } else if (adjustmentAmount < -0.01) {
                    showRoomChangeCreditInfo(Math.abs(adjustmentAmount), newRoomNo);
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Room Change Successful", 
                         String.format("Room changed from %s to %s for %s.\nAdjustment: ₹%.2f", 
                         oldRoomNo, newRoomNo, currentGuest.getName(), adjustmentAmount));
                         
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Update Booking for Room Change
     */
    private void updateBookingForRoomChange(Connection conn, Long bookingId, String newRoomNo, 
                                          double adjustmentAmount) throws SQLException {
        String sql = """
            UPDATE bookings 
            SET room_no = ?, 
                total_amount = total_amount + ?,
                payment_status = CASE
                    WHEN (total_amount + ?) <= COALESCE(advance_paid, 0) THEN 'Paid'
                    WHEN COALESCE(advance_paid, 0) > 0 THEN 'Partial'
                    ELSE 'Pending'
                END
            WHERE id = ?
            """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRoomNo);
            ps.setBigDecimal(2, BigDecimal.valueOf(adjustmentAmount));
            ps.setBigDecimal(3, BigDecimal.valueOf(adjustmentAmount));
            ps.setLong(4, bookingId);
            
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Failed to update booking for room change");
            }
        }
    }

    /**
     * Update Room Status After Change
     */
    private void updateRoomStatusAfterChange(Connection conn, String oldRoomNo, String newRoomNo) throws SQLException {
        // Set old room to Cleaning
        String updateOldRoomSql = "UPDATE rooms SET status = 'Cleaning' WHERE room_no = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateOldRoomSql)) {
            ps.setString(1, oldRoomNo);
            ps.executeUpdate();
        }
        
        // Set new room to Occupied
        String updateNewRoomSql = "UPDATE rooms SET status = 'Occupied' WHERE room_no = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateNewRoomSql)) {
            ps.setString(1, newRoomNo);
            ps.executeUpdate();
        }
    }

    /**
     * Update Invoice for Room Change
     */
    private void updateInvoiceForRoomChange(Connection conn, Long bookingId, double adjustmentAmount,
                                          String oldRoomNo, String newRoomNo) throws SQLException {
        String sql = """
            UPDATE invoices 
            SET subtotal = subtotal + ?,
                gst = (subtotal + ?) * 0.18,
                total = (subtotal + ?) + ((subtotal + ?) * 0.18),
                due_amount = GREATEST(0, 
                    ((subtotal + ?) + ((subtotal + ?) * 0.18)) - COALESCE(paid_amount, 0)
                ),
                invoice_date = CURRENT_DATE
            WHERE booking_id = ?
            """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, BigDecimal.valueOf(adjustmentAmount));
            ps.setBigDecimal(2, BigDecimal.valueOf(adjustmentAmount));
            ps.setBigDecimal(3, BigDecimal.valueOf(adjustmentAmount));
            ps.setBigDecimal(4, BigDecimal.valueOf(adjustmentAmount));
            ps.setBigDecimal(5, BigDecimal.valueOf(adjustmentAmount));
            ps.setBigDecimal(6, BigDecimal.valueOf(adjustmentAmount));
            ps.setLong(7, bookingId);
            
            ps.executeUpdate();
        }
    }

    /**
     * Room Change History Logging
     */
    private void logRoomChangeHistory(Connection conn, Long bookingId, String oldRoomNo, 
                                    String newRoomNo, String reason, double adjustmentAmount) throws SQLException {
        String sql = """
            INSERT INTO room_change_history 
            (booking_id, old_room_no, new_room_no, change_reason, adjustment_amount, change_date, created_by)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'SYSTEM')
            """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setString(2, oldRoomNo);
            ps.setString(3, newRoomNo);
            ps.setString(4, reason);
            ps.setBigDecimal(5, BigDecimal.valueOf(adjustmentAmount));
            ps.executeUpdate();
        } catch (SQLException e) {
            // Log the history attempt but don't fail the transaction
            LOGGER.log(Level.WARNING, "Failed to log room change history", e);
        }
    }

    /**
     * Check Room Availability for Change
     */
    private boolean isRoomAvailableForChange(Connection conn, String roomNo, 
                                           LocalDate checkIn, LocalDate checkOut, Long currentBookingId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM bookings
            WHERE room_no = ?
            AND id != ?
            AND status IN ('Checked-in', 'Confirmed')
            AND (? < check_out_date) AND (? > check_in_date)
            """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNo);
            ps.setLong(2, currentBookingId);
            ps.setDate(3, Date.valueOf(checkOut));
            ps.setDate(4, Date.valueOf(checkIn));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    /**
     * Handle Room Change Payment Option
     */
    private void showRoomChangePaymentOption(double additionalAmount, String newRoomNo) {
        Alert paymentAlert = new Alert(Alert.AlertType.CONFIRMATION);
        paymentAlert.setTitle("Room Change - Additional Payment Required");
        paymentAlert.setHeaderText("Room upgrade requires additional payment");
        paymentAlert.setContentText(String.format(
            "New Room: %s\nAdditional Amount: ₹%.2f\n\nWould you like to process payment now?",
            newRoomNo, additionalAmount));
        
        ButtonType payNowButton = new ButtonType("Pay Now", ButtonBar.ButtonData.YES);
        ButtonType payLaterButton = new ButtonType("Pay Later", ButtonBar.ButtonData.NO);
        paymentAlert.getButtonTypes().setAll(payNowButton, payLaterButton);
        
        Optional<ButtonType> result = paymentAlert.showAndWait();
        if (result.isPresent() && result.get() == payNowButton) {
            handlePayment();
        }
    }

    /**
     * Show Room Change Credit Information
     */
    private void showRoomChangeCreditInfo(double creditAmount, String newRoomNo) {
        Alert creditAlert = new Alert(Alert.AlertType.INFORMATION);
        creditAlert.setTitle("Room Change - Credit Applied");
        creditAlert.setHeaderText("Room downgrade credit applied");
        creditAlert.setContentText(String.format(
            "New Room: %s\nCredit Amount: ₹%.2f\n\nThe credit has been applied to your booking.",
            newRoomNo, creditAmount));
        creditAlert.showAndWait();
    }

    /**
     * EXISTING EXTEND STAY FUNCTIONALITY
     */
    @FXML
    private void handleExtendStay() {
        if (currentGuest == null) {
            showAlert(Alert.AlertType.WARNING, "Extend Stay", "No guest selected.");
            return;
        }

        try {
            openExtendStayDialog();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in extend stay", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to process extend stay request: " + e.getMessage());
        }
    }

    private void openExtendStayDialog() throws IOException {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Extend Stay - " + currentGuest.getName());
        dialog.setHeaderText("Select new checkout date and review charges");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Current checkout date
        Label currentCheckoutLabel = new Label("Current Checkout: " +
                currentGuest.getCheckOutDate().format(DATE_FORMATTER));
        currentCheckoutLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // New checkout date picker
        Label newCheckoutLabel = new Label("New Checkout Date:");
        DatePicker newCheckoutPicker = new DatePicker();
        newCheckoutPicker.setValue(currentGuest.getCheckOutDate().plusDays(1));
        newCheckoutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(currentGuest.getCheckOutDate()) ||
                        date.equals(currentGuest.getCheckOutDate()));
            }
        });

        // Cost calculation labels
        Label currentTotalLabel = new Label(String.format("Current Total: " + CURRENCY_FORMAT,
                currentGuest.getTotalAmount()));
        Label additionalAmountLabel = new Label("Additional Amount: ₹0.00");
        Label newTotalLabel = new Label("New Total: ₹0.00");
        Label pendingAmountLabel = new Label(String.format("Currently Pending: " + CURRENCY_FORMAT,
                currentGuest.getPendingAmount()));

        // Style labels
        additionalAmountLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
        newTotalLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Update calculations when date changes
        newCheckoutPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                calculateExtendedStayAmounts(newDate, additionalAmountLabel, newTotalLabel);
            }
        });

        // Initial calculation
        calculateExtendedStayAmounts(newCheckoutPicker.getValue(), additionalAmountLabel, newTotalLabel);

        content.getChildren().addAll(
                currentCheckoutLabel,
                new Separator(),
                newCheckoutLabel,
                newCheckoutPicker,
                new Separator(),
                currentTotalLabel,
                additionalAmountLabel,
                newTotalLabel,
                pendingAmountLabel
        );

        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType extendButtonType = new ButtonType("Extend Stay", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(extendButtonType, cancelButtonType);

        // Enable/Disable extend button based on valid date
        Button extendButton = (Button) dialog.getDialogPane().lookupButton(extendButtonType);
        extendButton.setDisable(true);

        newCheckoutPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            extendButton.setDisable(newDate == null ||
                    !newDate.isAfter(currentGuest.getCheckOutDate()));
        });

        // Set result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == extendButtonType) {
                return newCheckoutPicker.getValue();
            }
            return null;
        });

        // Show dialog and process result
        Optional<LocalDate> result = dialog.showAndWait();
        result.ifPresent(newCheckoutDate -> {
            try {
                processExtendStay(newCheckoutDate);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing extend stay", e);
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to extend stay: " + e.getMessage());
            }
        });
    }

    private void calculateExtendedStayAmounts(LocalDate newCheckoutDate,
                                            Label additionalAmountLabel,
                                            Label newTotalLabel) {
        if (newCheckoutDate == null || currentGuest == null) return;

        try {
            long currentNights = ChronoUnit.DAYS.between(
                    currentGuest.getCheckInDate(), currentGuest.getCheckOutDate());
            long newNights = ChronoUnit.DAYS.between(
                    currentGuest.getCheckInDate(), newCheckoutDate);
            long additionalNights = newNights - currentNights;

            if (additionalNights <= 0) {
                additionalAmountLabel.setText("Additional Amount: ₹0.00");
                newTotalLabel.setText(String.format("New Total: " + CURRENCY_FORMAT,
                        currentGuest.getTotalAmount()));
                return;
            }

            // Get room rate from database
            double roomRate = getRoomRateByNumber(currentGuest.getRoomNumber());

            // Calculate additional charges
            double additionalRoomCharges = roomRate * additionalNights;
            double additionalServiceCharges = additionalRoomCharges * 0.05; // 5% service charge
            double additionalSubtotal = additionalRoomCharges + additionalServiceCharges;
            double additionalGst = additionalSubtotal * 0.18; // 18% GST
            double totalAdditionalAmount = additionalSubtotal + additionalGst;

            double newTotalAmount = currentGuest.getTotalAmount() + totalAdditionalAmount;

            additionalAmountLabel.setText(String.format("Additional Amount: " + CURRENCY_FORMAT +
                    " (%d nights × ₹%.2f)", totalAdditionalAmount, additionalNights, roomRate));

            newTotalLabel.setText(String.format("New Total: " + CURRENCY_FORMAT, newTotalAmount));

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error calculating extended stay amounts", e);
            additionalAmountLabel.setText("Additional Amount: Error calculating");
            newTotalLabel.setText("New Total: Error calculating");
        }
    }

    private void processExtendStay(LocalDate newCheckoutDate) throws SQLException {
        long currentNights = ChronoUnit.DAYS.between(
                currentGuest.getCheckInDate(), currentGuest.getCheckOutDate());
        long newNights = ChronoUnit.DAYS.between(
                currentGuest.getCheckInDate(), newCheckoutDate);
        long additionalNights = newNights - currentNights;

        if (additionalNights <= 0) {
            showAlert(Alert.AlertType.WARNING, "Invalid Extension",
                    "New checkout date must be after current checkout date.");
            return;
        }

        // Get room rate and calculate additional amount
        double roomRate = getRoomRateByNumber(currentGuest.getRoomNumber());
        double additionalRoomCharges = roomRate * additionalNights;
        double additionalServiceCharges = additionalRoomCharges * 0.05;
        double additionalSubtotal = additionalRoomCharges + additionalServiceCharges;
        double additionalGst = additionalSubtotal * 0.18;
        double totalAdditionalAmount = additionalSubtotal + additionalGst;
        double newTotalAmount = currentGuest.getTotalAmount() + totalAdditionalAmount;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long bookingId = fetchBookingIdForGuest(currentGuest.getId());
                if (bookingId == null) {
                    throw new SQLException("No active booking found for guest.");
                }

                // Check room availability for extended period
                if (!isRoomAvailableForExtension(conn, currentGuest.getRoomNumber(),
                        currentGuest.getCheckOutDate(), newCheckoutDate, bookingId)) {
                    throw new SQLException("Room is not available for the extended period. " +
                            "There are conflicting bookings or reservations.");
                }

                // Update booking record
                updateBookingForExtendedStay(conn, bookingId, newCheckoutDate, newTotalAmount);

                // Update invoice record
                updateInvoiceForExtendedStay(conn, bookingId,
                        additionalSubtotal, additionalGst, totalAdditionalAmount);

                conn.commit();

                // Update current guest object
                currentGuest.setCheckOutDate(newCheckoutDate);
                currentGuest.setTotalAmount(newTotalAmount);
                currentGuest.setPendingAmount(currentGuest.getPendingAmount() + totalAdditionalAmount);

                // Refresh UI
                populateGuestData();

                // Show success message and ask about payment
                showExtensionSuccessAndPaymentOption(totalAdditionalAmount);

                LOGGER.info("Successfully extended stay for guest: " + currentGuest.getName() +
                        " to " + newCheckoutDate + ", additional amount: ₹" + totalAdditionalAmount);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private boolean isRoomAvailableForExtension(Connection conn, String roomNo,
                                              LocalDate currentCheckout, LocalDate newCheckout,
                                              Long currentBookingId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM (
                SELECT 1 FROM bookings
                WHERE room_no = ?
                AND id != ?
                AND status IN ('Confirmed', 'Checked-in', 'Reserved')
                AND ((check_in_date < ? AND check_out_date > ?)
                OR (check_in_date >= ? AND check_in_date < ?))
                UNION ALL
                SELECT 1 FROM reservations
                WHERE room_no = ?
                AND status IN ('Confirmed', 'Pending')
                AND ((start_date < ? AND end_date > ?)
                OR (start_date >= ? AND start_date < ?))
            ) AS conflicts
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            // Bookings check
            ps.setString(idx++, roomNo);
            ps.setLong(idx++, currentBookingId);
            ps.setDate(idx++, Date.valueOf(newCheckout));
            ps.setDate(idx++, Date.valueOf(currentCheckout));
            ps.setDate(idx++, Date.valueOf(currentCheckout));
            ps.setDate(idx++, Date.valueOf(newCheckout));
            // Reservations check
            ps.setString(idx++, roomNo);
            ps.setDate(idx++, Date.valueOf(newCheckout));
            ps.setDate(idx++, Date.valueOf(currentCheckout));
            ps.setDate(idx++, Date.valueOf(currentCheckout));
            ps.setDate(idx++, Date.valueOf(newCheckout));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    private void updateBookingForExtendedStay(Connection conn, Long bookingId,
                                            LocalDate newCheckoutDate, double newTotalAmount) throws SQLException {
        String sql = """
            UPDATE bookings
            SET check_out_date = ?,
                total_amount = ?,
                payment_status = CASE
                    WHEN COALESCE(advance_paid, 0) >= ? THEN 'Paid'
                    WHEN COALESCE(advance_paid, 0) > 0 THEN 'Partial'
                    ELSE 'Pending'
                END
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(newCheckoutDate));
            ps.setBigDecimal(2, BigDecimal.valueOf(newTotalAmount));
            ps.setBigDecimal(3, BigDecimal.valueOf(newTotalAmount));
            ps.setLong(4, bookingId);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No booking found to update for booking ID: " + bookingId);
            }
        }
    }

    private void updateInvoiceForExtendedStay(Connection conn, Long bookingId,
                                            double additionalSubtotal, double additionalGst,
                                            double totalAdditionalAmount) throws SQLException {
        String sql = """
            UPDATE invoices
            SET subtotal = subtotal + ?,
                gst = gst + ?,
                total = total + ?,
                due_amount = GREATEST(0, (total + ?) - COALESCE(paid_amount, 0)),
                invoice_date = CURRENT_DATE
            WHERE booking_id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, BigDecimal.valueOf(additionalSubtotal));
            ps.setBigDecimal(2, BigDecimal.valueOf(additionalGst));
            ps.setBigDecimal(3, BigDecimal.valueOf(totalAdditionalAmount));
            ps.setBigDecimal(4, BigDecimal.valueOf(totalAdditionalAmount));
            ps.setLong(5, bookingId);
            ps.executeUpdate();
        }
    }

    private void showExtensionSuccessAndPaymentOption(double additionalAmount) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Stay Extended Successfully");
        successAlert.setHeaderText("Stay has been extended for " + currentGuest.getName());
        successAlert.setContentText(String.format(
                "New checkout date: %s\nAdditional amount: " + CURRENCY_FORMAT +
                        "\n\nWould you like to process payment for the additional amount now?",
                currentGuest.getCheckOutDate().format(DATE_FORMATTER),
                additionalAmount));

        ButtonType payNowButton = new ButtonType("Pay Now", ButtonBar.ButtonData.YES);
        ButtonType payLaterButton = new ButtonType("Pay Later", ButtonBar.ButtonData.NO);
        successAlert.getButtonTypes().setAll(payNowButton, payLaterButton);

        Optional<ButtonType> result = successAlert.showAndWait();
        if (result.isPresent() && result.get() == payNowButton) {
            handlePayment();
        }
    }

    /**
     * Opens the summary popup for guest checkout
     */
    @FXML
    private void handleCheckout() {
        if (currentGuest == null) {
            showAlert(Alert.AlertType.WARNING, "Checkout", "No guest selected.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/SummaryPopup.fxml"));
            Parent root = loader.load();
            SummaryPopupController controller = loader.getController();

            ScheduledCheckout checkoutData = createScheduledCheckoutForGuest(currentGuest);
            controller.setCheckoutData(checkoutData);

            Stage stage = new Stage();
            stage.setTitle("Checkout Summary - " + currentGuest.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (dialogStage != null) stage.initOwner(dialogStage);
            controller.setPopupStage(stage);
            stage.showAndWait();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening checkout summary", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open checkout summary window: " + e.getMessage());
        }
    }

    // HELPER METHODS

    /**
     * Get room rate by room number
     */
    private double getRoomRateByNumber(String roomNumber) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT price FROM rooms WHERE room_no = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, roomNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("price");
                    }
                }
            }
        }
        throw new SQLException("Room rate not found for room: " + roomNumber);
    }

    private void refreshGuestDataFromDatabase() {
        if (currentGuest == null || currentGuest.getId() == null) return;

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = """
                SELECT b.check_out_date, b.total_amount,
                       COALESCE(i.due_amount, 0) as pending_amount
                FROM bookings b
                LEFT JOIN invoices i ON b.id = i.booking_id
                WHERE b.guest_id = ?
                AND b.status IN ('Checked-in', 'Confirmed')
                ORDER BY b.check_in_date DESC LIMIT 1
                """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, currentGuest.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentGuest.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                        currentGuest.setTotalAmount(rs.getDouble("total_amount"));
                        currentGuest.setPendingAmount(rs.getDouble("pending_amount"));
                        populateGuestData();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error refreshing guest data", e);
        }
    }

    private Long fetchBookingIdForGuest(Long guestId) {
        if (guestId == null) return null;
        
        String sql = "SELECT id FROM bookings WHERE guest_id = ? AND status IN ('Checked-in','Confirmed') ORDER BY check_in_date DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching booking ID", e);
        }
        return null;
    }

    private ScheduledCheckout createScheduledCheckoutForGuest(Guest guest) {
        if (guest == null) return null;
        
        ScheduledCheckout checkout = new ScheduledCheckout();
        checkout.setGuestName(guest.getName());
        checkout.setRoomNo(guest.getRoomNumber());
        
        Long bookingId = fetchBookingIdForGuest(guest.getId());
        if (bookingId != null) {
            checkout.setBookingId(bookingId.toString());
            
            String sql = """
                SELECT b.check_in_date, b.check_out_date, b.status,
                       i.subtotal, i.gst, i.total, i.paid_amount, i.due_amount,
                       r.room_type
                FROM bookings b
                LEFT JOIN invoices i ON b.id = i.booking_id
                LEFT JOIN rooms r ON b.room_no = r.room_no
                WHERE b.id = ?
                """;
                
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                        LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();
                        checkout.setDurationOfStay(String.valueOf(ChronoUnit.DAYS.between(checkIn, checkOut)));
                        checkout.setStatus(rs.getString("status"));
                        checkout.setRoomTypeNo(rs.getString("room_type") + " / " + guest.getRoomNumber());
                        checkout.setTotalAmount(String.format(CURRENCY_FORMAT, rs.getDouble("total")));
                        checkout.setAmountPaid(String.format(CURRENCY_FORMAT, rs.getDouble("paid_amount")));
                        checkout.setPendingPayment(String.format(CURRENCY_FORMAT, rs.getDouble("due_amount")));
                        checkout.setGstDiscounts(String.format("GST: " + CURRENCY_FORMAT, rs.getDouble("gst")));
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error creating scheduled checkout", e);
            }
        }
        return checkout;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
