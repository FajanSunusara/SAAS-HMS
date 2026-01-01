package application.models;

import java.sql.Timestamp;
import java.util.Objects;

public class Housekeeping {
    private Long id;
    private String roomNo;  // Changed from roomId to match schema
    private Long staffId;   // Renamed for clarity (assigned_staff_id in DB)
    private String taskType;
    private String status;
    private Timestamp assignedAt;    // Renamed from requestTime
    private Timestamp completedAt;   // Renamed from completionTime
    private String notes;            // Renamed from comments

    // Constructors
    public Housekeeping() {}

    public Housekeeping(String roomNo, Long staffId, String taskType, String status, 
                       Timestamp assignedAt, Timestamp completedAt, String notes) {
        this.roomNo = roomNo;
        this.staffId = staffId;
        this.taskType = taskType;
        this.status = status;
        this.assignedAt = assignedAt;
        this.completedAt = completedAt;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Timestamp assignedAt) { this.assignedAt = assignedAt; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Backward compatibility methods
    public Long getRoomId() { return null; } // Deprecated
    public void setRoomId(Long roomId) { /* Deprecated */ }

    public String getComments() { return notes; }
    public void setComments(String comments) { this.notes = comments; }

    public Timestamp getRequestTime() { return assignedAt; }
    public void setRequestTime(Timestamp requestTime) { this.assignedAt = requestTime; }

    public Timestamp getCompletionTime() { return completedAt; }
    public void setCompletionTime(Timestamp completionTime) { this.completedAt = completionTime; }

    @Override
    public String toString() {
        return "Housekeeping{" +
                "id=" + id +
                ", roomNo='" + roomNo + '\'' +
                ", staffId=" + staffId +
                ", taskType='" + taskType + '\'' +
                ", status='" + status + '\'' +
                ", assignedAt=" + assignedAt +
                ", completedAt=" + completedAt +
                ", notes='" + notes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Housekeeping that = (Housekeeping) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
