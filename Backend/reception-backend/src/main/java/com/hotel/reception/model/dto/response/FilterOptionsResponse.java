package com.hotel.reception.model.dto.response;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterOptionsResponse {
    private List<String> roomTypes;
    private List<String> floors;
    private List<String> amenities;
    private List<String> sources;
    private List<String> paymentStatuses;
}