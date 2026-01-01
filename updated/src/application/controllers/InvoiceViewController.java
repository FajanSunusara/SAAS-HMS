package application.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import application.models.BookingData;
import application.models.ServiceItem1;
import application.models.TaxItem;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class InvoiceViewController implements Initializable {
    
    // ========== INNER CLASS: PrintSettings ==========
    /**
     * PrintSettings encapsulates all print configuration parameters
     * that can be controlled from the Settings page
     */
    public static class PrintSettings {
        private double width, height;
        private double marginTop, marginRight, marginBottom, marginLeft;
        private double paddingTop, paddingRight, paddingBottom, paddingLeft;

        public PrintSettings(double width, double height, 
                             double marginTop, double marginRight,
                             double marginBottom, double marginLeft,
                             double paddingTop, double paddingRight, 
                             double paddingBottom, double paddingLeft) {
            this.width = width;
            this.height = height;
            this.marginTop = marginTop;
            this.marginRight = marginRight;
            this.marginBottom = marginBottom;
            this.marginLeft = marginLeft;
            this.paddingTop = paddingTop;
            this.paddingRight = paddingRight;
            this.paddingBottom = paddingBottom;
            this.paddingLeft = paddingLeft;
        }

        // Getters
        public double getWidth() { return width; }
        public double getHeight() { return height; }
        public double getMarginTop() { return marginTop; }
        public double getMarginRight() { return marginRight; }
        public double getMarginBottom() { return marginBottom; }
        public double getMarginLeft() { return marginLeft; }
        
        public Insets toInsets() {
            return new Insets(paddingTop, paddingRight, paddingBottom, paddingLeft);
        }
        
        public double getContentWidth() { 
            return width - paddingLeft - paddingRight; 
        }
        
        public double getContentHeight() { 
            return height - paddingTop - paddingBottom; 
        }

        // Preset configurations
        public static PrintSettings createCompact() {
            return new PrintSettings(650, 750, 15, 10, 15, 10, 15, 15, 15, 15);
        }

        public static PrintSettings createStandard() {
            return new PrintSettings(600, 800, 20, 15, 20, 15, 20, 20, 20, 20);
        }

        public static PrintSettings createWide() {
            return new PrintSettings(700, 900, 15, 20, 15, 20, 25, 25, 25, 25);
        }
    }

    // ========== FXML UI COMPONENTS ==========
    @FXML private VBox actionContainer;
    @FXML private Button printButton, editButton, submitButton;
    @FXML private VBox invoiceContainer;
    @FXML private ImageView hotelLogo;
    
    // Invoice header labels
    @FXML private Label invoiceNoLabel, invoiceDateLabel, invoiceTimeLabel;
    
    // Customer information labels
    @FXML private Label customerNameLabel, customerAddressLabel, customerGstLabel, customerMobileLabel;
    
    // Stay details labels
    @FXML private Label roomNoLabel, checkinLabel, checkoutLabel, nightsLabel, guestsLabel;
    
    // Payment and total labels
    @FXML private Label amountWordsLabel, paymentModeLabel, transactionIdLabel, paymentStatusLabel;
    @FXML private Label subtotalLabel, totalLabel;

    // Services table
    @FXML private TableView<ServiceItem1> servicesTable;
    @FXML private TableColumn<ServiceItem1, Integer> srNoColumn;
    @FXML private TableColumn<ServiceItem1, String> descriptionColumn;
    @FXML private TableColumn<ServiceItem1, Integer> quantityColumn;
    @FXML private TableColumn<ServiceItem1, Double> rateColumn, amountColumn;

    // Tax summary table
    @FXML private TableView<TaxItem> taxesTable;
    @FXML private TableColumn<TaxItem, Double> taxableAmountColumn, cgstColumn, sgstColumn, totalAmountColumn;

    // ========== DATA AND STATE ==========
    private ObservableList<ServiceItem1> serviceItems = FXCollections.observableArrayList();
    private ObservableList<TaxItem> taxItems = FXCollections.observableArrayList();
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private BookingData bookingData;

    // ========== INITIALIZATION ==========
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadHotelLogo();
        setupTables();
        
        // Set current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        if (invoiceDateLabel != null)
            invoiceDateLabel.setText(now.format(dateFormatter));
        if (invoiceTimeLabel != null)
            invoiceTimeLabel.setText(now.format(timeFormatter));
    }

    // ========== PUBLIC API ==========
    /**
     * Set the booking data for this invoice
     */
    public void setBookingData(BookingData data) {
        this.bookingData = data;
    }

    /**
     * Populate all invoice fields from the BookingData model
     * Supports room payments, services, expenses, and return payments
     */
    public void populateInvoice() {
        if (bookingData == null) return;

        // Invoice header
        invoiceNoLabel.setText(safeString(bookingData.getInvoiceNumber()));
        
        if (bookingData.getInvoiceDate() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            invoiceDateLabel.setText(bookingData.getInvoiceDate().format(fmt));
        }
        invoiceTimeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        // Customer information
        customerNameLabel.setText(safeString(bookingData.getGuestName()));
        customerAddressLabel.setText(safeString(bookingData.getGuestAddress()));
        customerGstLabel.setText("GST: " + safeString(bookingData.getGuestGst()));
        customerMobileLabel.setText("Mobile: " + safeString(bookingData.getMobileNumber()));

        // Room and stay details
        String roomNo = safeString(bookingData.getRoomNumber());
        String roomType = safeString(bookingData.getRoomType());
        roomNoLabel.setText(roomType.isBlank() ? roomNo : roomType + " - " + roomNo);
        
        if (bookingData.getCheckInDate() != null)
            checkinLabel.setText(bookingData.getCheckInDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        if (bookingData.getCheckOutDate() != null)
            checkoutLabel.setText(bookingData.getCheckOutDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        
        nightsLabel.setText(String.valueOf(Math.max(0, bookingData.getNumberOfNights())));
        guestsLabel.setText(bookingData.getGuestCount() + " Adults");

        // Payment information
        paymentModeLabel.setText(safeString(bookingData.getPaymentMethod()));
        transactionIdLabel.setText(safeString(bookingData.getTransactionId()));
        paymentStatusLabel.setText(safeString(bookingData.getPaymentStatus()));

        // Populate service items
        populateServiceItems();

        // Populate tax summary
        populateTaxSummary();

        // Set financial totals
        double subtotal = bookingData.getSubtotal();
        double totalAmount = bookingData.getTotalPayable();
        
        subtotalLabel.setText("â‚¹ " + currencyFormat.format(subtotal));
        totalLabel.setText("â‚¹ " + currencyFormat.format(totalAmount));
        amountWordsLabel.setText(convertToWords(Math.round(totalAmount)) + " Only");

        // Adjust table heights to show all entries
        setTableHeightToFitAllEntries();
    }

    /**
     * Populate service items from BookingData
     * Handles room charges, service charges, amenities, restaurant, laundry, misc, and return payments
     */
    private void populateServiceItems() {
        serviceItems.clear();

        // Check if explicit service items list is provided
        if (bookingData.getServiceItems() != null && !bookingData.getServiceItems().isEmpty()) {
            int sr = 1;
            for (Object item : bookingData.getServiceItems()) {
                if (item instanceof ServiceItem1) {
                    ServiceItem1 si = (ServiceItem1) item;
                    si.setSrNo(sr++);
                    serviceItems.add(si);
                }
            }
        } else {
            // Auto-generate service items from individual charges
            int sr = 1;
            int nights = Math.max(1, bookingData.getNumberOfNights());
            
            // Room charges
            double roomCharges = bookingData.getRoomCharges();
            if (roomCharges > 0) {
                double ratePerNight = nights > 0 ? roomCharges / nights : roomCharges;
                serviceItems.add(new ServiceItem1(sr++, 
                    "Room Charges - " + displayRoomInfo(), 
                    nights, 
                    round2(ratePerNight), 
                    round2(roomCharges)));
            }
            
            // Other charges
            addChargeIfPositive(sr++, "Service Charges", bookingData.getServiceCharges());
            addChargeIfPositive(sr++, "Amenities", bookingData.getAmenityCharges());
            addChargeIfPositive(sr++, "Restaurant & Dining", bookingData.getRestaurantCharges());
            addChargeIfPositive(sr++, "Laundry Services", bookingData.getLaundryCharges());
            addChargeIfPositive(sr++, "Miscellaneous Charges", bookingData.getMiscellaneousCharges());
            
            // Discount (if any)
            if (bookingData.getDiscount() > 0) {
                serviceItems.add(new ServiceItem1(sr++, 
                    "Discount", 
                    1, 
                    -round2(bookingData.getDiscount()), 
                    -round2(bookingData.getDiscount())));
            }
        }

        servicesTable.setItems(serviceItems);
        servicesTable.refresh();
    }

    /**
     * Populate tax summary table with GST breakdown
     */
    private void populateTaxSummary() {
        taxItems.clear();
        
        double subtotal = bookingData.getSubtotal();
        double cgst = bookingData.getCgst();
        double sgst = bookingData.getSgst();
        double totalAmount = bookingData.getTotalPayable();

        taxItems.add(new TaxItem(
            round2(subtotal), 
            round2(cgst), 
            round2(sgst), 
            round2(totalAmount)
        ));
        
        taxesTable.setItems(taxItems);
        taxesTable.refresh();
    }

    /**
     * Add a charge to service items if amount is positive
     */
    private void addChargeIfPositive(int sr, String label, double amount) {
        if (amount > 0) {
            serviceItems.add(new ServiceItem1(sr, label, 1, round2(amount), round2(amount)));
        }
    }

    // ========== PRINTING METHODS ==========
    /**
     * Print invoice with custom print settings
     * Settings can be loaded from Settings page preferences
     */
    public void printInvoiceWithSettings(PrintSettings settings) {
        setTableHeightToFitAllEntries();
        
        double originalWidth = invoiceContainer.getPrefWidth();
        double originalHeight = invoiceContainer.getPrefHeight();
        Insets originalPadding = invoiceContainer.getPadding();

        try {
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob == null) {
                showAlert("Print Error", "No printer available!", Alert.AlertType.ERROR);
                return;
            }

            Printer printer = printerJob.getPrinter();
            PageLayout pageLayout = printer.createPageLayout(
                Paper.A4,
                PageOrientation.PORTRAIT,
                settings.getMarginLeft(),
                settings.getMarginRight(),
                settings.getMarginTop(),
                settings.getMarginBottom()
            );

            printerJob.getJobSettings().setPageLayout(pageLayout);

            // Show print dialog
            if (printButton != null && printButton.getScene() != null) {
                if (!printerJob.showPrintDialog(printButton.getScene().getWindow())) {
                    return; // User cancelled
                }
            }

            // Prepare invoice for printing
            prepareForPrinting(settings);

            // Calculate scaling
            double availableWidth = pageLayout.getPrintableWidth();
            double availableHeight = pageLayout.getPrintableHeight();
            double scaleX = availableWidth / invoiceContainer.getBoundsInParent().getWidth();
            double scaleY = availableHeight / invoiceContainer.getBoundsInParent().getHeight();
            double scale = Math.min(scaleX, scaleY);

            Scale printScale = null;
            if (scale < 1.0) {
                printScale = new Scale(scale, scale);
                invoiceContainer.getTransforms().add(printScale);
            }

            // Print the page
            boolean success = printerJob.printPage(pageLayout, invoiceContainer);

            // Remove scaling
            if (printScale != null)
                invoiceContainer.getTransforms().remove(printScale);

            // Restore original layout
            restoreAfterPrinting(originalWidth, originalHeight, originalPadding);

            if (success) {
                printerJob.endJob();
                showAlert("Print Success", "Invoice printed successfully.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Print Error", "Failed to print invoice.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            restoreAfterPrinting(originalWidth, originalHeight, originalPadding);
            showAlert("Print Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Print with standard/default settings
     */
    public void printStandard() {
        printInvoiceWithSettings(PrintSettings.createWide());
    }

    /**
     * Prepare invoice layout for printing (hide buttons, adjust sizes)
     */
    private void prepareForPrinting(PrintSettings settings) {
        if (actionContainer != null) {
            actionContainer.setVisible(false);
            actionContainer.setManaged(false);
        }
        
        invoiceContainer.setPrefWidth(settings.getContentWidth());
        invoiceContainer.setMaxWidth(settings.getContentWidth());
        invoiceContainer.setPrefHeight(settings.getContentHeight());
        invoiceContainer.setMaxHeight(settings.getContentHeight());
        invoiceContainer.setPadding(settings.toInsets());

        servicesTable.refresh();
        taxesTable.refresh();
    }

    /**
     * Restore invoice layout after printing
     */
    private void restoreAfterPrinting(double originalWidth, double originalHeight, Insets originalPadding) {
        if (actionContainer != null) {
            actionContainer.setVisible(true);
            actionContainer.setManaged(true);
        }
        
        invoiceContainer.setPrefWidth(originalWidth);
        invoiceContainer.setMaxWidth(originalWidth);
        invoiceContainer.setPrefHeight(originalHeight);
        invoiceContainer.setMaxHeight(originalHeight);
        invoiceContainer.setPadding(originalPadding);
    }

    // ========== TABLE CONFIGURATION ==========
    private void setupTables() {
        // Services table columns
        srNoColumn.setCellValueFactory(new PropertyValueFactory<>("srNo"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // Tax table columns
        taxableAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        cgstColumn.setCellValueFactory(new PropertyValueFactory<>("cgst"));
        sgstColumn.setCellValueFactory(new PropertyValueFactory<>("sgst"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Currency formatting
        setupCurrencyFormatting();

        servicesTable.setItems(serviceItems);
        taxesTable.setItems(taxItems);

        servicesTable.setEditable(false);
        taxesTable.setEditable(false);

        configureTableForPrinting(servicesTable);
        configureTableForPrinting(taxesTable);
    }

    /**
     * Setup currency formatting for numeric columns
     */
    private void setupCurrencyFormatting() {
        rateColumn.setCellFactory(col -> new TableCell<ServiceItem1, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        amountColumn.setCellFactory(col -> new TableCell<ServiceItem1, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        taxableAmountColumn.setCellFactory(col -> new TableCell<TaxItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        cgstColumn.setCellFactory(col -> new TableCell<TaxItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        sgstColumn.setCellFactory(col -> new TableCell<TaxItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        totalAmountColumn.setCellFactory(col -> new TableCell<TaxItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });
    }

    /**
     * Configure table to remove scrollbars for printing
     */
    private void configureTableForPrinting(TableView<?> table) {
        table.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                for (Node node : table.lookupAll(".scroll-bar")) {
                    if (node instanceof ScrollBar) {
                        ScrollBar sb = (ScrollBar) node;
                        sb.setPrefWidth(0);
                        sb.setPrefHeight(0);
                        sb.setMaxWidth(0);
                        sb.setMaxHeight(0);
                        sb.setVisible(false);
                        sb.setManaged(false);
                        sb.setDisable(true);
                    }
                }
                Node sp = table.lookup(".scroll-pane");
                if (sp instanceof ScrollPane) {
                    ScrollPane scrollPane = (ScrollPane) sp;
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                }
            }
        });
    }

    /**
     * Adjust table heights to fit all entries without scrolling
     */
    private void setTableHeightToFitAllEntries() {
        final double rowHeight = 30.0;
        final double headerHeight = 35.0;

        int serviceRowCount = serviceItems.size();
        double servicesTableHeight = headerHeight + serviceRowCount * rowHeight;
        servicesTable.setPrefHeight(servicesTableHeight);
        servicesTable.setMinHeight(servicesTableHeight);
        servicesTable.setMaxHeight(servicesTableHeight);

        int taxRowCount = Math.max(1, taxItems.size());
        double taxesTableHeight = headerHeight + taxRowCount * rowHeight;
        taxesTable.setPrefHeight(taxesTableHeight);
        taxesTable.setMinHeight(taxesTableHeight);
        taxesTable.setMaxHeight(taxesTableHeight);
    }

    // ========== EVENT HANDLERS ==========
    @FXML
    private void handlePrint() {
        printStandard();
    }

    @FXML
    private void handleEdit() {
        showAlert("Edit Invoice", "Invoice editing is disabled. Please modify from payment screen.", 
                  Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSubmit() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Submit Invoice");
        confirmAlert.setHeaderText("Confirm Invoice Submission");
        confirmAlert.setContentText("Do you want to finalize and submit this invoice?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (paymentStatusLabel != null) {
                    String status = bookingData != null ? bookingData.getPaymentStatus() : "Submitted";
                    paymentStatusLabel.setText(status);
                }
                if (editButton != null)
                    editButton.setDisable(true);
                if (submitButton != null)
                    submitButton.setDisable(true);
                    
                showAlert("Success", "Invoice submitted successfully.", Alert.AlertType.INFORMATION);
            }
        });
    }

    // ========== UTILITY METHODS ==========
    private void loadHotelLogo() {
        try {
            var stream = getClass().getResourceAsStream("/images/hotel_logo.png");
            if (stream != null) {
                Image logoImage = new Image(stream);
                if (hotelLogo != null) {
                    hotelLogo.setImage(logoImage);
                    hotelLogo.setFitWidth(80);
                    hotelLogo.setFitHeight(80);
                    hotelLogo.setPreserveRatio(true);
                    hotelLogo.setSmooth(true);
                }
            }
        } catch (Exception e) {
            // Logo loading failed, continue without logo
        }
    }

    private String safeString(String s) {
        return s == null || s.isBlank() ? "" : s;
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    private String displayRoomInfo() {
        if (bookingData == null) return "";
        String rn = safeString(bookingData.getRoomNumber());
        String rt = safeString(bookingData.getRoomType());
        return rt.isBlank() ? rn : rt + " (" + rn + ")";
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Convert amount to Indian Rupees words format
     */
    private String convertToWords(long n) {
        if (n == 0) return "Zero";
        
        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", 
                          "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", 
                          "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

        StringBuilder result = new StringBuilder();

        long crore = n / 10000000;
        n %= 10000000;
        long lakh = n / 100000;
        n %= 100000;
        long thousand = n / 1000;
        n %= 1000;
        long hundred = n / 100;
        n %= 100;

        if (crore > 0) result.append(convertToWords(crore)).append(" Crore ");
        if (lakh > 0) result.append(convertToWords(lakh)).append(" Lakh ");
        if (thousand > 0) result.append(convertToWords(thousand)).append(" Thousand ");
        if (hundred > 0) result.append(convertToWords(hundred)).append(" Hundred ");

        if (n > 0) {
            if (n < 20) {
                result.append(units[(int) n]).append(" ");
            } else {
                result.append(tens[(int) n / 10]).append(" ");
                if ((n % 10) > 0) result.append(units[(int) n % 10]).append(" ");
            }
        }

        return result.toString().trim();
    }
}