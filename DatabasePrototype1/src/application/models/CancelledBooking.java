// CancelledBooking.java
package application.models;

import javafx.beans.property.*;

public class CancelledBooking {
    private final StringProperty guestName;
    private final StringProperty roomType;
    private final StringProperty cancelDate;
    private final StringProperty reason;

    public CancelledBooking(String guestName, String roomType, String cancelDate, String reason) {
        this.guestName = new SimpleStringProperty(guestName);
        this.roomType = new SimpleStringProperty(roomType);
        this.cancelDate = new SimpleStringProperty(cancelDate);
        this.reason = new SimpleStringProperty(reason);
    }

    public StringProperty guestNameProperty(){ return guestName; }
    public StringProperty roomTypeProperty(){ return roomType; }
    public StringProperty cancelDateProperty(){ return cancelDate; }
    public StringProperty reasonProperty(){ return reason; }
}
