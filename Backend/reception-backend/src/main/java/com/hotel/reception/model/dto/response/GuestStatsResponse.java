package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestStatsResponse {
    private Long currentGuests;
    private Long checkInsToday;
    private Long expectedCheckIns;
    private Long checkOutsToday;
    private Double occupancyRate;
    private Long totalRooms;
    private Long occupiedRooms;
}