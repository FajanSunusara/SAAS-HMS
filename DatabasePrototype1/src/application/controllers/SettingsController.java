package application.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Optional;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.file.Path;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Import your DAO classes and models
import application.services.dao.*;
import application.services.store.ServiceCatalogStore;
import application.utils.PropertyReader;
import application.services.store.InvoiceSettingsStore;
import application.services.store.RoomCategoryStore;
import application.enums.SidebarItem;
import application.models.*;

public class SettingsController implements Initializable {
	@FXML private Button controlsTab;
	@FXML private VBox controlsContent;
	@FXML private TextField gstRateField;
	@FXML private ComboBox<String> defaultNationalityCombo;
	@FXML private Label gstValidationLabel;
	@FXML private Label controlsStatusLabel;
    // Tab Navigation Buttons
    @FXML private Button roomsTab, servicesTab, referencesTab, userAccessTab, invoiceDesignTab;
    @FXML private HBox navbarTabs;
    
    // Content Areas
    @FXML private VBox roomsContent, servicesContent, referencesContent, userAccessContent, invoiceDesignContent;
    
    // Room Management Components
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> roomNoCol, roomTypeCol, floorCol, statusCol, priceCol, acCol;
    
    // Service Management Components
    @FXML private TableView<Service> servicesTable;
    @FXML private TableColumn<Service, String> serviceNameCol, descriptionCol, servicePriceCol, taxCol, availableCol;
    
    // Reference Management Components
    @FXML private TableView<Reference> referencesTable;
    @FXML private TableColumn<Reference, String> sourceNameCol, sourceTypeCol;
    @FXML private TableView<Commission> commissionTable;
    @FXML private TableColumn<Commission, String> commissionSourceCol, commissionPercentCol, fixedAmountCol, notesCol;
    
    // User Access Components
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameCol, roleCol, emailCol, userStatusCol, lastLoginCol;
    
    // Invoice Design Components
    @FXML private Label logoPreview;
    @FXML private TextArea headerTextArea, footerTextArea;
    @FXML private ComboBox<String> templateStyleCombo;
    @FXML private Button uploadLogoBtn;
    @FXML private VBox pageAccessControlContainer;
    @FXML private FlowPane pageAccessFlowPane;
    // Data Lists
    private ObservableList<Room> roomData = FXCollections.observableArrayList();
    private ObservableList<Service> serviceData = FXCollections.observableArrayList();
    private ObservableList<String> roomCategories = FXCollections.observableArrayList();
    private ObservableList<Reference> referenceData = FXCollections.observableArrayList();
    private ObservableList<Commission> commissionData = FXCollections.observableArrayList();
    private ObservableList<User> userData = FXCollections.observableArrayList();
    
    // DAOs and Services
    private RoomDAO roomDAO;
    private ServiceDAO serviceDAO;
    private UserDAO userDAO;
    private ReferenceSourceDAO referenceSourceDAO;
    private ServiceCatalogStore catalogStore;
    private ServiceCatalogStore.Catalog serviceCatalog;
    private InvoiceSettingsStore invoiceSettingsStore;
    private RoomCategoryStore roomCategoryStore;
//    private Map<SidebarItem, CheckBox> pageAccessCheckboxes = new HashMap<>();
//    private static final String PAGE_ACCESS_PREFIX = "page.access.";
    private Map<SidebarItem, CheckBox> pageAccessCheckboxes = new HashMap<>();
    private static final String PAGE_ACCESS_PREFIX = "page.access.";
    // Current invoice settings
    private InvoiceSettingsStore.InvoiceSettings currentInvoiceSettings;
    private final PropertyReader propertyReader = PropertyReader.getInstance();
 
