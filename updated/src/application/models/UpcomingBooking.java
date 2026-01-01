// UpcomingBooking.java
package application.models;

import javafx.beans.property.*;

public class UpcomingBooking {
    private final StringProperty guestName = new SimpleStringProperty();
    private final StringProperty checkInDate = new SimpleStringProperty();
    private final StringProperty roomType = new SimpleStringProperty();
    private final StringProperty roomNo = new SimpleStringProperty();

    public UpcomingBooking(String guestName, String checkInDate, String roomType, String roomNo) {
        this.guestName.set(guestName);
        this.checkInDate.set(checkInDate);
        this.roomType.set(roomType);
        this.roomNo.set(roomNo);
    }

    public StringProperty guestNameProperty(){ return guestName; }
    public StringProperty checkInDateProperty(){ return checkInDate; }
    public StringProperty roomTypeProperty(){ return roomType; }
    public StringProperty roomNoProperty(){ return roomNo; }
}
