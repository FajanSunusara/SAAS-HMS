package application.services.dao;

import application.models.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    public Payment getPaymentById(Long id) throws SQLException {
        String sql = "SELECT * FROM payments WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToPayment(rs);
            }
        }
        return null;
    }

    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) payments.add(mapResultSetToPayment(rs));
        }
        return payments;
    }

    public List<Payment> getPendingPayments() throws SQLException {
        // There is no status column; "pending payments" should be derived from bookings/invoices.
        // Use DashboardPopupDao.getPendingPayments() instead. Keep this for compatibility and return empty here.
        return new ArrayList<>();
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setBookingId(rs.getLong("booking_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentDate(rs.getDate("payment_date"));
        payment.setPaymentMethod(rs.getString("method"));
        // no status in schema; skip
        payment.setCreatedAt(null); // not in schema
        return payment;
    }
    public List<Payment> getPaymentsForBooking(Long bookingId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE booking_id = ? ORDER BY payment_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        }
        return payments;
    }


}
