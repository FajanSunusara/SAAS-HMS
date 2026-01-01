package application.services.dao;

import application.models.ServiceUsed;
import application.models.ServiceItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUsedDAO {

    public Long addServiceUsed(ServiceUsed serviceUsed) throws SQLException {
        String sql = "INSERT INTO service_used (booking_id, service_date, service, category, total_qty, total_amount) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, serviceUsed.getBookingId());
            pstmt.setDate(2, serviceUsed.getServiceDate());
            pstmt.setString(3, serviceUsed.getService());
            pstmt.setString(4, serviceUsed.getCategory());
            pstmt.setInt(5, serviceUsed.getTotalQty());
            pstmt.setBigDecimal(6, serviceUsed.getTotalAmount());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating service used failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long serviceUsedId = generatedKeys.getLong(1);
                    serviceUsed.setId(serviceUsedId);
                    return serviceUsedId;
                } else {
                    throw new SQLException("Creating service used failed, no ID obtained.");
                }
            }
        }
    }

    public ServiceUsed getServiceUsedById(Long id) throws SQLException {
        String sql = "SELECT * FROM service_used WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ServiceUsed serviceUsed = mapResultSetToServiceUsed(rs);
                    // Load service items
                    ServiceItemDAO itemDAO = new ServiceItemDAO();
                    serviceUsed.setServiceItems(itemDAO.getServiceItemsByServiceUsedId(id));
                    return serviceUsed;
                }
            }
        }
        return null;
    }

    public List<ServiceUsed> getServiceUsedByBookingId(Long bookingId) throws SQLException {
        List<ServiceUsed> servicesUsed = new ArrayList<>();
        String sql = "SELECT * FROM service_used WHERE booking_id = ? ORDER BY service_date DESC, id DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, bookingId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                ServiceItemDAO itemDAO = new ServiceItemDAO();
                while (rs.next()) {
                    ServiceUsed serviceUsed = mapResultSetToServiceUsed(rs);
                    serviceUsed.setServiceItems(itemDAO.getServiceItemsByServiceUsedId(serviceUsed.getId()));
                    servicesUsed.add(serviceUsed);
                }
            }
        }
        return servicesUsed;
    }

    public void updateServiceUsedTotals(Long serviceUsedId) throws SQLException {
        String sql = "UPDATE service_used SET " +
                     "total_qty = (SELECT COALESCE(SUM(quantity), 0) FROM service_items WHERE service_used_id = ?), " +
                     "total_amount = (SELECT COALESCE(SUM(amount), 0) FROM service_items WHERE service_used_id = ?) " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, serviceUsedId);
            pstmt.setLong(2, serviceUsedId);
            pstmt.setLong(3, serviceUsedId);
            
            pstmt.executeUpdate();
        }
    }

    public void deleteServiceUsed(Long id) throws SQLException {
        String sql = "DELETE FROM service_used WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Deleting service used failed, no rows affected.");
            }
        }
    }

    public double getTotalServiceAmountByBookingId(Long bookingId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM service_used WHERE booking_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, bookingId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    private ServiceUsed mapResultSetToServiceUsed(ResultSet rs) throws SQLException {
        ServiceUsed serviceUsed = new ServiceUsed();
        serviceUsed.setId(rs.getLong("id"));
        serviceUsed.setBookingId(rs.getLong("booking_id"));
        serviceUsed.setServiceDate(rs.getDate("service_date"));
        serviceUsed.setService(rs.getString("service"));
        serviceUsed.setCategory(rs.getString("category"));
        serviceUsed.setTotalQty(rs.getInt("total_qty"));
        serviceUsed.setTotalAmount(rs.getBigDecimal("total_amount"));
        serviceUsed.setCreatedAt(rs.getTimestamp("created_at"));
        return serviceUsed;
    }

    public void ensureTableExists() throws SQLException {
        String createServiceUsedTable = """
            CREATE TABLE IF NOT EXISTS service_used (
                id IDENTITY PRIMARY KEY,
                booking_id BIGINT NOT NULL,
                service_date DATE NOT NULL,
                service VARCHAR(255) NOT NULL,
                category VARCHAR(255) NOT NULL,
                total_qty INT NOT NULL DEFAULT 0,
                total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (booking_id) REFERENCES bookings(id)
            )
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createServiceUsedTable);
        }
    }
    
   
   
}
