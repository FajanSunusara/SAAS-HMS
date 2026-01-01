package application.controllers;

import application.services.dao.DatabaseManager;
import application.services.dao.PhotoIdVerificationDAO;
import application.services.dao.RoomDAO;
import application.services.dao.BookingDocumentDAO;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import application.models.RoomBooking;
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
import application.utils.BookingFileManager;
import application.models.BookingDocument;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javax.imageio.ImageIO;
import application.utils.SimpleCameraCapture;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import application.models.GroupBookingData;
import application.models.GuestMember;

public class BookingController {

    // ============ FROM FIRST CODE ============
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

    @FXML private ToggleButton gstToggle;
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
    
    @FXML private ToggleButton groupBookingToggle;
    @FXML private TableView<GroupRoom> groupRoomsTable;
    @FXML private TableColumn<GroupRoom, String> groupRoomNoColumn;
    @FXML private TableColumn<GroupRoom, String> groupRoomTypeColumn;
    @FXML private TableColumn<GroupRoom, String> groupGuestIdsColumn;
    @FXML private VBox groupBookingSection;
    
    @FXML private TableView<GuestDocument> documentsTable;
    @FXML private TableColumn<GuestDocument, String> docPersonNameColumn;
    @FXML private TableColumn<GuestDocument, String> docTypeColumn;
    @FXML private TableColumn<GuestDocument, String> docFileNameColumn;
    @FXML private TableColumn<GuestDocument, Void> docActionsColumn;
    @FXML private VBox documentsSection;
    @FXML private FlowPane photosPreviewPane;
    @FXML private TableColumn<GroupRoom, String> groupRateColumn;
    @FXML private TableColumn<GroupRoom, String> groupTotalColumn;
    @FXML private Label groupSummaryLabel;

    private final RoomDAO roomDAO = new RoomDAO();
    private final ObservableList<String> sourceOptions = FXCollections.observableArrayList();
    private final ObservableList<String> allRoomTypes = FXCollections.observableArrayList();
    private final Map<String, BigDecimal> roomRatesByRoomNo = new HashMap<>();
    private final Map<String, String> roomTypeByRoomNo = new HashMap<>();
    
    private final BookingDocumentDAO bookingDocumentDAO = new BookingDocumentDAO();
    private ObservableList<GuestDocument> guestDocuments = FXCollections.observableArrayList();
    private ObservableList<GroupRoom> groupRooms = FXCollections.observableArrayList();
    
    private Long selectedGuestId = null;
    private String selectedRoomType = null;
    private String selectedRoomNo = null;
    private BigDecimal selectedRoomRate = BigDecimal.ZERO;

    private final GstCalculationService gstService = GstCalculationService.getInstance();
    private String documentLink = null;
    private PropertyReader propertyReader;

    // ============ FROM SECOND CODE ============
    @FXML private TabPane bookingTabPane;
    @FXML private Tab singleBookingTab;
    @FXML private Tab groupBookingTab;
    
    @FXML private TextField groupNameField;
    @FXML private TextField companyNameField;
    @FXML private TextField contactPersonField;
    @FXML private TextField contactPhoneField;
    @FXML private TextField contactEmailField;
    @FXML private TextField groupGstField;
    @FXML private TextArea groupAddressArea;
    @FXML private ComboBox<String> groupNationalityCombo;
    @FXML private DatePicker groupCheckInDate;
    @FXML private DatePicker groupCheckOutDate;
    
    @FXML private TableView<RoomBooking> groupRoomsTableMain;
    @FXML private TableColumn<RoomBooking, String> colGroupRoomNo;
    @FXML private TableColumn<RoomBooking, String> colGroupRoomType;
    @FXML private TableColumn<RoomBooking, String> colGroupRoomRate;
    @FXML private TableColumn<RoomBooking, String> colGroupRoomNights;
    @FXML private TableColumn<RoomBooking, String> colGroupRoomTotal;
    @FXML private TableColumn<RoomBooking, Void> colGroupRoomActions;
    
    @FXML private TableView<GuestMember> groupMembersTable;
    @FXML private TableColumn<GuestMember, String> colMemberName;
    @FXML private TableColumn<GuestMember, String> colMemberPhone;
    @FXML private TableColumn<GuestMember, String> colMemberDocument;
    @FXML private TableColumn<GuestMember, String> colMemberRoom;
    @FXML private TableColumn<GuestMember, Void> colMemberActions;
    
    @FXML private ComboBox<String> availableRoomsCombo;
    @FXML private TextField memberNameField;
    @FXML private TextField memberPhoneField;
    @FXML private TextField memberEmailField;
    @FXML private ComboBox<String> memberDocumentTypeCombo;
    @FXML private TextField memberDocumentNumberField;
    @FXML private ComboBox<String> memberAssignedRoomCombo;
    
    @FXML private Label groupDocumentPathLabel;
    @FXML private Label groupPhotoPathLabel;
    @FXML private Button uploadGroupDocumentBtn;
    @FXML private Button uploadGroupPhotoBtn;
    
    @FXML private Label groupTotalRoomsLabel;
    @FXML private Label groupTotalMembersLabel;
    @FXML private Label groupTotalNightsLabel;
    @FXML private Label groupTotalAmountLabel;
    @FXML private Label groupGstAmountLabel;
    @FXML private Label groupFinalAmountLabel;
    
    @FXML private Button addRoomToGroupBtn;
    @FXML private Button addMemberToGroupBtn;
    @FXML private Button proceedToPaymentBtn;
    @FXML private Button clearGroupFormBtn;

    private GroupBookingData currentGroupBooking;
    private ObservableList<RoomBooking> groupRoomsList;
    private ObservableList<GuestMember> groupMembersList;
    private ObservableList<String> availableRooms;
    private String documentUploadPath = "uploads/documents/";
    private String photoUploadPath = "uploads/photos/";

    // ============ INITIALIZATION FROM FIRST CODE ============
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
            initializeVerificationStatus();
            initializeGroupBooking();
            
            setupTableDynamicHeight();
            
            double gstRate = propertyReader.getGstRate();
            System.out.println("gst rate " + gstRate);
            
            if (gstToggle != null) {
                gstToggle.setText("GST Excluded");
                gstToggle.setSelected(false);
                gstToggle.selectedProperty().addListener((obs, old, selected) -> {
                    gstToggle.setText(selected ? "GST Included" : "GST Excluded");
                    updateSummaryAndPrice();
                });
            }
            
