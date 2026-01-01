package application.services.dao;

import application.models.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class BookingDAO {

    public Long addBooking(Booking booking) throws SQLException {
        String sql = """
            INSERT INTO bookings (reservation_id, guest_id, room_no, check_in_date, check_out_date, 
                                 total_amount, advance_paid, payment_status, status, nationality, 
                                 rate_per_night, document_link, gst_rate, base_amount, gst_amount, gst_included) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            int idx = 1;
            pstmt.setObject(idx++, booking.getReservationId());
            pstmt.setLong(idx++, booking.getGuestId());
            pstmt.setString(idx++, booking.getRoomNo());
            pstmt.setDate(idx++, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(idx++, Date.valueOf(booking.getCheckOutDate()));
            pstmt.setBigDecimal(idx++, booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO);
            pstmt.setBigDecimal(idx++, booking.getAdvancePaid() != null ? booking.getAdvancePaid() : BigDecimal.ZERO);
            pstmt.setString(idx++, booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "Pending");
            pstmt.setString(idx++, booking.getStatus() != null ? booking.getStatus() : "Confirmed");
            pstmt.setString(idx++, booking.getNationality() != null ? booking.getNationality() : "India");
            pstmt.setBigDecimal(idx++, booking.getRatePerNight());
            pstmt.setString(idx++, booking.getDocumentLink());
            pstmt.setBigDecimal(idx++, booking.getGstRate() != null ? booking.getGstRate() : new BigDecimal("18.00"));
            pstmt.setBigDecimal(idx++, booking.getBaseAmount());
            pstmt.setBigDecimal(idx++, booking.getGstAmount());
            pstmt.setBoolean(idx++, booking.getGstIncluded() != null ? booking.getGstIncluded() : false);
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    booking.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public Booking getBookingById(Long id) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBooking(rs);
                }
            }
        }
        return null;
    }

    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    public List<Booking> getUpcomingBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.*, g.name AS guest_name, r.room_type 
            FROM bookings b 
            JOIN guests g ON b.guest_id = g.id 
            JOIN rooms r ON b.room_no = r.room_no 
            WHERE b.check_in_date > CURRENT_DATE
            ORDER BY b.check_in_date
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Booking booking = mapResultSetToBooking(rs);
                booking.setGuestName(rs.getString("guest_name"));
                booking.setRoomType(rs.getString("room_type"));
                bookings.add(booking);
            }
        }
        return bookings;
    }

    public Booking getBookingByRoomNo(String roomNo) throws SQLException {
        String sql = """
            SELECT * FROM bookings 
            WHERE room_no = ? AND check_out_date >= CURRENT_DATE 
            ORDER BY check_in_date DESC LIMIT 1
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBooking(rs);
                }
            }
        }
        return null;
    }

    public void updateBooking(Booking booking) throws SQLException {
        String sql = """
            UPDATE bookings SET guest_id = ?, room_no = ?, check_in_date = ?, check_out_date = ?, 
                               total_amount = ?, advance_paid = ?, payment_status = ?, status = ?,
                               nationality = ?, rate_per_night = ?, document_link = ?, gst_rate = ?,
                               base_amount = ?, gst_amount = ?, gst_included = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int idx = 1;
            pstmt.setLong(idx++, booking.getGuestId());
            pstmt.setString(idx++, booking.getRoomNo());
            pstmt.setDate(idx++, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(idx++, Date.valueOf(booking.getCheckOutDate()));
            pstmt.setBigDecimal(idx++, booking.getTotalAmount());
            pstmt.setBigDecimal(idx++, booking.getAdvancePaid());
            pstmt.setString(idx++, booking.getPaymentStatus());
            pstmt.setString(idx++, booking.getStatus());
            pstmt.setString(idx++, booking.getNationality());
            pstmt.setBigDecimal(idx++, booking.getRatePerNight());
            pstmt.setString(idx++, booking.getDocumentLink());
            pstmt.setBigDecimal(idx++, booking.getGstRate());
            pstmt.setBigDecimal(idx++, booking.getBaseAmount());
            pstmt.setBigDecimal(idx++, booking.getGstAmount());
            pstmt.setBoolean(idx++, booking.getGstIncluded());
            pstmt.setLong(idx++, booking.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteBooking(Long id) throws SQLException {
        String sql = "DELETE FROM bookings WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getLong("id"));
        booking.setReservationId(getNullableLong(rs, "reservation_id"));
        booking.setGuestId(rs.getLong("guest_id"));
        booking.setRoomNo(rs.getString("room_no"));
        booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        booking.setTotalAmount(rs.getBigDecimal("total_amount"));
        booking.setAdvancePaid(rs.getBigDecimal("advance_paid"));
        booking.setPaymentStatus(rs.getString("payment_status"));
        booking.setStatus(rs.getString("status"));
        
        // MAP NEW FIELDS
        booking.setNationality(rs.getString("nationality"));
        booking.setRatePerNight(rs.getBigDecimal("rate_per_night"));
        booking.setDocumentLink(rs.getString("document_link"));
        booking.setGstRate(rs.getBigDecimal("gst_rate"));
        booking.setBaseAmount(rs.getBigDecimal("base_amount"));
        booking.setGstAmount(rs.getBigDecimal("gst_amount"));
        booking.setGstIncluded(rs.getBoolean("gst_included"));
        
        return booking;
    }

    private Long getNullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    public Long getGuestIdByBookingId(long bookingId) throws SQLException {
        String sql = "SELECT guest_id FROM bookings WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("guest_id");
            }
        }
        return null;
    }

    public Booking getActiveBookingByRoomNo(String roomNo) throws SQLException {
        String sql = """
            SELECT b.*, g.name as guest_name FROM bookings b 
            JOIN guests g ON b.guest_id = g.id 
            WHERE b.room_no = ? AND b.status IN ('Checked-in', 'Confirmed') 
            ORDER BY b.check_in_date DESC LIMIT 1
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Booking booking = mapResultSetToBooking(rs);
                    booking.setGuestName(rs.getString("guest_name"));
                    return booking;
                }
            }
        }
        return null;
    }

    public Booking getBookingWithGuestById(Long bookingId) throws SQLException {
        String sql = """
            SELECT b.*, g.name as guest_name FROM bookings b 
            JOIN guests g ON b.guest_id = g.id 
            WHERE b.id = ?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Booking booking = mapResultSetToBooking(rs);
                    booking.setGuestName(rs.getString("guest_name"));
                    return booking;
                }
            }
        }
        return null;
    }

    public List<Booking> getActiveBookingsWithGuests() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.*, g.name as guest_name FROM bookings b 
            JOIN guests g ON b.guest_id = g.id 
            WHERE b.status IN ('Checked-in', 'Confirmed') 
            ORDER BY b.room_no
            """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Booking booking = mapResultSetToBooking(rs);
                booking.setGuestName(rs.getString("guest_name"));
                bookings.add(booking);
            }
        }
        return bookings;
    }

    public double calculateDailyPendingAmount(Long bookingId) throws SQLException {
        String sql = """
            SELECT b.check_in_date, b.check_out_date,
                   COALESCE(b.advance_paid, 0) as advance_paid,
                   COALESCE(b.rate_per_night, r.price, 0) as daily_rate,
                   DATEDIFF(DAY, b.check_in_date, CURRENT_DATE) as days_stayed
            FROM bookings b
            LEFT JOIN rooms r ON b.room_no = r.room_no
            WHERE b.id = ? AND b.status IN ('Checked-in', 'Confirmed')
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int daysStayed = Math.max(0, rs.getInt("days_stayed"));
                    double dailyRate = rs.getDouble("daily_rate");
                    double advancePaid = rs.getDouble("advance_paid");
                    
                    // Calculate charges incurred so far (room + GST)
                    double roomCharges = daysStayed * dailyRate;
                    double gstCharges = roomCharges * 0.18;
                    double totalChargesIncurred = roomCharges + gstCharges;
                    
                    return Math.max(0, totalChargesIncurred - advancePaid);
                }
            }
        }
        return 0.0;
    }

    public List<Booking> getActiveBookingsForDailyUpdate() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.*, g.name as guest_name
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            WHERE b.status IN ('Checked-in', 'Confirmed')
            AND b.check_out_date > CURRENT_DATE
            AND b.check_in_date <= CURRENT_DATE
            """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Booking booking = mapResultSetToBooking(rs);
                booking.setGuestName(rs.getString("guest_name"));
                bookings.add(booking);
            }
        }
        return bookings;
    }
}
