package application.services.dao;

import application.models.Room;
import application.services.dao.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public void addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_no, room_type, floor, beds, ac, price, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, room.getRoomNo());
            pstmt.setString(2, room.getRoomType());
            pstmt.setInt(3, room.getFloor());
            pstmt.setInt(4, room.getBeds());
            pstmt.setBoolean(5, room.isAc());
            pstmt.setBigDecimal(6, room.getPrice());
            pstmt.setString(7, room.getStatus());
            pstmt.setString(8, room.getDescription());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    room.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public Room getRoomById(Long id) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoom(rs);
                }
            }
        }
        return null;
    }

    public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_no";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        }
        return rooms;
    }

    public void updateRoom(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_no = ?, room_type = ?, floor = ?, beds = ?, ac = ?, price = ?, status = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomNo());
            pstmt.setString(2, room.getRoomType());
            pstmt.setInt(3, room.getFloor());
            pstmt.setInt(4, room.getBeds());
            pstmt.setBoolean(5, room.isAc());
            pstmt.setBigDecimal(6, room.getPrice());
            pstmt.setString(7, room.getStatus());
            pstmt.setString(8, room.getDescription());
            pstmt.setLong(9, room.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteRoom(Long id) throws SQLException {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setRoomNo(rs.getString("room_no"));
        room.setRoomType(rs.getString("room_type"));
        room.setFloor(rs.getInt("floor"));
        room.setBeds(rs.getInt("beds"));
        room.setAc(rs.getBoolean("ac"));
        room.setPrice(rs.getBigDecimal("price"));
        room.setStatus(rs.getString("status"));
        room.setDescription(rs.getString("description"));
        return room;
    }

    // === New helpers for Reservation page filters ===

    public List<String> findAllRoomNos() throws SQLException {
        String sql = "SELECT room_no FROM rooms ORDER BY room_no";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> rooms = new ArrayList<>();
            while (rs.next()) rooms.add(rs.getString("room_no"));
            return rooms;
        }
    }

    public List<String> findRoomNosByType(String roomType) throws SQLException {
        String sql = "SELECT room_no FROM rooms WHERE room_type = ? ORDER BY room_no";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomType);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> rooms = new ArrayList<>();
                while (rs.next()) rooms.add(rs.getString("room_no"));
                return rooms;
            }
        }
    }

	public String findAllRooms() {
		// TODO Auto-generated method stub
		return null;
	}
	// Add at bottom of RoomDAO.java
	public java.util.List<String> findAllRoomTypes() throws SQLException {
	    String sql = "SELECT DISTINCT room_type FROM rooms ORDER BY room_type";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        java.util.List<String> types = new java.util.ArrayList<>();
	        while (rs.next()) types.add(rs.getString(1));
	        return types;
	    }
	}

	public List<String> findRoomNumbersByType(String selectedCategory) throws SQLException {
		   String sql = "SELECT room_no FROM rooms WHERE room_type = ? ORDER BY room_no";
	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setString(1,selectedCategory);
	            try (ResultSet rs = ps.executeQuery()) {
	                List<String> rooms = new ArrayList<>();
	                while (rs.next()) rooms.add(rs.getString("room_no"));
	                return rooms;
	            }
	        }
	}
	public String getRoomTypeByRoomNumber(String roomNumber) throws Exception {
	    String query = "SELECT room_type FROM rooms WHERE room_no = ?";
	    
	    try (Connection conn =  DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        
	        stmt.setString(1, roomNumber);
	        ResultSet rs = stmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("room_type");
	        }
	    }
	    
	    return null;
	}
	public boolean isRoomAvailable(String roomNo, LocalDate checkIn, LocalDate checkOut) throws Exception {
	    String query = """
	        SELECT COUNT(*) FROM (
	            SELECT 1 FROM bookings 
	            WHERE room_no = ? 
	            AND ((check_in_date <= ? AND check_out_date > ?) 
	            OR (check_in_date < ? AND check_out_date >= ?))
	            UNION ALL
	            SELECT 1 FROM reservations 
	            WHERE room_no = ? 
	            AND ((check_in_date <= ? AND check_out_date > ?) 
	            OR (check_in_date < ? AND check_out_date >= ?))
	        ) AS occupied_rooms
	        """;
	    
	    try (Connection conn =  DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        
	        stmt.setString(1, roomNo);
	        stmt.setDate(2, java.sql.Date.valueOf(checkIn));
	        stmt.setDate(3, java.sql.Date.valueOf(checkIn));
	        stmt.setDate(4, java.sql.Date.valueOf(checkOut));
	        stmt.setDate(5, java.sql.Date.valueOf(checkOut));
	        stmt.setString(6, roomNo);
	        stmt.setDate(7, java.sql.Date.valueOf(checkIn));
	        stmt.setDate(8, java.sql.Date.valueOf(checkIn));
	        stmt.setDate(9, java.sql.Date.valueOf(checkOut));
	        stmt.setDate(10, java.sql.Date.valueOf(checkOut));
	        
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) == 0;
	        }
	    }
	    
	    return false;
	}
}
