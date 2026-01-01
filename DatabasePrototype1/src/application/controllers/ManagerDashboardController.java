// src/main/java/application/controllers/housekeeping/ManagerDashboardController.java
package application.controllers;

import application.models.UserDetail;
import application.models.HousekeepingTask;
import application.models.RoomStatus;
import application.services.HousekeepingService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ManagerDashboardController {

    @FXML private VBox rootLayout;
    @FXML private Label managerNameLabel;
    @FXML private Label managerDesignationLabel;
    @FXML private Label managerFloorShiftLabel;
    @FXML private TextField searchField;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> autoRefreshDropdown;
    @FXML private ToggleButton darkModeToggle;
    @FXML private Button assignNewTaskButton;

    // Room Status Overview
    @FXML private FlowPane roomCardsFlowPane;

    private ScrollPane roomCardsScrollPane; // Add this field
    @FXML private ComboBox<String> roomStatusFilterDropdown;
    @FXML private Button refreshRoomStatusButton;

    // Task Table
    @FXML private TableView<HousekeepingTask> taskTable;
    @FXML private TableColumn<HousekeepingTask, String> roomNoColumn;
    @FXML private TableColumn<HousekeepingTask, String> staffColumn;
    @FXML private TableColumn<HousekeepingTask, String> taskTypeColumn;
    @FXML private TableColumn<HousekeepingTask, String> statusColumn;
    @FXML private TableColumn<HousekeepingTask, String> priorityColumn;
    @FXML private TableColumn<HousekeepingTask, String> assignedAtColumn;
    @FXML private TableColumn<HousekeepingTask, Void> actionColumn;

    private ToggleGroup filterToggleGroup;
    private final ObservableList<HousekeepingTask> taskData = FXCollections.observableArrayList();
    private final ObservableList<HousekeepingTask> filteredTaskData = FXCollections.observableArrayList();
    private final ObservableList<RoomStatus> allRoomStatuses = FXCollections.observableArrayList();
    private final ObservableList<RoomStatus> filteredRoomStatuses = FXCollections.observableArrayList();
    private final Map<HousekeepingTask, Timeline> blinkingTimelines = new HashMap<>();

    private HousekeepingService housekeepingService;
    @FXML
    public void initialize() {
        housekeepingService = new HousekeepingService();
        
        // Wrap FlowPane in ScrollPane
        setupScrollableRoomCards();
        
        initializeTableColumns();
        setupTableFactories();
        initializeTopBarControls();
        initializeFilterToggles();
        initializeRoomStatusOverview();
        setupEventHandlers();

        // Load data from database
        loadTaskData();
        updateRoomStatusCards();
    }
    private void initializeTableColumns() {
        roomNoColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        staffColumn.setCellValueFactory(new PropertyValueFactory<>("assignedStaff"));
        taskTypeColumn.setCellValueFactory(new PropertyValueFactory<>("taskType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        
        assignedAtColumn.setCellValueFactory(cellData -> {
            LocalDateTime assignedTime = cellData.getValue().getAssignedAt();
            if (assignedTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");
                return new SimpleStringProperty(assignedTime.format(formatter));
            }
            return new SimpleStringProperty("");
        });
    }

    private void setupTableFactories() {
        setupStatusBadgeFactory();
        setupPriorityCircleFactory();
        setupActionColumnButtons();
        setupTableRowStyling();
    }

    private void initializeTopBarControls() {
        ObservableList<String> refreshIntervals = FXCollections.observableArrayList(
            "Off", "15 Sec", "30 Sec", "1 Min", "5 Min"
        );
        autoRefreshDropdown.setItems(refreshIntervals);
        autoRefreshDropdown.getSelectionModel().select("Off");
    }

    private void initializeFilterToggles() {
        filterToggleGroup = new ToggleGroup();
        // Find filter toggles in the UI and set up toggle group
        // This would need to be implemented based on your FXML structure
    }
    private void setupScrollableRoomCards() {
        // Create ScrollPane and wrap the FlowPane
        roomCardsScrollPane = new ScrollPane();
        roomCardsScrollPane.setContent(roomCardsFlowPane);
        
        // Configure ScrollPane properties
        roomCardsScrollPane.setFitToWidth(true);
        roomCardsScrollPane.setFitToHeight(false);
        roomCardsScrollPane.setPannable(true);
        roomCardsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        roomCardsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Set preferred viewport size
        roomCardsScrollPane.setPrefViewportHeight(300); // Adjust as needed
        
        // Get the parent container and replace FlowPane with ScrollPane
        Parent parent = roomCardsFlowPane.getParent();
        if (parent instanceof VBox) {
            VBox vbox = (VBox) parent;
            int index = vbox.getChildren().indexOf(roomCardsFlowPane);
            vbox.getChildren().remove(roomCardsFlowPane);
            vbox.getChildren().add(index, roomCardsScrollPane);
        } else if (parent instanceof AnchorPane) {
            AnchorPane anchorPane = (AnchorPane) parent;
            
            // Copy layout constraints from original FlowPane
            Double topAnchor = AnchorPane.getTopAnchor(roomCardsFlowPane);
            Double bottomAnchor = AnchorPane.getBottomAnchor(roomCardsFlowPane);
            Double leftAnchor = AnchorPane.getLeftAnchor(roomCardsFlowPane);
            Double rightAnchor = AnchorPane.getRightAnchor(roomCardsFlowPane);
            
            anchorPane.getChildren().remove(roomCardsFlowPane);
            anchorPane.getChildren().add(roomCardsScrollPane);
            
            // Apply constraints to ScrollPane
            if (topAnchor != null) AnchorPane.setTopAnchor(roomCardsScrollPane, topAnchor);
            if (bottomAnchor != null) AnchorPane.setBottomAnchor(roomCardsScrollPane, bottomAnchor);
            if (leftAnchor != null) AnchorPane.setLeftAnchor(roomCardsScrollPane, leftAnchor);
            if (rightAnchor != null) AnchorPane.setRightAnchor(roomCardsScrollPane, rightAnchor);
        }
        
        // Style the ScrollPane (optional)
        roomCardsScrollPane.getStyleClass().add("room-cards-scroll-pane");
    }

    private void initializeRoomStatusOverview() {
        ObservableList<String> statusFilters = FXCollections.observableArrayList(
            "All", "Dirty", "Needs Maintenance", "Occupied", "Vacant & Clean", "Pending Task"
        );
        roomStatusFilterDropdown.setItems(statusFilters);
        roomStatusFilterDropdown.getSelectionModel().select("All");
    }

    private void setupEventHandlers() {
        assignNewTaskButton.setOnAction(event -> openAssignTaskWindow(null));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTasks());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTasks());
        refreshRoomStatusButton.setOnAction(event -> {
            loadTaskData();
            updateRoomStatusCards();
        });
        roomStatusFilterDropdown.valueProperty().addListener((obs, oldVal, newVal) -> filterRoomStatusCards());
        darkModeToggle.setOnAction(event -> handleDarkModeToggle());
    }

    private void loadTaskData() {
        try {
            taskData.clear();
            ObservableList<HousekeepingTask> tasks = housekeepingService.getAllTasks();
            taskData.addAll(tasks);
            filterTasks();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load tasks: " + e.getMessage());
        }
    }

    public void setManagerDetails(UserDetail userDetail) {
        managerNameLabel.setText("Logged in as: " + userDetail.getName());
        managerDesignationLabel.setText("Designation: " + userDetail.getDesignation().getDisplayValue());
        managerFloorShiftLabel.setText("Floor: " + userDetail.getFloor() + ", Shift: " + userDetail.getShift());
    }

    private void updateRoomStatusCards() {
        try {
            allRoomStatuses.clear();
            
            // Get all rooms from database
            ObservableList<String> allRooms = housekeepingService.getAllRoomNumbers();
            
            for (String roomNo : allRooms) {
                String overallRoomStatus = determineRoomStatus(roomNo);
                allRoomStatuses.add(new RoomStatus(roomNo, overallRoomStatus));
            }
            
            filterRoomStatusCards();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String determineRoomStatus(String roomNo) {
        // Check for issues or maintenance
        boolean hasIssue = taskData.stream().anyMatch(task -> 
            task.getRoomNo().equals(roomNo) && 
            ("Issue".equals(task.getStatus()) || "Maintenance".equals(task.getTaskType())) &&
            !"Completed".equals(task.getStatus())
        );
        
        if (hasIssue) {
            return "Needs Maintenance";
        }

        // Check for cleaning tasks
        boolean hasDirtyTask = taskData.stream().anyMatch(task -> 
            task.getRoomNo().equals(roomNo) && 
            "Cleaning".equals(task.getTaskType()) &&
            !"Completed".equals(task.getStatus())
        );
        
        if (hasDirtyTask) {
            return "Dirty";
        }

        // Check for any pending tasks
        boolean hasPendingTask = taskData.stream().anyMatch(task -> 
            task.getRoomNo().equals(roomNo) && 
            ("Pending".equals(task.getStatus()) || "In Progress".equals(task.getStatus()))
        );
        
        if (hasPendingTask) {
            return "Pending Task";
        }

        return "Vacant & Clean";
    }

    private void filterRoomStatusCards() {
        filteredRoomStatuses.clear();
        String currentFilter = roomStatusFilterDropdown.getValue();
        
        for (RoomStatus rs : allRoomStatuses) {
            if ("All".equals(currentFilter) || rs.getStatus().equals(currentFilter)) {
                filteredRoomStatuses.add(rs);
            }
        }
        
        renderRoomCards();
    }
    private void renderRoomCards() {
        roomCardsFlowPane.getChildren().clear();
        
        // Set preferred width for FlowPane to enable proper wrapping
        roomCardsFlowPane.setPrefWrapLength(Double.MAX_VALUE);
        
        for (RoomStatus rs : filteredRoomStatuses) {
            VBox roomCard = createRoomCard(rs);
            roomCardsFlowPane.getChildren().add(roomCard);
        }
    }

    private VBox createRoomCard(RoomStatus rs) {
        VBox card = new VBox(5);
        card.getStyleClass().addAll("room-card", 
            "room-card-" + rs.getStatus().toLowerCase().replace(" ", "-"));
        card.setPadding(new Insets(10));
        card.setPrefSize(120, 90);
        card.setAlignment(javafx.geometry.Pos.CENTER);

        Label roomNoLabel = new Label(rs.getRoomNo());
        roomNoLabel.getStyleClass().add("room-card-roomno");

        Label statusLabel = new Label(rs.getStatus());
        statusLabel.getStyleClass().add("room-card-status");

        card.getChildren().addAll(roomNoLabel, statusLabel);
        card.setOnMouseClicked(event -> openAssignTaskWindow(rs.getRoomNo()));

        return card;
    }

    private void setupStatusBadgeFactory() {
        statusColumn.setCellFactory(column -> new TableCell<HousekeepingTask, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label statusBadge = new Label(status);
                    statusBadge.getStyleClass().add("status-badge");
                    
                    switch(status) {
                        case "Completed": statusBadge.getStyleClass().add("status-done"); break;
                        case "In Progress": statusBadge.getStyleClass().add("status-inprogress"); break;
                        case "Issue": statusBadge.getStyleClass().add("status-issue"); break;
                        case "Pending": statusBadge.getStyleClass().add("status-pending"); break;
                        default: statusBadge.getStyleClass().add("status-pending"); break;
                    }
                    
                    setGraphic(statusBadge);
                    setText(null);
                }
            }
        });
    }

    private void setupPriorityCircleFactory() {
        priorityColumn.setCellFactory(column -> new TableCell<HousekeepingTask, String>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Circle circle = new Circle(6);
                    circle.getStyleClass().add("priority-circle");
                    
                    switch (priority) {
                        case "High": circle.getStyleClass().add("priority-high-fill"); break;
                        case "Medium": circle.getStyleClass().add("priority-medium-fill"); break;
                        case "Low": circle.getStyleClass().add("priority-low-fill"); break;
                        default: circle.getStyleClass().add("priority-low-fill"); break;
                    }
                    
                    setGraphic(circle);
                    setText(null);
                }
            }
        });
    }

    private void setupActionColumnButtons() {
        Callback<TableColumn<HousekeepingTask, Void>, TableCell<HousekeepingTask, Void>> cellFactory = 
            param -> new TableCell<HousekeepingTask, Void>() {
            
            private final Button btnMarkDone = new Button("✅");
            private final Button btnReportIssue = new Button("🛠");
            private final Button btnUpdate = new Button("🔁");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(5, btnMarkDone, btnReportIssue, btnUpdate, btnDelete);

            {
                btnMarkDone.getStyleClass().add("icon-action-button");
                btnMarkDone.setTooltip(new Tooltip("Mark as Completed"));
                
                btnReportIssue.getStyleClass().add("icon-action-button");
                btnReportIssue.setTooltip(new Tooltip("Report Issue"));
                
                btnUpdate.getStyleClass().add("icon-action-button");
                btnUpdate.setTooltip(new Tooltip("Update Task"));
                
                btnDelete.getStyleClass().add("icon-action-button");
                btnDelete.setTooltip(new Tooltip("Delete Task"));

                btnMarkDone.setOnAction(event -> {
                    HousekeepingTask task = getTableRow().getItem();
                    if (task != null) {
                        task.setStatus("Completed");
                        task.setCompletedAt(LocalDateTime.now());
                        housekeepingService.saveTask(task);
                        loadTaskData();
                        updateRoomStatusCards();
                    }
                });

                btnReportIssue.setOnAction(event -> {
                    HousekeepingTask task = getTableRow().getItem();
                    if (task != null) {
                        task.setStatus("Issue");
                        housekeepingService.saveTask(task);
                        loadTaskData();
                        updateRoomStatusCards();
                    }
                });

                btnUpdate.setOnAction(event -> {
                    HousekeepingTask task = getTableRow().getItem();
                    if (task != null) {
                        openAssignTaskWindow(task);
                    }
                });

                btnDelete.setOnAction(event -> {
                    HousekeepingTask task = getTableRow().getItem();
                    if (task != null && confirmDelete(task)) {
                        if (task.getId() != null) {
                            housekeepingService.deleteTask(task.getId());
                        }
                        loadTaskData();
                        updateRoomStatusCards();
                    }
                });

                pane.getStyleClass().add("button-group");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HousekeepingTask task = getTableRow().getItem();
                    if (task != null) {
                        btnMarkDone.setVisible(!"Completed".equals(task.getStatus()));
                        btnReportIssue.setVisible(!"Issue".equals(task.getStatus()) && 
                                                 !"Completed".equals(task.getStatus()));
                    }
                    setGraphic(pane);
                }
            }
        };
        
        actionColumn.setCellFactory(cellFactory);
    }

    private boolean confirmDelete(HousekeepingTask task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Task");
        alert.setContentText("Are you sure you want to delete this task for room " + task.getRoomNo() + "?");
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void setupTableRowStyling() {
        taskTable.setRowFactory(tv -> new TableRow<HousekeepingTask>() {
            @Override
            protected void updateItem(HousekeepingTask item, boolean empty) {
                super.updateItem(item, empty);
                
                stopBlinking(item);
                getStyleClass().removeAll("issue-row", "high-priority-row", "blinking");
                
                if (item != null) {
                    if ("Issue".equals(item.getStatus())) {
                        getStyleClass().add("issue-row");
                        startBlinking(this, "issue-row");
                    } else if ("High".equals(item.getPriority()) && 
                              !"Completed".equals(item.getStatus())) {
                        getStyleClass().add("high-priority-row");
                        startBlinking(this, "high-priority-row");
                    }
                }
            }
        });
    }

    private void startBlinking(TableRow<HousekeepingTask> row, String baseStyleClass) {
        if (row.getItem() == null) return;
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, e -> row.getStyleClass().add("blinking")),
            new KeyFrame(Duration.millis(750), e -> row.getStyleClass().remove("blinking"))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(false);
        timeline.play();
        
        blinkingTimelines.put(row.getItem(), timeline);
    }

    private void stopBlinking(HousekeepingTask task) {
        if (task != null && blinkingTimelines.containsKey(task)) {
            Timeline timeline = blinkingTimelines.remove(task);
            if (timeline != null) {
                timeline.stop();
            }
        }
    }

    private void openAssignTaskWindow(Object item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml1/assign-task-modal.fxml"));
            AnchorPane assignTaskLayout = loader.load();
            AssignTaskController assignTaskController = loader.getController();

            if (item instanceof HousekeepingTask) {
                assignTaskController.loadTaskForEditing((HousekeepingTask) item);
            } else if (item instanceof String) {
                assignTaskController.loadTaskForNewAssignment((String) item);
            }

            Stage stage = new Stage();
            stage.setTitle(item instanceof HousekeepingTask ? "Update Housekeeping Task" : "Assign New Task");
            stage.setScene(new Scene(assignTaskLayout));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(rootLayout.getScene().getWindow());

            assignTaskController.setTaskUpdateCallback(updatedTask -> {
                loadTaskData();
                updateRoomStatusCards();
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the task assignment window.");
        }
    }

    private void filterTasks() {
        filteredTaskData.clear();
        String searchText = searchField.getText();
        if (searchText == null) searchText = "";
        searchText = searchText.toLowerCase();

        String selectedStatusFilter = "All";
        if (filterToggleGroup != null && filterToggleGroup.getSelectedToggle() != null) {
            selectedStatusFilter = ((ToggleButton) filterToggleGroup.getSelectedToggle())
                .getUserData().toString();
        }

        LocalDateTime selectedDate = dateFilter.getValue() != null ? 
            dateFilter.getValue().atStartOfDay() : null;

        for (HousekeepingTask task : taskData) {
            boolean matchesSearch = searchText.isEmpty() ||
                task.getRoomNo().toLowerCase().contains(searchText) ||
                task.getAssignedStaff().toLowerCase().contains(searchText) ||
                task.getTaskType().toLowerCase().contains(searchText);

            boolean matchesStatus = selectedStatusFilter.equals("All") || 
                task.getStatus().equals(selectedStatusFilter);

            boolean matchesDate = selectedDate == null || 
                (task.getAssignedAt() != null && 
                 task.getAssignedAt().toLocalDate().isEqual(selectedDate.toLocalDate()));

            if (matchesSearch && matchesStatus && matchesDate) {
                filteredTaskData.add(task);
            }
        }

        taskTable.setItems(filteredTaskData);
        taskTable.refresh();
    }

    private void handleDarkModeToggle() {
        if (rootLayout.getStyleClass().contains("dark")) {
            rootLayout.getStyleClass().remove("dark");
        } else {
            rootLayout.getStyleClass().add("dark");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
