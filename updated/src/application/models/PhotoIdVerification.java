package application.models;

import java.time.LocalDateTime;
import java.util.List;

public class PhotoIdVerification {
    private Long id;
    private Long bookingId;
    private LocalDateTime verificationDate;
    private Integer totalPersons;
    private String mainGuestPhotoPath;
    private String verifiedBy;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<VerifiedPerson> persons;

    // Constructors
    public PhotoIdVerification() {}

    public PhotoIdVerification(Long bookingId, Integer totalPersons) {
        this.bookingId = bookingId;
        this.totalPersons = totalPersons;
        this.verificationDate = LocalDateTime.now();
        this.status = "Pending";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public LocalDateTime getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; }

    public Integer getTotalPersons() { return totalPersons; }
    public void setTotalPersons(Integer totalPersons) { this.totalPersons = totalPersons; }

    public String getMainGuestPhotoPath() { return mainGuestPhotoPath; }
    public void setMainGuestPhotoPath(String mainGuestPhotoPath) { this.mainGuestPhotoPath = mainGuestPhotoPath; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<VerifiedPerson> getPersons() { return persons; }
    public void setPersons(List<VerifiedPerson> persons) { this.persons = persons; }
}