            // Initialize group booking components from second code
            initializeGroupBookingTab();
            
        } catch (Exception e) {
            showModernAlert(Alert.AlertType.ERROR, "Initialization Error", e.getMessage());
            e.printStackTrace();
        }
    }

    // ============ METHODS FROM FIRST CODE ============

    private void initNationalityOptions() {
        if (nationalityField != null && propertyReader != null) {
            String[] countries = propertyReader.getAllNationalities();
            nationalityField.setItems(FXCollections.observableArrayList(countries));
            
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
        if (groupBookingToggle != null && groupBookingToggle.isSelected()) {
            updateGroupBookingSummary();
            return;
        }
        
        long nights = calculateNights();
        if (summaryNights != null) summaryNights.setText(String.valueOf(nights));

        BigDecimal rate = selectedRoomRate;
        BigDecimal manual = parseCurrencyToBD(roomPriceField != null ? roomPriceField.getText() : null);
        if (manual != null && manual.compareTo(BigDecimal.ZERO) > 0) rate = manual;

        BigDecimal roomAmount = rate.multiply(BigDecimal.valueOf(nights));
        
        boolean gstIncluded = gstToggle != null && gstToggle.isSelected();
        GstCalculationService.GstResult gstResult = gstService.calculate(roomAmount, gstIncluded);
        
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
        return true;
    }

    private void onRoomNoSelected() {
        selectedRoomRate = roomRatesByRoomNo.getOrDefault(selectedRoomNo, BigDecimal.ZERO);
        if (roomPriceField != null) roomPriceField.setText(toCurrency(selectedRoomRate));
        updateAvailabilityLabel();
        updateSummaryAndPrice();
    }

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
        if (nationalityField != null) nationalityField.setDisable(disabled);
        
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
        if (!validateRequiredFields()) return;

        if (groupBookingToggle.isSelected() && groupRoomsTable.getItems().isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Group Booking Error", 
                "Group booking is enabled but no rooms have been added.");
            return;
        }

        saveDocumentsToDatabase();

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

        if (selectedRoomNo == null || selectedRoomNo.isBlank()) {
            showModernAlert(Alert.AlertType.WARNING, "Room Required", "Please select a room number.");
            return;
        }

        BigDecimal rate = parseCurrencyToBD(roomPriceField != null ? roomPriceField.getText() : null);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            rate = selectedRoomRate != null ? selectedRoomRate : BigDecimal.ZERO;
        }

        BigDecimal advance = parseCurrencyToBD(advancePaymentField != null ? advancePaymentField.getText() : null);
        if (advance == null) advance = BigDecimal.ZERO;

        if (!isRoomAvailable(selectedRoomNo, ci, co)) {
            showModernAlert(Alert.AlertType.WARNING, "Not Available", "Room is no longer available for the selected dates.");
            refreshRoomNumbers(selectedRoomType, ci, co, null);
            return;
        }

        String nationality = "India";
        String documentPath = null;
        if (photoIdPathLabel != null && !photoIdPathLabel.getText().equals("No document uploaded")) {
            Tooltip tooltip = photoIdPathLabel.getTooltip();
            if (tooltip != null) {
                documentPath = tooltip.getText();
            }
        }
       
        BigDecimal charge = rate.multiply(new BigDecimal(nights));
        boolean gstIncluded = gstToggle != null && gstToggle.isSelected();
        System.out.println("This is new  gstIncluded : "+  gstIncluded );
        GstCalculationService.GstResult gstResult = gstService.calculate(charge , gstIncluded);

        BigDecimal baseAmount = gstResult.getBaseAmount();
        BigDecimal gstAmount = gstResult.getTaxAmount();
        BigDecimal total = gstResult.getTotalAmount();
        System.out.println("This is new BaseAmount : "+baseAmount);
        System.out.println("This is new GST : "+gstAmount);
        BigDecimal gstRate = new BigDecimal(gstResult.getGstRate());
        
        String sourceWebsite = sourceWebsiteComboBox != null && sourceWebsiteComboBox.getValue() != null 
            ? sourceWebsiteComboBox.getValue().toString() : "Direct";

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
            	    rate,
            	    gstResult.getTotalAmount(),
            	    advance,
            	    nationality,
            	    documentPath,
            	    gstRate,
            	    baseAmount,
            	    gstAmount,
            	    gstIncluded,
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
                handleReset();
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

    // ============ VERIFICATION METHODS FROM FIRST CODE ============

    @FXML
    private void handlePhotoIdVerification() {
        try {
            System.out.println("Starting Photo ID Verification...");
            
            if (bookingIdField == null || bookingIdField.getText() == null || bookingIdField.getText().isEmpty()) {
                showModernAlert(Alert.AlertType.WARNING, "Booking Required", 
                    "Please generate a booking ID first before starting photo ID verification.");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Photo ID Verification");
            dialog.setHeaderText("Complete Photo ID Verification for All Persons");
            
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/PhotoIdVerification.fxml"));
                Parent root = loader.load();
                
                PhotoIdVerificationController controller = loader.getController();
                Long tempBookingId = System.currentTimeMillis();
                controller.setBookingId(tempBookingId);

                dialog.getDialogPane().setContent(root);
                
            } catch (IOException e) {
                e.printStackTrace();
                createFallbackVerificationDialog(dialog);
            }

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Complete Verification");
            okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                updateVerificationStatus(true);
                showModernAlert(Alert.AlertType.INFORMATION, "Verification Complete", 
                    "Photo ID verification completed successfully for all persons.");
            } else {
                updateVerificationStatus(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showModernAlert(Alert.AlertType.ERROR, "Error", 
                "Could not open photo ID verification: " + e.getMessage());
        }
    }

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

    @FXML
    private void handleUploadPhotoId(javafx.event.ActionEvent event) {
        handlePhotoIdVerification();
    }

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

    private Long getOrCreateBookingId() {
        return System.currentTimeMillis();
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

    // ============ GROUP BOOKING METHODS FROM FIRST CODE ============

    private void initializeGroupBooking() {
        if (groupBookingToggle != null) {
            groupBookingToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                toggleGroupBooking(newVal);
                if (newVal) {
                    if (selectedRoomNo != null && roomTypeByRoomNo.containsKey(selectedRoomNo)) {
                        String roomType = roomTypeByRoomNo.get(selectedRoomNo);
                        String guestName = fullNameField.getText();
                        String rate = roomPriceField.getText();
                        long nights = calculateNights();
                        BigDecimal total = calculateRoomTotal(selectedRoomRate, nights);
                        
                        GroupRoom mainRoom = new GroupRoom(selectedRoomNo, roomType, guestName, rate, toCurrency(total));
                        groupRoomsTable.getItems().add(mainRoom);
                        updateGroupBookingSummary();
                    }
                } else {
                    groupRoomsTable.getItems().clear();
                    updateGroupBookingSummary();
                }
            });
        }
        
        initializeGroupRoomsTable();
        initializeDocumentsTable();
    }
    
    private void toggleGroupBooking(boolean enabled) {
        if (groupBookingSection != null) {
            groupBookingSection.setVisible(enabled);
            groupBookingSection.setManaged(enabled);
        }
        
        if (documentsSection != null) {
            documentsSection.setVisible(enabled);
            documentsSection.setManaged(enabled);
        }
        
        if (!enabled && groupRoomsTable != null) {
            groupRoomsTable.getItems().clear();
        }
    }

    private void initializeGroupRoomsTable() {
        if (groupRoomNoColumn != null) {
            groupRoomNoColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        }
        if (groupRoomTypeColumn != null) {
            groupRoomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        }
        if (groupGuestIdsColumn != null) {
            groupGuestIdsColumn.setCellValueFactory(new PropertyValueFactory<>("guestIds"));
        }
        if (groupRateColumn != null) {
            groupRateColumn.setCellValueFactory(new PropertyValueFactory<>("ratePerNight"));
        }
        if (groupTotalColumn != null) {
            groupTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        }
        
        groupRoomsTable.setItems(groupRooms);
        
        groupRoomsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private long calculateNights() {
        LocalDate ci = checkInDatePicker.getValue();
        LocalDate co = checkOutDatePicker.getValue();
        if (ci != null && co != null && co.isAfter(ci)) {
            return ChronoUnit.DAYS.between(ci, co);
        }
        return 0;
    }

    private BigDecimal calculateRoomTotal(BigDecimal rate, long nights) {
        if (rate == null) rate = BigDecimal.ZERO;
        return rate.multiply(BigDecimal.valueOf(nights));
    }

    private void initializeDocumentsTable() {
        if (docPersonNameColumn != null) {
            docPersonNameColumn.setCellValueFactory(new PropertyValueFactory<>("personName"));
        }
        if (docTypeColumn != null) {
            docTypeColumn.setCellValueFactory(new PropertyValueFactory<>("docType"));
        }
        if (docFileNameColumn != null) {
            docFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        }
        if (docActionsColumn != null) {
            docActionsColumn.setCellFactory(param -> new TableCell<GuestDocument, Void>() {
                private final Button viewBtn = new Button("👁️");
                private final Button deleteBtn = new Button("🗑️");
                
                {
                    viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");
                    deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");
                    
                    viewBtn.setOnAction(e -> {
                        GuestDocument doc = getTableView().getItems().get(getIndex());
                        viewDocument(doc);
                    });
                    
                    deleteBtn.setOnAction(e -> {
                        GuestDocument doc = getTableView().getItems().get(getIndex());
                        deleteDocument(doc);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox buttons = new HBox(5, viewBtn, deleteBtn);
                        buttons.setAlignment(Pos.CENTER);
                        setGraphic(buttons);
                    }
                }
            });
        }
        
        documentsTable.setItems(guestDocuments);
        documentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleAddGroupRoom() {
        try {
            if (selectedRoomNo == null || selectedRoomNo.isEmpty()) {
                showModernAlert(Alert.AlertType.WARNING, "No Room Selected", 
                    "Please select a room from the Stay Details section first.");
                return;
            }

            LocalDate ci = checkInDatePicker.getValue();
            LocalDate co = checkOutDatePicker.getValue();
            
            if (ci == null || co == null || !co.isAfter(ci)) {
                showModernAlert(Alert.AlertType.WARNING, "Invalid Dates", 
                    "Please select valid check-in and check-out dates.");
                return;
            }

            String currentRoom = selectedRoomNo;
            boolean roomExists = groupRoomsTable.getItems().stream()
                .anyMatch(room -> room.getRoomNo().equals(currentRoom));
            
            if (roomExists) {
                showModernAlert(Alert.AlertType.WARNING, "Room Already Added", 
                    "This room is already in the group booking.");
                return;
            }

            if (!isRoomAvailable(currentRoom, ci, co)) {
                showModernAlert(Alert.AlertType.WARNING, "Room Not Available", 
                    "Selected room is not available for the chosen dates.");
                return;
            }

            String roomType = roomTypeByRoomNo.get(currentRoom);
            String guestName = fullNameField.getText();
            if (guestName == null || guestName.isEmpty()) {
                guestName = "Main Guest";
            }
            
            BigDecimal rate = selectedRoomRate;
            BigDecimal manualRate = parseCurrencyToBD(roomPriceField.getText());
            if (manualRate != null && manualRate.compareTo(BigDecimal.ZERO) > 0) {
                rate = manualRate;
            }
            
            long nights = calculateNights();
            BigDecimal total = calculateRoomTotal(rate, nights);

            GroupRoom groupRoom = new GroupRoom(
                currentRoom, 
                roomType, 
                guestName, 
                toCurrency(rate), 
                toCurrency(total)
            );
            
            groupRoomsTable.getItems().add(groupRoom);
            Platform.runLater(() -> {
                groupRoomsTable.refresh();
                groupRoomsTable.requestLayout();
                
                if (groupBookingSection != null) {
                    groupBookingSection.requestLayout();
                }
            });
            updateGroupBookingSummary();
            
            showModernAlert(Alert.AlertType.INFORMATION, "Room Added", 
                "Room " + currentRoom + " added to group booking successfully.");

        } catch (Exception e) {
            showModernAlert(Alert.AlertType.ERROR, "Error", "Failed to add room: " + e.getMessage());
        }
    }

    private void updateGroupBookingSummary() {
        if (!groupBookingToggle.isSelected()) {
            return;
        }

        BigDecimal totalGroupAmount = BigDecimal.ZERO;
        BigDecimal totalAdvance = parseCurrencyToBD(advancePaymentField.getText());
        if (totalAdvance == null) totalAdvance = BigDecimal.ZERO;

        for (GroupRoom room : groupRoomsTable.getItems()) {
            BigDecimal roomTotal = parseCurrencyToBD(room.getTotalAmount());
            if (roomTotal != null) {
                totalGroupAmount = totalGroupAmount.add(roomTotal);
            }
        }

        boolean gstIncluded = gstToggle != null && gstToggle.isSelected();
        GstCalculationService.GstResult gstResult = gstService.calculate(totalGroupAmount, gstIncluded);

        if (summaryRoomRate != null) {
            if (groupRoomsTable.getItems().size() > 1) {
                summaryRoomRate.setText("Multiple Rooms");
            } else if (!groupRoomsTable.getItems().isEmpty()) {
                summaryRoomRate.setText(groupRoomsTable.getItems().get(0).getRatePerNight());
            }
        }
        
        if (summaryNights != null) summaryNights.setText(String.valueOf(calculateNights()));
        if (baseAmountLabel != null) baseAmountLabel.setText(toCurrency(gstResult.getBaseAmount()));
        if (gstAmountLabel != null) gstAmountLabel.setText(toCurrency(gstResult.getTaxAmount()));
        if (summaryTotal != null) summaryTotal.setText(toCurrency(gstResult.getTotalAmount()));
        if (summaryAdvance != null) summaryAdvance.setText(toCurrency(totalAdvance));

        BigDecimal pending = gstResult.getTotalAmount().subtract(totalAdvance);
        if (pending.compareTo(BigDecimal.ZERO) < 0) pending = BigDecimal.ZERO;
        if (summaryPending != null) summaryPending.setText(toCurrency(pending));

        if (groupSummaryLabel != null) {
            groupSummaryLabel.setText(String.format("Group Total: %s (%d rooms)", 
                toCurrency(gstResult.getTotalAmount()), groupRoomsTable.getItems().size()));
        }
    }

    @FXML
    private void handleAddDocument() {
        try {
            Dialog<GuestDocument> dialog = new Dialog<>();
            dialog.setTitle("Add Document");
            dialog.setHeaderText("Add guest document or photo");

            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField personNameField = new TextField();
            ComboBox<String> docTypeCombo = new ComboBox<>();
            docTypeCombo.getItems().addAll("PHOTO", "AADHAAR", "PASSPORT", "DRIVER_LICENSE", "VOTER_ID", "OTHER");
            
            Button uploadBtn = new Button("Upload File");
            Button captureBtn = new Button("Capture Photo");
            
            Label fileLabel = new Label("No file selected");
            fileLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

            if (fullNameField.getText() != null && !fullNameField.getText().isEmpty()) {
                personNameField.setText(fullNameField.getText());
            }

            grid.add(new Label("Person Name:"), 0, 0);
            grid.add(personNameField, 1, 0);
            grid.add(new Label("Document Type:"), 0, 1);
            grid.add(docTypeCombo, 1, 1);
            grid.add(new Label("File:"), 0, 2);
            grid.add(uploadBtn, 1, 2);
            grid.add(captureBtn, 2, 2);
            grid.add(fileLabel, 1, 3, 2, 1);

            final String[] selectedFilePath = {null};

            uploadBtn.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Document");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    selectedFilePath[0] = file.getAbsolutePath();
                    fileLabel.setText(file.getName());
                    fileLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                }
            });

            captureBtn.setOnAction(e -> {
                try {
                    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                    Image capturedImage = SimpleCameraCapture.captureFromCamera(stage);
                    
                    if (capturedImage != null) {
                        String bookingId = bookingIdField.getText();
                        String personName = personNameField.getText();
                        if (personName == null || personName.isEmpty()) {
                            personName = "Guest";
                        }
                        
                        String savedPath = BookingFileManager.saveBookingImage(capturedImage, bookingId, "PHOTO", personName);
                        if (savedPath != null) {
                            selectedFilePath[0] = savedPath;
                            fileLabel.setText("Captured Photo - Saved");
                            fileLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                        } else {
                            showModernAlert(Alert.AlertType.ERROR, "Error", "Failed to save captured photo.");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showModernAlert(Alert.AlertType.ERROR, "Camera Error", 
                        "Failed to capture photo: " + ex.getMessage());
                }
            });

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType && selectedFilePath[0] != null) {
                    String personName = personNameField.getText();
                    String docType = docTypeCombo.getValue();
                    String fileName = new File(selectedFilePath[0]).getName();
                    
                    if (personName != null && !personName.isEmpty() && docType != null) {
                        if (!selectedFilePath[0].contains("temp") && bookingIdField.getText() != null) {
                            String bookingId = bookingIdField.getText();
                            File sourceFile = new File(selectedFilePath[0]);
                            String savedPath = BookingFileManager.saveBookingDocument(sourceFile, bookingId, docType, personName);
                            if (savedPath != null) {
                                selectedFilePath[0] = savedPath;
                                fileName = new File(savedPath).getName();
                            }
                        }
                        
                        return new GuestDocument(personName, docType, fileName, selectedFilePath[0]);
                    }
                }
                return null;
            });

            Optional<GuestDocument> result = dialog.showAndWait();
            result.ifPresent(doc -> {
                guestDocuments.add(doc);
                refreshPhotosPreview();
                
                updateDocumentVerificationStatus();
                
                showModernAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Document added successfully!\n\n" +
                    "Person: " + doc.getPersonName() + "\n" +
                    "Type: " + doc.getDocType() + "\n" +
                    "File: " + doc.getFileName());
            });

        } catch (Exception e) {
            showModernAlert(Alert.AlertType.ERROR, "Error", "Failed to add document: " + e.getMessage());
        }
    }

    private void updateDocumentVerificationStatus() {
        if (!guestDocuments.isEmpty()) {
            if (documentStatusLabel != null) {
                documentStatusLabel.setText("✅ Completed");
                documentStatusLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
            }
            
            if (overallVerificationStatusLabel != null) {
                overallVerificationStatusLabel.setText("🟢 Complete");
                overallVerificationStatusLabel.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
            }
        }
    }

    private void viewDocument(GuestDocument doc) {
        try {
            File file = new File(doc.getFilePath());
            if (file.exists()) {
                if (doc.getFileName().toLowerCase().endsWith(".png") || 
                    doc.getFileName().toLowerCase().endsWith(".jpg") || 
                    doc.getFileName().toLowerCase().endsWith(".jpeg")) {
                    
                    Stage stage = new Stage();
                    ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(600);
                    
                    ScrollPane scrollPane = new ScrollPane(imageView);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setFitToHeight(true);
                    
                    VBox root = new VBox(10);
                    root.setPadding(new Insets(15));
                    root.getChildren().addAll(
                        new Label("Document: " + doc.getFileName()),
                        new Label("Person: " + doc.getPersonName()),
                        new Label("Type: " + doc.getDocType()),
                        scrollPane
                    );
                    
                    stage.setScene(new Scene(root, 800, 600));
                    stage.setTitle("Document Viewer - " + doc.getFileName());
                    stage.show();
                } else {
                    Desktop.getDesktop().open(file);
                }
            } else {
                showModernAlert(Alert.AlertType.WARNING, "File Not Found", 
                    "The document file was not found at:\n" + doc.getFilePath());
            }
        } catch (Exception e) {
            showModernAlert(Alert.AlertType.ERROR, "Error", "Failed to open document: " + e.getMessage());
        }
    }

    private void deleteDocument(GuestDocument doc) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Document");
        confirm.setContentText("Are you sure you want to delete this document?\n\n" +
                              "Person: " + doc.getPersonName() + "\n" +
                              "Type: " + doc.getDocType() + "\n" +
                              "File: " + doc.getFileName());
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            guestDocuments.remove(doc);
            refreshPhotosPreview();
            
            boolean fileDeleted = BookingFileManager.deleteDocument(doc.getFilePath());
            
            if (fileDeleted) {
                showModernAlert(Alert.AlertType.INFORMATION, "Success", "Document deleted successfully.");
            } else {
                showModernAlert(Alert.AlertType.WARNING, "Warning", 
                    "Document removed from list but file may not have been deleted.");
            }
        }
    }

    private void refreshPhotosPreview() {
        if (photosPreviewPane == null) return;
        
        photosPreviewPane.getChildren().clear();
        
        for (GuestDocument doc : guestDocuments) {
            if (doc.getDocType().equals("PHOTO")) {
                try {
                    File file = new File(doc.getFilePath());
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString(), 80, 80, true, true);
                        ImageView imageView = new ImageView(image);
                        imageView.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5;");
                        
                        VBox photoBox = new VBox(5, imageView, new Label(doc.getPersonName()));
                        photoBox.setAlignment(Pos.CENTER);
                        photoBox.setPadding(new Insets(5));
                        photoBox.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f9fafb;");
                        
                        photoBox.setOnMouseClicked(e -> viewDocument(doc));
                        photoBox.setStyle(photoBox.getStyle() + " -fx-cursor: hand;");
                        
                        photosPreviewPane.getChildren().add(photoBox);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load photo: " + e.getMessage());
                    VBox placeholderBox = new VBox(5, new Label("❌"), new Label(doc.getPersonName()));
                    placeholderBox.setAlignment(Pos.CENTER);
                    placeholderBox.setPadding(new Insets(5));
                    placeholderBox.setStyle("-fx-border-color: #fca5a5; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #fef2f2;");
                    photosPreviewPane.getChildren().add(placeholderBox);
                }
            }
        }
        
        if (photosPreviewPane.getChildren().isEmpty()) {
            Label noPhotosLabel = new Label("No photos added yet");
            noPhotosLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic;");
            photosPreviewPane.getChildren().add(noPhotosLabel);
        }
    }

    private void saveDocumentsToDatabase() {
        System.out.println("Preparing to save " + guestDocuments.size() + " documents for booking.");
        
        for (GuestDocument guestDoc : guestDocuments) {
            System.out.println("Document: " + guestDoc.getPersonName() + " - " + guestDoc.getDocType() + " - " + guestDoc.getFileName());
        }
    }

    @FXML
    public void handleReset() {
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
        }
        updateGuestStatus("New Guest");

        guestDocuments.clear();
        groupRooms.clear();
        refreshPhotosPreview();
        
        if (groupBookingToggle != null) {
            groupBookingToggle.setSelected(false);
            toggleGroupBooking(false);
        }

        initializeVerificationStatus();

        if (roomTypeComboBox != null) roomTypeComboBox.getSelectionModel().clearSelection();
        if (roomNumberComboBox != null) roomNumberComboBox.getSelectionModel().clearSelection();
        if (sourceWebsiteComboBox != null) sourceWebsiteComboBox.getSelectionModel().clearSelection();
        if (autoFillComboBox != null) autoFillComboBox.getSelectionModel().clearSelection();
        if (checkInDatePicker != null) checkInDatePicker.setValue(null);
        if (checkOutDatePicker != null) checkOutDatePicker.setValue(null);
        if (totalDaysField != null) totalDaysField.clear();
        if (roomPriceField != null) roomPriceField.setText("₹0.00");
        if (advancePaymentField != null) advancePaymentField.clear();

        if (wifiCheckbox != null) wifiCheckbox.setSelected(false);
        if (breakfastCheckbox != null) breakfastCheckbox.setSelected(false);
        if (extraBedCheckbox != null) extraBedCheckbox.setSelected(false);
        if (parkingCheckbox != null) parkingCheckbox.setSelected(false);

        if (summaryRoomRate != null) summaryRoomRate.setText("₹0.00");
        if (summaryNights != null) summaryNights.setText("0");
        if (summaryTotal != null) summaryTotal.setText("₹0.00");
        if (summaryAdvance != null) summaryAdvance.setText("₹0.00");
        if (summaryPending != null) summaryPending.setText("₹0.00");
        if (availabilityLabel != null) availabilityLabel.setText("⏳ Select room and dates");
        
        if (gstToggle != null) {
            gstToggle.setSelected(false);
            gstToggle.setText("GST Excluded");
        }

        if (baseAmountLabel != null) baseAmountLabel.setText("₹0.00");
        if (gstAmountLabel != null) gstAmountLabel.setText("₹0.00");
        if (gstBreakdownLabel != null) gstBreakdownLabel.setText("GST @ 18.0%");

        if (bookingIdField != null) bookingIdField.setText(generateTempBookingCode());

        refreshRoomNumbers(null, null, null, null);

        showModernAlert(Alert.AlertType.INFORMATION, "Form Reset Complete",
                "✅ All form fields have been cleared successfully.\n\nYou can now start a new booking.");
    }

    @FXML
    private void handleRemoveGroupRoom() {
        GroupRoom selectedRoom = groupRoomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Remove");
            confirm.setHeaderText("Remove Room from Group");
            confirm.setContentText("Are you sure you want to remove room " + selectedRoom.getRoomNo() + " from group booking?");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                groupRoomsTable.getItems().remove(selectedRoom);
                updateGroupBookingSummary();
            }
        } else {
            showModernAlert(Alert.AlertType.WARNING, "No Selection", "Please select a room to remove.");
        }
    }
    
    private void setupTableDynamicHeight() {
        if (groupRoomsTable != null) {
            groupRoomsTable.setFixedCellSize(35);
            groupRoomsTable.prefHeightProperty().bind(
                groupRoomsTable.fixedCellSizeProperty()
                    .multiply(Bindings.size(groupRoomsTable.getItems()).add(1.5))
            );
            groupRoomsTable.minHeightProperty().bind(groupRoomsTable.prefHeightProperty());
            groupRoomsTable.maxHeightProperty().bind(groupRoomsTable.prefHeightProperty());
            
            groupRoomsTable.getItems().addListener((ListChangeListener<? super GroupRoom>) change -> {
                Platform.runLater(() -> {
                    groupRoomsTable.requestLayout();
                    groupRoomsTable.refresh();
                });
            });
        }
        
        if (documentsTable != null) {
            documentsTable.setFixedCellSize(35);
            documentsTable.prefHeightProperty().bind(
                documentsTable.fixedCellSizeProperty()
                    .multiply(Bindings.size(documentsTable.getItems()).add(1.5))
            );
            documentsTable.minHeightProperty().bind(documentsTable.prefHeightProperty());
            documentsTable.maxHeightProperty().bind(documentsTable.prefHeightProperty());
            
            documentsTable.getItems().addListener((ListChangeListener<GuestDocument>) change -> {
                Platform.runLater(() -> {
                    documentsTable.requestLayout();
                    documentsTable.refresh();
                });
            });
        }
    }

    // ============ METHODS FROM SECOND CODE ============
    private void initializeGroupBookingTab() {
        System.out.println("Initializing group booking tab...");
        
        // Initialize collections
        currentGroupBooking = new GroupBookingData();
        groupRoomsList = FXCollections.observableArrayList();
        groupMembersList = FXCollections.observableArrayList();
        availableRooms = FXCollections.observableArrayList();
        
        setupGroupBookingTab();
        loadAvailableRooms();
        setupNationalityComboBox();
        setupDocumentTypeComboBox();
        
        createUploadDirectories();
        
        System.out.println("Group booking tab initialized successfully");
    }

    private void setupGroupBookingTab() {
        System.out.println("Setting up group booking tab...");
        
        // Check if the main table exists
        if (groupRoomsTableMain == null) {
            System.err.println("Group booking table not found in FXML");
            return;
        }
        
        // Clear any existing columns (in case of re-initialization)
        groupRoomsTableMain.getColumns().clear();
        
        // Create columns programmatically - this ensures they always exist
        TableColumn<RoomBooking, String> roomNoCol = new TableColumn<>("Room No");
        roomNoCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomNoCol.setPrefWidth(80);
        
        TableColumn<RoomBooking, String> roomTypeCol = new TableColumn<>("Room Type");
        roomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        roomTypeCol.setPrefWidth(120);
        
        TableColumn<RoomBooking, String> rateCol = new TableColumn<>("Rate/Night");
        rateCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRatePerNightFormatted()));
        rateCol.setPrefWidth(100);
        
        TableColumn<RoomBooking, String> nightsCol = new TableColumn<>("Nights");
        nightsCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getNights())));
        nightsCol.setPrefWidth(60);
        
        TableColumn<RoomBooking, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTotalAmountFormatted()));
        totalCol.setPrefWidth(100);
        
        TableColumn<RoomBooking, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<RoomBooking, Void>() {
            private final Button deleteButton = new Button("❌");
            
            {
                deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-size: 10px;");
                deleteButton.setOnAction(event -> {
                    RoomBooking room = getTableView().getItems().get(getIndex());
                    if (room != null) {
                        removeRoomFromGroup(room);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, deleteButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
        actionsCol.setPrefWidth(80);
        
        // Add all columns to the table
        groupRoomsTableMain.getColumns().addAll(roomNoCol, roomTypeCol, rateCol, nightsCol, totalCol, actionsCol);
        
        // Set the items
        if (groupRoomsList == null) {
            groupRoomsList = FXCollections.observableArrayList();
        }
        groupRoomsTableMain.setItems(groupRoomsList);
        
        System.out.println("Group booking tab setup completed with " + groupRoomsTableMain.getColumns().size() + " columns");
    }
    private void loadAvailableRooms() {
        System.out.println("Loading available rooms...");
        try {
            availableRooms.clear();
            memberAssignedRoomCombo.getItems().clear();
            
            String sql = "SELECT room_no, room_type, price FROM rooms WHERE status = 'Available'";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    String roomNo = rs.getString("room_no");
                    String roomType = rs.getString("room_type");
                    BigDecimal price = rs.getBigDecimal("price");
                    
                    String displayText = String.format("%s - %s (₹%.2f/night)", 
                        roomNo, roomType, price);
                    
                    availableRooms.add(displayText);
                    memberAssignedRoomCombo.getItems().add(roomNo);
                }
                
                availableRoomsCombo.setItems(availableRooms);
                System.out.println("Loaded " + availableRooms.size() + " available rooms");
            }
        } catch (SQLException e) {
            System.err.println("Error loading available rooms: " + e.getMessage());
            showModernAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Could not load available rooms: " + e.getMessage());
        }
    }

    private void setupNationalityComboBox() {
        if (groupNationalityCombo != null) {
            groupNationalityCombo.getItems().addAll("India", "USA", "UK", "Canada", "Australia", 
                                               "Germany", "France", "Japan", "China", "Other");
            groupNationalityCombo.setValue("India");
        }
    }

    private void setupDocumentTypeComboBox() {
        if (memberDocumentTypeCombo != null) {
            memberDocumentTypeCombo.getItems().addAll("Passport", "Driver License", "Aadhar Card", 
                                                 "Voter ID", "PAN Card", "Other");
            memberDocumentTypeCombo.setValue("Passport");
        }
    }

    private void createUploadDirectories() {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(documentUploadPath));
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(photoUploadPath));
            System.out.println("Upload directories created successfully");
        } catch (IOException e) {
            System.err.println("Error creating upload directories: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddRoomToGroupMain() {
        System.out.println("Adding room to group...");
        
        String selectedRoom = availableRoomsCombo.getValue();
        if (selectedRoom == null || selectedRoom.isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Selection Required", 
                     "Please select a room from the available rooms list.");
            return;
        }
        
        try {
            // Parse room information from the selected string
            String[] roomParts = selectedRoom.split(" - ");
            if (roomParts.length < 2) {
                showModernAlert(Alert.AlertType.ERROR, "Invalid Format", 
                         "Room information format is invalid: " + selectedRoom);
                return;
            }
            
            String roomNumber = roomParts[0];
            String roomType = roomParts[1].split(" \\(")[0];
            String priceStr = selectedRoom.split("₹")[1].split("/")[0];
            BigDecimal ratePerNight = new BigDecimal(priceStr.trim());
            
            LocalDate checkIn = groupCheckInDate.getValue();
            LocalDate checkOut = groupCheckOutDate.getValue();
            
            if (checkIn == null || checkOut == null) {
                showModernAlert(Alert.AlertType.WARNING, "Dates Required", 
                         "Please select check-in and check-out dates for the group.");
                return;
            }
            
            if (!checkOut.isAfter(checkIn)) {
                showModernAlert(Alert.AlertType.WARNING, "Invalid Dates", 
                         "Check-out date must be after check-in date.");
                return;
            }
            
            // Check if room is already added
            if (isRoomAlreadyAdded(roomNumber)) {
                showModernAlert(Alert.AlertType.WARNING, "Room Already Added", 
                         "Room " + roomNumber + " is already in the group booking.");
                return;
            }
            
            // Check room availability
            if (!isRoomAvailable(roomNumber, checkIn, checkOut)) {
                showModernAlert(Alert.AlertType.WARNING, "Room Not Available", 
                         "Room " + roomNumber + " is not available for the selected dates.");
                return;
            }
            
            // Create and add the room booking
            String primaryGuest = contactPersonField.getText();
            if (primaryGuest == null || primaryGuest.trim().isEmpty()) {
                primaryGuest = "Group Guest";
            }
            
            RoomBooking roomBooking = new RoomBooking(roomNumber, roomType, ratePerNight, 
                                                    checkIn, checkOut, primaryGuest);
            
            // Add to both the current group booking and the observable list
            currentGroupBooking.addRoom(roomBooking);
            groupRoomsList.add(roomBooking);
            
            // Remove from available rooms
            availableRooms.remove(selectedRoom);
            availableRoomsCombo.setItems(FXCollections.observableArrayList(availableRooms));
            availableRoomsCombo.getSelectionModel().clearSelection();
            
            // Update the member room assignment combo box
            updateMemberRoomAssignmentCombo();
            
            // Update summary
            updateGroupBookingSummaryMain();
            
            System.out.println("Room " + roomNumber + " added to group booking. Total rooms: " + groupRoomsList.size());
            
            showModernAlert(Alert.AlertType.INFORMATION, "Room Added", 
                     "Room " + roomNumber + " successfully added to group booking!\n" +
                     "Total rooms: " + groupRoomsList.size());
            
        } catch (Exception e) {
            System.err.println("Error adding room to group: " + e.getMessage());
            e.printStackTrace();
            showModernAlert(Alert.AlertType.ERROR, "Error", 
                     "Could not add room to group: " + e.getMessage());
        }
    }
    
    private boolean isRoomAlreadyAdded(String roomNumber) {
        if (groupRoomsList == null) return false;
        return groupRoomsList.stream()
                .anyMatch(room -> roomNumber.equals(room.getRoomNumber()));
    }

    private void updateMemberRoomAssignmentCombo() {
        if (memberAssignedRoomCombo != null) {
            // Clear and repopulate with current room numbers
            memberAssignedRoomCombo.getItems().clear();
            for (RoomBooking room : groupRoomsList) {
                memberAssignedRoomCombo.getItems().add(room.getRoomNumber());
            }
            
            if (!groupRoomsList.isEmpty()) {
                memberAssignedRoomCombo.setValue(groupRoomsList.get(0).getRoomNumber());
            }
        }
    }

    private void updateGroupBookingSummaryMain() {
        if (currentGroupBooking == null) return;
        
        int totalRooms = groupRoomsList.size();
        int totalMembers = groupMembersList != null ? groupMembersList.size() : 0;
        long totalNights = currentGroupBooking.getTotalNights();
        
        BigDecimal baseAmount = currentGroupBooking.getBaseAmount();
        BigDecimal gstAmount = currentGroupBooking.getGstAmount();
        BigDecimal finalAmount = currentGroupBooking.getTotalAmount();
        
        // Update UI labels
        if (groupTotalRoomsLabel != null) {
            groupTotalRoomsLabel.setText(String.valueOf(totalRooms));
        }
        if (groupTotalMembersLabel != null) {
            groupTotalMembersLabel.setText(String.valueOf(totalMembers));
        }
        if (groupTotalNightsLabel != null) {
            groupTotalNightsLabel.setText(String.valueOf(totalNights));
        }
        if (groupTotalAmountLabel != null) {
            groupTotalAmountLabel.setText(String.format("₹%.2f", baseAmount));
        }
        if (groupGstAmountLabel != null) {
            groupGstAmountLabel.setText(String.format("₹%.2f", gstAmount));
        }
        if (groupFinalAmountLabel != null) {
            groupFinalAmountLabel.setText(String.format("₹%.2f", finalAmount));
        }
        
        System.out.println("Group summary updated: " + totalRooms + " rooms, ₹" + finalAmount);
    }
