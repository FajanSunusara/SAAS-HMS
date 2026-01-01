package application.services;

import application.models.Guest;
import application.models.Payment;
import application.models.Room;
import application.models.TodayReservation;
import application.services.dao.DashboardPopupDao;
import application.services.dao.RoomDAO;
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
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class HomeController {

    @FXML private ComboBox<String> roomTypeFilter, statusFilter, floorFilter, priceRangeFilter;
    @FXML private GridPane roomGrid;

    private final RoomDAO roomDAO = new RoomDAO();
    private final DashboardPopupDao dashboardDao = new DashboardPopupDao();
    private final ObservableList<Room> allRoomsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        populateFilters();
        try {
            // Load rooms once
            List<Room> roomList = roomDAO.getAllRooms();
            if (roomList != null) {
                allRoomsData.setAll(roomList);
            }
            // Initialize floors list based on loaded rooms (All + distinct floors)
            refreshFloorFilterChoices();

            // Draw initial view with all rooms (grouped by floor)
            populateRoomCards(applyFiltersInternal());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load room data from the database.");
        }
    }

    // ================= Dashboard =================

    @FXML
    private void showDashboard() {
        try {
            ObservableList<Payment> pendingPayments = dashboardDao.getPendingPayments();
            ObservableList<TodayReservation> todaysReservations = dashboardDao.getTodaysReservations();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/DashboardPopup.fxml"));
            Parent root = loader.load();

            DashboardPopupController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Hotel Dashboard");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(roomGrid.getScene().getWindow());
            controller.setDialogStage(dialogStage);

            // FINAL: call the 2-argument method
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

    // ================= Guest Details =================

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
            dialogStage.initModality(Modality.WINDOW_MODAL);
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

    // ================= Filters =================

    private void populateFilters() {
        // Room Types
        roomTypeFilter.setItems(FXCollections.observableArrayList(
                "All", "Single", "Double", "Deluxe", "Suite", "Twin"
        ));
        roomTypeFilter.setValue("All");

        // Statuses
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Available", "Occupied", "Cleaning", "Maintenance", "Under Maintenance", "Empty"
        ));
        statusFilter.setValue("All");

        // Floors (initial: All only; populated after rooms are loaded)
        floorFilter.setItems(FXCollections.observableArrayList("All"));
        floorFilter.setValue("All");

        // Price ranges
        priceRangeFilter.setItems(FXCollections.observableArrayList(
                "All", "$2000-$3000", "$3000-$4000", "$4000+"
        ));
        priceRangeFilter.setValue("All");

        // Listeners
        roomTypeFilter.valueProperty().addListener((obs, o, n) -> onFilterChanged());
        statusFilter.valueProperty().addListener((obs, o, n) -> onFilterChanged());
        floorFilter.valueProperty().addListener((obs, o, n) -> onFilterChanged());
        priceRangeFilter.valueProperty().addListener((obs, o, n) -> onFilterChanged());
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

    @FXML
    private void onFilterChanged() {
        try {
            populateRoomCards(applyFiltersInternal());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load room data from the database.");
        }
    }

    private List<Room> applyFiltersInternal() {
        String type = safeValue(roomTypeFilter);
        String stat = safeValue(statusFilter);
        String flr = safeValue(floorFilter);
        String priceR = safeValue(priceRangeFilter);

        return allRoomsData.stream()
                .filter(r -> "All".equalsIgnoreCase(type) || equalsIgnoreCaseSafe(r.getRoomType(), type))
                .filter(r -> "All".equalsIgnoreCase(stat) || equalsIgnoreCaseSafe(r.getStatus(), stat))
                .filter(r -> "All".equalsIgnoreCase(flr) || (r.getFloor() != null && String.valueOf(r.getFloor()).equals(flr)))
                .filter(r -> priceMatch(r, priceR))
                .sorted(Comparator.comparing(Room::getFloor, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Room::getRoomNo, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private boolean priceMatch(Room r, String priceR) {
        if (r.getPrice() == null) return false;
        double price = r.getPrice().doubleValue();
        switch (priceR) {
            case "All":
                return true;
            case "$2000-$3000":
                return price >= 2000 && price <= 3000;
            case "$3000-$4000":
                return price > 3000 && price <= 4000;
            case "$4000+":
                return price > 4000;
            default:
                return true;
        }
    }

    private String safeValue(ComboBox<String> combo) {
        return combo == null || combo.getValue() == null ? "All" : combo.getValue();
    }

    private boolean equalsIgnoreCaseSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    // ================= Room Cards (segmented by floor) =================

    private void populateRoomCards(List<Room> roomsToDisplay) throws SQLException {
        roomGrid.getChildren().clear();
        if (roomsToDisplay == null || roomsToDisplay.isEmpty()) {
            Label noRoomsLabel = new Label("No rooms found.");
            noRoomsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #808080;");
            roomGrid.add(noRoomsLabel, 0, 0);
            return;
        }

        // Group by floor (ascending)
        Map<Integer, List<Room>> byFloor = roomsToDisplay.stream()
                .collect(Collectors.groupingBy(Room::getFloor, TreeMap::new, Collectors.toList()));

        int gridRow = 0;
        for (Map.Entry<Integer, List<Room>> entry : byFloor.entrySet()) {
            Integer floor = entry.getKey();

            // Header
            Label floorHeader = new Label("Floor " + (floor == null ? "-" : floor));
            floorHeader.setFont(Font.font("System", FontWeight.BOLD, 18));
            floorHeader.setPadding(new Insets(10, 0, 5, 0));

            HBox floorHeaderBox = new HBox(floorHeader);
            floorHeaderBox.setAlignment(Pos.CENTER_LEFT);
            floorHeaderBox.setPadding(new Insets(10, 10, 0, 10));
            roomGrid.add(floorHeaderBox, 0, gridRow++, 6, 1);

            // Cards
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
            if (col != 0) gridRow++;
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("room-card");

        // Assign status-specific style
        String status = room.getStatus() == null ? "" : room.getStatus().toLowerCase();
        switch (status) {
            case "available":
            case "empty":
                card.getStyleClass().add("empty");
                break;
            case "occupied":
                card.getStyleClass().add("occupied");
                card.setOnMouseClicked(e -> showGuestDetails(room.getRoomNo()));
                break;
            case "cleaning":
                card.getStyleClass().add("cleaning");
                break;
            case "maintenance":
            case "under maintenance":
                card.getStyleClass().add("maintenance");
                break;
            default:
                card.getStyleClass().add("maintenance");
        }

        Label roomNumberLabel = new Label(room.getRoomNo());
        roomNumberLabel.getStyleClass().add("room-name-label");

        Label detailsLabel = new Label(room.getRoomType() + " | Floor " + room.getFloor());
        detailsLabel.getStyleClass().add("room-category-label");

        Label priceLabel = new Label(String.format("$%.2f", room.getPrice()));
        priceLabel.getStyleClass().add("room-price-label");

        Label statusLabel = new Label(room.getStatus());
        statusLabel.getStyleClass().add("room-status-text");

        card.getChildren().addAll(roomNumberLabel, detailsLabel,  statusLabel);
        return card;
    }

    // ================= Alerts =================

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
