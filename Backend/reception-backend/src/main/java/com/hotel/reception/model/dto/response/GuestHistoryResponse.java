package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestHistoryResponse {
    private Long guestId;
    private String guestCode;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private String vipLevel;
    private String loyaltyTier;
    private String lastStay;
    private String lastRoom;
    private Integer totalStays;
    private BigDecimal lifetimeValue;
    private String status;
}