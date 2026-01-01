package application.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.math.BigDecimal;
import java.util.Objects;

public class Room {
    private Long id;
    private String roomNo;
    private String roomType;
    private Integer floor;
    private Integer beds;
    private Boolean ac;
    private BigDecimal price;
    private String status;
    private String description;

    // JavaFX Properties (lazy initialization)
    private StringProperty roomNoProperty;
    private StringProperty roomTypeProperty;
    private StringProperty typeProperty; // For backward compatibility
    private StringProperty floorProperty;
    private StringProperty statusProperty;
    private StringProperty priceProperty;
    private StringProperty acProperty;
    private StringProperty acTypeProperty; // For backward compatibility

    // Constructors
    public Room() {}

    public Room(String roomNo, String roomType, Integer floor, Integer beds, Boolean ac, BigDecimal price, String status, String description) {
        this.roomNo = roomNo;
        this.roomType = roomType;
        this.floor = floor;
        this.beds = beds;
        this.ac = ac;
        this.price = price;
        this.status = status;
        this.description = description;
    }

    // Getters and Setters (maintain compatibility)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { 
        this.roomNo = roomNo; 
        if (roomNoProperty != null) {
            roomNoProperty.set(roomNo);
        }
    }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { 
        this.roomType = roomType; 
        if (roomTypeProperty != null) {
            roomTypeProperty.set(roomType);
        }
        if (typeProperty != null) {
            typeProperty.set(roomType);
        }
    }

    // Backward compatibility methods
    public String getType() { return roomType; }
    public void setType(String type) { setRoomType(type); }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { 
        this.floor = floor; 
        if (floorProperty != null) {
            floorProperty.set(floor != null ? floor.toString() : "");
        }
    }

    public void setFloor(String floorStr) {
        try {
            setFloor(Integer.parseInt(floorStr));
        } catch (NumberFormatException e) {
            setFloor(0);
        }
    }

    public Integer getBeds() { return beds; }
    public void setBeds(Integer beds) { this.beds = beds; }

    public Boolean isAc() { return ac; }
    public void setAc(Boolean ac) { 
        this.ac = ac; 
        if (acProperty != null) {
            acProperty.set(ac != null && ac ? "AC" : "Non-AC");
        }
        if (acTypeProperty != null) {
            acTypeProperty.set(ac != null && ac ? "AC" : "Non-AC");
        }
    }

    // Backward compatibility methods
    public String getAcType() { return ac != null && ac ? "AC" : "Non-AC"; }
    public void setAcType(String acType) { setAc("AC".equals(acType)); }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { 
        this.price = price; 
        if (priceProperty != null) {
            priceProperty.set(price != null ? "₹" + String.format("%.2f", price.doubleValue()) : "₹0.00");
        }
    }

    public void setPrice(String priceStr) {
        try {
            String cleanPrice = priceStr.replace("₹", "").replace(",", "");
            setPrice(new BigDecimal(cleanPrice));
        } catch (NumberFormatException e) {
            setPrice(BigDecimal.ZERO);
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        if (statusProperty != null) {
            statusProperty.set(status);
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // JavaFX Property getters (lazy initialization)
    public StringProperty roomNoProperty() { 
        if (roomNoProperty == null) {
            roomNoProperty = new SimpleStringProperty(roomNo);
        }
        return roomNoProperty; 
    }

    public StringProperty roomTypeProperty() { 
        if (roomTypeProperty == null) {
            roomTypeProperty = new SimpleStringProperty(roomType);
        }
        return roomTypeProperty; 
    }

    public StringProperty typeProperty() { 
        if (typeProperty == null) {
            typeProperty = new SimpleStringProperty(roomType);
        }
        return typeProperty; 
    }

    public StringProperty floorProperty() { 
        if (floorProperty == null) {
            floorProperty = new SimpleStringProperty(floor != null ? floor.toString() : "");
        }
        return floorProperty; 
    }

    public StringProperty statusProperty() { 
        if (statusProperty == null) {
            statusProperty = new SimpleStringProperty(status);
        }
        return statusProperty; 
    }

    public StringProperty priceProperty() { 
        if (priceProperty == null) {
            priceProperty = new SimpleStringProperty(
                price != null ? "₹" + String.format("%.2f", price.doubleValue()) : "₹0.00"
            );
        }
        return priceProperty; 
    }

    public StringProperty acProperty() { 
        if (acProperty == null) {
            acProperty = new SimpleStringProperty(ac != null && ac ? "AC" : "Non-AC");
        }
        return acProperty; 
    }

    public StringProperty acTypeProperty() { 
        if (acTypeProperty == null) {
            acTypeProperty = new SimpleStringProperty(ac != null && ac ? "AC" : "Non-AC");
        }
        return acTypeProperty; 
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomNo='" + roomNo + '\'' +
                ", roomType='" + roomType + '\'' +
                ", floor=" + floor +
                ", beds=" + beds +
                ", ac=" + ac +
                ", price=" + price +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(id, room.id) &&
                Objects.equals(roomNo, room.roomNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roomNo);
    }
}
