package com.hotel.reception.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @Column(name = "bed_preference", length = 50)
    private String bedPreference;
    
    @Column(name = "smoking_preference", length = 20)
    private String smokingPreference = "NON_SMOKING";
    
    @Column(name = "floor_preference", length = 50)
    private String floorPreference;
    
    @Column(name = "view_preference", length = 50)
    private String viewPreference;
    
    @Column(name = "extra_bed")
    private Boolean extraBed = false;
    
    @Column(name = "crib")
    private Boolean crib = false;
    
    @Column(name = "wheelchair_access")
    private Boolean wheelchairAccess = false;
    
    @Column(name = "early_check_in")
    private Boolean earlyCheckIn = false;
    
    @Column(name = "late_check_out")
    private Boolean lateCheckOut = false;
}
