package application;

import application.services.DailyChargesService;
import application.services.dao.DatabaseManager;
import application.utils.CrashReporter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.io.FileWriter;

public class Main extends Application {

    private DailyChargesService dailyChargesService;

    @Override
    public void start(Stage primaryStage) {
        try {
            // ✅ FIXED: Set global exception handler FIRST
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                CrashReporter.logException(throwable, "Global Handler");
                log("Exception in thread " + thread.getName() + ": " + throwable.getMessage());
            });

            // ✅ FIXED: Ensure logs directory exists
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            log("Application starting...");
            log("Working directory: " + System.getProperty("user.dir"));
            log("Database path: " + application.services.dao.DatabaseManager.getDatabaseLocation());

            // ✅ FIXED: Initialize database (H2 embedded)
            DatabaseManager.initializeDatabase();
            log("Database initialized successfully.");

            // ✅ FIXED: Load FXML with better error handling
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Login.fxml"));
            Parent root = loader.load();

            // ✅ FIXED: Set application icon to prevent Java coffee cup
            try {
                Image appIcon = new Image(getClass().getResourceAsStream("/images/app_icon.png"));
                primaryStage.getIcons().add(appIcon);
                log("Application icon loaded successfully.");
            } catch (Exception e) {
                log("Warning: Could not load application icon: " + e.getMessage());
            }

            primaryStage.setTitle("Hotel Management System - Login");
            primaryStage.setScene(new Scene(root));
            
            // ✅ FIXED: Set minimum window size
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            primaryStage.show();
            log("Login page loaded successfully.");

        } catch (IOException e) {
            String errorMsg = "Failed to load Login.fxml: " + e.getMessage();
            log(errorMsg);
            e.printStackTrace();
            showErrorDialog("FXML Error", "Cannot load login screen", errorMsg);
        } catch (Exception e) {
            String errorMsg = "Startup error: " + e.getMessage();
            log(errorMsg);
            e.printStackTrace();
            showErrorDialog("Startup Error", "Application failed to start", errorMsg);
        }
    }

    @Override
    public void stop() throws Exception {
        log("Application stopping...");
        if (dailyChargesService != null) {
            dailyChargesService.stopService();
            log("DailyChargesService stopped.");
        }
        super.stop();
        log("Application stopped.");
    }

    public static void main(String[] args) {
        try {
            // ✅ FIXED: Launch JavaFX properly
            launch(args);
        } catch (Exception e) {
            // ✅ FIXED: Catch any startup crashes and log them
            try (FileWriter fw = new FileWriter("logs/startup_crash.log", true)) {
                fw.write(LocalDateTime.now() + " - CRASH ON STARTUP: " + e.getMessage() + "\n");
                for (StackTraceElement ste : e.getStackTrace()) {
                    fw.write("    " + ste.toString() + "\n");
                }
            } catch (IOException ioException) {
                System.err.println("Could not write crash log: " + ioException.getMessage());
            }
            e.printStackTrace();
        }
    }

    // ✅ Local logger
    private static void log(String message) {
        try (FileWriter fw = new FileWriter("logs/app.log", true)) {
            fw.write(LocalDateTime.now() + " - " + message + "\n");
        } catch (IOException e) {
            System.err.println("Log write failed: " + e.getMessage());
        }
    }

    // ✅ FIXED: Show error dialog for user-friendly messages
    private void showErrorDialog(String title, String header, String content) {
        try {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            // Fallback if JavaFX alert fails
            System.err.println("ERROR: " + title + " - " + header + " - " + content);
        }
    }
}