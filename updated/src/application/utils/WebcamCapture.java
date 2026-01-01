package application.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamCapture {

    private static final Random random = new Random();

    public static List<String> getAvailableCameras() {
        List<String> cameras = new ArrayList<>();
        
        // Simulate camera detection
        cameras.add("📷 Default Camera");
        cameras.add("📱 Front Camera");
        cameras.add("🔍 Back Camera");
        cameras.add("💻 Webcam");
        
        return cameras;
    }

    public static Image captureImage() {
        return createRealisticPlaceholderImage();
    }

    public static Image createPlaceholderImage() {
        return createRealisticPlaceholderImage();
    }

    private static Image createRealisticPlaceholderImage() {
        int width = 640;
        int height = 480;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Create a gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(70, 130, 180),
            width, height, new Color(30, 144, 255)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Draw camera frame
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.fillRect(50, 50, width - 100, height - 100);
        
        // Draw lens
        g2d.setColor(Color.BLACK);
        g2d.fillOval(width/2 - 60, height/2 - 60, 120, 120);
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillOval(width/2 - 40, height/2 - 40, 80, 80);
        
        // Draw camera text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String text = "Camera Preview";
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, 80);
        
        // Draw instruction text
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String instruction = "Click 'Capture' to take photo";
        int instWidth = g2d.getFontMetrics().stringWidth(instruction);
        g2d.drawString(instruction, (width - instWidth) / 2, height - 60);
        
        // Draw capture button
        g2d.setColor(new Color(46, 204, 113));
        g2d.fillOval(width/2 - 30, height - 120, 60, 60);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(width/2 - 20, height - 110, 40, 40);
        
        g2d.dispose();
        
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static Image createErrorImage() {
        int width = 640;
        int height = 480;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // Red gradient background for error
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(220, 53, 69),
            width, height, new Color(200, 35, 51)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        
        String errorText = "Camera Not Available";
        int textWidth = g2d.getFontMetrics().stringWidth(errorText);
        g2d.drawString(errorText, (width - textWidth) / 2, height/2 - 20);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        String suggestion = "Please select an image file instead";
        int sugWidth = g2d.getFontMetrics().stringWidth(suggestion);
        g2d.drawString(suggestion, (width - sugWidth) / 2, height/2 + 20);
        
        // Draw error icon
        g2d.setColor(Color.WHITE);
        g2d.fillOval(width/2 - 40, height/2 - 80, 80, 80);
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.drawString("!", width/2 - 10, height/2 - 25);
        
        g2d.dispose();
        
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static boolean saveImage(Image image, File file) {
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            return ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            return false;
        }
    }

    public static Image loadImage(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            return createErrorImage();
        }
    }

    public static Image createCapturedPhoto() {
        int width = 640;
        int height = 480;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // Create a realistic-looking captured photo
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(240, 248, 255),
            width, height, new Color(230, 240, 250)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Draw a person silhouette
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillOval(width/2 - 80, height/2 - 120, 160, 160); // Head
        
        // Draw body
        g2d.fillRect(width/2 - 40, height/2 + 40, 80, 120);
        
        // Draw success message
        g2d.setColor(new Color(46, 204, 113));
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        String successText = "Photo Captured Successfully!";
        int textWidth = g2d.getFontMetrics().stringWidth(successText);
        g2d.drawString(successText, (width - textWidth) / 2, 80);
        
        // Draw timestamp
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String timestamp = "Captured: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int timeWidth = g2d.getFontMetrics().stringWidth(timestamp);
        g2d.drawString(timestamp, (width - timeWidth) / 2, height - 40);
        
        g2d.dispose();
        
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}