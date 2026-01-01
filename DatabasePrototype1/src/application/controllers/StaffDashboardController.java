// src/main/java/application/controllers/housekeeping/StaffDashboardController.java
package application.controllers;

import application.models.UserDetail;
import application.models.HousekeepingTask;
import application.services.HousekeepingService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class StaffDashboardController {

    @FXML private VBox rootLayout;
    @FXML private Label staffNameLabel;
    @FXML private Label staffDesignationLabel;
    @FXML private Label staffFloorShiftLabel;
    @FXML private TextField searchField;
    @FXML private ToggleButton darkModeToggle;

    @FXML private TableView<HousekeepingTask> assignedTaskTable;
    @FXML private TableColumn<HousekeepingTask, String> roomNoColumn;
    @FXML private TableColumn<HousekeepingTask, String> taskTypeColumn;
    @FXML private TableColumn<HousekeepingTask, String> statusColumn;
    @FXML private TableColumn<HousekeepingTask, String> priorityColumn;
    @FXML private TableColumn<HousekeepingTask, String> assignedAtColumn;
    @FXML private TableColumn<HousekeepingTask, Void> actionColumn;

    @FXML private TableView<HousekeepingTask> taskHistoryTable;
    @FXML private TableColumn<HousekeepingTask, String> historyRoomNoColumn;
    @FXML private TableColumn<HousekeepingTask, String> historyTaskTypeColumn;
    @FXML private TableColumn<HousekeepingTask, String> historyStatusColumn;
    @FXML private TableColumn<HousekeepingTask, String> historyAssignedAtColumn;
    @FXML private TableColumn<HousekeepingTask, String> historyCompletedAtColumn;

    private String currentStaffName;
    private final ObservableList<HousekeepingTask> assignedTasks = FXCollections.observableArrayList();
    private final ObservableList<HousekeepingTask> taskHistory = FXCollections.observableArrayList();
    private final Map<HousekeepingTask, Timeline> blinkingTimelines = new HashMap<>();

    private HousekeepingService housekeepingService;

    @FXML
    public void initialize() {
        housekeepingService = new HousekeepingService();
        
        initializeTableColumns();
        setupTableFactories();
        setupEventHandlers();
    }

    private void initializeTableColumns() {
        // Assigned tasks table
        roomNoColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
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

        // History table
        historyRoomNoColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        historyTaskTypeColumn.setCellValueFactory(new PropertyValueFactory<>("taskType"));
        historyStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        historyAssignedAtColumn.setCellValueFactory(cellData -> {
            LocalDateTime assignedTime = cellData.getValue().getAssignedAt();
            if (assignedTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");
                return new SimpleStringProperty(assignedTime.format(formatter));
            }
            return new SimpleStringProperty("");
        });

        historyCompletedAtColumn.setCellValueFactory(cellData -> {
            LocalDateTime completedTime = cellData.getValue().getCompletedAt();
            if (completedTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");
                return new SimpleStringProperty(completedTime.format(formatter));
            }
            return new SimpleStringProperty("N/A");
        });
    }

    private void setupTableFactories() {
        setupStaffActionColumnButtons();
        setupStatusBadgeFactory(assignedTaskTable, statusColumn);
        setupStatusBadgeFactory(taskHistoryTable, historyStatusColumn);
        setupPriorityCircleFactory(assignedTaskTable, priorityColumn);
        setupTableRowStyling(assignedTaskTable);
        setupTableRowStyling(taskHistoryTable);
    }

    private void setupEventHandlers() {
        darkModeToggle.setOnAction(event -> handleDarkModeToggle());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTasksForStaff());
    }

    public void setStaffDetails(UserDetail userDetail) {
        this.currentStaffName = userDetail.getName();
        staffNameLabel.setText("Logged in as: " + userDetail.getName());
        staffDesignationLabel.setText("Designation: " + userDetail.getDesignation().getDisplayValue());
        staffFloorShiftLabel.setText("Floor: " + userDetail.getFloor() + ", Shift: " + userDetail.getShift());
        
        loadTasksForStaff();
    }

    private void loadTasksForStaff() {
        try {
            ObservableList<HousekeepingTask> allTasks = housekeepingService.getTasksForStaff(currentStaffName);
            
            assignedTasks.clear();
            taskHistory.clear();
            
            for (HousekeepingTask task : allTasks) {
                if ("Completed".equals(task.getStatus())) {
                    taskHistory.add(task);
                } else {
                    assignedTasks.add(task);
                }
            }
            
            filterTasksForStaff();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load tasks: " + e.getMessage());
        }
    }

    private void filterTasksForStaff() {
        ObservableList<HousekeepingTask> filteredAssigned = FXCollections.observableArrayList();
        ObservableList<HousekeepingTask> filteredHistory = FXCollections.observableArrayList();
        
        String searchText = searchField.getText();
        if (searchText == null) searchText = "";
        searchText = searchText.toLowerCase();

        // Filter assigned tasks
        for (HousekeepingTask task : assignedTasks) {
            boolean matchesSearch = searchText.isEmpty() ||
                task.getRoomNo().toLowerCase().contains(searchText) ||
                task.getTaskType().toLowerCase().contains(searchText);
                
            if (matchesSearch) {
                filteredAssigned.add(task);
            }
        }

        // Filter history
        for (HousekeepingTask task : taskHistory) {
            boolean matchesSearch = searchText.isEmpty() ||
                task.getRoomNo().toLowerCase().contains(searchText) ||
                task.getTaskType().toLowerCase().contains(searchText);
                
            if (matchesSearch) {
                filteredHistory.add(task);
            }
        }

        assignedTaskTable.setItems(filteredAssigned);
        taskHistoryTable.setItems(filteredHistory);
        
        assignedTaskTable.refresh();
        taskHistoryTable.refresh();
    }

    private void setupStaffActionColumnButtons() {
        Callback<TableColumn<HousekeepingTask, Void>, TableCell<HousekeepingTask, Void>> cellFactory = 
            param -> new TableCell<HousekeepingTask, Void>() {

            private final Button btnMarkDone = new Button("✅ Done");
            private final Button btnReportIssue = new Button("🛠 Issue");
            private final HBox pane = new HBox(5, btnMarkDone, btnReportIssue);

            {
                btnMarkDone.getStyleClass().add("action-button-primary");
                btnMarkDone.setTooltip(new Tooltip("Mark task as Completed"));
                
                btnReportIssue.getStyleClass().add("action-button-secondary");
                btnReportIssue.setTooltip(new Tooltip("Report an issue with this task"));

                btnMarkDone.setOnAction(event -> {
                    HousekeepingTask task = getTableRow().getItem();
                    if (task != null) {
                        task.setStatus("Completed");
                        task.setCompletedAt(LocalDateTime.now());
                        housekeepingService.saveTask(task);
                        loadTasksForStaff();
                    }
                });

                btnReportIssue.setOnAction(event -> {
                    HousekeepingTask task = getTableRow().getItem();
                    if (task != null) {
                        task.setStatus("Issue");
                        housekeepingService.saveTask(task);
                        loadTasksForStaff();
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
                    if (task != null && "Completed".equals(task.getStatus())) {
                        setGraphic(null);
                    } else if (task != null) {
                        btnMarkDone.setVisible(!"Completed".equals(task.getStatus()));
                        btnReportIssue.setVisible(!"Issue".equals(task.getStatus()) && 
                                                 !"Completed".equals(task.getStatus()));
                        setGraphic(pane);
                    }
                }
            }
        };
        
        actionColumn.setCellFactory(cellFactory);
    }

    private void setupStatusBadgeFactory(TableView<HousekeepingTask> table, 
                                        TableColumn<HousekeepingTask, String> column) {
        column.setCellFactory(col -> new TableCell<HousekeepingTask, String>() {
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

    private void setupPriorityCircleFactory(TableView<HousekeepingTask> table, 
                                          TableColumn<HousekeepingTask, String> column) {
        column.setCellFactory(col -> new TableCell<HousekeepingTask, String>() {
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

    private void setupTableRowStyling(TableView<HousekeepingTask> table) {
        table.setRowFactory(tv -> new TableRow<HousekeepingTask>() {
            @Override
            protected void updateItem(HousekeepingTask item, boolean empty) {
                super.updateItem(item, empty);
                
                stopBlinking(item);
                getStyleClass().removeAll("issue-row", "high-priority-row", "blinking");
                
                if (item != null && table == assignedTaskTable) {
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