//    private boolean isRoomAlreadyAdded(String roomNumber) {
//        return groupRoomsList.stream()
//                .anyMatch(room -> roomNumber.equals(room.getRoomNumber()));
//    }

    @FXML
    private void handleAddMemberToGroupMain() {
        System.out.println("Adding member to group...");
        
        if (!validateMemberForm()) {
            return;
        }
        
        try {
            GuestMember member = new GuestMember(
                memberNameField.getText().trim(),
                memberPhoneField.getText().trim(),
                memberEmailField.getText().trim(),
                memberDocumentTypeCombo.getValue(),
                memberDocumentNumberField.getText().trim(),
                memberAssignedRoomCombo.getValue()
            );
            
            currentGroupBooking.addMember(member);
            groupMembersList.add(member);
            
            clearMemberForm();
            
            updateGroupBookingSummaryMain();
            
            System.out.println("Member " + member.getName() + " added to group booking");
            
        } catch (Exception e) {
            System.err.println("Error adding member to group: " + e.getMessage());
            showModernAlert(Alert.AlertType.ERROR, "Error", 
                     "Could not add member to group: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadGroupDocumentMain() {
        System.out.println("Uploading group document...");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Group Document");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        Stage stage = (Stage) uploadGroupDocumentBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                String fileName = "group_doc_" + System.currentTimeMillis() + "_" + file.getName();
                java.nio.file.Path targetPath = java.nio.file.Paths.get(documentUploadPath, fileName);
                java.nio.file.Files.copy(file.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                currentGroupBooking.setDocumentPath(targetPath.toString());
                groupDocumentPathLabel.setText(fileName);
                
                System.out.println("Document uploaded: " + targetPath.toString());
                showModernAlert(Alert.AlertType.INFORMATION, "Success", "Document uploaded successfully.");
                
            } catch (IOException e) {
                System.err.println("Error uploading document: " + e.getMessage());
                showModernAlert(Alert.AlertType.ERROR, "Upload Error", 
                         "Could not upload document: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUploadGroupPhotoMain() {
        System.out.println("Uploading group photo...");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Group Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        Stage stage = (Stage) uploadGroupPhotoBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                String fileName = "group_photo_" + System.currentTimeMillis() + "_" + file.getName();
                java.nio.file.Path targetPath = java.nio.file.Paths.get(photoUploadPath, fileName);
                java.nio.file.Files.copy(file.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                currentGroupBooking.setPhotoPath(targetPath.toString());
                groupPhotoPathLabel.setText(fileName);
                
                System.out.println("Photo uploaded: " + targetPath.toString());
                showModernAlert(Alert.AlertType.INFORMATION, "Success", "Photo uploaded successfully.");
                
            } catch (IOException e) {
                System.err.println("Error uploading photo: " + e.getMessage());
                showModernAlert(Alert.AlertType.ERROR, "Upload Error", 
                         "Could not upload photo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleProceedToPaymentMain() {
        System.out.println("Proceeding to payment for group booking...");
        
        if (!validateGroupBooking()) {
            return;
        }
        
        try {
            updateGroupBookingFromForm();
            
            openPaymentWindowForGroupBooking();
            
        } catch (Exception e) {
            System.err.println("Error proceeding to payment: " + e.getMessage());
            showModernAlert(Alert.AlertType.ERROR, "Error", 
                     "Could not proceed to payment: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearGroupFormMain() {
        System.out.println("Clearing group booking form...");
        
        groupNameField.clear();
        companyNameField.clear();
        contactPersonField.clear();
        contactPhoneField.clear();
        contactEmailField.clear();
        groupGstField.clear();
        groupAddressArea.clear();
        groupNationalityCombo.setValue("India");
        groupCheckInDate.setValue(null);
        groupCheckOutDate.setValue(null);
        
        groupRoomsList.clear();
        groupMembersList.clear();
        
        groupDocumentPathLabel.setText("No document selected");
        groupPhotoPathLabel.setText("No photo selected");
        
        currentGroupBooking = new GroupBookingData();
        
        loadAvailableRooms();
        
        clearGroupBookingSummary();
        
        System.out.println("Group booking form cleared successfully");
    }

    private boolean isRoomAlreadyAddedMain(String roomNumber) {
        return groupRoomsList.stream()
                .anyMatch(room -> roomNumber.equals(room.getRoomNumber()));
    }

    private boolean validateMemberForm() {
        if (memberNameField.getText() == null || memberNameField.getText().trim().isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", "Member name is required.");
            return false;
        }
        
        if (memberPhoneField.getText() == null || memberPhoneField.getText().trim().isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", "Member phone is required.");
            return false;
        }
        
        if (memberDocumentNumberField.getText() == null || memberDocumentNumberField.getText().trim().isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", "Document number is required.");
            return false;
        }
        
        if (memberAssignedRoomCombo.getValue() == null) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", "Assigned room is required.");
            return false;
        }
        
        String assignedRoom = memberAssignedRoomCombo.getValue();
        boolean roomExists = groupRoomsList.stream()
                .anyMatch(room -> assignedRoom.equals(room.getRoomNumber()));
        
        if (!roomExists) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", 
                     "Assigned room must be added to the group booking first.");
            return false;
        }
        
        return true;
    }

    private boolean validateGroupBooking() {
        if (groupNameField.getText() == null || groupNameField.getText().trim().isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", "Group name is required.");
            return false;
        }
        
        if (contactPersonField.getText() == null || contactPersonField.getText().trim().isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", "Contact person is required.");
            return false;
        }
        
        if (contactPhoneField.getText() == null || contactPhoneField.getText().trim().isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", "Contact phone is required.");
            return false;
        }
        
        if (groupCheckInDate.getValue() == null) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", "Check-in date is required.");
            return false;
        }
        
        if (groupCheckOutDate.getValue() == null) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", "Check-out date is required.");
            return false;
        }
        
        if (!groupCheckOutDate.getValue().isAfter(groupCheckInDate.getValue())) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", 
                     "Check-out date must be after check-in date.");
            return false;
        }
        
        if (groupRoomsList.isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", 
                     "At least one room must be added to the group booking.");
            return false;
        }
        
        List<String> validationErrors = currentGroupBooking.validate();
        if (!validationErrors.isEmpty()) {
            String errorMessage = String.join("\n", validationErrors);
            showModernAlert(Alert.AlertType.WARNING, "Validation Error", errorMessage);
            return false;
        }
        
        return true;
    }

    private void updateGroupBookingFromForm() {
        currentGroupBooking.setGroupName(groupNameField.getText().trim());
        currentGroupBooking.setCompanyName(companyNameField.getText().trim());
        currentGroupBooking.setContactPerson(contactPersonField.getText().trim());
        currentGroupBooking.setContactPhone(contactPhoneField.getText().trim());
        currentGroupBooking.setContactEmail(contactEmailField.getText().trim());
        currentGroupBooking.setGstNumber(groupGstField.getText().trim());
        currentGroupBooking.setAddress(groupAddressArea.getText().trim());
        currentGroupBooking.setNationality(groupNationalityCombo.getValue());
        currentGroupBooking.setCheckInDate(groupCheckInDate.getValue());
        currentGroupBooking.setCheckOutDate(groupCheckOutDate.getValue());
        
        System.out.println("Group booking data updated from form");
    }

    private void updateGroupBookingDates() {
        LocalDate checkIn = groupCheckInDate.getValue();
        LocalDate checkOut = groupCheckOutDate.getValue();
        
        if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
            for (RoomBooking room : groupRoomsList) {
                room.setCheckInDate(checkIn);
                room.setCheckOutDate(checkOut);
            }
            
            currentGroupBooking.setCheckInDate(checkIn);
            currentGroupBooking.setCheckOutDate(checkOut);
            
            groupRoomsTableMain.refresh();
            updateGroupBookingSummaryMain();
        }
    }

