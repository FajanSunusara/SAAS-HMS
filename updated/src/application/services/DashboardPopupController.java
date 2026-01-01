package application.services;

import application.models.Payment;
import application.models.TodayReservation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class DashboardPopupController {

    // Pending Payments Table (match FXML ids and add generics)
    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, String> paymentGuestCol;
    @FXML private TableColumn<Payment, String> paymentRoomCol;
    @FXML private TableColumn<Payment, Number> paymentAmountCol;
    @FXML private TableColumn<Payment, String> paymentDueCol;

    // Today's Reservations Table (match FXML ids and add generics)
    @FXML private TableView<TodayReservation> todaysTable;
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
            todaysGuestCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getGuestName()));
            todaysRoomCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRoomNumber()));
            todaysCheckInCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckIn()));
            todaysCheckOutCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckOut()));
            todaysStatusCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus()));
            todaysTable.setItems(todaysReservations);
        }
    }
}
