package application.models;

import java.time.LocalDate;

public class ReservationStub {

    public ReservationStub() {
		super();
	}
	private Long id;
    public ReservationStub(Long id, Long guestId, String roomNo, LocalDate checkInDate, LocalDate checkOutDate,
			String guestName, String roomType, String status, Integer numGuests, String notes) {
		super();
		this.id = id;
		this.guestId = guestId;
		this.roomNo = roomNo;
		this.checkInDate = checkInDate;
		this.checkOutDate = checkOutDate;
		this.guestName = guestName;
		this.roomType = roomType;
		this.status = status;
		this.numGuests = numGuests;
		this.notes = notes;
	}
	private Long guestId;
    private String roomNo;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    // Optional UI helpers
    private String guestName;
    private String roomType;

    // Added fields to match schema for insert/update
    private String status;
    private Integer numGuests;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getNumGuests() { return numGuests; }
    public void setNumGuests(Integer numGuests) { this.numGuests = numGuests; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
