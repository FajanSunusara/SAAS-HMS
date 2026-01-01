package application.services.dao;

import application.models.ReferenceSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ReferenceSourceDAO {

    public void addReferenceSource(ReferenceSource source) throws SQLException {
        String sql = "INSERT INTO reference_sources (source_name, contact, commission_percentage, fixed_amount, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, source.getSourceName());
            pstmt.setString(2, source.getContact());
            pstmt.setBigDecimal(3, source.getCommissionPercentage());
            pstmt.setBigDecimal(4, source.getFixedAmount());
            pstmt.setString(5, source.getNotes());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    source.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public List<ReferenceSource> getAllReferenceSources() throws SQLException {
        List<ReferenceSource> sources = new ArrayList<>();
        String sql = "SELECT * FROM reference_sources ORDER BY source_name";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sources.add(mapResultSetToReferenceSource(rs));
            }
        }
        return sources;
    }

    public void deleteReferenceSource(Long id) throws SQLException {
        String sql = "DELETE FROM reference_sources WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    private ReferenceSource mapResultSetToReferenceSource(ResultSet rs) throws SQLException {
        ReferenceSource source = new ReferenceSource();
        source.setId(rs.getLong("id"));
        source.setSourceName(rs.getString("source_name"));
        source.setContact(rs.getString("contact"));
        source.setCommissionPercentage(rs.getBigDecimal("commission_percentage"));
        source.setFixedAmount(rs.getBigDecimal("fixed_amount"));
        source.setNotes(rs.getString("notes"));
        return source;
    }
}
