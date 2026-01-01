package application.utils.inventory;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utility class for showing various types of alerts and dialogs in the application
 */
public class AlertUtils {

    private static final String APP_TITLE = "Hotel Management System";
    private static String iconPath = "/images/hotel-icon.png"; // Optional: Set your app icon path

    /**
     * Shows a success alert dialog
     * @param title Alert title
     * @param message Alert message
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setupAlert(alert, title, message);
        alert.setHeaderText("Success");
        alert.showAndWait();
    }

    /**
     * Shows an information alert dialog
     * @param title Alert title
     * @param message Alert message
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setupAlert(alert, title, message);
        alert.setHeaderText("Information");
        alert.showAndWait();
    }

    /**
     * Shows a warning alert dialog
     * @param title Alert title
     * @param message Alert message
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        setupAlert(alert, title, message);
        alert.setHeaderText("Warning");
        alert.showAndWait();
    }

    /**
     * Shows an error alert dialog
     * @param title Alert title
     * @param message Alert message
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setupAlert(alert, title, message);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog and returns the user's choice
     * @param title Dialog title
     * @param message Dialog message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupAlert(alert, title, message);
        alert.setHeaderText("Confirmation");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a confirmation dialog with custom buttons
     * @param title Dialog title
     * @param message Dialog message
     * @param okButtonText Custom text for OK button
     * @param cancelButtonText Custom text for Cancel button
     * @return true if user clicked the OK button, false otherwise
     */
    public static boolean showConfirmation(String title, String message, String okButtonText, String cancelButtonText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupAlert(alert, title, message);
        alert.setHeaderText("Confirmation");
        
        // Create custom buttons
        ButtonType okButton = new ButtonType(okButtonText);
        ButtonType cancelButton = new ButtonType(cancelButtonText);
        alert.getButtonTypes().setAll(okButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == okButton;
    }

    /**
     * Shows a text input dialog
     * @param title Dialog title
     * @param headerText Dialog header text
     * @param contentText Dialog content text
     * @param defaultValue Default value in the input field
     * @return User input as Optional<String>
     */
    public static Optional<String> showTextInputDialog(String title, String headerText, String contentText, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        
        // Set icon if available
        try {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            if (iconPath != null) {
                stage.getIcons().add(new Image(AlertUtils.class.getResourceAsStream(iconPath)));
            }
        } catch (Exception e) {
            // Ignore if icon not found
        }
        
        return dialog.showAndWait();
    }

    /**
     * Shows a text input dialog without default value
     * @param title Dialog title
     * @param headerText Dialog header text
     * @param contentText Dialog content text
     * @return User input as Optional<String>
     */
    public static Optional<String> showTextInputDialog(String title, String headerText, String contentText) {
        return showTextInputDialog(title, headerText, contentText, "");
    }

    /**
     * Shows a custom alert with specified alert type
     * @param alertType Type of alert (INFO, WARNING, ERROR, CONFIRMATION)
     * @param title Alert title
     * @param headerText Alert header text
     * @param message Alert message
     * @return Optional<ButtonType> for confirmation dialogs, empty for others
     */
    public static Optional<ButtonType> showCustomAlert(Alert.AlertType alertType, String title, String headerText, String message) {
        Alert alert = new Alert(alertType);
        setupAlert(alert, title, message);
        alert.setHeaderText(headerText);
        return alert.showAndWait();
    }

    /**
     * Shows an exception dialog with detailed error information
     * @param title Dialog title
     * @param message Main error message
     * @param exception The exception that occurred
     */
    public static void showExceptionDialog(String title, String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setupAlert(alert, title, message);
        alert.setHeaderText("An Exception Occurred");
        
        // Create expandable content
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();
        
        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        javafx.scene.layout.GridPane gridPane = new javafx.scene.layout.GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(new javafx.scene.control.Label("Exception Details:"), 0, 0);
        gridPane.add(textArea, 0, 1);
        
        alert.getDialogPane().setExpandableContent(gridPane);
        alert.showAndWait();
    }

    /**
     * Shows a progress dialog (information dialog that can be used for progress updates)
     * @param title Dialog title
     * @param message Dialog message
     * @return The Alert object for further customization
     */
    public static Alert showProgressDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setupAlert(alert, title, message);
        alert.setHeaderText("Processing...");
        alert.getButtonTypes().clear(); // Remove default OK button
        alert.show(); // Show non-modal
        return alert;
    }

