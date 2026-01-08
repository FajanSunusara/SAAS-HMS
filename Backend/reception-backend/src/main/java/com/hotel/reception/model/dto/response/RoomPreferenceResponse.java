package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPreferenceResponse {
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