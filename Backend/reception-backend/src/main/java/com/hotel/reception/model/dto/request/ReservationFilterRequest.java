package com.hotel.reception.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationFilterRequest {
    private String selectedFilter;
    private String searchQuery;
    private String activeView;
    private LocalDate currentCenterDate;
    private LocalDate currentMonth;
    private FilterOptions appliedFilters;
    private SortConfig sortConfig;
    private Integer page;
    private Integer size;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class FilterOptions {
    private List<String> roomType;
    private List<String> floor;
    private List<String> status;
    private List<String> amenities;
    private List<String> source;
    private List<String> paymentStatus;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class SortConfig {
    private String key;
    private String direction;
}