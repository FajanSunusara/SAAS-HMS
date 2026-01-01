package application.controllers;

import application.utils.WebcamCapture;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class CameraCaptureDialog implements Initializable {

    @FXML private ComboBox<String> cameraSelector;
    @FXML private ImageView cameraPreview;
    @FXML private Button captureButton;
    @FXML private Button retakeButton;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;

    private Image capturedImage;
    private Stage stage;
    private File outputFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCameraSelector();
        setupEventHandlers();
        startCameraPreview();
    }

    private void setupCameraSelector() {
        cameraSelector.getItems().addAll(WebcamCapture.getAvailableCameras());
        if (!cameraSelector.getItems().isEmpty()) {
            cameraSelector.setValue(cameraSelector.getItems().get(0));
        }
    }

    private void setupEventHandlers() {
        captureButton.setOnAction(e -> captureImage());
        retakeButton.setOnAction(e -> retakeImage());
        saveButton.setOnAction(e -> saveImage());
        
        // Camera selector change
        cameraSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            restartCameraPreview();
        });
    }

    private void startCameraPreview() {
        // Start with a placeholder
        cameraPreview.setImage(WebcamCapture.createPlaceholderImage());
        
        // Simulate live preview with periodic updates
        Thread previewThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Simulate live preview - in real implementation, this would be actual camera feed
                    if (capturedImage == null) {
                        Image previewImage = createLivePreviewImage();
                        javafx.application.Platform.runLater(() -> {
                            cameraPreview.setImage(previewImage);
                        });
                    }
                    Thread.sleep(200); // 5 FPS for demo
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        previewThread.setDaemon(true);
        previewThread.start();
    }

    private Image createLivePreviewImage() {
        // Create a dynamic preview image
        return WebcamCapture.createPlaceholderImage();
    }

    private void restartCameraPreview() {
        capturedImage = null;
        retakeButton.setDisable(true);
        saveButton.setDisable(true);
        captureButton.setDisable(false);
        statusLabel.setText("Camera: " + cameraSelector.getValue());
    }

    @FXML
    private void captureImage() {
        capturedImage = WebcamCapture.captureImage();
        cameraPreview.setImage(capturedImage);
        
        // Update UI state
        captureButton.setDisable(true);
        retakeButton.setDisable(false);
        saveButton.setDisable(false);
        statusLabel.setText("Image captured - Click Save or Retake");
    }

    @FXML
    private void retakeImage() {
        restartCameraPreview();
        statusLabel.setText("Ready to capture");
    }

    @FXML
    private void saveImage() {
        if (capturedImage != null) {
            // For now, we'll just close the dialog and handle the image in the parent
            if (stage != null) {
                stage.close();
            }
        }
    }

    public Image getCapturedImage() {
        return capturedImage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public static Image showCaptureDialog(Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(CameraCaptureDialog.class.getResource("/fxml1/CameraCapture.fxml"));
            Parent root = loader.load();
            
            CameraCaptureDialog controller = loader.getController();
            
            Stage dialog = new Stage();
            controller.setStage(dialog);
            
            dialog.initOwner(parentStage);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle("Capture Photo");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            
            dialog.showAndWait();
            
            return controller.getCapturedImage();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}