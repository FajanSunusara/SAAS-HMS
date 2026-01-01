package application.controllers;

import application.models.ScheduledCheckout;
import application.controllers.PaymentController;
import application.services.dao.DatabaseManager;
import application.utils.PropertyReader;
import application.services.dao.DailyChargesDAO;
import application.services.dao.ServiceUsedDAO;
import application.services.dao.ServiceItemDAO;
import application.services.GstCalculationService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SummaryPopupController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(SummaryPopupController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CURRENCY_FORMAT = "₹%.2f";

    @FXML private Label summaryName;
    @FXML private Label summaryRoomTypeNo;
    @FXML private Label summaryDuration;
    @FXML private Label summaryRoomCharges;
    @FXML private Label summaryGstDiscounts;
    @FXML private Label summaryTotal;
    @FXML private Label summaryAmountPaid;
    @FXML private Label summaryPendingAmount;
    @FXML private Label summaryStatus;

    @FXML private Button fullFinalSettlementBtn;
    @FXML private Button checkoutBtn;
    @FXML private Button printBillBtn;
    @FXML private Button extendStayBtn;
    @FXML private Button feedbackEmailBtn;

    @FXML private ImageView summarySettleIconView;
    @FXML private ImageView summaryCheckedOutIconView;
    @FXML private ImageView summaryPrintIconView;
    @FXML private ImageView summaryExtendIconView;
    @FXML private ImageView summaryEmailIconView;
    @FXML private Button refundPaymentBtn;

    private ScheduledCheckout currentCheckout;
    private Stage popupStage;
    private double pendingPaymentAmount = 0.0;
    private final DailyChargesDAO dailyChargesDAO = new DailyChargesDAO();
    
    // Configuration services
    private final PropertyReader propertyReader = PropertyReader.getInstance();
    private final GstCalculationService gstService = GstCalculationService.getInstance();
    
    // Service DAOs for retrieving service charges like PaymentController
    private final ServiceUsedDAO serviceUsedDAO = new ServiceUsedDAO();
    private final ServiceItemDAO serviceItemDAO = new ServiceItemDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadIcons();
        setupInitialButtonStates();
    }

    public void setCheckoutData(ScheduledCheckout checkout) {
        this.currentCheckout = checkout;
        if (checkout != null) {
            populateCheckoutData();
            configureButtonStates();
        } else {
            clearSummaryData();
        }
    }

    private void populateCheckoutData() {
        summaryName.setText(currentCheckout.getGuestName());
        summaryRoomTypeNo.setText(currentCheckout.getRoomTypeNo());
        
        // Calculate real-time duration and charges
        updateRealTimeCharges();
        summaryGstDiscounts.setText(currentCheckout.getGstDiscounts());
        summaryAmountPaid.setText(currentCheckout.getAmountPaid());
        if (summaryStatus != null) {
            summaryStatus.setText(currentCheckout.getStatus());
        }
        
        // Update pending amount with real-time calculation
        summaryPendingAmount.setText(String.format("₹%.2f", pendingPaymentAmount));
    }

    /**
     * Update charges based on real-time calculation using service charges from database
     */
    private void updateRealTimeCharges() {
        try {
            Long bookingId = Long.parseLong(currentCheckout.getBookingId());
            
            // Get booking details for real-time calculation
            String sql = """
                SELECT b.check_in_date, b.check_out_date, b.advance_paid, r.price,
                       COALESCE(b.gst_rate, ?) as booking_gst_rate,
                       COALESCE(b.gst_included, false) as gst_included
                FROM bookings b
                JOIN rooms r ON b.room_no = r.room_no
                WHERE b.id = ?
            """;
            
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                // Get default GST rate from properties
                double defaultGstRate = propertyReader.getGstRate();
                ps.setDouble(1, defaultGstRate);
                ps.setLong(2, bookingId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                        LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();
                        double dailyRate = rs.getDouble("price");
                        double advancePaid = rs.getDouble("advance_paid");
                        double gstRate = rs.getDouble("booking_gst_rate");
                        boolean gstIncluded = rs.getBoolean("gst_included");
                        
                        // Calculate days stayed from check-in to current date
                        LocalDate today = LocalDate.now();
                        LocalDate effectiveDate = today.isBefore(checkOut) ? today : checkOut;
                        int daysStayed = (int) ChronoUnit.DAYS.between(checkIn, effectiveDate);
                        if (daysStayed < 0) daysStayed = 0;
                        
                        // Update duration display
                        summaryDuration.setText(daysStayed + " days stayed (of " + 
                            ChronoUnit.DAYS.between(checkIn, checkOut) + " booked)");
                        
                        // Calculate charges using service data from database like PaymentController
                        double roomCharges = dailyRate * daysStayed;
                        
                        // Get actual service charges from service tables (like PaymentController)
                        double serviceCharges = getServiceChargesFromDatabase(bookingId);
                        double subtotal = roomCharges + serviceCharges;
                        
                        // Calculate GST using GST service
                        BigDecimal subtotalBD = BigDecimal.valueOf(subtotal);
                        GstCalculationService.GstResult gstResult = gstService.calculate(subtotalBD, gstIncluded);
                        double totalChargesIncurred = gstResult.getTotalAmount().doubleValue();
                        
                        // Update display labels
                        summaryRoomCharges.setText(String.format("₹%.2f", roomCharges));
                        summaryTotal.setText(String.format("₹%.2f", totalChargesIncurred));
                        
                        // Calculate real-time pending amount
                        pendingPaymentAmount = Math.max(0, totalChargesIncurred - advancePaid);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to original extraction method
            extractPendingAmount();
        }
    }

    /**
     * Get service charges from database like PaymentController does
     * This matches the implementation in PaymentController.loadServiceItemsForBooking()
     */
    private double getServiceChargesFromDatabase(Long bookingId) {
        double serviceTotal = 0.0;
        
        if (bookingId == null) {
            return serviceTotal;
        }
        
        try {
            // Get service items for this booking exactly like PaymentController
            var servicesUsed = serviceUsedDAO.getServiceUsedByBookingId(bookingId);
            
            for (var serviceUsed : servicesUsed) {
                var items = serviceItemDAO.getServiceItemsByServiceUsedId(serviceUsed.getId());
                for (var item : items) {
                    // Calculate total amount for each service item
                    double itemAmount = item.getQuantity() * item.getPrice().doubleValue();
                    serviceTotal += itemAmount;
                }
            }
            
            LOGGER.info("Retrieved service charges from database for booking " + bookingId + ": ₹" + serviceTotal);
            
        } catch (Exception e) {
            LOGGER.warning("Could not load service charges from database for booking " + bookingId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return serviceTotal;
    }

    /**
     * Get GST rate for calculations - uses PropertyReader and booking-specific rates
     */
    private double getGstRate(Long bookingId) {
        try {
            // Try to get booking-specific GST rate first
            try (Connection conn = DatabaseManager.getConnection()) {
                String sql = "SELECT gst_rate FROM bookings WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            double bookingGstRate = rs.getDouble("gst_rate");
                            if (bookingGstRate > 0) {
                                return bookingGstRate / 100.0; // Convert percentage to decimal
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Could not retrieve booking-specific GST rate: " + e.getMessage());
        }
        
        // Fallback to PropertyReader
        return propertyReader.getGstRate() / 100.0;
    }

    private void extractPendingAmount() {
        try {
            String pendingStr = currentCheckout.getPendingPayment();
            if (pendingStr != null) {
                pendingStr = pendingStr.replaceAll("[^\\d.]", "");
                if (!pendingStr.isEmpty()) {
                    pendingPaymentAmount = Double.parseDouble(pendingStr);
                } else {
                    pendingPaymentAmount = 0.0;
                }
            } else {
                pendingPaymentAmount = 0.0;
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing pending payment amount: " + e.getMessage());
            pendingPaymentAmount = 0.0;
        }
    }

    // ... [All other existing methods remain the same until extend stay functionality] ...

    /**
     * ENHANCED EXTEND STAY FUNCTIONALITY with service charges from database
     */
    @FXML
    private void handleExtendStay() {
        if (currentCheckout == null) {
            showAlert(Alert.AlertType.WARNING, "Extend Stay", "No checkout data available.");
            return;
        }
        
        try {
            openExtendStayDialog();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in extend stay", e);
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Failed to process extend stay request: " + e.getMessage());
        }
    }

    /**
     * Comprehensive Extend Stay Dialog with service charges from database
     */
    private void openExtendStayDialog() {
        try {
            // Get current booking details
            Long bookingId = Long.parseLong(currentCheckout.getBookingId());
            BookingDetails bookingDetails = getCurrentBookingDetails(bookingId);
            
            if (bookingDetails == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not retrieve booking details.");
                return;
            }

            Dialog<LocalDate> dialog = new Dialog<>();
            dialog.setTitle("Extend Stay - " + currentCheckout.getGuestName());
            dialog.setHeaderText("Select new checkout date and review charges");

            VBox content = new VBox(15);
            content.setPadding(new Insets(20));

            // Current checkout date
            Label currentCheckoutLabel = new Label("Current Checkout: " + 
                bookingDetails.checkoutDate.format(DATE_FORMATTER));
            currentCheckoutLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            // New checkout date picker
            Label newCheckoutLabel = new Label("New Checkout Date:");
            DatePicker newCheckoutPicker = new DatePicker();
            newCheckoutPicker.setValue(bookingDetails.checkoutDate.plusDays(1));
            newCheckoutPicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(bookingDetails.checkoutDate) ||
                        date.equals(bookingDetails.checkoutDate));
                }
            });

            // Cost calculation labels
            Label currentTotalLabel = new Label(String.format("Current Total: " + CURRENCY_FORMAT,
                bookingDetails.totalAmount));
            Label roomRateLabel = new Label(String.format("Room Rate: " + CURRENCY_FORMAT + " per night",
                bookingDetails.roomRate));
            Label currentServiceChargesLabel = new Label(String.format("Current Service Charges: " + CURRENCY_FORMAT,
                bookingDetails.currentServiceCharges));
            Label gstRateLabel = new Label(String.format("GST Rate: %.1f%%", 
                bookingDetails.gstRate));
            Label additionalAmountLabel = new Label("Additional Amount: ₹0.00");
            Label newTotalLabel = new Label("New Total: ₹0.00");
            Label currentPendingLabel = new Label(String.format("Currently Pending: " + CURRENCY_FORMAT,
                pendingPaymentAmount));

            // Style labels
            additionalAmountLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
            newTotalLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");

            // Update calculations when date changes
            newCheckoutPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    calculateExtendedStayAmountsWithServiceData(newDate, bookingDetails, 
                        additionalAmountLabel, newTotalLabel);
                }
            });

            // Initial calculation
            calculateExtendedStayAmountsWithServiceData(newCheckoutPicker.getValue(), bookingDetails, 
                additionalAmountLabel, newTotalLabel);

            content.getChildren().addAll(
                currentCheckoutLabel,
                new Separator(),
                newCheckoutLabel,
                newCheckoutPicker,
                new Separator(),
                roomRateLabel,
                currentServiceChargesLabel,
                gstRateLabel,
                currentTotalLabel,
                additionalAmountLabel,
                newTotalLabel,
                currentPendingLabel
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
                    !newDate.isAfter(bookingDetails.checkoutDate));
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
                    processExtendStayWithServiceData(newCheckoutDate, bookingDetails);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error processing extend stay", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to extend stay: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening extend stay dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open extend stay dialog: " + e.getMessage());
        }
    }

    /**
     * Enhanced Booking Details Class with service charge information from database
     */
    private static class BookingDetails {
        LocalDate checkinDate;
        LocalDate checkoutDate;
        double totalAmount;
        double roomRate;
        double gstRate;
        boolean gstIncluded;
        String roomNo;
        double currentServiceCharges;

        BookingDetails(LocalDate checkinDate, LocalDate checkoutDate, 
                      double totalAmount, double roomRate, String roomNo,
                      double gstRate, boolean gstIncluded, double currentServiceCharges) {
            this.checkinDate = checkinDate;
            this.checkoutDate = checkoutDate;
            this.totalAmount = totalAmount;
            this.roomRate = roomRate;
            this.roomNo = roomNo;
            this.gstRate = gstRate;
            this.gstIncluded = gstIncluded;
            this.currentServiceCharges = currentServiceCharges;
        }
    }

    /**
     * Get Current Booking Details including service charges from database
     */
    private BookingDetails getCurrentBookingDetails(Long bookingId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = """
                SELECT b.check_in_date, b.check_out_date, b.total_amount, 
                       b.room_no, r.price as room_rate,
                       COALESCE(b.gst_rate, ?) as gst_rate,
                       COALESCE(b.gst_included, false) as gst_included
                FROM bookings b
                JOIN rooms r ON b.room_no = r.room_no
                WHERE b.id = ?
            """;
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // Get default GST rate from properties
                ps.setDouble(1, propertyReader.getGstRate());
                ps.setLong(2, bookingId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Get current service charges from database like PaymentController
                        double currentServiceCharges = getServiceChargesFromDatabase(bookingId);
                        
                        return new BookingDetails(
                            rs.getDate("check_in_date").toLocalDate(),
                            rs.getDate("check_out_date").toLocalDate(),
                            rs.getDouble("total_amount"),
                            rs.getDouble("room_rate"),
                            rs.getString("room_no"),
                            rs.getDouble("gst_rate"),
                            rs.getBoolean("gst_included"),
                            currentServiceCharges
                        );
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving booking details", e);
        }
        return null;
    }

    /**
     * Calculate Extended Stay Financial Amounts using service data from database
     */
    private void calculateExtendedStayAmountsWithServiceData(LocalDate newCheckoutDate, BookingDetails bookingDetails,
                                            Label additionalAmountLabel, Label newTotalLabel) {
        if (newCheckoutDate == null || bookingDetails == null) return;
        
        try {
            long currentNights = ChronoUnit.DAYS.between(
                bookingDetails.checkinDate, bookingDetails.checkoutDate);
            long newNights = ChronoUnit.DAYS.between(
                bookingDetails.checkinDate, newCheckoutDate);
            long additionalNights = newNights - currentNights;

            if (additionalNights <= 0) {
                additionalAmountLabel.setText("Additional Amount: ₹0.00");
                newTotalLabel.setText(String.format("New Total: " + CURRENCY_FORMAT,
                    bookingDetails.totalAmount));
                return;
            }

            // Calculate additional charges (only room charges, services remain same)
            double additionalRoomCharges = bookingDetails.roomRate * additionalNights;
            double additionalSubtotal = additionalRoomCharges; // No additional service charges for room extension
            
            // Use GstCalculationService for proper GST calculation
            BigDecimal subtotalBD = BigDecimal.valueOf(additionalSubtotal);
            GstCalculationService.GstResult gstResult = gstService.calculate(subtotalBD, bookingDetails.gstIncluded);
            double totalAdditionalAmount = gstResult.getTotalAmount().doubleValue();
            double newTotalAmount = bookingDetails.totalAmount + totalAdditionalAmount;

            additionalAmountLabel.setText(String.format("Additional Amount: " + CURRENCY_FORMAT +
                " (%d nights × ₹%.2f + %.1f%% GST)", 
                totalAdditionalAmount, additionalNights, bookingDetails.roomRate, bookingDetails.gstRate));
            newTotalLabel.setText(String.format("New Total: " + CURRENCY_FORMAT, newTotalAmount));

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error calculating extended stay amounts", e);
            additionalAmountLabel.setText("Additional Amount: Error calculating");
            newTotalLabel.setText("New Total: Error calculating");
        }
    }

    /**
     * Process Extended Stay using service data from database
     */
    private void processExtendStayWithServiceData(LocalDate newCheckoutDate, BookingDetails bookingDetails) 
            throws Exception {
        long currentNights = ChronoUnit.DAYS.between(
            bookingDetails.checkinDate, bookingDetails.checkoutDate);
        long newNights = ChronoUnit.DAYS.between(
            bookingDetails.checkinDate, newCheckoutDate);
        long additionalNights = newNights - currentNights;

        if (additionalNights <= 0) {
            showAlert(Alert.AlertType.WARNING, "Invalid Extension",
                "New checkout date must be after current checkout date.");
            return;
        }

        // Calculate additional amounts (only room charges, services remain same)
        double additionalRoomCharges = bookingDetails.roomRate * additionalNights;
        double additionalSubtotal = additionalRoomCharges;
        
        // Use GstCalculationService for proper GST calculation
        BigDecimal subtotalBD = BigDecimal.valueOf(additionalSubtotal);
        GstCalculationService.GstResult gstResult = gstService.calculate(subtotalBD, bookingDetails.gstIncluded);
        double additionalGst = gstResult.getTaxAmount().doubleValue();
        double totalAdditionalAmount = gstResult.getTotalAmount().doubleValue();
        double newTotalAmount = bookingDetails.totalAmount + totalAdditionalAmount;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long bookingId = Long.parseLong(currentCheckout.getBookingId());

                // Check room availability for extended period
                if (!isRoomAvailableForExtension(conn, bookingDetails.roomNo,
                        bookingDetails.checkoutDate, newCheckoutDate, bookingId)) {
                    throw new Exception("Room is not available for the extended period. " +
                        "There are conflicting bookings or reservations.");
                }

                // Update booking record
                updateBookingForExtendedStay(conn, bookingId, newCheckoutDate, newTotalAmount);

                // Update invoice record (service charges remain unchanged)
                updateInvoiceForExtendedStay(conn, bookingId,
                    additionalSubtotal, additionalGst, totalAdditionalAmount);

                conn.commit();

                // Refresh UI data
                refreshCheckoutData();

                // Show success message and ask about payment
                showExtensionSuccessAndPaymentOption(totalAdditionalAmount, newCheckoutDate);

                LOGGER.info("Successfully extended stay for guest: " + currentCheckout.getGuestName() +
                    " to " + newCheckoutDate + ", additional amount: ₹" + totalAdditionalAmount);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ... [All remaining methods stay the same as in previous implementation] ...
    
    private void configureButtonStates() {
        boolean hasPendingPayment = pendingPaymentAmount > 0.01;
        
        if (hasPendingPayment) {
            // Enable and highlight Full & Final Settlement button
            fullFinalSettlementBtn.setDisable(false);
            fullFinalSettlementBtn.setStyle(
                "-fx-font-weight: bold; " +
                "-fx-background-color: #2563eb; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"
            );
            
            // Disable checkout button
            checkoutBtn.setDisable(true);
            checkoutBtn.setStyle(
                "-fx-opacity: 0.5; " +
                "-fx-background-color: #9ca3af; " +
                "-fx-text-fill: #6b7280; " +
                "-fx-background-radius: 6;"
            );
        } else {
            // Disable Full & Final Settlement button
            fullFinalSettlementBtn.setDisable(true);
            fullFinalSettlementBtn.setStyle(
                "-fx-opacity: 0.5; " +
                "-fx-background-color: #9ca3af; " +
                "-fx-text-fill: #6b7280; " +
                "-fx-background-radius: 6;"
            );
            
            // Enable checkout button
            checkoutBtn.setDisable(false);
            checkoutBtn.setStyle(
                "-fx-font-weight: bold; " +
                "-fx-background-color: #16a34a; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"
            );
        }
     // ADD this inside the configureButtonStates method after fullFinalSettlementBtn configuration:

     // Configure refund button
     if (refundPaymentBtn != null) {
         // Only enable refund if there's a payment to refund
         boolean hasPaymentToRefund = hasPaymentRecord();
         
         if (hasPaymentToRefund) {
             enableButton(refundPaymentBtn);
             refundPaymentBtn.setStyle(
                 "-fx-background-color: #E17055; " +
                 "-fx-text-fill: white; " +
                 "-fx-background-radius: 6; " +
                 "-fx-cursor: hand; " +
                 "-fx-font-weight: bold;"
             );
         } else {
             refundPaymentBtn.setDisable(true);
             refundPaymentBtn.setStyle(
                 "-fx-opacity: 0.5; " +
                 "-fx-background-color: #9ca3af; " +
                 "-fx-text-fill: #6b7280; " +
                 "-fx-background-radius: 6;"
             );
         }
     }

        
        enableButton(printBillBtn);
        enableButton(extendStayBtn);
        enableButton(feedbackEmailBtn);
    }

    private void enableButton(Button button) {
        if (button != null) {
            button.setDisable(false);
            button.setStyle(
                "-fx-background-color: #374151; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"
            );
        }
    }
    private boolean hasPaymentRecord() {
        if (currentCheckout == null) return false;
        
        try {
            Long bookingId = Long.parseLong(currentCheckout.getBookingId());
            try (Connection conn = DatabaseManager.getConnection()) {
                // Use the exact column names from your schema
                String sql = "SELECT COUNT(*) FROM payments WHERE BOOKING_ID = ? AND amount > 0";
                
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, bookingId);
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1) > 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking payment records", e);
        }
        return false;
    }

    private void setupInitialButtonStates() {
        if (fullFinalSettlementBtn != null) fullFinalSettlementBtn.setDisable(true);
        if (checkoutBtn != null) checkoutBtn.setDisable(true);
        if (printBillBtn != null) printBillBtn.setDisable(true);
        if (extendStayBtn != null) extendStayBtn.setDisable(true);
        if (feedbackEmailBtn != null) feedbackEmailBtn.setDisable(true);
    }

    private void clearSummaryData() {
        summaryName.setText("N/A");
        summaryRoomTypeNo.setText("-");
        summaryDuration.setText("-");
        summaryRoomCharges.setText("₹0.00");
        summaryGstDiscounts.setText("-");
        summaryTotal.setText("₹0.00");
        summaryAmountPaid.setText("₹0.00");
        summaryPendingAmount.setText("₹0.00");
        if (summaryStatus != null) summaryStatus.setText("-");
        setupInitialButtonStates();
    }

    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }

    private void loadIcons() {
        loadIcon(summarySettleIconView, "/icons/checkmark_white.png");
        loadIcon(summaryCheckedOutIconView, "/icons/checked_out_white.png");
        loadIcon(summaryPrintIconView, "/icons/print_white.png");
        loadIcon(summaryExtendIconView, "/icons/extend_white.png");
        loadIcon(summaryEmailIconView, "/icons/email_white.png");
    }

    private void loadIcon(ImageView imageView, String path) {
        if (imageView == null) return;
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                imageView.setImage(new Image(url.toExternalForm(), true));
            } else {
                System.err.println("Icon not found: " + path);
            }
        } catch (Exception ex) {
            System.err.println("Failed to load icon " + path + ": " + ex.getMessage());
        }
    }

    @FXML
    private void handleFullFinalSettlement() {
        if (currentCheckout != null && pendingPaymentAmount > 0.01) {
            System.out.println("Opening Payment page for: " + currentCheckout.getGuestName());
            openPaymentPage();
        } else {
            showAlert(Alert.AlertType.INFORMATION, "No Payment Required",
                "There is no pending payment for this checkout.");
        }
    }
    

    private void openPaymentPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Payment.fxml"));
            Parent paymentRoot = loader.load();
            PaymentController paymentController = loader.getController();
            
            Long bookingId = Long.parseLong(currentCheckout.getBookingId());
            Long guestId = getGuestIdFromBooking(bookingId);
            
            if (guestId != null) {
                paymentController.setBookingContext(bookingId, guestId);
                
                Stage paymentStage = new Stage();
                paymentStage.setScene(new Scene(paymentRoot));
                paymentStage.setTitle("Payment - Settle Pending Amount");
                paymentStage.initModality(Modality.WINDOW_MODAL);
                paymentStage.initOwner(popupStage);
                paymentStage.setResizable(false);
                paymentStage.showAndWait();
                
                if (paymentController.isPaymentSuccessful()) {
                    refreshCheckoutData();
                    showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                        "Payment completed successfully. You can now proceed with checkout.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not load booking information for payment.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                "Could not open payment page: " + e.getMessage());
        }
    }

    private Long getGuestIdFromBooking(Long bookingId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT guest_id FROM bookings WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("guest_id");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void refreshCheckoutData() {
        if (currentCheckout != null) {
            try {
                Long bookingId = Long.parseLong(currentCheckout.getBookingId());
                
                // Update invoice with real-time charges
                dailyChargesDAO.updateInvoiceWithRealTimeCharges(bookingId);
                
                // Refresh pending amount
                pendingPaymentAmount = dailyChargesDAO.calculateRealTimePendingAmount(bookingId);
                summaryPendingAmount.setText(String.format("₹%.2f", pendingPaymentAmount));
                
                // Update real-time charges display
                updateRealTimeCharges();
                
                // Reconfigure button states
                configureButtonStates();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleMarkAsCheckedOut() {
        if (currentCheckout != null && pendingPaymentAmount <= 0.01) {
            System.out.println("Checking out: " + currentCheckout.getGuestName());
            performCheckout();
        } else {
            showAlert(Alert.AlertType.WARNING, "Cannot Checkout",
                "Please settle all pending payments before checkout.");
        }
    }

    private void performCheckout() {
        try {
            Long bookingId = Long.parseLong(currentCheckout.getBookingId());
            String roomNo = currentCheckout.getRoomNo();
            
            // Simple status update instead of deletion
            updateBookingAndRoomStatus(bookingId, roomNo);
            
            showAlert(Alert.AlertType.INFORMATION, "Checkout Successful",
                "Guest has been successfully checked out.\n" +
                "Room " + roomNo + " is now marked for cleaning.");
            
            if (popupStage != null) {
                popupStage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                "Database error during checkout: " + e.getMessage());
        }
    }

    private void updateBookingAndRoomStatus(Long bookingId, String roomNo) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update booking status to 'Checked-out'
                String updateBookingSql = "UPDATE bookings SET status = 'Checked-out' WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateBookingSql)) {
                    ps.setLong(1, bookingId);
                    int updated = ps.executeUpdate();
                    if (updated > 0) {
                        System.out.println("Updated booking status to 'Checked-out' for booking ID: " + bookingId);
                    }
                }
                
                boolean isHousekeepingPageEnabled = PropertyReader.getInstance()
                    .getBooleanProperty("page.access.HOUSEKEEPING", true);
                    
                // Update room status based on housekeeping page lock status
                String newRoomStatus = isHousekeepingPageEnabled ? "Cleaning" : "Available";
                String updateRoomSql = "UPDATE rooms SET status = ? WHERE room_no = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateRoomSql)) {
                    ps.setString(1, newRoomStatus);
                    ps.setString(2, roomNo);
                    int updated = ps.executeUpdate();
                    if (updated > 0) {
                        System.out.println("Updated room status to '" + newRoomStatus + "' for room: " + roomNo);
                    }
                }
                
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new Exception("Checkout failed: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @FXML
    private void handlePrintFinalBill() {
        if (currentCheckout != null) {
            System.out.println("Printing final bill for: " + currentCheckout.getGuestName());
            showAlert(Alert.AlertType.INFORMATION, "Print Bill",
                "Bill printing functionality will be implemented here.\n" +
                "This will generate and print the final bill for room " +
                currentCheckout.getRoomNo());
        }
    }

    // ... [Include all the remaining extend stay methods from previous implementation] ...
    
    @FXML
    private void handleSendFeedbackEmail() {
        if (currentCheckout != null) {
            System.out.println("Sending feedback email to: " + currentCheckout.getGuestName());
            showAlert(Alert.AlertType.INFORMATION, "Feedback Email",
                "Feedback email functionality will be implemented here.\n" +
                "This will send a feedback survey email to the guest.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Include the remaining extend stay helper methods...
    private boolean isRoomAvailableForExtension(Connection conn, String roomNo,
            LocalDate currentCheckout, LocalDate newCheckout,
            Long currentBookingId) throws Exception {
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

/**
* Update Booking for Extended Stay
*/
private void updateBookingForExtendedStay(Connection conn, Long bookingId,
          LocalDate newCheckoutDate, double newTotalAmount) 
throws Exception {
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
throw new Exception("No booking found to update for booking ID: " + bookingId);
}
}
}

/**
* Update Invoice for Extended Stay
*/
private void updateInvoiceForExtendedStay(Connection conn, Long bookingId,
          double additionalSubtotal, double additionalGst,
          double totalAdditionalAmount) throws Exception {
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

/**
* Show Extension Success and Payment Option
*/
private void showExtensionSuccessAndPaymentOption(double additionalAmount, LocalDate newCheckoutDate) {
Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
successAlert.setTitle("Stay Extended Successfully");
successAlert.setHeaderText("Stay has been extended for " + currentCheckout.getGuestName());
successAlert.setContentText(String.format(
"New checkout date: %s\nAdditional amount: " + CURRENCY_FORMAT +
"\n\nWould you like to process payment for the additional amount now?",
newCheckoutDate.format(DATE_FORMATTER),
additionalAmount));

ButtonType payNowButton = new ButtonType("Pay Now", ButtonBar.ButtonData.YES);
ButtonType payLaterButton = new ButtonType("Pay Later", ButtonBar.ButtonData.NO);
successAlert.getButtonTypes().setAll(payNowButton, payLaterButton);

Optional<ButtonType> result = successAlert.showAndWait();
if (result.isPresent() && result.get() == payNowButton) {
handleFullFinalSettlement();
}
}
@FXML
private void handleRefundPayment() {
    if (currentCheckout == null) {
        showAlert(Alert.AlertType.WARNING, "No Data", "No checkout data available.");
        return;
    }
    
    try {
        // Get booking ID and payment information
        Long bookingId = Long.parseLong(currentCheckout.getBookingId());
        
        // Get the latest payment information for this booking
        PaymentInfo paymentInfo = getLatestPaymentInfo(bookingId);
        
        if (paymentInfo == null || paymentInfo.paymentId == null) {
            showAlert(Alert.AlertType.WARNING, "No Payment Found", 
                      "No payment record found for this booking.");
            return;
        }
        
        if (paymentInfo.amount.compareTo(BigDecimal.ZERO) <= 0) {
            showAlert(Alert.AlertType.WARNING, "No Amount to Refund", 
                      "No amount available for refund.");
            return;
        }
        
        // Confirm refund action
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Refund");
        confirmAlert.setHeaderText("Process Payment Refund");
        confirmAlert.setContentText(String.format(
            "Do you want to process a refund for:\n\n" +
            "Guest: %s\n" +
            "Room: %s\n" +
            "Payment Amount: ₹%.2f\n\n" +
            "This will open the refund processing window.",
            currentCheckout.getGuestName(),
            currentCheckout.getRoomNo(),
            paymentInfo.amount.doubleValue()));
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Open refund window
            PaymentController.openRefundWindow(bookingId, paymentInfo.paymentId, 
                                             paymentInfo.amount, popupStage);
            
            // Refresh data after refund window closes
            refreshCheckoutData();
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", 
                  "Failed to open refund window: " + e.getMessage());
    }
}

private PaymentInfo getLatestPaymentInfo(Long bookingId) {
    try (Connection conn = DatabaseManager.getConnection()) {
        // Use the exact column names from your schema
        String sql = "SELECT id, amount FROM payments WHERE BOOKING_ID = ? ORDER BY PAYMENT_DATE DESC LIMIT 1";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PaymentInfo info = new PaymentInfo();
                    info.paymentId = rs.getLong("id");
                    info.amount = rs.getBigDecimal("amount");
                    return info;
                }
            }
        }
    } catch (SQLException e) {
        LOGGER.log(Level.WARNING, "Error retrieving payment info", e);
    }
    return null;
}
// Helper class for payment information
private static class PaymentInfo {
    Long paymentId;
    BigDecimal amount;
}

}
