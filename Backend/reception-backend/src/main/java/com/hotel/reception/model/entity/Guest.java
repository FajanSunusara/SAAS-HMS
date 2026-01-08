package com.hotel.reception.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "guests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Guest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Long guestId;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "email", length = 150)
    private String email;
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Column(name = "country_code", length = 5)
    private String countryCode = "+91";
    
    @Column(name = "nationality", length = 50)
    private String nationality;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "gender", length = 20)
    private String gender;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "state", length = 100)
    private String state;
    
    @Column(name = "country", length = 100)
    private String country;
    
    @Column(name = "zip_code", length = 20)
    private String zipCode;
    
    @Column(name = "id_type", length = 50)
    private String idType;
    
    @Column(name = "id_number", length = 100)
    private String idNumber;
    
    @Column(name = "passport_number", length = 50)
    private String passportNumber;
    
    @Column(name = "passport_expiry")
    private LocalDate passportExpiry;
    
    @Column(name = "id_proof_url", length = 500)
    private String idProofUrl;
    
    @Column(name = "guest_photo_url", length = 500)
    private String guestPhotoUrl;
    
    @Column(name = "loyalty_number", length = 50)
    private String loyaltyNumber;
    
    @Column(name = "vip_status", length = 20)
    private String vipStatus = "REGULAR";
    
    @Column(name = "company", length = 200)
    private String company;
    
    @Column(name = "business_email", length = 150)
    private String businessEmail;
    
    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;
    
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;
    
    @Column(name = "marketing_opt_in")
    private Boolean marketingOptIn = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
