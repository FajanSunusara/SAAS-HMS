package application.services.dao.inventory;



import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import application.models.inventory.InventoryItem;
import application.services.dao.DatabaseManager;

public class InventoryDAO {
    
    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public List<InventoryItem> getAllItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items WHERE active = TRUE ORDER BY item_name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    public InventoryItem getItemById(int itemId) throws SQLException {
        String sql = "SELECT * FROM inventory_items WHERE item_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToItem(rs);
            }
        }
        return null;
    }

    public List<InventoryItem> getItemsByCategory(String category) throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items WHERE category = ? AND active = TRUE ORDER BY item_name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    public List<InventoryItem> getLowStockItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items WHERE available_quantity <= min_stock_level AND active = TRUE ORDER BY item_name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    public int saveItem(InventoryItem item) throws SQLException {
        if (item.getItemId() == 0) {
            return insertItem(item);
        } else {
            updateItem(item);
            return item.getItemId();
        }
    }

    private int insertItem(InventoryItem item) throws SQLException {
        String sql = """
            INSERT INTO inventory_items (item_name, category, location, description, 
                                       available_quantity, reserved_quantity, min_stock_level, 
                                       max_stock_level, unit_price, unit, supplier, 
                                       supplier_contact, last_updated, updated_by, active) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setItemParameters(stmt, item);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating item failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int itemId = generatedKeys.getInt(1);
                    item.setItemId(itemId);
                    return itemId;
                } else {
                    throw new SQLException("Creating item failed, no ID obtained.");
                }
            }
        }
    }

    private void updateItem(InventoryItem item) throws SQLException {
        String sql = """
            UPDATE inventory_items SET item_name = ?, category = ?, location = ?, description = ?, 
                                     available_quantity = ?, reserved_quantity = ?, min_stock_level = ?, 
                                     max_stock_level = ?, unit_price = ?, unit = ?, supplier = ?, 
                                     supplier_contact = ?, last_updated = ?, updated_by = ?, active = ?
            WHERE item_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setItemParameters(stmt, item);
            stmt.setInt(16, item.getItemId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating item failed, no rows affected.");
            }
        }
    }

    private void setItemParameters(PreparedStatement stmt, InventoryItem item) throws SQLException {
        stmt.setString(1, item.getItemName());
        stmt.setString(2, item.getCategory());
        stmt.setString(3, item.getLocation());
        stmt.setString(4, item.getDescription());
        stmt.setInt(5, item.getAvailableQuantity());
        stmt.setInt(6, item.getReservedQuantity());
        stmt.setInt(7, item.getMinStockLevel());
        stmt.setInt(8, item.getMaxStockLevel());
        stmt.setBigDecimal(9, item.getUnitPrice());
        stmt.setString(10, item.getUnit());
        stmt.setString(11, item.getSupplier());
        stmt.setString(12, item.getSupplierContact());
        stmt.setTimestamp(13, item.getLastUpdated() != null ? Timestamp.valueOf(item.getLastUpdated()) : Timestamp.valueOf(LocalDateTime.now()));
        stmt.setString(14, item.getUpdatedBy());
        stmt.setBoolean(15, item.isActive());
    }

    public void deleteItem(int itemId) throws SQLException {
        // Soft delete - mark as inactive
        String sql = "UPDATE inventory_items SET active = FALSE, last_updated = ? WHERE item_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, itemId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting item failed, no rows affected.");
            }
        }
    }

    public void adjustQuantity(int itemId, int quantityChange, String reason, String updatedBy) throws SQLException {
        String sql = """
            UPDATE inventory_items 
            SET available_quantity = available_quantity + ?, last_updated = ?, updated_by = ? 
            WHERE item_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantityChange);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, updatedBy);
            stmt.setInt(4, itemId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Quantity adjustment failed, no rows affected.");
            }
        }
    }

    public List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM inventory_items WHERE active = TRUE ORDER BY category";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }
        return categories;
    }

    public List<String> getAllLocations() throws SQLException {
        List<String> locations = new ArrayList<>();
        String sql = "SELECT DISTINCT location FROM inventory_items WHERE active = TRUE ORDER BY location";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                locations.add(rs.getString("location"));
            }
        }
        return locations;
    }

    private InventoryItem mapResultSetToItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        
        item.setItemId(rs.getInt("item_id"));
        item.setItemName(rs.getString("item_name"));
        item.setCategory(rs.getString("category"));
        item.setLocation(rs.getString("location"));
        item.setDescription(rs.getString("description"));
        item.setAvailableQuantity(rs.getInt("available_quantity"));
        item.setReservedQuantity(rs.getInt("reserved_quantity"));
        item.setMinStockLevel(rs.getInt("min_stock_level"));
        item.setMaxStockLevel(rs.getInt("max_stock_level"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setUnit(rs.getString("unit"));
        item.setSupplier(rs.getString("supplier"));
        item.setSupplierContact(rs.getString("supplier_contact"));
        
        Timestamp lastUpdated = rs.getTimestamp("last_updated");
        if (lastUpdated != null) {
            item.setLastUpdated(lastUpdated.toLocalDateTime());
        }
        
        item.setUpdatedBy(rs.getString("updated_by"));
        item.setActive(rs.getBoolean("active"));
        
        return item;
    }
}
