package application.controllers;

import application.enums.SidebarItem;
import application.services.AuthContext;
import application.utils.PropertyReader;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class SidebarController {

    private static final double SIDEBAR_COLLAPSED_WIDTH = 70.0;
    private static final double SIDEBAR_EXPANDED_WIDTH = 250.0;
    private static final Duration ANIMATION_DURATION = Duration.millis(300);

    @FXML private VBox sidebar;
    @FXML private Button hamburgerButton;
    @FXML private ImageView hamburgerIconView;

    @FXML private Button homeButton;
    @FXML private Button bookingButton;
    @FXML private Button checkoutButton;
    @FXML private Button reservationButton;
    @FXML private Button invoiceButton;
    @FXML private Button housekeepingButton;
    @FXML private Button guestManagementButton;
    @FXML private Button staffManagementButton;
    @FXML private Button reportsButton;
    @FXML private Button servicesButton;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;
    @FXML private Button inventoryButton;
    @FXML private Button financesButton;
    @FXML private Button laundryButton;
    @FXML private Button channelManagerButton;
    @FXML private Button RestauentButton;


    private boolean isCollapsed = true;

    private final Map<SidebarItem, Button> sidebarButtons = new HashMap<>();
    private final Map<Button, Tooltip> buttonTooltips = new HashMap<>();
    private final Map<SidebarItem, ImageView> sidebarIconViews = new HashMap<>();

    private Button activeButton;
    private final Deque<SidebarItem> navigationStack = new ArrayDeque<>();
    private SidebarItem currentView = null;
    private static final String PAGE_ACCESS_PREFIX = "page.access.";

    @FXML
    public void initialize() {
        System.out.println("SidebarController.initialize() called.");

        populateSidebarButtons();
        applyRoleVisibility();
        setupHamburgerIcon();

        // Initial state collapsed
        sidebar.setPrefWidth(SIDEBAR_COLLAPSED_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_COLLAPSED_WIDTH);
        if (!sidebar.getStyleClass().contains("collapsed")) {
            sidebar.getStyleClass().add("collapsed");
        }
        isCollapsed = true;
        updateButtonTextVisibility();

        // Optional: auto-compact for short screens
        // Platform.runLater(() -> {
        //     Stage stage = (Stage) sidebar.getScene().getWindow();
        //     stage.heightProperty().addListener((obs, ov, nv) -> {
        //         if (nv.doubleValue() < 720) {
        //             if (!sidebar.getStyleClass().contains("compact")) sidebar.getStyleClass().add("compact");
        //         } else {
        //             sidebar.getStyleClass().remove("compact");
        //         }
        //     });
        // });
    }

    private void setupHamburgerIcon() {
        if (hamburgerIconView != null) {
            try {
                Image hamburgerImage = new Image(getClass().getResourceAsStream("/icons/icons8-menu-48.png"));
                if (hamburgerImage == null || hamburgerImage.isError()
                        || (hamburgerImage.getWidth() == 0 && hamburgerImage.getHeight() == 0)) {
                    hamburgerIconView.setImage(createPlaceholderImage());
                } else {
                    hamburgerIconView.setImage(hamburgerImage);
                }
                hamburgerIconView.setFitWidth(32);
                hamburgerIconView.setFitHeight(32);
            } catch (Exception e) {
                e.printStackTrace();
                hamburgerIconView.setImage(createPlaceholderImage());
            }
        }
    }
    
    private boolean isPageAccessAllowed(SidebarItem item) {
        // Check role-based access first
        String role = AuthContext.getCurrentRole();
        Set<SidebarItem> allowedForRole = allowedItemsForRole(role);
        if (!allowedForRole.contains(item)) {
            return false;
        }
        // Check configured page access from properties
        String key = PAGE_ACCESS_PREFIX + item.name();
        return PropertyReader.getInstance().getBooleanProperty(key, true);
    }


    private void populateSidebarButtons() {
        addButton(SidebarItem.HOME, homeButton, "/icons/icons8-home-24.png");
        addButton(SidebarItem.BOOKING, bookingButton, "/icons/icons8-calendar-50.png");
        addButton(SidebarItem.CHECKOUT, checkoutButton, "/icons/icons8-to-go-50.png");
        addButton(SidebarItem.RESERVATION, reservationButton, "/icons/icons8-clipboard-approve-32.png");
        addButton(SidebarItem.INVOICE, invoiceButton, "/icons/icons8-cash-receipt-48.png");
        addButton(SidebarItem.HOUSEKEEPING, housekeepingButton, "/icons/icons8-broom-32.png");
        addButton(SidebarItem.GUEST_MANAGEMENT, guestManagementButton, "/icons/icons8-people-64.png");
        addButton(SidebarItem.STAFF_MANAGEMENT, staffManagementButton, "/icons/icons8-employee-50.png");
        addButton(SidebarItem.REPORTS, reportsButton, "/icons/icons8-combo-chart-50.png");
        addButton(SidebarItem.Inventory, inventoryButton, "/icons/icons8-calendar-50.png");
        addButton(SidebarItem.SERVICES, servicesButton, "/icons/icons8-service-bell-50.png");
        addButton(SidebarItem.SETTINGS, settingsButton, "/icons/icons8-gear-64.png");
        addButton(SidebarItem.Inventory, inventoryButton, "/icons/icons8-inventory-50.png");
        addButton(SidebarItem.FINANCES, financesButton, "/icons/icons8-finances-64.png");
        addButton(SidebarItem.LAUNDRY, laundryButton, "/icons/icons8-laundry-bag-50.png");
        addButton(SidebarItem.EMAILS, channelManagerButton, "/icons/icons8-email-50.png");
        addButton(SidebarItem.RESTAURENT, RestauentButton, "/icons/icons8-chef-50.png");
        addButton(SidebarItem.LOGOUT, logoutButton, "/icons/icons8-logout-rounded-50.png");
    }

    private void addButton(SidebarItem item, Button button, String imagePath) {
        if (button == null) {
            System.err.println("Error: Button for " + item.getName() + " is null (fx:id mismatch?).");
            return;
        }
        sidebarButtons.put(item, button);
        button.setOnAction(event -> handleSidebarButtonClick(item));

        ImageView iconView = new ImageView();
        iconView.setFitWidth(24);
        iconView.setFitHeight(24);
        try {
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl == null) {
                System.err.println("ERROR: Icon not found for " + item.getName() + " at " + imagePath);
                iconView.setImage(createPlaceholderImage());
            } else {
                Image iconImage = new Image(imageUrl.toExternalForm());
                if (iconImage.isError() || (iconImage.getWidth() == 0 && iconImage.getHeight() == 0)) {
                    iconView.setImage(createPlaceholderImage());
                } else {
                    iconView.setImage(iconImage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            iconView.setImage(createPlaceholderImage());
        }

        button.setGraphic(iconView);
        sidebarIconViews.put(item, iconView);

        // collapsed text hidden; expanded shows name
        button.setText("");
        Tooltip tooltip = new Tooltip(item.getName());
        buttonTooltips.put(button, tooltip);
        button.getStyleClass().add("sidebar-button");
    }

    private Image createPlaceholderImage() {
        // 24x24 transparent image as placeholder
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAAF0lEQVRIiWNgGAWjYBSMglEwCkb9DwYAAD2q1Jbq3lC/AAAAAElFTkSuQmCC");
    }

    @FXML
    private void toggleSidebar() {
        isCollapsed = !isCollapsed;
        double targetWidth = isCollapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH;

        if (isCollapsed) {
            if (!sidebar.getStyleClass().contains("collapsed")) sidebar.getStyleClass().add("collapsed");
        } else {
            sidebar.getStyleClass().remove("collapsed");
        }

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(ANIMATION_DURATION,
                        new KeyValue(sidebar.prefWidthProperty(), targetWidth),
                        new KeyValue(sidebar.maxWidthProperty(), targetWidth)
                )
        );
        timeline.setOnFinished(event -> {
            sidebar.setPrefWidth(targetWidth);
            sidebar.setMaxWidth(targetWidth);
            updateButtonTextVisibility();
        });
        timeline.play();
    }

    private void updateButtonTextVisibility() {
        for (Map.Entry<SidebarItem, Button> entry : sidebarButtons.entrySet()) {
            SidebarItem item = entry.getKey();
            Button button = entry.getValue();
            Tooltip tooltip = buttonTooltips.get(button);
            ImageView iconView = sidebarIconViews.get(item);

            if (button == null) continue;

            if (isCollapsed) {
                button.setText("");
                button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                if (iconView != null) {
                    iconView.setFitWidth(28);
                    iconView.setFitHeight(28);
                }
                if (tooltip != null) Tooltip.install(button, tooltip);
            } else {
                button.setText(item.getName());
                button.setContentDisplay(ContentDisplay.LEFT);
                if (iconView != null) {
                    iconView.setFitWidth(24);
                    iconView.setFitHeight(24);
                }
                if (tooltip != null) Tooltip.uninstall(button, tooltip);
            }
        }

        // Adjust hamburger icon as well
        if (hamburgerIconView != null) {
            if (isCollapsed) {
                hamburgerIconView.setFitWidth(32);
                hamburgerIconView.setFitHeight(32);
            } else {
                hamburgerIconView.setFitWidth(28);
                hamburgerIconView.setFitHeight(28);
            }
        }
    }

    private Set<SidebarItem> allowedItemsForRole(String role) {
        if (role == null) return Set.of(); // no access
        String r = role.trim().toUpperCase(Locale.ROOT);

        if ("ADMIN".equals(r)) {
            return EnumSet.allOf(SidebarItem.class);
        }
        if ("RECEPTIONIST".equals(r)) {
            return EnumSet.of(
                    SidebarItem.HOME,
                    SidebarItem.BOOKING,
                    SidebarItem.RESERVATION,
                    SidebarItem.CHECKOUT,
                    SidebarItem.SERVICES,
                    SidebarItem.LOGOUT
            );
        }
        if ("MANAGER".equals(r)) {
            return EnumSet.of(
                    SidebarItem.HOUSEKEEPING,
                    SidebarItem.STAFF_MANAGEMENT,
                    SidebarItem.HOME,
                    SidebarItem.LOGOUT
            );
        }
        if ("STAFF".equals(r)) {
            return EnumSet.of(
                    SidebarItem.HOUSEKEEPING,
                    SidebarItem.LOGOUT
            );
        }
        // default minimal
        return EnumSet.of(SidebarItem.LOGOUT);
    }

    private void applyRoleVisibility() {
        String role = AuthContext.getCurrentRole();
        Set<SidebarItem> allowed = allowedItemsForRole(role);

        for (Map.Entry<SidebarItem, Button> e : sidebarButtons.entrySet()) {
            SidebarItem item = e.getKey();
            Button btn = e.getValue();
            if (btn != null) {
                boolean visible = allowed.contains(item);
                btn.setManaged(visible);
                btn.setVisible(visible);
            }
        }
    }

    private void handleSidebarButtonClick(SidebarItem clickedItem) {
        // Guard unauthorized access
        String role = AuthContext.getCurrentRole();
        Set<SidebarItem> allowed = allowedItemsForRole(role);
        if (!isPageAccessAllowed(clickedItem)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Access Denied");
            alert.setHeaderText(null);
            alert.setContentText("Access to " + clickedItem.getName() + " is disabled. Please contact administrator.");
            alert.showAndWait();
            return;
        }
        if (clickedItem == SidebarItem.LOGOUT) {
            performLogout();
            return;
        }

        if (clickedItem.getFxmlPath() != null) {
            if (currentView != null && clickedItem != currentView) {
                navigationStack.push(currentView);
            }
            loadNewScene(clickedItem);
        } else {
            System.out.println("No FXML path defined for " + clickedItem.getName() + " (action item).");
        }
    }

    private void loadNewScene(SidebarItem itemToLoad) {
        System.out.println("SidebarController: Loading " + itemToLoad.getName() + " from " + itemToLoad.getFxmlPath());
        try {
            URL fxmlUrl = getClass().getResource(itemToLoad.getFxmlPath());
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + itemToLoad.getFxmlPath());
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent newRoot = loader.load();
            Stage currentStage = (Stage) sidebar.getScene().getWindow();
            Scene newScene = new Scene(newRoot, currentStage.getWidth(), currentStage.getHeight());
            newScene.getStylesheets().addAll(
                    getClass().getResource("/css/app.css").toExternalForm(),
                    getClass().getResource("/css/sidebar.css").toExternalForm()
            );
            currentStage.setScene(newScene);
            currentStage.setTitle("Hotel Management - " + itemToLoad.getName());
            currentStage.show();
            currentView = itemToLoad;

            setActiveButton(sidebarButtons.get(itemToLoad));
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not load page: " + itemToLoad.getName());
            alert.setContentText("Details: " + e.getMessage() + "\nEnsure FXML file exists and is valid.");
            alert.showAndWait();
        }
    }

    private void performLogout() {
        System.out.println("Performing logout action...");
        try {
            URL loginFxmlUrl = getClass().getResource("/fxml1/Login.fxml");
            if (loginFxmlUrl == null) {
                System.err.println("Error: Login.fxml not found at /fxml1/Login.fxml for logout!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(loginFxmlUrl);
            Parent loginView = loader.load();
            Scene scene = new Scene(loginView);
            Stage currentStage = (Stage) sidebar.getScene().getWindow();
            scene.getStylesheets().addAll(
                    getClass().getResource("/css/app.css").toExternalForm(),
                    getClass().getResource("/css/sidebar.css").toExternalForm()
            );
            currentStage.setScene(scene);
            currentStage.setTitle("Login");
            currentStage.centerOnScreen();
//            currentStage.setMaximized(false);
        } catch (IOException e) {
            System.err.println("Error loading Login.fxml for logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button newActiveButton) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active-sidebar-button");
        }
        activeButton = newActiveButton;
        if (activeButton != null) {
            if (!activeButton.getStyleClass().contains("active-sidebar-button")) {
                activeButton.getStyleClass().add("active-sidebar-button");
            }
        }
    }
}
