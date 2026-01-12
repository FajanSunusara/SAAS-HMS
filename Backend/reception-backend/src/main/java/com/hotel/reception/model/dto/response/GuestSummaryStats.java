package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestSummaryStats {
    private String mostFrequentGuest;
    private Integer mostFrequentGuestStays;
    private BigDecimal mostFrequentGuestValue;
    
    private String highestLtvGuest;
    private Integer highestLtvGuestStays;
    private BigDecimal highestLtvGuestValue;
    
    private Long totalGuests;
    private Long activeGuests;
    private Long vipGuests;
}