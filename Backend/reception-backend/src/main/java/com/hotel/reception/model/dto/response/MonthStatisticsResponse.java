package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthStatisticsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal avgOccupancy;
    private BigDecimal peakOccupancy;
    private Integer totalArrivals;
    private Integer totalDepartures;
}