package application.controllers;

import application.models.GuestBookingPayment;
import application.services.dao.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;

public class GuestManagementController {

    @FXML private TableView<GuestBookingPayment> guestTable;
    @FXML private TextField searchField;

    // Guest Columns
    @FXML private TableColumn<GuestBookingPayment, String> colGuestName;
    @FXML private TableColumn<GuestBookingPayment, String> colPhone;
    @FXML private TableColumn<GuestBookingPayment, String> colEmail;
    @FXML private TableColumn<GuestBookingPayment, String> colAddress;
    @FXML private TableColumn<GuestBookingPayment, String> colGuestNationality;
    @FXML private TableColumn<GuestBookingPayment, String> colIdType;
    @FXML private TableColumn<GuestBookingPayment, String> colIdNumber;
    @FXML private TableColumn<GuestBookingPayment, String> colGstNumber;


    // Booking Columns
    @FXML private TableColumn<GuestBookingPayment, Integer> colBookingId;
    @FXML private TableColumn<GuestBookingPayment, String> colRoomNo;
    @FXML private TableColumn<GuestBookingPayment, String> colCheckIn;
    @FXML private TableColumn<GuestBookingPayment, String> colCheckOut;
    @FXML private TableColumn<GuestBookingPayment, Double> colRatePerNight;
    @FXML private TableColumn<GuestBookingPayment, Double> colBaseAmount;
    @FXML private TableColumn<GuestBookingPayment, Double> colGstRate;
    @FXML private TableColumn<GuestBookingPayment, Double> colGstAmount;
    @FXML private TableColumn<GuestBookingPayment, Boolean> colGstIncluded;
    @FXML private TableColumn<GuestBookingPayment, Double> colTotalAmount;
    @FXML private TableColumn<GuestBookingPayment, Double> colAdvancePaid;
    @FXML private TableColumn<GuestBookingPayment, String> colPaymentStatus;
    @FXML private TableColumn<GuestBookingPayment, String> colStatus;
    @FXML private TableColumn<GuestBookingPayment, String> colDocumentLink;
    @FXML private TableColumn<GuestBookingPayment, String> colBookingNationality;


    private ObservableList<GuestBookingPayment> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind Guest Columns
        colGuestName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getGuestName()));
        colPhone.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        colAddress.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAddress()));
        colGuestNationality.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBookingNationality()));
        colIdType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIdType()));
        colIdNumber.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIdNumber()));
        colGstNumber.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getGstNumber()));

        // Bind Booking Columns
        colBookingId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getBookingId()));
        colRoomNo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRoomNo()));
        colCheckIn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCheckIn() != null ? c.getValue().getCheckIn().toString() : ""));
        colCheckOut.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCheckOut() != null ? c.getValue().getCheckOut().toString() : ""));
        colRatePerNight.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getRatePerNight()));
        colBaseAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getBaseAmount()));
        colGstRate.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getGstRate()));
        colGstAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getGstAmount()));
        colGstIncluded.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().isGstIncluded()));
        colTotalAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTotalAmount()));
        colAdvancePaid.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getAdvancePaid()));
        colPaymentStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentStatus()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBookingStatus()));
        colDocumentLink.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDocumentLink()));
        colBookingNationality.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBookingNationality()));

        loadData("");
    }

    private void loadData(String keyword) {
        data.clear();

        String sql = """
            SELECT
                g.name, g.phone, g.email, g.address, g.nationality as guest_nationality,
                g.id_type, g.id_number, g.gst_number,
                b.id as booking_id, b.room_no, b.check_in_date, b.check_out_date,
                b.rate_per_night, b.base_amount, b.gst_rate, b.gst_amount, b.gst_included,
                b.total_amount, b.advance_paid, b.payment_status, b.status,
                b.document_link, b.nationality as booking_nationality
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            WHERE g.name LIKE ? OR g.phone LIKE ? OR CAST(b.id AS VARCHAR) LIKE ?
            ORDER BY b.id DESC
        """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (!tableExists(conn, "BOOKINGS")) {
                System.err.println("[DB] BOOKINGS table not found. Skipping query.");
                guestTable.setItems(data);
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + keyword + "%");
                stmt.setString(2, "%" + keyword + "%");
                stmt.setString(3, "%" + keyword + "%");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    data.add(new GuestBookingPayment(
                            rs.getInt("booking_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getString("guest_nationality"),
                            rs.getString("id_type"),
                            rs.getString("id_number"),
                            rs.getString("gst_number"),
                            rs.getString("room_no"),
                            rs.getDate("check_in_date") != null ? rs.getDate("check_in_date").toLocalDate() : null,
                            rs.getDate("check_out_date") != null ? rs.getDate("check_out_date").toLocalDate() : null,
                            rs.getDouble("rate_per_night"),
                            rs.getDouble("base_amount"),
                            rs.getDouble("gst_rate"),
                            rs.getDouble("gst_amount"),
                            rs.getBoolean("gst_included"),
                            rs.getDouble("total_amount"),
                            rs.getDouble("advance_paid"),
                            rs.getString("payment_status"),
                            rs.getString("status"),
                            rs.getString("document_link"),
                            rs.getString("booking_nationality")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        guestTable.setItems(data);
    }

    private boolean tableExists(Connection conn, String tableName) {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    @FXML
    private void onSearch() {
        loadData(searchField.getText().trim());
    }
}