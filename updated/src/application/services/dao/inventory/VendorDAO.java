package application.services.dao.inventory;





import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import application.models.inventory.Vendor;
import application.services.dao.DatabaseManager;

public class VendorDAO {
    
    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public List<Vendor> getAllVendors() throws SQLException {
        List<Vendor> vendors = new ArrayList<>();
        String sql = "SELECT * FROM vendors WHERE active = TRUE ORDER BY vendor_name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                vendors.add(mapResultSetToVendor(rs));
            }
        }
        return vendors;
    }

    public Vendor getVendorById(int vendorId) throws SQLException {
        String sql = "SELECT * FROM vendors WHERE vendor_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vendorId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToVendor(rs);
            }
        }
        return null;
    }

    public Vendor getVendorByName(String vendorName) throws SQLException {
        String sql = "SELECT * FROM vendors WHERE vendor_name = ? AND active = TRUE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, vendorName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToVendor(rs);
            }
        }
        return null;
    }

    public int saveVendor(Vendor vendor) throws SQLException {
        if (vendor.getVendorId() == 0) {
            return insertVendor(vendor);
        } else {
            updateVendor(vendor);
            return vendor.getVendorId();
        }
    }

    private int insertVendor(Vendor vendor) throws SQLException {
        String sql = """
            INSERT INTO vendors (vendor_name, contact_person, email, phone, address, 
                               gst_number, payment_terms, credit_limit, active, 
                               created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setVendorParameters(stmt, vendor);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating vendor failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int vendorId = generatedKeys.getInt(1);
                    vendor.setVendorId(vendorId);
                    return vendorId;
                } else {
                    throw new SQLException("Creating vendor failed, no ID obtained.");
                }
            }
        }
    }

    private void updateVendor(Vendor vendor) throws SQLException {
        String sql = """
            UPDATE vendors 
            SET vendor_name = ?, contact_person = ?, email = ?, phone = ?, address = ?, 
                gst_number = ?, payment_terms = ?, credit_limit = ?, active = ?, 
                updated_at = ?
            WHERE vendor_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setVendorParameters(stmt, vendor);
            stmt.setInt(11, vendor.getVendorId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating vendor failed, no rows affected.");
            }
        }
    }

    private void setVendorParameters(PreparedStatement stmt, Vendor vendor) throws SQLException {
        stmt.setString(1, vendor.getVendorName());
        stmt.setString(2, vendor.getContactPerson());
        stmt.setString(3, vendor.getEmail());
        stmt.setString(4, vendor.getPhone());
        stmt.setString(5, vendor.getAddress());
        stmt.setString(6, vendor.getGstNumber());
        stmt.setString(7, vendor.getPaymentTerms());
        stmt.setBigDecimal(8, vendor.getCreditLimit());
        stmt.setBoolean(9, vendor.isActive());
        stmt.setTimestamp(10, vendor.getUpdatedAt() != null ? Timestamp.valueOf(vendor.getUpdatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
    }

    public void deleteVendor(int vendorId) throws SQLException {
        // Soft delete - mark as inactive
        String sql = "UPDATE vendors SET active = FALSE, updated_at = ? WHERE vendor_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, vendorId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting vendor failed, no rows affected.");
            }
        }
    }

    private Vendor mapResultSetToVendor(ResultSet rs) throws SQLException {
        Vendor vendor = new Vendor();
        
        vendor.setVendorId(rs.getInt("vendor_id"));
        vendor.setVendorName(rs.getString("vendor_name"));
        vendor.setContactPerson(rs.getString("contact_person"));
        vendor.setEmail(rs.getString("email"));
        vendor.setPhone(rs.getString("phone"));
        vendor.setAddress(rs.getString("address"));
        vendor.setGstNumber(rs.getString("gst_number"));
        vendor.setPaymentTerms(rs.getString("payment_terms"));
        vendor.setCreditLimit(rs.getBigDecimal("credit_limit"));
        vendor.setActive(rs.getBoolean("active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            vendor.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            vendor.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return vendor;
    }
}
