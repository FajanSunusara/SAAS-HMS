// src/main/java/application/controllers/housekeeping/AssignTaskController.java
package application.controllers;

import application.models.HousekeepingTask;
import application.services.HousekeepingService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.function.Consumer;

public class AssignTaskController {

    @FXML private Label modalTitleLabel;
    @FXML private TextField roomNoField;
    @FXML private ComboBox<String> assignedStaffComboBox;
    @FXML private ComboBox<String> taskTypeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private TextArea notesTextArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private HousekeepingTask currentTask;
    private Consumer<HousekeepingTask> taskUpdateCallback;
    private HousekeepingService housekeepingService;

    @FXML
    public void initialize() {
        housekeepingService = new HousekeepingService();
        loadDropdownData();
        
        saveButton.setOnAction(event -> handleSaveTask());
        cancelButton.setOnAction(event -> closeWindow());
    }

    private void loadDropdownData() {
        try {
            // Load rooms
            ObservableList<String> rooms = housekeepingService.getAllRoomNumbers();
            
            // Load staff
            ObservableList<String> staff = housekeepingService.getAllStaffNames();
            assignedStaffComboBox.setItems(staff);

            // Load task types
            ObservableList<String> taskTypes = housekeepingService.getTaskTypes();
            taskTypeComboBox.setItems(taskTypes);

            // Load statuses
            ObservableList<String> statuses = housekeepingService.getStatuses();
            statusComboBox.setItems(statuses);

            // Load priorities
            ObservableList<String> priorities = housekeepingService.getPriorities();
            priorityComboBox.setItems(priorities);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dropdown data: " + e.getMessage());
        }
    }

    public void setTaskUpdateCallback(Consumer<HousekeepingTask> callback) {
        this.taskUpdateCallback = callback;
    }

    public void loadTaskForEditing(HousekeepingTask task) {
        this.currentTask = task;
        modalTitleLabel.setText("Update Housekeeping Task");
        
        roomNoField.setText(task.getRoomNo());
        roomNoField.setDisable(true);
        
        assignedStaffComboBox.getSelectionModel().select(task.getAssignedStaff());
        taskTypeComboBox.getSelectionModel().select(task.getTaskType());
        statusComboBox.getSelectionModel().select(task.getStatus());
        priorityComboBox.getSelectionModel().select(task.getPriority());
        notesTextArea.setText(task.getNotes());
    }

    public void loadTaskForNewAssignment(String roomNo) {
        this.currentTask = null;
        modalTitleLabel.setText("Assign New Task");
        
        roomNoField.setText(roomNo);
        roomNoField.setDisable(false);
        
        statusComboBox.getSelectionModel().select("Pending");
        priorityComboBox.getSelectionModel().select("Medium");
    }

    private void handleSaveTask() {
        // Validation
        String roomNo = roomNoField.getText();
        String assignedStaff = assignedStaffComboBox.getValue();
        String taskType = taskTypeComboBox.getValue();
        String status = statusComboBox.getValue();
        String priority = priorityComboBox.getValue();
        String notes = notesTextArea.getText();

        if (roomNo == null || roomNo.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a room number.");
            return;
        }

        if (assignedStaff == null || assignedStaff.isEmpty()) {
            showAlert("Validation Error", "Please select a staff member.");
            return;
        }

        if (taskType == null || taskType.isEmpty()) {
            showAlert("Validation Error", "Please select a task type.");
            return;
        }

        if (status == null || status.isEmpty()) {
            showAlert("Validation Error", "Please select a status.");
            return;
        }

        if (priority == null || priority.isEmpty()) {
            showAlert("Validation Error", "Please select a priority.");
            return;
        }

        try {
            if (currentTask == null) {
                // Create new task
                currentTask = new HousekeepingTask(
                    roomNo, assignedStaff, taskType, status, priority, LocalDateTime.now()
                );
            } else {
                // Update existing task
                currentTask.setRoomNo(roomNo);
                currentTask.setAssignedStaff(assignedStaff);
                currentTask.setTaskType(taskType);
                currentTask.setStatus(status);
                currentTask.setPriority(priority);
                
                // Set completed time if status is completed
                if ("Completed".equals(status) && currentTask.getCompletedAt() == null) {
                    currentTask.setCompletedAt(LocalDateTime.now());
                }
            }

            currentTask.setNotes(notes);

            // Save to database
            housekeepingService.saveTask(currentTask);

            // Callback to refresh parent view
            if (taskUpdateCallback != null) {
                taskUpdateCallback.accept(currentTask);
            }

            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save task: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
