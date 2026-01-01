package application.services;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RoomStatus {
    private final StringProperty roomNo;
    private final StringProperty status; // e.g., "Dirty", "Needs Maintenance", "Vacant & Clean", "Occupied"

    public RoomStatus(String roomNo, String status) {
        this.roomNo = new SimpleStringProperty(roomNo);
        this.status = new SimpleStringProperty(status);
    }

    public String getRoomNo() {
        return roomNo.get();
    }

    public StringProperty roomNoProperty() {
        return roomNo;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    // Optional: Override toString for simpler display in ListView if not using cell factory
    @Override
    public String toString() {
        return roomNo.get() + " (" + status.get() + ")";
    }
}