//    private void updateGroupBookingSummaryMain() {
//        groupTotalRoomsLabel.setText(String.valueOf(currentGroupBooking.getTotalRooms()));
//        groupTotalMembersLabel.setText(String.valueOf(currentGroupBooking.getTotalMembers()));
//        groupTotalNightsLabel.setText(String.valueOf(currentGroupBooking.getTotalNights()));
//        groupTotalAmountLabel.setText(String.format("₹%.2f", currentGroupBooking.getBaseAmount()));
//        groupGstAmountLabel.setText(String.format("₹%.2f", currentGroupBooking.getGstAmount()));
//        groupFinalAmountLabel.setText(String.format("₹%.2f", currentGroupBooking.getTotalAmount()));
//    }

    private void clearGroupBookingSummary() {
        groupTotalRoomsLabel.setText("0");
        groupTotalMembersLabel.setText("0");
        groupTotalNightsLabel.setText("0");
        groupTotalAmountLabel.setText("₹0.00");
        groupGstAmountLabel.setText("₹0.00");
        groupFinalAmountLabel.setText("₹0.00");
    }

    private void clearMemberForm() {
        memberNameField.clear();
        memberPhoneField.clear();
        memberEmailField.clear();
        memberDocumentNumberField.clear();
        memberAssignedRoomCombo.getSelectionModel().clearSelection();
    }

    private void removeRoomFromGroup(RoomBooking room) {
        if (room == null) return;
        
        String roomNumber = room.getRoomNumber();
        
        // Remove from group booking data
        currentGroupBooking.removeRoom(room);
        
        // Remove from observable list
        groupRoomsList.remove(room);
        
        // Add back to available rooms
        String roomDisplay = String.format("%s - %s (₹%.2f/night)", 
            room.getRoomNumber(), room.getRoomType(), room.getRatePerNight());
        availableRooms.add(roomDisplay);
        availableRoomsCombo.setItems(FXCollections.observableArrayList(availableRooms));
        
        // Remove members assigned to this room
        if (groupMembersList != null) {
            groupMembersList.removeIf(member -> roomNumber.equals(member.getAssignedRoom()));
        }
        
        // Update room assignment combo
        updateMemberRoomAssignmentCombo();
        
        // Update summary
        updateGroupBookingSummaryMain();
        
        System.out.println("Room " + roomNumber + " removed from group booking");
        
        showModernAlert(Alert.AlertType.INFORMATION, "Room Removed", 
                 "Room " + roomNumber + " has been removed from the group booking.");
    }
    private void removeMemberFromGroup(GuestMember member) {
        currentGroupBooking.removeMember(member);
        
        groupMembersList.remove(member);
        
        updateGroupBookingSummaryMain();
        
        System.out.println("Member " + member.getName() + " removed from group booking");
    }

   private void openPaymentWindowForGroupBooking() {
    try {
        System.out.println("Opening payment window for group booking...");
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Payment.fxml"));
        Parent root = loader.load();
        
        PaymentController paymentController = loader.getController();
        
        // Create proper GroupBookingData instance
        GroupBookingData groupData = new GroupBookingData();
        groupData.setGroupName(groupNameField.getText().trim());
        groupData.setCompanyName(companyNameField.getText().trim());
        groupData.setContactPerson(contactPersonField.getText().trim());
        groupData.setContactPhone(contactPhoneField.getText().trim());
        groupData.setContactEmail(contactEmailField.getText().trim());
        groupData.setGstNumber(groupGstField.getText().trim());
        groupData.setAddress(groupAddressArea.getText().trim());
        groupData.setNationality(groupNationalityCombo.getValue());
        groupData.setCheckInDate(groupCheckInDate.getValue());
        groupData.setCheckOutDate(groupCheckOutDate.getValue());
        groupData.setDocumentPath(currentGroupBooking.getDocumentPath());
        groupData.setPhotoPath(currentGroupBooking.getPhotoPath());
        
        // Add rooms
        for (RoomBooking room : groupRoomsList) {
            // Convert your inner RoomBooking to application.models.RoomBooking
            application.models.RoomBooking properRoom = new application.models.RoomBooking(
                room.getRoomNumber(),
                room.getRoomType(),
                room.getRatePerNightValue(), // This returns BigDecimal
                groupCheckInDate.getValue(),
                groupCheckOutDate.getValue(),
                room.getGuestName()
            );
            groupData.addRoom(properRoom);
        }
        
        // Add members
        for (GuestMember member : groupMembersList) {
            // Convert your inner GuestMember to application.models.GuestMember
            application.models.GuestMember properMember = new application.models.GuestMember(
                member.getName(),
                member.getPhone(),
                member.getEmail(),
                member.getDocumentType(),
                member.getDocumentNumber(),
                member.getAssignedRoom()
            );
            groupData.addMember(properMember);
        }
        
        paymentController.setGroupBookingContext(groupData);
        
        Stage paymentStage = new Stage();
        paymentStage.setTitle("Payment - " + groupData.getGroupName());
        paymentStage.setScene(new Scene(root));
        paymentStage.initModality(Modality.WINDOW_MODAL);
        paymentStage.initOwner(proceedToPaymentBtn.getScene().getWindow());
        
        paymentStage.showAndWait();
        
        if (paymentController.isPaymentSuccessful()) {
            showModernAlert(Alert.AlertType.INFORMATION, "Success", 
                     "Group booking confirmed and payment processed successfully!");
            
            handleClearGroupFormMain();
            
            bookingTabPane.getSelectionModel().select(singleBookingTab);
        } else {
            System.out.println("Payment was cancelled or failed");
        }
        
    } catch (IOException e) {
        System.err.println("Error opening payment window: " + e.getMessage());
        showModernAlert(Alert.AlertType.ERROR, "Error", 
                 "Could not open payment window: " + e.getMessage());
    }
}

    // ============ SUPPORTING CLASSES FROM BOTH CODES ============

    public static class GuestDocument {
        private final StringProperty personName;
        private final StringProperty docType;
        private final StringProperty fileName;
        private final StringProperty filePath;
        
        public GuestDocument(String personName, String docType, String fileName, String filePath) {
            this.personName = new SimpleStringProperty(personName);
            this.docType = new SimpleStringProperty(docType);
            this.fileName = new SimpleStringProperty(fileName);
            this.filePath = new SimpleStringProperty(filePath);
        }
        
        public String getPersonName() { return personName.get(); }
        public StringProperty personNameProperty() { return personName; }
        
        public String getDocType() { return docType.get(); }
        public StringProperty docTypeProperty() { return docType; }
        
        public String getFileName() { return fileName.get(); }
        public StringProperty fileNameProperty() { return fileName; }
        
        public String getFilePath() { return filePath.get(); }
        public StringProperty filePathProperty() { return filePath; }
    }

    public static class GroupRoom {
        private final StringProperty roomNo;
        private final StringProperty roomType;
        private final StringProperty guestIds;
        private final StringProperty ratePerNight;
        private final StringProperty totalAmount;
        
        public GroupRoom(String roomNo, String roomType, String guestIds, String ratePerNight, String totalAmount) {
            this.roomNo = new SimpleStringProperty(roomNo);
            this.roomType = new SimpleStringProperty(roomType);
            this.guestIds = new SimpleStringProperty(guestIds);
            this.ratePerNight = new SimpleStringProperty(ratePerNight);
            this.totalAmount = new SimpleStringProperty(totalAmount);
        }
        
        public String getRoomNo() { return roomNo.get(); }
        public void setRoomNo(String roomNo) { this.roomNo.set(roomNo); }
        public StringProperty roomNoProperty() { return roomNo; }
        
        public String getRoomType() { return roomType.get(); }
        public void setRoomType(String roomType) { this.roomType.set(roomType); }
        public StringProperty roomTypeProperty() { return roomType; }
        
        public String getGuestIds() { return guestIds.get(); }
        public void setGuestIds(String guestIds) { this.guestIds.set(guestIds); }
        public StringProperty guestIdsProperty() { return guestIds; }
        
        public String getRatePerNight() { return ratePerNight.get(); }
        public void setRatePerNight(String ratePerNight) { this.ratePerNight.set(ratePerNight); }
        public StringProperty ratePerNightProperty() { return ratePerNight; }
        
        public String getTotalAmount() { return totalAmount.get(); }
        public void setTotalAmount(String totalAmount) { this.totalAmount.set(totalAmount); }
        public StringProperty totalAmountProperty() { return totalAmount; }
    }

    // Classes from second code
