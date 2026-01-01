package application.controllers;

import application.models.Guest;
import application.models.ReservationStub;
import application.services.dao.DatabaseManager;
import application.services.dao.GuestDAO;
import application.services.dao.ReservationDAO;
import application.services.dao.RoomDAO;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.stage.Window;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Optional;
import java.math.BigDecimal;
import java.sql.*;

import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;

public class ReservationFormController {

    @FXML private TextField guestNameField, emailField, phoneField;
    @FXML private DatePicker formCheckInDate, formCheckOutDate;
    @FXML private TextField noOfDaysField;
    @FXML private TextField noOfPersonsField;
    @FXML private ComboBox<String> roomsComboField;
    @FXML private TextField timeOfArrivalField;
    @FXML private TextField roomRateField;
    @FXML private TextField advancePaidField;
    @FXML private TextArea specialRequestField;
    @FXML private ComboBox<String> roomCategoryCombo;
    @FXML private Label formTitleLabel;
    @FXML private Label availabilityLabel;
 // FXML fields for the YouTube-style search bar
    @FXML private TextField guestSearchField;
    @FXML private ListView<String> guestSuggestionsListView;
    @FXML private VBox searchOverlay;
    private ReservationController parentController;
    private Long selectedReservationId = null;
    private Long selectedGuestId = null;
    private boolean isEditMode = false;

    private final ReservationDAO reservationsDAO = new ReservationDAO();
    private final GuestDAO guestDAO = new GuestDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    private ObservableList<String> allRoomTypes = FXCollections.observableArrayList();
    private ObservableList<String> allRoomNumbers = FXCollections.observableArrayList();
    private ObservableList<String> filteredRoomNumbers = FXCollections.observableArrayList();
    
    @FXML private Button refundPaymentBtn;


    public void initialize() {
        initializeRoomData();
        setupListeners();
        initYouTubeStyleSearch();
    }

    private void initializeRoomData() {
        loadRoomTypesFromDatabase();
        loadAllRoomNumbersFromDatabase();
       
    }

    private void loadRoomTypesFromDatabase() {
        try {
            allRoomTypes.clear();
            List<String> dbRoomTypes = roomDAO.findAllRoomTypes();
            if (dbRoomTypes != null && !dbRoomTypes.isEmpty()) {
                allRoomTypes.addAll(dbRoomTypes);
            } else {
                allRoomTypes.addAll(Arrays.asList("Single", "Double", "Deluxe", "Suite", "Twin"));
            }
            roomCategoryCombo.setItems(allRoomTypes);
        } catch (Exception e) {
            e.printStackTrace();
            allRoomTypes.addAll(Arrays.asList("Single", "Double", "Deluxe", "Suite", "Twin"));
            roomCategoryCombo.setItems(allRoomTypes);
        }
    }

    private void loadAllRoomNumbersFromDatabase() {
        try {
            allRoomNumbers.clear();
            List<String> dbRoomNumbers = roomDAO.findAllRoomNos();
            if (dbRoomNumbers != null && !dbRoomNumbers.isEmpty()) {
                dbRoomNumbers.sort((a, b) -> {
                    try {
                        return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                    } catch (NumberFormatException e) {
                        return a.compareTo(b);
                    }
                });
                allRoomNumbers.addAll(dbRoomNumbers);
            }

            filteredRoomNumbers.setAll(allRoomNumbers);
//            roomsComboField.setItems(filteredRoomNumbers);
        } catch (Exception e) {
            e.printStackTrace();
            allRoomNumbers.addAll(Arrays.asList("101", "102", "201", "202", "301", "302"));
            filteredRoomNumbers.setAll(allRoomNumbers);
            roomsComboField.setItems(filteredRoomNumbers);
        }
    }

