package application.controllers;

import application.models.Guest;
import application.models.Room;
import application.services.DailyChargesService;
import application.services.dao.DashboardPopupDao;
import application.services.dao.DatabaseManager;
import application.services.dao.RoomDAO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HomeController {

    @FXML private ComboBox<String> roomTypeFilter, statusFilter, floorFilter, priceRangeFilter;
    @FXML private GridPane roomGrid;
	private DailyChargesService dailyChargesService;
    private final RoomDAO roomDAO = new RoomDAO();
    private final DashboardPopupDao dashboardDao = new DashboardPopupDao();
    private final ObservableList<Room> allRoomsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dailyChargesService = DailyChargesService.getInstance();
//        dailyChargesService.startService();
        populateFilters();
        refreshAll();

        Platform.runLater(() -> {
            Stage stage = (Stage) roomGrid.getScene().getWindow();
            if (stage != null) {
                stage.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (Boolean.TRUE.equals(newV)) refreshAll();
                });
            }
        });
        
    }
 
    private void refreshAll() {
        try {
            List<Room> roomList = roomDAO.getAllRooms();
            allRoomsData.setAll(roomList != null ? roomList : List.of());
            refreshFloorFilterChoices();
            populateRoomCards(applyFiltersInternal());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load room data from the database.");
        }
    }

    // Dashboard still available via your menu button
    @FXML
    private void showDashboard() {
        try {
            var pendingPayments = dashboardDao.getPendingPayments();
            var todaysReservations = dashboardDao.getTodaysReservations();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/DashboardPopup.fxml"));
            Parent root = loader.load();
            DashboardPopupController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Hotel Dashboard");
            dialogStage.initOwner(roomGrid.getScene().getWindow());
            controller.setDialogStage(dialogStage);
            controller.setDashboardData(pendingPayments, todaysReservations);

            Scene scene = new Scene(root, 900, 700);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load dashboard popup.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load dashboard data.");
        }
    }

    private void showGuestDetails(String roomNo) {
        try {
            Guest guest = dashboardDao.getGuestByRoomNo(roomNo);
            if (guest == null) {
                showAlert(Alert.AlertType.INFORMATION, "No Guest", "No checked-in guest found for room " + roomNo);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/GuestDetailPopup.fxml"));
            Parent root = loader.load();
            GuestDetailController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Guest Details - Room " + roomNo);
            dialogStage.initOwner(roomGrid.getScene().getWindow());
            controller.setDialogStage(dialogStage);
            controller.setGuestData(guest);

            Scene scene = new Scene(root, 500, 650);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load guest details popup.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load guest data.");
        }
    }

    // Filters
    private void populateFilters() {
        roomTypeFilter.setItems(FXCollections.observableArrayList(
                "All", "Single", "Double", "Deluxe", "Suite", "Twin"
        ));
        roomTypeFilter.setValue("All");

        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Available", "Occupied", "Cleaning", "Maintenance", "Under Maintenance", "Empty"
        ));
        statusFilter.setValue("All");

        floorFilter.setItems(FXCollections.observableArrayList("All"));
        floorFilter.setValue("All");

        priceRangeFilter.setItems(FXCollections.observableArrayList(
                "All", "₹2000-₹3000", "₹3000-₹4000", "₹4000+"
        ));
        priceRangeFilter.setValue("All");

        roomTypeFilter.valueProperty().addListener((obs, o, n) -> refreshAll());
        statusFilter.valueProperty().addListener((obs, o, n) -> refreshAll());
        floorFilter.valueProperty().addListener((obs, o, n) -> refreshAll());
        priceRangeFilter.valueProperty().addListener((obs, o, n) -> refreshAll());
    }

    private void refreshFloorFilterChoices() {
        Set<Integer> floors = allRoomsData.stream()
                .map(Room::getFloor)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        List<String> values = new ArrayList<>();
        values.add("All");
        values.addAll(floors.stream().map(String::valueOf).toList());

        floorFilter.setItems(FXCollections.observableArrayList(values));
        if (!values.contains(floorFilter.getValue())) {
            floorFilter.setValue("All");
        }
    }

    private List<Room> applyFiltersInternal() {
        String type = safeValue(roomTypeFilter);
        String stat = safeValue(statusFilter);
        String flr = safeValue(floorFilter);
        String priceR = safeValue(priceRangeFilter);

        return allRoomsData.stream()
                .filter(r -> "All".equalsIgnoreCase(type) || equalsIgnoreCaseSafe(r.getRoomType(), type))
                .filter(r -> "All".equalsIgnoreCase(stat) || equalsIgnoreCaseSafe(normalizeStatus(r.getStatus()), normalizeStatus(stat)))
                .filter(r -> "All".equalsIgnoreCase(flr) || (r.getFloor() != null && String.valueOf(r.getFloor()).equals(flr)))
                .filter(r -> priceMatch(r, priceR))
                .sorted(Comparator.comparing(Room::getFloor, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Room::getRoomNo, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private boolean priceMatch(Room r, String priceR) {
        if (r.getPrice() == null) return false;
        double price = r.getPrice().doubleValue();
        return switch (priceR) {
            case "All" -> true;
            case "₹2000-₹3000" -> price >= 2000 && price <= 3000;
            case "₹3000-₹4000" -> price > 3000 && price <= 4000;
            case "₹4000+" -> price > 4000;
            default -> true;
        };
    }

    private String safeValue(ComboBox<String> combo) {
        return combo == null || combo.getValue() == null ? "All" : combo.getValue();
    }

    private boolean equalsIgnoreCaseSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private String normalizeStatus(String s) {
        if (s == null) return "";
        String v = s.trim().toLowerCase();
        if (v.equals("empty")) return "available";
        return v; // This will handle "reserved" as-is
    }

    // Cards
    private void populateRoomCards(List<Room> roomsToDisplay) {
        roomGrid.getChildren().clear();

        if (roomsToDisplay == null || roomsToDisplay.isEmpty()) {
            Label noRoomsLabel = new Label("No rooms found.");
            noRoomsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #808080;");
            roomGrid.add(noRoomsLabel, 0, 0);
            return;
        }

        Map<Integer, List<Room>> byFloor = roomsToDisplay.stream()
                .collect(Collectors.groupingBy(Room::getFloor, TreeMap::new, Collectors.toList()));

        int gridRow = 0;
        for (Map.Entry<Integer, List<Room>> entry : byFloor.entrySet()) {
            Integer floor = entry.getKey();

            Label floorHeader = new Label("Floor " + (floor == null ? "-" : floor));
            floorHeader.setFont(Font.font("System", FontWeight.BOLD, 18));
            floorHeader.setPadding(new Insets(10, 0, 5, 0));
            HBox floorHeaderBox = new HBox(floorHeader);
            floorHeaderBox.setAlignment(Pos.CENTER_LEFT);
            floorHeaderBox.setPadding(new Insets(10, 10, 0, 10));
            roomGrid.add(floorHeaderBox, 0, gridRow++, 6, 1);

            int col = 0;
            for (Room room : entry.getValue().stream()
                    .sorted(Comparator.comparing(Room::getRoomNo, Comparator.nullsLast(String::compareTo)))
                    .toList()) {
                VBox card = createRoomCard(room);
                roomGrid.add(card, col, gridRow);
                GridPane.setMargin(card, new Insets(8));
                col++;
                if (col == 6) {
                    col = 0;
                    gridRow++;
                }
            }
            gridRow++;
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("room-card");
        
        String normalized = normalizeStatus(room.getStatus());
        switch (normalized) {
            case "available" -> {
                card.getStyleClass().add("empty");
                card.setOnMouseClicked(e -> openBookingFullscreenAndCloseHome(room));
            }
            case "occupied" -> {
                card.getStyleClass().add("occupied");
                card.setOnMouseClicked(e -> showGuestDetails(room.getRoomNo()));
            }
            case "reserved" -> { // NEW: Add reserved status
                card.getStyleClass().add("reserved");
                card.setOnMouseClicked(e -> openBookingWithReservationDetails(room.getRoomNo(),room));
            }
            case "cleaning" -> card.getStyleClass().add("cleaning");
            case "maintenance", "under maintenance" -> card.getStyleClass().add("maintenance");
            default -> card.getStyleClass().add("maintenance");
        }

        Label roomNumberLabel = new Label(room.getRoomNo());
        roomNumberLabel.getStyleClass().add("room-name-label");
        
        Label detailsLabel = new Label((room.getRoomType() == null ? "-" : room.getRoomType()) + " | Floor " + room.getFloor());
        detailsLabel.getStyleClass().add("room-category-label");
        
        BigDecimal price = room.getPrice() == null ? BigDecimal.ZERO : room.getPrice();
        Label priceLabel = new Label(String.format("₹%,.2f", price.doubleValue()));
        priceLabel.getStyleClass().add("room-price-label");
        
        Label statusLabel = new Label(room.getStatus());
        statusLabel.getStyleClass().add("room-status-text");
        
        card.getChildren().addAll(roomNumberLabel, detailsLabel, statusLabel);
        return card;
    }
    private void openBookingWithReservationDetails(String roomNo,Room room) {
        try {
            // First, fetch reservation and guest details
            ReservationData reservationData = fetchReservationDataForRoom(roomNo);
            
            if (reservationData == null) {
                showAlert(Alert.AlertType.WARNING, "No Reservation", 
                    "No active reservation found for room " + roomNo);
                
                openBookingFullscreenAndCloseHome( room) ;
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Booking.fxml"));
            Parent root = loader.load();
            BookingController bookingController = loader.getController();

            // Pre-fill the booking form with reservation data
            Platform.runLater(() -> {
                try {
                    // Set room details
                    var typeCombo = bookingControllerRoomTypeCombo(bookingController);
                    if (typeCombo != null && reservationData.roomType != null) {
                        typeCombo.getSelectionModel().select(reservationData.roomType);
                    }

                    var noCombo = bookingControllerRoomNoCombo(bookingController);
                    if (noCombo != null) {
                        if (!noCombo.getItems().contains(roomNo)) {
                            // Refresh room numbers if needed
                            if (typeCombo != null && reservationData.roomType != null) {
                                typeCombo.getSelectionModel().select(reservationData.roomType);
                            }
                        }
                        noCombo.getSelectionModel().select(roomNo);
                    }

                    // Set guest details using reflection
                    setBookingControllerField(bookingController, "fullNameField", reservationData.guestName);
                    setBookingControllerField(bookingController, "mobileNumberField", reservationData.guestPhone);
                    setBookingControllerField(bookingController, "emailField", reservationData.guestEmail);
                    setBookingControllerField(bookingController, "addressArea", reservationData.guestAddress);
                    setBookingControllerField(bookingController, "gstNumberField", reservationData.guestGst);

                    // Set dates
                    setBookingControllerDatePicker(bookingController, "checkInDatePicker", reservationData.checkInDate);
                    setBookingControllerDatePicker(bookingController, "checkOutDatePicker", reservationData.checkOutDate);

                    // Set advance payment if any
                    if (reservationData.advancePaid > 0) {
                        setBookingControllerField(bookingController, "advancePaymentField", 
                            String.valueOf(reservationData.advancePaid));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to prefill booking details: " + ex.getMessage());
                }
            });

            Stage bookingStage = new Stage();
            bookingStage.setTitle("Convert Reservation to Booking - Room " + roomNo);
            bookingStage.setScene(new Scene(root));
            bookingStage.setMaximized(true);

            Stage homeStage = (Stage) roomGrid.getScene().getWindow();
            bookingStage.show();
            homeStage.close();
         

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Could not open booking screen: " + ex.getMessage());
        }
    }


    // Open Booking fullscreen and close Home
    private void openBookingFullscreenAndCloseHome(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/Booking.fxml"));
            Parent root = loader.load();
            BookingController bookingController = loader.getController();

            // If you added a helper in BookingController:
            // public void preselectRoom(String roomType, String roomNo)
            try {
                BookingController.class.getMethod("preselectRoom", String.class, String.class)
                        .invoke(bookingController, room.getRoomType(), room.getRoomNo());
            } catch (NoSuchMethodException ignore) {
                // Fallback: best-effort preselect via reflection to access ComboBoxes
                Platform.runLater(() -> {
                    var typeCombo = bookingControllerRoomTypeCombo(bookingController);
                    if (typeCombo != null && room.getRoomType() != null) {
                        typeCombo.getSelectionModel().select(room.getRoomType());
                    }
                    var noCombo = bookingControllerRoomNoCombo(bookingController);
                    if (noCombo != null && room.getRoomNo() != null) {
                        if (!noCombo.getItems().contains(room.getRoomNo())) {
                            if (typeCombo != null && room.getRoomType() != null) {
                                typeCombo.getSelectionModel().select(room.getRoomType());
                            }
                        }
                        noCombo.getSelectionModel().select(room.getRoomNo());
                    }
                });
            }

            Stage bookingStage = new Stage();
            bookingStage.setTitle("Create Booking");
            bookingStage.setScene(new Scene(root));
            bookingStage.setMaximized(true);

            Stage homeStage = (Stage) roomGrid.getScene().getWindow();
         

            bookingStage.show();
            homeStage.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not open Booking screen.");
        }
    }

    @SuppressWarnings("unchecked")
    private ComboBox<String> bookingControllerRoomTypeCombo(BookingController bc) {
        try {
            var f = BookingController.class.getDeclaredField("roomTypeComboBox");
            f.setAccessible(true);
            return (ComboBox<String>) f.get(bc);
        } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private ComboBox<String> bookingControllerRoomNoCombo(BookingController bc) {
        try {
            var f = BookingController.class.getDeclaredField("roomNumberComboBox");
            f.setAccessible(true);
            return (ComboBox<String>) f.get(bc);
        } catch (Exception e) { return null; }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    private ReservationData fetchReservationDataForRoom(String roomNo) {
        String sql = """
            SELECT r.id, r.guest_id, r.room_type, r.start_date, r.end_date, 
                   r.advance_paid, r.notes,
                   g.name, g.phone, g.email, g.address, g.gst_number
            FROM reservations r
            JOIN guests g ON r.guest_id = g.id
            WHERE r.room_no = ? 
            AND r.status IN ('Confirmed', 'Pending')
            AND r.start_date >= CURRENT_DATE
            ORDER BY r.start_date ASC
            LIMIT 1
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, roomNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ReservationData data = new ReservationData();
                    data.reservationId = rs.getLong("id");
                    data.guestId = rs.getLong("guest_id");
                    data.roomType = rs.getString("room_type");
                    data.checkInDate = rs.getDate("start_date").toLocalDate();
                    data.checkOutDate = rs.getDate("end_date").toLocalDate();
                    data.advancePaid = rs.getDouble("advance_paid");
                    data.notes = rs.getString("notes");
                    data.guestName = rs.getString("name");
                    data.guestPhone = rs.getString("phone");
                    data.guestEmail = rs.getString("email");
                    data.guestAddress = rs.getString("address");
                    data.guestGst = rs.getString("gst_number");
                    return data;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper class to hold reservation data
    private static class ReservationData {
        Long reservationId;
        Long guestId;
        String roomType;
        LocalDate checkInDate;
        LocalDate checkOutDate;
        double advancePaid;
        String notes;
        String guestName;
        String guestPhone;
        String guestEmail;
        String guestAddress;
        String guestGst;
    }

    // Helper methods to set fields in BookingController using reflection
    private void setBookingControllerField(BookingController bc, String fieldName, String value) {
        try {
            var field = BookingController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object fieldObj = field.get(bc);
            
            if (fieldObj instanceof TextField) {
                ((TextField) fieldObj).setText(value != null ? value : "");
            } else if (fieldObj instanceof TextArea) {
                ((TextArea) fieldObj).setText(value != null ? value : "");
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
    }

    private void setBookingControllerDatePicker(BookingController bc, String fieldName, LocalDate value) {
        try {
            var field = BookingController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object fieldObj = field.get(bc);
            
            if (fieldObj instanceof DatePicker) {
                ((DatePicker) fieldObj).setValue(value);
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
    }

}
