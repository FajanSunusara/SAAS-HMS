package com.hotel.reception.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    
    @NotNull(message = "Room assignments are required")
    private List<RoomAssignment> roomAssignments;
    
    private LocalDateTime actualCheckInTime;
    private String verificationStatus;
    private String specialNotes;
    private String checkedInBy;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomAssignment {
        private Long roomId;
        private Long guestId; // For group bookings
        private String keyCardNumber;
        private String wifiPassword;
    }
}
