package application.services.dao;



import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.models.FinanceModels.DailyCharges;
import application.models.FinanceModels.DailySheetData;
import application.models.FinanceModels.DailySheetDetailData;
import application.models.FinanceModels.Expense;
import application.models.FinanceModels.FinancialSummary;
import application.models.FinanceModels.HotelExpense;
import application.models.FinanceModels.Payment;
import application.models.FinanceModels.ReservationPayment;
import application.models.FinanceModels.ReturnPayment;

/**
 * Data Access Object for Finance operations - H2 Database Compatible
 * Handles all database interactions for financial data
 */
public class FinanceDAO {
    
    // ===== PAYMENT OPERATIONS (H2 COMPATIBLE) =====
    
//    public List<Payment> getPaymentHistory(LocalDate fromDate, LocalDate toDate) throws SQLException {
//        List<Payment> payments = new ArrayList<>();
//        // H2 compatible: use CAST to DATE instead of DATE() function
//        String sql = """
//            SELECT p.id, p.amount, p.method, p.transaction_id, p.payment_date, p.notes,
//                   g.name as guest_name, b.room_no, g.phone, g.nationality
//            FROM payments p
//            JOIN bookings b ON p.booking_id = b.id
//            JOIN guests g ON b.guest_id = g.id
//            WHERE CAST(p.payment_date AS DATE) >= ? AND CAST(p.payment_date AS DATE) <= ?
//            ORDER BY p.payment_date DESC
//            LIMIT 1000
//            """;
//        
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            
//            ps.setDate(1, Date.valueOf(fromDate));
//            ps.setDate(2, Date.valueOf(toDate));
//            
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    Payment payment = new Payment();
//                    payment.setId(rs.getLong("id"));
//                    payment.setAmount(rs.getBigDecimal("amount"));
//                    payment.setMethod(rs.getString("method"));
//                    payment.setTransactionId(rs.getString("transaction_id"));
//                    payment.setPaymentDate(rs.getTimestamp("payment_date"));
//                    payment.setNotes(rs.getString("notes"));
//                    payment.setGuestName(rs.getString("guest_name"));
//                    payment.setRoomNo(rs.getString("room_no"));
//                    payment.setPhone(rs.getString("phone"));
//                    payment.setNationality(rs.getString("nationality"));
//                    payments.add(payment);
//                }
//            }
//        }
//        return payments;
//    }

