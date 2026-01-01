package application.services.dao;

import application.models.Booking;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class ReservationServiceDAO {

    // Returns bookings overlapping [start, end) grouped by room_no with optional filters
    public Map<String, List<Booking>> findBookingsOverlappingByRoom(LocalDate start,
                                                                    LocalDate end,
                                                                    String roomTypeFilter,
                                                                    String roomNoFilter,
                                                                    String guestSearchTerm) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT b.id, b.guest_id, b.room_no, b.check_in_date, b.check_out_date, " +
            "       g.name AS guest_name, r.room_type " +
            "FROM bookings b " +
            "JOIN guests g ON b.guest_id = g.id " +
            "JOIN rooms r ON b.room_no = r.room_no " +
            "WHERE b.check_out_date > ? AND b.check_in_date < ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(start));
        params.add(Date.valueOf(end));

        if (roomTypeFilter != null && !"All Categories".equals(roomTypeFilter)) {
            sql.append(" AND r.room_type = ? ");
            params.add(roomTypeFilter);
        }
        if (roomNoFilter != null && !"All Rooms".equals(roomNoFilter)) {
            sql.append(" AND b.room_no = ? ");
            params.add(roomNoFilter);
        }
        if (guestSearchTerm != null && !guestSearchTerm.trim().isEmpty()) {
            sql.append(" AND LOWER(g.name) LIKE ? ");
            params.add("%" + guestSearchTerm.trim().toLowerCase() + "%");
        }
        sql.append(" ORDER BY b.check_in_date, g.name");

        Map<String, List<Booking>> byRoom = new HashMap<>();

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
                    Booking b = new Booking();
                    b.setId(rs.getLong("id"));
                    b.setGuestId(rs.getLong("guest_id"));
                    b.setRoomNo(rs.getString("room_no"));
                    b.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                    b.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                    try { b.setGuestName(rs.getString("guest_name")); } catch (SQLException ignore) {}
                    try { b.setRoomType(rs.getString("room_type")); } catch (SQLException ignore) {}
                    byRoom.computeIfAbsent(b.getRoomNo(), k -> new ArrayList<>()).add(b);
                }
            }
        }
        return byRoom;
    }
}
