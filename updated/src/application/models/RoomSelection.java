package application.models;

import javafx.beans.property.*;

import java.math.BigDecimal;

/**
 * Model class for representing a room selection in group bookings
 * Each instance represents one room selected for the group booking
 */
public class RoomSelection {
    private final StringProperty roomNo;
    private final StringProperty roomType;
    private final ObjectProperty<BigDecimal> ratePerNight;
    private final IntegerProperty nights;
    private final ObjectProperty<BigDecimal> totalAmount;
    private final BooleanProperty selected;

    /**
     * Constructor for creating a room selection
     * 
     * @param roomNo The room number
     * @param roomType The type of room
     * @param ratePerNight The rate per night for this room
     * @param nights The number of nights for the stay
     */
    public RoomSelection(String roomNo, String roomType, BigDecimal ratePerNight, int nights) {
        this.roomNo = new SimpleStringProperty(roomNo);
        this.roomType = new SimpleStringProperty(roomType);
        this.ratePerNight = new SimpleObjectProperty<>(ratePerNight);
        this.nights = new SimpleIntegerProperty(nights);
        this.selected = new SimpleBooleanProperty(true); // Default to selected
        
        // Calculate total amount
        BigDecimal total = ratePerNight.multiply(BigDecimal.valueOf(nights));
        this.totalAmount = new SimpleObjectProperty<>(total);
    }

    // ==================== GETTERS & SETTERS ====================

    public String getRoomNo() { 
        return roomNo.get(); 
    }
    
    public void setRoomNo(String roomNo) { 
        this.roomNo.set(roomNo); 
    }
    
    public StringProperty roomNoProperty() { 
        return roomNo; 
    }

    public String getRoomType() { 
        return roomType.get(); 
    }
    
    public void setRoomType(String roomType) { 
        this.roomType.set(roomType); 
    }
    
    public StringProperty roomTypeProperty() { 
        return roomType; 
    }

    public BigDecimal getRatePerNight() { 
        return ratePerNight.get(); 
    }
    
    public void setRatePerNight(BigDecimal ratePerNight) { 
        this.ratePerNight.set(ratePerNight); 
        updateTotal(); // Recalculate total when rate changes
    }
    
    public ObjectProperty<BigDecimal> ratePerNightProperty() { 
        return ratePerNight; 
    }

    public int getNights() { 
        return nights.get(); 
    }
    
    public void setNights(int nights) { 
        this.nights.set(nights); 
        updateTotal(); // Recalculate total when nights change
    }
    
    public IntegerProperty nightsProperty() { 
        return nights; 
    }

    public BigDecimal getTotalAmount() { 
        return totalAmount.get(); 
    }
    
    public ObjectProperty<BigDecimal> totalAmountProperty() { 
        return totalAmount; 
    }

    public boolean isSelected() { 
        return selected.get(); 
    }
    
    public void setSelected(boolean selected) { 
        this.selected.set(selected); 
    }
    
    public BooleanProperty selectedProperty() { 
        return selected; 
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Updates the total amount based on current rate and nights
     * Called automatically when rate or nights change
     */
    public void updateTotal() {
        BigDecimal currentRate = ratePerNight.get();
        int currentNights = nights.get();
        
        if (currentRate != null && currentNights > 0) {
            BigDecimal newTotal = currentRate.multiply(BigDecimal.valueOf(currentNights));
            totalAmount.set(newTotal);
        } else {
            totalAmount.set(BigDecimal.ZERO);
        }
    }

    /**
     * Updates the rate per night and recalculates the total
     * 
     * @param newRate The new rate per night
     */
    public void updateRate(BigDecimal newRate) {
        if (newRate != null && newRate.compareTo(BigDecimal.ZERO) >= 0) {
            ratePerNight.set(newRate);
            updateTotal();
        }
    }

    /**
     * Updates the number of nights and recalculates the total
     * 
     * @param newNights The new number of nights
     */
    public void updateNights(int newNights) {
        if (newNights >= 0) {
            nights.set(newNights);
            updateTotal();
        }
    }

    /**
     * Creates a copy of this RoomSelection
     * Useful for creating backups or for display purposes
     * 
     * @return A new RoomSelection with the same values
     */
    public RoomSelection copy() {
        RoomSelection copy = new RoomSelection(
            this.roomNo.get(),
            this.roomType.get(),
            this.ratePerNight.get(),
            this.nights.get()
        );
        copy.setSelected(this.selected.get());
        return copy;
    }

    /**
     * Checks if this room selection is valid for booking
     * 
     * @return true if all required fields are present and valid
     */
    public boolean isValid() {
        return roomNo.get() != null && !roomNo.get().trim().isEmpty() &&
               roomType.get() != null && !roomType.get().trim().isEmpty() &&
               ratePerNight.get() != null && ratePerNight.get().compareTo(BigDecimal.ZERO) > 0 &&
               nights.get() > 0 &&
               totalAmount.get() != null && totalAmount.get().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Gets a formatted string representation of the room rate
     * 
     * @return Formatted rate string (e.g., "₹2,500.00")
     */
    public String getFormattedRate() {
        return String.format("₹%,.2f", ratePerNight.get());
    }

    /**
     * Gets a formatted string representation of the total amount
     * 
     * @return Formatted total string (e.g., "₹15,000.00")
     */
    public String getFormattedTotal() {
        return String.format("₹%,.2f", totalAmount.get());
    }

    // ==================== EQUALS & HASHCODE ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RoomSelection that = (RoomSelection) obj;
        return roomNo.get().equals(that.roomNo.get());
    }

    @Override
    public int hashCode() {
        return roomNo.get().hashCode();
    }

    // ==================== TO STRING ====================

    @Override
    public String toString() {
        return String.format("RoomSelection{roomNo=%s, roomType=%s, rate=₹%.2f, nights=%d, total=₹%.2f, selected=%s}",
                roomNo.get(), roomType.get(), ratePerNight.get(), nights.get(), totalAmount.get(), selected.get());
    }

    /**
     * Gets a user-friendly display string
     * 
     * @return Formatted display string
     */
    public String toDisplayString() {
        return String.format("%s (%s) - ₹%.2f/night × %d nights = ₹%.2f",
                roomNo.get(), roomType.get(), ratePerNight.get(), nights.get(), totalAmount.get());
    }
}