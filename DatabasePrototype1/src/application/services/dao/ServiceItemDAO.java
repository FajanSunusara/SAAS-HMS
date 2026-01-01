package application.services.dao;

import application.models.ServiceItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceItemDAO {

    public void addServiceItem(ServiceItem serviceItem) throws SQLException {
        String sql = "INSERT INTO service_items (service_used_id, item_name, quantity, price, amount) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, serviceItem.getServiceUsedId());
            pstmt.setString(2, serviceItem.getItemName());
            pstmt.setInt(3, serviceItem.getQuantity());
            pstmt.setBigDecimal(4, serviceItem.getPrice());
            pstmt.setBigDecimal(5, serviceItem.getAmount());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating service item failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    serviceItem.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating service item failed, no ID obtained.");
                }
            }
        }
    }

    public List<ServiceItem> getServiceItemsByServiceUsedId(Long serviceUsedId) throws SQLException {
        List<ServiceItem> serviceItems = new ArrayList<>();
        String sql = "SELECT * FROM service_items WHERE service_used_id = ? ORDER BY id";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, serviceUsedId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    serviceItems.add(mapResultSetToServiceItem(rs));
                }
            }
        }
        return serviceItems;
    }

    public void deleteServiceItem(Long id) throws SQLException {
        String sql = "DELETE FROM service_items WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Deleting service item failed, no rows affected.");
            }
        }
    }

    private ServiceItem mapResultSetToServiceItem(ResultSet rs) throws SQLException {
        ServiceItem serviceItem = new ServiceItem();
        serviceItem.setId(rs.getLong("id"));
        serviceItem.setServiceUsedId(rs.getLong("service_used_id"));
        serviceItem.setItemName(rs.getString("item_name"));
        serviceItem.setQuantity(rs.getInt("quantity"));
        serviceItem.setPrice(rs.getBigDecimal("price"));
        serviceItem.setAmount(rs.getBigDecimal("amount"));
        return serviceItem;
    }

    public void ensureTableExists() throws SQLException {
        String createServiceItemsTable = """
            CREATE TABLE IF NOT EXISTS service_items (
                id IDENTITY PRIMARY KEY,
                service_used_id BIGINT NOT NULL,
                item_name VARCHAR(255) NOT NULL,
                quantity INT NOT NULL,
                price DECIMAL(10, 2) NOT NULL,
                amount DECIMAL(10, 2) NOT NULL,
                FOREIGN KEY (service_used_id) REFERENCES service_used(id) ON DELETE CASCADE
            )
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createServiceItemsTable);
        }
    }
    public List<ServiceItem> getServiceItemsByBookingId(Long bookingId) throws SQLException {
        List<ServiceItem> serviceItems = new ArrayList<>();
        String sql = "SELECT si.* FROM service_items si " +
                     "JOIN service_used su ON si.service_used_id = su.id " +
                     "WHERE su.booking_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    serviceItems.add(mapResultSetToServiceItem(rs));
                }
            }
        }
        return serviceItems;
    }

}
