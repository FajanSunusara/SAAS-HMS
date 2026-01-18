package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalendarViewResponse {
    private List<RoomResponse> rooms;
    private List<CalendarBookingResponse> bookings; // Fixed: Changed type to List<CalendarBookingResponse>
    private List<LocalDate> dateRange;
    private LocalDate centerDate;
}