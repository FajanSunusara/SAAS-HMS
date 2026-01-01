package application.services.dao;

import application.models.Guest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {

    public void addGuest(Guest guest) throws SQLException {
        String sql = "INSERT INTO guests (name, address, phone, email, gst_number, id_type, id_number, nationality) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, guest.getName());
            pstmt.setString(2, guest.getAddress());
            pstmt.setString(3, guest.getPhone());
            pstmt.setString(4, guest.getEmail());
            pstmt.setString(5, guest.getGstNumber());
            pstmt.setString(6, guest.getIdProof());   // maps to id_type
            pstmt.setString(7, guest.getIdNumber());  // id_number
            pstmt.setString(8, guest.getNationality());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) guest.setId(generatedKeys.getLong(1));
            }
        }
    }

    public Guest getGuestById(Long id) throws SQLException {
        String sql = "SELECT id, name, address, phone, email, gst_number, id_type, id_number, nationality FROM guests WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToGuest(rs);
            }
        }
        return null;
    }

    public List<Guest> getAllGuests() throws SQLException {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT id, name, address, phone, email, gst_number, id_type, id_number, nationality FROM guests";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) guests.add(mapResultSetToGuest(rs));
        }
        return guests;
    }

    public void updateGuest(Guest guest) throws SQLException {
        String sql = "UPDATE guests SET name = ?, address = ?, phone = ?, email = ?, gst_number = ?, id_type = ?, id_number = ?, nationality = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guest.getName());
            pstmt.setString(2, guest.getAddress());
            pstmt.setString(3, guest.getPhone());
            pstmt.setString(4, guest.getEmail());
            pstmt.setString(5, guest.getGstNumber());
            pstmt.setString(6, guest.getIdProof());   // id_type
            pstmt.setString(7, guest.getIdNumber());  // id_number
            pstmt.setString(8, guest.getNationality());
            pstmt.setLong(9, guest.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteGuest(Long id) throws SQLException {
        String sql = "DELETE FROM guests WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        Guest guest = new Guest();
        guest.setId(rs.getLong("id"));
        guest.setName(rs.getString("name"));
        guest.setAddress(rs.getString("address"));
        guest.setPhone(rs.getString("phone"));
        guest.setEmail(rs.getString("email"));
        guest.setGstNumber(rs.getString("gst_number"));
        guest.setIdProof(rs.getString("id_type"));   // map to model field idProof
        guest.setIdNumber(rs.getString("id_number"));
        guest.setNationality(rs.getString("nationality"));
        return guest;
    }

    // === Helpers for Reservation page filters ===
    public List<String> findAllRoomTypes() throws SQLException {
        String sql = "SELECT DISTINCT room_type FROM rooms ORDER BY room_type";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> types = new ArrayList<>();
            while (rs.next()) types.add(rs.getString(1));
            return types;
        }
    }

    public Long ensureGuest(String name, String email, String phone) throws SQLException {
        if (name == null || name.trim().isEmpty()) return null;
        String findSql = "SELECT id FROM guests WHERE LOWER(name)=LOWER(?) AND (email IS NOT NULL AND LOWER(email)=LOWER(?)) LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email == null ? "" : email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        String insertSql = "INSERT INTO guests (name, email, phone, nationality) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, email == null ? null : email.trim());
            ps.setString(3, phone == null ? null : phone.trim());
            ps.setString(4, "India"); // Default nationality
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getLong(1);
                }
            }
        }
        return null;
    }

    // Add or update based on name+email simple heuristic, return guestId
    public Long addOrUpdateGuest(Guest guest) throws SQLException {
        if (guest == null || guest.getName() == null || guest.getName().trim().isEmpty()) return null;

        String selectSql = "SELECT id FROM guests WHERE LOWER(name)=LOWER(?) AND (email IS NOT NULL AND LOWER(email)=LOWER(?)) LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, guest.getName().trim());
            ps.setString(2, guest.getEmail() == null ? "" : guest.getEmail().trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long existingId = rs.getLong("id");
                    guest.setId(existingId);
                    updateGuest(guest); // reuse your existing update
                    return existingId;
                }
            }
        }

        // Insert new
        addGuest(guest); // reuse your existing insert
        return guest.getId();
    }

    // Additional method to find guest by phone number (used by PaymentController)
    public Guest getGuestByPhone(String phone) throws SQLException {
        String sql = "SELECT id, name, address, phone, email, gst_number, id_type, id_number, nationality FROM guests WHERE phone = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToGuest(rs);
            }
        }
        return null;
    }

    // Method to search guests by name, phone, or email (used by BookingController)
    public List<Guest> searchGuests(String query) throws SQLException {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT id, name, address, phone, email, gst_number, id_type, id_number, nationality " +
                    "FROM guests WHERE LOWER(name) LIKE ? OR phone LIKE ? OR LOWER(email) LIKE ? " +
                    "ORDER BY name LIMIT 20";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + query.toLowerCase() + "%";
            ps.setString(1, like);
            ps.setString(2, "%" + query + "%");
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    guests.add(mapResultSetToGuest(rs));
                }
            }
        }
        return guests;
    }
}
