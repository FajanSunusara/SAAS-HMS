package application.services.dao.inventory;



import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import application.models.inventory.PurchaseOrder;
import application.services.dao.DatabaseManager;

public class PurchaseOrderDAO {
    
    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public List<PurchaseOrder> getAllPurchaseOrders() throws SQLException {
        List<PurchaseOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                orders.add(mapResultSetToPurchaseOrder(rs));
            }
        }
        return orders;
    }

    public List<PurchaseOrder> getRecentPurchaseOrders() throws SQLException {
        return getRecentPurchaseOrders(50);
    }

    public List<PurchaseOrder> getRecentPurchaseOrders(int limit) throws SQLException {
        List<PurchaseOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders ORDER BY created_at DESC LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToPurchaseOrder(rs));
            }
        }
        return orders;
    }

    public List<PurchaseOrder> getPurchaseOrdersByStatus(String status) throws SQLException {
        List<PurchaseOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders WHERE status = ? ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToPurchaseOrder(rs));
            }
        }
        return orders;
    }

    public List<PurchaseOrder> getPurchaseOrdersByVendor(int vendorId) throws SQLException {
        List<PurchaseOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders WHERE vendor_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vendorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToPurchaseOrder(rs));
            }
        }
        return orders;
    }

    public PurchaseOrder getPurchaseOrderById(int poId) throws SQLException {
        String sql = "SELECT * FROM purchase_orders WHERE po_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, poId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPurchaseOrder(rs);
            }
        }
        return null;
    }

    public PurchaseOrder getPurchaseOrderByNumber(String poNumber) throws SQLException {
        String sql = "SELECT * FROM purchase_orders WHERE po_number = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, poNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPurchaseOrder(rs);
            }
        }
        return null;
    }

    public int savePurchaseOrder(PurchaseOrder order) throws SQLException {
        if (order.getPoId() == 0) {
            return insertPurchaseOrder(order);
        } else {
            updatePurchaseOrder(order);
            return order.getPoId();
        }
    }

    private int insertPurchaseOrder(PurchaseOrder order) throws SQLException {
        String sql = """
            INSERT INTO purchase_orders (po_number, vendor_id, vendor_name, vendor_contact, 
                                       order_date, expected_date, delivery_date, total_items, 
                                       total_amount, tax_amount, final_amount, status, 
                                       payment_terms, shipping_address, notes, created_by, 
                                       approved_by, received_by, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Generate PO number if not provided
            if (order.getPoNumber() == null || order.getPoNumber().isEmpty()) {
                order.setPoNumber(generatePONumber());
            }
            
            setPurchaseOrderParameters(stmt, order);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating purchase order failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int poId = generatedKeys.getInt(1);
                    order.setPoId(poId);
                    return poId;
                } else {
                    throw new SQLException("Creating purchase order failed, no ID obtained.");
                }
            }
        }
    }

    private void updatePurchaseOrder(PurchaseOrder order) throws SQLException {
        String sql = """
            UPDATE purchase_orders 
            SET po_number = ?, vendor_id = ?, vendor_name = ?, vendor_contact = ?, 
                order_date = ?, expected_date = ?, delivery_date = ?, total_items = ?, 
                total_amount = ?, tax_amount = ?, final_amount = ?, status = ?, 
                payment_terms = ?, shipping_address = ?, notes = ?, created_by = ?, 
                approved_by = ?, received_by = ?, updated_at = ?
            WHERE po_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setPurchaseOrderParameters(stmt, order);
            stmt.setInt(20, order.getPoId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating purchase order failed, no rows affected.");
            }
        }
    }

    private void setPurchaseOrderParameters(PreparedStatement stmt, PurchaseOrder order) throws SQLException {
        stmt.setString(1, order.getPoNumber());
        stmt.setInt(2, order.getVendorId());
        stmt.setString(3, order.getVendorName());
        stmt.setString(4, order.getVendorContact());
        stmt.setDate(5, order.getOrderDate() != null ? Date.valueOf(order.getOrderDate()) : Date.valueOf(LocalDate.now()));
        stmt.setDate(6, order.getExpectedDate() != null ? Date.valueOf(order.getExpectedDate()) : null);
        stmt.setDate(7, order.getDeliveryDate() != null ? Date.valueOf(order.getDeliveryDate()) : null);
        stmt.setInt(8, order.getTotalItems());
        stmt.setBigDecimal(9, order.getTotalAmount());
        stmt.setBigDecimal(10, order.getTaxAmount());
        stmt.setBigDecimal(11, order.getFinalAmount());
        stmt.setString(12, order.getStatus());
        stmt.setString(13, order.getPaymentTerms());
        stmt.setString(14, order.getShippingAddress());
        stmt.setString(15, order.getNotes());
        stmt.setString(16, order.getCreatedBy());
        stmt.setString(17, order.getApprovedBy());
        stmt.setString(18, order.getReceivedBy());
        stmt.setTimestamp(19, order.getUpdatedAt() != null ? Timestamp.valueOf(order.getUpdatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
    }

    public void updatePurchaseOrderStatus(int poId, String status, String updatedBy) throws SQLException {
        String sql = "UPDATE purchase_orders SET status = ?, updated_at = ? WHERE po_id = ?";
        String updateField = "";
        
        // Set specific fields based on status
        switch (status) {
            case "Ordered":
                updateField = ", approved_by = '" + updatedBy + "'";
                break;
            case "Received":
                updateField = ", received_by = '" + updatedBy + "', delivery_date = CURRENT_DATE";
                break;
        }
        
        sql = sql.replace("SET status = ?", "SET status = ?" + updateField);
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, poId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating purchase order status failed, no rows affected.");
            }
        }
    }

    public void deletePurchaseOrder(int poId) throws SQLException {
        // First delete purchase order items
        String deleteItemsSql = "DELETE FROM purchase_order_items WHERE po_id = ?";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(deleteItemsSql)) {
                stmt.setInt(1, poId);
                stmt.executeUpdate();
            }
            
            // Then delete purchase order
            String deleteOrderSql = "DELETE FROM purchase_orders WHERE po_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderSql)) {
                stmt.setInt(1, poId);
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Deleting purchase order failed, no rows affected.");
                }
            }
            
            conn.commit();
        } catch (SQLException e) {
            throw new SQLException("Failed to delete purchase order: " + e.getMessage());
        }
    }

    public List<PurchaseOrder> getPendingPurchaseOrders() throws SQLException {
        return getPurchaseOrdersByStatus("Ordered");
    }

    public List<PurchaseOrder> getOverduePurchaseOrders() throws SQLException {
        List<PurchaseOrder> orders = new ArrayList<>();
        String sql = """
            SELECT * FROM purchase_orders 
            WHERE status IN ('Ordered', 'Partial') 
              AND expected_date < CURRENT_DATE 
            ORDER BY expected_date ASC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                orders.add(mapResultSetToPurchaseOrder(rs));
            }
        }
        return orders;
    }

    private String generatePONumber() throws SQLException {
        String sql = "SELECT COUNT(*) + 1 as next_num FROM purchase_orders WHERE YEAR(order_date) = YEAR(CURRENT_DATE)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int nextNum = rs.getInt("next_num");
                return String.format("PO%d%04d", java.time.LocalDate.now().getYear(), nextNum);
            }
        }
        return "PO" + System.currentTimeMillis();
    }

    private PurchaseOrder mapResultSetToPurchaseOrder(ResultSet rs) throws SQLException {
        PurchaseOrder order = new PurchaseOrder();
        
        order.setPoId(rs.getInt("po_id"));
        order.setPoNumber(rs.getString("po_number"));
        order.setVendorId(rs.getInt("vendor_id"));
        order.setVendorName(rs.getString("vendor_name"));
        order.setVendorContact(rs.getString("vendor_contact"));
        
        Date orderDate = rs.getDate("order_date");
        if (orderDate != null) {
            order.setOrderDate(orderDate.toLocalDate());
        }
        
        Date expectedDate = rs.getDate("expected_date");
        if (expectedDate != null) {
            order.setExpectedDate(expectedDate.toLocalDate());
        }
        
        Date deliveryDate = rs.getDate("delivery_date");
        if (deliveryDate != null) {
            order.setDeliveryDate(deliveryDate.toLocalDate());
        }
        
        order.setTotalItems(rs.getInt("total_items"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setTaxAmount(rs.getBigDecimal("tax_amount"));
        order.setFinalAmount(rs.getBigDecimal("final_amount"));
        order.setStatus(rs.getString("status"));
        order.setPaymentTerms(rs.getString("payment_terms"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setNotes(rs.getString("notes"));
        order.setCreatedBy(rs.getString("created_by"));
        order.setApprovedBy(rs.getString("approved_by"));
        order.setReceivedBy(rs.getString("received_by"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            order.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return order;
    }
}
