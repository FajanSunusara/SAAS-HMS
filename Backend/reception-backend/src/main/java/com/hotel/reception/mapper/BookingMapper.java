package com.hotel.reception.mapper;

import com.hotel.reception.model.dto.response.DashboardBookingResponse;
import com.hotel.reception.model.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public DashboardBookingResponse toDashboardResponse(Booking booking) {

        String guestName = "N/A";
        if (booking.getGuest() != null) {
            guestName = booking.getGuest().getFirstName() + " " +
                        booking.getGuest().getLastName();
        }

        String roomNumber = "N/A";
        if (booking.getBookingRooms() != null && !booking.getBookingRooms().isEmpty()) {
            roomNumber = booking.getBookingRooms()
                    .get(0)
                    .getRoom()
                    .getRoomNumber();
        }

        return DashboardBookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .guestName(guestName)
                .roomNumber(roomNumber)
                .status(
                    booking.getStatus()
                           .name()
                           .toLowerCase()
                           .replace("_", "-")
                )
                .build();
    }
}
