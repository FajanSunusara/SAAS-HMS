package application.controllers;

import application.models.Staff;
import application.services.dao.StaffDAO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StaffManagementController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> roleFilter;
    @FXML private DatePicker attendanceDatePicker;

    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, Long> idColumn;
    @FXML private TableColumn<Staff, String> nameColumn;
    @FXML private TableColumn<Staff, String> departmentColumn;
    @FXML private TableColumn<Staff, String> roleColumn;
    @FXML private TableColumn<Staff, Date> joinDateColumn;
    @FXML private TableColumn<Staff, Integer> daysActiveColumn;
    @FXML private TableColumn<Staff, Integer> daysPresentColumn;
    @FXML private TableColumn<Staff, Integer> daysAbsentColumn;
    @FXML private TableColumn<Staff, String> statusColumn;
    @FXML private TableColumn<Staff, Boolean> attendanceStatusColumn;

    @FXML private TextField nameField;
    @FXML private ComboBox<String> departmentField;
    @FXML private ComboBox<String> roleField;
    @FXML private TextField floorField;
    @FXML private DatePicker joinDateField;
    @FXML private CheckBox staffActiveFormToggle;

    @FXML private Label dayJoinedLabel;
    @FXML private Label workingDaysLabel;
    @FXML private Label daysActiveLabel;
    @FXML private Label daysPresentLabel;
    @FXML private Label daysAbsentLabel;
    @FXML private Label totalWorkingDaysLabel;

    private final ObservableList<Staff> staffData = FXCollections.observableArrayList();
    private final StaffDAO staffDAO = new StaffDAO();
    private Staff selectedStaff = null;
    private boolean isEditMode = false;

    // FAST: Debounced search scheduler
    private final ScheduledExecutorService searchScheduler = Executors.newScheduledThreadPool(1);
    private volatile Task<?> searchTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // FAST: Simple PropertyValueFactory columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        joinDateColumn.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        daysActiveColumn.setCellValueFactory(new PropertyValueFactory<>("daysActive"));
        daysPresentColumn.setCellValueFactory(new PropertyValueFactory<>("daysPresent"));
        daysAbsentColumn.setCellValueFactory(new PropertyValueFactory<>("daysAbsent"));

        // FAST: Simple status column
        statusColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getStatus()));

        // FAST: Attendance checkbox column
        attendanceStatusColumn.setCellValueFactory(cellData -> {
            Staff staff = cellData.getValue();
            LocalDate selectedDate = attendanceDatePicker.getValue();
            
            BooleanProperty presentProperty = new SimpleBooleanProperty(false);
            
            // Async check attendance to avoid blocking
            Task<Boolean> checkTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return staffDAO.isStaffPresentOnDate(staff.getId(), selectedDate);
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> presentProperty.set(getValue()));
                }
            };
            
            Thread checkThread = new Thread(checkTask);
            checkThread.setDaemon(true);
            checkThread.start();
            
            presentProperty.addListener((obs, oldVal, newVal) -> {
                handleAttendanceChangeAsync(staff, selectedDate, newVal);
            });
            
            return presentProperty;
        });

        attendanceStatusColumn.setCellFactory(CheckBoxTableCell.forTableColumn(attendanceStatusColumn));
        attendanceStatusColumn.setEditable(true);

        staffTable.setEditable(true);
        staffTable.setItems(staffData);

        // ASYNC: Load all data in background
        loadStaffDataAsync();
        initializeComboBoxesAsync();

        attendanceDatePicker.setValue(LocalDate.now());
        attendanceDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                staffTable.refresh();
            }
        });

        // DEBOUNCED: Search with 500ms delay
        searchField.textProperty().addListener((obs, oldVal, newVal) -> debouncedSearch());
        departmentFilter.valueProperty().addListener((obs, oldVal, newVal) -> debouncedSearch());
        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> debouncedSearch());

        staffTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedStaff = newSelection;
            if (newSelection != null) {
                updateStaffDetailsForm(newSelection);
                updateWorkingDaysTracker(newSelection);
            } else {
                clearStaffDetailsForm();
                clearWorkingDaysTracker();
            }
        });
    }

    // ASYNC: Load staff data in background thread
    private void loadStaffDataAsync() {
        Task<List<Staff>> task = new Task<List<Staff>>() {
            @Override
            protected List<Staff> call() throws Exception {
                return staffDAO.getAllStaff();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> staffData.setAll(getValue()));
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("Error", "Failed to load staff data: " + getException().getMessage()));
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // ASYNC: Load dropdown data in background
    private void initializeComboBoxesAsync() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<String> departments = staffDAO.getDistinctDepartments();
                List<String> roles = staffDAO.getDistinctRoles();

                Platform.runLater(() -> {
                    departments.add(0, "All");
                    roles.add(0, "All");

                    departmentFilter.setItems(FXCollections.observableArrayList(departments));
                    departmentFilter.setValue("All");

                    roleFilter.setItems(FXCollections.observableArrayList(roles));
                    roleFilter.setValue("All");

                    departmentField.setItems(FXCollections.observableArrayList(departments.subList(1, departments.size())));
                    roleField.setItems(FXCollections.observableArrayList(roles.subList(1, roles.size())));
                });
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // DEBOUNCED: Prevent excessive searches while typing
    private void debouncedSearch() {
        if (searchTask != null) {
            searchTask.cancel();
        }

        searchTask = new Task<List<Staff>>() {
            @Override
            protected List<Staff> call() throws Exception {
                String searchTerm = searchField.getText();
                String department = departmentFilter.getValue();
                String role = roleFilter.getValue();
                return staffDAO.searchStaff(searchTerm, department, role);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> staffData.setAll(getValue()));
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("Search Error", "Failed to search: " + getException().getMessage()));
            }
        };

        searchScheduler.schedule(() -> {
            Thread thread = new Thread(searchTask);
            thread.setDaemon(true);
            thread.start();
        }, 500, TimeUnit.MILLISECONDS); // 500ms delay
    }

    // ASYNC: Handle attendance changes in background
    private void handleAttendanceChangeAsync(Staff staff, LocalDate date, boolean isPresent) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String status = isPresent ? "Present" : "Absent";
                staffDAO.updateOrCreateAttendance(staff.getId(), date, status);
                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("Database Error", "Failed to update attendance: " + getException().getMessage()));
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleSearch() {
        debouncedSearch();
    }

    @FXML
    private void handleAddStaff() {
        isEditMode = false;
        selectedStaff = null;
        clearStaffDetailsForm();
        staffTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleEditStaff() {
        Staff selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a staff member to edit.");
            return;
        }
        isEditMode = true;
        selectedStaff = selected;
        updateStaffDetailsForm(selected);
    }

    @FXML
    private void handleSaveStaff() {
        // ASYNC: Save staff in background
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String name = nameField.getText();
                if (name == null || name.trim().isEmpty()) {
                    Platform.runLater(() -> showAlert("Validation Error", "Name is required."));
                    return null;
                }

                Staff staff = isEditMode && selectedStaff != null ? selectedStaff : new Staff();
                staff.setName(name.trim());
                staff.setDepartment(departmentField.getValue());
                staff.setPosition(roleField.getValue());

                String floorText = floorField.getText();
                if (floorText != null && !floorText.trim().isEmpty()) {
                    try {
                        staff.setFloor(Integer.parseInt(floorText.trim()));
                    } catch (NumberFormatException e) {
                        Platform.runLater(() -> showAlert("Validation Error", "Floor must be a valid number."));
                        return null;
                    }
                }

                String email = name.toLowerCase().replace(" ", ".") + "@hotel.com";
                staff.setEmail(email);
                staff.setPhone(staff.getPhone() != null ? staff.getPhone() : "");

                if (joinDateField.getValue() != null) {
                    staff.setHireDate(Date.valueOf(joinDateField.getValue()));
                }
                staff.setIsActive(staffActiveFormToggle.isSelected());

                if (isEditMode) {
                    staffDAO.updateStaff(staff);
                } else {
                    staffDAO.addStaff(staff);
                }

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    showAlert("Success", isEditMode ? "Staff updated successfully." : "Staff added successfully.");
                    loadStaffDataAsync();
                    clearStaffDetailsForm();
                    isEditMode = false;
                    selectedStaff = null;
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("Database Error", "Failed to save staff: " + getException().getMessage()));
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCancelStaff() {
        clearStaffDetailsForm();
        staffTable.getSelectionModel().clearSelection();
        isEditMode = false;
        selectedStaff = null;
    }

    private void updateStaffDetailsForm(Staff staff) {
        nameField.setText(staff.getName());
        departmentField.setValue(staff.getDepartment());
        roleField.setValue(staff.getPosition());
        floorField.setText(staff.getFloor() != null ? staff.getFloor().toString() : "");
        joinDateField.setValue(staff.getHireDate() != null ? staff.getHireDate().toLocalDate() : null);
        staffActiveFormToggle.setSelected(staff.isActive());
    }

    private void clearStaffDetailsForm() {
        nameField.clear();
        departmentField.setValue(null);
        roleField.setValue(null);
        floorField.clear();
        joinDateField.setValue(null);
        staffActiveFormToggle.setSelected(true);
    }

    private void updateWorkingDaysTracker(Staff staff) {
        if (staff.getHireDate() == null) {
            clearWorkingDaysTracker();
            return;
        }

        LocalDate joinDate = staff.getHireDate().toLocalDate();
        long totalDays = ChronoUnit.DAYS.between(joinDate, LocalDate.now());
        long workingDays = totalDays - (totalDays / 7 * 2);

        dayJoinedLabel.setText(joinDate.toString());
        workingDaysLabel.setText(String.valueOf(workingDays));
        daysActiveLabel.setText(String.valueOf(staff.getDaysActive()));
        daysPresentLabel.setText(String.valueOf(staff.getDaysPresent()));
        daysAbsentLabel.setText(String.valueOf(staff.getDaysAbsent()));
        totalWorkingDaysLabel.setText(String.valueOf(totalDays));
    }

    private void clearWorkingDaysTracker() {
        dayJoinedLabel.setText("");
        workingDaysLabel.setText("");
        daysActiveLabel.setText("");
        daysPresentLabel.setText("");
        daysAbsentLabel.setText("");
        totalWorkingDaysLabel.setText("");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
