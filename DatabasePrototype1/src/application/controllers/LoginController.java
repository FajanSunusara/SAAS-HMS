package application.controllers;

import application.services.AuthService;
import application.services.AuthContext;
import application.services.dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorMessageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null) username = "";
        if (password == null) password = "";

        if (username.isEmpty() || password.isEmpty()) {
            errorMessageLabel.setText("Username and password cannot be empty.");
            errorMessageLabel.setVisible(true);
            return;
        }

        System.out.println("username = " + username);
        System.out.println("password = " + password);

        if (authService.authenticateUser(username, password)) {
            try {
                // Load full user to know role and set into AuthContext
                var user = new UserDAO().findByUsername(username);
                AuthContext.setCurrentUser(user);

                // Navigate to Home
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/home.fxml"));
                Parent root = loader.load();
                stage.setScene(new Scene(root));
                stage.setTitle("Hotel Management System - Home");
                stage.setMaximized(true);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load the main application window.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Login Error", "Unable to start session.");
            }
        } else {
            errorMessageLabel.setText("Invalid username or password.");
            errorMessageLabel.setVisible(true);
        }
    }

    @FXML
    private void handleSignupLink(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Sign Up", "Sign up functionality is not yet implemented.");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
