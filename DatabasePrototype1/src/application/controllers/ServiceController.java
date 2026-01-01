package application.controllers;

import application.models.*;
import application.services.GstCalculationService;
import application.services.dao.*;
import application.services.store.ServiceCatalogStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import application.services.dao.GuestDAO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ServiceController implements Initializable {

    // FXML UI Components - Guest Information
    @FXML private TextField txtBookingId;
    @FXML private TextField txtRoomNo;
    @FXML private TextField txtGuestName;

    // FXML UI Components - Service Selection
    @FXML private TextField txtServiceName;
    @FXML private ComboBox<String> comboServiceType;
    @FXML private ListView<String> listServices;

    // FXML UI Components - Line Item Entry
    @FXML private ComboBox<String> comboLineItemName;
    @FXML private TextField txtLineItemQuantity;
    @FXML private TextField txtLineItemUnitPrice;
    @FXML private Button btnAddService;

    // FXML UI Components - Main Service Table
    @FXML private TableView<ServiceUsed> tableServices;
    @FXML private TableColumn<ServiceUsed, String> colDate;
    @FXML private TableColumn<ServiceUsed, String> colName;
    @FXML private TableColumn<ServiceUsed, String> colType;
    @FXML private TableColumn<ServiceUsed, Integer> colQuantity;
    @FXML private TableColumn<ServiceUsed, Double> colAmount;
    @FXML private TableColumn<ServiceUsed, String> colDetails;
    @FXML private Label lblTotal;

    @FXML private Button btnEditService;
    @FXML private Button btnDeleteService;
    @FXML private Button btnPrintInvoice;

    // FXML UI Components - Line Items Table
    @FXML private TableView<ServiceLineItem> tableLineItems;
    @FXML private TableColumn<ServiceLineItem, String> colLineItemName;
    @FXML private TableColumn<ServiceLineItem, Integer> colLineItemQuantity;
    @FXML private TableColumn<ServiceLineItem, Double> colLineItemUnitPrice;
    @FXML private TableColumn<ServiceLineItem, Double> colLineItemTotal;
    @FXML private Label lblServiceSubTotal;

    // Data Models
    private final ObservableList<ServiceUsed> serviceList = FXCollections.observableArrayList();
    private final ObservableList<ServiceLineItem> currentServiceLineItems = FXCollections.observableArrayList();
    private List<ServiceCatalogStore.CatalogItem> loadedItems = new ArrayList<>();

    // Service catalog and user preferences
    private ServiceCatalogStore.Catalog catalog;
    private ServiceCatalogStore catalogStore;
    private final Preferences prefs = Preferences.userNodeForPackage(ServiceController.class);

    // DAO references
    private BookingDAO bookingDAO;
    private GuestDAO guestDAO;
    private ServiceUsedDAO serviceUsedDAO;
    private ServiceItemDAO serviceItemDAO;

    // Current booking context
    private Long currentBookingId;
    private String currentSelectedCategory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initializeServices();
            initializeCatalog();
            setupTableColumns();
            setupEventHandlers();
            configureUIComponents();
            populateAvailableServices();
            reloadLineItemNames();
            setupItemNameSearchableComboBox();
            System.out.println("ServiceController initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing ServiceController: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Initialization Error", "Failed to initialize service page: " + e.getMessage());
        }
    }

    private void initializeServices() throws SQLException {
        bookingDAO = new BookingDAO();
        guestDAO = new GuestDAO();
        serviceUsedDAO = new ServiceUsedDAO();
        serviceItemDAO = new ServiceItemDAO();
        serviceUsedDAO.ensureTableExists();
        serviceItemDAO.ensureTableExists();
    }

    private void initializeCatalog() {
        try {
            Path path = Path.of(System.getProperty("user.home"), ".hotelapp", "services.properties");
            catalogStore = new ServiceCatalogStore(path);
            catalog = catalogStore.loadOrInit();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Catalog Error", "Failed to load services catalog: " + e.getMessage());
            catalog = new ServiceCatalogStore.Catalog();
        }
    }

    private void setupTableColumns() {
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getServiceDate() != null ? c.getValue().getServiceDate().toString() : ""));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getService()));
        colType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        colQuantity.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTotalQty()).asObject());
        colAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotalAmount().doubleValue()).asObject());
        colDetails.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getServiceItems().stream()
                        .map(item -> item.getItemName() + " x" + item.getQuantity())
                        .collect(Collectors.joining(", "))));

        colAmount.setCellFactory(tc -> new TableCell<ServiceUsed, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("₹%.2f", amount));
            }
        });

        colLineItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colLineItemQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colLineItemUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colLineItemTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        colLineItemUnitPrice.setCellFactory(tc -> new TableCell<ServiceLineItem, Double>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("₹%.2f", val));
            }
        });
        colLineItemTotal.setCellFactory(tc -> new TableCell<ServiceLineItem, Double>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("₹%.2f", val));
            }
        });

        tableServices.setItems(serviceList);
        tableLineItems.setItems(currentServiceLineItems);
    }

    private void setupEventHandlers() {
        txtRoomNo.setOnKeyReleased(this::handleRoomNoKeyReleased);
        listServices.setOnMouseClicked(this::handleServiceListClick);
        comboServiceType.valueProperty().addListener((obs, oldVal, newVal) -> reloadLineItemNames());
        currentServiceLineItems.addListener((ListChangeListener<ServiceLineItem>) c -> updateServiceSubTotal());
        comboLineItemName.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadedItems.stream()
                        .filter(i -> i.getItemName().equalsIgnoreCase(newVal))
                        .findFirst()
                        .ifPresent(i -> txtLineItemUnitPrice.setText(String.valueOf(i.getUnitPrice())));
            }
        });
        btnAddService.setOnAction(this::handleAddService);
        btnDeleteService.setOnAction(e -> handleDeleteService());
        btnEditService.setOnAction(e -> showAlert(AlertType.INFORMATION, "Edit Service", "Edit service is not implemented yet."));

        btnPrintInvoice.setOnAction(e -> handlePrintInvoice());
        setLineItemInputsEnabled(false);
    }

    private void configureUIComponents() {
        txtBookingId.setEditable(false);
        txtGuestName.setEditable(false);
        txtRoomNo.setEditable(true);
        txtRoomNo.setPromptText("Enter room number to find guest...");
        comboLineItemName.setPromptText("Item name");
        txtLineItemQuantity.setPromptText("Qty");
        txtLineItemUnitPrice.setPromptText("Price");
        txtRoomNo.setStyle("-fx-border-color: #cccccc;");
    }

    private void populateAvailableServices() {
        if (catalog == null || catalog.categories == null) {
            showAlert(AlertType.WARNING, "No Services", "No service categories available. Please check catalog.");
            return;
        }
        List<String> categoryNames = catalog.categories.stream()
                .map(c -> c.name)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (categoryNames.isEmpty()) {
            showAlert(AlertType.WARNING, "No Services", "No categories found in the service catalog.");
            return;
        }
        ObservableList<String> obsList = FXCollections.observableArrayList(categoryNames);
        listServices.setItems(obsList);
        String lastCategory = prefs.get("service.lastCategory", null);
        if (lastCategory != null && categoryNames.contains(lastCategory)) {
            listServices.getSelectionModel().select(lastCategory);
            handleServiceListClick(null);
        } else {
            listServices.getSelectionModel().selectFirst();
        }
    }

    private void reloadLineItemNames() {
        if (currentSelectedCategory == null || comboServiceType.getValue() == null) {
            comboLineItemName.setItems(FXCollections.emptyObservableList());
            return;
        }
        try {
            loadedItems = catalogStore.getItems(currentSelectedCategory, comboServiceType.getValue());
            ObservableList<String> items = FXCollections.observableArrayList(
                    loadedItems.stream().map(ServiceCatalogStore.CatalogItem::getItemName).collect(Collectors.toList()));
            comboLineItemName.setItems(items);
        } catch (IOException e) {
            comboLineItemName.setItems(FXCollections.emptyObservableList());
            e.printStackTrace();
        }
    }

    private void setupItemNameSearchableComboBox() {
        comboLineItemName.setEditable(true);
        TextField editor = comboLineItemName.getEditor();

        // Flag to prevent recursive updates
        final boolean[] updating = {false};

        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (updating[0]) return; // Skip if already updating to prevent recursion
            if (newVal == null) return;

            List<String> filteredList;
            if (newVal.isEmpty()) {
                filteredList = loadedItems.stream()
                        .map(i -> i.itemName)
                        .collect(Collectors.toList());
            } else {
                filteredList = loadedItems.stream()
                        .map(i -> i.itemName)
                        .filter(name -> name.toLowerCase().contains(newVal.toLowerCase()))
                        .collect(Collectors.toList());
            }

            ObservableList<String> newItems = FXCollections.observableArrayList(filteredList);
            ObservableList<String> currentItems = comboLineItemName.getItems();

            // Only update if different
            if (!newItems.equals(currentItems)) {
                updating[0] = true;
                Platform.runLater(() -> {
                    comboLineItemName.setItems(newItems);
                    comboLineItemName.show();
                    updating[0] = false;
                });
            }
        });

        comboLineItemName.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadedItems.stream()
                        .filter(i -> i.itemName.equalsIgnoreCase(newVal))
                        .findFirst()
                        .ifPresent(i -> txtLineItemUnitPrice.setText(String.valueOf(i.unitPrice)));
            }
        });
    }
    private void setLineItemInputsEnabled(boolean enabled) {
        comboLineItemName.setDisable(!enabled);
        txtLineItemQuantity.setDisable(!enabled);
        txtLineItemUnitPrice.setDisable(!enabled);
    }

    // Event handlers

    @FXML
    private void handleRoomNoKeyReleased(KeyEvent event) {
        String roomNo = txtRoomNo.getText().trim();
        if (roomNo.isEmpty()) {
            clearGuestInfo();
            return;
        }
        if (roomNo.length() < 2) return;

        try {
            Booking booking = bookingDAO.getActiveBookingByRoomNo(roomNo);
            if (booking != null) {
                currentBookingId = booking.getId();
                txtBookingId.setText(String.valueOf(booking.getId()));
                txtGuestName.setText(booking.getGuestName());
                refreshServiceTakenTable();
                txtRoomNo.setStyle("-fx-border-color: #28a745; -fx-border-width: 2px;");
            } else {
                clearGuestInfoExceptRoom();
                txtRoomNo.setStyle("-fx-border-color: #ffc107; -fx-border-width: 2px;");
            }
        } catch (SQLException e) {
            txtRoomNo.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2px;");
            showAlert(AlertType.ERROR, "Database Error", "Failed to lookup room: " + e.getMessage());
        }
    }

    private void clearGuestInfo() {
        currentBookingId = null;
        txtBookingId.clear();
        txtGuestName.clear();
        txtRoomNo.clear();
        txtRoomNo.setStyle("-fx-border-color: #cccccc;");
        serviceList.clear();
        updateTotal();
        clearInputForms();
    }

    private void clearGuestInfoExceptRoom() {
        currentBookingId = null;
        txtBookingId.clear();
        txtGuestName.clear();
        serviceList.clear();
        updateTotal();
        clearInputForms();
    }

    private void refreshServiceTakenTable() {
        if (currentBookingId == null) {
            serviceList.clear();
            updateTotal();
            return;
        }
        try {
            List<ServiceUsed> used = serviceUsedDAO.getServiceUsedByBookingId(currentBookingId);
            serviceList.setAll(used);
            updateTotal();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Database Error", "Failed to load services: " + e.getMessage());
        }
    }

    @FXML
    private void handleServiceListClick(MouseEvent event) {
        String category = listServices.getSelectionModel().getSelectedItem();
        if (category == null) {
            txtServiceName.clear();
            comboServiceType.getItems().clear();
            setLineItemInputsEnabled(false);
            currentSelectedCategory = null;
            return;
        }
        currentSelectedCategory = category;
        prefs.put("service.lastCategory", category);
        txtServiceName.setText(category);

        Optional<ServiceCatalogStore.CatalogCategory> catOpt = catalog.categories.stream()
                .filter(c -> c.name.equals(category))
                .findFirst();

        if (catOpt.isPresent()) {
            List<String> serviceNames = catOpt.get().services.stream()
                    .map(s -> s.name)
                    .collect(Collectors.toList());
            if (serviceNames.isEmpty()) serviceNames = List.of("General");
            comboServiceType.setItems(FXCollections.observableArrayList(serviceNames));
            comboServiceType.getSelectionModel().selectFirst();
        } else {
            comboServiceType.setItems(FXCollections.observableArrayList("General"));
            comboServiceType.getSelectionModel().selectFirst();
        }
        setLineItemInputsEnabled(true);
        reloadLineItemNames();
    }

    @FXML
    private void handleAddLineItem(ActionEvent event) {
        String itemName = comboLineItemName.getEditor().getText().trim();
        String qtyText = txtLineItemQuantity.getText().trim();
        String priceText = txtLineItemUnitPrice.getText().trim();

        if (itemName.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
            showAlert(AlertType.WARNING, "Missing Information", "Please enter item name, quantity, and unit price.");
            return;
        }
        try {
            int qty = Integer.parseInt(qtyText);
            double unitPrice = Double.parseDouble(priceText);
            if (qty <= 0) {
                showAlert(AlertType.WARNING, "Invalid Quantity", "Quantity must be positive.");
                return;
            }
            if (unitPrice < 0) {
                showAlert(AlertType.WARNING, "Invalid Price", "Unit price cannot be negative.");
                return;
            }
            ServiceLineItem lineItem = new ServiceLineItem(itemName, qty, unitPrice);
            currentServiceLineItems.add(lineItem);
            addLineItemToCatalogIfNew(txtServiceName.getText(), comboServiceType.getValue(), itemName, unitPrice);
            comboLineItemName.getSelectionModel().clearSelection();
            comboLineItemName.setValue(null);
            comboLineItemName.getEditor().clear();
            comboLineItemName.hide();
            txtLineItemQuantity.clear();
            txtLineItemUnitPrice.clear();
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Invalid Input", "Quantity and price must be valid numbers.");
        }
    }

    private void addLineItemToCatalogIfNew(String categoryName, String serviceName, String itemName, double unitPrice) {
        if (categoryName == null || serviceName == null) return;
        try {
            ServiceCatalogStore.CatalogCategory category = catalog.categories.stream()
                    .filter(c -> c.name.equals(categoryName))
                    .findFirst().orElse(null);
            if (category == null) {
                category = new ServiceCatalogStore.CatalogCategory(categoryName);
                catalog.categories.add(category);
            }

            ServiceCatalogStore.CatalogService service = category.services.stream()
                    .filter(s -> s.name.equals(serviceName))
                    .findFirst().orElse(null);
            if (service == null) {
                service = new ServiceCatalogStore.CatalogService(serviceName);
                category.services.add(service);
            }

            boolean exists = service.items.stream()
                    .anyMatch(i -> i.itemName.equalsIgnoreCase(itemName));
            if (!exists) {
                service.items.add(new ServiceCatalogStore.CatalogItem(itemName, unitPrice));
                catalogStore.save(catalog);
            }
        } catch (Exception e) {
            System.err.println("Error updating catalog: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearLineItems(ActionEvent event) {
        currentServiceLineItems.clear();
        comboLineItemName.getSelectionModel().clearSelection();
        comboLineItemName.setValue(null);
        comboLineItemName.getEditor().clear();
        txtLineItemQuantity.clear();
        txtLineItemUnitPrice.clear();
        setLineItemInputsEnabled(false);
        lblServiceSubTotal.setText("₹0.00");
    }

    private void updateServiceSubTotal() {
        double subtotal = currentServiceLineItems.stream()
                .mapToDouble(ServiceLineItem::getTotalAmount)
                .sum();
        lblServiceSubTotal.setText(String.format("₹%.2f", subtotal));
    }

 // Add GuestDAO import at the top


    // Add this method to get guest details
    private Map<String, String> getGuestDetails(Long guestId) throws SQLException {
        Map<String, String> guestDetails = new HashMap<>();
        guestDetails.put("phone", "");
        guestDetails.put("email", "");
        guestDetails.put("address", "");
        guestDetails.put("gst", "");
        
        if (guestId != null) {
            GuestDAO guestDAO = new GuestDAO();
            Guest guest = guestDAO.getGuestById(guestId);
            if (guest != null) {
                if (guest.getPhone() != null) guestDetails.put("phone", guest.getPhone());
                if (guest.getEmail() != null) guestDetails.put("email", guest.getEmail());
                if (guest.getAddress() != null) guestDetails.put("address", guest.getAddress());
                if (guest.getGstNumber() != null) guestDetails.put("gst", guest.getGstNumber());
            }
        }
        return guestDetails;
    }

    // Update the handleAddService method for better error handling
    @FXML
    private void handleAddService(ActionEvent event) {
        String serviceName = txtServiceName.getText().trim();
        String categoryType = comboServiceType.getValue();

        if (serviceName.isEmpty() || categoryType == null) {
            showAlert(AlertType.WARNING, "Missing Service Details", "Please select a service and category.");
            return;
        }
        if (currentServiceLineItems.isEmpty()) {
            showAlert(AlertType.WARNING, "No Items Added", "Add at least one item to the service.");
            return;
        }
        if (currentBookingId == null) {
            showAlert(AlertType.ERROR, "No Booking", "Select a room/guest before adding services.");
            txtRoomNo.requestFocus();
            return;
        }

        // Show payment option BEFORE saving the service
        showPayNowOrLaterDialog(serviceName, categoryType);
    }

    private void showPayNowOrLaterDialog(String serviceName, String categoryType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Payment Option");
        alert.setHeaderText("Add Service to Bill");
        alert.setContentText("Would you like to pay for this service now or add to bill for later payment?");

        ButtonType payNow = new ButtonType("Pay Now");
        ButtonType payLater = new ButtonType("Add to Bill");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(payNow, payLater, cancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == payNow) {
                // Save service and open payment
                boolean serviceSaved = saveServiceToDatabase(serviceName, categoryType);
                if (serviceSaved) {
                    openPaymentWindow();
                }
            } else if (result.get() == payLater) {
                // Just save service to bill
                boolean serviceSaved = saveServiceToDatabase(serviceName, categoryType);
                if (serviceSaved) {
                    showAlert(AlertType.INFORMATION, "Service Added", 
                        "Service successfully added to bill for later payment.");
                }
            }
            // If cancel, do nothing - service won't be saved
        }
    }

    private boolean saveServiceToDatabase(String serviceName, String categoryType) {
        try {
            ServiceUsed serviceUsed = new ServiceUsed(currentBookingId, Date.valueOf(LocalDate.now()), serviceName, categoryType);
            Long id = serviceUsedDAO.addServiceUsed(serviceUsed);
            
            for (ServiceLineItem lineItem : currentServiceLineItems) {
                ServiceItem item = new ServiceItem(id, lineItem.getItemName(), lineItem.getQuantity(), BigDecimal.valueOf(lineItem.getUnitPrice()));
                serviceItemDAO.addServiceItem(item);
                serviceUsed.addServiceItem(item);
            }
            
            serviceUsedDAO.updateServiceUsedTotals(id);
            refreshServiceTakenTable();
            clearInputForms();
            
            // Add to catalog if new items
            for (ServiceLineItem lineItem : currentServiceLineItems) {
                addLineItemToCatalogIfNew(serviceName, categoryType, lineItem.getItemName(), lineItem.getUnitPrice());
            }
            
            return true;
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Database Error", "Failed to save service: " + e.getMessage());
            return false;
        }
    }

    // Update the payment dialog to show amount
    private void showPayNowOrLaterDialog(double serviceTotal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Payment Option");
        alert.setHeaderText("Service Added to Bill");
        alert.setContentText(String.format(
            "Service has been added to the bill.\nTotal Amount: ₹%.2f\n\nWould you like to pay now or pay later?",
            serviceTotal
        ));

        ButtonType payNow = new ButtonType("Pay Now");
        ButtonType payLater = new ButtonType("Pay Later");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(payNow, payLater, cancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == payNow) {
                openPaymentWindow();
            } else if (result.get() == payLater) {
                showAlert(AlertType.INFORMATION, "Service Added", 
                    "Service has been added to the bill. Payment can be made later.");
            }
        }
    }
    
    private void showPayNowOrLaterDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Payment Option");
        alert.setHeaderText("Service Added to Bill");
        alert.setContentText("Would you like to pay now or pay later?");

        ButtonType payNow = new ButtonType("Pay Now");
        ButtonType payLater = new ButtonType("Pay Later");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(payNow, payLater, cancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == payNow) {
                openPaymentWindow();
            } else if (result.get() == payLater) {
                // User chooses to pay later - do nothing or show a message
            } // Cancel or close dialog does nothing
        }
    }

    private void openPaymentWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Payment.fxml"));
            Parent root = loader.load();

            PaymentController paymentController = loader.getController();

            // Fetch full booking and guest details for current booking ID
            BookingDAO bookingDAO = new BookingDAO();
            Booking booking = bookingDAO.getBookingById(currentBookingId);

            if (booking == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Booking details not found for payment.");
                return;
            }

            // Fetch guest details to ensure we have all required information
            GuestDAO guestDAO = new GuestDAO();
            Guest guest = guestDAO.getGuestById(booking.getGuestId());

            if (guest == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Guest details not found for payment.");
                return;
            }

            // Pass complete booking and guest context
            paymentController.setBookingContext(
                booking.getId(),
                guest.getId() // Pass guest ID to avoid guest creation
            );

            // Set guest details explicitly to ensure they're available
            paymentController.setGuestDetails(
                guest.getName(),
                guest.getPhone(),
                guest.getEmail(),
                guest.getAddress(),
                guest.getGstNumber()
            );

            // Load ALL services for this booking, not just the current ones
            List<ServiceLineItem> allServiceItems = loadAllServiceItemsForBooking();
            paymentController.setServiceItems(allServiceItems);

            Stage stage = new Stage();
            stage.setTitle("Payment - Service Charges");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnAddService.getScene().getWindow());
            stage.setResizable(false);
            
            // Set up close handler to refresh service table after payment
            stage.setOnHidden(e -> refreshServiceTakenTable());
            stage.showAndWait();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open payment window: " + e.getMessage());
        }
    }

    private List<ServiceLineItem> loadAllServiceItemsForBooking() throws SQLException {
        List<ServiceLineItem> allItems = new ArrayList<>();
        
        if (currentBookingId != null) {
            List<ServiceUsed> servicesUsed = serviceUsedDAO.getServiceUsedByBookingId(currentBookingId);
            for (ServiceUsed serviceUsed : servicesUsed) {
                List<ServiceItem> items = serviceItemDAO.getServiceItemsByServiceUsedId(serviceUsed.getId());
                for (ServiceItem item : items) {
                    allItems.add(new ServiceLineItem(
                        serviceUsed.getService() + " - " + item.getItemName(),
                        item.getQuantity(),
                        item.getPrice().doubleValue()
                    ));
                }
            }
        }
        
        return allItems;
    }
    
    
    @FXML
    private void handleDeleteService() {
        ServiceUsed selected = tableServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "No Selection", "Select a service to delete.");
            return;
        }
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Service");
        confirm.setContentText("Are you sure you want to delete this service?\nService: " + selected.getService() + " (" + selected.getCategory() + ")\nDate: " + selected.getServiceDate());
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUsedDAO.deleteServiceUsed(selected.getId());
                refreshServiceTakenTable();
                showAlert(AlertType.INFORMATION, "Service Deleted", "Service deleted successfully.");
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Database Error", "Failed to delete service: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrintInvoice() {
        if (serviceList.isEmpty()) {
            showAlert(AlertType.INFORMATION, "Print Invoice", "No services to print.");
            return;
        }
        if (currentBookingId == null) {
            showAlert(AlertType.WARNING, "No Booking Selected", "Select a guest before printing invoice.");
            return;
        }
        StringBuilder invoice = new StringBuilder();
        invoice.append("=".repeat(75)).append("\n");
        invoice.append("SERVICE INVOICE").append("\n");
        invoice.append("=".repeat(75)).append("\n");
        invoice.append("Date: ").append(LocalDate.now()).append("\n");
        invoice.append("Booking ID: ").append(txtBookingId.getText()).append("\n");
        invoice.append("Guest Name: ").append(txtGuestName.getText()).append("\n");
        invoice.append("Room No: ").append(txtRoomNo.getText()).append("\n");
        invoice.append("-".repeat(75)).append("\n");
        invoice.append("Services Consumed:\n");
        invoice.append("-".repeat(75)).append("\n");

        double grandTotal = 0.0;
        int count = 1;

        for (ServiceUsed service : serviceList) {
            invoice.append(String.format("%d. %-20s %-15s %25s\n",
                    count++, service.getService(), "(" + service.getCategory() + ")",
                    "₹" + String.format("%.2f", service.getTotalAmount().doubleValue())));
            invoice.append(String.format(" Date: %s\n", service.getServiceDate()));
            for (ServiceItem item : service.getServiceItems()) {
                invoice.append(String.format("   • %-30s %3d x %10s = %12s\n",
                        item.getItemName(),
                        item.getQuantity(),
                        "₹" + String.format("%.2f", item.getPrice().doubleValue()),
                        "₹" + String.format("%.2f", item.getAmount().doubleValue())));
            }
            invoice.append("\n");
            grandTotal += service.getTotalAmount().doubleValue();
        }
        invoice.append("-".repeat(75)).append("\n");
        invoice.append(String.format("%55s %20s\n", "GRAND TOTAL:", "₹" + String.format("%.2f", grandTotal)));
        invoice.append("=".repeat(75)).append("\n");
        invoice.append("Thank You!\nGenerated on ").append(LocalDate.now()).append("\n");
        invoice.append("=".repeat(75)).append("\n");

        System.out.println(invoice.toString());

        showAlert(AlertType.INFORMATION, "Invoice Generated", "Invoice printed to console.\nUse a proper print or save function in production.");
    }

    private void updateTotal() {
        double total = serviceList.stream().mapToDouble(s -> s.getTotalAmount().doubleValue()).sum();
        lblTotal.setText(String.format("₹%.2f", total));
    }

    private void clearInputForms() {
        txtServiceName.clear();
        comboServiceType.getSelectionModel().clearSelection();
        comboServiceType.setItems(FXCollections.emptyObservableList());
        listServices.getSelectionModel().clearSelection();
        handleClearLineItems(null);
        currentSelectedCategory = null;
        setLineItemInputsEnabled(false);
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void showAlertError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header, just content text
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Public helpers

    public void setRoomNumber(String roomNo) {
        txtRoomNo.setText(roomNo);
        handleRoomNoKeyReleased(null);
    }

    public String getRoomNumber() {
        return txtRoomNo.getText().trim();
    }

    public Long getCurrentBookingId() {
        return currentBookingId;
    }

    public double getTotalAmount() {
        return serviceList.stream().mapToDouble(s -> s.getTotalAmount().doubleValue()).sum();
    }

    public int getServiceCount() {
        return serviceList.size();
    }

    public void refreshData() {
        refreshServiceTakenTable();
        populateAvailableServices();
    }

    public void reset() {
        clearGuestInfo();
        clearInputForms();
        currentSelectedCategory = null;
    }
}
