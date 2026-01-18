package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyStatisticResponse {
    private LocalDate date;
    private BigDecimal occupancy;
    private BigDecimal revenue;
    private Integer arrivals;
    private Integer departures;
}