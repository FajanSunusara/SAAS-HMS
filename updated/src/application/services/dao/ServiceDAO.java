package application.services.dao;

import application.models.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {
    
    public void addService(Service service) throws SQLException {
        String sql = "INSERT INTO services (name, type, description, price, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, service.getName());
            pstmt.setString(2, service.getType());
            pstmt.setString(3, service.getDescription());
            pstmt.setBigDecimal(4, service.getPrice());
            pstmt.setBoolean(5, service.getActive() != null ? service.getActive() : true);
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    service.setId(generatedKeys.getLong(1));
                }
            }
        }
    }
    
    public Service getServiceById(Long id) throws SQLException {
        String sql = "SELECT * FROM services WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToService(rs);
                }
            }
        }
        return null;
    }
    
    public List<Service> getAllServices() throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services ORDER BY name";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        }
        return services;
    }
    
    public void updateService(Service service) throws SQLException {
        String sql = "UPDATE services SET name = ?, type = ?, description = ?, price = ?, active = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, service.getName());
            pstmt.setString(2, service.getType());
            pstmt.setString(3, service.getDescription());
            pstmt.setBigDecimal(4, service.getPrice());
            pstmt.setBoolean(5, service.getActive() != null ? service.getActive() : true);
            pstmt.setLong(6, service.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    public void deleteService(Long id) throws SQLException {
        String sql = "DELETE FROM services WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }
    
    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setId(rs.getLong("id"));
        service.setName(rs.getString("name"));
        service.setType(rs.getString("type"));
        service.setDescription(rs.getString("description"));
        service.setPrice(rs.getBigDecimal("price"));
        service.setActive(rs.getBoolean("active"));
        // Set taxable for backward compatibility
        service.setTaxable(false);
        return service;
    }
}
