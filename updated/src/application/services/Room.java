package application.services;

import javafx.beans.property.SimpleStringProperty;

public class Room {
    private SimpleStringProperty roomNo;
    private SimpleStringProperty type;
    private SimpleStringProperty floor;
    private SimpleStringProperty status;
    private SimpleStringProperty price;
    private SimpleStringProperty acType;
    
    public Room(String roomNo, String type, String floor, String status, String price, String acType) {
        this.roomNo = new SimpleStringProperty(roomNo);
        this.type = new SimpleStringProperty(type);
        this.floor = new SimpleStringProperty(floor);
        this.status = new SimpleStringProperty(status);
        this.price = new SimpleStringProperty(price);
        this.acType = new SimpleStringProperty(acType);
    }
    
    // Getters
    public String getRoomNo() { return roomNo.get(); }
    public String getType() { return type.get(); }
    public String getFloor() { return floor.get(); }
    public String getStatus() { return status.get(); }
    public String getPrice() { return price.get(); }
    public String getAcType() { return acType.get(); }
    
    // Setters
    public void setRoomNo(String roomNo) { this.roomNo.set(roomNo); }
    public void setType(String type) { this.type.set(type); }
    public void setFloor(String floor) { this.floor.set(floor); }
    public void setStatus(String status) { this.status.set(status); }
    public void setPrice(String price) { this.price.set(price); }
    public void setAcType(String acType) { this.acType.set(acType); }
    
    // Property methods
    public SimpleStringProperty roomNoProperty() { return roomNo; }
    public SimpleStringProperty typeProperty() { return type; }
    public SimpleStringProperty floorProperty() { return floor; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty priceProperty() { return price; }
    public SimpleStringProperty acTypeProperty() { return acType; }
}
