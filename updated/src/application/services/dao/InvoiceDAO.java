package application.services.dao;

import application.models.Invoice;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class InvoiceDAO {

    public List<Invoice> getAllInvoicesWithDetails() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        String sql = """
            SELECT i.id, i.booking_id, i.guest_id, i.invoice_number, i.invoice_date,
                   i.subtotal, i.gst, i.total, i.paid_amount, i.due_amount,
                   g.name AS guest_name, g.email AS guest_email, g.phone AS guest_phone,
                   b.room_no, b.check_in_date, b.check_out_date, b.payment_status,
                   (SELECT method FROM payments WHERE booking_id = b.id ORDER BY payment_date DESC LIMIT 1) AS payment_method
            FROM invoices i
            JOIN guests g ON i.guest_id = g.id
            JOIN bookings b ON i.booking_id = b.id
            ORDER BY i.invoice_date DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Invoice invoice = mapResultSetToInvoice(rs);
                invoices.add(invoice);
            }
        }
        return invoices;
    }

    public List<Invoice> getFilteredInvoices(LocalDate dateFrom, LocalDate dateTo, 
                                           String paymentMethod, String status) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT i.id, i.booking_id, i.guest_id, i.invoice_number, i.invoice_date,
                   i.subtotal, i.gst, i.total, i.paid_amount, i.due_amount,
                   g.name AS guest_name, g.email AS guest_email, g.phone AS guest_phone,
                   b.room_no, b.check_in_date, b.check_out_date, b.payment_status,
                   (SELECT method FROM payments WHERE booking_id = b.id ORDER BY payment_date DESC LIMIT 1) AS payment_method
            FROM invoices i
            JOIN guests g ON i.guest_id = g.id
            JOIN bookings b ON i.booking_id = b.id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (dateFrom != null) {
            sql.append(" AND i.invoice_date >= ?");
            params.add(Date.valueOf(dateFrom));
        }

        if (dateTo != null) {
            sql.append(" AND i.invoice_date <= ?");
            params.add(Date.valueOf(dateTo));
        }

        if (paymentMethod != null && !"All".equals(paymentMethod)) {
            sql.append(" AND EXISTS (SELECT 1 FROM payments WHERE booking_id = b.id AND method = ?)");
            params.add(paymentMethod);
        }

        if (status != null && !"All".equals(status)) {
            if ("Paid".equals(status)) {
                sql.append(" AND (i.due_amount IS NULL OR i.due_amount <= 0)");
            } else if ("Unpaid".equals(status)) {
                sql.append(" AND (i.paid_amount IS NULL OR i.paid_amount <= 0)");
            } else if ("Partial".equals(status)) {
                sql.append(" AND i.paid_amount > 0 AND i.due_amount > 0");
            }
        }

        sql.append(" ORDER BY i.invoice_date DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    invoices.add(invoice);
                }
            }
        }
        return invoices;
    }

    public Invoice getInvoiceByIdWithDetails(Long id) throws SQLException {
        String sql = """
            SELECT i.id, i.booking_id, i.guest_id, i.invoice_number, i.invoice_date,
                   i.subtotal, i.gst, i.total, i.paid_amount, i.due_amount,
                   g.name AS guest_name, g.email AS guest_email, g.phone AS guest_phone, g.address,
                   b.room_no, b.check_in_date, b.check_out_date, b.payment_status,
                   r.room_type, r.price AS room_rate,
                   (SELECT method FROM payments WHERE booking_id = b.id ORDER BY payment_date DESC LIMIT 1) AS payment_method
            FROM invoices i
            JOIN guests g ON i.guest_id = g.id
            JOIN bookings b ON i.booking_id = b.id
            JOIN rooms r ON b.room_no = r.room_no
            WHERE i.id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        }
        return null;
    }

    public void deleteInvoice(Long id) throws SQLException {
        String sql = "DELETE FROM invoices WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getLong("id"));
        invoice.setBookingId(rs.getLong("booking_id"));
        invoice.setGuestId(rs.getLong("guest_id"));
        invoice.setInvoiceNumber(rs.getString("invoice_number"));
        
        Date invoiceDate = rs.getDate("invoice_date");
        if (invoiceDate != null) {
            invoice.setInvoiceDate(invoiceDate.toLocalDate());
        }
        
        invoice.setSubtotal(rs.getBigDecimal("subtotal"));
        invoice.setGst(rs.getBigDecimal("gst"));
        invoice.setTotal(rs.getBigDecimal("total"));
        invoice.setPaidAmount(rs.getBigDecimal("paid_amount"));
        invoice.setDueAmount(rs.getBigDecimal("due_amount"));
        
        // Additional display fields
        invoice.setGuestName(rs.getString("guest_name"));
        invoice.setGuestEmail(rs.getString("guest_email"));
        invoice.setGuestPhone(rs.getString("guest_phone"));
        invoice.setRoomNo(rs.getString("room_no"));
        
        Date checkIn = rs.getDate("check_in_date");
        if (checkIn != null) {
            invoice.setCheckInDate(checkIn.toLocalDate());
        }
        
        Date checkOut = rs.getDate("check_out_date");
        if (checkOut != null) {
            invoice.setCheckOutDate(checkOut.toLocalDate());
        }
        
        invoice.setPaymentStatus(rs.getString("payment_status"));
        invoice.setPaymentMethod(rs.getString("payment_method"));
        
        return invoice;
    }
    public boolean saveFullInvoiceDetails(Invoice invoice) throws SQLException {
        String updateInvoiceSql = "UPDATE invoices SET GUEST_ID = ?, INVOICE_DATE = ?, SUBTOTAL = ?, GST = ?, TOTAL = ? WHERE INVOICE_NUMBER = ?";

        String updateGuestSql = "UPDATE guests SET NAME = ?, EMAIL = ?, PHONE = ? WHERE ID = ?";

        String updateBookingSql = "UPDATE bookings SET ROOM_NO = ?, CHECK_IN_DATE = ?, CHECK_OUT_DATE = ?, PAYMENT_STATUS = ? WHERE ID = ?";

        // Assuming you want to update payment method in payments table by booking_id
        String updatePaymentSql = "UPDATE payments SET METHOD = ? WHERE BOOKING_ID = ?";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Update invoices table
            try (PreparedStatement psInvoice = conn.prepareStatement(updateInvoiceSql)) {
                psInvoice.setLong(1, invoice.getGuestId());
                psInvoice.setDate(2, invoice.getInvoiceDate() != null ? java.sql.Date.valueOf(invoice.getInvoiceDate()) : null);
                psInvoice.setBigDecimal(3, invoice.getSubtotal());
                psInvoice.setBigDecimal(4, invoice.getGst());
                psInvoice.setBigDecimal(5, invoice.getTotal());
                psInvoice.setString(6, invoice.getInvoiceNumber());
                psInvoice.executeUpdate();
            }

            // Update guests table
            try (PreparedStatement psGuest = conn.prepareStatement(updateGuestSql)) {
                psGuest.setString(1, invoice.getGuestName());
                psGuest.setString(2, invoice.getGuestEmail());
                psGuest.setString(3, invoice.getGuestPhone());
                psGuest.setLong(4, invoice.getGuestId());
                psGuest.executeUpdate();
            }

            // Update bookings table
            try (PreparedStatement psBooking = conn.prepareStatement(updateBookingSql)) {
                psBooking.setString(1, invoice.getRoomNo());
                psBooking.setDate(2, invoice.getCheckInDate() != null ? java.sql.Date.valueOf(invoice.getCheckInDate()) : null);
                psBooking.setDate(3, invoice.getCheckOutDate() != null ? java.sql.Date.valueOf(invoice.getCheckOutDate()) : null);
                psBooking.setString(4, invoice.getPaymentStatus());
                psBooking.setLong(5, invoice.getBookingId());
                psBooking.executeUpdate();
            }

            // Update payments table - optional, based on booking id
            try (PreparedStatement psPayment = conn.prepareStatement(updatePaymentSql)) {
                psPayment.setString(1, invoice.getPaymentMethod());
                psPayment.setLong(2, invoice.getBookingId());
                psPayment.executeUpdate();
            }

            conn.commit();  // Commit transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset autocommit
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
