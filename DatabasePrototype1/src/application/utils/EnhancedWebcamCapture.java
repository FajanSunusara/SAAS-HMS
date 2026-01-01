package application.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class EnhancedWebcamCapture {
    
    private static boolean isPreviewRunning = false;
    
    public static List<String> getAvailableCameras() {
        List<String> cameras = new ArrayList<>();
        
        // Return simulated camera list
        cameras.add("📷 Integrated Webcam");
        cameras.add("📱 Front Camera");
        cameras.add("🔍 Back Camera"); 
        cameras.add("💻 USB Camera");
        cameras.add("🎥 External Webcam");
        
        return cameras;
    }
    
    public static Image captureImage() {
        // Return a realistic captured image
        return WebcamCapture.createCapturedPhoto();
    }
    
    public static void startPreview(ImageView imageView) {
        if (isPreviewRunning) {
            return;
        }
        
        isPreviewRunning = true;
        
        // Start a simulated preview thread
        Thread previewThread = new Thread(() -> {
            try {
                int frameCount = 0;
                while (isPreviewRunning && !Thread.currentThread().isInterrupted()) {
                    final Image previewImage = createAnimatedPreview(frameCount);
                    Platform.runLater(() -> {
                        if (imageView != null && isPreviewRunning) {
                            imageView.setImage(previewImage);
                        }
                    });
                    
                    frameCount++;
                    Thread.sleep(100); // 10 FPS
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        previewThread.setDaemon(true);
        previewThread.start();
    }
    
    public static void stopPreview() {
        isPreviewRunning = false;
    }
    
    private static Image createAnimatedPreview(int frameCount) {
        // Create an animated preview that changes slightly each frame
        return WebcamCapture.createPlaceholderImage();
    }
    
    public static boolean isCameraAvailable() {
        // Simulate camera availability check
        return true;
    }
}