    @FXML 
    private void showControlsTab() {
        hideAllContent();
        controlsContent.setVisible(true);
        updateTabStyles(controlsTab);
        loadControlsSettings();
    }
    

private void loadControlsSettings() {
    // Load GST rate
    if (gstRateField != null) {
        double currentGstRate = propertyReader.getGstRate();
        gstRateField.setText(String.valueOf(currentGstRate));
        
        // Add validation listener
        gstRateField.textProperty().addListener((obs, old, newVal) -> {
            validateGstRate(newVal);
        });
    }
    
    // Load default nationality
    if (defaultNationalityCombo != null) {
        defaultNationalityCombo.setItems(FXCollections.observableArrayList(
            "India", "United States", "United Kingdom", "Canada", "Australia", 
            "Germany", "France", "Japan", "Singapore", "UAE", "China", "Russia"
        ));
        defaultNationalityCombo.setValue(propertyReader.getDefaultNationality());
    }
}

private void validateGstRate(String value) {
    if (gstValidationLabel == null) return;
    
    try {
        double gstRate = Double.parseDouble(value);
        if (propertyReader.isValidGstRate(gstRate)) {
            gstValidationLabel.setText("✓ Valid");
            gstValidationLabel.setStyle("-fx-text-fill: green;");
        } else {
            gstValidationLabel.setText("✗ Must be 0-100%");
            gstValidationLabel.setStyle("-fx-text-fill: red;");
        }
    } catch (NumberFormatException e) {
        gstValidationLabel.setText("✗ Invalid number");
        gstValidationLabel.setStyle("-fx-text-fill: red;");
    }
}

@FXML
private void saveControlsSettings() {
    try {
        // Save GST rate
        if (gstRateField != null) {
            String gstText = gstRateField.getText();
            double gstRate = Double.parseDouble(gstText);
            
            if (!propertyReader.isValidGstRate(gstRate)) {
                showAlert("Invalid GST rate. Must be between 0 and 100.");
                return;
            }
            
            propertyReader.setGstRate(gstRate);
        }
        
        // Save default nationality
        if (defaultNationalityCombo != null) {
            propertyReader.set("default.nationality", defaultNationalityCombo.getValue());
        }
        
        // Show success message
        if (controlsStatusLabel != null) {
            controlsStatusLabel.setText("✓ Settings saved successfully");
            controlsStatusLabel.setStyle("-fx-text-fill: green;");
            
            // Clear message after 3 seconds
            javafx.application.Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        javafx.application.Platform.runLater(() -> {
                            if (controlsStatusLabel != null) {
                                controlsStatusLabel.setText("");
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        }
        
        showAlert("Settings saved successfully!");
        
    } catch (Exception e) {
        showAlert("Error saving settings: " + e.getMessage());
        if (controlsStatusLabel != null) {
            controlsStatusLabel.setText("✗ Error saving settings");
            controlsStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}

@FXML
private void resetControlsSettings() {
    if (gstRateField != null) {
        gstRateField.setText("18.0");
    }
    if (defaultNationalityCombo != null) {
        defaultNationalityCombo.setValue("India");
    }
    if (gstValidationLabel != null) {
        gstValidationLabel.setText("");
    }
    if (controlsStatusLabel != null) {
        controlsStatusLabel.setText("Settings reset to defaults");
        controlsStatusLabel.setStyle("-fx-text-fill: blue;");
    }
}

// Update hideAllContent() method to include controlsContent


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize DAOs
            initializeDAOs();
            
            // Initialize components
            initializeComponents();
            
            // Load data from database and properties
            loadDataFromDatabase();
            loadServiceCatalog();
            loadInvoiceSettings();
            initializePageAccessCheckboxes();
            loadPageAccessSettings();
            // Show rooms tab by default
            showRoomsTab();
            
            System.out.println("SettingsController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing SettingsController: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error initializing Settings: " + e.getMessage());
        }
    }

    private void initializeDAOs() {
        try {
            roomDAO = new RoomDAO();
            serviceDAO = new ServiceDAO();
            userDAO = new UserDAO();
            referenceSourceDAO = new ReferenceSourceDAO();
            
            // Initialize service catalog store
            Path catalogPath = Path.of(System.getProperty("user.home"), ".hotelapp", "services.properties");
            catalogStore = new ServiceCatalogStore(catalogPath);
            
            // Initialize invoice settings store
            Path invoicePath = Path.of(System.getProperty("user.home"), ".hotelapp", "invoice_settings.properties");
            invoiceSettingsStore = new InvoiceSettingsStore(invoicePath);
            
            // Initialize room category store
            Path roomCategoryPath = Path.of(System.getProperty("user.home"), ".hotelapp", "room_categories.properties");
            roomCategoryStore = new RoomCategoryStore(roomCategoryPath);
            
            System.out.println("All DAOs initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing DAOs: " + e.getMessage());
            // Create fallback stores
            Path catalogPath = Path.of(System.getProperty("user.home"), ".hotelapp", "services.properties");
            catalogStore = new ServiceCatalogStore(catalogPath);
            Path invoicePath = Path.of(System.getProperty("user.home"), ".hotelapp", "invoice_settings.properties");
            invoiceSettingsStore = new InvoiceSettingsStore(invoicePath);
            Path roomCategoryPath = Path.of(System.getProperty("user.home"), ".hotelapp", "room_categories.properties");
            roomCategoryStore = new RoomCategoryStore(roomCategoryPath);
        }
    }

    private void initializeComponents() {
        templateStyleCombo.setItems(FXCollections.observableArrayList("Classic", "Modern", "Minimal"));
        initializeRoomsTable();
        initializeServicesTable();
        initializeReferencesTable();
        initializeUsersTable();
    }
    

    
    private void initializePageAccessCheckboxes() {
        pageAccessFlowPane.getChildren().clear();
        for (SidebarItem item : SidebarItem.values()) {
            if (item.getFxmlPath() == null) continue; // Skip action or non-page items
            CheckBox cb = new CheckBox(item.getName());
            pageAccessCheckboxes.put(item, cb);
            pageAccessFlowPane.getChildren().add(cb);
        }
    }

    private void loadPageAccessSettings() {
        for (SidebarItem item : pageAccessCheckboxes.keySet()) {
            boolean enabled = PropertyReader.getInstance()
                .getBooleanProperty(PAGE_ACCESS_PREFIX + item.name(), true);
            pageAccessCheckboxes.get(item).setSelected(enabled);
        }
    }
    @FXML
    private void savePageAccessSettings() {
        for (Map.Entry<SidebarItem, CheckBox> entry : pageAccessCheckboxes.entrySet()) {
            PropertyReader.getInstance().setProperty(
                PAGE_ACCESS_PREFIX + entry.getKey().name(),
                Boolean.toString(entry.getValue().isSelected())
            );
        }
        PropertyReader.getInstance().saveProperties();

        controlsStatusLabel.setText("Page access settings saved!");
        controlsStatusLabel.setStyle("-fx-text-fill: green;");
        // Optionally clear message later on in a thread or timeline
    }

    private void initializeRoomsTable() {
        roomNoCol.setCellValueFactory(c -> c.getValue().roomNoProperty());
        roomTypeCol.setCellValueFactory(c -> c.getValue().typeProperty());
        floorCol.setCellValueFactory(c -> c.getValue().floorProperty());
        statusCol.setCellValueFactory(c -> c.getValue().statusProperty());
        priceCol.setCellValueFactory(c -> c.getValue().priceProperty());
        acCol.setCellValueFactory(c -> c.getValue().acTypeProperty());
        roomsTable.setItems(roomData);
    }

    private void initializeServicesTable() {
        serviceNameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        descriptionCol.setCellValueFactory(c -> c.getValue().descriptionProperty());
        servicePriceCol.setCellValueFactory(c -> c.getValue().priceProperty());
        taxCol.setCellValueFactory(c -> c.getValue().taxProperty());
        availableCol.setCellValueFactory(c -> c.getValue().availableProperty());
        servicesTable.setItems(serviceData);
    }

    private void initializeReferencesTable() {
        sourceNameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        sourceTypeCol.setCellValueFactory(c -> c.getValue().typeProperty());
        referencesTable.setItems(referenceData);
        
        commissionSourceCol.setCellValueFactory(c -> c.getValue().sourceProperty());
        commissionPercentCol.setCellValueFactory(c -> c.getValue().percentProperty());
        fixedAmountCol.setCellValueFactory(c -> c.getValue().fixedAmountProperty());
        notesCol.setCellValueFactory(c -> c.getValue().notesProperty());
        commissionTable.setItems(commissionData);
    }

    private void initializeUsersTable() {
        usernameCol.setCellValueFactory(c -> c.getValue().usernameProperty());
        roleCol.setCellValueFactory(c -> c.getValue().roleProperty());
        emailCol.setCellValueFactory(c -> c.getValue().emailProperty());
        userStatusCol.setCellValueFactory(c -> c.getValue().statusProperty());
        lastLoginCol.setCellValueFactory(c -> c.getValue().lastLoginProperty());
        usersTable.setItems(userData);
    }

    private void loadDataFromDatabase() {
        loadRoomsFromDatabase();
        loadRoomCategoriesFromDatabase();
        loadReferencesFromDatabase();
        loadUsersFromDatabase();
    }

    private void loadRoomsFromDatabase() {
        try {
            roomData.clear();
            if (roomDAO != null) {
                for (application.models.Room dbRoom : roomDAO.getAllRooms()) {
                    Room uiRoom = new Room(
                        dbRoom.getRoomNo(),
                        dbRoom.getRoomType(),
                        String.valueOf(dbRoom.getFloor()),
                        dbRoom.getStatus(),
                        "₹" + String.format("%.2f", dbRoom.getPrice().doubleValue()),
                        dbRoom.isAc() ? "AC" : "Non-AC"
                    );
                    uiRoom.setDbId(dbRoom.getId()); // Store database ID for updates
                    roomData.add(uiRoom);
                }
            }
            System.out.println("Loaded " + roomData.size() + " rooms from database");
        } catch (Exception e) {
            System.err.println("Error loading rooms: " + e.getMessage());
            // Load some sample data as fallback
            loadSampleRoomData();
        }
    }

    private void loadRoomCategoriesFromDatabase() {
        try {
            roomCategories.clear();
            if (roomDAO != null) {
                List<String> dbCategories = roomDAO.findAllRoomTypes();
                roomCategories.setAll(dbCategories);
            }
            
            // Also load from room category store as additional options
            if (roomCategoryStore != null) {
                try {
                    List<String> storeCategories = roomCategoryStore.getCategoryNames();
                    for (String category : storeCategories) {
                        List<String> roomTypes = roomCategoryStore.getRoomTypeNames(category);
                        for (String roomType : roomTypes) {
                            if (!roomCategories.contains(roomType)) {
                                roomCategories.add(roomType);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading room categories from store: " + e.getMessage());
                }
            }
            
            // Fallback categories
            if (roomCategories.isEmpty()) {
                roomCategories.addAll("Single", "Double", "Suite", "Twin", "Deluxe");
            }
            
            System.out.println("Loaded " + roomCategories.size() + " room categories");
        } catch (Exception e) {
            System.err.println("Error loading room categories: " + e.getMessage());
            // Fallback categories
            roomCategories.addAll("Single", "Double", "Suite", "Twin", "Deluxe");
        }
    }

    private void loadServiceCatalog() {
        try {
            serviceCatalog = catalogStore.loadOrInit();
            populateServiceDataFromCatalog();
            System.out.println("Service catalog loaded with " + serviceCatalog.categories.size() + " categories");
        } catch (Exception e) {
            System.err.println("Error loading service catalog: " + e.getMessage());
            serviceCatalog = new ServiceCatalogStore.Catalog();
        }
    }

    private void populateServiceDataFromCatalog() {
        serviceData.clear();
        for (ServiceCatalogStore.CatalogCategory category : serviceCatalog.categories) {
            for (ServiceCatalogStore.CatalogService service : category.services) {
                for (ServiceCatalogStore.CatalogItem item : service.items) {
                    Service uiService = new Service(
                        item.itemName,
                        category.name + " - " + service.name,
                        "₹" + String.format("%.2f", item.unitPrice),
                        "18%",
                        "Yes"
                    );
                    serviceData.add(uiService);
                }
            }
        }
    }

    private void loadReferencesFromDatabase() {
        try {
            referenceData.clear();
            commissionData.clear();
            if (referenceSourceDAO != null) {
                for (ReferenceSource dbRef : referenceSourceDAO.getAllReferenceSources()) {
                    Reference uiRef = new Reference(dbRef.getSourceName(), "Online");
                    uiRef.setDbId(dbRef.getId()); // Store database ID for updates
                    referenceData.add(uiRef);
                    
                    Commission uiCommission = new Commission(
                        dbRef.getSourceName(),
                        String.format("%.2f%%", dbRef.getCommissionPercentage().doubleValue()),
                        "₹" + String.format("%.2f", dbRef.getFixedAmount().doubleValue()),
                        dbRef.getNotes() != null ? dbRef.getNotes() : ""
                    );
                    uiCommission.setDbId(dbRef.getId()); // Store database ID for updates
                    commissionData.add(uiCommission);
                }
            }
            System.out.println("Loaded " + referenceData.size() + " references from database");
        } catch (Exception e) {
            System.err.println("Error loading references: " + e.getMessage());
            loadSampleReferenceData();
        }
    }

    private void loadUsersFromDatabase() {
        try {
            userData.clear();
            if (userDAO != null) {
                for (application.models.User dbUser : userDAO.findAllUsers()) {
                    User uiUser = new User(
                        dbUser.getUsername(),
                        dbUser.getRole(),
                        dbUser.getEmail(),
                        dbUser.getStatus(),
                        dbUser.getCreatedAt() != null ? dbUser.getCreatedAt().toString() : "Never"
                    );
                    uiUser.setDbId(dbUser.getId());
                    uiUser.setFullName(dbUser.getFullName());
                    uiUser.setPassword(dbUser.getPasswordHash()); // Store existing password hash
                    userData.add(uiUser);
                }
            }
            System.out.println("Loaded " + userData.size() + " users from database");
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            loadSampleUserData();
        }
    }

    private void loadInvoiceSettings() {
        try {
            currentInvoiceSettings = invoiceSettingsStore.loadOrDefault();
            // Populate UI with loaded settings
            headerTextArea.setText(currentInvoiceSettings.headerText);
            footerTextArea.setText(currentInvoiceSettings.footerText);
            templateStyleCombo.setValue(currentInvoiceSettings.templateStyle);
            if (!currentInvoiceSettings.logoPath.isEmpty()) {
                logoPreview.setText(new File(currentInvoiceSettings.logoPath).getName());
            }
        } catch (Exception e) {
            System.err.println("Error loading invoice settings: " + e.getMessage());
            currentInvoiceSettings = new InvoiceSettingsStore.InvoiceSettings();
        }
    }

    // Fallback sample data methods
    private void loadSampleRoomData() {
        roomData.addAll(
            new Room("101", "Single", "1", "Available", "₹2000", "AC"),
            new Room("102", "Double", "1", "Occupied", "₹3000", "AC")
        );
    }

    private void loadSampleReferenceData() {
        referenceData.add(new Reference("Booking.com", "Online Travel Agent"));
        commissionData.add(new Commission("Booking.com", "15%", "₹0", "Standard commission"));
    }

    private void loadSampleUserData() {
        userData.add(new User("admin", "Administrator", "admin@hotel.com", "Active", "2024-01-15 10:30"));
    }

    // ===== TAB NAVIGATION =====
    @FXML private void showRoomsTab() {
        hideAllContent();
        roomsContent.setVisible(true);
        updateTabStyles(roomsTab);
    }

    @FXML private void showServicesTab() {
        hideAllContent();
        servicesContent.setVisible(true);
        updateTabStyles(servicesTab);
    }

    @FXML private void showReferencesTab() {
        hideAllContent();
        referencesContent.setVisible(true);
        updateTabStyles(referencesTab);
    }

    @FXML private void showUserAccessTab() {
        hideAllContent();
        userAccessContent.setVisible(true);
        updateTabStyles(userAccessTab);
    }

    @FXML private void showInvoiceDesignTab() {
        hideAllContent();
        invoiceDesignContent.setVisible(true);
        updateTabStyles(invoiceDesignTab);
    }

    private void hideAllContent() {
    	   roomsContent.setVisible(false);
    	    servicesContent.setVisible(false);
    	    referencesContent.setVisible(false);
    	    userAccessContent.setVisible(false);
    	    invoiceDesignContent.setVisible(false);
    	    if (controlsContent != null) controlsContent.setVisible(false);
    }

    private void updateTabStyles(Button activeTab) {
        for (javafx.scene.Node node : navbarTabs.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("active-tab");
            }
        }
        activeTab.getStyleClass().add("active-tab");
    }

    // ===== ROOM MANAGEMENT =====
    @FXML private void showAddRoomModal() {
        showRoomModal("Add Room", null);
    }

    @FXML private void showEditRoomModal() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showRoomModal("Edit Room", selected);
        } else {
            showAlert("Please select a room to edit.");
        }
    }

    @FXML private void deleteRoom() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showConfirmation("Delete Room", "Are you sure you want to delete room " + selected.getRoomNo() + "?")) {
                try {
                    // Delete from database if DAO is available and room has DB ID
                    if (roomDAO != null && selected.getDbId() != null) {
                        roomDAO.deleteRoom(selected.getDbId());
                    }
                    roomData.remove(selected);
                    showAlert("Room deleted successfully!");
                } catch (Exception e) {
                    showAlert("Error deleting room: " + e.getMessage());
                }
            }
        } else {
            showAlert("Please select a room to delete.");
        }
    }

