package application;

import application.services.DailyChargesService;
import application.services.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
	private DailyChargesService dailyChargesService;
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseManager.initializeDatabase();
           

            // Load the login page as the main entry point
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Login.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Hotel Management System - Login");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load the login page. Check your FXML file path and structure.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to initialize database or start application: " + e.getMessage());
        }
    }
    @Override
    public void stop() throws Exception {
        if (dailyChargesService != null) {
            dailyChargesService.stopService();
        }
        super.stop();
    }
    public static void main(String[] args) {
        launch(args);
    }
}