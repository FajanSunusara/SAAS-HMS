package application.controllers;

import application.services.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleSignup() {
        String username = value(usernameField);
        String fullName = value(fullNameField);
        String email = value(emailField);
        String password = value(passwordField);
        String confirmPassword = value(confirmPasswordField);

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password and confirm password do not match.");
            return;
        }

        try {
            boolean created = authService.registerUser(username, password, "USER", fullName, email);
            if (created) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully! You can now log in.");
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Signup Failed", "Username already exists. Please choose a different one.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while creating the account.");
        }
    }

    @FXML
    private void handleLoginLink() {
        // Simply close the signup window to return to login
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private String value(TextField tf) {
        return tf.getText() == null ? "" : tf.getText().trim();
    }

    private String value(PasswordField pf) {
        return pf.getText() == null ? "" : pf.getText();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