    private void filterRoomNumbersByCategory(String selectedCategory) {
        try {
            filteredRoomNumbers.clear();
            if (selectedCategory == null || selectedCategory.isEmpty()) {
                filteredRoomNumbers.setAll(allRoomNumbers);
            } else {
                List<String> roomsOfType = roomDAO.findRoomNumbersByType(selectedCategory);
                if (roomsOfType != null && !roomsOfType.isEmpty()) {
                    roomsOfType.sort((a, b) -> {
                        try {
                            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                        } catch (NumberFormatException e) {
                            return a.compareTo(b);
                        }
                    });
                    filteredRoomNumbers.addAll(roomsOfType);
                }
            }

            roomsComboField.setItems(filteredRoomNumbers);
            if (roomsComboField.getValue() != null &&
                    !filteredRoomNumbers.contains(roomsComboField.getValue())) {
                roomsComboField.setValue(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            filteredRoomNumbers.setAll(allRoomNumbers);
            roomsComboField.setItems(filteredRoomNumbers);
        }
    }

    private void setupListeners() {
        formCheckInDate.valueProperty().addListener((obs, o, n) -> {
            updateNoOfDays();
            checkRoomAvailability();
        });

        formCheckOutDate.valueProperty().addListener((obs, o, n) -> {
            updateNoOfDays();
            checkRoomAvailability();
        });

        roomCategoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterRoomNumbersByCategory(newVal);
            checkRoomAvailability();
        });

        roomsComboField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                autoSelectRoomType(newVal);
                checkRoomAvailability();
            }
        });
    }

    private void checkRoomAvailability() {
        String selectedRoom = roomsComboField.getValue();
        LocalDate checkIn = formCheckInDate.getValue();
        LocalDate checkOut = formCheckOutDate.getValue();

        if (selectedRoom == null || checkIn == null || checkOut == null) {
            if (availabilityLabel != null) {
                availabilityLabel.setText("⏳ Select room and dates");
                availabilityLabel.setStyle("-fx-text-fill: #6b7280;");
            }
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            if (availabilityLabel != null) {
                availabilityLabel.setText("❗ Check-out must be after check-in");
                availabilityLabel.setStyle("-fx-text-fill: #ef4444;");
            }
            return;
        }

        try {
            RoomAvailabilityResult result = checkRoomAvailabilityWithDetails(selectedRoom, checkIn, checkOut);
            updateAvailabilityDisplay(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (availabilityLabel != null) {
                availabilityLabel.setText("❌ Error checking availability");
                availabilityLabel.setStyle("-fx-text-fill: #ef4444;");
            }
        }
    }

    private RoomAvailabilityResult checkRoomAvailabilityWithDetails(String roomNo, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        RoomAvailabilityResult result = new RoomAvailabilityResult();
        result.roomNo = roomNo;
        result.checkIn = checkIn;
        result.checkOut = checkOut;
        result.isAvailable = true;

        // Check for existing bookings
        String bookingQuery = """
            SELECT b.id, b.check_in_date, b.check_out_date, g.name as guest_name, b.status
            FROM bookings b 
            JOIN guests g ON b.guest_id = g.id
            WHERE b.room_no = ? 
            AND b.status IN ('Confirmed', 'Checked-in')
            AND (? < b.check_out_date AND ? > b.check_in_date)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(bookingQuery)) {

            ps.setString(1, roomNo);
            ps.setDate(2, java.sql.Date.valueOf(checkOut));
            ps.setDate(3, java.sql.Date.valueOf(checkIn));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.isAvailable = false;
                    result.conflictType = "Booking";
                    result.conflictGuestName = rs.getString("guest_name");
                    result.conflictCheckIn = rs.getDate("check_in_date").toLocalDate();
                    result.conflictCheckOut = rs.getDate("check_out_date").toLocalDate();
                    result.conflictStatus = rs.getString("status");
                    return result;
                }
            }
        }

        // Check for existing reservations
        String reservationQuery = """
            SELECT r.id, r.start_date, r.end_date, g.name as guest_name, r.status
            FROM reservations r 
            JOIN guests g ON r.guest_id = g.id
            WHERE r.room_no = ? 
            AND r.status IN ('Confirmed', 'Pending')
            AND (? < r.end_date AND ? > r.start_date)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(reservationQuery)) {

            ps.setString(1, roomNo);
            ps.setDate(2, java.sql.Date.valueOf(checkOut));
            ps.setDate(3, java.sql.Date.valueOf(checkIn));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Skip if editing the same reservation
                    Long conflictReservationId = rs.getLong("id");
                    if (isEditMode && selectedReservationId != null && selectedReservationId.equals(conflictReservationId)) {
                        return result; // Available (same reservation being edited)
                    }

                    result.isAvailable = false;
                    result.conflictType = "Reservation";
                    result.conflictGuestName = rs.getString("guest_name");
                    result.conflictCheckIn = rs.getDate("start_date").toLocalDate();
                    result.conflictCheckOut = rs.getDate("end_date").toLocalDate();
                    result.conflictStatus = rs.getString("status");
                }
            }
        }

        return result;
    }

    private void updateAvailabilityDisplay(RoomAvailabilityResult result) {
        if (availabilityLabel == null) return;

        if (result.isAvailable) {
            availabilityLabel.setText("✅ Room " + result.roomNo + " is available");
            availabilityLabel.setStyle("-fx-text-fill: #10b981;");
        } else {
            String conflictMessage = String.format(
                    "❌ %s conflict: %s (%s to %s) - %s",
                    result.conflictType,
                    result.conflictGuestName,
                    result.conflictCheckIn.toString(),
                    result.conflictCheckOut.toString(),
                    result.conflictStatus
            );
            availabilityLabel.setText(conflictMessage);
            availabilityLabel.setStyle("-fx-text-fill: #ef4444;");

            // Show detailed alert only if this is a new reservation attempt
            if (!isEditMode) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Room Not Available");
                alert.setHeaderText("Booking Conflict Detected");
                alert.setContentText(String.format(
                        "Room %s is not available for the selected dates.\n\n" +
                        "Existing %s:\n" +
                        "Guest: %s\n" +
                        "Dates: %s to %s\n" +
                        "Status: %s\n\n" +
                        "Please select different dates or another room.",
                        result.roomNo,
                        result.conflictType,
                        result.conflictGuestName,
                        result.conflictCheckIn,
                        result.conflictCheckOut,
                        result.conflictStatus
                ));
                alert.showAndWait();
            }
        }
    }

    // Helper class for availability results
    private static class RoomAvailabilityResult {
        String roomNo;
        LocalDate checkIn;
        LocalDate checkOut;
        boolean isAvailable;
        String conflictType; // "Booking" or "Reservation"
        String conflictGuestName;
        LocalDate conflictCheckIn;
        LocalDate conflictCheckOut;
        String conflictStatus;
    }

    private void autoSelectRoomType(String roomNumber) {
        try {
            String roomType = roomDAO.getRoomTypeByRoomNumber(roomNumber);
            if (roomType != null && allRoomTypes.contains(roomType)) {
                roomCategoryCombo.getSelectionModel().select(roomType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParentController(ReservationController parentController) {
        this.parentController = parentController;
    }

    public void initializeForNewReservation(String roomNo, LocalDate checkIn, LocalDate checkOut) {
        isEditMode = false;
        formTitleLabel.setText("New Reservation");
        if (roomNo != null) {
        	 System.out.println("You Click on this room nO : " + roomNo );
            roomsComboField.setValue(roomNo);
            autoSelectRoomType(roomNo);
        }
        if (checkIn != null) formCheckInDate.setValue(checkIn);
        if (checkOut != null) formCheckOutDate.setValue(checkOut);
        System.out.println("You Click on this room nO : " + roomNo );
        updateNoOfDays();
        checkRoomAvailability();
    }

    public void loadReservationForEdit(Long reservationId) {
        try {
            ReservationStub res = reservationsDAO.getReservationById(reservationId);
            if (res == null) {
                showAlert("Error", "Reservation not found.", Alert.AlertType.ERROR);
                return;
            }

            isEditMode = true;
            selectedReservationId = res.getId();
            selectedGuestId = res.getGuestId();
            formTitleLabel.setText("Edit Reservation");

            if (res.getRoomType() != null) {
                roomCategoryCombo.setValue(res.getRoomType());
                filterRoomNumbersByCategory(res.getRoomType());
            }

            if (res.getRoomNo() != null) {
                roomsComboField.setValue(res.getRoomNo());
            }

            formCheckInDate.setValue(res.getCheckInDate());
            formCheckOutDate.setValue(res.getCheckOutDate());
            noOfPersonsField.setText(res.getNumGuests() == null ? "" : String.valueOf(res.getNumGuests()));
            specialRequestField.setText(res.getNotes() == null ? "" : res.getNotes());
            updateNoOfDays();

            if (res.getGuestId() != null) {
                Guest g = guestDAO.getGuestById(res.getGuestId());
                if (g != null) {
                    guestNameField.setText(g.getName() == null ? "" : g.getName());
                    emailField.setText(g.getEmail() == null ? "" : g.getEmail());
                    phoneField.setText(g.getPhone() == null ? "" : g.getPhone());
                }
            }

            checkRoomAvailability();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Could not load reservation.", Alert.AlertType.ERROR);
        }
    }

    private void updateNoOfDays() {
        LocalDate checkIn = formCheckInDate.getValue();
        LocalDate checkOut = formCheckOutDate.getValue();
        if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
            long days = ChronoUnit.DAYS.between(checkIn, checkOut);
            noOfDaysField.setText(String.valueOf(days));
        } else {
            noOfDaysField.setText("0");
        }
    }

    @FXML
    private void onRefreshRoomData() {
        initializeRoomData();
        showAlert("Data Refreshed", "Room data has been refreshed from the database.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void onSaveReservation() {
        try {
            if (!validateForm()) return;

            String gName = safeTrim(guestNameField.getText());
            String gEmail = safeTrim(emailField.getText());
            String gPhone = safeTrim(phoneField.getText());

            Guest guest = new Guest();
            if (isEditMode && selectedGuestId != null) {
                guest.setId(selectedGuestId);
            }

            guest.setName(gName);
            guest.setEmail(gEmail);
            guest.setPhone(gPhone);

            Long guestId = guestDAO.addOrUpdateGuest(guest);
            if (guestId == null) {
                showAlert("Error", "Failed to save guest information.", Alert.AlertType.ERROR);
                return;
            }

            ReservationStub res = new ReservationStub();
            if (isEditMode) {
                res.setId(selectedReservationId);
            }

            res.setGuestId(guestId);
            res.setRoomNo(safeTrim(roomsComboField.getValue()));
            res.setRoomType(roomCategoryCombo.getValue());
            res.setCheckInDate(formCheckInDate.getValue());
            res.setCheckOutDate(formCheckOutDate.getValue());
            res.setStatus("Confirmed");
            res.setNumGuests(parseIntSafe(noOfPersonsField.getText()));
            res.setNotes(specialRequestField.getText());

            Long reservationId;
            boolean shouldProceedToPayment = false;

            if (isEditMode) {
                reservationsDAO.updateReservation(res);
                reservationId = selectedReservationId;
                showAlert("Success", "Reservation updated successfully.", Alert.AlertType.INFORMATION);
            } else {
                reservationId = reservationsDAO.addReservation(res);
                if (reservationId == null) {
                    showAlert("Error", "Failed to create reservation.", Alert.AlertType.ERROR);
                    return;
                }

                // Offer to go to payment for new reservations only
                Alert paymentAlert = new Alert(Alert.AlertType.CONFIRMATION);
                paymentAlert.setTitle("Reservation Created");
                paymentAlert.setHeaderText("Success!");
                paymentAlert.setContentText("Reservation created successfully. Would you like to proceed to payment?");
                ButtonType paymentBtn = new ButtonType("Go to Payment");
                ButtonType laterBtn = new ButtonType("Later");

                paymentAlert.getButtonTypes().setAll(paymentBtn, laterBtn);
                paymentAlert.showAndWait().ifPresent(response -> {
                    if (response == paymentBtn) {
                        openPaymentWindowForReservation(guestId, gName, gPhone, gEmail, res, reservationId);
                    }
                });

                shouldProceedToPayment = paymentAlert.getResult() == paymentBtn;
            }

            // Close window if in edit mode OR if user chose not to go to payment
            if (isEditMode || !shouldProceedToPayment) {
                closeWindow();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save reservation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onProceedToPayment() {
        if (!validateForm()) return;

        // First save the reservation if it's new
        if (!isEditMode) {
            onSaveReservation();
        } else {
            // For edit mode, proceed directly to payment
            String guestName = guestNameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String roomNo = roomsComboField.getValue();
            String roomType = roomCategoryCombo.getValue();
            LocalDate checkIn = formCheckInDate.getValue();
            LocalDate checkOut = formCheckOutDate.getValue();

            ReservationStub res = new ReservationStub();
            res.setRoomNo(roomNo);
            res.setRoomType(roomType);
            res.setCheckInDate(checkIn);
            res.setCheckOutDate(checkOut);

            openPaymentWindowForReservation(selectedGuestId, guestName, phone, email, res, selectedReservationId);
        }
    }

    private void openPaymentWindowForReservation(
            Long guestId, String guestName, String phone, String email,
            ReservationStub res, Long reservationId
    ) {
        try {
            BigDecimal rate = safeMoney(roomRateField.getText());
            long nights = Math.max(1, ChronoUnit.DAYS.between(res.getCheckInDate(), res.getCheckOutDate()));
            BigDecimal total = rate.multiply(BigDecimal.valueOf(nights));
            BigDecimal advance = safeMoney(advancePaidField.getText());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Payment.fxml"));
            Parent root = loader.load();
            PaymentController paymentController = loader.getController();

            // Set reservation context in PaymentController
            paymentController.setReservationContext(
                    guestId,
                    guestName, phone, email, null, null, // address, gst can be null for reservations
                    res.getRoomNo(), res.getRoomType(),
                    res.getCheckInDate(), res.getCheckOutDate(),
                    rate, total, advance,
                    reservationId
            );

            paymentController.populateForReservationContext();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Payment - Reservation Confirmation");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(guestNameField.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

            if (paymentController.isPaymentSuccessful()) {
                showAlert("Payment Success", "Payment processed and reservation confirmed.", Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Payment Not Completed", "Payment was cancelled or not completed.", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open payment window: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

//    @FXML
//    private void onDeleteReservation() {
//        if (!isEditMode || selectedReservationId == null) {
//            showAlert("Error", "No reservation selected for deletion.", Alert.AlertType.WARNING);
//            return;
//        }
//
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//        confirm.setTitle("Confirm Delete");
//        confirm.setHeaderText("Delete Reservation");
//        confirm.setContentText("Are you sure you want to delete this reservation?");
//
//        confirm.showAndWait().ifPresent(response -> {
//            if (response == ButtonType.OK) {
//                try {
//                    reservationsDAO.deleteFullReservation(selectedReservationId);
//                    showAlert("Success", "Reservation deleted successfully.", Alert.AlertType.INFORMATION);
//                    closeWindow();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    showAlert("Error", "Failed to delete reservation.", Alert.AlertType.ERROR);
//                }
//            }
//        });
//    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        if (safeTrim(guestNameField.getText()).isEmpty()) {
            showAlert("Validation Error", "Guest name is required.", Alert.AlertType.WARNING);
            return false;
        }

        if (roomsComboField.getValue() == null || safeTrim(roomsComboField.getValue()).isEmpty()) {
            showAlert("Validation Error", "Room number is required.", Alert.AlertType.WARNING);
            return false;
        }

        if (roomCategoryCombo.getValue() == null || roomCategoryCombo.getValue().isEmpty()) {
            showAlert("Validation Error", "Room type is required.", Alert.AlertType.WARNING);
            return false;
        }

        LocalDate checkIn = formCheckInDate.getValue();
        LocalDate checkOut = formCheckOutDate.getValue();

        if (checkIn == null || checkOut == null) {
            showAlert("Validation Error", "Check-in and check-out dates are required.", Alert.AlertType.WARNING);
            return false;
        }

        if (!checkOut.isAfter(checkIn)) {
            showAlert("Validation Error", "Check-out date must be after check-in date.", Alert.AlertType.WARNING);
            return false;
        }

        // Check room availability before saving
        String selectedRoom = roomsComboField.getValue();
        if (selectedRoom != null && checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
            try {
                RoomAvailabilityResult result = checkRoomAvailabilityWithDetails(selectedRoom, checkIn, checkOut);
                if (!result.isAvailable) {
                    showAlert("Room Not Available",
                            String.format("Room %s has a %s conflict with %s from %s to %s",
                                    selectedRoom, result.conflictType, result.conflictGuestName,
                                    result.conflictCheckIn, result.conflictCheckOut),
                            Alert.AlertType.ERROR);
                    return false;
                }
            } catch (Exception e) {
                showAlert("Validation Error", "Could not check room availability: " + e.getMessage(), Alert.AlertType.ERROR);
                return false;
            }
        }

        return true;
    }

//    private void closeWindow() {
//        Stage stage = (Stage) guestNameField.getScene().getWindow();
//        stage.close();
//    }
//
//    private void showAlert(String title, String content, Alert.AlertType type) {
//        Alert alert = new Alert(type);
//        alert.setTitle(title);
//        alert.setContentText(content);
//        alert.showAndWait();
//    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private Integer parseIntSafe(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    /**
     * Initializes the YouTube-style search bar functionality.
     * Sets up listeners for the search field and the suggestions list.
     */
    private void initYouTubeStyleSearch() {
        if (guestSearchField == null || guestSuggestionsListView == null || searchOverlay == null) return;
        hideOverlay();

        // This listener clears the guest fields if the user deletes the search query
        guestSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                showSuggestions(newVal.trim());
            } else {
                clearGuestFields();
                hideOverlay();
            }
        });

        // This listener handles the selection from the list
        guestSuggestionsListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    onGuestSuggestionSelected(newVal);
                }
            }
        );

        // Hide the overlay when the search field loses focus, unless the list gains it
        guestSearchField.focusedProperty().addListener((obs, o, focused) -> {
            if (!focused && !guestSuggestionsListView.isFocused()) hideOverlay();
        });

        // Hide the overlay when the list loses focus, unless the search field gains it
        guestSuggestionsListView.focusedProperty().addListener((obs, o, focused) -> {
            if (!focused && !guestSearchField.isFocused()) hideOverlay();
        });
    }

    /**
     * Queries the database and shows guest suggestions in the list view.
     *
     * @param query The search query string.
     */
    private void showSuggestions(String query) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT id, name, COALESCE(phone,''), COALESCE(email,'') " +
                "FROM guests WHERE LOWER(name) LIKE ? OR phone LIKE ? OR LOWER(email) LIKE ? " +
                "ORDER BY name LIMIT 20";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + query.toLowerCase() + "%";
            ps.setString(1, like);
            ps.setString(2, "%" + query + "%");
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(2);
                    String phone = rs.getString(3);
                    String email = rs.getString(4);
                    results.add(String.format("%s • %s • %s",
                            name != null ? name : "-",
                            (phone != null && !phone.isBlank()) ? phone : "No phone",
                            (email != null && !email.isBlank()) ? email : "No email"
                    ));
                }
            }
        } catch (SQLException ignored) { }
        Platform.runLater(() -> {
            guestSuggestionsListView.setItems(FXCollections.observableArrayList(results));
            if (results.isEmpty()) {
                hideOverlay();
                return;
            }
            int maxVisibleItems = Math.min(results.size(), 6);
            double itemHeight = 30; // Adjust based on your list item height
            double listHeight = maxVisibleItems * itemHeight + 16;
            guestSuggestionsListView.setPrefHeight(listHeight);
            guestSuggestionsListView.setMaxHeight(300);
            animateOverlayIn();
        });
    }

    /**
     * Handles the selection of a guest from the suggestions list.
     *
     * @param item The selected string from the ListView.
     */
    private void onGuestSuggestionSelected(String item) {
        String[] parts = item.split("•");
        String name = parts.length >= 1 ? parts[0].trim() : null;
        String phone = parts.length >= 2 ? parts[1].trim() : null;
        String email = parts.length >= 3 ? parts[2].trim() : null;
        Platform.runLater(() -> {
            loadGuestByUnique(name, phone, email);
            if (guestSearchField != null) guestSearchField.setText(name != null ? name : "");
            hideOverlay();
        });
    }

    /**
     * Loads guest data into the form fields based on unique identifiers.
     *
     * @param name The guest's name.
     * @param phone The guest's phone number.
     * @param email The guest's email address.
     */
    private void loadGuestByUnique(String name, String phone, String email) {
        String sqlPhone = "SELECT id, name, phone, email FROM guests WHERE phone = ? LIMIT 1";
        String sqlNameEmail = "SELECT id, name, phone, email FROM guests WHERE LOWER(name)=? AND LOWER(email)=? LIMIT 1";
        try (Connection c = DatabaseManager.getConnection()) {
            if (phone != null && !"No phone".equalsIgnoreCase(phone)) {
                try (PreparedStatement ps = c.prepareStatement(sqlPhone)) {
                    ps.setString(1, phone);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            fillGuestFieldsFromRS(rs);
                            return;
                        }
                    }
                }
            }
            if (name != null && email != null && !"No email".equalsIgnoreCase(email)) {
                try (PreparedStatement ps = c.prepareStatement(sqlNameEmail)) {
                    ps.setString(1, name.toLowerCase());
                    ps.setString(2, email.toLowerCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            fillGuestFieldsFromRS(rs);
                            return;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load guest: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        clearGuestFields();
    }

    /**
     * Fills the form's guest fields from a database ResultSet.
     *
     * @param rs The ResultSet containing guest data.
     */
    private void fillGuestFieldsFromRS(ResultSet rs) throws SQLException {
        selectedGuestId = rs.getLong("id");
        if (guestNameField != null) guestNameField.setText(rs.getString("name"));
        if (phoneField != null) phoneField.setText(rs.getString("phone"));
        if (emailField != null) emailField.setText(rs.getString("email"));
    }

    /**
     * Clears all guest-related fields on the form.
     */
    private void clearGuestFields() {
        selectedGuestId = null;
        guestNameField.setText("");
        phoneField.setText("");
        emailField.setText("");
    }

    /**
     * Animates the search suggestions overlay to slide and fade in.
     */
    private void animateOverlayIn() {
        if (!searchOverlay.isVisible()) {
            searchOverlay.setVisible(true);
            searchOverlay.setManaged(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), searchOverlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), guestSuggestionsListView);
            slideDown.setFromY(-20);
            slideDown.setToY(0);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), guestSuggestionsListView);
            scaleIn.setFromX(0.95);
            scaleIn.setFromY(0.95);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            new ParallelTransition(fadeIn, slideDown, scaleIn).play();
        }
    }

    /**
     * Hides the search suggestions overlay.
     */
    private void hideOverlay() {
        if (searchOverlay == null || guestSuggestionsListView == null) return;
        if (!searchOverlay.isVisible()) return;
        searchOverlay.setVisible(false);
        searchOverlay.setManaged(false);
        guestSuggestionsListView.getSelectionModel().clearSelection();
        guestSuggestionsListView.setItems(FXCollections.emptyObservableList());
    }

    private BigDecimal safeMoney(String s) {
        if (s == null || s.trim().isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    @FXML
    private void onDeleteReservation() {
        if (!isEditMode || selectedReservationId == null) {
            showAlert("Error", "No reservation selected for deletion.", Alert.AlertType.WARNING);
            return;
        }

        BigDecimal advancePaid = getAdvancePaidForReservation(selectedReservationId);

        if (advancePaid == null || advancePaid.compareTo(BigDecimal.ZERO) <= 0) {
            boolean confirmed = confirmDeletionWithoutRefund();
            if (confirmed) {
                performDeleteReservation();
            }
            return;
        }

        boolean refundCompleted = processRefundViaPaymentPage(selectedReservationId, advancePaid);

        if (refundCompleted) {
            performDeleteReservation();
        } else {
            showAlert("Refund Not Completed", "Reservation deletion cancelled as refund was not processed.", Alert.AlertType.INFORMATION);
        }
    }

// private void performDeleteReservation() {
//	    try {
////	        reservationsDAO.deleteFullReservation(selectedReservationId);
//	        showAlert("Success", "Reservation deleted successfully.", Alert.AlertType.INFORMATION);
//	        
//	        // Refresh parent controller if available
////	        if (parentController != null) {
////	            parentController.refreshReservationData();
////	        }
////	        
//	        closeWindow();
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        showAlert("Error", "Failed to delete reservation: " + e.getMessage(), Alert.AlertType.ERROR);
//	    }
//	}
 private boolean processRefundForReservation() {
	    try {
	        // Get payment information for this reservation
	        ReservationPaymentInfo paymentInfo = getReservationPaymentInfo(selectedReservationId);
	        
	        if (paymentInfo == null) {
	            Alert alert = new Alert(Alert.AlertType.WARNING);
	            alert.setTitle("No Payment Found");
	            alert.setHeaderText("No Payment to Refund");
	            alert.setContentText(
	                "No payment record found for this reservation.\n\n" +
	                "Do you want to proceed with deletion without refund?"
	            );
	            
	            ButtonType proceedButton = new ButtonType("Yes, Delete");
	            ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
	            alert.getButtonTypes().setAll(proceedButton, cancelButton);
	            
	            Optional<ButtonType> result = alert.showAndWait();
	            return result.isPresent() && result.get() == proceedButton;
	        }
	        
	        if (paymentInfo.amount.compareTo(BigDecimal.ZERO) <= 0) {
	            Alert alert = new Alert(Alert.AlertType.WARNING);
	            alert.setTitle("No Amount to Refund");
	            alert.setHeaderText("Zero Payment Amount");
	            alert.setContentText(
	                "The payment amount is ₹0.00. No refund needed.\n\n" +
	                "Do you want to proceed with deletion?"
	            );
	            
	            ButtonType proceedButton = new ButtonType("Yes, Delete");
	            ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
	            alert.getButtonTypes().setAll(proceedButton, cancelButton);
	            
	            Optional<ButtonType> result = alert.showAndWait();
	            return result.isPresent() && result.get() == proceedButton;
	        }
	        
	        // Show refund confirmation with amount
	        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
	        confirmAlert.setTitle("Process Refund");
	        confirmAlert.setHeaderText("Refund Payment - ₹" + String.format("%.2f", paymentInfo.amount.doubleValue()));
	        confirmAlert.setContentText(String.format(
	            "Payment Details:\n" +
	            "Guest: %s\n" +
	            "Room: %s\n" +
	            "Amount: ₹%.2f\n\n" +
	            "This will open the refund processing window.\n" +
	            "Proceed with refund?",
	            safeTrim(guestNameField.getText()),
	            roomsComboField.getValue(),
	            paymentInfo.amount.doubleValue()
	        ));
	        
	        Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
	        if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
	            // Open refund window
	            Window parentWindow = guestNameField.getScene().getWindow();
	            PaymentController.openRefundWindow(
	                paymentInfo.bookingId, 
	                paymentInfo.paymentId, 
	                paymentInfo.amount, 
	                parentWindow
	            );
	            
	            // Show completion message
	            showAlert("Refund Window Closed", 
	                     "Please verify the refund was processed before continuing with deletion.", 
	                     Alert.AlertType.INFORMATION);
	            
	            return true;
	        }
	        
	        return false; // User cancelled refund
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        showAlert("Refund Error", 
	                 "Failed to process refund: " + e.getMessage() + "\n\nDeletion cancelled.", 
	                 Alert.AlertType.ERROR);
	        return false;
	    }
	}
// private ReservationPaymentInfo getReservationPaymentInfo(Long reservationId) {
//	    try (Connection conn = DatabaseManager.getConnection()) {
//	        // First, find the booking associated with this reservation
//	        String bookingSql = "SELECT id FROM bookings WHERE reservation_id = ?";
//	        Long bookingId = null;
//	        
//	        try (PreparedStatement ps = conn.prepareStatement(bookingSql)) {
//	            ps.setLong(1, reservationId);
//	            
//	            try (ResultSet rs = ps.executeQuery()) {
//	                if (rs.next()) {
//	                    bookingId = rs.getLong("id");
//	                }
//	            }
//	        }
//	        
//	        if (bookingId == null) {
//	            // If no booking found, check for reservation payments directly
//	            String reservationPaymentSql = "SELECT id, amount FROM reservation_payments WHERE reservation_id = ? ORDER BY payment_date DESC LIMIT 1";
//	            
//	            try (PreparedStatement ps = conn.prepareStatement(reservationPaymentSql)) {
//	                ps.setLong(1, reservationId);
//	                
//	                try (ResultSet rs = ps.executeQuery()) {
//	                    if (rs.next()) {
//	                        ReservationPaymentInfo info = new ReservationPaymentInfo();
//	                        info.paymentId = rs.getLong("id");
//	                        info.amount = rs.getBigDecimal("amount");
//	                        info.bookingId = null; // No associated booking
//	                        return info;
//	                    }
//	                }
//	            }
//	        } else {
//	            // Look for booking payments
//	            String paymentSql = "SELECT id, amount FROM payments WHERE bookingid = ? ORDER BY paymentdate DESC LIMIT 1";
//	            
//	            try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
//	                ps.setLong(1, bookingId);
//	                
//	                try (ResultSet rs = ps.executeQuery()) {
//	                    if (rs.next()) {
//	                        ReservationPaymentInfo info = new ReservationPaymentInfo();
//	                        info.paymentId = rs.getLong("id");
//	                        info.amount = rs.getBigDecimal("amount");
//	                        info.bookingId = bookingId;
//	                        return info;
//	                    }
//	                }
//	            }
//	        }
//	        
//	    } catch (SQLException e) {
//	        e.printStackTrace();
//	    }
//	    return null;
//	}
 /**
  * Helper class for storing reservation payment information
  */
 private static class ReservationPaymentInfo {
     Long paymentId;
     Long bookingId;
     BigDecimal amount;
 }
 
 @FXML
 private void onRefundReservation() {
     if (!isEditMode || selectedReservationId == null) {
         showAlert("Error", "No reservation selected for refund.", Alert.AlertType.WARNING);
         return;
     }

     try {
         ReservationPaymentInfo paymentInfo = getReservationPaymentInfo(selectedReservationId);

         if (paymentInfo == null) {
             showAlert("No Payment Found", "No payment record found for this reservation.", Alert.AlertType.WARNING);
             return;
         }

         if (paymentInfo.amount.compareTo(BigDecimal.ZERO) <= 0) {
             showAlert("No Amount to Refund", "No amount available for refund.", Alert.AlertType.WARNING);
             return;
         }

         Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
         confirmAlert.setTitle("Confirm Reservation Refund");
         confirmAlert.setHeaderText("Process Reservation Refund");
         confirmAlert.setContentText(String.format(
             "Do you want to process a refund for:\n\n" +
             "Guest: %s\n" +
             "Room: %s\n" +
             "Dates: %s to %s\n" +
             "Payment Amount: ₹%.2f\n\n" +
             "This will open the refund processing window.",
             safeTrim(guestNameField.getText()),
             roomsComboField.getValue(),
             formCheckInDate.getValue() != null ? formCheckInDate.getValue().toString() : "N/A",
             formCheckOutDate.getValue() != null ? formCheckOutDate.getValue().toString() : "N/A",
             paymentInfo.amount.doubleValue()));

         Optional<ButtonType> result = confirmAlert.showAndWait();
         if (result.isPresent() && result.get() == ButtonType.OK) {
             Window parentWindow = guestNameField.getScene().getWindow();
             PaymentController.openRefundWindow(paymentInfo.bookingId, paymentInfo.paymentId, 
                                               paymentInfo.amount, parentWindow);
             showAlert("Refund Processed", "Refund window closed. Please check the refund status.", Alert.AlertType.INFORMATION);
         }
     } catch (Exception e) {
         e.printStackTrace();
         showAlert("Error", "Failed to process refund: " + e.getMessage(), Alert.AlertType.ERROR);
     }
 }
 
 
 private BigDecimal getAdvancePaidForReservation(Long reservationId) {
	    try (Connection conn = DatabaseManager.getConnection()) {
	        String sql = "SELECT COALESCE(advance_paid, 0) FROM reservations WHERE id = ?";
	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setLong(1, reservationId);
	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next()) {
	                    return rs.getBigDecimal(1);
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        showAlert("Error", "Failed to retrieve advance payment info: " + e.getMessage(), Alert.AlertType.ERROR);
	    }
	    return BigDecimal.ZERO;
	}

	private boolean confirmDeletionWithoutRefund() {
	    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
	    alert.setTitle("Delete Reservation");
	    alert.setHeaderText("No Advance Payment Found");
	    alert.setContentText("This reservation has no advance payment. Do you want to delete it directly?");

	    Optional<ButtonType> result = alert.showAndWait();
	    return result.isPresent() && result.get() == ButtonType.OK;
	}

	private boolean processRefundViaPaymentPage(Long reservationId, BigDecimal advanceAmount) {
	    // Fetch payment info
	    ReservationPaymentInfo paymentInfo = getReservationPaymentInfo(reservationId);
	    
	    if (paymentInfo == null) {
	        showAlert("No Payment Found", "No payment record found for this reservation. Cannot process refund.", Alert.AlertType.WARNING);
	        return false;
	    }
	    
	    // Build ReservationStub from current form data
	    ReservationStub reservation = buildReservationStubFromForm();
	    reservation.setId(reservationId);
	    
	    try {
	        Window parentWindow = guestNameField.getScene().getWindow();
	        
	        // Call the new method with reservation data
	        PaymentController.openRefundWindowWithReservation(
	            reservation,
	            paymentInfo.paymentId,
	            advanceAmount,
	            parentWindow
	        );
	        
	        // Confirm with user that refund was processed
	        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
	        confirmAlert.setTitle("Confirm Refund Completion");
	        confirmAlert.setHeaderText("Was the refund processed successfully?");
	        confirmAlert.setContentText("Click OK if the refund was completed successfully, otherwise click Cancel.");
	        
	        Optional<ButtonType> result = confirmAlert.showAndWait();
	        return result.isPresent() && result.get() == ButtonType.OK;
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        showAlert("Error", "Error opening refund window: " + e.getMessage(), Alert.AlertType.ERROR);
	        return false;
	    }
	}

	/**
	 * Build ReservationStub from current form fields
	 */
	private ReservationStub buildReservationStubFromForm() {
	    ReservationStub reservation = new ReservationStub();
	    
	    // Guest information
	    reservation.setGuestName(safeTrim(guestNameField.getText()));
	    reservation.setGuestId(selectedGuestId);
	    
	    // Room information
	    reservation.setRoomNo(roomsComboField.getValue());
	    
	    // Get room type from combo or database
	    if (roomCategoryCombo != null && roomCategoryCombo.getValue() != null) {
	        reservation.setRoomType(roomCategoryCombo.getValue());
	    }
	    
	    // Date information
	    reservation.setCheckInDate(formCheckInDate.getValue());
	    reservation.setCheckOutDate(formCheckOutDate.getValue());
	    
	    // Additional fields
	    if (noOfPersonsField != null && !noOfPersonsField.getText().isEmpty()) {
	        try {
	            reservation.setNumGuests(Integer.parseInt(noOfPersonsField.getText()));
	        } catch (NumberFormatException e) {
	            reservation.setNumGuests(1);
	        }
	    }
	    
	    if (specialRequestField != null) {
	        reservation.setNotes(safeTrim(specialRequestField.getText()));
	    }
	    
	    reservation.setStatus("Cancelled");
	    
	    return reservation;
	}


	private void performDeleteReservation() {
	    try {
	        reservationsDAO.deleteFullReservation(selectedReservationId);
	        showAlert("Success", "Reservation deleted successfully.", Alert.AlertType.INFORMATION);

	        closeWindow();
	    } catch (Exception e) {
	        e.printStackTrace();
	        showAlert("Error", "Failed to delete reservation: " + e.getMessage(), Alert.AlertType.ERROR);
	    }
	}

	private ReservationPaymentInfo getReservationPaymentInfo(Long reservationId) {
	    try (Connection conn = DatabaseManager.getConnection()) {
	        String bookingSql = "SELECT id FROM bookings WHERE reservation_id = ?";
	        Long bookingId = null;

	        try (PreparedStatement ps = conn.prepareStatement(bookingSql)) {
	            ps.setLong(1, reservationId);
	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next()) {
	                    bookingId = rs.getLong("id");
	                }
	            }
	        }

	        if (bookingId == null) {
	            String reservationPaymentSql = "SELECT id, amount FROM reservation_payments WHERE reservation_id = ? ORDER BY payment_date DESC LIMIT 1";
	            try (PreparedStatement ps = conn.prepareStatement(reservationPaymentSql)) {
	                ps.setLong(1, reservationId);
	                try (ResultSet rs = ps.executeQuery()) {
	                    if (rs.next()) {
	                        ReservationPaymentInfo info = new ReservationPaymentInfo();
	                        info.paymentId = rs.getLong("id");
	                        info.amount = rs.getBigDecimal("amount");
	                        info.bookingId = null;
	                        return info;
	                    }
	                }
	            }
	        } else {
	            String paymentSql = "SELECT id, amount FROM payments WHERE bookingid = ? ORDER BY paymentdate DESC LIMIT 1";
	            try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
	                ps.setLong(1, bookingId);
	                try (ResultSet rs = ps.executeQuery()) {
	                    if (rs.next()) {
	                        ReservationPaymentInfo info = new ReservationPaymentInfo();
	                        info.paymentId = rs.getLong("id");
	                        info.amount = rs.getBigDecimal("amount");
	                        info.bookingId = bookingId;
	                        return info;
	                    }
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

//	private static class ReservationPaymentInfo {
//	    Long paymentId;
//	    Long bookingId;
//	    BigDecimal amount;
//	}

	private void showAlert(String title, String message, Alert.AlertType type) {
	    Alert alert = new Alert(type);
	    alert.setTitle(title);
	    alert.setContentText(message);
	    alert.showAndWait();
	}

	private void closeWindow() {
	    Stage stage = (Stage) guestNameField.getScene().getWindow();
	    stage.close();
	}


}
