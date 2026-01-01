// src/main/java/application/controllers/HousekeepingController.java
package application.controllers;

import application.models.UserDetail;
import application.controllers.StaffDashboardController;
import application.enums.UserRole;
import application.services.dao.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HousekeepingController {

    @FXML private ComboBox<UserRole> userRoleComboBox;
    @FXML private ComboBox<UserDetail> userNameComboBox;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label loggedInUserLabel;
    
    @FXML private TabPane dashboardTabPane;
    @FXML private Tab managerDashboardTab;
    @FXML private Tab staffDashboardTab;

    @FXML
    public void initialize() {
        setupLoginControls();
        initializeEventHandlers();
        
        // Initially disable dashboard tabs
        dashboardTabPane.setVisible(false);
        managerDashboardTab.setDisable(true);
        staffDashboardTab.setDisable(true);
    }

    private void setupLoginControls() {
        userRoleComboBox.getItems().addAll(UserRole.MANAGER, UserRole.STAFF);
        userRoleComboBox.setPromptText("Select Role");
        
        userNameComboBox.setPromptText("Select Name");
        userNameComboBox.setDisable(true);
        
        passwordField.setPromptText("Enter Password");
        passwordField.setDisable(true);
        
        loggedInUserLabel.setText("Not Logged In");
        loggedInUserLabel.getStyleClass().add("not-logged-in-label");
    }

    private void initializeEventHandlers() {
        userRoleComboBox.valueProperty().addListener((obs, oldVal, newRole) -> {
            userNameComboBox.getItems().clear();
            userNameComboBox.setDisable(false);
            passwordField.clear();
            passwordField.setDisable(true);

            if (newRole != null) {
                loadUsersForRole(newRole);
            }

            userNameComboBox.getSelectionModel().clearSelection();
        });

        userNameComboBox.valueProperty().addListener((obs, oldVal, newUser) -> {
            passwordField.clear();
            passwordField.setDisable(newUser == null);
        });

        loginButton.setOnAction(event -> handleLogin());
    }

    private void loadUsersForRole(UserRole role) {
        try {
            List<UserDetail> users = getUsersFromDatabase(role);
            userNameComboBox.getItems().setAll(users);
            debugStaffData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }
 // Add this temporary debug method to HousekeepingController
    private void debugStaffData() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM staff");
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("=== ALL STAFF IN DATABASE ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("id"));
                System.out.println("Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
                System.out.println("Department: " + rs.getString("department"));
                System.out.println("Active: " + rs.getBoolean("is_active"));
                System.out.println("---");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<UserDetail> getUsersFromDatabase(UserRole role) throws Exception {
        List<UserDetail> users = new ArrayList<>();
        String roleStr = role.name();
        
        String sql;
        if (role == UserRole.MANAGER) {
            sql = "SELECT u.full_name, u.role FROM USERS u WHERE u.role = 'MANAGER' AND u.status = 'ACTIVE'";
        } else {
            sql = "SELECT s.first_name, s.last_name, s.role, s.department, s.floor " +
                  "FROM staff s WHERE s.is_active = true AND s.department = 'Housekeeping'";
        }
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                if (role == UserRole.MANAGER) {
                    String fullName = rs.getString("full_name");
                    users.add(new UserDetail(fullName, UserRole.MANAGER, "All Floors", "Day Shift"));
                } else {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String fullName = firstName + (lastName != null ? " " + lastName : "");
                    Integer floor = rs.getObject("floor", Integer.class);
                    String floorStr = floor != null ? "Floor " + floor : "All Floors";
                    
                    users.add(new UserDetail(fullName, UserRole.STAFF, floorStr, "Day Shift"));
                }
            }
        }
        
        return users;
    }

    private void handleLogin() {
        UserRole selectedRole = userRoleComboBox.getValue();
        UserDetail selectedUser = userNameComboBox.getValue();
        String enteredPassword = passwordField.getText();

        if (selectedRole == null || selectedUser == null || enteredPassword.isEmpty()) {
            showAlert("Login Error", "Please select a role, a name, and enter your password.");
            return;
        }

        // Simple password validation (in real app, use proper authentication)
        if (!validatePassword(selectedUser, enteredPassword)) {
            showAlert("Login Failed", "Incorrect password for " + selectedUser.getName() + ".");
            return;
        }

        // Login successful
        loggedInUserLabel.setText("Logged in as: " + selectedUser.getName());
        loggedInUserLabel.getStyleClass().remove("not-logged-in-label");
        loggedInUserLabel.getStyleClass().add("logged-in-label");

        // Disable login controls
        disableLoginControls();

        loadDashboard(selectedRole, selectedUser);
    }

    private boolean validatePassword(UserDetail user, String password) {
        // Simple validation - in real app, check against database hash
        if (user.getDesignation() == UserRole.MANAGER) {
            return "managerpass".equals(password);
        } else {
            return "staffpass".equals(password);
        }
    }

    private void disableLoginControls() {
        userRoleComboBox.setDisable(true);
        userNameComboBox.setDisable(true);
        passwordField.setDisable(true);
        loginButton.setDisable(true);
    }

    private void loadDashboard(UserRole role, UserDetail user) {
        try {
            String fxmlPath;
            Tab targetTab;

            if (role == UserRole.MANAGER) {
                fxmlPath = "/fxml1/manager-housekeeping-dashboard.fxml";
                targetTab = managerDashboardTab;
                staffDashboardTab.setDisable(true);
                managerDashboardTab.setDisable(false);
                dashboardTabPane.getSelectionModel().select(managerDashboardTab);
            } else {
                fxmlPath = "/fxml1/staff-housekeeping-dashboard.fxml";
                targetTab = staffDashboardTab;
                managerDashboardTab.setDisable(true);
                staffDashboardTab.setDisable(false);
                dashboardTabPane.getSelectionModel().select(staffDashboardTab);
            }

            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Dashboard FXML file not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent dashboardContent = loader.load();
            targetTab.setContent(dashboardContent);

            // Pass user details to controller
            if (role == UserRole.MANAGER) {
                ManagerDashboardController controller = loader.getController();
                controller.setManagerDetails(user);
            } else {
                StaffDashboardController controller = loader.getController();
                controller.setStaffDetails(user);
            }

            dashboardTabPane.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Dashboard Load Error", "Could not load the dashboard UI: " + e.getMessage());
            enableLoginControls();
        }
    }

    private void enableLoginControls() {
        userRoleComboBox.setDisable(false);
        userNameComboBox.setDisable(false);
        passwordField.setDisable(false);
        loginButton.setDisable(false);
        loggedInUserLabel.setText("Login Failed!");
        loggedInUserLabel.getStyleClass().remove("logged-in-label");
        loggedInUserLabel.getStyleClass().add("not-logged-in-label");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
