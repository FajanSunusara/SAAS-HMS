package application.utils;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;

import java.io.File;
import java.util.Optional;

public class SimpleCameraCapture {
    
    public static Image captureImage(Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(parentStage);
        if (selectedFile != null) {
            return WebcamCapture.loadImage(selectedFile);
        }
        
        return null;
    }
    
    public static Image captureFromCamera(Stage parentStage) {
        // Show option dialog for camera or file
        Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
        choiceDialog.setTitle("Capture Photo");
        choiceDialog.setHeaderText("How would you like to capture the photo?");
        choiceDialog.setContentText("Choose your preferred method:");
        
        ButtonType cameraButton = new ButtonType("📷 Use Camera");
        ButtonType fileButton = new ButtonType("📁 Select File");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        choiceDialog.getButtonTypes().setAll(cameraButton, fileButton, cancelButton);
        
        Optional<ButtonType> result = choiceDialog.showAndWait();
        
        if (result.isPresent()) {
            if (result.get() == cameraButton) {
                // Try to use real camera first
                Image cameraImage = tryRealCameraCapture(parentStage);
                if (cameraImage != null) {
                    return cameraImage;
                }
                // Fallback to simulation
                return simulateCameraCapture(parentStage);
            } else if (result.get() == fileButton) {
                return captureImage(parentStage);
            }
        }
        
        return null;
    }
    
    private static Image tryRealCameraCapture(Stage parentStage) {
        try {
            // Try to use webcam-capture library
            return attemptWebcamCapture();
        } catch (Exception e) {
            System.out.println("Real camera capture failed: " + e.getMessage());
            return null;
        }
    }
    
    private static Image attemptWebcamCapture() {
        try {
            // Check if webcam-capture classes are available
            Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            Class<?> openClass = Class.forName("com.github.sarxos.webcam.Webcam");
            
            // Get default webcam
            Object webcam = webcamClass.getMethod("getDefault").invoke(null);
            if (webcam == null) {
                System.out.println("No webcam found");
                return null;
            }
            
            // Open webcam
            webcamClass.getMethod("open").invoke(webcam);
            
            // Capture image
            Object awtImage = webcamClass.getMethod("getImage").invoke(webcam);
            
            // Close webcam
            webcamClass.getMethod("close").invoke(webcam);
            
            // Convert AWT Image to JavaFX Image
            return convertAwtToFx(awtImage);
            
        } catch (Exception e) {
            System.out.println("Webcam capture attempt failed: " + e.getMessage());
            return null;
        }
    }
    
    private static Image convertAwtToFx(Object awtImage) {
        try {
            // Convert java.awt.Image to JavaFX Image
            java.awt.image.BufferedImage bufferedImage = (java.awt.image.BufferedImage) awtImage;
            return javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            System.out.println("Image conversion failed: " + e.getMessage());
            return null;
        }
    }
    
    private static Image simulateCameraCapture(Stage parentStage) {
        // Show a simulated camera capture dialog
        Alert cameraDialog = new Alert(Alert.AlertType.INFORMATION);
        cameraDialog.setTitle("Camera Capture");
        cameraDialog.setHeaderText("📸 Camera Simulation");
        cameraDialog.setContentText("In a real implementation, this would open your camera.\n\nFor now, we'll use a simulated photo capture.\nClick OK to continue.");
        
        Optional<ButtonType> result = cameraDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return WebcamCapture.createCapturedPhoto();
        }
        
        return null;
    }
}