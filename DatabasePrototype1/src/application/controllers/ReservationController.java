package application.controllers;

import application.models.Booking;
import application.models.Guest;
import application.models.ReservationStub;
import application.services.dao.GuestDAO;
import application.services.dao.ReservationDAO;
import application.services.dao.ReservationServiceDAO;
import application.services.dao.RoomDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ReservationController {

    // Filter Bar FXML elements
    @FXML private DatePicker checkInDate, checkOutDate;
    @FXML private ComboBox<String> roomCategoryFilter, roomNumberFilter;
    @FXML private TextField searchBar;
    @FXML private GridPane calendarGrid;
    @FXML private Label dateRangeLabel;

    // Constants
    private final int NUM_DAYS_DISPLAYED = 7;

    // Calendar navigation tracking
    private LocalDate currentCalendarStart = LocalDate.now();
    private LocalDate currentCalendarEnd = LocalDate.now().plusDays(7);

    // DAOs
    private final ReservationServiceDAO reservationServiceDAO = new ReservationServiceDAO();
    private final ReservationDAO reservationsDAO = new ReservationDAO();
    private final GuestDAO guestDAO = new GuestDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    // Data storage for filters
    private ObservableList<String> allRoomTypes = FXCollections.observableArrayList();
    private ObservableList<String> allRoomNumbers = FXCollections.observableArrayList();

    public void initialize() {
        // Initialize filters first
        initializeFilters();
        
        // Set default filter dates
        checkInDate.setValue(LocalDate.now());
        checkOutDate.setValue(LocalDate.now().plusDays(NUM_DAYS_DISPLAYED));

        // Setup filter listeners
        setupFilterListeners();

        // Initial calendar generation
        generateCombinedCalendar();
    }

    private void initializeFilters() {
        try {
            // Load room types from database
            loadRoomTypesFromDatabase();
            
            // Load room numbers from database
            loadRoomNumbersFromDatabase();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load room data from database. Using default values.", Alert.AlertType.WARNING);
            setDefaultFilterValues();
        }
    }

    private void loadRoomTypesFromDatabase() {
        try {
            allRoomTypes.clear();
            allRoomTypes.add("All Categories");
            
            List<String> dbRoomTypes = roomDAO.findAllRoomTypes();
            if (dbRoomTypes != null && !dbRoomTypes.isEmpty()) {
                allRoomTypes.addAll(dbRoomTypes);
            } else {
                // Fallback if no data in database
                allRoomTypes.addAll(Arrays.asList("Single", "Double", "Deluxe", "Suite", "Twin"));
            }
            
            roomCategoryFilter.setItems(allRoomTypes);
            roomCategoryFilter.getSelectionModel().selectFirst();
            
        } catch (Exception e) {
            e.printStackTrace();
            setDefaultRoomTypes();
        }
    }

    private void loadRoomNumbersFromDatabase() {
        try {
            allRoomNumbers.clear();
            allRoomNumbers.add("All Rooms");
            
            List<String> dbRoomNumbers = roomDAO.findAllRoomNos();
            if (dbRoomNumbers != null && !dbRoomNumbers.isEmpty()) {
                // Sort room numbers numerically
                dbRoomNumbers.sort((a, b) -> {
                    try {
                        return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                    } catch (NumberFormatException e) {
                        return a.compareTo(b);
                    }
                });
                allRoomNumbers.addAll(dbRoomNumbers);
            }
            
            roomNumberFilter.setItems(allRoomNumbers);
            roomNumberFilter.getSelectionModel().selectFirst();
            
        } catch (Exception e) {
            e.printStackTrace();
            setDefaultRoomNumbers();
        }
    }

    private void setDefaultFilterValues() {
        setDefaultRoomTypes();
        setDefaultRoomNumbers();
    }

    private void setDefaultRoomTypes() {
        allRoomTypes.clear();
        allRoomTypes.addAll(Arrays.asList("All Categories", "Single", "Double", "Deluxe", "Suite", "Twin"));
        roomCategoryFilter.setItems(allRoomTypes);
        roomCategoryFilter.getSelectionModel().selectFirst();
    }

    private void setDefaultRoomNumbers() {
        allRoomNumbers.clear();
        allRoomNumbers.addAll(Arrays.asList("All Rooms", "101", "102", "201", "202", "301", "302"));
        roomNumberFilter.setItems(allRoomNumbers);
        roomNumberFilter.getSelectionModel().selectFirst();
    }

    private void setupFilterListeners() {
        // Date filter listeners with debouncing
        checkInDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Ensure check-out is after check-in
                if (checkOutDate.getValue() != null && !checkOutDate.getValue().isAfter(newVal)) {
                    checkOutDate.setValue(newVal.plusDays(1));
                }
                generateCombinedCalendar();
            }
        });

        checkOutDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Ensure check-out is after check-in
                if (checkInDate.getValue() != null && !newVal.isAfter(checkInDate.getValue())) {
                    checkInDate.setValue(newVal.minusDays(1));
                }
                generateCombinedCalendar();
            }
        });

        // Room category filter listener
        roomCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterRoomNumbersByCategory(newVal);
            generateCombinedCalendar();
        });

        // Room number filter listener
        roomNumberFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            generateCombinedCalendar();
        });

        // Search bar listener with debouncing
        if (searchBar != null) {
            searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
                // Simple debouncing - only search after user stops typing for a moment
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            if (searchBar.getText().equals(newVal)) {
                                generateCombinedCalendar();
                            }
                        });
                    }
                }, 300); // 300ms delay
            });
        }
    }

    private void filterRoomNumbersByCategory(String selectedCategory) {
        try {
            if (selectedCategory == null || "All Categories".equals(selectedCategory)) {
                // Show all room numbers
                loadRoomNumbersFromDatabase();
            } else {
                // Filter room numbers by category
                List<String> filteredRooms = roomDAO.findRoomNumbersByType(selectedCategory);
                
                allRoomNumbers.clear();
                allRoomNumbers.add("All Rooms");
                if (filteredRooms != null && !filteredRooms.isEmpty()) {
                    // Sort filtered rooms
                    filteredRooms.sort((a, b) -> {
                        try {
                            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                        } catch (NumberFormatException e) {
                            return a.compareTo(b);
                        }
                    });
                    allRoomNumbers.addAll(filteredRooms);
                }
                
                roomNumberFilter.setItems(allRoomNumbers);
                roomNumberFilter.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // If database query fails, keep current room numbers
        }
    }

    // --- Filter Action Methods ---
    @FXML
    private void onClearFilters() {
        checkInDate.setValue(LocalDate.now());
        checkOutDate.setValue(LocalDate.now().plusDays(NUM_DAYS_DISPLAYED));
        roomCategoryFilter.getSelectionModel().selectFirst();
        roomNumberFilter.getSelectionModel().selectFirst();
        if (searchBar != null) searchBar.clear();
        
        // Reload all room data
        loadRoomNumbersFromDatabase();
        generateCombinedCalendar();
        
        showAlert("Filters Reset", "All filters have been reset to default values.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void onFilterToday() {
        LocalDate today = LocalDate.now();
        checkInDate.setValue(today);
        checkOutDate.setValue(today.plusDays(1));
    }

    @FXML
    private void onFilterWeek() {
        LocalDate start = LocalDate.now();
        checkInDate.setValue(start);
        checkOutDate.setValue(start.plusDays(7));
    }

    @FXML
    private void onFilterMonth() {
        LocalDate start = LocalDate.now();
        checkInDate.setValue(start);
        checkOutDate.setValue(start.plusDays(30));
    }

    @FXML
    private void onSearch() {
        generateCombinedCalendar();
        if (searchBar != null && searchBar.getText() != null && !searchBar.getText().trim().isEmpty()) {
            showAlert("Search Applied", "Results filtered for: \"" + searchBar.getText().trim() + "\"", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void onRefreshData() {
        try {
            // Reload all data from database
            initializeFilters();
            generateCombinedCalendar();
            showAlert("Data Refreshed", "Room data has been refreshed from the database.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Refresh Error", "Failed to refresh data from database.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onNewReservation() {
        openReservationFormWindow(null, null, null, null, null);
    }

    // --- Calendar Navigation Methods ---
    @FXML
    private void onCalendarPrevious() {
        // Calculate previous date range
        LocalDate newEnd = currentCalendarStart;
        LocalDate newStart = newEnd.minusDays(NUM_DAYS_DISPLAYED);
        
        // Don't allow going too far in the past (optional restriction)
        LocalDate minimumStart = LocalDate.now().minusDays(30); // Allow 30 days in the past
        if (newStart.isBefore(minimumStart)) {
            newStart = minimumStart;
            newEnd = newStart.plusDays(NUM_DAYS_DISPLAYED);
        }
        
        // Update the date pickers
        checkInDate.setValue(newStart);
        checkOutDate.setValue(newEnd);
        
        // Update internal tracking
        currentCalendarStart = newStart;
        currentCalendarEnd = newEnd;
        
        // Calendar will be regenerated automatically by the listeners
    }

    @FXML
    private void onCalendarNext() {
        // Calculate next date range
        LocalDate newStart = currentCalendarEnd;
        LocalDate newEnd = newStart.plusDays(NUM_DAYS_DISPLAYED);
        
        // Optional: Limit how far in the future we can go
        LocalDate maximumEnd = LocalDate.now().plusDays(365); // Allow 1 year ahead
        if (newEnd.isAfter(maximumEnd)) {
            newEnd = maximumEnd;
            newStart = newEnd.minusDays(NUM_DAYS_DISPLAYED);
        }
        
        // Update the date pickers
        checkInDate.setValue(newStart);
        checkOutDate.setValue(newEnd);
        
        // Update internal tracking
        currentCalendarStart = newStart;
        currentCalendarEnd = newEnd;
        
        // Calendar will be regenerated automatically by the listeners
    }

    @FXML
    private void onCalendarToday() {
        LocalDate today = LocalDate.now();
        LocalDate newStart = today;
        LocalDate newEnd = today.plusDays(NUM_DAYS_DISPLAYED);
        
        // Update the date pickers
        checkInDate.setValue(newStart);
        checkOutDate.setValue(newEnd);
        
        // Update internal tracking
        currentCalendarStart = newStart;
        currentCalendarEnd = newEnd;
        
        // Calendar will be regenerated automatically by the listeners
    }

    // --- Calendar Generation ---
    private LocalDate normalizeEnd(LocalDate start, LocalDate end) {
        if (start == null) start = LocalDate.now();
        if (end == null) end = start.plusDays(NUM_DAYS_DISPLAYED);
        if (!end.isAfter(start)) end = start.plusDays(1);
        return end;
    }

    private void generateCombinedCalendar() {
        calendarGrid.getChildren().clear();
        
        LocalDate start = (checkInDate.getValue() != null) ? checkInDate.getValue() : LocalDate.now();
        LocalDate end = normalizeEnd(start, checkOutDate.getValue());
        int days = (int) Math.max(1, ChronoUnit.DAYS.between(start, end));
        if (days > NUM_DAYS_DISPLAYED) days = NUM_DAYS_DISPLAYED;
        LocalDate finalEnd = start.plusDays(days);

        // Update internal tracking
        currentCalendarStart = start;
        currentCalendarEnd = finalEnd;

        // Update check-out date if needed
        if (checkOutDate.getValue() == null || !finalEnd.equals(checkOutDate.getValue())) {
            checkOutDate.setValue(finalEnd);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd");
        
        // Get filter values
        String selectedType = roomCategoryFilter.getValue();
        String selectedRoom = roomNumberFilter.getValue();
        String searchTerm = (searchBar != null && searchBar.getText() != null) ? searchBar.getText().trim() : "";

        // Normalize filter values
        String typeFilter = (selectedType == null || "All Categories".equals(selectedType)) ? null : selectedType;
        String roomFilter = (selectedRoom == null || "All Rooms".equals(selectedRoom)) ? null : selectedRoom;

        // Create calendar header
        createCalendarHeader(start, days, dateFormatter);

        // Get filtered rooms from database
        List<String> rooms = getFilteredRooms(typeFilter, roomFilter);

        // Get booking and reservation data
        Map<String, List<Booking>> bookingsByRoom = getBookingsData(start, finalEnd, typeFilter, roomFilter, searchTerm);
        Map<String, List<ReservationStub>> reservationsByRoom = getReservationsData(start, finalEnd, typeFilter, roomFilter, searchTerm);

        // Create calendar grid
        createCalendarGrid(rooms, start, days, bookingsByRoom, reservationsByRoom);
        
        // Update date range display
        updateDateRangeDisplay();
    }

    private void createCalendarHeader(LocalDate start, int days, DateTimeFormatter dateFormatter) {
        Label roomHeaderLabel = new Label("Room / Date");
        roomHeaderLabel.getStyleClass().add("calendar-header-corner");
        GridPane.setHalignment(roomHeaderLabel, javafx.geometry.HPos.CENTER);
        GridPane.setValignment(roomHeaderLabel, javafx.geometry.VPos.CENTER);
        calendarGrid.add(roomHeaderLabel, 0, 0);

        for (int col = 0; col < days; col++) {
            LocalDate date = start.plusDays(col);
            Label dateLabel = new Label(date.format(dateFormatter));
            dateLabel.getStyleClass().add("calendar-header-day");
            GridPane.setHalignment(dateLabel, javafx.geometry.HPos.CENTER);
            calendarGrid.add(dateLabel, col + 1, 0);
        }
    }

    private List<String> getFilteredRooms(String typeFilter, String roomFilter) {
        try {
            if (roomFilter != null) {
                return List.of(roomFilter);
            } else if (typeFilter != null) {
                List<String> roomsByType = roomDAO.findRoomNumbersByType(typeFilter);
                return roomsByType != null ? roomsByType : new ArrayList<>();
            } else {
                List<String> allRooms = roomDAO.findAllRoomNos();
                return allRooms != null ? allRooms : new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load room data. Please check your database connection.", Alert.AlertType.ERROR);
            return new ArrayList<>();
        }
    }

    private Map<String, List<Booking>> getBookingsData(LocalDate start, LocalDate end, String typeFilter, String roomFilter, String searchTerm) {
        try {
            return reservationServiceDAO.findBookingsOverlappingByRoom(start, end, typeFilter, roomFilter, searchTerm);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private Map<String, List<ReservationStub>> getReservationsData(LocalDate start, LocalDate end, String typeFilter, String roomFilter, String searchTerm) {
        try {
            return reservationsDAO.findReservationsOverlappingByRoom(start, end, typeFilter, roomFilter, searchTerm);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private void createCalendarGrid(List<String> rooms, LocalDate start, int days, 
                                   Map<String, List<Booking>> bookingsByRoom, 
                                   Map<String, List<ReservationStub>> reservationsByRoom) {
        int currentRow = 1;
        for (String room : rooms) {
            Label roomLabel = new Label("Room " + room);
            roomLabel.getStyleClass().add("calendar-header-roomnumber");
            GridPane.setHalignment(roomLabel, javafx.geometry.HPos.LEFT);
            GridPane.setValignment(roomLabel, javafx.geometry.VPos.CENTER);
            calendarGrid.add(roomLabel, 0, currentRow);

            List<Booking> roomBookings = bookingsByRoom.getOrDefault(room, Collections.emptyList());
            List<ReservationStub> roomReservations = reservationsByRoom.getOrDefault(room, Collections.emptyList());

            for (int col = 0; col < days; col++) {
                LocalDate day = start.plusDays(col);
                StackPane cell = createCalendarCell(room, day, roomBookings, roomReservations);
                System.out.println(room);
                calendarGrid.add(cell, col + 1, currentRow);
            }
            currentRow++;
        }
    }

    private StackPane createCalendarCell(String roomNo, LocalDate date, List<Booking> roomBookings, List<ReservationStub> roomReservations) {
        Long bookingId = null;
        Long reservationId = null;
        String cellText = "";
        String styleClass = "cell-available";

        // Check for bookings
        if (roomBookings != null) {
            for (Booking b : roomBookings) {
                if (b != null && b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    if (!date.isBefore(b.getCheckInDate()) && date.isBefore(b.getCheckOutDate())) {
                        bookingId = b.getId();
                        cellText = "Ocupied";
                        styleClass = "cell-booked";
                        break;
                    }
                }
            }
        }

        // Check for reservations if no booking found
        if (bookingId == null && roomReservations != null) {
            for (ReservationStub r : roomReservations) {
                if (r != null && r.getCheckInDate() != null && r.getCheckOutDate() != null) {
                    if (!date.isBefore(r.getCheckInDate()) && date.isBefore(r.getCheckOutDate())) {
                        reservationId = r.getId();
                        cellText = "Reserved";
                        styleClass = "cell-reserved";
                        break;
                    }
                }
            }
        }

        StackPane cell = new StackPane();
        cell.getStyleClass().add("cell");
        if (!"cell-available".equals(styleClass)) {
            cell.getStyleClass().add(styleClass);
        }

        Label content = new Label(cellText);
        content.getStyleClass().add("cell-text");
        cell.getChildren().add(content);
        cell.setPrefSize(110, 54);

        // Cell click handler
        final Long clickBookingId = bookingId;
        final Long clickReservationId = reservationId;
        cell.setOnMouseClicked(e -> handleCellClick(clickBookingId, clickReservationId, roomNo, date));

        return cell;
    }

    private void handleCellClick(Long bookingId, Long reservationId, String roomNo, LocalDate date) {
        if (bookingId != null) {
            // Show booking information
            showBookingInfo(bookingId);
        } else if (reservationId != null) {
            // Open reservation form for editing
            openReservationFormWindow("edit", reservationId, roomNo, date, null);
        } else {
            // Open reservation form for new reservation
            openReservationFormWindow("new", null, roomNo, date, date.plusDays(1));
        }
    }

    private void showBookingInfo(Long bookingId) {
        try {
            application.services.dao.BookingDAO bookingDAO = new application.services.dao.BookingDAO();
            Booking b = bookingDAO.getBookingById(bookingId);
            if (b != null) {
                Long guestId = b.getGuestId();
                Guest guest = (guestId != null) ? guestDAO.getGuestById(guestId) : null;
                String guestName = (guest != null && guest.getName() != null) ? guest.getName() : "Guest";
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Room Booking Information");
                alert.setHeaderText("Booking Details");
                alert.setContentText(String.format(
                    "Room %s is occupied by %s\nCheck-in: %s\nCheck-out: %s\nBooking ID: %d",
                    b.getRoomNo(), guestName, b.getCheckInDate(), b.getCheckOutDate(), b.getId()
                ));
                alert.showAndWait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Booking Info", "Room is currently booked.", Alert.AlertType.INFORMATION);
        }
    }

    private void openReservationFormWindow(String mode, Long reservationId, String roomNo, LocalDate checkIn, LocalDate checkOut) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/ReservationForm.fxml"));
            Parent root = loader.load();
            
            ReservationFormController controller = loader.getController();
            controller.setParentController(this);
            
            if ("edit".equals(mode) && reservationId != null) {
                controller.loadReservationForEdit(reservationId);
            } else {
                controller.initializeForNewReservation(roomNo, checkIn, checkOut);
            }
            
            Stage stage = new Stage();
            stage.setTitle("Reservation Management");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(calendarGrid.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();
            
            generateCombinedCalendar();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open reservation form.", Alert.AlertType.ERROR);
        }
    }

    private void updateDateRangeDisplay() {
        if (dateRangeLabel != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
            String rangeText = currentCalendarStart.format(formatter) + " - " + 
                              currentCalendarEnd.format(formatter) + ", " + 
                              currentCalendarStart.getYear();
            dateRangeLabel.setText(rangeText);
        }
    }

    private void showAlert(String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Reservation System");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
