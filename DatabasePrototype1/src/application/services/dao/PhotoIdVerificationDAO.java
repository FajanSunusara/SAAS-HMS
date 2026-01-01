package application.services.dao;

import application.models.PhotoIdVerification;
import application.models.VerifiedPerson;
import application.services.dao.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PhotoIdVerificationDAO {

    public Long saveVerification(PhotoIdVerification verification) throws SQLException {
        String sql = "INSERT INTO photo_id_verifications (booking_id, verification_date, total_persons, " +
                    "main_guest_photo_path, verified_by, status, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setLong(1, verification.getBookingId());
            ps.setTimestamp(2, Timestamp.valueOf(verification.getVerificationDate()));
            ps.setInt(3, verification.getTotalPersons());
            ps.setString(4, verification.getMainGuestPhotoPath());
            ps.setString(5, verification.getVerifiedBy());
            ps.setString(6, verification.getStatus());
            ps.setString(7, verification.getNotes());
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public void saveVerifiedPerson(VerifiedPerson person) throws SQLException {
        String sql = "INSERT INTO verified_persons (verification_id, person_name, person_index, " +
                    "id_type, id_number, id_photo_path, coming_from, proceeding_to, " +
                    "contact_number, relationship_with_guest) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, person.getVerificationId());
            ps.setString(2, person.getPersonName());
            ps.setInt(3, person.getPersonIndex());
            ps.setString(4, person.getIdType());
            ps.setString(5, person.getIdNumber());
            ps.setString(6, person.getIdPhotoPath());
            ps.setString(7, person.getComingFrom());
            ps.setString(8, person.getProceedingTo());
            ps.setString(9, person.getContactNumber());
            ps.setString(10, person.getRelationshipWithGuest());
            
            ps.executeUpdate();
        }
    }

    public Optional<PhotoIdVerification> findByBookingId(Long bookingId) throws SQLException {
        String sql = "SELECT * FROM photo_id_verifications WHERE booking_id = ? ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, bookingId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhotoIdVerification verification = mapResultSetToVerification(rs);
                    verification.setPersons(findPersonsByVerificationId(verification.getId()));
                    return Optional.of(verification);
                }
            }
        }
        return Optional.empty();
    }

    public void updateVerificationStatus(Long verificationId, String status, String notes) throws SQLException {
        String sql = "UPDATE photo_id_verifications SET status = ?, notes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setLong(3, verificationId);
            
            ps.executeUpdate();
        }
    }

    private List<VerifiedPerson> findPersonsByVerificationId(Long verificationId) throws SQLException {
        String sql = "SELECT * FROM verified_persons WHERE verification_id = ? ORDER BY person_index";
        List<VerifiedPerson> persons = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, verificationId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    persons.add(mapResultSetToVerifiedPerson(rs));
                }
            }
        }
        return persons;
    }

    private PhotoIdVerification mapResultSetToVerification(ResultSet rs) throws SQLException {
        PhotoIdVerification verification = new PhotoIdVerification();
        verification.setId(rs.getLong("id"));
        verification.setBookingId(rs.getLong("booking_id"));
        verification.setVerificationDate(rs.getTimestamp("verification_date").toLocalDateTime());
        verification.setTotalPersons(rs.getInt("total_persons"));
        verification.setMainGuestPhotoPath(rs.getString("main_guest_photo_path"));
        verification.setVerifiedBy(rs.getString("verified_by"));
        verification.setStatus(rs.getString("status"));
        verification.setNotes(rs.getString("notes"));
        verification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        verification.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return verification;
    }

    private VerifiedPerson mapResultSetToVerifiedPerson(ResultSet rs) throws SQLException {
        VerifiedPerson person = new VerifiedPerson();
        person.setId(rs.getLong("id"));
        person.setVerificationId(rs.getLong("verification_id"));
        person.setPersonName(rs.getString("person_name"));
        person.setPersonIndex(rs.getInt("person_index"));
        person.setIdType(rs.getString("id_type"));
        person.setIdNumber(rs.getString("id_number"));
        person.setIdPhotoPath(rs.getString("id_photo_path"));
        person.setComingFrom(rs.getString("coming_from"));
        person.setProceedingTo(rs.getString("proceeding_to"));
        person.setContactNumber(rs.getString("contact_number"));
        person.setRelationshipWithGuest(rs.getString("relationship_with_guest"));
        person.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return person;
    }
}