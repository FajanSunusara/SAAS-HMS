package application.services.dao;

import application.models.Staff;
import application.services.dao.DatabaseManager;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class StaffDAO {
    
    public void addStaff(Staff staff) throws SQLException {
        String sql = "INSERT INTO staff (first_name, last_name, role, department, floor, join_date, is_active, email, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            String[] nameParts = splitName(staff.getName());
            pstmt.setString(1, nameParts[0]);
            pstmt.setString(2, nameParts[1]);
            pstmt.setString(3, staff.getPosition());
            pstmt.setString(4, staff.getDepartment());
            pstmt.setObject(5, staff.getFloor(), Types.INTEGER);
            pstmt.setDate(6, staff.getHireDate());
            pstmt.setBoolean(7, staff.getIsActive());
            pstmt.setString(8, staff.getEmail());
            pstmt.setString(9, staff.getPhone());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    staff.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public Staff getStaffById(Long id) throws SQLException {
        String sql = "SELECT * FROM staff WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStaff(rs);
                }
            }
        }
        return null;
    }

    // OPTIMIZED: Single query with batch attendance calculation
    public List<Staff> getAllStaff() throws SQLException {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT * FROM staff ORDER BY first_name, last_name";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                staffList.add(mapResultSetToStaff(rs));
            }
        }
        
        // FAST: Calculate all attendance stats in one batch query
        if (!staffList.isEmpty()) {
            calculateBatchAttendanceStats(staffList);
        }
        
        return staffList;
    }

    public void updateStaff(Staff staff) throws SQLException {
        String sql = "UPDATE staff SET first_name = ?, last_name = ?, role = ?, department = ?, floor = ?, join_date = ?, is_active = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String[] nameParts = splitName(staff.getName());
            pstmt.setString(1, nameParts[0]);
            pstmt.setString(2, nameParts[1]);
            pstmt.setString(3, staff.getPosition());
            pstmt.setString(4, staff.getDepartment());
            pstmt.setObject(5, staff.getFloor(), Types.INTEGER);
            pstmt.setDate(6, staff.getHireDate());
            pstmt.setBoolean(7, staff.getIsActive());
            pstmt.setString(8, staff.getEmail());
            pstmt.setString(9, staff.getPhone());
            pstmt.setLong(10, staff.getId());
            
            pstmt.executeUpdate();
        }
    }

    public void deleteStaff(Long id) throws SQLException {
        String sql = "DELETE FROM staff WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<String> getDistinctDepartments() throws SQLException {
        List<String> departments = new ArrayList<>();
        String sql = "SELECT DISTINCT department FROM staff WHERE department IS NOT NULL ORDER BY department";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                departments.add(rs.getString("department"));
            }
        }
        return departments;
    }

    public List<String> getDistinctRoles() throws SQLException {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT DISTINCT role FROM staff WHERE role IS NOT NULL ORDER BY role";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                roles.add(rs.getString("role"));
            }
        }
        return roles;
    }

    public List<Staff> searchStaff(String searchTerm, String department, String role) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM staff WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (LOWER(CONCAT(first_name, ' ', last_name)) LIKE ? OR CAST(id AS VARCHAR) LIKE ?)");
            String likeTerm = "%" + searchTerm.toLowerCase() + "%";
            params.add(likeTerm);
            params.add(likeTerm);
        }

        if (department != null && !department.trim().isEmpty() && !"All".equals(department)) {
            sql.append(" AND department = ?");
            params.add(department);
        }

        if (role != null && !role.trim().isEmpty() && !"All".equals(role)) {
            sql.append(" AND role = ?");
            params.add(role);
        }

        sql.append(" ORDER BY first_name, last_name");

        List<Staff> staffList = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    staffList.add(mapResultSetToStaff(rs));
                }
            }
        }
        
        return staffList;
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
        String upsertSql = "MERGE INTO attendance (staff_id, attendance_date, status, check_in) " +
                          "KEY (staff_id, attendance_date) " +
                          "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(upsertSql)) {
            
            pstmt.setLong(1, staffId);
            pstmt.setDate(2, Date.valueOf(date));
            pstmt.setString(3, status);
            pstmt.setTime(4, "Present".equals(status) ? 
                new Time(System.currentTimeMillis()) : null);
            
            pstmt.executeUpdate();
        }
    }

    private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setId(rs.getLong("id"));
        
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String fullName = (firstName != null ? firstName : "") + 
                         (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
        staff.setName(fullName.trim());
        
        staff.setPosition(rs.getString("role"));
        staff.setDepartment(rs.getString("department"));
        staff.setPhone(rs.getString("phone"));
        staff.setEmail(rs.getString("email"));
        staff.setHireDate(rs.getDate("join_date"));
        staff.setIsActive(rs.getBoolean("is_active"));
        staff.setFloor(rs.getObject("floor", Integer.class));
        
        return staff;
    }

    // OPTIMIZED: Single batch query for all attendance stats
    private void calculateBatchAttendanceStats(List<Staff> staffList) throws SQLException {
        if (staffList.isEmpty()) return;
        
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < staffList.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }
        
        String sql = "SELECT staff_id, " +
                    "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as present_days, " +
                    "SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END) as absent_days " +
                    "FROM attendance WHERE staff_id IN (" + inClause + ") " +
                    "GROUP BY staff_id";
        
        Map<Long, int[]> attendanceStats = new HashMap<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < staffList.size(); i++) {
                pstmt.setLong(i + 1, staffList.get(i).getId());
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long staffId = rs.getLong("staff_id");
                    int presentDays = rs.getInt("present_days");
                    int absentDays = rs.getInt("absent_days");
                    attendanceStats.put(staffId, new int[]{presentDays, absentDays});
                }
            }
        }
        
        for (Staff staff : staffList) {
            int[] stats = attendanceStats.get(staff.getId());
            if (stats != null) {
                staff.setDaysPresent(stats[0]);
                staff.setDaysAbsent(stats[1]);
            }
            
            if (staff.getHireDate() != null) {
                LocalDate joinDate = staff.getHireDate().toLocalDate();
                LocalDate today = LocalDate.now();
                long totalDays = ChronoUnit.DAYS.between(joinDate, today);
                long workingDays = totalDays - (totalDays / 7 * 2);
                staff.setDaysActive((int) Math.max(0, workingDays));
            }
        }
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }
}
