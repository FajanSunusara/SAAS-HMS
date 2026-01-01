package application.services.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReportsDao {

    public BigDecimal getRevenueBetween(LocalDate start, LocalDate end) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payment_history WHERE payment_date BETWEEN ? AND ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }

    public Map<String, Long> getBookingsByRoomType(LocalDate start, LocalDate end) {
        Map<String, Long> result = new HashMap<>();
        String sql = "SELECT room_type, COUNT(*) as cnt FROM bookings " +
                     "WHERE check_in_date >= ? AND check_out_date <= ? GROUP BY room_type";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getString("room_type"), rs.getLong("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public BigDecimal getPendingPaymentsTotal(LocalDate start, LocalDate end) {
        String sql = """
            SELECT COALESCE(SUM(i.total - COALESCE(p.paid, 0)), 0)
            FROM invoices i
            JOIN bookings b ON b.id = i.booking_id
            LEFT JOIN (
                SELECT booking_id, SUM(amount) as paid FROM payment_history GROUP BY booking_id
            ) p ON p.booking_id = b.id
            WHERE b.check_in_date >= ? AND b.check_out_date <= ? 
              AND COALESCE(p.paid,0) < i.total
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }
}
