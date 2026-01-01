package application.controllers;

import application.models.Payment;
import application.models.TodayReservation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DashboardPopupController {

    // Pending Payments Table (match FXML ids and add generics)
    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, String> paymentGuestCol;
    @FXML private TableColumn<Payment, String> paymentRoomCol;
    @FXML private TableColumn<Payment, Number> paymentAmountCol;
    @FXML private TableColumn<Payment, String> paymentDueCol;

    // Today's Reservations Table (match FXML ids and add generics)
    @FXML private TableView<TodayReservation> todaysTable;
    @FXML private TableColumn<TodayReservation, String> todaysReservationIdCol;
    @FXML private TableColumn<TodayReservation, String> todaysGuestCol;
    @FXML private TableColumn<TodayReservation, String> todaysRoomCol;
    @FXML private TableColumn<TodayReservation, String> todaysCheckInCol;
    @FXML private TableColumn<TodayReservation, String> todaysCheckOutCol;
    @FXML private TableColumn<TodayReservation, String> todaysStatusCol;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void closePopup() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // FINAL: only two lists, because FXML shows two tables
    public void setDashboardData(ObservableList<Payment> pendingPayments,
                               ObservableList<TodayReservation> todaysReservations) {
        
        // Populate Pending Payments Table
        if (paymentTable != null && pendingPayments != null) {
            paymentGuestCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getGuestName()));
            paymentRoomCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRoomNumber()));
            paymentAmountCol.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getAmount().doubleValue()));
            paymentDueCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDueDate()));
            paymentTable.setItems(pendingPayments);
        }

        // Populate Today's Reservations Table
        if (todaysTable != null && todaysReservations != null) {
            todaysReservationIdCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getReservationId()));
            todaysGuestCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getGuestName()));
            todaysRoomCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRoomNumber()));
            todaysCheckInCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckIn()));
            todaysCheckOutCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckOut()));
            todaysStatusCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus()));
            todaysTable.setItems(todaysReservations);
            
            // Add row click handler for today's reservations table
            setupTodaysTableClickHandler();
        }
    }

    /**
     * Setup click handler for today's reservations table
     */
    private void setupTodaysTableClickHandler() {
        todaysTable.setOnMouseClicked(this::handleTodaysTableRowClick);
    }

    /**
     * Handle row click on today's reservations table
     */
    private void handleTodaysTableRowClick(MouseEvent event) {
        // Check for double-click
        if (event.getClickCount() == 2) { 
            TodayReservation selectedReservation = todaysTable.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                openBookingPageWithReservationData(selectedReservation);
            }
        }
    }

    /**
     * Open booking page with reservation data pre-filled and close all other windows
     */
    private void openBookingPageWithReservationData(TodayReservation reservation) {
        try {
            // Try multiple possible locations for the Booking FXML file
            String[] possiblePaths = {
                "/fxml1/Booking.fxml",
                "/application/views/Booking.fxml", 
                "Booking.fxml",
                "/Booking.fxml"
            };
            
            FXMLLoader loader = null;
            Parent bookingRoot = null;
            
            // Try each path until we find the FXML file
            for (String path : possiblePaths) {
                try {
                    java.net.URL fxmlUrl = getClass().getResource(path);
                    if (fxmlUrl != null) {
                        loader = new FXMLLoader(fxmlUrl);
                        bookingRoot = loader.load();
                        System.out.println("Successfully loaded FXML from: " + path);
                        break; // Found the file, stop looking
                    }
                } catch (Exception e) {
                    // Continue to next path
                    System.out.println("Failed to load from " + path + ": " + e.getMessage());
                }
            }
            
            if (bookingRoot == null) {
                throw new RuntimeException("Could not find Booking.fxml in any of the expected locations");
            }
            
            // Get the booking controller
            BookingController bookingController = loader.getController();
            
            // Pre-populate booking form with reservation data
            if (bookingController != null) {
                bookingController.populateFromReservation(reservation);
            }
            
            // Create new stage for booking page
            Stage bookingStage = new Stage();
            bookingStage.setScene(new Scene(bookingRoot));
            bookingStage.setTitle("Hotel Management - Booking (From Reservation)");
            bookingStage.setResizable(true);
            bookingStage.setMaximized(true); // Optional: maximize the booking window
            
            // CLOSE ALL OTHER WINDOWS/STAGES
            closeAllOtherWindows();
            
            // Show booking page as the only remaining window
            bookingStage.show();
            
            System.out.println("Successfully opened booking page and closed all other windows");
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", 
                "Could not open Booking page: " + e.getMessage() + 
                "\n\nPlease make sure the Booking.fxml file exists in your resources folder.");
        }
    }

    /**
     * Close all currently open windows/stages
     */
    private void closeAllOtherWindows() {
        try {
            // Get all open windows
            javafx.collections.ObservableList<Window> windows = Stage.getWindows();
            
            // Create a copy of the list to avoid ConcurrentModificationException
            java.util.List<Window> windowsToClose = new java.util.ArrayList<>(windows);
            
            // Close all windows
            for (Window window : windowsToClose) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    System.out.println("Closing window: " + stage.getTitle());
                    stage.close();
                }
            }
            
            System.out.println("All windows closed successfully");
            
        } catch (Exception e) {
            System.err.println("Error closing windows: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
