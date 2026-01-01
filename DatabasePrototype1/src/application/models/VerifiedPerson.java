package application.models;

import java.time.LocalDateTime;

public class VerifiedPerson {
    private Long id;
    private Long verificationId;
    private String personName;
    private Integer personIndex;
    private String idType;
    private String idNumber;
    private String idPhotoPath;
    private String comingFrom;
    private String proceedingTo;
    private String contactNumber;
    private String relationshipWithGuest;
    private LocalDateTime createdAt;

    // Constructors
    public VerifiedPerson() {}

    public VerifiedPerson(Long verificationId, String personName, Integer personIndex) {
        this.verificationId = verificationId;
        this.personName = personName;
        this.personIndex = personIndex;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVerificationId() { return verificationId; }
    public void setVerificationId(Long verificationId) { this.verificationId = verificationId; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public Integer getPersonIndex() { return personIndex; }
    public void setPersonIndex(Integer personIndex) { this.personIndex = personIndex; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getIdPhotoPath() { return idPhotoPath; }
    public void setIdPhotoPath(String idPhotoPath) { this.idPhotoPath = idPhotoPath; }

    public String getComingFrom() { return comingFrom; }
    public void setComingFrom(String comingFrom) { this.comingFrom = comingFrom; }

    public String getProceedingTo() { return proceedingTo; }
    public void setProceedingTo(String proceedingTo) { this.proceedingTo = proceedingTo; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getRelationshipWithGuest() { return relationshipWithGuest; }
    public void setRelationshipWithGuest(String relationshipWithGuest) { this.relationshipWithGuest = relationshipWithGuest; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}