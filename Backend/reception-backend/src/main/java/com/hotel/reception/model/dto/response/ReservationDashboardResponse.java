package com.hotel.reception.model.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ReservationDashboardResponse {
    private Integer todayArrivals;
    private Integer todayDepartures;
    private BigDecimal occupancyRate;
    private BigDecimal revenueToday;
}