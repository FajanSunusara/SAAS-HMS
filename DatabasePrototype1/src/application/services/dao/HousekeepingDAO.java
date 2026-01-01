package application.services.dao;

import application.models.HousekeepingTask;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HousekeepingDAO {

    public void addHousekeepingTask(HousekeepingTask task) throws SQLException {
        String sql = "INSERT INTO housekeeping (room_no, assigned_staff_id, task_type, status, assigned_at, completed_at, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, task.getRoomNo());
            pstmt.setObject(2, task.getStaffId(), Types.BIGINT);
            pstmt.setString(3, task.getTaskType());
            pstmt.setString(4, task.getStatus());
            pstmt.setTimestamp(5, task.getAssignedAtTimestamp());
            pstmt.setTimestamp(6, task.getCompletedAtTimestamp());
            pstmt.setString(7, task.getNotes());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public HousekeepingTask getHousekeepingTaskById(Long id) throws SQLException {
        String sql = "SELECT * FROM housekeeping WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToHousekeepingTask(rs);
                }
            }
        }
        return null;
    }

    public List<HousekeepingTask> getAllHousekeepingTasks() throws SQLException {
        List<HousekeepingTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM housekeeping ORDER BY assigned_at DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tasks.add(mapResultSetToHousekeepingTask(rs));
            }
        }
        return tasks;
    }

    public List<HousekeepingTask> getTasksByStaffId(Long staffId) throws SQLException {
        List<HousekeepingTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM housekeeping WHERE assigned_staff_id = ? ORDER BY assigned_at DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, staffId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToHousekeepingTask(rs));
                }
            }
        }
        return tasks;
    }

    public void updateHousekeepingTask(HousekeepingTask task) throws SQLException {
        String sql = "UPDATE housekeeping SET room_no = ?, assigned_staff_id = ?, task_type = ?, status = ?, assigned_at = ?, completed_at = ?, notes = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, task.getRoomNo());
            pstmt.setObject(2, task.getStaffId(), Types.BIGINT);
            pstmt.setString(3, task.getTaskType());
            pstmt.setString(4, task.getStatus());
            pstmt.setTimestamp(5, task.getAssignedAtTimestamp());
            pstmt.setTimestamp(6, task.getCompletedAtTimestamp());
            pstmt.setString(7, task.getNotes());
            pstmt.setLong(8, task.getId());
            
            pstmt.executeUpdate();
        }
    }

    public void deleteHousekeepingTask(Long id) throws SQLException {
        String sql = "DELETE FROM housekeeping WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    private HousekeepingTask mapResultSetToHousekeepingTask(ResultSet rs) throws SQLException {
        HousekeepingTask task = new HousekeepingTask();
        task.setId(rs.getLong("id"));
        task.setRoomNo(rs.getString("room_no"));
        task.setStaffId(rs.getObject("assigned_staff_id", Long.class));
        task.setTaskType(rs.getString("task_type"));
        task.setStatus(rs.getString("status"));
        task.setAssignedAtTimestamp(rs.getTimestamp("assigned_at"));
        task.setCompletedAtTimestamp(rs.getTimestamp("completed_at"));
        task.setNotes(rs.getString("notes"));
        task.setPriority("Medium"); // Default priority since not stored in DB
        return task;
    }
}
