package application.controllers;

import application.services.dao.DatabaseManager;
import application.services.dao.PhotoIdVerificationDAO;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import application.utils.PropertyReader;
import application.models.PhotoIdVerification;
import application.models.TodayReservation;
import application.services.GstCalculationService;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BookingController {

    @FXML private TextField fullNameField;
    @FXML private TextField mobileNumberField;
    @FXML private TextField emailField;
    @FXML private Label photoIdPathLabel;
    @FXML private TextField gstNumberField;
    @FXML private ComboBox<String> sourceWebsiteComboBox;
    @FXML private TextField referencePersonField;
    @FXML private TextArea addressArea;
    @FXML private CheckBox wifiCheckbox;
    @FXML private CheckBox breakfastCheckbox;
    @FXML private CheckBox extraBedCheckbox;
    @FXML private CheckBox parkingCheckbox;

    @FXML private ComboBox<String> roomTypeComboBox;
    @FXML private ComboBox<String> roomNumberComboBox;
    @FXML private Label availabilityLabel;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private TextField totalDaysField;
    @FXML private ComboBox<String> autoFillComboBox;
    @FXML private TextField roomPriceField;
    @FXML private TextField advancePaymentField;

    @FXML private ToggleButton ocrToggle;
    @FXML private TextField guestSearchField;
    @FXML private ListView<String> guestSuggestionsListView;
    @FXML private VBox searchOverlay;

    @FXML private TextField bookingIdField;
    @FXML private TextField guestIdField;


    @FXML private Label guestStatusLabel;
    @FXML private Label summaryRoomRate;
    @FXML private Label summaryNights;
    @FXML private Label summaryTotal;
    @FXML private Label summaryAdvance;
    @FXML private Label summaryPending;
    @FXML private ScrollPane mainScrollPane;
    
    @FXML private ComboBox<String> nationalityField;

    @FXML private ToggleButton gstToggle; // GST Included/Excluded toggle
    @FXML private Label baseAmountLabel;
    @FXML private Label gstAmountLabel;
    @FXML private Label gstBreakdownLabel;
    
    private final PhotoIdVerificationDAO photoIdVerificationDAO = new PhotoIdVerificationDAO();
    @FXML private Label verificationStatusLabel;
    @FXML private Label documentStatusLabel;
    @FXML private Label photoIdStatusLabel;
    @FXML private Label allPersonsStatusLabel;
    @FXML private Label overallVerificationStatusLabel;
    @FXML private Button photoIdVerificationBtn;
    

    private final RoomDAO roomDAO = new RoomDAO();
    private final ObservableList<String> sourceOptions = FXCollections.observableArrayList();
    private final ObservableList<String> allRoomTypes = FXCollections.observableArrayList();
    private final Map<String, BigDecimal> roomRatesByRoomNo = new HashMap<>();
    private final Map<String, String> roomTypeByRoomNo = new HashMap<>();

    private Long selectedGuestId = null;
    private String selectedRoomType = null;
    private String selectedRoomNo = null;
    private BigDecimal selectedRoomRate = BigDecimal.ZERO;


    private final GstCalculationService gstService = GstCalculationService.getInstance();
    private String documentLink = null; // Store document file path
       private PropertyReader propertyReader;
       public void initialize() {
           try {
               propertyReader = PropertyReader.getInstance();
               if (bookingIdField != null) {
                   bookingIdField.setText(generateTempBookingCode());
                   bookingIdField.setEditable(false);
               }
               if (guestIdField != null) guestIdField.setEditable(false);
               if (ocrToggle != null) ocrToggle.setSelected(false);
               updateGuestStatus("New Guest");
               initSourceOptions();
               initRoomData();
               initAutoFillOptions();
               initNationalityOptions();
               initListeners();
               initYouTubeStyleSearch();
               updateSummaryAndPrice();
               
               // Initialize verification status
               initializeVerificationStatus();
               
               double gstRate = propertyReader.getGstRate();
               System.out.println("gst rate " + gstRate);
               
               // Initialize GST toggle
               if (gstToggle != null) {
                   gstToggle.setText("GST Excluded");
                   gstToggle.setSelected(false);
                   gstToggle.selectedProperty().addListener((obs, old, selected) -> {
                       gstToggle.setText(selected ? "GST Included" : "GST Excluded");
                       updateSummaryAndPrice();
                   });
               }
           } catch (Exception e) {
               showModernAlert(Alert.AlertType.ERROR, "Initialization Error", e.getMessage());
           }
       }
    private void initNationalityOptions() {
        if (nationalityField != null && propertyReader != null) {
            String[] countries = propertyReader.getAllNationalities();
            nationalityField.setItems(FXCollections.observableArrayList(countries));
            
            // Set default nationality
            String defaultNationality = propertyReader.getDefaultNationality();
            nationalityField.setValue(defaultNationality);
        }
    }

    private void initSourceOptions() {
        sourceOptions.clear();
        String sql = "SELECT source_name FROM reference_sources ORDER BY source_name";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) sourceOptions.add(rs.getString(1));
        } catch (SQLException e) {
            if (sourceOptions.isEmpty()) {
                sourceOptions.addAll("Walk-in", "Direct Website", "Expedia", "Booking.com", "Agoda", "MakeMyTrip", "Goibibo", "Phone Booking", "Other OTA");
            }
        }
        if (sourceWebsiteComboBox != null) sourceWebsiteComboBox.setItems(sourceOptions);
    }

    private void initRoomData() {
        try {
            var rooms = roomDAO.getAllRooms();
            Set<String> types = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            roomRatesByRoomNo.clear();
            roomTypeByRoomNo.clear();
            for (var r : rooms) {
                if (r.getRoomType() != null) types.add(r.getRoomType());
                if (r.getRoomNo() != null && r.getPrice() != null) {
                    roomRatesByRoomNo.put(r.getRoomNo(), r.getPrice());
                }
                if (r.getRoomNo() != null && r.getRoomType() != null) {
                    roomTypeByRoomNo.put(r.getRoomNo(), r.getRoomType());
                }
            }
            allRoomTypes.setAll(types);
            if (roomTypeComboBox != null) roomTypeComboBox.setItems(allRoomTypes);
            refreshRoomNumbers(null, null, null, null);
        } catch (SQLException e) {
            showModernAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load rooms: " + e.getMessage());
        }
    }

    private void initAutoFillOptions() {
        if (autoFillComboBox != null) {
            autoFillComboBox.setItems(FXCollections.observableArrayList(
                    "Today Check-in", "Tomorrow Check-in", "Weekend Package", "Week Long Stay", "Business Trip"
            ));
        }
    }

    private void initListeners() {
        if (roomTypeComboBox != null) {
            roomTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                selectedRoomType = n;
                refreshRoomNumbers(selectedRoomType, checkInDatePicker.getValue(), checkOutDatePicker.getValue(), selectedRoomNo);
                updateSummaryAndPrice();
                addModernVisualFeedback(roomTypeComboBox);
            });
        }

        if (roomNumberComboBox != null) {
            roomNumberComboBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                selectedRoomNo = n;
                onRoomNoSelected();
                addModernVisualFeedback(roomNumberComboBox);
            });
        }

        if (checkInDatePicker != null) {
            checkInDatePicker.valueProperty().addListener((obs, o, n) -> {
                refreshRoomNumbers(selectedRoomType, n, checkOutDatePicker.getValue(), selectedRoomNo);
                validateDates();
                updateSummaryAndPrice();
            });
        }

        if (checkOutDatePicker != null) {
            checkOutDatePicker.valueProperty().addListener((obs, o, n) -> {
                refreshRoomNumbers(selectedRoomType, checkInDatePicker.getValue(), n, selectedRoomNo);
                validateDates();
                updateSummaryAndPrice();
            });
        }

        // ADD THIS NEW LISTENER FOR ROOM PRICE FIELD
        if (roomPriceField != null) {
            roomPriceField.textProperty().addListener((obs, oldVal, newVal) -> updateSummaryAndPrice());
        }

        if (advancePaymentField != null) {
            advancePaymentField.textProperty().addListener((obs, o, n) -> updateSummaryAndPrice());
        }

        if (autoFillComboBox != null) {
            autoFillComboBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                if (n != null) handleAutoFill(n);
            });
        }
    }

    private void initYouTubeStyleSearch() {
        if (guestSearchField == null || guestSuggestionsListView == null || searchOverlay == null) return;
        hideOverlay();

        guestSuggestionsListView.setOnMouseClicked(e -> {
            String selectedItem = guestSuggestionsListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && !selectedItem.isEmpty()) {
                onGuestSuggestionSelected(selectedItem);
            }
        });
        guestSuggestionsListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    onGuestSuggestionSelected(newVal);
                }
            }
        );
        guestSearchField.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.isBlank()) {
                showSuggestions(n.trim());
            } else {
                clearGuestIfSearchEmpty();
                hideOverlay();
            }
        });
        guestSearchField.focusedProperty().addListener((obs, o, focused) -> {
            if (!focused && !guestSuggestionsListView.isFocused()) hideOverlayWithDelay();
        });
        guestSuggestionsListView.focusedProperty().addListener((obs, o, focused) -> {
            if (!focused && !guestSearchField.isFocused()) hideOverlayWithDelay();
        });
    }

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
            guestSuggestionsListView.getSelectionModel().clearSelection();
            if (results.isEmpty()) {
                hideOverlay();
                return;
            }
            int maxVisibleItems = Math.min(results.size(), 6);
            double itemHeight = 45;
            double listHeight = maxVisibleItems * itemHeight + 16;
            guestSuggestionsListView.setPrefHeight(listHeight);
            guestSuggestionsListView.setMaxHeight(300);
            animateOverlayIn();
        });
    }

    private void onGuestSuggestionSelected(String item) {
        String[] parts = item.split("•");
        String name = parts.length >= 1 ? parts[0].trim() : null;
        String phone = parts.length >= 2 ? parts[1].trim() : null;
        String email = parts.length >= 3 ? parts[2].trim() : null;

        // Wrap in Platform.runLater for JavaFX thread safety
        Platform.runLater(() -> {
            loadGuestByUnique(name, phone, email);
            if (guestSearchField != null) guestSearchField.setText(name != null ? name : "");
            hideOverlay();
        });
    }

    private void loadGuestByUnique(String name, String phone, String email) {
        String sqlPhone = "SELECT id, name, phone, email, address, gst_number FROM guests WHERE phone = ? LIMIT 1";
        String sqlNameEmail = "SELECT id, name, phone, email, address, gst_number FROM guests WHERE LOWER(name)=? AND LOWER(email)=? LIMIT 1";
        try (Connection c = DatabaseManager.getConnection()) {
            if (phone != null && !"No phone".equalsIgnoreCase(phone)) {
                try (PreparedStatement ps = c.prepareStatement(sqlPhone)) {
                    ps.setString(1, phone);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { fillGuestFieldsFromRS(rs); return; }
                    }
                }
            }
            if (name != null && email != null && !"No email".equalsIgnoreCase(email)) {
                try (PreparedStatement ps = c.prepareStatement(sqlNameEmail)) {
                    ps.setString(1, name.toLowerCase());
                    ps.setString(2, email.toLowerCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { fillGuestFieldsFromRS(rs); return; }
                    }
                }
            }
        } catch (SQLException e) {
            showModernAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load guest: " + e.getMessage());
        }
        selectedGuestId = null;
        if (guestIdField != null) guestIdField.setText("");
        if (fullNameField != null) fullNameField.setText(name != null && !name.isBlank() ? name : "");
        if (mobileNumberField != null && phone != null && !"No phone".equalsIgnoreCase(phone)) mobileNumberField.setText(phone);
        if (emailField != null && email != null && !"No email".equalsIgnoreCase(email)) emailField.setText(email);
        updateGuestStatus("New Guest");
    }

    private void fillGuestFieldsFromRS(ResultSet rs) throws SQLException {
        selectedGuestId = rs.getLong("id");
        if (guestIdField != null) guestIdField.setText(String.valueOf(selectedGuestId));
        if (fullNameField != null) fullNameField.setText(rs.getString("name"));
        if (mobileNumberField != null) mobileNumberField.setText(rs.getString("phone"));
        if (emailField != null) emailField.setText(rs.getString("email"));
        if (addressArea != null) addressArea.setText(rs.getString("address"));
        if (gstNumberField != null) gstNumberField.setText(rs.getString("gst_number"));
        updateGuestStatus("Returning Guest");
    }

    // ... (other methods unchanged – see previous full versions)
    // Continue with handleAutoFill, validateDates, updateAvailabilityLabel, showModernAlert, etc.

    // The rest of the controller is unchanged from the version in earlier answers unless you have more bugs to fix.
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

    private void hideOverlay() {
        if (searchOverlay == null || guestSuggestionsListView == null) return;
        if (!searchOverlay.isVisible()) return;
        searchOverlay.setVisible(false);
        searchOverlay.setManaged(false);
        // Defer clearing to avoid selection model IndexOutOfBounds
        javafx.application.Platform.runLater(() -> {
            var items = guestSuggestionsListView.getItems();
            if (items != null) items.clear();
            var sm = guestSuggestionsListView.getSelectionModel();
            if (sm != null) sm.clearSelection();
            guestSuggestionsListView.setPrefHeight(0);
        });
    }

    private void hideOverlayWithDelay() {
        javafx.application.Platform.runLater(this::hideOverlay);
    }

    private void clearGuestIfSearchEmpty() {
        if (guestSearchField == null) return;
        if (guestSearchField.getText() == null || guestSearchField.getText().isBlank()) {
            selectedGuestId = null;
            if (guestIdField != null) guestIdField.setText("");
            updateGuestStatus("New Guest");
        }
    }

    // ---------------- UI helpers & summary ----------------

    private void updateGuestStatus(String status) {
        if (guestStatusLabel == null) return;
        guestStatusLabel.setText(status);
        guestStatusLabel.getStyleClass().clear();
        guestStatusLabel.getStyleClass().add("modern-status-badge");
        if ("Returning Guest".equalsIgnoreCase(status)) {
            guestStatusLabel.getStyleClass().add("status-badge-returning");
        }
    }

    private void addModernVisualFeedback(Control node) {
        if (node == null) return;
        node.getStyleClass().add("highlight");
        javafx.application.Platform.runLater(() -> node.getStyleClass().remove("highlight"));
    }

    private void handleAutoFill(String option) {
        LocalDate today = LocalDate.now();
        switch (option) {
            case "Today Check-in" -> { checkInDatePicker.setValue(today); checkOutDatePicker.setValue(today.plusDays(1)); }
            case "Tomorrow Check-in" -> { checkInDatePicker.setValue(today.plusDays(1)); checkOutDatePicker.setValue(today.plusDays(2)); }
            case "Weekend Package" -> {
                int dow = today.getDayOfWeek().getValue();
                int addToFri = (5 - dow + 7) % 7;
                LocalDate friday = today.plusDays(addToFri);
                checkInDatePicker.setValue(friday);
                checkOutDatePicker.setValue(friday.plusDays(2));
            }
            case "Week Long Stay" -> { checkInDatePicker.setValue(today); checkOutDatePicker.setValue(today.plusDays(7)); }
            case "Business Trip" -> { checkInDatePicker.setValue(today); checkOutDatePicker.setValue(today.plusDays(3)); }
        }
        validateDates();
        updateSummaryAndPrice();
    }

    private void validateDates() {
        LocalDate ci = checkInDatePicker.getValue();
        LocalDate co = checkOutDatePicker.getValue();
        if (ci != null && co != null && !co.isAfter(ci)) {
            addError(checkInDatePicker); addError(checkOutDatePicker);
            availabilityLabel.setText("❗ Check-out must be after Check-in");
        } else {
            removeError(checkInDatePicker); removeError(checkOutDatePicker);
        }
        updateAvailabilityLabel();
        updateTotalDaysField();
    }

    private void updateTotalDaysField() {
        if (totalDaysField == null) return;
        LocalDate ci = checkInDatePicker.getValue();
        LocalDate co = checkOutDatePicker.getValue();
        if (ci != null && co != null && co.isAfter(ci)) {
            long nights = ChronoUnit.DAYS.between(ci, co);
            totalDaysField.setText(String.valueOf(nights));
        } else {
            totalDaysField.clear();
        }
    }

    private void addError(Control c) { if (c != null && !c.getStyleClass().contains("error")) c.getStyleClass().add("error"); }
    private void removeError(Control c) { if (c != null) c.getStyleClass().remove("error"); }

    private void updateAvailabilityLabel() {
        if (availabilityLabel == null) return;
        if (selectedRoomNo == null || checkInDatePicker.getValue() == null || checkOutDatePicker.getValue() == null) {
            availabilityLabel.setText("⏳ Select room and dates");
            return;
        }
        boolean available = isRoomAvailable(selectedRoomNo, checkInDatePicker.getValue(), checkOutDatePicker.getValue());
        availabilityLabel.setText(available ? "✅ Available" : "❌ Not Available");
    }
    private void updateSummaryAndPrice() {
        long nights = 0;
        LocalDate ci = checkInDatePicker.getValue();
        LocalDate co = checkOutDatePicker.getValue();
        if (ci != null && co != null && co.isAfter(ci)) {
            nights = ChronoUnit.DAYS.between(ci, co);
        }
        
        if (summaryNights != null) summaryNights.setText(String.valueOf(nights));

        BigDecimal rate = selectedRoomRate;
        // allow manual override
        BigDecimal manual = parseCurrencyToBD(roomPriceField != null ? roomPriceField.getText() : null);
        if (manual != null && manual.compareTo(BigDecimal.ZERO) > 0) rate = manual;

        BigDecimal roomAmount = rate.multiply(BigDecimal.valueOf(nights));
        
        // Apply GST calculation based on toggle
        boolean gstIncluded = gstToggle != null && gstToggle.isSelected();
        GstCalculationService.GstResult gstResult = gstService.calculate(roomAmount, gstIncluded);
        
        // Update UI with GST breakdown
        if (summaryRoomRate != null) summaryRoomRate.setText(toCurrency(rate));
        if (baseAmountLabel != null) baseAmountLabel.setText(toCurrency(gstResult.getBaseAmount()));
        if (gstAmountLabel != null) gstAmountLabel.setText(toCurrency(gstResult.getTaxAmount()));
        if (gstBreakdownLabel != null) {
            gstBreakdownLabel.setText(String.format("GST @ %.1f%%", gstResult.getGstRate()));
        }
        
        BigDecimal totalWithGst = gstResult.getTotalAmount();
        if (summaryTotal != null) summaryTotal.setText(toCurrency(totalWithGst));

        BigDecimal advance = parseCurrencyToBD(advancePaymentField != null ? advancePaymentField.getText() : null);
        if (summaryAdvance != null) summaryAdvance.setText(toCurrency(advance));

        BigDecimal pending = totalWithGst.subtract(advance);
        if (pending.compareTo(BigDecimal.ZERO) < 0) pending = BigDecimal.ZERO;
        if (summaryPending != null) summaryPending.setText(toCurrency(pending));
    }

    // ---------------- Rooms: loading & availability ----------------

    private void refreshRoomNumbers(String roomType, LocalDate ci, LocalDate co, String keepSelectedIfValid) {
        if (roomNumberComboBox == null) return;
        ObservableList<String> roomNos = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder("SELECT room_no, room_type, price, status FROM rooms");
        List<String> where = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (roomType != null && !roomType.isBlank()) {
            where.add("LOWER(room_type) = ?");
            params.add(roomType.toLowerCase());
        }
        if (!where.isEmpty()) sql.append(" WHERE ").append(String.join(" AND ", where));
        sql.append(" ORDER BY room_no");

        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rn = rs.getString("room_no");
                    String rt = rs.getString("room_type");
                    BigDecimal price = rs.getBigDecimal("price");
                    roomRatesByRoomNo.put(rn, price != null ? price : BigDecimal.ZERO);
                    roomTypeByRoomNo.put(rn, rt);

                    boolean ok = true;
                    if (ci != null && co != null && co.isAfter(ci)) {
                        ok = isRoomAvailable(rn, ci, co);
                    }
                    if (ok) roomNos.add(rn);
                }
            }
        } catch (SQLException e) {
            showModernAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load rooms: " + e.getMessage());
        }

        roomNumberComboBox.setItems(roomNos);
        if (keepSelectedIfValid != null && roomNos.contains(keepSelectedIfValid)) {
            roomNumberComboBox.getSelectionModel().select(keepSelectedIfValid);
        } else if (!roomNos.isEmpty()) {
            roomNumberComboBox.getSelectionModel().selectFirst();
        }
    }

    private boolean isRoomAvailable(String roomNo, LocalDate ci, LocalDate co) {
        String sql = """
                SELECT COUNT(*) FROM bookings
                WHERE room_no = ?
                AND status IN ('Checked-in','Confirmed','Reserved')
                AND (? < check_out_date) AND (? > check_in_date)
                """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roomNo);
            ps.setDate(2, Date.valueOf(co));
            ps.setDate(3, Date.valueOf(ci));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 0;
            }
        } catch (SQLException ignored) { }
        // In case of error, don't block booking
        return true;
    }

    private void onRoomNoSelected() {
        selectedRoomRate = roomRatesByRoomNo.getOrDefault(selectedRoomNo, BigDecimal.ZERO);
        if (roomPriceField != null) roomPriceField.setText(toCurrency(selectedRoomRate));
        updateAvailabilityLabel();
        updateSummaryAndPrice();
    }

    // ---------------- Actions wired from FXML ----------------

