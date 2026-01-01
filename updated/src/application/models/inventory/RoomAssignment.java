package application.models.inventory;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class RoomAssignment {
    private final IntegerProperty assignmentId = new SimpleIntegerProperty();
    private final StringProperty roomNo = new SimpleStringProperty();
    private final IntegerProperty itemId = new SimpleIntegerProperty();
    private final StringProperty itemName = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> assignedDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> returnDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> expectedReturnDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty assignedBy = new SimpleStringProperty();
    private final StringProperty returnedBy = new SimpleStringProperty();
    private final StringProperty conditionOnAssignment = new SimpleStringProperty();
    private final StringProperty conditionOnReturn = new SimpleStringProperty();
    private final StringProperty notes = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Constructors
    public RoomAssignment() {
        setAssignedDate(LocalDateTime.now());
        setStatus("Assigned");
        setConditionOnAssignment("Good");
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    public RoomAssignment(String roomNo, int itemId, int quantity, String assignedBy) {
        this();
        setRoomNo(roomNo);
        setItemId(itemId);
        setQuantity(quantity);
        setAssignedBy(assignedBy);
    }

    // Property getters
    public IntegerProperty assignmentIdProperty() { return assignmentId; }
    public StringProperty roomNoProperty() { return roomNo; }
    public IntegerProperty itemIdProperty() { return itemId; }
    public StringProperty itemNameProperty() { return itemName; }
    public IntegerProperty quantityProperty() { return quantity; }
    public ObjectProperty<LocalDateTime> assignedDateProperty() { return assignedDate; }
    public ObjectProperty<LocalDateTime> returnDateProperty() { return returnDate; }
    public ObjectProperty<LocalDateTime> expectedReturnDateProperty() { return expectedReturnDate; }
    public StringProperty statusProperty() { return status; }
    public StringProperty assignedByProperty() { return assignedBy; }
    public StringProperty returnedByProperty() { return returnedBy; }
    public StringProperty conditionOnAssignmentProperty() { return conditionOnAssignment; }
    public StringProperty conditionOnReturnProperty() { return conditionOnReturn; }
    public StringProperty notesProperty() { return notes; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    // Getters and Setters
    public int getAssignmentId() { return assignmentId.get(); }
    public void setAssignmentId(int assignmentId) { this.assignmentId.set(assignmentId); }

    public String getRoomNo() { return roomNo.get(); }
    public void setRoomNo(String roomNo) { this.roomNo.set(roomNo); }

    public int getItemId() { return itemId.get(); }
    public void setItemId(int itemId) { this.itemId.set(itemId); }

    public String getItemName() { return itemName.get(); }
    public void setItemName(String itemName) { this.itemName.set(itemName); }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }

    public LocalDateTime getAssignedDate() { return assignedDate.get(); }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate.set(assignedDate); }

    public LocalDateTime getReturnDate() { return returnDate.get(); }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate.set(returnDate); }

    public LocalDateTime getExpectedReturnDate() { return expectedReturnDate.get(); }
    public void setExpectedReturnDate(LocalDateTime expectedReturnDate) { this.expectedReturnDate.set(expectedReturnDate); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public String getAssignedBy() { return assignedBy.get(); }
    public void setAssignedBy(String assignedBy) { this.assignedBy.set(assignedBy); }

    public String getReturnedBy() { return returnedBy.get(); }
    public void setReturnedBy(String returnedBy) { this.returnedBy.set(returnedBy); }

    public String getConditionOnAssignment() { return conditionOnAssignment.get(); }
    public void setConditionOnAssignment(String conditionOnAssignment) { this.conditionOnAssignment.set(conditionOnAssignment); }

    public String getConditionOnReturn() { return conditionOnReturn.get(); }
    public void setConditionOnReturn(String conditionOnReturn) { this.conditionOnReturn.set(conditionOnReturn); }

    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }

    // Utility methods
    public boolean isOverdue() {
        if (expectedReturnDate.get() != null && returnDate.get() == null) {
            return LocalDateTime.now().isAfter(expectedReturnDate.get());
        }
        return false;
    }

    public boolean isReturned() {
        return "Returned".equals(status.get());
    }

    @Override
    public String toString() {
        return "Room " + roomNo.get() + " - " + itemName.get() + " (" + quantity.get() + ")";
    }
}
