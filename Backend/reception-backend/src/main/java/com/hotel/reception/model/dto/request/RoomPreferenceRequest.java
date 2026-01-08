package com.hotel.reception.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomPreferenceRequest {
    private String bedPreference;
    private String smokingPreference;
    private String floorPreference;
    private String viewPreference;
    private Boolean extraBed;
    private Boolean crib;
    private Boolean wheelchairAccess;
    private Boolean earlyCheckIn;
    private Boolean lateCheckOut;
}
