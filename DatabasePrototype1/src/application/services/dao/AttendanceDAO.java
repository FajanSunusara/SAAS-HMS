package application.services.dao;

import application.models.Attendance;
import application.services.dao.DatabaseManager;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public void addAttendance(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (staff_id, attendance_date, status, check_in, check_out) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, attendance.getStaffId());
            pstmt.setDate(2, attendance.getAttendanceDate());
            pstmt.setString(3, attendance.getStatus());
            
            // Convert Timestamp to Time for check_in and check_out
            if (attendance.getCheckInTime() != null) {
                pstmt.setTime(4, new Time(attendance.getCheckInTime().getTime()));
            } else {
                pstmt.setTime(4, null);
            }
            
            if (attendance.getCheckOutTime() != null) {
                pstmt.setTime(5, new Time(attendance.getCheckOutTime().getTime()));
            } else {
                pstmt.setTime(5, null);
            }

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    attendance.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public Attendance getAttendanceById(Long id) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }
        }
        return null;
    }

    public List<Attendance> getAllAttendance() throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance ORDER BY attendance_date DESC, staff_id";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
        }
        return attendanceList;
    }

    public void updateAttendance(Attendance attendance) throws SQLException {
        String sql = "UPDATE attendance SET staff_id = ?, attendance_date = ?, status = ?, check_in = ?, check_out = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, attendance.getStaffId());
            pstmt.setDate(2, attendance.getAttendanceDate());
            pstmt.setString(3, attendance.getStatus());
            
            if (attendance.getCheckInTime() != null) {
                pstmt.setTime(4, new Time(attendance.getCheckInTime().getTime()));
            } else {
                pstmt.setTime(4, null);
            }
            
            if (attendance.getCheckOutTime() != null) {
                pstmt.setTime(5, new Time(attendance.getCheckOutTime().getTime()));
            } else {
                pstmt.setTime(5, null);
            }
            
            pstmt.setLong(6, attendance.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteAttendance(Long id) throws SQLException {
        String sql = "DELETE FROM attendance WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    public boolean isStaffPresentOnDate(Long staffId, LocalDate date) throws SQLException {
        String sql = "SELECT status FROM attendance WHERE staff_id = ? AND attendance_date = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, staffId);
            pstmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "Present".equals(rs.getString("status"));
                }
            }
        }
        return false;
    }

    public void updateOrCreateAttendance(Long staffId, LocalDate date, String status) throws SQLException {
        // First, check if attendance record exists
        String checkSql = "SELECT id FROM attendance WHERE staff_id = ? AND attendance_date = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setLong(1, staffId);
            checkStmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Update existing record
                    String updateSql = "UPDATE attendance SET status = ?, check_in = ? WHERE staff_id = ? AND attendance_date = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, status);
                        updateStmt.setTime(2, "Present".equals(status) ? 
                            new Time(System.currentTimeMillis()) : null);
                        updateStmt.setLong(3, staffId);
                        updateStmt.setDate(4, Date.valueOf(date));
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Create new record
                    String insertSql = "INSERT INTO attendance (staff_id, attendance_date, status, check_in) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setLong(1, staffId);
                        insertStmt.setDate(2, Date.valueOf(date));
                        insertStmt.setString(3, status);
                        insertStmt.setTime(4, "Present".equals(status) ? 
                            new Time(System.currentTimeMillis()) : null);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getLong("id"));
        attendance.setStaffId(rs.getLong("staff_id"));
        attendance.setAttendanceDate(rs.getDate("attendance_date"));
        attendance.setStatus(rs.getString("status"));
        
        // Convert Time to Timestamp for check_in and check_out
        Time checkIn = rs.getTime("check_in");
        if (checkIn != null) {
            attendance.setCheckInTime(new Timestamp(checkIn.getTime()));
        }
        
        Time checkOut = rs.getTime("check_out");
        if (checkOut != null) {
            attendance.setCheckOutTime(new Timestamp(checkOut.getTime()));
        }
        
        return attendance;
    }
}