//    @FXML
//    private void handleUploadPhotoId(javafx.event.ActionEvent event) {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Select Photo ID Document");
//        fileChooser.getExtensionFilters().addAll(
//            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
//            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
//            new FileChooser.ExtensionFilter("All Files", "*.*")
//        );
//
//        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
//        File selectedFile = fileChooser.showOpenDialog(stage);
//
//        if (selectedFile != null) {
//            // Store document link
//            documentLink = selectedFile.getAbsolutePath();
//            
//            if (photoIdPathLabel != null) {
//                String fileName = selectedFile.getName();
//                photoIdPathLabel.setText("📄 " + fileName);
//                photoIdPathLabel.setTooltip(new Tooltip(selectedFile.getAbsolutePath()));
//                photoIdPathLabel.getStyleClass().clear();
//                photoIdPathLabel.getStyleClass().add("modern-upload-status");
//                photoIdPathLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
//                addModernVisualFeedback(photoIdPathLabel);
//            }
//        } else {
//            documentLink = null;
//            if (photoIdPathLabel != null) {
//                photoIdPathLabel.setText("No document uploaded");
//                photoIdPathLabel.setStyle("-fx-text-fill: #a0aec0;");
//            }
//        }
//    }


    @FXML
    private void handleOcrToggle(javafx.event.ActionEvent event) {
        boolean isOcrEnabled = ocrToggle != null && ocrToggle.isSelected();
        setFieldsDisabled(isOcrEnabled);
        if (isOcrEnabled) simulateModernOcrProcessing();
        hideOverlay();
    }

    private void setFieldsDisabled(boolean disabled) {
        if (fullNameField != null) fullNameField.setDisable(disabled);
        if (mobileNumberField != null) mobileNumberField.setDisable(disabled);
        if (emailField != null) emailField.setDisable(disabled);
        if (gstNumberField != null) gstNumberField.setDisable(disabled);
        if (addressArea != null) addressArea.setDisable(disabled);
        if (guestSearchField != null) guestSearchField.setDisable(disabled);
        if (nationalityField != null) nationalityField.setDisable(disabled);  // ADD THIS LINE
        
        Node uploadButton = findUploadButton();
        if (uploadButton != null) uploadButton.setDisable(disabled);
    }

    private Node findUploadButton() {
        if (photoIdPathLabel != null && photoIdPathLabel.getParent() instanceof HBox parentHBox) {
            for (Node node : parentHBox.getChildren()) {
                if (node instanceof Button b && b.getText().contains("Upload")) return node;
            }
        }
        return null;
    }

    private void simulateModernOcrProcessing() {
        if (fullNameField != null) fullNameField.getStyleClass().add("loading");
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(1000);
                javafx.application.Platform.runLater(() -> {
                    if (fullNameField != null) {
                        fullNameField.getStyleClass().remove("loading");
                        fullNameField.setText("🤖 John Doe (AI Extracted)");
                    }
                    if (mobileNumberField != null) mobileNumberField.setText("9876543210");
                    if (emailField != null) emailField.setText("john.doe@email.com");
                    if (addressArea != null) addressArea.setText("123 Main Street, Smart City, Digital State - 123456");
                    showModernAlert(Alert.AlertType.INFORMATION, "AI Processing Complete",
                            "🤖 Document processed successfully!\n\nGuest details have been automatically extracted.");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @FXML
    private void handleSearchGuest() {
        if (guestSearchField == null) return;
        String q = guestSearchField.getText();
        if (q == null || q.isBlank()) {
            showModernAlert(Alert.AlertType.INFORMATION, "Search", "Enter name/phone/email to search.");
            return;
        }
        showSuggestions(q.trim());
    }

    @FXML
    public void handleReset() {
        // Clear guest block
    	 selectedGuestId = null;
    	    if (guestIdField != null) guestIdField.clear();
    	    if (fullNameField != null) fullNameField.clear();
    	    if (mobileNumberField != null) mobileNumberField.clear();
    	    if (emailField != null) emailField.clear();
    	    if (gstNumberField != null) gstNumberField.clear();
    	    if (referencePersonField != null) referencePersonField.clear();
    	    if (addressArea != null) addressArea.clear();
    	    if (guestSearchField != null) guestSearchField.clear();
    	    if (nationalityField != null && propertyReader != null) {
    	        nationalityField.setValue(propertyReader.getDefaultNationality());
    	    } // ADD THIS LINE
    	    updateGuestStatus("New Guest");

        // Photo ID label
        if (photoIdPathLabel != null) {
            photoIdPathLabel.setText("No document uploaded");
            photoIdPathLabel.setStyle("-fx-text-fill: #a0aec0;");
        }

        // Stay details
        if (roomTypeComboBox != null) roomTypeComboBox.getSelectionModel().clearSelection();
        if (roomNumberComboBox != null) roomNumberComboBox.getSelectionModel().clearSelection();
        if (sourceWebsiteComboBox != null) sourceWebsiteComboBox.getSelectionModel().clearSelection();
        if (autoFillComboBox != null) autoFillComboBox.getSelectionModel().clearSelection();
        if (checkInDatePicker != null) checkInDatePicker.setValue(null);
        if (checkOutDatePicker != null) checkOutDatePicker.setValue(null);
        if (totalDaysField != null) totalDaysField.clear();
        if (roomPriceField != null) roomPriceField.setText("₹0.00");
        if (advancePaymentField != null) advancePaymentField.clear();

        // Checkboxes
        if (wifiCheckbox != null) wifiCheckbox.setSelected(false);
        if (breakfastCheckbox != null) breakfastCheckbox.setSelected(false);
        if (extraBedCheckbox != null) extraBedCheckbox.setSelected(false);
        if (parkingCheckbox != null) parkingCheckbox.setSelected(false);

        // Summary
        if (summaryRoomRate != null) summaryRoomRate.setText("₹0.00");
        if (summaryNights != null) summaryNights.setText("0");
        if (summaryTotal != null) summaryTotal.setText("₹0.00");
        if (summaryAdvance != null) summaryAdvance.setText("₹0.00");
        if (summaryPending != null) summaryPending.setText("₹0.00");
        if (availabilityLabel != null) availabilityLabel.setText("⏳ Select room and dates");
     // Reset GST toggle
        if (gstToggle != null) {
            gstToggle.setSelected(false);
            gstToggle.setText("GST Excluded");
        }

        // Reset GST labels  
        if (baseAmountLabel != null) baseAmountLabel.setText("₹0.00");
        if (gstAmountLabel != null) gstAmountLabel.setText("₹0.00");
        if (gstBreakdownLabel != null) gstBreakdownLabel.setText("GST @ 18.0%");


        // New temp booking code
        if (bookingIdField != null) bookingIdField.setText(generateTempBookingCode());

        // Refresh rooms
        refreshRoomNumbers(null, null, null, null);

        showModernAlert(Alert.AlertType.INFORMATION, "Form Reset Complete",
                "✅ All form fields have been cleared successfully.\n\nYou can now start a new booking.");
    }
    

    @FXML
    private void handleCancelBooking() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancel");
        confirm.setHeaderText("Cancel Current Booking?");
        confirm.setContentText("Are you sure you want to cancel the current booking process and return to Home?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
                Parent home = loader.load();
                Stage stage = (Stage) fullNameField.getScene().getWindow();
                stage.setScene(new Scene(home));
                stage.setTitle("Hotel Management - Dashboard");
                stage.show();
            } catch (Exception e) {
                showModernAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load Home screen.");
            }
        }
    }

    
    @FXML
    private void handleProceedToPayment() {
        // 1) Validate
        if (!validateRequiredFields()) return;

        // 2) Collect UI data
        String name = safe(fullNameField.getText());
        String phone = safe(mobileNumberField.getText());
        String email = safe(emailField.getText());
        String address = safe(addressArea.getText());
        String gst = safe(gstNumberField.getText());
        LocalDate ci = checkInDatePicker.getValue();
        LocalDate co = checkOutDatePicker.getValue();

        if (ci == null || co == null || !co.isAfter(ci)) {
            showModernAlert(Alert.AlertType.WARNING, "Invalid Dates", "Check-out must be after check-in.");
            return;
        }

        long nights = ChronoUnit.DAYS.between(ci, co);
        if (nights <= 0) {
            showModernAlert(Alert.AlertType.WARNING, "Invalid Stay", "Stay must be at least 1 night.");
            return;
        }
        System.out.println("This is new night : "+ nights);

        // Ensure a room is selected
        if (selectedRoomNo == null || selectedRoomNo.isBlank()) {
            showModernAlert(Alert.AlertType.WARNING, "Room Required", "Please select a room number.");
            return;
        }

        // 3) Compute rate and totals from UI (fallback to selectedRoomRate)
        BigDecimal rate = parseCurrencyToBD(roomPriceField != null ? roomPriceField.getText() : null);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            rate = selectedRoomRate != null ? selectedRoomRate : BigDecimal.ZERO;
        }

//        BigDecimal total = rate.multiply(BigDecimal.valueOf(nights));
        BigDecimal advance = parseCurrencyToBD(advancePaymentField != null ? advancePaymentField.getText() : null);
        if (advance == null) advance = BigDecimal.ZERO;

        // 4) Re-check availability (soft check; final check happens on confirm)
        if (!isRoomAvailable(selectedRoomNo, ci, co)) {
            showModernAlert(Alert.AlertType.WARNING, "Not Available", "Room is no longer available for the selected dates.");
            refreshRoomNumbers(selectedRoomType, ci, co, null);
            return;
        }

        // 5) Collect additional booking data
        String nationality = "India"; // Default nationality
        String documentPath = null;
        if (photoIdPathLabel != null && !photoIdPathLabel.getText().equals("No document uploaded")) {
            Tooltip tooltip = photoIdPathLabel.getTooltip();
            if (tooltip != null) {
                documentPath = tooltip.getText();
            }
        }
       
        BigDecimal charge = rate.multiply(new BigDecimal(nights));
        // Calculate GST components
     // Get GST toggle state and calculate properly  
        boolean gstIncluded = gstToggle != null && gstToggle.isSelected();
        System.out.println("This is new  gstIncluded : "+  gstIncluded );
        GstCalculationService.GstResult gstResult = gstService.calculate(charge , gstIncluded);

        // Use calculated values from GST service
        BigDecimal baseAmount = gstResult.getBaseAmount();
        BigDecimal gstAmount = gstResult.getTaxAmount();
        BigDecimal total = gstResult.getTotalAmount();
        System.out.println("This is new BaseAmount : "+baseAmount);
        System.out.println("This is new GST : "+gstAmount);
//        System.out.println("This is new Total : "+total);
        BigDecimal gstRate = new BigDecimal(gstResult.getGstRate());
        // Collect source information
        String sourceWebsite = sourceWebsiteComboBox != null && sourceWebsiteComboBox.getValue() != null 
            ? sourceWebsiteComboBox.getValue().toString() : "Direct";

        // 6) Open Payment window with COMPLETE context (NO DATABASE WRITE YET)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Payment.fxml"));
            Parent paymentRoot = loader.load();
            PaymentController paymentController = loader.getController();

            System.out.println("This is new Total : "+ gstResult.getTotalAmount());
            paymentController.setCompleteNewBookingContext(
            	    selectedGuestId,
            	    name, phone, email, address, gst,
            	    selectedRoomNo,
            	    roomTypeByRoomNo.getOrDefault(selectedRoomNo, "Standard"),
            	    ci, co,
            	    rate, // rate per night
            	    gstResult.getTotalAmount(), // total amount with proper GST
            	    advance, // advance payment
            	    nationality,
            	    documentPath,
            	    gstRate,
            	    baseAmount,
            	    gstAmount,
            	    gstIncluded, // pass the actual toggle state
            	    sourceWebsite
            	);

            paymentController.populateForNewBookingContext();

            Stage paymentStage = new Stage();
            paymentStage.setScene(new Scene(paymentRoot));
            paymentStage.setTitle("Complete Payment - Hotel Management");
            paymentStage.initModality(Modality.WINDOW_MODAL);
            paymentStage.initOwner(fullNameField.getScene().getWindow());
            paymentStage.setResizable(false);
            paymentStage.showAndWait();

            if (paymentController.isPaymentSuccessful()) {
                showModernAlert(Alert.AlertType.INFORMATION, "Success", 
                    "✅ Payment completed successfully!\n\nBooking has been confirmed and saved to database.");
                handleReset(); // reset the form for new booking
            } else {
                showModernAlert(Alert.AlertType.INFORMATION, "Payment Cancelled", 
                    "Payment was cancelled. No booking has been created in the database.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showModernAlert(Alert.AlertType.ERROR, "Loading Error", 
                "Could not load Payment screen. Details: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showModernAlert(Alert.AlertType.ERROR, "Application Error", 
                "An unexpected error occurred: " + e.getMessage());
        }
    }


    // ---------------- Validation & utils ----------------
    /**
     * Populate booking form from reservation data
     */
    public void populateFromReservation(TodayReservation reservation) {
        try {
            // Set guest information from reservation
            if (fullNameField != null) {
                fullNameField.setText(reservation.getGuestName());
            }
            
            // Set room information
            if (roomNumberComboBox != null) {
                roomNumberComboBox.setValue(reservation.getRoomNumber());
            }
            
            // Set dates
            if (checkInDatePicker != null) {
                checkInDatePicker.setValue(java.time.LocalDate.parse(reservation.getCheckIn()));
            }
            
            if (checkOutDatePicker != null) {
                checkOutDatePicker.setValue(java.time.LocalDate.parse(reservation.getCheckOut()));
            }
            
            // Try to load additional guest details from database using reservation ID
            loadGuestDetailsFromReservation(reservation.getReservationId());
            
            // Update form state
            updateGuestStatus("From Reservation");
            validateDates();
            updateSummaryAndPrice();
            
            // Show info message
            showModernAlert(Alert.AlertType.INFORMATION, 
                "Reservation Loaded", 
                "Reservation data loaded successfully. Please verify and complete the booking details.");
                
        } catch (Exception e) {
            e.printStackTrace();
            showModernAlert(Alert.AlertType.WARNING, 
                "Load Warning", 
                "Some reservation data could not be loaded: " + e.getMessage());
        }
    }

    /**
     * Load additional guest details from database using reservation ID
     */
    private void loadGuestDetailsFromReservation(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            return;
        }
        
        // Query to get full guest details from reservation
        String sql = """
            SELECT g.id, g.name, g.phone, g.email, g.address, g.gst_number, 
                   r.room_no, rt.room_type, rt.price
            FROM reservations r
            JOIN guests g ON r.guest_id = g.id
            JOIN rooms rt ON r.room_no = rt.room_no
            WHERE r.id = ?
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, reservationId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Set guest ID
                    selectedGuestId = rs.getLong("id");
                    if (guestIdField != null) {
                        guestIdField.setText(String.valueOf(selectedGuestId));
                    }
                    
                    // Set guest contact details
                    if (mobileNumberField != null) {
                        mobileNumberField.setText(rs.getString("phone"));
                    }
                    
                    if (emailField != null) {
                        emailField.setText(rs.getString("email"));
                    }
                    
                    if (addressArea != null) {
                        addressArea.setText(rs.getString("address"));
                    }
                    
                    if (gstNumberField != null) {
                        gstNumberField.setText(rs.getString("gst_number"));
                    }
                    
                    // Set room type and update pricing
                    String roomType = rs.getString("room_type");
                    if (roomTypeComboBox != null && roomType != null) {
                        roomTypeComboBox.setValue(roomType);
                    }
                    
                    // Set room rate
                    BigDecimal roomRate = rs.getBigDecimal("price");
                    if (roomPriceField != null && roomRate != null) {
                        roomPriceField.setText(toCurrency(roomRate));
                    }
                    
                    selectedRoomRate = roomRate != null ? roomRate : BigDecimal.ZERO;
                    updateGuestStatus("Returning Guest (From Reservation)");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading guest details from reservation: " + e.getMessage());
            // Don't show error alert here, just log it
        }
    }


    private boolean validateRequiredFields() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();
        clearErrorStates();

        if (isBlank(fullNameField)) { addError(fullNameField); errorMessage.append("• Full Name is required\n"); isValid = false; }
        if (isBlank(mobileNumberField)) {
            addError(mobileNumberField); errorMessage.append("• Mobile Number is required\n"); isValid = false;
        } else if (!mobileNumberField.getText().matches("\\d{10}")) {
            addError(mobileNumberField); errorMessage.append("• Mobile Number must be 10 digits\n"); isValid = false;
        }
        if (isBlank(addressArea)) { addError(addressArea); errorMessage.append("• Address is required\n"); isValid = false; }

        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();
        if (checkIn == null || checkOut == null) {
            if (checkIn == null) addError(checkInDatePicker);
            if (checkOut == null) addError(checkOutDatePicker);
            errorMessage.append("• Check-in and Check-out dates are required\n");
            isValid = false;
        } else if (!checkOut.isAfter(checkIn)) {
            addError(checkInDatePicker); addError(checkOutDatePicker);
            errorMessage.append("• Check-out date must be after check-in date\n");
            isValid = false;
        }

        if (roomTypeComboBox.getValue() == null) { addError(roomTypeComboBox); errorMessage.append("• Room Type is required\n"); isValid = false; }
        if (roomNumberComboBox.getValue() == null) { addError(roomNumberComboBox); errorMessage.append("• Room Number is required\n"); isValid = false; }

        if (!isValid) showModernAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage.toString());
        return isValid;
    }

    private void clearErrorStates() {
        removeError(fullNameField);
        removeError(mobileNumberField);
        removeError(addressArea);
        removeError(checkInDatePicker);
        removeError(checkOutDatePicker);
        removeError(roomTypeComboBox);
        removeError(roomNumberComboBox);
    }

    private boolean isBlank(TextInputControl c) { return c == null || c.getText() == null || c.getText().isBlank(); }
    private String generateTempBookingCode() { return "BKG-" + (System.currentTimeMillis() % 1_000_000); }
    private String safe(String s) { return s == null ? "" : s.trim(); }
    private String nullIfBlank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    private BigDecimal parseCurrencyToBD(String text) {
        if (text == null) return BigDecimal.ZERO;
        try {
            String clean = text.replace("₹", "").replace(",", "").trim();
            if (clean.isBlank()) return BigDecimal.ZERO;
            return new BigDecimal(clean);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String toCurrency(BigDecimal bd) {
        if (bd == null) bd = BigDecimal.ZERO;
        return "₹" + bd.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private void showModernAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/Booking.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("modern-alert");
        } catch (Exception ignored) { }
        alert.showAndWait();
    }
    
    
    @FXML
    private void handlePhotoIdVerification() {
        try {
            System.out.println("Starting Photo ID Verification...");
            
            // Check if we have a booking ID
            if (bookingIdField == null || bookingIdField.getText() == null || bookingIdField.getText().isEmpty()) {
                showModernAlert(Alert.AlertType.WARNING, "Booking Required", 
                    "Please generate a booking ID first before starting photo ID verification.");
                return;
            }

            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Photo ID Verification");
            dialog.setHeaderText("Complete Photo ID Verification for All Persons");
            
            // Set the dialog icon (REMOVED problematic icon loading)
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            // Removed the problematic icon loading that was causing NPE

            try {
                // Load the FXML content
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/PhotoIdVerification.fxml"));
                Parent root = loader.load();
                
                PhotoIdVerificationController controller = loader.getController();
                // Use a temporary booking ID for demo - replace with actual booking ID logic
                Long tempBookingId = System.currentTimeMillis();
                controller.setBookingId(tempBookingId);

                // Set the dialog content
                dialog.getDialogPane().setContent(root);
                
            } catch (IOException e) {
                e.printStackTrace();
                // Fallback: Create a simple dialog if FXML fails to load
                System.err.println("Failed to load PhotoIdVerification FXML: " + e.getMessage());
                createFallbackVerificationDialog(dialog);
            }

            // Set button types
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Customize the OK button
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Complete Verification");
            okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

            // Show dialog and wait for response
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Verification completed
                updateVerificationStatus(true);
                showModernAlert(Alert.AlertType.INFORMATION, "Verification Complete", 
                    "Photo ID verification completed successfully for all persons.");
            } else {
                // Verification cancelled
                updateVerificationStatus(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showModernAlert(Alert.AlertType.ERROR, "Error", 
                "Could not open photo ID verification: " + e.getMessage());
        }
    }

    // Fallback method if FXML fails to load
    private void createFallbackVerificationDialog(Dialog<ButtonType> dialog) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);
        
        Label titleLabel = new Label("📷 Photo ID Verification");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Complete photo ID verification for all persons staying in the room");
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        
        VBox formContainer = new VBox(10);
        formContainer.setPadding(new Insets(15));
        formContainer.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f8f9fa;");
        
        Label formTitle = new Label("Basic Information");
        formTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        HBox personsBox = new HBox(10);
        personsBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<Integer> personsCombo = new ComboBox<>();
        for (int i = 1; i <= 10; i++) {
            personsCombo.getItems().add(i);
        }
        personsCombo.setValue(1);
        personsBox.getChildren().addAll(new Label("Number of Persons:"), personsCombo);
        
        Button captureBtn = new Button("📸 Capture Main Guest Photo");
        captureBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        
        formContainer.getChildren().addAll(formTitle, personsBox, captureBtn);
        content.getChildren().addAll(titleLabel, subtitleLabel, formContainer);
        
        dialog.getDialogPane().setContent(content);
    }

    // Update verification status in the main form
    private void updateVerificationStatus(boolean completed) {
        Platform.runLater(() -> {
            if (completed) {
                if (verificationStatusLabel != null) {
                    verificationStatusLabel.setText("✅ Verified");
                    verificationStatusLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                }
                if (photoIdPathLabel != null) {
                    photoIdPathLabel.setText("✅ Photo ID Verified");
                    photoIdPathLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                }
                if (overallVerificationStatusLabel != null) {
                    overallVerificationStatusLabel.setText("🟢 Complete");
                    overallVerificationStatusLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                }
            } else {
                if (verificationStatusLabel != null) {
                    verificationStatusLabel.setText("⏳ Pending");
                    verificationStatusLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                }
            }
        });
    }

    // Update the existing handleUploadPhotoId method - SIMPLIFIED VERSION
    @FXML
    private void handleUploadPhotoId(javafx.event.ActionEvent event) {
        // Simple approach - directly open photo ID verification
        handlePhotoIdVerification();
    }

    // Alternative version with choice dialog (if you want options)
    /*
    @FXML
    private void handleUploadPhotoId(javafx.event.ActionEvent event) {
        // Create a simple choice dialog
        Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
        choiceDialog.setTitle("Document Options");
        choiceDialog.setHeaderText("Choose Document Action");
        choiceDialog.setContentText("What would you like to do?");
        
        ButtonType verifyButton = new ButtonType("Photo ID Verification");
        ButtonType uploadButton = new ButtonType("Upload Document");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        choiceDialog.getButtonTypes().setAll(verifyButton, uploadButton, cancelButton);
        
        Optional<ButtonType> result = choiceDialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == verifyButton) {
                handlePhotoIdVerification();
            } else if (result.get() == uploadButton) {
                // Original upload logic
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Photo ID Document");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Files", "*.*"),
                    new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
                );
                
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                File selectedFile = fileChooser.showOpenDialog(stage);
                if (selectedFile != null) {
                    photoIdPathLabel.setText(selectedFile.getName());
                    photoIdPathLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                }
            }
        }
    }
    */

    // Add this method to initialize verification status
    private void initializeVerificationStatus() {
        if (verificationStatusLabel != null) {
            verificationStatusLabel.setText("⏳ Not Verified");
        }
        if (documentStatusLabel != null) {
            documentStatusLabel.setText("❌ Pending");
        }
        if (photoIdStatusLabel != null) {
            photoIdStatusLabel.setText("❌ Pending");
        }
        if (allPersonsStatusLabel != null) {
            allPersonsStatusLabel.setText("❌ No");
        }
        if (overallVerificationStatusLabel != null) {
            overallVerificationStatusLabel.setText("🔴 Incomplete");
        }
    }

    // Update your initialize() method to call initializeVerificationStatus()

    private Long getOrCreateBookingId() {
        // This is a placeholder - you'll need to implement based on your actual booking creation logic
        // For now, return a temporary ID or use the actual booking ID from your system
        return System.currentTimeMillis(); // Temporary implementation
    }

    private void updatePhotoIdStatus() {
        try {
            Long currentBookingId = getOrCreateBookingId();
            var verification = photoIdVerificationDAO.findByBookingId(currentBookingId);
            
            if (verification.isPresent()) {
                PhotoIdVerification v = verification.get();
                if (photoIdPathLabel != null) {
                    photoIdPathLabel.setText("✅ Photo ID Verified (" + v.getTotalPersons() + " persons)");
                    photoIdPathLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                    
                    // Update tooltip with verification details
                    Tooltip tooltip = new Tooltip();
                    tooltip.setText("Verified on: " + v.getVerificationDate() + 
                                   "\nTotal Persons: " + v.getTotalPersons() + 
                                   "\nStatus: " + v.getStatus());
                    photoIdPathLabel.setTooltip(tooltip);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
