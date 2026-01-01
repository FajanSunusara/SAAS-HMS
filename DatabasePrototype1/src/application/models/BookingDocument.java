package application.models;

import java.sql.Timestamp;

public class BookingDocument {
    private Long id;
    private Long bookingId;
    private String documentType;
    private String fileName;
    private String filePath;
    private String personName;
    private int personIndex;
    private Timestamp uploadedAt;
    
    // Constructors
    public BookingDocument() {}
    
    public BookingDocument(Long bookingId, String documentType, String fileName, 
                          String filePath, String personName, int personIndex) {
        this.bookingId = bookingId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.personName = personName;
        this.personIndex = personIndex;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    
    public int getPersonIndex() { return personIndex; }
    public void setPersonIndex(int personIndex) { this.personIndex = personIndex; }
    
    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }
}