    public List<ReservationPayment> getReservationPayments(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<ReservationPayment> payments = new ArrayList<>();
        // H2 compatible date filtering
        String sql = """
            SELECT rp.id, rp.reservation_id, rp.amount, rp.method, rp.notes, rp.payment_date,
                   g.name as guest_name, r.room_no, g.phone
            FROM reservation_payments rp
            JOIN reservations r ON rp.reservation_id = r.id
            JOIN guests g ON r.guest_id = g.id
            WHERE CAST(rp.payment_date AS DATE) >= ? AND CAST(rp.payment_date AS DATE) <= ?
            ORDER BY rp.payment_date DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReservationPayment payment = new ReservationPayment();
                    payment.setId(rs.getLong("id"));
                    payment.setReservationId(rs.getLong("reservation_id"));
                    payment.setAmount(rs.getBigDecimal("amount"));
                    payment.setMethod(rs.getString("method"));
                    payment.setNotes(rs.getString("notes"));
                    payment.setPaymentDate(rs.getTimestamp("payment_date"));
                    payment.setGuestName(rs.getString("guest_name"));
                    payment.setRoomNo(rs.getString("room_no"));
                    payment.setPhone(rs.getString("phone"));
                    payments.add(payment);
                }
            }
        }
        return payments;
    }

    // ===== EXPENSE OPERATIONS (H2 COMPATIBLE) =====
    
    public List<Expense> getExpenseHistory(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        // Using service_used as expense proxy with H2 date syntax
        String sql = """
            SELECT su.id, su.service_date, su.service, su.category, 
                   su.total_amount, g.name as guest_name, b.room_no
            FROM service_used su
            JOIN bookings b ON su.booking_id = b.id
            JOIN guests g ON b.guest_id = g.id
            WHERE su.service_date >= ? AND su.service_date <= ?
            ORDER BY su.service_date DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Expense expense = new Expense();
                    expense.setId(rs.getLong("id"));
                    expense.setExpenseDate(rs.getDate("service_date").toLocalDate());
                    expense.setCategory(rs.getString("category"));
                    expense.setDescription(rs.getString("service"));
                    expense.setAmount(rs.getBigDecimal("total_amount"));
                    expense.setGuestName(rs.getString("guest_name"));
                    expense.setRoomNo(rs.getString("room_no"));
                    expense.setPaidBy("Guest"); // Default value
                    expenses.add(expense);
                }
            }
        }
        return expenses;
    }

    // ===== DAILY CHARGES OPERATIONS (H2 COMPATIBLE) =====
    
    public List<DailyCharges> getDailyCharges(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<DailyCharges> charges = new ArrayList<>();
        // H2 compatible date functions and calculations
        String sql = """
            SELECT dcl.booking_id, dcl.calculation_date, dcl.charges_incurred, dcl.pending_amount,
                   b.room_no, g.name as guest_name,
                   (dcl.charges_incurred - dcl.pending_amount) as received_amount,
                   DATEDIFF('DAY', b.check_in_date, COALESCE(b.check_out_date, CURRENT_DATE)) as days_stayed,
                   CASE 
                       WHEN DATEDIFF('DAY', b.check_in_date, COALESCE(b.check_out_date, CURRENT_DATE)) > 0
                       THEN dcl.charges_incurred / DATEDIFF('DAY', b.check_in_date, COALESCE(b.check_out_date, CURRENT_DATE))
                       ELSE dcl.charges_incurred
                   END as daily_rate
            FROM daily_charges_log dcl
            JOIN bookings b ON dcl.booking_id = b.id
            JOIN guests g ON b.guest_id = g.id
            WHERE dcl.calculation_date >= ? AND dcl.calculation_date <= ?
            ORDER BY dcl.calculation_date DESC, b.room_no
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailyCharges charge = new DailyCharges();
                    charge.setBookingId(rs.getLong("booking_id"));
                    charge.setCalculationDate(rs.getDate("calculation_date").toLocalDate());
                    charge.setChargesIncurred(rs.getBigDecimal("charges_incurred"));
                    charge.setPendingAmount(rs.getBigDecimal("pending_amount"));
                    charge.setReceivedAmount(rs.getBigDecimal("received_amount"));
                    charge.setRoomNo(rs.getString("room_no"));
                    charge.setGuestName(rs.getString("guest_name"));
                    charge.setDaysStayed(rs.getInt("days_stayed"));
                    charge.setDailyRate(rs.getBigDecimal("daily_rate"));
                    charges.add(charge);
                }
            }
        }
        return charges;
    }

    // ===== FINANCIAL SUMMARY OPERATIONS (H2 COMPATIBLE) =====
    
    public FinancialSummary getFinancialSummary(LocalDate fromDate, LocalDate toDate) throws SQLException {
        FinancialSummary summary = new FinancialSummary();
        summary.setFromDate(fromDate);
        summary.setToDate(toDate);
        
        // H2 compatible query with CAST for date comparison
        String summarySQL = """
            SELECT 
                COALESCE(SUM(p.amount), 0) as total_payments,
                COALESCE((SELECT SUM(rp.amount) FROM reservation_payments rp 
                         WHERE CAST(rp.payment_date AS DATE) >= ? AND CAST(rp.payment_date AS DATE) <= ?), 0) as total_reservation_payments,
                COALESCE((SELECT SUM(su.total_amount) FROM service_used su 
                         WHERE su.service_date >= ? AND su.service_date <= ?), 0) as total_expenses
            FROM payments p
            WHERE CAST(p.payment_date AS DATE) >= ? AND CAST(p.payment_date AS DATE) <= ?
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(summarySQL)) {
            
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            ps.setDate(3, Date.valueOf(fromDate));
            ps.setDate(4, Date.valueOf(toDate));
            ps.setDate(5, Date.valueOf(fromDate));
            ps.setDate(6, Date.valueOf(toDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    summary.setTotalPayments(rs.getBigDecimal("total_payments"));
                    summary.setTotalReservationPayments(rs.getBigDecimal("total_reservation_payments"));
                    summary.setTotalExpenses(rs.getBigDecimal("total_expenses"));
                }
            }
        }
        
        // Calculate totals
        BigDecimal totalIncome = summary.getTotalPayments().add(summary.getTotalReservationPayments());
        summary.setTotalIncome(totalIncome);
        summary.setNetBalance(totalIncome.subtract(summary.getTotalExpenses()));
        
        return summary;
    }

    // ===== SEARCH OPERATIONS (H2 COMPATIBLE) =====
    
    public List<Map<String, Object>> searchTransactions(String searchTerm, LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String searchPattern = "%" + searchTerm.toLowerCase() + "%";
        
        // H2 compatible search query
        String searchSQL = """
            SELECT 'PAYMENT' as type, p.id, p.amount, g.name as guest_name, b.room_no, p.payment_date
            FROM payments p
            JOIN bookings b ON p.booking_id = b.id
            JOIN guests g ON b.guest_id = g.id
            WHERE (LOWER(g.name) LIKE ? OR LOWER(b.room_no) LIKE ? OR LOWER(COALESCE(p.transaction_id, '')) LIKE ?)
            AND CAST(p.payment_date AS DATE) >= ? AND CAST(p.payment_date AS DATE) <= ?
            
            UNION ALL
            
            SELECT 'RESERVATION' as type, rp.id, rp.amount, g.name as guest_name, r.room_no, rp.payment_date
            FROM reservation_payments rp
            JOIN reservations r ON rp.reservation_id = r.id
            JOIN guests g ON r.guest_id = g.id
            WHERE (LOWER(g.name) LIKE ? OR LOWER(r.room_no) LIKE ?)
            AND CAST(rp.payment_date AS DATE) >= ? AND CAST(rp.payment_date AS DATE) <= ?
            
            ORDER BY payment_date DESC
            LIMIT 100
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(searchSQL)) {
            
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setDate(4, Date.valueOf(fromDate));
            ps.setDate(5, Date.valueOf(toDate));
            ps.setString(6, searchPattern);
            ps.setString(7, searchPattern);
            ps.setDate(8, Date.valueOf(fromDate));
            ps.setDate(9, Date.valueOf(toDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("type", rs.getString("type"));
                    result.put("id", rs.getLong("id"));
                    result.put("amount", rs.getBigDecimal("amount"));
                    result.put("guest_name", rs.getString("guest_name"));
                    result.put("room_no", rs.getString("room_no"));
                    result.put("date", rs.getTimestamp("payment_date"));
                    results.add(result);
                }
            }
        }
        return results;
    }

    // ===== UTILITY METHODS (H2 COMPATIBLE) =====
    
    public void addPayment(Payment payment) throws SQLException {
        String sql = """
            INSERT INTO payments (booking_id, amount, method, transaction_id, payment_date, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setLong(1, payment.getBookingId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getMethod());
            ps.setString(4, payment.getTransactionId());
            ps.setTimestamp(5, payment.getPaymentDate());
            ps.setString(6, payment.getNotes());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
    }

    public boolean updatePayment(Payment payment) throws SQLException {
        String sql = """
            UPDATE payments 
            SET amount = ?, method = ?, transaction_id = ?, notes = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setBigDecimal(1, payment.getAmount());
            ps.setString(2, payment.getMethod());
            ps.setString(3, payment.getTransactionId());
            ps.setString(4, payment.getNotes());
            ps.setLong(5, payment.getId());
            
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePayment(Long paymentId) throws SQLException {
        String sql = "DELETE FROM payments WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, paymentId);
            return ps.executeUpdate() > 0;
        }
    }

    // ===== SIMPLE QUERIES FOR TESTING =====
    
    /**
     * Get all payments without date filtering - for testing
     */
//    public List<Payment> getAllPayments() throws SQLException {
//        List<Payment> payments = new ArrayList<>();
//        String sql = """
//            SELECT p.id, p.amount, p.method, p.transaction_id, p.payment_date, p.notes,
//                   g.name as guest_name, b.room_no, g.phone, g.nationality
//            FROM payments p
//            LEFT JOIN bookings b ON p.booking_id = b.id
//            LEFT JOIN guests g ON b.guest_id = g.id
//            ORDER BY p.payment_date DESC
//            LIMIT 100
//            """;
//        
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            
//            while (rs.next()) {
//                Payment payment = new Payment();
//                payment.setId(rs.getLong("id"));
//                payment.setAmount(rs.getBigDecimal("amount"));
//                payment.setMethod(rs.getString("method"));
//                payment.setTransactionId(rs.getString("transaction_id"));
//                payment.setPaymentDate(rs.getTimestamp("payment_date"));
//                payment.setNotes(rs.getString("notes"));
//                payment.setGuestName(rs.getString("guest_name"));
//                payment.setRoomNo(rs.getString("room_no"));
//                payment.setPhone(rs.getString("phone"));
//                payment.setNationality(rs.getString("nationality"));
//                payments.add(payment);
//            }
//        }
//        return payments;
//    }

 // ===== RETURN PAYMENTS OPERATIONS =====
    public List<ReturnPayment> getReturnPayments(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<ReturnPayment> returns = new ArrayList<>();
        String sql = """
            SELECT rp.id, rp.return_date, rp.return_type, rp.return_reason,
                   rp.return_amount, rp.return_method, rp.status, rp.notes,
                   g.name as guest_name, 
                   COALESCE(b.room_no, 'N/A') as room_no
            FROM return_payments rp
            JOIN guests g ON rp.guest_id = g.id
            LEFT JOIN bookings b ON rp.booking_id = b.id
            WHERE rp.return_date >= ? AND rp.return_date <= ?
            ORDER BY rp.return_date DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReturnPayment returnPayment = new ReturnPayment();
                    returnPayment.setId(rs.getLong("id"));
                    returnPayment.setReturnDate(rs.getDate("return_date").toLocalDate());
                    returnPayment.setReturnType(rs.getString("return_type"));
                    returnPayment.setReturnReason(rs.getString("return_reason"));
                    returnPayment.setReturnAmount(rs.getBigDecimal("return_amount"));
                    returnPayment.setReturnMethod(rs.getString("return_method"));
                    returnPayment.setGuestName(rs.getString("guest_name"));
                    returnPayment.setRoomNo(rs.getString("room_no"));
                    returnPayment.setStatus(rs.getString("status"));
                    returnPayment.setNotes(rs.getString("notes"));
                    returns.add(returnPayment);
                }
            }
        }
        return returns;
    }

    // ===== HOTEL EXPENSES OPERATIONS =====
 // ===== UPDATED: Hotel Expenses Query =====
    public List<HotelExpense> getHotelExpenseHistory(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<HotelExpense> expenses = new ArrayList<>();
        String sql = """
            SELECT he.id, he.expense_date, he.category, he.subcategory, he.description, 
                   he.amount, he.payment_method, he.vendor_name, he.department, 
                   COALESCE(s1.first_name || ' ' || s1.last_name, 'System') as paid_by_name,
                   he.status, he.notes, he.paid_by
            FROM hotel_expenses he
            LEFT JOIN staff s1 ON he.paid_by = s1.id
            WHERE he.expense_date >= ? AND he.expense_date <= ?
            ORDER BY he.expense_date DESC, he.created_at DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HotelExpense expense = new HotelExpense();
                    expense.setId(rs.getLong("id"));
                    expense.setExpenseDate(rs.getDate("expense_date").toLocalDate());
                    expense.setCategory(rs.getString("category"));
                    expense.setSubcategory(rs.getString("subcategory"));
                    expense.setDescription(rs.getString("description"));
                    expense.setAmount(rs.getBigDecimal("amount"));
                    expense.setPaymentMethod(rs.getString("payment_method"));
                    expense.setVendorName(rs.getString("vendor_name"));
                    expense.setDepartment(rs.getString("department"));
                    expense.setPaidById(rs.getLong("paid_by")); // FIXED: Use new method name
                    expense.setPaidByName(rs.getString("paid_by_name")); // FIXED: Set display name
                    expense.setStatus(rs.getString("status"));
                    expense.setNotes(rs.getString("notes"));
                    expenses.add(expense);
                }
            }
        }
        return expenses;
    }


    // ===== DAILY SHEET OPERATIONS =====