    // ===== SERVICE MANAGEMENT =====
    @FXML private void showAddServiceModal() {
        showServiceCatalogWindow();
    }

    @FXML private void showEditServiceModal() {
        showServiceCatalogWindow();
    }

    @FXML private void deleteService() {
        Service selected = servicesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showConfirmation("Delete Service", "Are you sure you want to delete this service?")) {
                // Remove from service catalog and save
                try {
                    // Find and remove from catalog
                    boolean removed = false;
                    for (ServiceCatalogStore.CatalogCategory category : serviceCatalog.categories) {
                        for (ServiceCatalogStore.CatalogService service : category.services) {
                            service.items.removeIf(item -> item.itemName.equals(selected.getName()));
                            if (service.items.isEmpty()) {
                                category.services.remove(service);
                                removed = true;
                                break;
                            }
                        }
                        if (removed) break;
                    }
                    saveServiceCatalog();
                    populateServiceDataFromCatalog();
                    showAlert("Service deleted successfully!");
                } catch (Exception e) {
                    showAlert("Error deleting service: " + e.getMessage());
                }
            }
        } else {
            showAlert("Please select a service to delete.");
        }
    }

    private void showServiceCatalogWindow() {
        Stage serviceStage = new Stage();
        serviceStage.initModality(Modality.APPLICATION_MODAL);
        serviceStage.setTitle("Manage Service Catalog");
        serviceStage.setWidth(800);
        serviceStage.setHeight(600);
        
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Service Catalog Management");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Create TreeView for service hierarchy
        TreeView<String> serviceTreeView = new TreeView<>();
        TreeItem<String> rootNode = new TreeItem<>("Services");
        rootNode.setExpanded(true);
        serviceTreeView.setRoot(rootNode);
        serviceTreeView.setShowRoot(false);
        serviceTreeView.setPrefHeight(300);
        
        // Populate tree view
        populateServiceTreeView(rootNode);
        
        // Control fields
        VBox controlsBox = new VBox(10);
        
        HBox categoryBox = new HBox(10);
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category name...");
        Button addCategoryBtn = new Button("Add Category");
        Button editCategoryBtn = new Button("Edit Category");
        Button deleteCategoryBtn = new Button("Delete Category");
        categoryBox.getChildren().addAll(new Label("Category:"), categoryField, addCategoryBtn, editCategoryBtn, deleteCategoryBtn);
        
        HBox serviceBox = new HBox(10);
        TextField serviceField = new TextField();
        serviceField.setPromptText("Service name...");
        Button addServiceBtn = new Button("Add Service");
        Button editServiceBtn = new Button("Edit Service");
        Button deleteServiceBtn = new Button("Delete Service");
        serviceBox.getChildren().addAll(new Label("Service:"), serviceField, addServiceBtn, editServiceBtn, deleteServiceBtn);
        
        HBox itemBox = new HBox(10);
        TextField itemNameField = new TextField();
        itemNameField.setPromptText("Item name...");
        TextField itemPriceField = new TextField();
        itemPriceField.setPromptText("Price...");
        Button addItemBtn = new Button("Add Item");
        Button editItemBtn = new Button("Edit Item");
        Button deleteItemBtn = new Button("Delete Item");
        itemBox.getChildren().addAll(new Label("Item:"), itemNameField, new Label("Price:"), itemPriceField, addItemBtn, editItemBtn, deleteItemBtn);
        
        controlsBox.getChildren().addAll(categoryBox, serviceBox, itemBox);
        
        // Selection handler
        serviceTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int level = getTreeLevel(newVal);
                switch (level) {
                    case 1: // Category
                        categoryField.setText(newVal.getValue());
                        serviceField.clear();
                        itemNameField.clear();
                        itemPriceField.clear();
                        break;
                    case 2: // Service
                        categoryField.setText(newVal.getParent().getValue());
                        serviceField.setText(newVal.getValue());
                        itemNameField.clear();
                        itemPriceField.clear();
                        break;
                    case 3: // Item
                        TreeItem<String> serviceNode = newVal.getParent();
                        TreeItem<String> categoryNode = serviceNode.getParent();
                        categoryField.setText(categoryNode.getValue());
                        serviceField.setText(serviceNode.getValue());
                        String itemText = newVal.getValue();
                        String[] parts = itemText.split(" \\(₹");
                        itemNameField.setText(parts[0]);
                        if (parts.length > 1) {
                            itemPriceField.setText(parts[1].replace(")", ""));
                        }
                        break;
                }
            }
        });

        // Button actions
        addCategoryBtn.setOnAction(e -> {
            String name = categoryField.getText().trim();
            if (!name.isEmpty()) {
                ServiceCatalogStore.CatalogCategory newCategory = new ServiceCatalogStore.CatalogCategory(name);
                serviceCatalog.categories.add(newCategory);
                saveServiceCatalog();
                populateServiceTreeView(rootNode);
                populateServiceDataFromCatalog();
                showAlert("Category added successfully!");
            }
        });

        addServiceBtn.setOnAction(e -> {
            String categoryName = categoryField.getText().trim();
            String serviceName = serviceField.getText().trim();
            if (!categoryName.isEmpty() && !serviceName.isEmpty()) {
                ServiceCatalogStore.CatalogCategory category = findCategory(categoryName);
                if (category != null) {
                    ServiceCatalogStore.CatalogService newService = new ServiceCatalogStore.CatalogService(serviceName);
                    category.services.add(newService);
                    saveServiceCatalog();
                    populateServiceTreeView(rootNode);
                    populateServiceDataFromCatalog();
                    showAlert("Service added successfully!");
                }
            }
        });

        addItemBtn.setOnAction(e -> {
            String categoryName = categoryField.getText().trim();
            String serviceName = serviceField.getText().trim();
            String itemName = itemNameField.getText().trim();
            String priceText = itemPriceField.getText().trim();
            if (!categoryName.isEmpty() && !serviceName.isEmpty() && !itemName.isEmpty() && !priceText.isEmpty()) {
                try {
                    double price = Double.parseDouble(priceText);
                    ServiceCatalogStore.CatalogCategory category = findCategory(categoryName);
                    if (category != null) {
                        ServiceCatalogStore.CatalogService service = category.services.stream()
                            .filter(s -> s.name.equals(serviceName))
                            .findFirst().orElse(null);
                        if (service != null) {
                            ServiceCatalogStore.CatalogItem newItem = new ServiceCatalogStore.CatalogItem(itemName, price);
                            service.items.add(newItem);
                            saveServiceCatalog();
                            populateServiceTreeView(rootNode);
                            populateServiceDataFromCatalog();
                            showAlert("Item added successfully!");
                        }
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Please enter a valid price!");
                }
            }
        });
        
        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> serviceStage.close());
        
        mainLayout.getChildren().addAll(titleLabel, serviceTreeView, controlsBox, closeBtn);
        
        Scene scene = new Scene(mainLayout);
        serviceStage.setScene(scene);
        serviceStage.showAndWait();
    }

    private void populateServiceTreeView(TreeItem<String> rootNode) {
        rootNode.getChildren().clear();
        for (ServiceCatalogStore.CatalogCategory category : serviceCatalog.categories) {
            TreeItem<String> categoryNode = new TreeItem<>(category.name);
            categoryNode.setExpanded(false);
            
            for (ServiceCatalogStore.CatalogService service : category.services) {
                TreeItem<String> serviceNode = new TreeItem<>(service.name);
                serviceNode.setExpanded(false);
                
                for (ServiceCatalogStore.CatalogItem item : service.items) {
                    TreeItem<String> itemNode = new TreeItem<>(item.itemName + " (₹" + item.unitPrice + ")");
                    serviceNode.getChildren().add(itemNode);
                }
                
                categoryNode.getChildren().add(serviceNode);
            }
            
            rootNode.getChildren().add(categoryNode);
        }
    }

    private int getTreeLevel(TreeItem<String> item) {
        int level = 0;
        TreeItem<String> current = item;
        while (current.getParent() != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    private ServiceCatalogStore.CatalogCategory findCategory(String name) {
        return serviceCatalog.categories.stream()
            .filter(c -> c.name.equals(name))
            .findFirst().orElse(null);
    }

    private void saveServiceCatalog() {
        try {
            catalogStore.save(serviceCatalog);
        } catch (Exception e) {
            System.err.println("Error saving service catalog: " + e.getMessage());
        }
    }

    // ===== REFERENCE MANAGEMENT =====
    @FXML private void showAddReferenceModal() {
        showReferenceModal("Add Reference", null);
    }

    @FXML private void showEditReferenceModal() {
        Reference selected = referencesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showReferenceModal("Edit Reference", selected);
        } else {
            showAlert("Please select a reference to edit.");
        }
    }

    @FXML private void deleteReference() {
        Reference selected = referencesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showConfirmation("Delete Reference", "Are you sure you want to delete reference " + selected.getName() + "?")) {
                try {
                    // Delete from database if DAO is available and reference has DB ID
                    if (referenceSourceDAO != null && selected.getDbId() != null) {
                        referenceSourceDAO.deleteReferenceSource(selected.getDbId());
                    }
                    
                    referenceData.remove(selected);
                    commissionData.removeIf(c -> c.getSource().equals(selected.getName()));
                    showAlert("Reference deleted successfully!");
                } catch (Exception e) {
                    showAlert("Error deleting reference: " + e.getMessage());
                }
            }
        } else {
            showAlert("Please select a reference to delete.");
        }
    }

    // ===== USER MANAGEMENT =====
    @FXML private void showAddUserModal() {
        showUserModal("Add User", null);
    }

    @FXML private void showEditUserModal() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showUserModal("Edit User", selected);
        } else {
            showAlert("Please select a user to edit.");
        }
    }

    @FXML private void deleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showConfirmation("Delete User", "Are you sure you want to delete user " + selected.getUsername() + "?")) {
                try {
                    // Delete from database if DAO is available and user has DB ID
                    if (userDAO != null && selected.getDbId() != null) {
                        userDAO.deleteUser(selected.getDbId());
                    }
                    
                    userData.remove(selected);
                    showAlert("User deleted successfully!");
                } catch (Exception e) {
                    showAlert("Error deleting user: " + e.getMessage());
                }
            }
        } else {
            showAlert("Please select a user to delete.");
        }
    }

    // ===== INVOICE MANAGEMENT =====
    @FXML private void uploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Logo File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(uploadLogoBtn.getScene().getWindow());
        if (file != null) {
            currentInvoiceSettings.logoPath = file.getAbsolutePath();
            logoPreview.setText(file.getName());
        }
    }

    @FXML private void saveInvoiceSettings() {
        try {
            currentInvoiceSettings.headerText = headerTextArea.getText();
            currentInvoiceSettings.footerText = footerTextArea.getText();
            currentInvoiceSettings.templateStyle = templateStyleCombo.getValue();
            
            invoiceSettingsStore.save(currentInvoiceSettings);
            showAlert("Invoice settings saved successfully!");
        } catch (Exception e) {
            showAlert("Error saving invoice settings: " + e.getMessage());
        }
    }

    @FXML private void previewInvoice() {
        showAlert("Preview functionality will be implemented soon.");
    }

    // ===== MODAL DIALOGS =====
    private void showRoomModal(String title, Room room) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(title);
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField roomNoField = new TextField();
        ComboBox<String> typeCombo = new ComboBox<>(roomCategories);
        TextField floorField = new TextField();
        TextField bedsField = new TextField("1");
        CheckBox acCheckBox = new CheckBox();
        TextField priceField = new TextField();
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Available", "Occupied", "Maintenance", "Cleaning"));
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        
        if (room != null) {
            roomNoField.setText(room.getRoomNo());
            typeCombo.setValue(room.getType());
            floorField.setText(room.getFloor());
            acCheckBox.setSelected(room.getAcType().equals("AC"));
            priceField.setText(room.getPrice().replace("₹", "").replace(",", ""));
            statusCombo.setValue(room.getStatus());
        }
        
        grid.addRow(0, new Label("Room Number:"), roomNoField);
        grid.addRow(1, new Label("Type:"), typeCombo);
        grid.addRow(2, new Label("Floor:"), floorField);
        grid.addRow(3, new Label("Beds:"), bedsField);
        grid.addRow(4, new Label("AC:"), acCheckBox);
        grid.addRow(5, new Label("Price:"), priceField);
        grid.addRow(6, new Label("Status:"), statusCombo);
        grid.addRow(7, new Label("Description:"), descriptionArea);
        
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            try {
                // Validate input
                if (roomNoField.getText().trim().isEmpty() || 
                    typeCombo.getValue() == null || 
                    floorField.getText().trim().isEmpty() ||
                    priceField.getText().trim().isEmpty() ||
                    statusCombo.getValue() == null) {
                    showAlert("Please fill in all required fields.");
                    return;
                }
                
                if (room == null) {
                    // Create new room
                    application.models.Room dbRoom = new application.models.Room();
                    dbRoom.setRoomNo(roomNoField.getText().trim());
                    dbRoom.setRoomType(typeCombo.getValue());
                    dbRoom.setFloor(Integer.parseInt(floorField.getText().trim()));
                    dbRoom.setBeds(Integer.parseInt(bedsField.getText().trim()));
                    dbRoom.setAc(acCheckBox.isSelected());
                    dbRoom.setPrice(new BigDecimal(priceField.getText().trim()));
                    dbRoom.setStatus(statusCombo.getValue());
                    dbRoom.setDescription(descriptionArea.getText().trim());
                    
                    // Save to database
                    if (roomDAO != null) {
                        roomDAO.addRoom(dbRoom);
                    }
                    
                    // Add to UI
                    Room newUIRoom = new Room(
                        dbRoom.getRoomNo(),
                        dbRoom.getRoomType(),
                        String.valueOf(dbRoom.getFloor()),
                        dbRoom.getStatus(),
                        "₹" + String.format("%.2f", dbRoom.getPrice().doubleValue()),
                        dbRoom.isAc() ? "AC" : "Non-AC"
                    );
                    newUIRoom.setDbId(dbRoom.getId());
                    roomData.add(newUIRoom);
                    
                } else {
                    // Update existing room
                    application.models.Room dbRoom = new application.models.Room();
                    dbRoom.setId(room.getDbId());
                    dbRoom.setRoomNo(roomNoField.getText().trim());
                    dbRoom.setRoomType(typeCombo.getValue());
                    dbRoom.setFloor(Integer.parseInt(floorField.getText().trim()));
                    dbRoom.setBeds(Integer.parseInt(bedsField.getText().trim()));
                    dbRoom.setAc(acCheckBox.isSelected());
                    dbRoom.setPrice(new BigDecimal(priceField.getText().trim()));
                    dbRoom.setStatus(statusCombo.getValue());
                    dbRoom.setDescription(descriptionArea.getText().trim());
                    
                    // Update in database
                    if (roomDAO != null && room.getDbId() != null) {
                        roomDAO.updateRoom(dbRoom);
                    }
                    
                    // Update UI
                    room.setRoomNo(roomNoField.getText().trim());
                    room.setType(typeCombo.getValue());
                    room.setFloor(floorField.getText().trim());
                    room.setStatus(statusCombo.getValue());
                    room.setPrice("₹" + String.format("%.2f", Double.parseDouble(priceField.getText().trim())));
                    room.setAcType(acCheckBox.isSelected() ? "AC" : "Non-AC");
                    roomsTable.refresh();
                }
                
                modal.close();
                showAlert("Room saved successfully!");
                
            } catch (Exception ex) {
                showAlert("Error saving room: " + ex.getMessage());
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> modal.close());
        
        grid.addRow(8, saveBtn, cancelBtn);
        
        Scene scene = new Scene(grid, 500, 400);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void showReferenceModal(String title, Reference reference) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(title);
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField nameField = new TextField();
        TextField contactField = new TextField();
        TextField commissionPercentField = new TextField("0.00");
        TextField fixedAmountField = new TextField("0.00");
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);
        
        if (reference != null) {
            nameField.setText(reference.getName());
            // Try to find corresponding commission data
            Commission commission = commissionData.stream()
                .filter(c -> c.getSource().equals(reference.getName()))
                .findFirst().orElse(null);
            if (commission != null) {
                commissionPercentField.setText(commission.getPercent().replace("%", ""));
                fixedAmountField.setText(commission.getFixedAmount().replace("₹", "").replace(",", ""));
                notesArea.setText(commission.getNotes());
            }
        }
        
        grid.addRow(0, new Label("Source Name:"), nameField);
        grid.addRow(1, new Label("Contact:"), contactField);
        grid.addRow(2, new Label("Commission %:"), commissionPercentField);
        grid.addRow(3, new Label("Fixed Amount:"), fixedAmountField);
        grid.addRow(4, new Label("Notes:"), notesArea);
        
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            try {
                if (nameField.getText().trim().isEmpty()) {
                    showAlert("Please enter a source name.");
                    return;
                }
                
                if (reference == null) {
                    // Create new reference
                    ReferenceSource dbRef = new ReferenceSource();
                    dbRef.setSourceName(nameField.getText().trim());
                    dbRef.setContact(contactField.getText().trim());
                    dbRef.setCommissionPercentage(new BigDecimal(commissionPercentField.getText().trim()));
                    dbRef.setFixedAmount(new BigDecimal(fixedAmountField.getText().trim()));
                    dbRef.setNotes(notesArea.getText().trim());
                    
                    // Save to database
                    if (referenceSourceDAO != null) {
                        referenceSourceDAO.addReferenceSource(dbRef);
                    }
                    
                    // Add to UI
                    Reference newRef = new Reference(dbRef.getSourceName(), "Online");
                    newRef.setDbId(dbRef.getId());
                    referenceData.add(newRef);
                    
                    Commission newCommission = new Commission(
                        dbRef.getSourceName(),
                        String.format("%.2f%%", dbRef.getCommissionPercentage().doubleValue()),
                        "₹" + String.format("%.2f", dbRef.getFixedAmount().doubleValue()),
                        dbRef.getNotes() != null ? dbRef.getNotes() : ""
                    );
                    newCommission.setDbId(dbRef.getId());
                    commissionData.add(newCommission);
                    
                } else {
                    // Update existing reference
                    ReferenceSource dbRef = new ReferenceSource();
                    dbRef.setId(reference.getDbId());
                    dbRef.setSourceName(nameField.getText().trim());
                    dbRef.setContact(contactField.getText().trim());
                    dbRef.setCommissionPercentage(new BigDecimal(commissionPercentField.getText().trim()));
                    dbRef.setFixedAmount(new BigDecimal(fixedAmountField.getText().trim()));
                    dbRef.setNotes(notesArea.getText().trim());
                    
                    // Update in database (if DAO supports update - add method if needed)
                    // For now, delete and recreate
                    if (referenceSourceDAO != null && reference.getDbId() != null) {
                        referenceSourceDAO.deleteReferenceSource(reference.getDbId());
                        referenceSourceDAO.addReferenceSource(dbRef);
                    }
                    
                    // Update UI
                    reference.setName(nameField.getText().trim());
                    referencesTable.refresh();
                    
                    // Update commission
                    Commission commission = commissionData.stream()
                        .filter(c -> c.getSource().equals(reference.getName()))
                        .findFirst().orElse(null);
                    if (commission != null) {
                        commission.setPercent(String.format("%.2f%%", Double.parseDouble(commissionPercentField.getText().trim())));
                        commission.setFixedAmount("₹" + String.format("%.2f", Double.parseDouble(fixedAmountField.getText().trim())));
                        commission.setNotes(notesArea.getText().trim());
                        commissionTable.refresh();
                    }
                }
                
                modal.close();
                showAlert("Reference saved successfully!");
                
            } catch (Exception ex) {
                showAlert("Error saving reference: " + ex.getMessage());
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> modal.close());
        
        grid.addRow(5, saveBtn, cancelBtn);
        
        Scene scene = new Scene(grid, 400, 300);
        modal.setScene(scene);
        modal.showAndWait();
    }

   private void showUserModal(String title, User user) {
    Stage modal = new Stage();
    modal.initModality(Modality.APPLICATION_MODAL);
    modal.setTitle(title);
    
    GridPane grid = new GridPane();
    grid.setPadding(new Insets(20));
    grid.setHgap(10);
    grid.setVgap(10);
    
    TextField usernameField = new TextField();
    ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "MANAGER", "STAFF", "ACCOUNTANT"));
    TextField fullNameField = new TextField();
    TextField emailField = new TextField();
    ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
    PasswordField passwordField = new PasswordField();
    
    if (user != null) {
        usernameField.setText(user.getUsername());
        roleCombo.setValue(user.getRole());
        fullNameField.setText(user.getFullName() != null ? user.getFullName() : "");
        emailField.setText(user.getEmail());
        statusCombo.setValue(user.getStatus());
        passwordField.setPromptText("Leave empty to keep current password");
    }
    
    grid.addRow(0, new Label("Username:"), usernameField);
    grid.addRow(1, new Label("Role:"), roleCombo);
    grid.addRow(2, new Label("Full Name:"), fullNameField);
    grid.addRow(3, new Label("Email:"), emailField);
    grid.addRow(4, new Label("Status:"), statusCombo);
    
    if (user == null) {
        grid.addRow(5, new Label("Password:"), passwordField);
    } else {
        grid.addRow(5, new Label("New Password:"), passwordField);
    }
    
    Button saveBtn = new Button("Save");
    saveBtn.setOnAction(e -> {
        try {
            // Validate input
            if (usernameField.getText().trim().isEmpty() || 
                roleCombo.getValue() == null ||
                statusCombo.getValue() == null ||
                (user == null && passwordField.getText().trim().isEmpty())) {
                showAlert("Please fill in all required fields.");
                return;
            }
            
            if (user == null) {
                // Create new user
                application.models.User dbUser = new application.models.User();
                dbUser.setUsername(usernameField.getText().trim());
                dbUser.setPasswordHash(passwordField.getText()); // In real app, hash this!
                dbUser.setRole(roleCombo.getValue());
                dbUser.setFullName(fullNameField.getText().trim());
                dbUser.setEmail(emailField.getText().trim());
                dbUser.setStatus(statusCombo.getValue());
                
                // Save to database
                if (userDAO != null) {
                    userDAO.addUser(dbUser);
                }
                
                // Add to UI
                User newUIUser = new User(
                    dbUser.getUsername(),
                    dbUser.getRole(),
                    dbUser.getEmail(),
                    dbUser.getStatus(),
                    "Just created"
                );
                newUIUser.setDbId(dbUser.getId());
                newUIUser.setFullName(dbUser.getFullName());
                newUIUser.setPassword(dbUser.getPasswordHash());
                userData.add(newUIUser);
                
            } else {
                // Update existing user
                application.models.User dbUser = new application.models.User();
                dbUser.setId(user.getDbId());
                dbUser.setUsername(usernameField.getText().trim());
                
                // Handle password - only update if new password provided
                if (passwordField.getText().trim().isEmpty()) {
                    dbUser.setPasswordHash(user.getPassword()); // Keep existing password
                } else {
                    dbUser.setPasswordHash(passwordField.getText().trim()); // Use new password (should be hashed in real app)
                }
                
                dbUser.setRole(roleCombo.getValue());
                dbUser.setFullName(fullNameField.getText().trim());
                dbUser.setEmail(emailField.getText().trim());
                dbUser.setStatus(statusCombo.getValue());
                
                // Update in database
                if (userDAO != null && user.getDbId() != null) {
                    userDAO.updateUser(dbUser);
                }
                
                // Update UI
                user.setUsername(usernameField.getText().trim());
                user.setRole(roleCombo.getValue());
                user.setFullName(fullNameField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setStatus(statusCombo.getValue());
                
                // Update password in UI model if changed
                if (!passwordField.getText().trim().isEmpty()) {
                    user.setPassword(passwordField.getText().trim());
                }
                
                usersTable.refresh();
            }
            
            modal.close();
            showAlert("User saved successfully!");
            
        } catch (Exception ex) {
            showAlert("Error saving user: " + ex.getMessage());
            ex.printStackTrace();
        }
    });
    
    Button cancelBtn = new Button("Cancel");
    cancelBtn.setOnAction(e -> modal.close());
    
    int nextRow = 6;
    grid.addRow(nextRow, saveBtn, cancelBtn);
    
    Scene scene = new Scene(grid, 350, 320);
    modal.setScene(scene);
    modal.showAndWait();
}

    // ===== UTILITY METHODS =====
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ===== UI MODEL CLASSES =====
    public static class Room {
        private final StringProperty roomNo, type, floor, status, price, acType;
        private Long dbId; // Store database ID for updates

        public Room(String r, String t, String f, String s, String p, String a) {
            this.roomNo = new SimpleStringProperty(r);
            this.type = new SimpleStringProperty(t);
            this.floor = new SimpleStringProperty(f);
            this.status = new SimpleStringProperty(s);
            this.price = new SimpleStringProperty(p);
            this.acType = new SimpleStringProperty(a);
        }

        // Getters and setters
        public String getRoomNo() { return roomNo.get(); }
        public String getType() { return type.get(); }
        public String getFloor() { return floor.get(); }
        public String getStatus() { return status.get(); }
        public String getPrice() { return price.get(); }
        public String getAcType() { return acType.get(); }
        public void setRoomNo(String v) { roomNo.set(v); }
        public void setType(String v) { type.set(v); }
        public void setFloor(String v) { floor.set(v); }
        public void setStatus(String v) { status.set(v); }
        public void setPrice(String v) { price.set(v); }
        public void setAcType(String v) { acType.set(v); }
        public StringProperty roomNoProperty() { return roomNo; }
        public StringProperty typeProperty() { return type; }
        public StringProperty floorProperty() { return floor; }
        public StringProperty statusProperty() { return status; }
        public StringProperty priceProperty() { return price; }
        public StringProperty acTypeProperty() { return acType; }
        
        public Long getDbId() { return dbId; }
        public void setDbId(Long dbId) { this.dbId = dbId; }
    }

    public static class Service {
        private final StringProperty name, description, price, tax, available;

        public Service(String n, String d, String p, String t, String a) {
            this.name = new SimpleStringProperty(n);
            this.description = new SimpleStringProperty(d);
            this.price = new SimpleStringProperty(p);
            this.tax = new SimpleStringProperty(t);
            this.available = new SimpleStringProperty(a);
        }

        public String getName() { return name.get(); }
        public StringProperty nameProperty() { return name; }
        public StringProperty descriptionProperty() { return description; }
        public StringProperty priceProperty() { return price; }
        public StringProperty taxProperty() { return tax; }
        public StringProperty availableProperty() { return available; }
    }

    public static class Reference {
        private final StringProperty name, type;
        private Long dbId; // Store database ID for updates

        public Reference(String n, String t) {
            this.name = new SimpleStringProperty(n);
            this.type = new SimpleStringProperty(t);
        }

        public String getName() { return name.get(); }
        public String getType() { return type.get(); }
        public void setName(String v) { name.set(v); }
        public void setType(String v) { type.set(v); }
        public StringProperty nameProperty() { return name; }
        public StringProperty typeProperty() { return type; }
        
        public Long getDbId() { return dbId; }
        public void setDbId(Long dbId) { this.dbId = dbId; }
    }

    public static class Commission {
        private final StringProperty source, percent, fixedAmount, notes;
        private Long dbId; // Store database ID for updates

        public Commission(String s, String p, String f, String n) {
            this.source = new SimpleStringProperty(s);
            this.percent = new SimpleStringProperty(p);
            this.fixedAmount = new SimpleStringProperty(f);
            this.notes = new SimpleStringProperty(n);
        }

        public String getSource() { return source.get(); }
        public String getPercent() { return percent.get(); }
        public String getFixedAmount() { return fixedAmount.get(); }
        public String getNotes() { return notes.get(); }
        public void setPercent(String v) { percent.set(v); }
        public void setFixedAmount(String v) { fixedAmount.set(v); }
        public void setNotes(String v) { notes.set(v); }
        public StringProperty sourceProperty() { return source; }
        public StringProperty percentProperty() { return percent; }
        public StringProperty fixedAmountProperty() { return fixedAmount; }
        public StringProperty notesProperty() { return notes; }
        
        public Long getDbId() { return dbId; }
        public void setDbId(Long dbId) { this.dbId = dbId; }
    }

    public static class User {
        private final StringProperty username, role, email, status, lastLogin;
        private Long dbId; // Store database ID for updates
        private String fullName;
        private String password;

        public User(String u, String r, String e, String s, String l) {
            this.username = new SimpleStringProperty(u);
            this.role = new SimpleStringProperty(r);
            this.email = new SimpleStringProperty(e);
            this.status = new SimpleStringProperty(s);
            this.lastLogin = new SimpleStringProperty(l);
        }

        public String getUsername() { return username.get(); }
        public String getRole() { return role.get(); }
        public String getEmail() { return email.get(); }
        public String getStatus() { return status.get(); }
        public void setUsername(String v) { username.set(v); }
        public void setRole(String v) { role.set(v); }
        public void setEmail(String v) { email.set(v); }
        public void setStatus(String v) { status.set(v); }
        public StringProperty usernameProperty() { return username; }
        public StringProperty roleProperty() { return role; }
        public StringProperty emailProperty() { return email; }
        public StringProperty statusProperty() { return status; }
        public StringProperty lastLoginProperty() { return lastLogin; }
        
        public Long getDbId() { return dbId; }
        public void setDbId(Long dbId) { this.dbId = dbId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
