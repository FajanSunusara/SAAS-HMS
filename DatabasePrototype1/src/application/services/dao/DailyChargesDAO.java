package application.services.dao;

import application.models.DailyCharges;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DailyChargesDAO {
    
    /**
     * Calculate real-time pending amount for a booking based on days stayed
     */
    public double calculateRealTimePendingAmount(Long bookingId) throws SQLException {
        String sql = """
            SELECT b.check_in_date, b.check_out_date, b.advance_paid,
                   r.price, COALESCE(b.total_amount, 0) as original_total
            FROM bookings b
            JOIN rooms r ON b.room_no = r.room_no
            WHERE b.id = ? AND b.status IN ('Checked-in', 'Confirmed')
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                    LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();
                    double dailyRate = rs.getDouble("price");
                    double advancePaid = rs.getDouble("advance_paid");
                    
                    // Calculate days stayed from check-in to current date
                    LocalDate today = LocalDate.now();
                    LocalDate effectiveDate = today.isBefore(checkOut) ? today : checkOut;
                    
                    int daysStayed = (int) ChronoUnit.DAYS.between(checkIn, effectiveDate);
                    if (daysStayed < 0) daysStayed = 0;
                    
                    // Calculate charges incurred so far
                    double roomCharges = dailyRate * daysStayed;
                    double serviceCharges = roomCharges * 0.05; // 5% service charge
                    double subtotal = roomCharges + serviceCharges;
                    double gst = subtotal * 0.18; // 18% GST
                    double totalChargesIncurred = subtotal + gst;
                    
                    // Calculate current pending amount
                    return Math.max(0, totalChargesIncurred - advancePaid);
                }
            }
        }
        return 0.0;
    }
    
    /**
     * Get all bookings with real-time pending amounts
     */
    public List<DailyCharges> getAllBookingsWithRealTimePending() throws SQLException {
        List<DailyCharges> charges = new ArrayList<>();
        String sql = """
            SELECT b.id, b.guest_id, b.room_no, b.check_in_date, b.check_out_date,
                   b.advance_paid, b.status, g.name as guest_name, r.price
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            JOIN rooms r ON b.room_no = r.room_no
            WHERE b.status IN ('Checked-in', 'Confirmed')
            AND b.check_in_date <= CURRENT_DATE
            ORDER BY b.check_in_date
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                DailyCharges charge = new DailyCharges();
                charge.setBookingId(rs.getLong("id"));
                charge.setGuestId(rs.getLong("guest_id"));
                charge.setGuestName(rs.getString("guest_name"));
                charge.setRoomNo(rs.getString("room_no"));
                charge.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                charge.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                charge.setDailyRate(rs.getDouble("price"));
                charge.setAdvancePaid(rs.getDouble("advance_paid"));
                charge.setStatus(rs.getString("status"));
                
                // Calculate real-time pending amount
                double pendingAmount = calculateRealTimePendingAmount(charge.getBookingId());
                charge.setPendingAmount(pendingAmount);
                
                charges.add(charge);
            }
        }
        return charges;
    }
    
    /**
     * Update invoice with real-time calculations
     */
    public void updateInvoiceWithRealTimeCharges(Long bookingId) throws SQLException {
        double pendingAmount = calculateRealTimePendingAmount(bookingId);
        
        String sql = """
            SELECT advance_paid FROM bookings WHERE id = ?
            """;
        
        double advancePaid = 0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    advancePaid = rs.getDouble("advance_paid");
                }
            }
        }
        
        double totalIncurred = pendingAmount + advancePaid;
        double subtotal = totalIncurred / 1.18; // Remove GST to get subtotal
        double gst = totalIncurred - subtotal;
        
        // Update or insert invoice
        String updateSql = """
            UPDATE invoices 
            SET subtotal = ?, gst = ?, total = ?, due_amount = ?, invoice_date = CURRENT_DATE
            WHERE booking_id = ?
            """;
        
        String insertSql = """
            INSERT INTO invoices (booking_id, guest_id, invoice_number, invoice_date, 
                                subtotal, gst, total, paid_amount, due_amount)
            SELECT ?, guest_id, CONCAT('INV-', YEAR(CURRENT_DATE), '-', LPAD(?, 6, '0')), 
                   CURRENT_DATE, ?, ?, ?, ?, ?
            FROM bookings WHERE id = ?
            """;
        
        try (Connection conn = DatabaseManager.getConnection()) {
            // Try update first
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setDouble(1, subtotal);
                ps.setDouble(2, gst);
                ps.setDouble(3, totalIncurred);
                ps.setDouble(4, pendingAmount);
                ps.setLong(5, bookingId);
                
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    // Insert new invoice
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setLong(1, bookingId);
                        insertPs.setLong(2, bookingId);
                        insertPs.setDouble(3, subtotal);
                        insertPs.setDouble(4, gst);
                        insertPs.setDouble(5, totalIncurred);
                        insertPs.setDouble(6, advancePaid);
                        insertPs.setDouble(7, pendingAmount);
                        insertPs.setLong(8, bookingId);
                        insertPs.executeUpdate();
                    }
                }
            }
        }
    }
    
    /**
     * Run daily update process for all active bookings
     */
    public void runDailyUpdate() throws SQLException {
        List<DailyCharges> allCharges = getAllBookingsWithRealTimePending();
        
        for (DailyCharges charge : allCharges) {
            updateInvoiceWithRealTimeCharges(charge.getBookingId());
        }
        
        System.out.println("Daily charges updated for " + allCharges.size() + " bookings");
    }
}
