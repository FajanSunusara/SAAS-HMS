package application.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ScheduledCheckout {

    private final StringProperty bookingId = new SimpleStringProperty();
    private final StringProperty roomNo = new SimpleStringProperty();
    private final StringProperty guestName = new SimpleStringProperty();
    private final StringProperty pendingPayment = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty roomTypeNo = new SimpleStringProperty();
    private final StringProperty durationOfStay = new SimpleStringProperty();
    private final StringProperty totalAmount = new SimpleStringProperty();
    private final StringProperty gstDiscounts = new SimpleStringProperty();
    private final StringProperty amountPaid = new SimpleStringProperty();
    private final StringProperty calculatedPendingAmount = new SimpleStringProperty();

    // Updated constructor with bookingId as first parameter
    public ScheduledCheckout(String bookingId, String roomNo, String guestName, String pendingPayment, String status,
                             String roomTypeNo, String durationOfStay, String totalAmount, 
                             String gstDiscounts, String amountPaid, String dummy) {
        setBookingId(bookingId);
        setRoomNo(roomNo);
        setGuestName(guestName);
        setPendingPayment(pendingPayment);
        setStatus(status);
        setRoomTypeNo(roomTypeNo);
        setDurationOfStay(durationOfStay);
        setTotalAmount(totalAmount);
        setGstDiscounts(gstDiscounts);
        setAmountPaid(amountPaid);
        setCalculatedPendingAmount(pendingPayment);
    }

    public ScheduledCheckout() {
		// TODO Auto-generated constructor stub
	}

	// Property methods for JavaFX TableView binding
    public StringProperty bookingIdProperty() { return bookingId; }
    public StringProperty roomNoProperty() { return roomNo; }
    public StringProperty guestNameProperty() { return guestName; }
    public StringProperty pendingPaymentProperty() { return pendingPayment; }
    public StringProperty statusProperty() { return status; }
    public StringProperty roomTypeNoProperty() { return roomTypeNo; }
    public StringProperty durationOfStayProperty() { return durationOfStay; }
    public StringProperty totalAmountProperty() { return totalAmount; }
    public StringProperty gstDiscountsProperty() { return gstDiscounts; }
    public StringProperty amountPaidProperty() { return amountPaid; }
    public StringProperty calculatedPendingAmountProperty() { return calculatedPendingAmount; }

    // Getter methods
    public String getBookingId() { return bookingId.get(); }
    public String getRoomNo() { return roomNo.get(); }
    public String getGuestName() { return guestName.get(); }
    public String getPendingPayment() { return pendingPayment.get(); }
    public String getStatus() { return status.get(); }
    public String getRoomTypeNo() { return roomTypeNo.get(); }
    public String getDurationOfStay() { return durationOfStay.get(); }
    public String getTotalAmount() { return totalAmount.get(); }
    public String getGstDiscounts() { return gstDiscounts.get(); }
    public String getAmountPaid() { return amountPaid.get(); }
    public String getCalculatedPendingAmount() { return calculatedPendingAmount.get(); }

    // Setter methods
    public void setBookingId(String value) { bookingId.set(value != null ? value : ""); }
    public void setRoomNo(String value) { roomNo.set(value != null ? value : ""); }
    public void setGuestName(String value) { guestName.set(value != null ? value : ""); }
    public void setPendingPayment(String value) { pendingPayment.set(value != null ? value : "₹0.00"); }
    public void setStatus(String value) { status.set(value != null ? value : ""); }
    public void setRoomTypeNo(String value) { roomTypeNo.set(value != null ? value : ""); }
    public void setDurationOfStay(String value) { durationOfStay.set(value != null ? value : ""); }
    public void setTotalAmount(String value) { totalAmount.set(value != null ? value : "₹0.00"); }
    public void setGstDiscounts(String value) { gstDiscounts.set(value != null ? value : ""); }
    public void setAmountPaid(String value) { amountPaid.set(value != null ? value : "₹0.00"); }
    public void setCalculatedPendingAmount(String value) { calculatedPendingAmount.set(value != null ? value : "₹0.00"); }

    @Override
    public String toString() {
        return "ScheduledCheckout{" +
                "bookingId='" + getBookingId() + '\'' +
                ", roomNo='" + getRoomNo() + '\'' +
                ", guestName='" + getGuestName() + '\'' +
                ", pendingPayment='" + getPendingPayment() + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
