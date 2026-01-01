package application.models;

import java.util.Objects;

public class TodayReservation {
    private String guestName, roomNumber, checkIn, checkOut, status;
    private String reservationId ;

    public String getReservationId() {
		return reservationId;
	}

	public void setReservationId(String reservationId) {
		this.reservationId = reservationId;
	}

	public TodayReservation(String g, String r, String ci, String co, String s ,String reservID) {
        guestName = g; roomNumber = r; checkIn = ci; checkOut = co; status = s;reservationId = reservID ;
    }

    public String getGuestName(){ return guestName; }
    public String getRoomNumber(){ return roomNumber; }
    public String getCheckIn(){ return checkIn; }
    public String getCheckOut(){ return checkOut; }
    public String getStatus(){ return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TodayReservation)) return false;
        TodayReservation that = (TodayReservation) o;
        return Objects.equals(guestName, that.guestName) &&
               Objects.equals(roomNumber, that.roomNumber) &&
               Objects.equals(checkIn, that.checkIn) &&
               Objects.equals(checkOut, that.checkOut) &&
               Objects.equals(status, that.status)&&
               Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guestName, roomNumber, checkIn, checkOut, status,reservationId);
    }
}
