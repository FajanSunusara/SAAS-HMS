package application.services.dao.inventory;




import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import application.models.inventory.RoomAssignment;
import application.services.dao.DatabaseManager;

public class RoomAssignmentDAO {
    
    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public List<RoomAssignment> getAllAssignments() throws SQLException {
        List<RoomAssignment> assignments = new ArrayList<>();
        String sql = """
            SELECT ra.*, ii.item_name 
            FROM room_assignments ra 
            LEFT JOIN inventory_items ii ON ra.item_id = ii.item_id 
            ORDER BY ra.assigned_date DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                assignments.add(mapResultSetToAssignment(rs));
            }
        }
        return assignments;
    }

    public List<RoomAssignment> getActiveAssignments() throws SQLException {
        List<RoomAssignment> assignments = new ArrayList<>();
        String sql = """
            SELECT ra.*, ii.item_name 
            FROM room_assignments ra 
            LEFT JOIN inventory_items ii ON ra.item_id = ii.item_id 
            WHERE ra.status IN ('Assigned', 'Overdue') 
            ORDER BY ra.assigned_date DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                assignments.add(mapResultSetToAssignment(rs));
            }
        }
        return assignments;
    }

    public List<RoomAssignment> getAssignmentsByRoom(String roomNo) throws SQLException {
        List<RoomAssignment> assignments = new ArrayList<>();
        String sql = """
            SELECT ra.*, ii.item_name 
            FROM room_assignments ra 
            LEFT JOIN inventory_items ii ON ra.item_id = ii.item_id 
            WHERE ra.room_no = ? 
            ORDER BY ra.assigned_date DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, roomNo);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                assignments.add(mapResultSetToAssignment(rs));
            }
        }
        return assignments;
    }

    public List<RoomAssignment> getAssignmentsByItem(int itemId) throws SQLException {
        List<RoomAssignment> assignments = new ArrayList<>();
        String sql = """
            SELECT ra.*, ii.item_name 
            FROM room_assignments ra 
            LEFT JOIN inventory_items ii ON ra.item_id = ii.item_id 
            WHERE ra.item_id = ? 
            ORDER BY ra.assigned_date DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                assignments.add(mapResultSetToAssignment(rs));
            }
        }
        return assignments;
    }

    public List<RoomAssignment> getOverdueAssignments() throws SQLException {
        List<RoomAssignment> assignments = new ArrayList<>();
        String sql = """
            SELECT ra.*, ii.item_name 
            FROM room_assignments ra 
            LEFT JOIN inventory_items ii ON ra.item_id = ii.item_id 
            WHERE ra.expected_return_date < CURRENT_TIMESTAMP 
              AND ra.return_date IS NULL 
              AND ra.status = 'Assigned'
            ORDER BY ra.expected_return_date ASC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                assignments.add(mapResultSetToAssignment(rs));
            }
        }
        return assignments;
    }

    public int saveAssignment(RoomAssignment assignment) throws SQLException {
        if (assignment.getAssignmentId() == 0) {
            return insertAssignment(assignment);
        } else {
            updateAssignment(assignment);
            return assignment.getAssignmentId();
        }
    }

    private int insertAssignment(RoomAssignment assignment) throws SQLException {
        String sql = """
            INSERT INTO room_assignments (room_no, item_id, quantity, assigned_date, 
                                        return_date, expected_return_date, status, assigned_by, 
                                        returned_by, condition_on_assignment, condition_on_return, 
                                        notes, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, assignment.getRoomNo());
            stmt.setInt(2, assignment.getItemId());
            stmt.setInt(3, assignment.getQuantity());
            stmt.setTimestamp(4, Timestamp.valueOf(assignment.getAssignedDate()));
            stmt.setTimestamp(5, assignment.getReturnDate() != null ? Timestamp.valueOf(assignment.getReturnDate()) : null);
            stmt.setTimestamp(6, assignment.getExpectedReturnDate() != null ? Timestamp.valueOf(assignment.getExpectedReturnDate()) : null);
            stmt.setString(7, assignment.getStatus());
            stmt.setString(8, assignment.getAssignedBy());
            stmt.setString(9, assignment.getReturnedBy());
            stmt.setString(10, assignment.getConditionOnAssignment());
            stmt.setString(11, assignment.getConditionOnReturn());
            stmt.setString(12, assignment.getNotes());
            stmt.setTimestamp(13, Timestamp.valueOf(assignment.getCreatedAt()));
            stmt.setTimestamp(14, Timestamp.valueOf(assignment.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating assignment failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int assignmentId = generatedKeys.getInt(1);
                    assignment.setAssignmentId(assignmentId);
                    return assignmentId;
                } else {
                    throw new SQLException("Creating assignment failed, no ID obtained.");
                }
            }
        }
    }

    private void updateAssignment(RoomAssignment assignment) throws SQLException {
        String sql = """
            UPDATE room_assignments 
            SET room_no = ?, item_id = ?, quantity = ?, assigned_date = ?, 
                return_date = ?, expected_return_date = ?, status = ?, assigned_by = ?, 
                returned_by = ?, condition_on_assignment = ?, condition_on_return = ?, 
                notes = ?, updated_at = ?
            WHERE assignment_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, assignment.getRoomNo());
            stmt.setInt(2, assignment.getItemId());
            stmt.setInt(3, assignment.getQuantity());
            stmt.setTimestamp(4, Timestamp.valueOf(assignment.getAssignedDate()));
            stmt.setTimestamp(5, assignment.getReturnDate() != null ? Timestamp.valueOf(assignment.getReturnDate()) : null);
            stmt.setTimestamp(6, assignment.getExpectedReturnDate() != null ? Timestamp.valueOf(assignment.getExpectedReturnDate()) : null);
            stmt.setString(7, assignment.getStatus());
            stmt.setString(8, assignment.getAssignedBy());
            stmt.setString(9, assignment.getReturnedBy());
            stmt.setString(10, assignment.getConditionOnAssignment());
            stmt.setString(11, assignment.getConditionOnReturn());
            stmt.setString(12, assignment.getNotes());
            stmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(14, assignment.getAssignmentId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating assignment failed, no rows affected.");
            }
        }
    }

    public void returnAssignment(int assignmentId, String returnedBy, String condition, String notes) throws SQLException {
        String sql = """
            UPDATE room_assignments 
            SET return_date = ?, returned_by = ?, condition_on_return = ?, 
                notes = CASE 
                    WHEN notes IS NULL OR notes = '' THEN ? 
                    ELSE CONCAT(notes, '; ', ?) 
                END, 
                status = 'Returned', updated_at = ?
            WHERE assignment_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, returnedBy);
            stmt.setString(3, condition);
            stmt.setString(4, notes);
            stmt.setString(5, notes);
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(7, assignmentId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Returning assignment failed, no rows affected.");
            }
        }
    }

    public void markAsLostOrDamaged(int assignmentId, String status, String notes) throws SQLException {
        String sql = """
            UPDATE room_assignments 
            SET status = ?, notes = CASE 
                    WHEN notes IS NULL OR notes = '' THEN ? 
                    ELSE CONCAT(notes, '; ', ?) 
                END, updated_at = ?
            WHERE assignment_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setString(2, notes);
            stmt.setString(3, notes);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, assignmentId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating assignment status failed, no rows affected.");
            }
        }
    }

    public void deleteAssignment(int assignmentId) throws SQLException {
        String sql = "DELETE FROM room_assignments WHERE assignment_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, assignmentId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting assignment failed, no rows affected.");
            }
        }
    }

    public RoomAssignment getAssignmentById(int assignmentId) throws SQLException {
        String sql = """
            SELECT ra.*, ii.item_name 
            FROM room_assignments ra 
            LEFT JOIN inventory_items ii ON ra.item_id = ii.item_id 
            WHERE ra.assignment_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, assignmentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAssignment(rs);
            }
        }
        return null;
    }

    public int getTotalAssignedQuantity(int itemId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(quantity), 0) as total 
            FROM room_assignments 
            WHERE item_id = ? AND status IN ('Assigned', 'Overdue')
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    private RoomAssignment mapResultSetToAssignment(ResultSet rs) throws SQLException {
        RoomAssignment assignment = new RoomAssignment();
        
        assignment.setAssignmentId(rs.getInt("assignment_id"));
        assignment.setRoomNo(rs.getString("room_no"));
        assignment.setItemId(rs.getInt("item_id"));
        assignment.setItemName(rs.getString("item_name"));
        assignment.setQuantity(rs.getInt("quantity"));
        
        Timestamp assignedDate = rs.getTimestamp("assigned_date");
        if (assignedDate != null) {
            assignment.setAssignedDate(assignedDate.toLocalDateTime());
        }
        
        Timestamp returnDate = rs.getTimestamp("return_date");
        if (returnDate != null) {
            assignment.setReturnDate(returnDate.toLocalDateTime());
        }
        
        Timestamp expectedReturnDate = rs.getTimestamp("expected_return_date");
        if (expectedReturnDate != null) {
            assignment.setExpectedReturnDate(expectedReturnDate.toLocalDateTime());
        }
        
        assignment.setStatus(rs.getString("status"));
        assignment.setAssignedBy(rs.getString("assigned_by"));
        assignment.setReturnedBy(rs.getString("returned_by"));
        assignment.setConditionOnAssignment(rs.getString("condition_on_assignment"));
        assignment.setConditionOnReturn(rs.getString("condition_on_return"));
        assignment.setNotes(rs.getString("notes"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            assignment.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            assignment.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return assignment;
    }
}
