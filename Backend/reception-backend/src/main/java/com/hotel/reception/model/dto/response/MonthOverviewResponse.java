package com.hotel.reception.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MonthOverviewResponse {
    private List<DailyStatisticResponse> dailyStatistics;
    private MonthStatisticsResponse monthStatistics;
    private String monthYear;
}

