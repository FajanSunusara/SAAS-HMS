package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestResponse {
    private Long guestId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String countryCode;
    private String nationality;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String idType;
    private String idNumber;
    private String passportNumber;
    private LocalDate passportExpiry;
    private String loyaltyNumber;
    private String vipStatus;
    private String company;
    private LocalDateTime createdAt;
    private Integer totalBookings;
    private String status;
}
