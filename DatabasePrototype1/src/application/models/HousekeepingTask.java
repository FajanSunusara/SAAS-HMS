package application.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Unified HousekeepingTask class that serves both UI and database operations
 * Eliminates the need for separate Housekeeping entity
 */
public class HousekeepingTask {

    // Core fields
    private Long id;
    private String roomNo;
    private String assignedStaff;        // For UI display
    private Long staffId;                // For database operations
    private String taskType;
    private String status;
    private String priority;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private String notes;

    // Constructors
    public HousekeepingTask() {
        this.notes = "";
    }

    public HousekeepingTask(String roomNo, String assignedStaff, String taskType,
                           String status, String priority, LocalDateTime assignedAt) {
        this.roomNo = roomNo;
        this.assignedStaff = assignedStaff;
        this.taskType = taskType;
        this.status = status;
        this.priority = priority;
        this.assignedAt = assignedAt;
        this.notes = "";
    }

    public HousekeepingTask(String roomNo, String assignedStaff, String taskType,
                           String status, String priority, LocalDateTime assignedAt,
                           LocalDateTime completedAt) {
        this.roomNo = roomNo;
        this.assignedStaff = assignedStaff;
        this.taskType = taskType;
        this.status = status;
        this.priority = priority;
        this.assignedAt = assignedAt;
        this.completedAt = completedAt;
        this.notes = "";
    }

    // All getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public String getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(String assignedStaff) { this.assignedStaff = assignedStaff; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes != null ? notes : ""; }

    // Database conversion methods
    public Timestamp getAssignedAtTimestamp() {
        return assignedAt != null ? Timestamp.valueOf(assignedAt) : null;
    }

    public void setAssignedAtTimestamp(Timestamp timestamp) {
        this.assignedAt = timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    public Timestamp getCompletedAtTimestamp() {
        return completedAt != null ? Timestamp.valueOf(completedAt) : null;
    }

    public void setCompletedAtTimestamp(Timestamp timestamp) {
        this.completedAt = timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HousekeepingTask that = (HousekeepingTask) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
