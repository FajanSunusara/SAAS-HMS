package application.services.dao;

import application.models.PaymentHistoryEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentHistoryDao {

    public List<PaymentHistoryEntry> getByBookingId(long bookingId) {
        List<PaymentHistoryEntry> list = new ArrayList<>();

        // Order by a true timestamp for chronological ordering
        String sql =
            "SELECT payment_date AS ts, CAST(payment_date AS VARCHAR(30)) AS date_str, amount, method AS description " +
            "FROM payments WHERE booking_id = ? " +
            "UNION ALL " +
            "SELECT CAST(invoice_date AS TIMESTAMP) AS ts, CAST(invoice_date AS VARCHAR(30)) AS date_str, total AS amount, 'Invoice' AS description " +
            "FROM invoices WHERE booking_id = ? " +
            "ORDER BY ts";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bookingId);
            ps.setLong(2, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PaymentHistoryEntry(
                        rs.getString("date_str"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertPayment(long bookingId, long guestId, java.math.BigDecimal amount, String method) {
        String sql = "INSERT INTO payments (booking_id, guest_id, amount, method) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setLong(2, guestId);
            ps.setBigDecimal(3, amount);
            ps.setString(4, method);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<PaymentHistoryEntry> getByBookingId(Long bookingId) throws SQLException {
        List<PaymentHistoryEntry> history = new ArrayList<>();
        
        String sql = """
            SELECT payment_date, amount, method, transaction_id, notes
            FROM payments 
            WHERE booking_id = ? 
            ORDER BY payment_date DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getTimestamp("payment_date").toString();
                    double amount = rs.getDouble("amount");
                    String method = rs.getString("method");
                    String txnId = rs.getString("transaction_id");
                    String notes = rs.getString("notes");
                    
                    String description = String.format("%s - %s", 
                                                     method != null ? method : "Payment",
                                                     txnId != null ? txnId : "");
                    if (notes != null && !notes.trim().isEmpty()) {
                        description += " (" + notes + ")";
                    }
                    
                    history.add(new PaymentHistoryEntry(date, amount, description));
                }
            }
        }
        
        return history;
    }
}