//    public List<DailySheetDetailData> getDailySheetDetailData(LocalDate selectedDate) throws SQLException {
//        List<DailySheetDetailData> dailyData = new ArrayList<>();
//        String sql = """
//            SELECT r.room_no, r.status as room_status, r.price as room_rate,
//                   g.name as guest_name,
//                   b.check_in_date, b.check_out_date, b.total_amount,
//                   COALESCE(daily_payments.payment_received, 0) as today_payment_received,
//                   COALESCE(dcl.pending_amount, 0) as pending_amount
//            FROM rooms r
//            LEFT JOIN bookings b ON r.room_no = b.room_no 
//                AND ? BETWEEN b.check_in_date AND COALESCE(b.check_out_date, CURRENT_DATE)
//                AND b.status IN ('Checked-in', 'Confirmed')
//            LEFT JOIN guests g ON b.guest_id = g.id
//            LEFT JOIN daily_charges_log dcl ON b.id = dcl.booking_id 
//                AND dcl.calculation_date = ?
//            LEFT JOIN (
//                SELECT b2.room_no,
//                       SUM(p.amount) as payment_received
//                FROM payments p
//                JOIN bookings b2 ON p.booking_id = b2.id
//                WHERE CAST(p.payment_date AS DATE) = ?
//                GROUP BY b2.room_no
//            ) daily_payments ON r.room_no = daily_payments.room_no
//            ORDER BY r.room_no
//            """;
//        
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            
//            ps.setDate(1, Date.valueOf(selectedDate));
//            ps.setDate(2, Date.valueOf(selectedDate));
//            ps.setDate(3, Date.valueOf(selectedDate));
//            
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    DailySheetDetailData data = new DailySheetDetailData();
//                    data.setRoomNo(rs.getString("room_no"));
//                    data.setRoomStatus(rs.getString("guest_name") != null ? "Occupied" : "Available");
//                    data.setGuestName(rs.getString("guest_name"));
//                    
//                    Date checkIn = rs.getDate("check_in_date");
//                    Date checkOut = rs.getDate("check_out_date");
//                    data.setCheckInDate(checkIn != null ? checkIn.toLocalDate() : null);
//                    data.setCheckOutDate(checkOut != null ? checkOut.toLocalDate() : null);
//                    
//                    BigDecimal roomRate = rs.getBigDecimal("room_rate");
//                    data.setTodayAmount(roomRate != null ? roomRate : BigDecimal.ZERO);
//                    data.setTodayPaymentReceived(rs.getBigDecimal("today_payment_received"));
//                    data.setPendingAmount(rs.getBigDecimal("pending_amount"));
//                    
//                    BigDecimal totalAmount = rs.getBigDecimal("total_amount");
//                    data.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
//                    
//                    dailyData.add(data);
//                }
//            }
//        }
//        return dailyData;
//    }
 // ===== MISSING METHOD: Get Daily Sheet Data =====
    public List<DailySheetData> getDailySheetData(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<DailySheetData> dailyData = new ArrayList<>();
        String sql = """
            SELECT r.room_no,
                   ? as selected_date,
                   CASE 
                       WHEN b.id IS NOT NULL AND ? BETWEEN b.check_in_date AND COALESCE(b.check_out_date, CURRENT_DATE)
                       THEN true 
                       ELSE false 
                   END as is_occupied,
                   r.price as room_rate,
                   COALESCE(daily_payments.payment_received, 0) as today_payment_received,
                   COALESCE(dcl.pending_amount, 0) as pending_amount,
                   COALESCE(b.total_amount, 0) as total_amount
            FROM rooms r
            LEFT JOIN bookings b ON r.room_no = b.room_no 
                AND ? BETWEEN b.check_in_date AND COALESCE(b.check_out_date, CURRENT_DATE)
                AND b.status IN ('Checked-in', 'Confirmed')
            LEFT JOIN daily_charges_log dcl ON b.id = dcl.booking_id 
                AND dcl.calculation_date BETWEEN ? AND ?
            LEFT JOIN (
                SELECT b2.room_no,
                       SUM(p.amount) as payment_received
                FROM payments p
                JOIN bookings b2 ON p.booking_id = b2.id
                WHERE CAST(p.payment_date AS DATE) BETWEEN ? AND ?
                GROUP BY b2.room_no
            ) daily_payments ON r.room_no = daily_payments.room_no
            ORDER BY r.room_no
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(fromDate));
            ps.setDate(4, Date.valueOf(fromDate));
            ps.setDate(5, Date.valueOf(toDate));
            ps.setDate(6, Date.valueOf(fromDate));
            ps.setDate(7, Date.valueOf(toDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailySheetData data = new DailySheetData();
                    data.setRoomNo(rs.getString("room_no"));
                    data.setDate(rs.getDate("selected_date").toLocalDate());
                    data.setOccupied(rs.getBoolean("is_occupied"));
                    
                    BigDecimal roomRate = rs.getBigDecimal("room_rate");
                    data.setTodayAmount(roomRate != null ? roomRate : BigDecimal.ZERO);
                    data.setTodayPaymentReceived(rs.getBigDecimal("today_payment_received"));
                    data.setPendingAmount(rs.getBigDecimal("pending_amount"));
                    data.setTotalAmount(rs.getBigDecimal("total_amount"));
                    
                    dailyData.add(data);
                }
            }
        }
        return dailyData;
    }
 // ===== FIXED: Daily Sheet Detail Data with Proper Room Status =====
//    public List<DailySheetDetailData> getDailySheetDetailData(LocalDate selectedDate) throws SQLException {
//        List<DailySheetDetailData> dailyData = new ArrayList<>();
//        String sql = """
//            SELECT 
//                r.room_no, 
//                r.status as room_table_status,
//                r.price as room_rate,
//                g.name as guest_name,
//                b.check_in_date, 
//                b.check_out_date, 
//                b.total_amount,
//                b.status as booking_status,
//                COALESCE(daily_payments.payment_received, 0) as today_payment_received,
//                COALESCE(dcl.pending_amount, 0) as pending_amount,
//                -- Determine actual room occupancy status
//                CASE 
//                    WHEN b.id IS NOT NULL 
//                         AND ? BETWEEN b.check_in_date AND COALESCE(b.check_out_date, CURRENT_DATE)
//                         AND b.status IN ('Checked-in', 'Confirmed') 
//                    THEN 'Occupied'
//                    ELSE r.status
//                END as actual_room_status
//            FROM rooms r
//            LEFT JOIN bookings b ON r.room_no = b.room_no 
//                AND ? BETWEEN b.check_in_date AND COALESCE(b.check_out_date, CURRENT_DATE)
//                AND b.status IN ('Checked-in', 'Confirmed')
//            LEFT JOIN guests g ON b.guest_id = g.id
//            LEFT JOIN daily_charges_log dcl ON b.id = dcl.booking_id 
//                AND dcl.calculation_date = ?
//            LEFT JOIN (
//                SELECT b2.room_no,
//                       SUM(p.amount) as payment_received
//                FROM payments p
//                JOIN bookings b2 ON p.booking_id = b2.id
//                WHERE CAST(p.payment_date AS DATE) = ?
//                GROUP BY b2.room_no
//            ) daily_payments ON r.room_no = daily_payments.room_no
//            ORDER BY r.room_no
//            """;
//        
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            
//            ps.setDate(1, Date.valueOf(selectedDate));
//            ps.setDate(2, Date.valueOf(selectedDate));
//            ps.setDate(3, Date.valueOf(selectedDate));
//            ps.setDate(4, Date.valueOf(selectedDate));
//            
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    DailySheetDetailData data = new DailySheetDetailData();
//                    data.setRoomNo(rs.getString("room_no"));
//                    data.setRoomStatus(rs.getString("actual_room_status"));
//                    data.setGuestName(rs.getString("guest_name"));
//                    
//                    Date checkIn = rs.getDate("check_in_date");
//                    Date checkOut = rs.getDate("check_out_date");
//                    data.setCheckInDate(checkIn != null ? checkIn.toLocalDate() : null);
//                    data.setCheckOutDate(checkOut != null ? checkOut.toLocalDate() : null);
//                    
//                    BigDecimal roomRate = rs.getBigDecimal("room_rate");
//                    data.setTodayAmount(roomRate != null ? roomRate : BigDecimal.ZERO);
//                    data.setTodayPaymentReceived(rs.getBigDecimal("today_payment_received"));
//                    data.setPendingAmount(rs.getBigDecimal("pending_amount"));
//                    
//                    BigDecimal totalAmount = rs.getBigDecimal("total_amount");
//                    data.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
//                    
//                    dailyData.add(data);
//                }
//            }
//        }
//        return dailyData;
//    }

    // ===== FIXED: Daily Sheet Data for Main View =====
 // ===== FIXED: Daily Sheet Detail Data with Proper Pending Amount Calculation =====
    public List<DailySheetDetailData> getDailySheetDetailData(LocalDate selectedDate) throws SQLException {
        List<DailySheetDetailData> dailyData = new ArrayList<>();
        String sql = """
            SELECT 
                r.room_no, 
                r.status as room_table_status,
                r.price as room_rate,
                g.name as guest_name,
                b.check_in_date, 
                b.check_out_date, 
                b.total_amount,
                b.status as booking_status,
                -- Calculate today's payment received
                COALESCE(daily_payments.payment_received, 0) as today_payment_received,
                -- Calculate payment methods for today
                COALESCE(daily_payments.payment_methods, 'N/A') as payment_methods,
                -- Calculate total pending amount (Total booking - all payments received)
                CASE 
                    WHEN b.id IS NOT NULL THEN 
                        COALESCE(b.total_amount, 0) - COALESCE(all_payments.total_payments, 0)
                    ELSE 0
                END as pending_amount,
                -- Determine actual room occupancy status
                CASE 
                    WHEN b.id IS NOT NULL 
                         AND ? BETWEEN b.check_in_date AND COALESCE(b.check_out_date, CURRENT_DATE)
                         AND b.status IN ('Checked-in', 'Confirmed') 
                    THEN 'Occupied'
                    ELSE r.status
                END as actual_room_status
            FROM rooms r
            LEFT JOIN bookings b ON r.room_no = b.room_no 
                AND ? BETWEEN b.check_in_date AND COALESCE(b.check_out_date, CURRENT_DATE)
                AND b.status IN ('Checked-in', 'Confirmed')
            LEFT JOIN guests g ON b.guest_id = g.id
            LEFT JOIN (
                -- Today's payments for this room
                SELECT b2.room_no,
                       SUM(p.amount) as payment_received,
                       STRING_AGG(DISTINCT p.method, ', ') as payment_methods
                FROM payments p
                JOIN bookings b2 ON p.booking_id = b2.id
                WHERE CAST(p.payment_date AS DATE) = ?
                GROUP BY b2.room_no
            ) daily_payments ON r.room_no = daily_payments.room_no
            LEFT JOIN (
                -- All payments for this booking to calculate pending
                SELECT b3.id as booking_id,
                       SUM(p2.amount) as total_payments
                FROM payments p2
                JOIN bookings b3 ON p2.booking_id = b3.id
                GROUP BY b3.id
            ) all_payments ON b.id = all_payments.booking_id
            ORDER BY r.room_no
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, Date.valueOf(selectedDate));
            ps.setDate(2, Date.valueOf(selectedDate));
            ps.setDate(3, Date.valueOf(selectedDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailySheetDetailData data = new DailySheetDetailData();
                    data.setRoomNo(rs.getString("room_no"));
                    data.setRoomStatus(rs.getString("actual_room_status"));
                    data.setGuestName(rs.getString("guest_name"));
                    
                    Date checkIn = rs.getDate("check_in_date");
                    Date checkOut = rs.getDate("check_out_date");
                    data.setCheckInDate(checkIn != null ? checkIn.toLocalDate() : null);
                    data.setCheckOutDate(checkOut != null ? checkOut.toLocalDate() : null);
                    
                    BigDecimal roomRate = rs.getBigDecimal("room_rate");
                    data.setTodayAmount(roomRate != null ? roomRate : BigDecimal.ZERO);
                    data.setTodayPaymentReceived(rs.getBigDecimal("today_payment_received"));
                    data.setPendingAmount(rs.getBigDecimal("pending_amount"));
                    
                    // Set payment methods
                    data.setPaymentMethods(rs.getString("payment_methods"));
                    
                    BigDecimal totalAmount = rs.getBigDecimal("total_amount");
                    data.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
                    
                    dailyData.add(data);
                }
            }
        }
        return dailyData;
    }

    
    

    /**
     * Get simple financial summary without complex date filtering
     */
    public FinancialSummary getSimpleFinancialSummary() throws SQLException {
        FinancialSummary summary = new FinancialSummary();
        
        String summarySQL = """
            SELECT 
                COALESCE(SUM(p.amount), 0) as total_payments,
                COALESCE((SELECT SUM(rp.amount) FROM reservation_payments rp), 0) as total_reservation_payments,
                COALESCE((SELECT SUM(su.total_amount) FROM service_used su), 0) as total_expenses
            FROM payments p
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(summarySQL);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                summary.setTotalPayments(rs.getBigDecimal("total_payments"));
                summary.setTotalReservationPayments(rs.getBigDecimal("total_reservation_payments"));
                summary.setTotalExpenses(rs.getBigDecimal("total_expenses"));
            }
        }
        
        // Calculate totals
        BigDecimal totalIncome = summary.getTotalPayments().add(summary.getTotalReservationPayments());
        summary.setTotalIncome(totalIncome);
        summary.setNetBalance(totalIncome.subtract(summary.getTotalExpenses()));
        
        return summary;
    }
    
 // ===== FIXED: Payment History with Better Date Filtering =====
    public List<Payment> getPaymentHistory(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = """
            SELECT p.id, p.booking_id, p.amount, p.method, p.transaction_id, 
                   p.payer, p.notes, p.payment_date,
                   g.name as guest_name, g.phone, g.nationality,
                   b.room_no
            FROM payments p
            JOIN guests g ON p.guest_id = g.id
            LEFT JOIN bookings b ON p.booking_id = b.id
            WHERE CAST(p.payment_date AS DATE) >= ? 
              AND CAST(p.payment_date AS DATE) <= ?
            ORDER BY p.payment_date DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            
            // Add debug logging
            System.out.println("Payment query: " + sql);
            System.out.println("Date range: " + fromDate + " to " + toDate);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment payment = new Payment();
                    payment.setId(rs.getLong("id"));
                    payment.setBookingId(rs.getLong("booking_id"));
                    payment.setAmount(rs.getBigDecimal("amount"));
                    payment.setMethod(rs.getString("method"));
                    payment.setTransactionId(rs.getString("transaction_id"));
                    payment.setNotes(rs.getString("notes"));
                    payment.setPaymentDate(rs.getTimestamp("payment_date"));
                    payment.setGuestName(rs.getString("guest_name"));
                    payment.setPhone(rs.getString("phone"));
                    payment.setNationality(rs.getString("nationality"));
                    payment.setRoomNo(rs.getString("room_no"));
                    payments.add(payment);
                }
            }
        }
        return payments;
    }

    // ===== ADD: Method to get all payments for debugging =====
    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = """
            SELECT p.id, p.booking_id, p.amount, p.method, p.transaction_id, 
                   p.payer, p.notes, p.payment_date,
                   g.name as guest_name, g.phone, g.nationality,
                   b.room_no
            FROM payments p
            JOIN guests g ON p.guest_id = g.id
            LEFT JOIN bookings b ON p.booking_id = b.id
            ORDER BY p.payment_date DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment payment = new Payment();
                    payment.setId(rs.getLong("id"));
                    payment.setBookingId(rs.getLong("booking_id"));
                    payment.setAmount(rs.getBigDecimal("amount"));
                    payment.setMethod(rs.getString("method"));
                    payment.setTransactionId(rs.getString("transaction_id"));
                    payment.setNotes(rs.getString("notes"));
                    payment.setPaymentDate(rs.getTimestamp("payment_date"));
                    payment.setGuestName(rs.getString("guest_name"));
                    payment.setPhone(rs.getString("phone"));
                    payment.setNationality(rs.getString("nationality"));
                    payment.setRoomNo(rs.getString("room_no"));
                    payments.add(payment);
                }
            }
        }
        return payments;
    }

}
