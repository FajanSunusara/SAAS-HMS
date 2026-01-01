package application.models;

import java.util.Objects;

public class GuestMember {
    private String name;
    private String phone;
    private String email;
    private String documentType;
    private String documentNumber;
    private String documentPath;
    private String photoPath;
    private String assignedRoom;
    private String nationality = "India";

    // Default constructor
    public GuestMember() {}

    // Basic constructor
    public GuestMember(String name, String phone, String email, String documentType, 
                      String documentNumber, String assignedRoom) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.assignedRoom = assignedRoom;
    }

    // Full constructor
    public GuestMember(String name, String phone, String email, String documentType, 
                      String documentNumber, String documentPath, String photoPath, 
                      String assignedRoom, String nationality) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.documentPath = documentPath;
        this.photoPath = photoPath;
        this.assignedRoom = assignedRoom;
        this.nationality = nationality != null ? nationality : "India";
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getAssignedRoom() {
        return assignedRoom;
    }

    public void setAssignedRoom(String assignedRoom) {
        this.assignedRoom = assignedRoom;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public boolean hasDocument() {
        return documentPath != null && !documentPath.trim().isEmpty();
    }

    public boolean hasPhoto() {
        return photoPath != null && !photoPath.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuestMember that = (GuestMember) o;
        return Objects.equals(documentNumber, that.documentNumber) &&
               Objects.equals(phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentNumber, phone);
    }

    @Override
    public String toString() {
        return "GuestMember{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", assignedRoom='" + assignedRoom + '\'' +
                '}';
    }
}