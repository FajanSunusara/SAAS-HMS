package application.services.dao;

import application.models.BookingDocument;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDocumentDAO {
    
    /**
     * Saves a booking document reference to database
     */
    public boolean saveBookingDocument(BookingDocument document) {
        String sql = "INSERT INTO booking_documents (booking_id, document_type, file_name, file_path, " +
                    "person_name, person_index) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, document.getBookingId());
            pstmt.setString(2, document.getDocumentType());
            pstmt.setString(3, document.getFileName());
            pstmt.setString(4, document.getFilePath());
            pstmt.setString(5, document.getPersonName());
            pstmt.setInt(6, document.getPersonIndex());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets all documents for a booking
     */
    public List<BookingDocument> getDocumentsByBookingId(Long bookingId) {
        List<BookingDocument> documents = new ArrayList<>();
        String sql = "SELECT * FROM booking_documents WHERE booking_id = ? ORDER BY uploaded_at DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                BookingDocument doc = new BookingDocument();
                doc.setId(rs.getLong("id"));
                doc.setBookingId(rs.getLong("booking_id"));
                doc.setDocumentType(rs.getString("document_type"));
                doc.setFileName(rs.getString("file_name"));
                doc.setFilePath(rs.getString("file_path"));
                doc.setPersonName(rs.getString("person_name"));
                doc.setPersonIndex(rs.getInt("person_index"));
                doc.setUploadedAt(rs.getTimestamp("uploaded_at"));
                
                documents.add(doc);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return documents;
    }
    
    /**
     * Deletes a document from database and file system
     */
    public boolean deleteDocument(Long documentId) {
        // First get the file path
        String filePath = null;
        String sqlSelect = "SELECT file_path FROM booking_documents WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
            
            pstmt.setLong(1, documentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                filePath = rs.getString("file_path");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Delete from database
        String sqlDelete = "DELETE FROM booking_documents WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
            
            pstmt.setLong(1, documentId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0 && filePath != null) {
                // Delete physical file
                java.io.File file = new java.io.File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}