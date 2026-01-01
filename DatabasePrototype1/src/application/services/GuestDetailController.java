package application.services;

import application.models.Guest;
import application.models.PaymentHistoryEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class GuestDetailController {
    @FXML private Label guestNameLabel;
    @FXML private Label roomNoLabel;
    @FXML private Label bookingDateLabel;
    @FXML private Label checkoutDateLabel;
    @FXML private Label peopleCountLabel;
    @FXML private Label pendingAmountLabel;

    @FXML private TableView<PaymentHistoryEntry> paymentHistoryTable;
    @FXML private TableColumn<PaymentHistoryEntry, String> paymentDateCol;
    @FXML private TableColumn<PaymentHistoryEntry, Number> paidAmountCol;
    @FXML private TableColumn<PaymentHistoryEntry, String> descriptionCol;

    private Stage dialogStage;
    private Guest currentGuest;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setGuestData(Guest guest) {
        this.currentGuest = guest;
        if (guest != null) {
            guestNameLabel.setText("Guest Name: " + guest.getName());
            roomNoLabel.setText("Room No: " + guest.getRoomNumber());
            bookingDateLabel.setText("Check-in Date: " + guest.getCheckInDate());
            checkoutDateLabel.setText("Check-out Date: " + guest.getCheckOutDate());
            peopleCountLabel.setText("No. of People: " + guest.getNumberOfPeople());
            pendingAmountLabel.setText(String.format("Pending Amount: $%.2f", guest.getPendingAmount()));

            if (paymentHistoryTable != null) {
                paymentDateCol.setCellValueFactory(cd -> cd.getValue().dateProperty());
                paidAmountCol.setCellValueFactory(cd -> cd.getValue().amountProperty());
                descriptionCol.setCellValueFactory(cd -> cd.getValue().descriptionProperty());
                if (guest.getPaymentHistory() != null) {
                    paymentHistoryTable.setItems(guest.getPaymentHistory());
                }
            }
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void handlePayment() {
        if (currentGuest != null) {
            showAlert(Alert.AlertType.INFORMATION, "Payment",
                    "Payment processing for " + currentGuest.getName() + " will be implemented here.");
        }
    }

    @FXML
    private void handleExtendStay() {
        if (currentGuest != null) {
            showAlert(Alert.AlertType.INFORMATION, "Extend Stay",
                    "Extending stay for " + currentGuest.getName() + " will be implemented here.");
        }
    }

    @FXML
    private void handleRoomChange() {
        if (currentGuest != null) {
            showAlert(Alert.AlertType.INFORMATION, "Room Change",
                    "Room change for " + currentGuest.getName() + " will be implemented here.");
        }
    }

    @FXML
    private void handleCheckout() {
        if (currentGuest != null) {
            showAlert(Alert.AlertType.INFORMATION, "Checkout",
                    "Checkout for " + currentGuest.getName() + " will be implemented here.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