//    public static class GroupBookingData {
//        private String groupName;
//        private String companyName;
//        private String contactPerson;
//        private String contactPhone;
//        private String contactEmail;
//        private String gstNumber;
//        private String address;
//        private String nationality;
//        private LocalDate checkInDate;
//        private LocalDate checkOutDate;
//        private String documentPath;
//        private String photoPath;
//        private final List<RoomBooking> rooms = new ArrayList<>();
//        private final List<GuestMember> members = new ArrayList<>();
//        
//        public String getGroupName() { return groupName; }
//        public void setGroupName(String groupName) { this.groupName = groupName; }
//        
//        public String getCompanyName() { return companyName; }
//        public void setCompanyName(String companyName) { this.companyName = companyName; }
//        
//        public String getContactPerson() { return contactPerson; }
//        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
//        
//        public String getContactPhone() { return contactPhone; }
//        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
//        
//        public String getContactEmail() { return contactEmail; }
//        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
//        
//        public String getGstNumber() { return gstNumber; }
//        public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
//        
//        public String getAddress() { return address; }
//        public void setAddress(String address) { this.address = address; }
//        
//        public String getNationality() { return nationality; }
//        public void setNationality(String nationality) { this.nationality = nationality; }
//        
//        public LocalDate getCheckInDate() { return checkInDate; }
//        public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
//        
//        public LocalDate getCheckOutDate() { return checkOutDate; }
//        public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
//        
//        public String getDocumentPath() { return documentPath; }
//        public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
//        
//        public String getPhotoPath() { return photoPath; }
//        public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
//        
//        public List<RoomBooking> getRooms() { return rooms; }
//        public void addRoom(RoomBooking room) { this.rooms.add(room); }
//        public void removeRoom(RoomBooking room) { this.rooms.remove(room); }
//        
//        public List<GuestMember> getMembers() { return members; }
//        public void addMember(GuestMember member) { this.members.add(member); }
//        public void removeMember(GuestMember member) { this.members.remove(member); }
//        
//        public int getTotalRooms() { return rooms.size(); }
//        public int getTotalMembers() { return members.size(); }
//        public long getTotalNights() {
//            if (checkInDate == null || checkOutDate == null) return 0;
//            return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
//        }
//        
//        public BigDecimal getBaseAmount() {
//            return rooms.stream()
//                    .map(RoomBooking::getTotalAmountValue) // Use the method that returns BigDecimal
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//        }
//        
//        public BigDecimal getGstAmount() {
//            BigDecimal base = getBaseAmount();
//            GstCalculationService.GstResult result = GstCalculationService.getInstance().calculate(base, true);
//            return result.getTaxAmount();
//        }
//        
//        public BigDecimal getTotalAmount() {
//            return getBaseAmount().add(getGstAmount());
//        }
//        
//        public List<String> validate() {
//            List<String> errors = new ArrayList<>();
//            if (groupName == null || groupName.trim().isEmpty()) errors.add("Group name is required");
//            if (contactPerson == null || contactPerson.trim().isEmpty()) errors.add("Contact person is required");
//            if (contactPhone == null || contactPhone.trim().isEmpty()) errors.add("Contact phone is required");
//            if (checkInDate == null) errors.add("Check-in date is required");
//            if (checkOutDate == null) errors.add("Check-out date is required");
//            if (checkInDate != null && checkOutDate != null && !checkOutDate.isAfter(checkInDate)) 
//                errors.add("Check-out date must be after check-in date");
//            if (rooms.isEmpty()) errors.add("At least one room is required");
//            return errors;
//        }
//    }