    /**
     * Common setup for all alert dialogs
     * @param alert The alert to setup
     * @param title Alert title
     * @param message Alert message
     */
    private static void setupAlert(Alert alert, String title, String message) {
        alert.setTitle(title);
        alert.setContentText(message);
        
        // Set application icon if available
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            if (iconPath != null) {
                stage.getIcons().add(new Image(AlertUtils.class.getResourceAsStream(iconPath)));
            }
        } catch (Exception e) {
            // Ignore if icon not found
        }
        
        // Make dialog resizable
        alert.getDialogPane().setMinWidth(350);
        alert.setResizable(true);
    }

    /**
     * Sets the icon path for all dialogs
     * @param path Path to the icon resource
     */
    public static void setIconPath(String path) {
        iconPath = path;
    }

    /**
     * Quick success message for save operations
     * @param entityName Name of the entity that was saved (e.g., "Item", "Purchase Order")
     */
    public static void showSaveSuccess(String entityName) {
        showSuccess("Save Successful", entityName + " has been saved successfully.");
    }

    /**
     * Quick success message for delete operations
     * @param entityName Name of the entity that was deleted
     */
    public static void showDeleteSuccess(String entityName) {
        showSuccess("Delete Successful", entityName + " has been deleted successfully.");
    }

    /**
     * Quick success message for update operations
     * @param entityName Name of the entity that was updated
     */
    public static void showUpdateSuccess(String entityName) {
        showSuccess("Update Successful", entityName + " has been updated successfully.");
    }

    /**
     * Quick error message for save operations
     * @param entityName Name of the entity that failed to save
     * @param error Error message
     */
    public static void showSaveError(String entityName, String error) {
        showError("Save Failed", "Failed to save " + entityName + ": " + error);
    }

    /**
     * Quick error message for delete operations
     * @param entityName Name of the entity that failed to delete
     * @param error Error message
     */
    public static void showDeleteError(String entityName, String error) {
        showError("Delete Failed", "Failed to delete " + entityName + ": " + error);
    }

    /**
     * Quick error message for load operations
     * @param entityName Name of the entity that failed to load
     * @param error Error message
     */
    public static void showLoadError(String entityName, String error) {
        showError("Load Failed", "Failed to load " + entityName + ": " + error);
    }

    /**
     * Shows validation error with list of validation issues
     * @param validationErrors List of validation error messages
     */
    public static void showValidationErrors(java.util.List<String> validationErrors) {
        if (validationErrors == null || validationErrors.isEmpty()) {
            return;
        }
        
        StringBuilder message = new StringBuilder("Please fix the following issues:\n\n");
        for (int i = 0; i < validationErrors.size(); i++) {
            message.append((i + 1)).append(". ").append(validationErrors.get(i)).append("\n");
        }
        
        showWarning("Validation Error", message.toString());
    }

    /**
     * Shows a confirmation for deletion with entity name
     * @param entityName Name of the entity to delete
     * @param entityDescription Description or identifier of the specific entity
     * @return true if confirmed, false otherwise
     */
    public static boolean confirmDelete(String entityName, String entityDescription) {
        return showConfirmation(
            "Confirm Delete", 
            "Are you sure you want to delete " + entityName + " '" + entityDescription + "'?\n\nThis action cannot be undone.",
            "Delete",
            "Cancel"
        );
    }

    /**
     * Shows a low stock alert
     * @param itemName Name of the item with low stock
     * @param currentQuantity Current stock quantity
     * @param minQuantity Minimum stock level
     */
    public static void showLowStockAlert(String itemName, int currentQuantity, int minQuantity) {
        showWarning("Low Stock Alert", 
            String.format("Item '%s' is running low on stock.\n\nCurrent Quantity: %d\nMinimum Level: %d\n\nPlease consider reordering.", 
                itemName, currentQuantity, minQuantity));
    }

    /**
     * Shows an out of stock alert
     * @param itemName Name of the item that is out of stock
     */
    public static void showOutOfStockAlert(String itemName) {
        showError("Out of Stock", 
            String.format("Item '%s' is out of stock.\n\nPlease reorder immediately.", itemName));
    }
}
