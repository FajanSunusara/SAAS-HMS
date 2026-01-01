package application.models;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

public class Attendance {

    private Long id;
    private Long staffId;
    private Date attendanceDate;
    private String status; // e.g., 'Present', 'Absent', 'Leave'
    private Timestamp checkInTime;
    private Timestamp checkOutTime;
    private Timestamp createdAt;

    // Constructors
    public Attendance() {}

    public Attendance(Long staffId, Date attendanceDate, String status, Timestamp checkInTime, Timestamp checkOutTime) {
        this.staffId = staffId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public Date getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(Date attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Timestamp checkInTime) { this.checkInTime = checkInTime; }

    public Timestamp getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Timestamp checkOutTime) { this.checkOutTime = checkOutTime; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", staffId=" + staffId +
                ", attendanceDate=" + attendanceDate +
                ", status='" + status + '\'' +
                ", checkInTime=" + checkInTime +
                ", checkOutTime=" + checkOutTime +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attendance that = (Attendance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}