//    public static class RoomBooking {
//        private final StringProperty roomNumber;
//        private final StringProperty roomType;
//        private final StringProperty ratePerNight;
//        private final StringProperty nights;
//        private final StringProperty totalAmount;
//        private final StringProperty guestName;
//        
//        public RoomBooking(String roomNumber, String roomType, BigDecimal ratePerNight, 
//                         LocalDate checkIn, LocalDate checkOut, String guestName) {
//            this.roomNumber = new SimpleStringProperty(roomNumber);
//            this.roomType = new SimpleStringProperty(roomType);
//            this.ratePerNight = new SimpleStringProperty(String.format("₹%.2f", ratePerNight));
//            
//            long nightsCount = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
//            this.nights = new SimpleStringProperty(String.valueOf(nightsCount));
//            
//            BigDecimal total = ratePerNight.multiply(BigDecimal.valueOf(nightsCount));
//            this.totalAmount = new SimpleStringProperty(String.format("₹%.2f", total));
//            this.guestName = new SimpleStringProperty(guestName);
//        }
//        
//        public String getRoomNumber() { return roomNumber.get(); }
//        public String getRoomType() { return roomType.get(); }
//        public String getRatePerNight() { return ratePerNight.get(); }
//        public String getNights() { return nights.get(); }
//        public String getTotalAmount() { return totalAmount.get(); }
//        public String getGuestName() { return guestName.get(); }
//        
//        public StringProperty roomNumberProperty() { return roomNumber; }
//        public StringProperty roomTypeProperty() { return roomType; }
//        public StringProperty ratePerNightProperty() { return ratePerNight; }
//        public StringProperty nightsProperty() { return nights; }
//        public StringProperty totalAmountProperty() { return totalAmount; }
//        public StringProperty guestNameProperty() { return guestName; }
//        
//        public void setCheckInDate(LocalDate checkIn) {
//            // Implementation for date update
//        }
//        
//        public void setCheckOutDate(LocalDate checkOut) {
//            // Implementation for date update
//        }
//        
//        public BigDecimal getRatePerNightValue() {
//            String rateStr = ratePerNight.get().replace("₹", "").trim();
//            return new BigDecimal(rateStr);
//        }
//        
//        public BigDecimal getTotalAmountValue() {
//            String totalStr = totalAmount.get().replace("₹", "").trim();
//            return new BigDecimal(totalStr);
//        }
//    }

