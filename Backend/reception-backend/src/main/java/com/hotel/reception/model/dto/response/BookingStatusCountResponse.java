package com.hotel.reception.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingStatusCountResponse {
    private String label;
    private Long count;
    private String color;
    private String value;
}
