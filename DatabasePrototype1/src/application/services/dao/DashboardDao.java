package application.services.dao;

import java.math.BigDecimal;
import java.sql.*;

public class DashboardDao {

    public long getTotalGuests() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM guests")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long getActiveBookingsToday() {
        String sql = "SELECT COUNT(*) FROM bookings WHERE check_in_date <= CURRENT_DATE AND check_out_date >= CURRENT_DATE";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Replace payment_history with payments and use a half-open interval for TIMESTAMP
    public BigDecimal getTodayRevenue() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) FROM payments
            WHERE payment_date >= CURRENT_DATE
              AND payment_date < DATEADD(DAY, 1, CURRENT_DATE)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }

    // If you need “pending payments” count, compute from bookings/invoices.
    public long getPendingPaymentsCount() {
        String sql = """
            SELECT COUNT(*) FROM (
                SELECT b.id
                FROM bookings b
                JOIN invoices i ON i.booking_id = b.id
                LEFT JOIN payments p ON p.booking_id = b.id
                GROUP BY b.id, i.total
                HAVING COALESCE(SUM(p.amount), 0) < i.total
            )
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