//    public static class GuestMember {
//        private final StringProperty name;
//        private final StringProperty phone;
//        private final StringProperty email;
//        private final StringProperty documentType;
//        private final StringProperty documentNumber;
//        private final StringProperty assignedRoom;
//        
//        public GuestMember(String name, String phone, String email, String documentType, 
//                          String documentNumber, String assignedRoom) {
//            this.name = new SimpleStringProperty(name);
//            this.phone = new SimpleStringProperty(phone);
//            this.email = new SimpleStringProperty(email);
//            this.documentType = new SimpleStringProperty(documentType);
//            this.documentNumber = new SimpleStringProperty(documentNumber);
//            this.assignedRoom = new SimpleStringProperty(assignedRoom);
//        }
//        
//        public String getName() { return name.get(); }
//        public String getPhone() { return phone.get(); }
//        public String getEmail() { return email.get(); }
//        public String getDocumentType() { return documentType.get(); }
//        public String getDocumentNumber() { return documentNumber.get(); }
//        public String getAssignedRoom() { return assignedRoom.get(); }
//        
//        public StringProperty nameProperty() { return name; }
//        public StringProperty phoneProperty() { return phone; }
//        public StringProperty emailProperty() { return email; }
//        public StringProperty documentTypeProperty() { return documentType; }
//        public StringProperty documentNumberProperty() { return documentNumber; }
//        public StringProperty assignedRoomProperty() { return assignedRoom; }
//    }
}