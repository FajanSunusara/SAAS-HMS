package application.controllers;

import application.models.*;
import application.services.dao.*;
import application.services.GstCalculationService;
import application.utils.PropertyReader;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class PaymentController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal DEFAULT_GST_RATE = new BigDecimal("18.00");

    // UI Components
    @FXML private TextField guestNameTextField;
    @FXML private Label roomNumberLabel;
    @FXML private Label roomTypeLabel;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private Label numNightsLabel;
    @FXML private Label roomPricePerNightLabel;
    @FXML private Label roomChargesLabel;
    @FXML private Label serviceChargesLabel;
    @FXML private TextField discountTextField;
    @FXML private Label discountAppliedLabel;
    @FXML private Label advancePaidLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label gstLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label amountDueLabel;
    @FXML private TableView<ServiceLineItem> serviceDetailsTable;
    @FXML private TableColumn<ServiceLineItem, String> colServiceName;
    @FXML private TableColumn<ServiceLineItem, Integer> colServiceQty;
    @FXML private TableColumn<ServiceLineItem, Double> colServiceUnitPrice;
    @FXML private TableColumn<ServiceLineItem, Double> colServiceAmount;
    @FXML private Label serviceTotalLabel;
    @FXML private Label gstBreakdownLabel;
    @FXML private Label gstAmountLabel;
    @FXML private Label baseAmountLabel;
    @FXML private TextField amountPayingField;
    @FXML private ToggleGroup paymentMethodToggleGroup;
    @FXML private ToggleButton cashToggle;
    @FXML private ToggleButton upiToggle;
    @FXML private ToggleButton cardToggle;
    @FXML private ToggleButton netbankingToggle;
    @FXML private StackPane paymentDetailsStackPane;
    @FXML private VBox cashPane;
    @FXML private VBox upiPane;
    @FXML private ImageView qrImageView;
    @FXML private TextField upiIdField;
    @FXML private VBox cardPane;
    @FXML private TextField cardNumberField;
    @FXML private TextField cardHolderNameField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private VBox netbankingPane;
    @FXML private TextField bankNameField;
    @FXML private TextField accountNumberField;
    @FXML private TextField ifscNetbankingField;
    @FXML private TextField accountHolderNameNetbankingField;
    @FXML private HBox refundModeHeader;
    @FXML private Label refundModeSubtitle;
    @FXML private VBox paymentModeSection;
    @FXML private VBox refundModeSection;
    @FXML private Label originalPaymentIdLabel;
    @FXML private Label originalPaymentAmountLabel;
    @FXML private ComboBox<String> refundTypeComboBox;
    @FXML private TextField refundAmountField;
    @FXML private TextArea refundReasonTextArea;
    @FXML private Label refundCalculationLabel;
    @FXML private CheckBox applyProcessingFeeCheckBox;
    @FXML private TextField processingFeeField;
    @FXML private Button calculateRefundButton;
    @FXML private VBox refundCalculationBox;
    @FXML private Button processRefundButton;
    @FXML private Button submitPaymentButton;
    @FXML private HBox paymentAmountBox;
    @FXML private Label amountPayingLabel;

    // Context variables
    private Long bookingId;
    private Long guestId;
    private Long selectedGuestId;
    private String guestNameCtx;
    private String guestPhoneCtx;
    private String guestEmailCtx;
    private String guestAddressCtx;
    private String guestGstCtx;
    private String roomNoCtx;
    private String roomTypeCtx;
    private LocalDate checkInCtx;
    private LocalDate checkOutCtx;
    private BigDecimal ratePerNightCtx = ZERO;
    private BigDecimal totalAmountCtx = ZERO;
    private BigDecimal advancePaidCtx = ZERO;
    private Long reservationId = null;
    private boolean isReservationPayment = false;
    private boolean isSettlementPayment = false;

    // Computed values
    private double currentDiscount = 0.0;
    private double calculatedRoomCharges = 0.0;
    private double calculatedServiceCharges = 0.0;
    private double calculatedSubtotal = 0.0;
    private double calculatedGst = 0.0;
    private double calculatedTotalPayable = 0.0;
    private double calculatedAmountDue = 0.0;
    
    // Refund mode variables
    private boolean isRefundMode = false;
    private BigDecimal originalPaymentAmount = ZERO;
    private Long originalPaymentId = null;

    private BookingData bookingData;
    private boolean paymentSuccessful = false;
    private final PropertyReader propertyReader = PropertyReader.getInstance();
    private final GstCalculationService gstService = GstCalculationService.getInstance();
    
    private GstCalculationService.GstResult gstResultCtx;
    private String documentLinkCtx;
    private String nationalityCtx = "India";
    private String documentPathCtx;
    private BigDecimal gstRateCtx = DEFAULT_GST_RATE;
    private BigDecimal baseAmountCtx = ZERO;
    private BigDecimal gstAmountCtx = ZERO;
    private Boolean gstIncludedCtx = false;
    private String sourceWebsiteCtx = "Direct";
    private boolean isServicePaymentMode = false;
    private double servicePaymentAmount = 0.0;
    
    // Service items and DAOs
    private ObservableList<ServiceLineItem> serviceItemsObservableList = FXCollections.observableArrayList();
    private ServiceUsedDAO serviceUsedDAO = new ServiceUsedDAO();
    private ServiceItemDAO serviceItemDAO = new ServiceItemDAO();

    // ============ INITIALIZATION ============

    @FXML
    public void initialize() {
        setupPaymentMethodToggle();
        setupQRCode();
        setupServiceDetailsTable();
        initializeRefundComponents();
        
        if (cashToggle != null) cashToggle.setSelected(true);
        hideAllPaymentDetailsPanes();
    }

    private void setupPaymentMethodToggle() {
        if (paymentMethodToggleGroup != null) {
            paymentMethodToggleGroup.selectedToggleProperty().addListener((obs, o, n) -> {
                if (n != null) showPaymentDetailsPane(Objects.toString(n.getUserData(), ""));
                else hideAllPaymentDetailsPanes();
            });
        }
    }

    private void setupQRCode() {
        if (qrImageView != null) {
            try {
                Image qr = new Image(getClass().getResourceAsStream("/images/sample_qr_code.png"));
                qrImageView.setImage(qr);
            } catch (Exception ignored) {}
        }
    }

    private void setupServiceDetailsTable() {
        colServiceName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemName()));
        colServiceQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        colServiceUnitPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getUnitPrice()).asObject());
        colServiceAmount.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalAmount()).asObject());

        colServiceUnitPrice.setCellFactory(col -> createCurrencyTableCell());
        colServiceAmount.setCellFactory(col -> createCurrencyTableCell());

        serviceDetailsTable.setItems(serviceItemsObservableList);
    }

    private TableCell<ServiceLineItem, Double> createCurrencyTableCell() {
        return new TableCell<ServiceLineItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        };
    }

    private void initializeRefundComponents() {
        if (refundTypeComboBox != null) {
            refundTypeComboBox.getItems().addAll(
                "Full Refund", "Partial Refund", "Cancellation",
                "Early Departure", "Service Issue", "Overbooking"
            );
            refundTypeComboBox.setValue("Partial Refund");
        }
        
        if (processingFeeField != null) processingFeeField.setText("0.00");
        if (applyProcessingFeeCheckBox != null) applyProcessingFeeCheckBox.setSelected(false);
        setupRefundCalculation();
    }

    private void setupRefundCalculation() {
        if (calculateRefundButton != null) {
            calculateRefundButton.setOnAction(e -> calculateRefundAmount());
        }
    }

    // ============ PUBLIC API METHODS ============

    public boolean isPaymentSuccessful() { 
        return paymentSuccessful; 
    }

    public void setBookingContext(Long bookingId, Long guestId) {
        setBookingContext(bookingId, guestId, false);
    }

    public void setBookingContext(Long bookingId, Long guestId, boolean isSettlementPayment) {
        this.bookingId = bookingId;
        this.guestId = guestId;
        this.isReservationPayment = false;
        this.isSettlementPayment = isSettlementPayment;
        loadExistingBookingData();
    }

    public void setCompleteNewBookingContext(
            Long selectedGuestId,
            String name, String phone, String email, String address, String gst,
            String roomNo, String roomType,
            LocalDate checkIn, LocalDate checkOut,
            BigDecimal ratePerNight, BigDecimal totalAmount, BigDecimal advancePaid,
            String nationality, String documentPath, BigDecimal gstRate,
            BigDecimal baseAmount, BigDecimal gstAmount, Boolean gstIncluded,
            String sourceWebsite) {
        
        setBasicContext(selectedGuestId, name, phone, email, address, gst, roomNo, roomType, 
                       checkIn, checkOut, ratePerNight, totalAmount, advancePaid);
        
        this.nationalityCtx = safeString(nationality, "India");
        this.documentPathCtx = documentPath;
        this.gstRateCtx = safeBigDecimal(gstRate, DEFAULT_GST_RATE);
        this.baseAmountCtx = safeBigDecimal(baseAmount);
        this.gstAmountCtx = safeBigDecimal(gstAmount);
        this.gstIncludedCtx = gstIncluded != null ? gstIncluded : false;
        this.sourceWebsiteCtx = safeString(sourceWebsite, "Direct");
        
        this.isReservationPayment = false;
        this.isSettlementPayment = false;
    }

    public void setReservationContext(
            Long selectedGuestId,
            String name, String phone, String email, String address, String gst,
            String roomNo, Object roomType,
            LocalDate checkIn, LocalDate checkOut,
            BigDecimal ratePerNight, BigDecimal totalAmount, BigDecimal advancePaid,
            Long reservationId) {
        
        setBasicContext(selectedGuestId, name, phone, email, address, gst, roomNo, 
                       roomType != null ? roomType.toString() : null, 
                       checkIn, checkOut, ratePerNight, totalAmount, advancePaid);
        
        this.reservationId = reservationId;
        this.isReservationPayment = true;
        this.isSettlementPayment = false;
    }

    public void setEnhancedBookingContext(
            Long selectedGuestId,
            String name, String phone, String email, String address, String gst, String nationality,
            String roomNo, Object roomType,
            LocalDate checkIn, LocalDate checkOut,
            BigDecimal ratePerNight, BigDecimal totalAmount, BigDecimal advancePaid,
            GstCalculationService.GstResult gstResult, String documentLink) {
        
        setBasicContext(selectedGuestId, name, phone, email, address, gst, roomNo, 
                       roomType != null ? roomType.toString() : null,
                       checkIn, checkOut, ratePerNight, totalAmount, advancePaid);
        
        this.nationalityCtx = nationality; 
        this.gstResultCtx = gstResult;
        this.documentLinkCtx = documentLink;
        this.isReservationPayment = false;
        this.isSettlementPayment = false;
    }

    public void setBookingContext(Long bookingId, String guestName, String roomNo, String roomType,
            LocalDate checkInDate, LocalDate checkOutDate, double pricePerNight) {
        setTextField(guestNameTextField, guestName);
        setLabel(roomNumberLabel, roomNo);
        setLabel(roomTypeLabel, roomType);

        if (checkInDate != null) checkInDatePicker.setValue(checkInDate);
        if (checkOutDate != null) checkOutDatePicker.setValue(checkOutDate);

        long nights = calculateNights(checkInDate, checkOutDate);
        setLabel(numNightsLabel, String.valueOf(nights));
        setLabel(roomPricePerNightLabel, formatCurrency(pricePerNight));
    }

    // ============ UI POPULATION METHODS ============

    public void populateForNewBookingContext() {
        loadServiceItemsForBooking(null);
        populateUI();
    }

    public void populateForReservationContext() {
        loadServiceItemsForBooking(null);
        populateUI();
    }

    public void populateForExistingBooking() {
        if (bookingId != null) populateUIForExistingBooking();
    }
    private void populateUI() {
        setBasicUIValues();
        
        if (isServicePaymentMode) {
            // Update UI for service payment mode
            updateUIForServicePayment();
            calculateAndDisplayServiceFinancials();
        } else {
            calculateAndDisplayFinancials();
        }
    }

    private void populateUIForExistingBooking() {
        setBasicUIValues();
        calculateAndDisplayFinancials();
        
        if (amountPayingField != null) {
            if (isSettlementPayment) {
                amountPayingField.setText(formatDecimal(calculatedAmountDue));
            } else {
                amountPayingField.setText(formatDecimal(advancePaidCtx.doubleValue()));
            }
        }
    }

    private void setBasicUIValues() {
        setTextField(guestNameTextField, guestNameCtx);
        setLabel(roomNumberLabel, roomNoCtx);
        setLabel(roomTypeLabel, roomTypeCtx);
        
        if (checkInDatePicker != null) checkInDatePicker.setValue(checkInCtx);
        if (checkOutDatePicker != null) checkOutDatePicker.setValue(checkOutCtx);

        long nights = calculateNights(checkInCtx, checkOutCtx);
        setLabel(numNightsLabel, String.valueOf(nights));
        setLabel(roomPricePerNightLabel, formatCurrency(ratePerNightCtx.doubleValue()));
    }

    private void calculateAndDisplayFinancials() {
        calculatedRoomCharges = baseAmountCtx.doubleValue();
        calculatedServiceCharges = serviceItemsObservableList.stream()
                .mapToDouble(ServiceLineItem::getTotalAmount)
                .sum();

        setLabel(roomChargesLabel, formatCurrency(calculatedRoomCharges));
        setLabel(serviceChargesLabel, formatCurrency(calculatedServiceCharges));
        setLabel(discountAppliedLabel, formatCurrency(currentDiscount));

        calculatedSubtotal = Math.max(0.0, calculatedRoomCharges + calculatedServiceCharges - currentDiscount);
        setLabel(subtotalLabel, formatCurrency(calculatedSubtotal));

        handleGstCalculation();
        
        double totalPayable = calculatedSubtotal + calculatedGst;
        setLabel(totalAmountLabel, formatCurrency(totalPayable));
        setLabel(advancePaidLabel, formatCurrency(advancePaidCtx.doubleValue()));

        calculatedAmountDue = Math.max(0.0, totalPayable - advancePaidCtx.doubleValue());
        setLabel(amountDueLabel, formatCurrency(calculatedAmountDue));
    }

    private void handleGstCalculation() {
        if (gstResultCtx != null) {
            calculatedGst = gstAmountCtx.doubleValue();
            setLabel(gstLabel, formatCurrency(calculatedGst) + " @ " + formatPercentage(gstResultCtx.getGstRate()));
            setLabel(baseAmountLabel, formatCurrency(baseAmountCtx.doubleValue()));
            setLabel(gstAmountLabel, formatCurrency(gstAmountCtx.doubleValue()));
            setLabel(gstBreakdownLabel, String.format("GST @ %s", formatPercentage(gstResultCtx.getGstRate())));
        } else {
            calculatedGst = gstAmountCtx.doubleValue();
            setLabel(gstLabel, formatCurrency(calculatedGst));
            setLabel(baseAmountLabel, formatCurrency(calculatedSubtotal));
            setLabel(gstAmountLabel, formatCurrency(calculatedGst));
            double gstRate = propertyReader.getGstRate();
            setLabel(gstBreakdownLabel, formatPercentage(gstRate));
        }
    }

    // ============ PAYMENT PROCESSING ============

    @FXML
    private void handleSubmitPayment() {
        if (!validatePaymentInputs()) return;

        BigDecimal amountNow = parsePaymentAmount();
        if (amountNow == null) return;

        String method = getSelectedMethodOrNull();
        String transactionId = buildTransactionId(method);

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                processPayment(conn, amountNow, method, transactionId);
                conn.commit();
                paymentSuccessful = true;
                showSuccessAndInvoice(method, transactionId);
                closeWindowIfAny();
            } catch (Exception ex) {
                conn.rollback();
                handlePaymentError(ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private boolean validatePaymentInputs() {
        String method = getSelectedMethodOrNull();
        if (method == null) {
            showAlert(Alert.AlertType.WARNING, "Payment Method", "Please select a payment method.");
            return false;
        }
        return validateMethodSpecificInputs(method);
    }

    private boolean validateMethodSpecificInputs(String method) {
        switch (method) {
            case "Card":
                if (isBlank(cardNumberField) || isBlank(cardHolderNameField) || 
                    isBlank(expiryDateField) || isBlank(cvvField)) {
                    showAlert(Alert.AlertType.WARNING, "Missing Info", "All card details are required.");
                    return false;
                }
                break;
            case "UPI":
                if (isBlank(upiIdField)) {
                    showAlert(Alert.AlertType.WARNING, "Missing Info", "UPI Transaction/Reference ID is required.");
                    return false;
                }
                break;
            case "Netbanking":
                if (isBlank(bankNameField) || isBlank(accountNumberField)) {
                    showAlert(Alert.AlertType.WARNING, "Missing Info", "Bank Name and Account Number are required.");
                    return false;
                }
                break;
        }
        return true;
    }

    private BigDecimal parsePaymentAmount() {
        try {
            String amountText = amountPayingField.getText();
            if (isBlankString(amountText)) {
                showAlert(Alert.AlertType.WARNING, "Missing Amount", "Please enter the amount to pay.");
                return null;
            }

            BigDecimal amount = parseCurrencyToBD(amountText);
            if (amount.compareTo(ZERO) <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Payment amount must be greater than zero.");
                return null;
            }
            return amount;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid payment amount.");
            return null;
        }
    }

//    private void processPayment(Connection conn, BigDecimal amount, String method, String transactionId) throws SQLException {
//        Long effectiveGuestId = getEffectiveGuestId(conn);
//
//        if (isReservationPayment) {
//            handleReservationPayment(conn, effectiveGuestId, amount, method, transactionId);
//        } else if (bookingId != null) {
//            handleExistingBookingPayment(conn, effectiveGuestId, amount, method, transactionId);
//        } else {
//            handleNewBookingPayment(conn, effectiveGuestId, amount, method, transactionId);
//        }
//    }

    private Long getEffectiveGuestId(Connection conn) throws SQLException {
        if (selectedGuestId != null) {
            updateGuestIfChanged(conn, selectedGuestId, guestNameCtx, guestPhoneCtx, guestEmailCtx, guestAddressCtx, guestGstCtx);
            return selectedGuestId;
        } else {
            return insertGuest(conn, guestNameCtx, guestPhoneCtx, guestEmailCtx, guestAddressCtx, guestGstCtx);
        }
    }

    // ============ DATABASE OPERATIONS ============

    private void loadExistingBookingData() {
        if (bookingId == null) return;
        
        String sql = """
            SELECT b.id, b.guest_id, b.room_no, b.check_in_date, b.check_out_date,
                   b.total_amount, b.advance_paid, b.payment_status, b.status, b.base_amount, 
                   b.gst_rate, b.gst_amount, b.gst_included,
                   g.name, g.phone, g.email, g.address, g.gst_number,
                   r.room_type, r.price,
                   COALESCE(i.due_amount, 0) as pending_amount
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            JOIN rooms r ON b.room_no = r.room_no
            LEFT JOIN invoices i ON b.id = i.booking_id
            WHERE b.id = ?
            """;
            
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    extractBookingDataFromResultSet(rs);
                    loadServiceItemsForBooking(bookingId);
                    populateUIForExistingBooking();
                }
            }
        } catch (SQLException e) {
            logError("Failed to load booking data", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load booking data: " + e.getMessage());
        }
    }

    private void extractBookingDataFromResultSet(ResultSet rs) throws SQLException {
        this.selectedGuestId = rs.getLong("guest_id");
        this.guestNameCtx = rs.getString("name");
        this.guestPhoneCtx = rs.getString("phone");
        this.guestEmailCtx = rs.getString("email");
        this.guestAddressCtx = rs.getString("address");
        this.guestGstCtx = rs.getString("gst_number");
        this.roomNoCtx = rs.getString("room_no");
        this.roomTypeCtx = rs.getString("room_type");
        this.checkInCtx = rs.getDate("check_in_date").toLocalDate();
        this.checkOutCtx = rs.getDate("check_out_date").toLocalDate();
        this.ratePerNightCtx = safeBigDecimal(rs.getBigDecimal("price"));
        this.totalAmountCtx = safeBigDecimal(rs.getBigDecimal("total_amount"));
        this.advancePaidCtx = safeBigDecimal(rs.getBigDecimal("advance_paid"));
        this.baseAmountCtx = safeBigDecimal(rs.getBigDecimal("base_amount"));
        this.gstRateCtx = safeBigDecimal(rs.getBigDecimal("gst_rate"), DEFAULT_GST_RATE);
        this.gstAmountCtx = safeBigDecimal(rs.getBigDecimal("gst_amount"));
        this.gstIncludedCtx = rs.getBoolean("gst_included");
        this.calculatedTotalPayable = rs.getDouble("pending_amount");
    }

    private void loadServiceItemsForBooking(Long bookingId) {
        serviceItemsObservableList.clear();
        if (bookingId == null) {
            setLabel(serviceTotalLabel, "Total Services: \u20B90.00");
            return;
        }

        try {
            List<ServiceUsed> servicesUsed = serviceUsedDAO.getServiceUsedByBookingId(bookingId);
            for (ServiceUsed serviceUsed : servicesUsed) {
                List<ServiceItem> items = serviceItemDAO.getServiceItemsByServiceUsedId(serviceUsed.getId());
                for (ServiceItem item : items) {
                    serviceItemsObservableList.add(new ServiceLineItem(
                            serviceUsed.getService() + " - " + item.getItemName(),
                            item.getQuantity(),
                            item.getPrice().doubleValue()
                    ));
                }
            }
            updateServiceTotal();
        } catch (SQLException e) {
            logError("Failed to load service items", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load service items: " + e.getMessage());
        }
    }

    private void updateServiceTotal() {
        double total = serviceItemsObservableList.stream()
                .mapToDouble(ServiceLineItem::getTotalAmount)
                .sum();
        calculatedServiceCharges = total;
        setLabel(serviceTotalLabel, String.format("Total Services: \u20B9%.2f", total));
    }

    // ============ PAYMENT HANDLERS ============

    private void handleReservationPayment(Connection conn, Long guestId, BigDecimal amount, String method, String transactionId) throws SQLException {
        validateDatesOrThrow(checkInCtx, checkOutCtx);
        ensureRoomAvailableForReservation(conn, roomNoCtx, checkInCtx, checkOutCtx, reservationId);
        updateReservationPaymentStatus(conn, reservationId, amount);
        insertReservationPayment(conn, reservationId, amount, method);

        if (LocalDate.now().equals(checkInCtx)) {
            updateRoomStatus(conn, roomNoCtx, "Reserved");
        }
    }

    private void handleExistingBookingPayment(Connection conn, Long guestId, BigDecimal amount, String method, String transactionId) throws SQLException {
        Booking b = fetchBooking(conn, bookingId);
        if (b == null) throw new SQLException("Booking no longer exists.");
        
        validateDatesOrThrow(b.getCheckInDate(), b.getCheckOutDate());
        ensureRoomAvailableOrThrow(conn, b.getRoomNo(), b.getCheckInDate(), b.getCheckOutDate(), bookingId);

        insertPayment(conn, bookingId, guestId, amount, method, transactionId);
        updateBookingAdvancePaid(conn, bookingId, amount);
        updateInvoiceAfterPaymentWithAdditiveTotals(conn, bookingId, amount);
        updatePaymentStatusFromInvoices(conn, bookingId);
        updateRoomStatusIfCheckedIn(conn, b.getRoomNo(), "Checked-in", bookingId);
    }

    private void handleNewBookingPayment(Connection conn, Long guestId, BigDecimal amount, String method, String transactionId) throws SQLException {
        System.out.println("=== CREATING NEW BOOKING IN DATABASE ===");
        
        validateDatesOrThrow(checkInCtx, checkOutCtx);
        ensureRoomAvailableOrThrow(conn, roomNoCtx, checkInCtx, checkOutCtx, null);

        BigDecimal subtotal = baseAmountCtx.compareTo(ZERO) > 0 ? baseAmountCtx : bd(calculatedSubtotal);
        BigDecimal gst = gstAmountCtx.compareTo(ZERO) > 0 ? gstAmountCtx : bd(calculatedGst);
        BigDecimal total = subtotal.add(gst);

        bookingId = insertCompleteBooking(conn, null, guestId, roomNoCtx, checkInCtx, checkOutCtx,
                total, amount, derivePaymentStatus(total, amount), "Confirmed");

        if (bookingId == null) throw new SQLException("Failed to create booking record");

        // Link reservation if exists
        Long resId = findActiveReservationForRoomAndDates(conn, roomNoCtx, checkInCtx, checkOutCtx);
        if (resId != null) {
            attachReservationToBooking(conn, bookingId, resId);
            markReservationArrived(conn, resId);
        }

        upsertInvoice(conn, bookingId, guestId, subtotal.doubleValue(), gst.doubleValue(), total.doubleValue(), amount.doubleValue());
        insertPayment(conn, bookingId, guestId, amount, method, transactionId);
        updatePaymentStatusFromInvoices(conn, bookingId);
        updateRoomStatusBasedOnCheckIn(conn, roomNoCtx, bookingId);
        
        System.out.println("=== BOOKING CREATION COMPLETED ===");
    }

    // ============ REFUND METHODS ============

    public void enableRefundMode(Long bookingId, Long paymentId, BigDecimal originalAmount) {
        this.isRefundMode = true;
        this.bookingId = bookingId;
        this.originalPaymentId = paymentId;
        this.originalPaymentAmount = originalAmount;
        this.reservationId = null;

        loadExistingBookingData();
        switchToRefundMode();
    }

    public void enableRefundModeWithReservation(ReservationStub reservation, Long paymentId, BigDecimal refundAmount) {
        this.isRefundMode = true;
        this.reservationId = reservation.getId();  
        this.originalPaymentId = paymentId;
        this.originalPaymentAmount = refundAmount;
        this.bookingId = null;
        
        populateUIFromReservation(reservation);
        switchToRefundMode();
    }

    private void switchToRefundMode() {
        setVisibility(refundModeHeader, true);
        setVisibility(paymentModeSection, false);
        setVisibility(refundModeSection, true);
        setVisibility(submitPaymentButton, false);
        setVisibility(processRefundButton, true);

        if (refundModeSubtitle != null) {
            String context = bookingId != null ? "Booking ID: " + bookingId : "Reservation ID: " + reservationId;
            refundModeSubtitle.setText("Processing refund for " + context);
        }
        
        if (originalPaymentIdLabel != null) {
            originalPaymentIdLabel.setText(originalPaymentId != null ? originalPaymentId.toString() : "N/A");
        }
        
        if (originalPaymentAmountLabel != null) {
            originalPaymentAmountLabel.setText("\u20B9" + formatDecimal(originalPaymentAmount.doubleValue()));
        }
        
        if (amountPayingLabel != null) amountPayingLabel.setText("Refund Amount:");
        if (amountPayingField != null) amountPayingField.setPromptText("Enter refund amount");
    }

    @FXML
    private void handleRefundPayment() {
        if (!validateRefundInputs()) return;

        BigDecimal refundAmount = parseRefundAmount();
        if (refundAmount == null) return;

        String refundType = refundTypeComboBox.getValue();
        String refundReason = refundReasonTextArea.getText();

        if (confirmRefund(refundAmount, refundType, refundReason)) {
            processRefund(refundAmount, refundType, refundReason);
        }
    }

    private boolean validateRefundInputs() {
        String refundType = refundTypeComboBox.getValue();
        String refundReason = refundReasonTextArea.getText();
        
        if (isBlankString(refundType)) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select a refund type.");
            return false;
        }
        
        if (isBlankString(refundReason)) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please provide a reason for the refund.");
            return false;
        }
        return true;
    }

    private BigDecimal parseRefundAmount() {
        try {
            String amountText = refundAmountField.getText();
            if (isBlankString(amountText)) {
                showAlert(Alert.AlertType.WARNING, "Missing Amount", "Please enter the refund amount.");
                return null;
            }
            
            BigDecimal refundAmount = parseCurrencyToBD(amountText);
            if (refundAmount.compareTo(ZERO) <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Refund amount must be greater than zero.");
                return null;
            }
            
            if (refundAmount.compareTo(originalPaymentAmount) > 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Amount", 
                        "Refund amount cannot exceed original payment amount of \u20B9" + formatDecimal(originalPaymentAmount.doubleValue()));
                return null;
            }
            
            return refundAmount;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid refund amount.");
            return null;
        }
    }

    private boolean confirmRefund(BigDecimal refundAmount, String refundType, String refundReason) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Refund");
        confirmAlert.setHeaderText("Refund Confirmation");
        confirmAlert.setContentText(String.format(
            "Are you sure you want to process this refund?\n\n" +
            "Refund Type: %s\n" +
            "Refund Amount: \u20B9%.2f\n" +
            "Original Payment: \u20B9%.2f\n" +
            "Reason: %s",
            refundType, refundAmount.doubleValue(), originalPaymentAmount.doubleValue(), refundReason));
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void processRefund(BigDecimal refundAmount, String refundType, String refundReason) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long refundId = insertRefundRecord(conn, refundAmount, refundType, refundReason);
                updateEntityAfterRefund(conn, refundAmount);
                
                if ("Full Refund".equals(refundType) || "Cancellation".equals(refundType)) {
                    handleFullRefundOrCancellation(conn);
                }
                
                conn.commit();
                showRefundSuccess(refundAmount, refundId);
                closeWindowIfAny();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logError("Failed to process refund", e);
            showAlert(Alert.AlertType.ERROR, "Refund Error", "Failed to process refund: " + e.getMessage());
        }
    }

    private Long insertRefundRecord(Connection conn, BigDecimal refundAmount, String refundType, String refundReason) throws SQLException {
        String sql = "INSERT INTO return_payments (" +
                "return_date, booking_id, reservation_id, guest_id, original_payment_id, return_type, return_reason, " +
                "original_amount, return_amount, deduction_amount, return_method, processing_fee, " +
                "status, notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int idx = 1;
            ps.setDate(idx++, Date.valueOf(LocalDate.now()));
            ps.setObject(idx++, bookingId, Types.INTEGER);
            ps.setObject(idx++, reservationId, Types.INTEGER);
            ps.setLong(idx++, selectedGuestId != null ? selectedGuestId : guestId);
            ps.setLong(idx++, originalPaymentId);
            ps.setString(idx++, refundType);
            ps.setString(idx++, refundReason);
            ps.setBigDecimal(idx++, originalPaymentAmount);
            ps.setBigDecimal(idx++, refundAmount);
            ps.setBigDecimal(idx++, originalPaymentAmount.subtract(refundAmount));
            ps.setString(idx++, "Cash");
            
            BigDecimal processingFee = applyProcessingFeeCheckBox.isSelected() ? 
                parseCurrencyToBD(processingFeeField.getText()) : ZERO;
            ps.setBigDecimal(idx++, processingFee);
            
            ps.setString(idx++, "Completed");
            ps.setString(idx++, "Refund processed through hotel management system");
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : null;
            }
        }
    }

    private void updateEntityAfterRefund(Connection conn, BigDecimal refundAmount) throws SQLException {
        if (bookingId != null) {
            updateBookingAfterRefund(conn, refundAmount);
            updateInvoiceAfterRefund(conn, refundAmount);
        } else if (reservationId != null) {
            updateReservationAfterRefund(conn, refundAmount);
        }
    }

    private void updateBookingAfterRefund(Connection conn, BigDecimal refundAmount) throws SQLException {
        String sql = "UPDATE bookings SET advance_paid = COALESCE(advance_paid, 0) - ?, " +
                     "payment_status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, refundAmount);
            ps.setString(2, "Refunded");
            ps.setLong(3, bookingId);
            ps.executeUpdate();
        }
    }

    private void updateReservationAfterRefund(Connection conn, BigDecimal refundAmount) throws SQLException {
        String sql = "UPDATE reservations SET advance_paid = COALESCE(advance_paid, 0) - ?, " +
                     "payment_status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, refundAmount);
            ps.setString(2, "Refunded");
            ps.setLong(3, reservationId);
            ps.executeUpdate();
        }
    }

    private void updateInvoiceAfterRefund(Connection conn, BigDecimal refundAmount) throws SQLException {
        String sql = "UPDATE invoices SET " +
                     "paid_amount = COALESCE(paid_amount, 0) - ?, " +
                     "due_amount = COALESCE(due_amount, 0) + ? " +
                     "WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, refundAmount);
            ps.setBigDecimal(2, refundAmount);
            ps.setLong(3, bookingId);
            ps.executeUpdate();
        }
    }

    private void handleFullRefundOrCancellation(Connection conn) throws SQLException {
        if (bookingId != null) {
            updateBookingStatus(conn, bookingId, "Cancelled");
            if (roomNoCtx != null) {
                updateRoomStatus(conn, roomNoCtx, "Available");
            }
        } else if (reservationId != null) {
            updateReservationStatus(conn, reservationId, "Cancelled");
        }
    }

    // ============ REFUND CALCULATION METHODS ============

    @FXML
    private void calculateRefundAmount() {
        if (checkInCtx == null || checkOutCtx == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Check-in and check-out dates are required for refund calculation.");
            return;
        }
        
        LocalDate today = LocalDate.now();
        String selectedRefundType = refundTypeComboBox.getValue();
        
        try {
            BigDecimal refundAmount = calculateRefundByType(selectedRefundType, today);
            String calculationDetails = generateRefundDetails(selectedRefundType, today, refundAmount);
            
            if (applyProcessingFeeCheckBox.isSelected()) {
                BigDecimal processingFee = parseCurrencyToBD(processingFeeField.getText());
                refundAmount = refundAmount.subtract(processingFee);
                calculationDetails += "\nLess processing fee: \u20B9" + formatDecimal(processingFee.doubleValue());
            }
            
            refundAmountField.setText(formatDecimal(refundAmount.doubleValue()));
            refundCalculationLabel.setText(calculationDetails);
            
        } catch (Exception e) {
            logError("Failed to calculate refund amount", e);
            showAlert(Alert.AlertType.ERROR, "Calculation Error", "Failed to calculate refund amount: " + e.getMessage());
        }
    }

    private BigDecimal calculateRefundByType(String refundType, LocalDate today) {
        switch (refundType) {
            case "Full Refund":
                return calculateFullRefund();
            case "Partial Refund":
                return calculatePartialRefund(today);
            case "Cancellation":
                return calculateCancellationRefund(today);
            case "Early Departure":
                return calculateEarlyDepartureRefund(today);
            case "Service Issue":
            case "Overbooking":
                return originalPaymentAmount;
            default:
                return ZERO;
        }
    }

    private BigDecimal calculateFullRefund() {
        return originalPaymentAmount;
    }

    private BigDecimal calculatePartialRefund(LocalDate departureDate) {
        long totalNights = ChronoUnit.DAYS.between(checkInCtx, checkOutCtx);
        long nightsUsed = ChronoUnit.DAYS.between(checkInCtx, departureDate);
        long unusedNights = Math.max(0, totalNights - nightsUsed);
        
        if (totalNights <= 0) return ZERO;
        
        BigDecimal perNightRate = originalPaymentAmount.divide(BigDecimal.valueOf(totalNights), 2, RoundingMode.HALF_UP);
        BigDecimal baseRefund = perNightRate.multiply(BigDecimal.valueOf(unusedNights));
        return baseRefund.multiply(new BigDecimal("0.90"));
    }

    private BigDecimal calculateCancellationRefund(LocalDate cancellationDate) {
        long daysUntilCheckIn = ChronoUnit.DAYS.between(cancellationDate, checkInCtx);
        
        BigDecimal refundPercentage;
        if (daysUntilCheckIn >= 7) {
            refundPercentage = new BigDecimal("0.90");
        } else if (daysUntilCheckIn >= 3) {
            refundPercentage = new BigDecimal("0.70");
        } else if (daysUntilCheckIn >= 1) {
            refundPercentage = new BigDecimal("0.50");
        } else {
            refundPercentage = new BigDecimal("0.25");
        }
        
        return originalPaymentAmount.multiply(refundPercentage);
    }

    private BigDecimal calculateEarlyDepartureRefund(LocalDate departureDate) {
        long totalNights = ChronoUnit.DAYS.between(checkInCtx, checkOutCtx);
        long nightsUsed = ChronoUnit.DAYS.between(checkInCtx, departureDate);
        long unusedNights = Math.max(0, totalNights - nightsUsed);
        
        if (totalNights <= 0 || unusedNights <= 0) return ZERO;
        
        BigDecimal perNightRate = originalPaymentAmount.divide(BigDecimal.valueOf(totalNights), 2, RoundingMode.HALF_UP);
        BigDecimal baseRefund = perNightRate.multiply(BigDecimal.valueOf(unusedNights));
        return baseRefund.multiply(new BigDecimal("0.85"));
    }

    private String generateRefundDetails(String refundType, LocalDate date, BigDecimal refundAmount) {
        switch (refundType) {
            case "Partial Refund":
                return generatePartialRefundDetails(date, refundAmount);
            case "Cancellation":
                return generateCancellationRefundDetails(date, refundAmount);
            case "Early Departure":
                return generateEarlyDepartureDetails(date, refundAmount);
            default:
                return String.format("Full refund of original payment: \u20B9%.2f", refundAmount.doubleValue());
        }
    }

    private String generatePartialRefundDetails(LocalDate departureDate, BigDecimal refundAmount) {
        long totalNights = ChronoUnit.DAYS.between(checkInCtx, checkOutCtx);
        long nightsUsed = ChronoUnit.DAYS.between(checkInCtx, departureDate);
        long unusedNights = Math.max(0, totalNights - nightsUsed);
        
        return String.format("Partial Refund Details:\n" +
                "Total nights booked: %d\n" +
                "Nights used: %d\n" +
                "Unused nights: %d\n" +
                "Refund amount (90%% of unused nights): \u20B9%.2f",
                totalNights, nightsUsed, unusedNights, refundAmount.doubleValue());
    }

    private String generateCancellationRefundDetails(LocalDate cancellationDate, BigDecimal refundAmount) {
        long daysUntilCheckIn = ChronoUnit.DAYS.between(cancellationDate, checkInCtx);
        double percentage = refundAmount.divide(originalPaymentAmount, 4, RoundingMode.HALF_UP)
                                       .multiply(new BigDecimal("100")).doubleValue();
        
        return String.format("Cancellation Refund Details:\n" +
                "Days until check-in: %d\n" +
                "Refund percentage: %.0f%%\n" +
                "Original payment: \u20B9%.2f\n" +
                "Refund amount: \u20B9%.2f",
                daysUntilCheckIn, percentage, originalPaymentAmount.doubleValue(), refundAmount.doubleValue());
    }

    private String generateEarlyDepartureDetails(LocalDate departureDate, BigDecimal refundAmount) {
        long totalNights = ChronoUnit.DAYS.between(checkInCtx, checkOutCtx);
        long nightsUsed = ChronoUnit.DAYS.between(checkInCtx, departureDate);
        long unusedNights = Math.max(0, totalNights - nightsUsed);
        
        return String.format("Early Departure Refund Details:\n" +
                "Original stay: %s to %s (%d nights)\n" +
                "Actual departure: %s (%d nights used)\n" +
                "Unused nights: %d\n" +
                "Refund amount (85%% of unused nights): \u20B9%.2f",
                checkInCtx.format(DATE_FORMATTER),
                checkOutCtx.format(DATE_FORMATTER),
                totalNights,
                departureDate.format(DATE_FORMATTER),
                nightsUsed, unusedNights, refundAmount.doubleValue());
    }

    private void updateRefundCalculationDisplay() {
        // Optional: Add real-time validation if needed
    }

    // ============ DATABASE HELPER METHODS ============

    private Long insertGuest(Connection conn, String name, String phone, String email, String address, String gst) throws SQLException {
        String sql = "INSERT INTO guests (name, phone, email, address, gst_number) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nullSafe(name));
            ps.setString(2, nullSafe(phone));
            ps.setString(3, nullSafe(email));
            ps.setString(4, nullSafe(address));
            ps.setString(5, nullSafe(gst));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : null;
            }
        }
    }

    private void updateGuestIfChanged(Connection conn, Long guestId, String name, String phone, String email, String address, String gst) throws SQLException {
        String sql = "UPDATE guests SET name=?, phone=?, email=?, address=?, gst_number=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nullSafe(name));
            ps.setString(2, nullSafe(phone));
            ps.setString(3, nullSafe(email));
            ps.setString(4, nullSafe(address));
            ps.setString(5, nullSafe(gst));
            ps.setLong(6, guestId);
            ps.executeUpdate();
        }
    }

    private Long insertCompleteBooking(Connection conn, Long reservationId, Long guestId, String roomNo,
            LocalDate ci, LocalDate co, BigDecimal totalAmount, BigDecimal advancePaid,
            String paymentStatus, String status) throws SQLException {

        String sql = """
            INSERT INTO bookings (
            reservation_id, guest_id, room_no, check_in_date, check_out_date, 
            total_amount, advance_paid, payment_status, status, 
            nationality, rate_per_night, document_link, gst_rate, 
            base_amount, gst_amount, gst_included
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int idx = 1;
            ps.setObject(idx++, reservationId);
            ps.setLong(idx++, guestId);
            ps.setString(idx++, roomNo);
            ps.setDate(idx++, Date.valueOf(ci));
            ps.setDate(idx++, Date.valueOf(co));
            ps.setBigDecimal(idx++, totalAmount);
            ps.setBigDecimal(idx++, advancePaid);
            ps.setString(idx++, paymentStatus);
            ps.setString(idx++, status);
            ps.setString(idx++, nationalityCtx);
            ps.setBigDecimal(idx++, ratePerNightCtx);
            ps.setString(idx++, documentPathCtx);
            ps.setBigDecimal(idx++, gstRateCtx);
            ps.setBigDecimal(idx++, baseAmountCtx);
            ps.setBigDecimal(idx++, gstAmountCtx);
            ps.setBoolean(idx++, gstIncludedCtx);

            int rowsAffected = ps.executeUpdate();
            System.out.println("INSERT booking - rows affected: " + rowsAffected);

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    System.out.println("Generated booking ID: " + id);
                    return id;
                }
            }
        }
        return null;
    }

    private void updateReservationPaymentStatus(Connection conn, Long reservationId, BigDecimal newPaymentAmount) throws SQLException {
        String selectSql = "SELECT COALESCE(advance_paid, 0) as current_advance FROM reservations WHERE id = ?";
        BigDecimal currentAdvance = BigDecimal.ZERO;
        
        try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
            selectPs.setLong(1, reservationId);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    currentAdvance = rs.getBigDecimal("current_advance");
                }
            }
        }
        
        BigDecimal newTotalAdvance = currentAdvance.add(newPaymentAmount);
        String updateSql = "UPDATE reservations SET advance_paid = ?, payment_status = ? WHERE id = ?";
        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
            updatePs.setBigDecimal(1, newTotalAdvance);
            updatePs.setString(2, newTotalAdvance.compareTo(BigDecimal.ZERO) > 0 ? "Partial" : "Pending");
            updatePs.setLong(3, reservationId);
            updatePs.executeUpdate();
        }
    }

    private void insertReservationPayment(Connection conn, long reservationId, BigDecimal amount, String method) throws SQLException {
        String sql = "INSERT INTO reservation_payments (reservation_id, amount, method, notes) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method);
            ps.setString(4, "Total payment via " + method);
            ps.executeUpdate();
        }
    }

    private Booking fetchBooking(Connection conn, Long id) throws SQLException {
        String sql = "SELECT id, guest_id, room_no, check_in_date, check_out_date FROM bookings WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Booking b = new Booking();
                b.setId(rs.getLong("id"));
                b.setGuestId(rs.getLong("guest_id"));
                b.setRoomNo(rs.getString("room_no"));
                b.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                b.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                return b;
            }
        }
    }

    private void updateBookingAdvancePaid(Connection conn, Long bookingId, BigDecimal newPaymentAmount) throws SQLException {
        String selectSql = "SELECT COALESCE(advance_paid, 0) as current_advance FROM bookings WHERE id = ?";
        BigDecimal currentAdvance = BigDecimal.ZERO;
        
        try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
            selectPs.setLong(1, bookingId);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    currentAdvance = rs.getBigDecimal("current_advance");
                }
            }
        }
        
        BigDecimal newTotalAdvance = currentAdvance.add(newPaymentAmount);
        String updateSql = "UPDATE bookings SET advance_paid = ? WHERE id = ?";
        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
            updatePs.setBigDecimal(1, newTotalAdvance);
            updatePs.setLong(2, bookingId);
            updatePs.executeUpdate();
        }
    }

    private void updateInvoiceAfterPaymentWithAdditiveTotals(Connection conn, Long bookingId, BigDecimal newPaymentAmount) throws SQLException {
        String selectSql = """
            SELECT COALESCE(paid_amount, 0) as current_paid, 
                   COALESCE(total, 0) as invoice_total,
                   COALESCE(subtotal, 0) as current_subtotal,
                   COALESCE(gst, 0) as current_gst
            FROM invoices WHERE booking_id = ?
            """;
        
        BigDecimal currentPaidAmount = BigDecimal.ZERO;
        BigDecimal invoiceTotal = BigDecimal.ZERO;
        BigDecimal currentSubtotal = BigDecimal.ZERO;
        BigDecimal currentGst = BigDecimal.ZERO;
        
        try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
            selectPs.setLong(1, bookingId);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    currentPaidAmount = rs.getBigDecimal("current_paid");
                    invoiceTotal = rs.getBigDecimal("invoice_total");
                    currentSubtotal = rs.getBigDecimal("current_subtotal");
                    currentGst = rs.getBigDecimal("current_gst");
                }
            }
        }
        
        BigDecimal newPaidAmount = currentPaidAmount.add(newPaymentAmount);
        BigDecimal subtotal = calculatedSubtotal > 0 ? bd(calculatedSubtotal) : currentSubtotal;
        BigDecimal gst = calculatedGst > 0 ? bd(calculatedGst) : currentGst;
        BigDecimal total = subtotal.add(gst);
        BigDecimal dueAmount = total.subtract(newPaidAmount);
        if (dueAmount.compareTo(BigDecimal.ZERO) < 0) dueAmount = BigDecimal.ZERO;
        
        String updateSql = """
            UPDATE invoices 
            SET subtotal = ?, gst = ?, total = ?, paid_amount = ?, due_amount = ?
            WHERE booking_id = ?
            """;
        
        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
            updatePs.setBigDecimal(1, subtotal);
            updatePs.setBigDecimal(2, gst);
            updatePs.setBigDecimal(3, total);
            updatePs.setBigDecimal(4, newPaidAmount);
            updatePs.setBigDecimal(5, dueAmount);
            updatePs.setLong(6, bookingId);
            updatePs.executeUpdate();
        }
    }

    private void upsertInvoice(Connection conn, Long bookingId, Long guestId,
                              double subtotal, double gst, double total, double paidAmount) throws SQLException {
        Long existingId = findInvoiceIdByBooking(conn, bookingId);
        double due = Math.max(0.0, total - paidAmount);

        if (existingId == null) {
            String invoiceNo = "INV-" + LocalDate.now().getYear() + "-" + (1000 + new Random().nextInt(9000));
            String sql = "INSERT INTO invoices (booking_id, guest_id, invoice_number, invoice_date, subtotal, gst, total, paid_amount, due_amount) " +
                         "VALUES (?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, bookingId);
                ps.setLong(2, guestId);
                ps.setString(3, invoiceNo);
                ps.setDate(4, Date.valueOf(LocalDate.now()));
                ps.setBigDecimal(5, bd(subtotal));
                ps.setBigDecimal(6, bd(gst));
                ps.setBigDecimal(7, bd(total));
                ps.setBigDecimal(8, bd(paidAmount));
                ps.setBigDecimal(9, bd(due));
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE invoices SET invoice_date=?, subtotal=?, gst=?, total=?, paid_amount=?, due_amount=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(LocalDate.now()));
                ps.setBigDecimal(2, bd(subtotal));
                ps.setBigDecimal(3, bd(gst));
                ps.setBigDecimal(4, bd(total));
                ps.setBigDecimal(5, bd(paidAmount));
                ps.setBigDecimal(6, bd(due));
                ps.setLong(7, existingId);
                ps.executeUpdate();
            }
        }
    }

    private Long findInvoiceIdByBooking(Connection conn, long bookingId) throws SQLException {
        String sql = "SELECT id FROM invoices WHERE booking_id=? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private void insertPayment(Connection conn, long bookingId, long guestId, BigDecimal amount, String method, String transactionId) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, guest_id, amount, method, transaction_id, payer, notes, payment_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setLong(2, guestId);
            ps.setBigDecimal(3, amount);
            ps.setString(4, method);
            ps.setString(5, transactionId);
            ps.setString(6, guestNameTextField != null && guestNameTextField.getText() != null ? guestNameTextField.getText().trim() : null);
            ps.setString(7, "Total payment via " + method);
            ps.executeUpdate();
        }
    }

    private void updatePaymentStatusFromInvoices(Connection conn, long bookingId) throws SQLException {
        String sqlInvoice = "SELECT total, paid_amount FROM invoices WHERE booking_id=? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlInvoice)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    BigDecimal paid = rs.getBigDecimal("paid_amount");
                    String status;
                    if (paid == null || paid.compareTo(BigDecimal.ZERO) == 0) status = "Pending";
                    else if (total != null && paid.compareTo(total) >= 0) status = "Paid";
                    else status = "Partial";

                    String sqlUpd = "UPDATE bookings SET payment_status=? WHERE id=?";
                    try (PreparedStatement up = conn.prepareStatement(sqlUpd)) {
                        up.setString(1, status);
                        up.setLong(2, bookingId);
                        up.executeUpdate();
                    }
                }
            }
        }
    }

    private void updateRoomStatusIfCheckedIn(Connection conn, String roomNo, String newStatusIfChange, long bookingId) throws SQLException {
        Booking b = fetchBooking(conn, bookingId);
        if (b == null) return;

        LocalDate today = LocalDate.now();
        if (!today.isBefore(b.getCheckInDate()) && today.isBefore(b.getCheckOutDate())) {
            updateBookingStatus(conn, bookingId, "Checked-in");
            updateRoomStatus(conn, roomNo, "Occupied");
        } else if (newStatusIfChange != null) {
            updateBookingStatus(conn, bookingId, newStatusIfChange);
        }
    }

    private void updateRoomStatusBasedOnCheckIn(Connection conn, String roomNo, Long bookingId) throws SQLException {
        if (!LocalDate.now().isBefore(checkInCtx)) {
            updateBookingStatus(conn, bookingId, "Checked-in");
            updateRoomStatus(conn, roomNo, "Occupied");
        } else {
            updateRoomStatus(conn, roomNo, "Reserved");
        }
    }

    private void updateRoomStatus(Connection conn, String roomNo, String status) throws SQLException {
        String sql = "UPDATE rooms SET status=? WHERE room_no=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, roomNo);
            ps.executeUpdate();
        }
    }

    private void updateBookingStatus(Connection conn, Long bookingId, String status) throws SQLException {
        String sql = "UPDATE bookings SET status=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, bookingId);
            ps.executeUpdate();
        }
    }

    private void updateReservationStatus(Connection conn, Long reservationId, String status) throws SQLException {
        String sql = "UPDATE reservations SET status=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, reservationId);
            ps.executeUpdate();
        }
    }

    private Long findActiveReservationForRoomAndDates(Connection conn, String roomNo, LocalDate ci, LocalDate co) throws SQLException {
        String sql = """
            SELECT id FROM reservations
            WHERE room_no=? AND status IN ('Confirmed','Pending')
              AND (? < end_date) AND (? > start_date)
            ORDER BY id DESC LIMIT 1
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNo);
            ps.setDate(2, Date.valueOf(co));
            ps.setDate(3, Date.valueOf(ci));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private void attachReservationToBooking(Connection conn, Long bookingId, Long reservationId) throws SQLException {
        String sql = "UPDATE bookings SET reservation_id=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            ps.setLong(2, bookingId);
            ps.executeUpdate();
        }
    }

    private void markReservationArrived(Connection conn, Long reservationId) throws SQLException {
        String sql = "UPDATE reservations SET status='Completed' WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            ps.executeUpdate();
        }
    }

    private void ensureRoomAvailableOrThrow(Connection conn, String roomNo, LocalDate ci, LocalDate co, Long ignoreBookingId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM bookings
            WHERE room_no = ?
              AND status IN ('Checked-in','Confirmed','Reserved')
              AND (? < check_out_date) AND (? > check_in_date)
            %s
            """.formatted(ignoreBookingId == null ? "" : "AND id<>?");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, roomNo);
            ps.setDate(idx++, Date.valueOf(co));
            ps.setDate(idx++, Date.valueOf(ci));
            if (ignoreBookingId != null) ps.setLong(idx++, ignoreBookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Room " + roomNo + " is no longer available for the selected dates.");
                }
            }
        }
    }

    private void ensureRoomAvailableForReservation(Connection conn, String roomNo, LocalDate checkIn, LocalDate checkOut, Long ignoreReservationId) throws SQLException {
        String bookingQuery = """
            SELECT COUNT(*) FROM bookings
            WHERE room_no = ?
              AND status IN ('Checked-in','Confirmed','Reserved')
              AND (? < check_out_date) AND (? > check_in_date)
            """;
        try (PreparedStatement ps = conn.prepareStatement(bookingQuery)) {
            ps.setString(1, roomNo);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Room " + roomNo + " has existing booking conflicts for the selected dates.");
                }
            }
        }

        String reservationQuery = """
            SELECT COUNT(*) FROM reservations
            WHERE room_no = ?
              AND status IN ('Confirmed','Pending')
              AND (? < end_date) AND (? > start_date)
            %s
            """.formatted(ignoreReservationId == null ? "" : "AND id <> ?");
        try (PreparedStatement ps = conn.prepareStatement(reservationQuery)) {
            int idx = 1;
            ps.setString(idx++, roomNo);
            ps.setDate(idx++, Date.valueOf(checkOut));
            ps.setDate(idx++, Date.valueOf(checkIn));
            if (ignoreReservationId != null) ps.setLong(idx++, ignoreReservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Room " + roomNo + " has existing reservation conflicts for the selected dates.");
                }
            }
        }
    }

    private void validateDatesOrThrow(LocalDate ci, LocalDate co) throws SQLException {
        if (ci == null || co == null || !co.isAfter(ci)) {
            throw new SQLException("Invalid dates: Check-out must be after Check-in.");
        }
    }

    private String derivePaymentStatus(BigDecimal total, BigDecimal paid) {
        if (paid == null || paid.compareTo(BigDecimal.ZERO) == 0) return "Pending";
        if (total != null && paid.compareTo(total) >= 0) return "Paid";
        return "Partial";
    }

    // ============ UTILITY METHODS ============

    private void setBasicContext(Long selectedGuestId, String name, String phone, String email, 
                               String address, String gst, String roomNo, String roomType,
                               LocalDate checkIn, LocalDate checkOut, BigDecimal ratePerNight, 
                               BigDecimal totalAmount, BigDecimal advancePaid) {
        this.selectedGuestId = selectedGuestId;
        this.guestNameCtx = name;
        this.guestPhoneCtx = phone;
        this.guestEmailCtx = email;
        this.guestAddressCtx = address;
        this.guestGstCtx = gst;
        this.roomNoCtx = roomNo;
        this.roomTypeCtx = roomType;
        this.checkInCtx = checkIn;
        this.checkOutCtx = checkOut;
        this.ratePerNightCtx = safeBigDecimal(ratePerNight);
        this.totalAmountCtx = safeBigDecimal(totalAmount);
        this.advancePaidCtx = safeBigDecimal(advancePaid);
    }

    private void populateUIFromReservation(ReservationStub reservation) {
        try {
            setTextField(guestNameTextField, reservation.getGuestName());
            setLabel(roomNumberLabel, reservation.getRoomNo());
            setLabel(roomTypeLabel, reservation.getRoomType());
            
            if (checkInDatePicker != null) checkInDatePicker.setValue(reservation.getCheckInDate());
            if (checkOutDatePicker != null) checkOutDatePicker.setValue(reservation.getCheckOutDate());

            long nights = calculateNights(reservation.getCheckInDate(), reservation.getCheckOutDate());
            setLabel(numNightsLabel, String.valueOf(nights));
            
            checkInCtx = reservation.getCheckInDate();
            checkOutCtx = reservation.getCheckOutDate();
            roomNoCtx = reservation.getRoomNo();
            
        } catch (Exception e) {
            logError("Error populating UI from reservation", e);
        }
    }

    private void showPaymentDetailsPane(String method) {
        hideAllPaymentDetailsPanes();
        VBox pane = switch (method) {
            case "Cash" -> cashPane;
            case "UPI" -> upiPane;
            case "Card" -> cardPane;
            case "Netbanking" -> netbankingPane;
            default -> null;
        };
        
        if (pane != null) {
            pane.setVisible(true);
            pane.setManaged(true);
        }
    }

    private void hideAllPaymentDetailsPanes() {
        setVisibility(cashPane, false);
        setVisibility(upiPane, false);
        setVisibility(cardPane, false);
        setVisibility(netbankingPane, false);
    }

    @FXML
    private void handleApplyDiscount() {
        if (discountTextField == null) return;
        
        String discountText = discountTextField.getText();
        if (isBlankString(discountText)) {
            currentDiscount = 0.0;
        } else {
            try {
                double discount = Double.parseDouble(discountText.trim());
                if (discount < 0) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Discount", "Discount cannot be negative.");
                    return;
                }
                currentDiscount = discount;
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Enter a valid number for discount.");
                return;
            }
        }

        try {
            if (bookingId != null) {
                populateUIForExistingBooking();
            } else {
                populateUI();
            }
            showAlert(Alert.AlertType.INFORMATION, "Discount Applied", "Discount updated.");
        } catch (Exception e) {
            logError("Could not recalculate totals", e);
            showAlert(Alert.AlertType.ERROR, "Recalculate Error", "Could not recalculate totals.");
        }
    }

    @FXML
    private void handleCancelPayment() {
        paymentSuccessful = false;
        closeWindowIfAny();
    }

    private void showSuccessAndInvoice(String paymentMethod, String transactionId) {
        showAlert(Alert.AlertType.INFORMATION, "Payment Success", "Payment recorded successfully.");
        openInvoiceViewWithPaymentDetails(paymentMethod, transactionId);
    }

    private void openInvoiceViewWithPaymentDetails(String paymentMethod, String transactionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/InvoiceView.fxml"));
            Parent root = loader.load();
            InvoiceViewController controller = loader.getController();

            BookingData completeBookingData = prepareCompleteBookingDataWithPaymentDetails(paymentMethod, transactionId);
            controller.setBookingData(completeBookingData);
            controller.populateInvoice();

            Stage invoiceStage = new Stage();
            invoiceStage.setTitle("Invoice - Payment Completed");
            invoiceStage.setScene(new Scene(root));
            invoiceStage.setResizable(true);
            invoiceStage.initModality(Modality.NONE);
            invoiceStage.show();
        } catch (Exception ex) {
            logError("Could not open invoice view", ex);
            showAlert(Alert.AlertType.ERROR, "Invoice Error", "Could not open invoice view: " + ex.getMessage());
        }
    }

    private BookingData prepareCompleteBookingDataWithPaymentDetails(String paymentMethod, String transactionId) {
        BookingData bd = mergeBookingData(null);
        bd.setPaymentMethod(paymentMethod);
        bd.setTransactionId(transactionId);
        bd.setPaymentStatus("Paid");
        bd.setPaymentDate(LocalDate.now());

        double subtotal, gst, totalPayable;
        
        if (isServicePaymentMode) {
            // Use service payment calculations
            subtotal = baseAmountCtx != null ? baseAmountCtx.doubleValue() : calculatedSubtotal;
            gst = gstAmountCtx != null ? gstAmountCtx.doubleValue() : calculatedGst;
        } else {
            // Use regular calculations
            subtotal = calculatedSubtotal > 0 ? calculatedSubtotal : Math.max(0.0, bd.getSubtotal());
            gst = calculatedGst > 0 ? calculatedGst : Math.max(0.0, bd.getGst());
        }
        
        totalPayable = subtotal + gst;
        
        String amountText = amountPayingField != null ? amountPayingField.getText() : "0";
        double totalPaid = parseCurrencyToBD(amountText).doubleValue();

        bd.setSubtotal(round2(subtotal));
        bd.setGst(round2(gst));
        bd.setTotalPayable(round2(totalPayable));
        bd.setAdvancePaid(round2(totalPaid));
        bd.setBalanceDue(round2(Math.max(0.0, totalPayable - totalPaid)));

        if (currentDiscount > 0) bd.setDiscount(round2(currentDiscount));
        bd.setServiceCharges(round2(calculatedServiceCharges));
        bd.setCgst(round2(gst / 2.0));
        bd.setSgst(round2(gst / 2.0));

        return bd;
    }

    private BookingData mergeBookingData(BookingData seed) {
        BookingData bd = (seed != null) ? seed : (this.bookingData != null ? this.bookingData : new BookingData());

        if (isBlankString(bd.getGuestName())) bd.setGuestName(guestNameCtx);
        if (isBlankString(bd.getGuestAddress())) bd.setGuestAddress(guestAddressCtx);
        if (isBlankString(bd.getMobileNumber())) bd.setMobileNumber(guestPhoneCtx);
        if (isBlankString(bd.getRoomNumber())) bd.setRoomNumber(roomNoCtx);
        if (isBlankString(bd.getRoomType())) bd.setRoomType(roomTypeCtx);
        if (bd.getCheckInDate() == null) bd.setCheckInDate(checkInCtx);
        if (bd.getCheckOutDate() == null) bd.setCheckOutDate(checkOutCtx);

        long nights = calculateNights(bd.getCheckInDate(), bd.getCheckOutDate());
        bd.setNumberOfNights((int) nights);

        double roomCharges = calculatedRoomCharges > 0 ? calculatedRoomCharges : Math.max(0.0, bd.getRoomCharges());
        double service = calculatedServiceCharges > 0 ? calculatedServiceCharges : Math.max(0.0, bd.getServiceCharges());
        double discount = currentDiscount > 0 ? currentDiscount : Math.max(0.0, bd.getDiscount());
        double subtotal = Math.max(0.0, roomCharges + service - discount);

        bd.setRoomCharges(roomCharges);
        bd.setServiceCharges(service);
        bd.setDiscount(discount);
        bd.setSubtotal(round2(subtotal));

        double gst = calculatedGst > 0 ? calculatedGst : round2(subtotal * 0.18);
        bd.setGst(gst);
        double totalPayable = round2(subtotal + gst);
        bd.setTotalPayable(totalPayable);

        String amountText = amountPayingField != null ? amountPayingField.getText() : "0";
        double advance = parseCurrencyToBD(amountText).doubleValue();
        if (advance <= 0 && bd.getAdvancePaid() > 0) advance = bd.getAdvancePaid();

        bd.setAdvancePaid(round2(advance));
        bd.setBalanceDue(round2(Math.max(0.0, totalPayable - advance)));

        if (isBlankString(bd.getInvoiceNumber())) {
            bd.setInvoiceNumber("INV-" + LocalDate.now().getYear() + "-" + (1000 + new Random().nextInt(9000)));
        }
        if (bd.getInvoiceDate() == null) bd.setInvoiceDate(LocalDate.now());

        String method = getSelectedMethodOrNull();
        if (isBlankString(bd.getPaymentMethod())) bd.setPaymentMethod(method == null ? "N/A" : method);
        if (isBlankString(bd.getTransactionId())) bd.setTransactionId("PENDING");

        return bd;
    }

    public void setServiceItems(List<ServiceLineItem> items) {
        serviceItemsObservableList.clear();
        serviceItemsObservableList.addAll(items);
        updateServiceTotal();
    }

    // ============ STATIC METHODS ============

    public static void openRefundWindow(Long bookingId, Long paymentId, BigDecimal originalAmount, Window parentWindow) {
        try {
            FXMLLoader loader = new FXMLLoader(PaymentController.class.getResource("/fxml1/Payment.fxml"));
            Parent root = loader.load();

            PaymentController controller = loader.getController();
            controller.enableRefundMode(bookingId, paymentId, originalAmount);

            Stage refundStage = new Stage();
            refundStage.setTitle("Payment Refund - Hotel Management");
            refundStage.setScene(new Scene(root));
            refundStage.initModality(Modality.WINDOW_MODAL);

            if (parentWindow != null) refundStage.initOwner(parentWindow);
            refundStage.showAndWait();

        } catch (IOException e) {
            handleWindowOpenError(e);
        }
    }

    public static void openRefundWindowWithReservation(ReservationStub reservation, Long paymentId, 
                                                       BigDecimal refundAmount, Window parentWindow) {
        try {
            FXMLLoader loader = new FXMLLoader(PaymentController.class.getResource("/fxml1/Payment.fxml"));
            Parent root = loader.load();
            
            PaymentController controller = loader.getController();
            controller.enableRefundModeWithReservation(reservation, paymentId, refundAmount);
            
            Stage refundStage = new Stage();
            refundStage.setTitle("Refund Payment - Reservation Cancellation");
            refundStage.setScene(new Scene(root));
            refundStage.initModality(Modality.WINDOW_MODAL);
            
            if (parentWindow != null) refundStage.initOwner(parentWindow);
            refundStage.showAndWait();
            
        } catch (IOException e) {
            handleWindowOpenError(e);
        }
    }

    private static void handleWindowOpenError(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText("Could not open window: " + e.getMessage());
        alert.showAndWait();
    }

    // ============ HELPER METHODS ============

    private String getSelectedMethodOrNull() {
        if (paymentMethodToggleGroup == null || paymentMethodToggleGroup.getSelectedToggle() == null) return null;
        return (String) paymentMethodToggleGroup.getSelectedToggle().getUserData();
    }

    private String buildTransactionId(String method) {
        String base = "TRX-" + System.currentTimeMillis() + "-" + (100 + new Random().nextInt(900));
        return switch (method) {
            case "UPI" -> (upiIdField != null && !isBlank(upiIdField)) ? upiIdField.getText().trim() : base;
            case "Card" -> {
                String last4 = (cardNumberField != null && !isBlank(cardNumberField) && cardNumberField.getText().length() >= 4)
                        ? cardNumberField.getText().substring(cardNumberField.getText().length() - 4)
                        : "XXXX";
                yield "CARD-XXXX-XXXX-XXXX-" + last4 + "-" + base;
            }
            case "Netbanking" -> (accountNumberField != null && !isBlank(accountNumberField))
                    ? "NB-" + accountNumberField.getText().trim() + "-" + base
                    : "NB-" + base;
            default -> base;
        };
    }

    private void showRefundSuccess(BigDecimal refundAmount, Long refundId) {
        showAlert(Alert.AlertType.INFORMATION, "Refund Processed", 
                String.format("Refund of \u20B9%.2f has been processed successfully.\nRefund ID: %d", 
                            refundAmount.doubleValue(), refundId));
    }

    private void handlePaymentError(Exception ex) {
        logError("Payment failed", ex);
        showAlert(Alert.AlertType.ERROR, "Payment Error", "Payment failed: " + ex.getMessage());
    }

    private void handleDatabaseError(SQLException e) {
        logError("Database error", e);
        showAlert(Alert.AlertType.ERROR, "Database Error", "Could not process payment: " + e.getMessage());
    }

    private void closeWindowIfAny() {
        try {
            Stage stage = (Stage) (guestNameTextField != null ? guestNameTextField.getScene().getWindow()
                    : roomNumberLabel != null ? roomNumberLabel.getScene().getWindow()
                    : totalAmountLabel != null ? totalAmountLabel.getScene().getWindow()
                    : null);
            if (stage != null) stage.close();
        } catch (Exception ignored) {}
    }

    // ============ FORMATTING AND VALIDATION METHODS ============

    private boolean isBlank(TextField tf) { 
        return tf == null || tf.getText() == null || tf.getText().isBlank(); 
    }
    
    private boolean isBlankString(String s) { 
        return s == null || s.trim().isEmpty(); 
    }
    
    private String formatCurrency(double v) { 
        return String.format("\u20B9%,.2f", v); 
    }
    
    private String formatDecimal(double v) { 
        return String.format("%.2f", v); 
    }
    
    private String formatPercentage(double v) { 
        return String.format("%.1f%%", v); 
    }
    
    private double round2(double x) { 
        return BigDecimal.valueOf(x).setScale(2, RoundingMode.HALF_UP).doubleValue(); 
    }
    
    private BigDecimal bd(double v) { 
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP); 
    }
    
    private BigDecimal safeBigDecimal(BigDecimal v) { 
        return v == null ? ZERO : v.setScale(2, RoundingMode.HALF_UP); 
    }
    
    private BigDecimal safeBigDecimal(BigDecimal v, BigDecimal defaultValue) { 
        return v == null ? defaultValue : v.setScale(2, RoundingMode.HALF_UP); 
    }
    
    private String safeString(String s, String defaultValue) { 
        return (s == null || s.isBlank()) ? defaultValue : s.trim(); 
    }
    
    private String nullSafe(String s) { 
        return (s == null || s.isBlank()) ? null : s.trim(); 
    }

    private BigDecimal parseCurrencyToBD(String text) {
        if (text == null) return ZERO;
        try {
            String clean = text.replace("\u20B9", "").replace(",", "").trim();
            return clean.isBlank() ? ZERO : new BigDecimal(clean);
        } catch (Exception e) {
            return ZERO;
        }
    }

    private long calculateNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) return 0;
        return Math.max(0, ChronoUnit.DAYS.between(checkIn, checkOut));
    }

    private void setTextField(TextField field, String value) {
        if (field != null && value != null) field.setText(value);
    }

    private void setLabel(Label label, String value) {
        if (label != null && value != null) label.setText(value);
    }

    private void setVisibility(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
    
 // Add these methods to your PaymentController class

    @FXML
    private void handleViewInvoice(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();
        openInvoicePreview(mergeBookingData(null), owner);
    }

    private void openInvoicePreview(BookingData data, Window owner) {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml1/InvoiceView.fxml"));
            Parent root = fx.load();
            InvoiceViewController c = fx.getController();
            c.setBookingData(data);
            c.populateInvoice();

            Stage stage = new Stage();
            stage.setTitle("Invoice Preview");
            if (owner != null) stage.initOwner(owner);
            stage.initModality(Modality.NONE);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Invoice Error", "Could not open invoice: " + ex.getMessage());
        }
    }

//    private BookingData mergeBookingData(BookingData seed) {
//        BookingData bd = (seed != null) ? seed : (this.bookingData != null ? this.bookingData : new BookingData());
//
//        if (isBlankString(bd.getGuestName())) bd.setGuestName(guestNameCtx);
//        if (isBlankString(bd.getGuestAddress())) bd.setGuestAddress(guestAddressCtx);
//        if (isBlankString(bd.getMobileNumber())) bd.setMobileNumber(guestPhoneCtx);
//        if (isBlankString(bd.getRoomNumber())) bd.setRoomNumber(roomNoCtx);
//        if (isBlankString(bd.getRoomType())) bd.setRoomType(roomTypeCtx);
//        if (bd.getCheckInDate() == null) bd.setCheckInDate(checkInCtx);
//        if (bd.getCheckOutDate() == null) bd.setCheckOutDate(checkOutCtx);
//
//        long nights = calculateNights(bd.getCheckInDate(), bd.getCheckOutDate());
//        bd.setNumberOfNights((int) nights);
//
//        double roomCharges = calculatedRoomCharges > 0 ? calculatedRoomCharges : Math.max(0.0, bd.getRoomCharges());
//        double service = calculatedServiceCharges > 0 ? calculatedServiceCharges : Math.max(0.0, bd.getServiceCharges());
//        double discount = currentDiscount > 0 ? currentDiscount : Math.max(0.0, bd.getDiscount());
//        double subtotal = Math.max(0.0, roomCharges + service - discount);
//
//        bd.setRoomCharges(roomCharges);
//        bd.setServiceCharges(service);
//        bd.setDiscount(discount);
//        bd.setSubtotal(round2(subtotal));
//
//        double gst = calculatedGst > 0 ? calculatedGst : round2(subtotal * 0.18);
//        bd.setGst(gst);
//        double totalPayable = round2(subtotal + gst);
//        bd.setTotalPayable(totalPayable);
//
//        String amountText = amountPayingField != null ? amountPayingField.getText() : "0";
//        double advance = parseCurrencyToBD(amountText).doubleValue();
//        if (advance <= 0 && bd.getAdvancePaid() > 0) advance = bd.getAdvancePaid();
//
//        bd.setAdvancePaid(round2(advance));
//        bd.setBalanceDue(round2(Math.max(0.0, totalPayable - advance)));
//
//        if (isBlankString(bd.getInvoiceNumber())) {
//            bd.setInvoiceNumber("INV-" + LocalDate.now().getYear() + "-" + (1000 + new Random().nextInt(9000)));
//        }
//        if (bd.getInvoiceDate() == null) bd.setInvoiceDate(LocalDate.now());
//
//        String method = getSelectedMethodOrNull();
//        if (isBlankString(bd.getPaymentMethod())) bd.setPaymentMethod(method == null ? "N/A" : method);
//        if (isBlankString(bd.getTransactionId())) bd.setTransactionId("PENDING");
//
//        return bd;
//    }
    
 // Add these instance variables
  

    // Update payment processing to handle service payments
    private void processPayment(Connection conn, BigDecimal amount, String method, String transactionId) throws SQLException {
        Long effectiveGuestId = getEffectiveGuestId(conn);

        if (isReservationPayment) {
            handleReservationPayment(conn, effectiveGuestId, amount, method, transactionId);
        } else if (bookingId != null) {
            if (isServicePaymentMode) {
                handleServicePayment(conn, effectiveGuestId, amount, method, transactionId);
            } else {
                handleExistingBookingPayment(conn, effectiveGuestId, amount, method, transactionId);
            }
        } else {
            handleNewBookingPayment(conn, effectiveGuestId, amount, method, transactionId);
        }
    }

    // Add service payment handler
    private void handleServicePayment(Connection conn, Long guestId, BigDecimal amount, String method, String transactionId) throws SQLException {
        // For service payments, record payment against existing booking
        insertPayment(conn, bookingId, guestId, amount, method, transactionId);
        
        // Update invoice with service charges
        updateInvoiceForServicePayment(conn, bookingId, amount);
        
        // Update payment status
        updatePaymentStatusFromInvoices(conn, bookingId);
        
        System.out.println("Service payment processed: Booking ID " + bookingId + 
                          ", Amount: " + amount + ", Method: " + method);
    }

    // Add invoice update for service payments
    private void updateInvoiceForServicePayment(Connection conn, Long bookingId, BigDecimal paymentAmount) throws SQLException {
        // Check if invoice exists
        Long invoiceId = findInvoiceIdByBooking(conn, bookingId);
        
        BigDecimal serviceSubtotal = baseAmountCtx != null ? baseAmountCtx : bd(calculatedSubtotal);
        BigDecimal serviceGst = gstAmountCtx != null ? gstAmountCtx : bd(calculatedGst);
        BigDecimal serviceTotal = serviceSubtotal.add(serviceGst);
        
        if (invoiceId == null) {
            // Create new invoice for service payment
            String invoiceNo = "INV-" + LocalDate.now().getYear() + "-" + (1000 + new Random().nextInt(9000));
            String sql = "INSERT INTO invoices (booking_id, guest_id, invoice_number, invoice_date, subtotal, gst, total, paid_amount, due_amount) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, bookingId);
                ps.setLong(2, selectedGuestId != null ? selectedGuestId : guestId);
                ps.setString(3, invoiceNo);
                ps.setDate(4, Date.valueOf(LocalDate.now()));
                ps.setBigDecimal(5, serviceSubtotal);
                ps.setBigDecimal(6, serviceGst);
                ps.setBigDecimal(7, serviceTotal);
                ps.setBigDecimal(8, paymentAmount);
                ps.setBigDecimal(9, serviceTotal.subtract(paymentAmount));
                ps.executeUpdate();
            }
        } else {
            // Update existing invoice with service charges
            String selectSql = "SELECT subtotal, gst, total, paid_amount FROM invoices WHERE id = ?";
            BigDecimal currentSubtotal = BigDecimal.ZERO;
            BigDecimal currentGst = BigDecimal.ZERO;
            BigDecimal currentTotal = BigDecimal.ZERO;
            BigDecimal currentPaid = BigDecimal.ZERO;
            
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setLong(1, invoiceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentSubtotal = safeBigDecimal(rs.getBigDecimal("subtotal"));
                        currentGst = safeBigDecimal(rs.getBigDecimal("gst"));
                        currentTotal = safeBigDecimal(rs.getBigDecimal("total"));
                        currentPaid = safeBigDecimal(rs.getBigDecimal("paid_amount"));
                    }
                }
            }
            
            // Add service charges to existing totals
            BigDecimal newSubtotal = currentSubtotal.add(serviceSubtotal);
            BigDecimal newGst = currentGst.add(serviceGst);
            BigDecimal newTotal = currentTotal.add(serviceTotal);
            BigDecimal newPaid = currentPaid.add(paymentAmount);
            BigDecimal newDue = newTotal.subtract(newPaid);
            
            if (newDue.compareTo(BigDecimal.ZERO) < 0) newDue = BigDecimal.ZERO;
            
            String updateSql = "UPDATE invoices SET subtotal = ?, gst = ?, total = ?, paid_amount = ?, due_amount = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setBigDecimal(1, newSubtotal);
                ps.setBigDecimal(2, newGst);
                ps.setBigDecimal(3, newTotal);
                ps.setBigDecimal(4, newPaid);
                ps.setBigDecimal(5, newDue);
                ps.setLong(6, invoiceId);
                ps.executeUpdate();
            }
        }
    }
    
 // Add these instance variables


    // Add this method to set service payment mode
    public void setServicePaymentMode(boolean isServicePayment, double serviceAmount) {
        this.isServicePaymentMode = isServicePayment;
        this.servicePaymentAmount = serviceAmount;
        
        // Don't update UI here - wait for initialize to complete
        // The UI will be updated in populateUI() which is called after FXML loading
    }

    // Remove the problematic updateUIForServicePayment method and replace with this:
    private void updateUIForServicePayment() {
        // Safe UI updates - check for null before accessing UI components
        if (roomChargesLabel != null) {
            roomChargesLabel.setText(formatCurrency(0.0));
        }
        
        // Update window title safely
        try {
            Stage stage = (Stage) (guestNameTextField != null ? guestNameTextField.getScene().getWindow() : null);
            if (stage != null && isServicePaymentMode) {
                stage.setTitle("Service Payment - " + (guestNameCtx != null ? guestNameCtx : "Guest"));
            }
        } catch (Exception e) {
            // Ignore stage title update if window is not available yet
            System.out.println("Could not update window title: " + e.getMessage());
        }
    }

    // Update the populateUI method to handle service payments


    // Add service payment financial calculations
    private void calculateAndDisplayServiceFinancials() {
        calculatedServiceCharges = servicePaymentAmount;
        calculatedRoomCharges = 0.0; // No room charges for service payments
        currentDiscount = 0.0; // No discount for service payments

        // Safe UI updates with null checks
        if (roomChargesLabel != null) {
            roomChargesLabel.setText(formatCurrency(calculatedRoomCharges));
        }
        if (serviceChargesLabel != null) {
            serviceChargesLabel.setText(formatCurrency(calculatedServiceCharges));
        }
        if (discountAppliedLabel != null) {
            discountAppliedLabel.setText(formatCurrency(currentDiscount));
        }

        calculatedSubtotal = Math.max(0.0, calculatedRoomCharges + calculatedServiceCharges - currentDiscount);
        if (subtotalLabel != null) {
            subtotalLabel.setText(formatCurrency(calculatedSubtotal));
        }

        // Use the GST result from context (already calculated in ServiceController)
        if (gstResultCtx != null) {
            calculatedGst = gstAmountCtx.doubleValue();
            if (gstLabel != null) {
                gstLabel.setText(formatCurrency(calculatedGst) + " @ " + formatPercentage(gstResultCtx.getGstRate()));
            }
            if (baseAmountLabel != null) {
                baseAmountLabel.setText(formatCurrency(baseAmountCtx.doubleValue()));
            }
            if (gstAmountLabel != null) {
                gstAmountLabel.setText(formatCurrency(gstAmountCtx.doubleValue()));
            }
            if (gstBreakdownLabel != null) {
                gstBreakdownLabel.setText(String.format("GST @ %s", formatPercentage(gstResultCtx.getGstRate())));
            }
        } else {
            // Fallback calculation
            calculateServiceGst();
        }
        
        double totalPayable = calculatedSubtotal + calculatedGst;
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(formatCurrency(totalPayable));
        }
        if (advancePaidLabel != null) {
            advancePaidLabel.setText(formatCurrency(advancePaidCtx.doubleValue()));
        }

        calculatedAmountDue = Math.max(0.0, totalPayable - advancePaidCtx.doubleValue());
        if (amountDueLabel != null) {
            amountDueLabel.setText(formatCurrency(calculatedAmountDue));
        }
        
        // Pre-fill payment amount for service payments
        if (amountPayingField != null && isServicePaymentMode) {
            amountPayingField.setText(formatDecimal(calculatedAmountDue));
        }
    }

    // Fallback GST calculation for services
    private void calculateServiceGst() {
        double gstRate = propertyReader.getGstRate();
        calculatedGst = calculatedSubtotal * (gstRate / 100);
        
        if (gstLabel != null) {
            gstLabel.setText(formatCurrency(calculatedGst));
        }
        if (baseAmountLabel != null) {
            baseAmountLabel.setText(formatCurrency(calculatedSubtotal));
        }
        if (gstAmountLabel != null) {
            gstAmountLabel.setText(formatCurrency(calculatedGst));
        }
        if (gstBreakdownLabel != null) {
            gstBreakdownLabel.setText(formatPercentage(gstRate));
        }
    }
 // Add this method to PaymentController class
    public void setGuestDetails(String name, String phone, String email, String address, String gst) {
        this.guestNameCtx = name;
        this.guestPhoneCtx = phone;
        this.guestEmailCtx = email;
        this.guestAddressCtx = address;
        this.guestGstCtx = gst;
        
        // Also update the UI field if available
        if (guestNameTextField != null) {
            guestNameTextField.setText(name != null ? name : "");
        }
    }
    
}