package application.services.dao;

import application.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DashboardPopupDao {
    
    private final DailyChargesDAO dailyChargesDAO = new DailyChargesDAO();
    
    /**
     * Get pending payments with real-time calculations
     */
    public ObservableList<Payment> getPendingPayments() throws SQLException {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        String sql = """
            SELECT b.id, g.name AS guest_name, b.room_no, b.check_in_date, 
                   b.check_out_date, b.advance_paid, r.price
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            JOIN rooms r ON b.room_no = r.room_no
            WHERE b.status IN ('Checked-in', 'Confirmed')
            AND b.check_in_date <= CURRENT_DATE
            ORDER BY b.check_out_date
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Long bookingId = rs.getLong("id");
                String guestName = rs.getString("guest_name");
                String roomNo = rs.getString("room_no");
                LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();
                double dailyRate = rs.getDouble("price");
                double advancePaid = rs.getDouble("advance_paid");
                
                // Calculate real-time pending amount
                double pendingAmount = calculateRealTimePending(checkIn, checkOut, dailyRate, advancePaid);
                
                if (pendingAmount > 0.01) { // Only show if there's a meaningful pending amount
                    Payment payment = new Payment(
                        guestName,
                        roomNo,
                        pendingAmount,
                        checkOut.toString()
                    );
                    payments.add(payment);
                }
            }
        }
        return payments;
    }
    
    /**
     * Calculate real-time pending amount based on days stayed
     */
    private double calculateRealTimePending(LocalDate checkIn, LocalDate checkOut, 
                                          double dailyRate, double advancePaid) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveDate = today.isBefore(checkOut) ? today : checkOut;
        
        // Calculate days stayed from check-in to current date (or checkout if past)
        int daysStayed = (int) ChronoUnit.DAYS.between(checkIn, effectiveDate);
        if (daysStayed < 0) daysStayed = 0;
        
        // Calculate charges incurred so far
        double roomCharges = dailyRate * daysStayed;
//        double serviceCharges = roomCharges * 0.05; // 5% service charge
//        double subtotal = roomCharges + serviceCharges;
        double gst = roomCharges * 0.18; // 18% GST
        double totalChargesIncurred =roomCharges + gst;
        
        // Return pending amount (charges incurred - advance paid)
        return Math.max(0, totalChargesIncurred - advancePaid);
    }
    
    private double calculateRealTimeTotal(LocalDate checkIn, LocalDate checkOut, 
            double dailyRate, double advancePaid) {
LocalDate today = LocalDate.now();
LocalDate effectiveDate = today.isBefore(checkOut) ? today : checkOut;

// Calculate days stayed from check-in to current date (or checkout if past)
int daysStayed = (int) ChronoUnit.DAYS.between(checkIn, effectiveDate);
if (daysStayed < 0) daysStayed = 0;

// Calculate charges incurred so far
double roomCharges = dailyRate * daysStayed;
//double serviceCharges = roomCharges * 0.05; // 5% service charge
//double subtotal = roomCharges + serviceCharges;
double gst = roomCharges * 0.18; // 18% GST
double totalChargesIncurred =roomCharges + gst;

// Return pending amount (charges incurred - advance paid)
return Math.max(0, totalChargesIncurred - advancePaid);
}
    
    public ObservableList<TodayReservation> getTodaysReservations() throws SQLException {
        ObservableList<TodayReservation> reservations = FXCollections.observableArrayList();
        
        // UNION query to get both bookings and reservations for today
        String sql = """
            SELECT guest_name, room_no, check_in_date, check_out_date, status, source_type, record_id FROM (
                -- Today's check-ins from bookings table
                SELECT g.name AS guest_name, b.room_no,
                       b.check_in_date, b.check_out_date,
                       CASE
                           WHEN b.status IN ('Reserved','Confirmed') THEN 'Not Arrived'
                           WHEN b.status = 'Checked-in' THEN 'Arrived'
                           ELSE b.status
                       END AS status,
                       'Booking' AS source_type,
                       CAST(b.id AS VARCHAR) AS record_id
                FROM bookings b
                JOIN guests g ON b.guest_id = g.id
                WHERE b.check_in_date = CURRENT_DATE
                AND b.status IN ('Reserved','Confirmed','Checked-in')

                UNION ALL

                -- Today's reservations from reservations table
                SELECT g.name AS guest_name, r.room_no,
                       r.start_date AS check_in_date, r.end_date AS check_out_date,
                       CASE
                           WHEN r.status = 'Confirmed' THEN 'Reservation Confirmed'
                           WHEN r.status = 'Pending' THEN 'Reservation Pending'
                           ELSE r.status
                       END AS status,
                       'Reservation' AS source_type,
                       CAST(r.id AS VARCHAR) AS record_id
                FROM reservations r
                JOIN guests g ON r.guest_id = g.id
                WHERE r.start_date = CURRENT_DATE
                AND r.status IN ('Confirmed','Pending')
            ) AS combined_results
            ORDER BY guest_name, source_type
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                reservations.add(new TodayReservation(
                    rs.getString("guest_name"),
                    rs.getString("room_no"),
                    rs.getDate("check_in_date").toString(),
                    rs.getDate("check_out_date").toString(),
                    rs.getString("status"),
                    rs.getString("record_id")  // This will be the booking ID or reservation ID
                ));
            }
        }
        
        return reservations;
    }


    
    /**
     * Get guest by room number with real-time pending calculation
     */
    public Guest getGuestByRoomNo(String roomNo) throws SQLException {
        String sql = """
            SELECT g.*, b.room_no, b.check_in_date, b.check_out_date,
                   1 AS number_of_people, b.total_amount, b.advance_paid,
                   b.id AS booking_id, r.price
            FROM guests g
            JOIN bookings b ON g.id = b.guest_id
            JOIN rooms r ON b.room_no = r.room_no
            WHERE b.room_no = ?
            AND b.status IN ('Checked-in','Confirmed')
            AND (CURRENT_DATE < b.check_out_date) AND (CURRENT_DATE >= b.check_in_date)
            ORDER BY b.check_in_date DESC
            LIMIT 1
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, roomNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Guest guest = new Guest();
                    guest.setId(rs.getLong("id"));
                    guest.setName(rs.getString("name"));
                    guest.setPhone(rs.getString("phone"));
                    guest.setEmail(rs.getString("email"));
                    guest.setAddress(rs.getString("address"));
                    guest.setRoomNumber(roomNo);
                    guest.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                    guest.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                    guest.setNumberOfPeople(rs.getInt("number_of_people"));
                    
                    // Calculate real-time pending amount
                    LocalDate checkIn = guest.getCheckInDate();
                    LocalDate checkOut = guest.getCheckOutDate();	
                    double dailyRate = rs.getDouble("price");
                    double advancePaid = rs.getDouble("advance_paid");
                    
                    double realTimePending = calculateRealTimePending(checkIn, checkOut, dailyRate, advancePaid);
                    
                    System.out.println(realTimePending+""+rs.getString("name"));
//                    double totalAmount = realTimePending + advancePaid;
                    double totalAmount = rs.getDouble("total_amount");
                    double Pending = totalAmount-advancePaid;
                    guest.setPendingAmount(Pending);
                    guest.setTotalAmount(totalAmount);
                    guest.setAdvancePaid(advancePaid);
                    guest.setStayAmount(realTimePending);
                    
                    // Load payment history
                    PaymentHistoryDao paymentDao = new PaymentHistoryDao();
                    long bookingId = rs.getLong("booking_id");
                    guest.setPaymentHistory(FXCollections.observableArrayList(
                        paymentDao.getByBookingId(bookingId)
                    ));
                    
                    return guest;
                }
            }
        }
        return null;
    }
    
    // Keep existing methods unchanged
    public ObservableList<Booking> getUpcomingBookings() throws SQLException {
        ObservableList<Booking> bookings = FXCollections.observableArrayList();
        String sql = """
            SELECT b.id, g.name AS guest_name, b.check_in_date,
                   r.room_type, b.room_no, b.status
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            JOIN rooms r ON b.room_no = r.room_no
            WHERE b.check_in_date > CURRENT_DATE
            AND b.status IN ('Confirmed', 'Reserved')
            ORDER BY b.check_in_date
            LIMIT 10
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getLong("id"));
                booking.setGuestName(rs.getString("guest_name"));
                booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                booking.setRoomType(rs.getString("room_type"));
                booking.setRoomNo(rs.getString("room_no"));
                bookings.add(booking);
            }
        }
        return bookings;
    }
    
    public ObservableList<Booking> getCancellingBookings() throws SQLException {
        ObservableList<Booking> bookings = FXCollections.observableArrayList();
        String sql = """
            SELECT b.id, g.name AS guest_name, b.check_in_date,
                   r.room_type, b.room_no, b.status
            FROM bookings b
            JOIN guests g ON b.guest_id = g.id
            JOIN rooms r ON b.room_no = r.room_no
            WHERE b.status = 'Cancelled'
            ORDER BY b.check_in_date DESC
            LIMIT 10
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getLong("id"));
                booking.setGuestName(rs.getString("guest_name"));
                booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                booking.setRoomType(rs.getString("room_type"));
                booking.setRoomNo(rs.getString("room_no"));
                bookings.add(booking);
            }
        }
        return bookings;
    }
    
    public int getTotalOccupiedRooms() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE status = 'Occupied'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
    
    public int getTotalAvailableRooms() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE status = 'Available'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
    
    public BigDecimal getTotalRevenue() throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(p.amount), 0) AS total_revenue
            FROM payments p
            WHERE p.payment_date >= CURRENT_DATE
            AND p.payment_date < DATEADD(DAY, 1, CURRENT_DATE)
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal("total_revenue");
        }
        return BigDecimal.ZERO;
    }
    
    public int getTodaysCheckIns() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE check_in_date = CURRENT_DATE";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
    
    public int getTodaysCheckOuts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE check_out_date = CURRENT_DATE AND status = 'Checked-in'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
