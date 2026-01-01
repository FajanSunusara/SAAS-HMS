package application.services.dao;

import application.models.ReservationStub;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReservationDAO {

    // Return reservations overlapping [start, end) grouped by room_no with optional filters
	   // Return reservations overlapping [start, end) grouped by room_no with optional filters
    public Map<String, List<ReservationStub>> findReservationsOverlappingByRoom(
            LocalDate start,
            LocalDate end,
            String roomTypeFilter,
            String roomNoFilter,
            String guestSearchTerm) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT r.id, r.guest_id, r.room_no, " +
                " r.start_date AS check_in_date, r.end_date AS check_out_date, " +
                " g.name AS guest_name, rm.room_type " +
                "FROM reservations r " +
                "JOIN guests g ON r.guest_id = g.id " +
                "JOIN rooms rm ON r.room_no = rm.room_no " +
                "WHERE r.end_date > ? AND r.start_date < ? "
        );
        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(start));
        params.add(Date.valueOf(end));

        if (roomTypeFilter != null && !"All Categories".equals(roomTypeFilter)) {
            sql.append(" AND rm.room_type = ? ");
            params.add(roomTypeFilter);
        }
        if (roomNoFilter != null && !"All Rooms".equals(roomNoFilter)) {
            sql.append(" AND r.room_no = ? ");
            params.add(roomNoFilter);
        }
        if (guestSearchTerm != null && !guestSearchTerm.trim().isEmpty()) {
            sql.append(" AND LOWER(g.name) LIKE ? ");
            params.add("%" + guestSearchTerm.trim().toLowerCase() + "%");
        }
        sql.append(" ORDER BY r.start_date, g.name");

        Map<String, List<ReservationStub>> byRoom = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Date) {
                    ps.setDate(i + 1, (Date) p);
                } else if (p instanceof String) {
                    ps.setString(i + 1, (String) p);
                } else {
                    ps.setObject(i + 1, p);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReservationStub r = new ReservationStub();
                    r.setId(rs.getLong("id"));
                    r.setGuestId(rs.getLong("guest_id"));
                    r.setRoomNo(rs.getString("room_no"));
                    r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                    r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                    try { r.setGuestName(rs.getString("guest_name")); } catch (SQLException ignore) {}
                    try { r.setRoomType(rs.getString("room_type")); } catch (SQLException ignore) {}
                    byRoom.computeIfAbsent(r.getRoomNo(), k -> new ArrayList<>()).add(r);
                }
            }
        }
        return byRoom;
    }

    public Long addReservation(ReservationStub res) throws SQLException {
        String sql = "INSERT INTO reservations (guest_id, room_no, room_type, start_date, end_date, status, num_guests, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, res.getGuestId());
            ps.setString(2, res.getRoomNo());
            ps.setString(3, res.getRoomType());
            ps.setDate(4, Date.valueOf(res.getCheckInDate()));
            ps.setDate(5, Date.valueOf(res.getCheckOutDate()));
            ps.setString(6, res.getStatus() == null ? "Confirmed" : res.getStatus());
            if (res.getNumGuests() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, res.getNumGuests());
            ps.setString(8, res.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    res.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public void updateReservation(ReservationStub res) throws SQLException {
        String sql = "UPDATE reservations SET guest_id=?, room_no=?, room_type=?, start_date=?, end_date=?, status=?, num_guests=?, notes=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, res.getGuestId());
            ps.setString(2, res.getRoomNo());
            ps.setString(3, res.getRoomType());
            ps.setDate(4, Date.valueOf(res.getCheckInDate()));
            ps.setDate(5, Date.valueOf(res.getCheckOutDate()));
            ps.setString(6, res.getStatus());
            if (res.getNumGuests() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, res.getNumGuests());
            ps.setString(8, res.getNotes());
            ps.setLong(9, res.getId());
            ps.executeUpdate();
        }
    }

    public ReservationStub getReservationById(long id) throws SQLException {
        String sql = "SELECT r.id, r.guest_id, r.room_no, r.room_type, r.start_date, r.end_date, r.status, r.num_guests, r.notes " +
                "FROM reservations r WHERE r.id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ReservationStub r = new ReservationStub();
                    r.setId(rs.getLong("id"));
                    r.setGuestId(rs.getLong("guest_id"));
                    r.setRoomNo(rs.getString("room_no"));
                    r.setRoomType(rs.getString("room_type"));
                    r.setCheckInDate(rs.getDate("start_date").toLocalDate());
                    r.setCheckOutDate(rs.getDate("end_date").toLocalDate());
                    r.setStatus(rs.getString("status"));
                    int ng = rs.getInt("num_guests");
                    r.setNumGuests(rs.wasNull() ? null : ng);
                    r.setNotes(rs.getString("notes"));
                    return r;
                }
            }
        }
        return null;
    }
    public void deleteFullReservation(long reservationId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            try {
                // Get all booking IDs associated with this reservation ID
                List<Long> bookingIds = new ArrayList<>();
                String getBookingsSql = "SELECT id FROM bookings WHERE reservation_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(getBookingsSql)) {
                    ps.setLong(1, reservationId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            bookingIds.add(rs.getLong("id"));
                        }
                    }
                }

                // Delete all records related to bookings first
                if (!bookingIds.isEmpty()) {
                    String bookingIdsString = bookingIds.stream()
                                                      .map(String::valueOf)
                                                      .collect(Collectors.joining(", "));

                    // 1. Delete Service Items
                    String deleteServiceItemsSql = "DELETE FROM service_items WHERE service_used_id IN (SELECT id FROM service_used WHERE booking_id IN (" + bookingIdsString + "))";
                    try (PreparedStatement ps = conn.prepareStatement(deleteServiceItemsSql)) {
                        ps.executeUpdate();
                    }
                    
                    // 2. Delete Services Used
                    String deleteServicesUsedSql = "DELETE FROM service_used WHERE booking_id IN (" + bookingIdsString + ")";
                    try (PreparedStatement ps = conn.prepareStatement(deleteServicesUsedSql)) {
                        ps.executeUpdate();
                    }

                    // 3. Delete Payments
                    String deletePaymentsSql = "DELETE FROM payments WHERE booking_id IN (" + bookingIdsString + ")";
                    try (PreparedStatement ps = conn.prepareStatement(deletePaymentsSql)) {
                        ps.executeUpdate();
                    }

                    // 4. Delete Invoices
                    String deleteInvoicesSql = "DELETE FROM invoices WHERE booking_id IN (" + bookingIdsString + ")";
                    try (PreparedStatement ps = conn.prepareStatement(deleteInvoicesSql)) {
                        ps.executeUpdate();
                    }
                    
                    // 5. Delete Room Change History
                    String deleteRoomChangeHistorySql = "DELETE FROM room_change_history WHERE booking_id IN (" + bookingIdsString + ")";
                    try (PreparedStatement ps = conn.prepareStatement(deleteRoomChangeHistorySql)) {
                        ps.executeUpdate();
                    }

                    // 6. Delete Daily Charges Log
                    String deleteDailyChargesLogSql = "DELETE FROM daily_charges_log WHERE booking_id IN (" + bookingIdsString + ")";
                    try (PreparedStatement ps = conn.prepareStatement(deleteDailyChargesLogSql)) {
                        ps.executeUpdate();
                    }

                    // 7. Delete Bookings
                    String deleteBookingsSql = "DELETE FROM bookings WHERE reservation_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(deleteBookingsSql)) {
                        ps.setLong(1, reservationId);
                        ps.executeUpdate();
                    }
                }

                // 8. Delete Reservation Payments
                String deleteReservationPaymentsSql = "DELETE FROM reservation_payments WHERE reservation_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteReservationPaymentsSql)) {
                    ps.setLong(1, reservationId);
                    ps.executeUpdate();
                }

                // 9. Finally, delete the reservation
                String deleteReservationSql = "DELETE FROM reservations WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteReservationSql)) {
                    ps.setLong(1, reservationId);
                    ps.executeUpdate();
                }

                // Commit the transaction
                conn.commit();

            } catch (SQLException e) {
                // Rollback the transaction on error
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    // NEW: delete
    public void deleteReservation(long id) throws SQLException {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // Optional: mark as arrived (if you prefer to keep history in reservations)
    public void markReservationArrived(long id) throws SQLException {
        String sql = "UPDATE reservations SET status='Arrived' WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
    
}
