package application.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceData {
  // Branding
  private String hotelName, hotelAddressLine1, hotelAddressLine2, hotelContact, logoPath, taxLabel;

  // Invoice header
  private String invoiceNumber;
  private LocalDate invoiceDate;

  // Guest / booking
  private String guestName, mobileNumber, roomNumbers, roomType;
  private LocalDate checkInDate, checkOutDate;
  private long nights;

  // Items
  public static class LineItem {
    public String description; public double amount;
    public LineItem() {}
    public LineItem(String d, double a) { description = d; amount = a; }
  }
  private final List<LineItem> items = new ArrayList<>();

  // Totals
  private double discount, subtotal, taxAmount, totalPayable, advancePaid, balanceDue;

  // Payment
  private String paymentMethod, transactionId;

  // Notes
  private final List<String> notes = new ArrayList<>();

  // Getters/setters
  public String getHotelName() { return hotelName; }
  public void setHotelName(String v) { hotelName = v; }
  public String getHotelAddressLine1() { return hotelAddressLine1; }
  public void setHotelAddressLine1(String v) { hotelAddressLine1 = v; }
  public String getHotelAddressLine2() { return hotelAddressLine2; }
  public void setHotelAddressLine2(String v) { hotelAddressLine2 = v; }
  public String getHotelContact() { return hotelContact; }
  public void setHotelContact(String v) { hotelContact = v; }
  public String getLogoPath() { return logoPath; }
  public void setLogoPath(String v) { logoPath = v; }
  public String getTaxLabel() { return taxLabel; }
  public void setTaxLabel(String v) { taxLabel = v; }

  public String getInvoiceNumber() { return invoiceNumber; }
  public void setInvoiceNumber(String v) { invoiceNumber = v; }
  public LocalDate getInvoiceDate() { return invoiceDate; }
  public void setInvoiceDate(LocalDate v) { invoiceDate = v; }

  public String getGuestName() { return guestName; }
  public void setGuestName(String v) { guestName = v; }
  public String getMobileNumber() { return mobileNumber; }
  public void setMobileNumber(String v) { mobileNumber = v; }
  public String getRoomNumbers() { return roomNumbers; }
  public void setRoomNumbers(String v) { roomNumbers = v; }
  public String getRoomType() { return roomType; }
  public void setRoomType(String v) { roomType = v; }
  public LocalDate getCheckInDate() { return checkInDate; }
  public void setCheckInDate(LocalDate v) { checkInDate = v; }
  public LocalDate getCheckOutDate() { return checkOutDate; }
  public void setCheckOutDate(LocalDate v) { checkOutDate = v; }
  public long getNights() { return nights; }
  public void setNights(long v) { nights = v; }

  public List<LineItem> getItems() { return items; }

  public double getDiscount() { return discount; }
  public void setDiscount(double v) { discount = v; }
  public double getSubtotal() { return subtotal; }
  public void setSubtotal(double v) { subtotal = v; }
  public double getTaxAmount() { return taxAmount; }
  public void setTaxAmount(double v) { taxAmount = v; }
  public double getTotalPayable() { return totalPayable; }
  public void setTotalPayable(double v) { totalPayable = v; }
  public double getAdvancePaid() { return advancePaid; }
  public void setAdvancePaid(double v) { advancePaid = v; }
  public double getBalanceDue() { return balanceDue; }
  public void setBalanceDue(double v) { balanceDue = v; }

  public String getPaymentMethod() { return paymentMethod; }
  public void setPaymentMethod(String v) { paymentMethod = v; }
  public String getTransactionId() { return transactionId; }
  public void setTransactionId(String v) { transactionId = v; }

  public List<String> getNotes() { return notes; }
}
