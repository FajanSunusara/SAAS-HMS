package application.services.dao.inventory;



import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import application.models.inventory.InventoryTransaction;
import application.services.dao.DatabaseManager;

public class InventoryTransactionDAO {
    
    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public List<InventoryTransaction> getAllTransactions() throws SQLException {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = """
            SELECT it.*, ii.item_name 
            FROM inventory_transactions it 
            LEFT JOIN inventory_items ii ON it.item_id = ii.item_id 
            ORDER BY it.transaction_date DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public List<InventoryTransaction> getRecentTransactions(int limit) throws SQLException {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = """
            SELECT it.*, ii.item_name 
            FROM inventory_transactions it 
            LEFT JOIN inventory_items ii ON it.item_id = ii.item_id 
            ORDER BY it.transaction_date DESC 
            LIMIT ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public List<InventoryTransaction> getTransactionsByItem(int itemId) throws SQLException {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = """
            SELECT it.*, ii.item_name 
            FROM inventory_transactions it 
            LEFT JOIN inventory_items ii ON it.item_id = ii.item_id 
            WHERE it.item_id = ? 
            ORDER BY it.transaction_date DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public List<InventoryTransaction> getTransactionsByType(String transactionType) throws SQLException {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = """
            SELECT it.*, ii.item_name 
            FROM inventory_transactions it 
            LEFT JOIN inventory_items ii ON it.item_id = ii.item_id 
            WHERE it.transaction_type = ? 
            ORDER BY it.transaction_date DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionType);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public List<InventoryTransaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = """
            SELECT it.*, ii.item_name 
            FROM inventory_transactions it 
            LEFT JOIN inventory_items ii ON it.item_id = ii.item_id 
            WHERE it.transaction_date BETWEEN ? AND ? 
            ORDER BY it.transaction_date DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    public int saveTransaction(InventoryTransaction transaction) throws SQLException {
        String sql = """
            INSERT INTO inventory_transactions (trans_number, item_id, transaction_type, quantity, 
                                              unit_price, total_value, location, reference_number, 
                                              reference_type, reference_id, transaction_date, 
                                              handled_by, notes, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Generate transaction number if not provided
            if (transaction.getTransNumber() == null || transaction.getTransNumber().isEmpty()) {
                transaction.setTransNumber(generateTransactionNumber());
            }
            
            stmt.setString(1, transaction.getTransNumber());
            stmt.setInt(2, transaction.getItemId());
            stmt.setString(3, transaction.getTransactionType());
            stmt.setInt(4, transaction.getQuantity());
            stmt.setBigDecimal(5, transaction.getUnitPrice());
            stmt.setBigDecimal(6, transaction.getTotalValue());
            stmt.setString(7, transaction.getLocation());
            stmt.setString(8, transaction.getReferenceNumber());
            stmt.setString(9, transaction.getReferenceType());
            stmt.setObject(10, transaction.getReferenceId() > 0 ? transaction.getReferenceId() : null);
            stmt.setTimestamp(11, Timestamp.valueOf(transaction.getTransactionDate()));
            stmt.setString(12, transaction.getHandledBy());
            stmt.setString(13, transaction.getNotes());
            stmt.setString(14, transaction.getStatus());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int transactionId = generatedKeys.getInt(1);
                    transaction.setTransactionId(transactionId);
                    return transactionId;
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        }
    }

    public void updateTransaction(InventoryTransaction transaction) throws SQLException {
        String sql = """
            UPDATE inventory_transactions 
            SET trans_number = ?, transaction_type = ?, quantity = ?, unit_price = ?, 
                total_value = ?, location = ?, reference_number = ?, reference_type = ?, 
                reference_id = ?, handled_by = ?, notes = ?, status = ?
            WHERE transaction_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transaction.getTransNumber());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setInt(3, transaction.getQuantity());
            stmt.setBigDecimal(4, transaction.getUnitPrice());
            stmt.setBigDecimal(5, transaction.getTotalValue());
            stmt.setString(6, transaction.getLocation());
            stmt.setString(7, transaction.getReferenceNumber());
            stmt.setString(8, transaction.getReferenceType());
            stmt.setObject(9, transaction.getReferenceId() > 0 ? transaction.getReferenceId() : null);
            stmt.setString(10, transaction.getHandledBy());
            stmt.setString(11, transaction.getNotes());
            stmt.setString(12, transaction.getStatus());
            stmt.setInt(13, transaction.getTransactionId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating transaction failed, no rows affected.");
            }
        }
    }

    public void deleteTransaction(int transactionId) throws SQLException {
        String sql = "DELETE FROM inventory_transactions WHERE transaction_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting transaction failed, no rows affected.");
            }
        }
    }

    public InventoryTransaction getTransactionById(int transactionId) throws SQLException {
        String sql = """
            SELECT it.*, ii.item_name 
            FROM inventory_transactions it 
            LEFT JOIN inventory_items ii ON it.item_id = ii.item_id 
            WHERE it.transaction_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTransaction(rs);
            }
        }
        return null;
    }

    public BigDecimal getTotalValueByType(String transactionType, LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(total_value), 0) as total 
            FROM inventory_transactions 
            WHERE transaction_type = ? AND transaction_date BETWEEN ? AND ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionType);
            stmt.setTimestamp(2, Timestamp.valueOf(startDate));
            stmt.setTimestamp(3, Timestamp.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }

    private String generateTransactionNumber() throws SQLException {
        String sql = "SELECT COUNT(*) + 1 as next_num FROM inventory_transactions WHERE DATE(transaction_date) = CURRENT_DATE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int nextNum = rs.getInt("next_num");
                return String.format("TXN%s%04d", 
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")), 
                    nextNum);
            }
        }
        return "TXN" + System.currentTimeMillis();
    }

    private InventoryTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        InventoryTransaction transaction = new InventoryTransaction();
        
        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setTransNumber(rs.getString("trans_number"));
        transaction.setItemId(rs.getInt("item_id"));
        transaction.setItemName(rs.getString("item_name"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setQuantity(rs.getInt("quantity"));
        transaction.setUnitPrice(rs.getBigDecimal("unit_price"));
        transaction.setTotalValue(rs.getBigDecimal("total_value"));
        transaction.setLocation(rs.getString("location"));
        transaction.setReferenceNumber(rs.getString("reference_number"));
        transaction.setReferenceType(rs.getString("reference_type"));
        transaction.setReferenceId(rs.getInt("reference_id"));
        
        Timestamp transactionDate = rs.getTimestamp("transaction_date");
        if (transactionDate != null) {
            transaction.setTransactionDate(transactionDate.toLocalDateTime());
        }
        
        transaction.setHandledBy(rs.getString("handled_by"));
        transaction.setNotes(rs.getString("notes"));
        transaction.setStatus(rs.getString("status"));
        
        return transaction;
    }
}
