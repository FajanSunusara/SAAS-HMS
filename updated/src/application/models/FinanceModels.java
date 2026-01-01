package application.models;

import javafx.beans.property.SimpleStringProperty;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Finance-related data models for the hotel management system
 * Contains all model classes used in finance operations
 */
public class FinanceModels {

    // ===== CORE DATA MODELS =====
    
    public static class Payment {
        private Long id;
        private Long bookingId;
        private BigDecimal amount;
        private String method;
        private String transactionId;
        private Timestamp paymentDate;
        private String notes;
        private String guestName;
        private String roomNo;
        private String phone;
        private String nationality;

        // Constructors
        public Payment() {}

        public Payment(Long id, Long bookingId, BigDecimal amount, String method,
                      String transactionId, Timestamp paymentDate, String notes) {
            this.id = id;
            this.bookingId = bookingId;
            this.amount = amount;
            this.method = method;
            this.transactionId = transactionId;
            this.paymentDate = paymentDate;
            this.notes = notes;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public Timestamp getPaymentDate() { return paymentDate; }
        public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }
    }

    public static class ReservationPayment {
        private Long id;
        private Long reservationId;
        private BigDecimal amount;
        private String method;
        private String notes;
        private Timestamp paymentDate;
        private String guestName;
        private String roomNo;
        private String phone;

        // Constructors
        public ReservationPayment() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getReservationId() { return reservationId; }
        public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Timestamp getPaymentDate() { return paymentDate; }
        public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    // ===== NEW: RETURN PAYMENT MODEL =====
    
    public static class ReturnPayment {
        private Long id;
        private LocalDate returnDate;
        private Long bookingId;
        private Long guestId;
        private Long originalPaymentId;
        private String returnType;
        private String returnReason;
        private BigDecimal originalAmount;
        private BigDecimal returnAmount;
        private BigDecimal deductionAmount;
        private String returnMethod;
        private String transactionId;
        private String referenceNumber;
        private Long processedBy;
        private Long approvedBy;
        private BigDecimal gstReturnAmount;
        private BigDecimal baseReturnAmount;
        private BigDecimal processingFee;
        private String bankAccountDetails;
        private String status;
        private String notes;
        private String receiptPath;
        private Timestamp createdAt;
        private Timestamp processedAt;
        private Timestamp completedAt;
        // Additional fields for display
        private String guestName;
        private String roomNo;

        // Constructors
        public ReturnPayment() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDate getReturnDate() { return returnDate; }
        public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public Long getGuestId() { return guestId; }
        public void setGuestId(Long guestId) { this.guestId = guestId; }
        public Long getOriginalPaymentId() { return originalPaymentId; }
        public void setOriginalPaymentId(Long originalPaymentId) { this.originalPaymentId = originalPaymentId; }
        public String getReturnType() { return returnType; }
        public void setReturnType(String returnType) { this.returnType = returnType; }
        public String getReturnReason() { return returnReason; }
        public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
        public BigDecimal getOriginalAmount() { return originalAmount; }
        public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
        public BigDecimal getReturnAmount() { return returnAmount; }
        public void setReturnAmount(BigDecimal returnAmount) { this.returnAmount = returnAmount; }
        public BigDecimal getDeductionAmount() { return deductionAmount; }
        public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; }
        public String getReturnMethod() { return returnMethod; }
        public void setReturnMethod(String returnMethod) { this.returnMethod = returnMethod; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        public Long getProcessedBy() { return processedBy; }
        public void setProcessedBy(Long processedBy) { this.processedBy = processedBy; }
        public Long getApprovedBy() { return approvedBy; }
        public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
        public BigDecimal getGstReturnAmount() { return gstReturnAmount; }
        public void setGstReturnAmount(BigDecimal gstReturnAmount) { this.gstReturnAmount = gstReturnAmount; }
        public BigDecimal getBaseReturnAmount() { return baseReturnAmount; }
        public void setBaseReturnAmount(BigDecimal baseReturnAmount) { this.baseReturnAmount = baseReturnAmount; }
        public BigDecimal getProcessingFee() { return processingFee; }
        public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }
        public String getBankAccountDetails() { return bankAccountDetails; }
        public void setBankAccountDetails(String bankAccountDetails) { this.bankAccountDetails = bankAccountDetails; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getReceiptPath() { return receiptPath; }
        public void setReceiptPath(String receiptPath) { this.receiptPath = receiptPath; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
        public Timestamp getProcessedAt() { return processedAt; }
        public void setProcessedAt(Timestamp processedAt) { this.processedAt = processedAt; }
        public Timestamp getCompletedAt() { return completedAt; }
        public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    }

    // ===== NEW: HOTEL EXPENSE MODEL =====
    
 // ===== FIXED: HOTEL EXPENSE MODEL =====
    public static class HotelExpense {
        private Long id;
        private LocalDate expenseDate;
        private String category;
        private String subcategory;
        private String description;
        private BigDecimal amount;
        private String paymentMethod;
        private String vendorName;
        private String vendorContact;
        private String invoiceNumber;
        private String transactionId;
        private String department;
        private Long approvedBy;
        private Long paidBy;
        private String roomNo;
        private Boolean gstApplicable;
        private BigDecimal gstRate;
        private BigDecimal gstAmount;
        private BigDecimal netAmount;
        private String status;
        private String receiptPath;
        private String notes;
        private Timestamp createdAt;
        private Timestamp updatedAt;
        // Additional fields for display
        private String approvedByName;
        private String paidByName;

        // Constructors
        public HotelExpense() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDate getExpenseDate() { return expenseDate; }
        public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSubcategory() { return subcategory; }
        public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }
        public String getVendorContact() { return vendorContact; }
        public void setVendorContact(String vendorContact) { this.vendorContact = vendorContact; }
        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Long getApprovedBy() { return approvedBy; }
        public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
        public Long getPaidById() { return paidBy; }  // FIXED: Renamed to avoid conflict
        public void setPaidById(Long paidBy) { this.paidBy = paidBy; }  // FIXED: Renamed to avoid conflict
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
        public Boolean getGstApplicable() { return gstApplicable; }
        public void setGstApplicable(Boolean gstApplicable) { this.gstApplicable = gstApplicable; }
        public BigDecimal getGstRate() { return gstRate; }
        public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }
        public BigDecimal getGstAmount() { return gstAmount; }
        public void setGstAmount(BigDecimal gstAmount) { this.gstAmount = gstAmount; }
        public BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReceiptPath() { return receiptPath; }
        public void setReceiptPath(String receiptPath) { this.receiptPath = receiptPath; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
        public Timestamp getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
        public String getApprovedByName() { return approvedByName; }
        public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }
        public String getPaidByName() { return paidByName; }
        public void setPaidByName(String paidByName) { this.paidByName = paidByName; }
        
        // FIXED: Single getPaidBy method for display
        public String getPaidBy() { 
            return paidByName != null ? paidByName : "Unknown"; 
        }
        
        public void setPaidBy(String paidBy) { 
            this.paidByName = paidBy; 
        }
    }


    // ===== LEGACY: EXPENSE MODEL (keep for compatibility) =====
    
    public static class Expense {
        private Long id;
        private LocalDate expenseDate;
        private String category;
        private String description;
        private BigDecimal amount;
        private String paidBy;
        private String guestName;
        private String roomNo;

        // Constructors and getters/setters
        public Expense() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDate getExpenseDate() { return expenseDate; }
        public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaidBy() { return paidBy; }
        public void setPaidBy(String paidBy) { this.paidBy = paidBy; }
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    }

    // ===== DAILY SHEET MODELS =====
    
    public static class DailySheetData {
        private String roomNo;
        private LocalDate date;
        private boolean occupied;
        private BigDecimal todayAmount;
        private BigDecimal todayPaymentReceived;
        private BigDecimal pendingAmount;
        private BigDecimal totalAmount;

        // Constructors
        public DailySheetData() {}

        // Getters and Setters
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public boolean isOccupied() { return occupied; }
        public void setOccupied(boolean occupied) { this.occupied = occupied; }
        public BigDecimal getTodayAmount() { return todayAmount; }
        public void setTodayAmount(BigDecimal todayAmount) { this.todayAmount = todayAmount; }
        public BigDecimal getTodayPaymentReceived() { return todayPaymentReceived; }
        public void setTodayPaymentReceived(BigDecimal todayPaymentReceived) { this.todayPaymentReceived = todayPaymentReceived; }
        public BigDecimal getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }

    public static class DailySheetDetailData {
        private String roomNo;
        private String roomStatus;
        private String guestName;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private BigDecimal todayAmount;
        private BigDecimal todayPaymentReceived;
        private BigDecimal pendingAmount;
        private BigDecimal totalAmount;
        private String paymentMethods;

        // Constructors
        public DailySheetDetailData() {}

        // Getters and Setters
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
        public String getRoomStatus() { return roomStatus; }
        public void setRoomStatus(String roomStatus) { this.roomStatus = roomStatus; }
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public LocalDate getCheckInDate() { return checkInDate; }
        public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
        public LocalDate getCheckOutDate() { return checkOutDate; }
        public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
        public BigDecimal getTodayAmount() { return todayAmount; }
        public void setTodayAmount(BigDecimal todayAmount) { this.todayAmount = todayAmount; }
        public BigDecimal getTodayPaymentReceived() { return todayPaymentReceived; }
        public void setTodayPaymentReceived(BigDecimal todayPaymentReceived) { this.todayPaymentReceived = todayPaymentReceived; }
        public BigDecimal getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getPaymentMethods() { return paymentMethods; }
        public void setPaymentMethods(String paymentMethods) { this.paymentMethods = paymentMethods; }
    }

    public static class DailyCharges {
        private Long bookingId;
        private LocalDate calculationDate;
        private BigDecimal chargesIncurred;
        private BigDecimal pendingAmount;
        private BigDecimal receivedAmount;
        private String roomNo;
        private String guestName;
        private Integer daysStayed;
        private BigDecimal dailyRate;

        // Constructors and getters/setters
        public DailyCharges() {}

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public LocalDate getCalculationDate() { return calculationDate; }
        public void setCalculationDate(LocalDate calculationDate) { this.calculationDate = calculationDate; }
        public BigDecimal getChargesIncurred() { return chargesIncurred; }
        public void setChargesIncurred(BigDecimal chargesIncurred) { this.chargesIncurred = chargesIncurred; }
        public BigDecimal getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }
        public BigDecimal getReceivedAmount() { return receivedAmount; }
        public void setReceivedAmount(BigDecimal receivedAmount) { this.receivedAmount = receivedAmount; }
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public Integer getDaysStayed() { return daysStayed; }
        public void setDaysStayed(Integer daysStayed) { this.daysStayed = daysStayed; }
        public BigDecimal getDailyRate() { return dailyRate; }
        public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    }

    // ===== UPDATED: FINANCIAL SUMMARY =====
    
    public static class FinancialSummary {
        private BigDecimal totalPayments = BigDecimal.ZERO;
        private BigDecimal totalReservationPayments = BigDecimal.ZERO;
        private BigDecimal totalReturnPayments = BigDecimal.ZERO;
        private BigDecimal totalIncome = BigDecimal.ZERO;
        private BigDecimal totalExpenses = BigDecimal.ZERO;
        private BigDecimal totalHotelExpenses = BigDecimal.ZERO;
        private BigDecimal netBalance = BigDecimal.ZERO;
        private BigDecimal totalPending = BigDecimal.ZERO;
        private LocalDate fromDate;
        private LocalDate toDate;

        // Constructors and getters/setters
        public FinancialSummary() {}

        public BigDecimal getTotalPayments() { return totalPayments; }
        public void setTotalPayments(BigDecimal totalPayments) { this.totalPayments = totalPayments; }
        public BigDecimal getTotalReservationPayments() { return totalReservationPayments; }
        public void setTotalReservationPayments(BigDecimal totalReservationPayments) { this.totalReservationPayments = totalReservationPayments; }
        public BigDecimal getTotalReturnPayments() { return totalReturnPayments; }
        public void setTotalReturnPayments(BigDecimal totalReturnPayments) { this.totalReturnPayments = totalReturnPayments; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
        public BigDecimal getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
        public BigDecimal getTotalHotelExpenses() { return totalHotelExpenses; }
        public void setTotalHotelExpenses(BigDecimal totalHotelExpenses) { this.totalHotelExpenses = totalHotelExpenses; }
        public BigDecimal getNetBalance() { return netBalance; }
        public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }
        public BigDecimal getTotalPending() { return totalPending; }
        public void setTotalPending(BigDecimal totalPending) { this.totalPending = totalPending; }
        public LocalDate getFromDate() { return fromDate; }
        public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
        public LocalDate getToDate() { return toDate; }
        public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    }

    // ===== JAVAFX TABLE ROW MODELS =====

    public static class PaymentHistoryRow {
        private final SimpleStringProperty paymentId;
        private final SimpleStringProperty guestName;
        private final SimpleStringProperty roomNo;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty method;
        private final SimpleStringProperty transactionId;
        private final SimpleStringProperty paymentDate;

        public PaymentHistoryRow(String paymentId, String guestName, String roomNo,
                               String amount, String method, String transactionId, String paymentDate) {
            this.paymentId = new SimpleStringProperty(paymentId);
            this.guestName = new SimpleStringProperty(guestName);
            this.roomNo = new SimpleStringProperty(roomNo);
            this.amount = new SimpleStringProperty(amount);
            this.method = new SimpleStringProperty(method);
            this.transactionId = new SimpleStringProperty(transactionId);
            this.paymentDate = new SimpleStringProperty(paymentDate);
        }

        // Property methods
        public SimpleStringProperty paymentIdProperty() { return paymentId; }
        public SimpleStringProperty guestNameProperty() { return guestName; }
        public SimpleStringProperty roomNoProperty() { return roomNo; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty methodProperty() { return method; }
        public SimpleStringProperty transactionIdProperty() { return transactionId; }
        public SimpleStringProperty paymentDateProperty() { return paymentDate; }

        // Getters
        public String getPaymentId() { return paymentId.get(); }
        public String getGuestName() { return guestName.get(); }
        public String getRoomNo() { return roomNo.get(); }
        public String getAmount() { return amount.get(); }
        public String getMethod() { return method.get(); }
        public String getTransactionId() { return transactionId.get(); }
        public String getPaymentDate() { return paymentDate.get(); }

        @Override
        public String toString() {
            return String.join(" ", paymentId.get(), guestName.get(), roomNo.get(),
                              amount.get(), method.get(), transactionId.get(), paymentDate.get());
        }
    }

    public static class ReservationPaymentRow {
        private final SimpleStringProperty reservationId;
        private final SimpleStringProperty guestName;
        private final SimpleStringProperty roomNo;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty method;
        private final SimpleStringProperty notes;
        private final SimpleStringProperty paymentDate;

        public ReservationPaymentRow(String reservationId, String guestName, String roomNo,
                                   String amount, String method, String notes, String paymentDate) {
            this.reservationId = new SimpleStringProperty(reservationId);
            this.guestName = new SimpleStringProperty(guestName);
            this.roomNo = new SimpleStringProperty(roomNo);
            this.amount = new SimpleStringProperty(amount);
            this.method = new SimpleStringProperty(method);
            this.notes = new SimpleStringProperty(notes);
            this.paymentDate = new SimpleStringProperty(paymentDate);
        }

        // Property methods
        public SimpleStringProperty reservationIdProperty() { return reservationId; }
        public SimpleStringProperty guestNameProperty() { return guestName; }
        public SimpleStringProperty roomNoProperty() { return roomNo; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty methodProperty() { return method; }
        public SimpleStringProperty notesProperty() { return notes; }
        public SimpleStringProperty paymentDateProperty() { return paymentDate; }

        // Getters
        public String getReservationId() { return reservationId.get(); }
        public String getGuestName() { return guestName.get(); }
        public String getRoomNo() { return roomNo.get(); }
        public String getAmount() { return amount.get(); }
        public String getMethod() { return method.get(); }
        public String getNotes() { return notes.get(); }
        public String getPaymentDate() { return paymentDate.get(); }
    }

    public static class ReturnPaymentRow {
        private final SimpleStringProperty returnId;
        private final SimpleStringProperty guestName;
        private final SimpleStringProperty roomNo;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty method;
        private final SimpleStringProperty returnDate;

        public ReturnPaymentRow(String returnId, String guestName, String roomNo,
                               String amount, String method, String returnDate) {
            this.returnId = new SimpleStringProperty(returnId);
            this.guestName = new SimpleStringProperty(guestName);
            this.roomNo = new SimpleStringProperty(roomNo);
            this.amount = new SimpleStringProperty(amount);
            this.method = new SimpleStringProperty(method);
            this.returnDate = new SimpleStringProperty(returnDate);
        }

        // Property methods
        public SimpleStringProperty returnIdProperty() { return returnId; }
        public SimpleStringProperty guestNameProperty() { return guestName; }
        public SimpleStringProperty roomNoProperty() { return roomNo; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty methodProperty() { return method; }
        public SimpleStringProperty returnDateProperty() { return returnDate; }

        // Getters
        public String getReturnId() { return returnId.get(); }
        public String getGuestName() { return guestName.get(); }
        public String getRoomNo() { return roomNo.get(); }
        public String getAmount() { return amount.get(); }
        public String getMethod() { return method.get(); }
        public String getReturnDate() { return returnDate.get(); }
    }

    public static class ExpenseHistoryRow {
        private final SimpleStringProperty expenseDate;
        private final SimpleStringProperty category;
        private final SimpleStringProperty description;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty paidBy;

        public ExpenseHistoryRow(String expenseDate, String category, String description,
                               String amount, String paidBy) {
            this.expenseDate = new SimpleStringProperty(expenseDate);
            this.category = new SimpleStringProperty(category);
            this.description = new SimpleStringProperty(description);
            this.amount = new SimpleStringProperty(amount);
            this.paidBy = new SimpleStringProperty(paidBy);
        }

        // Property methods
        public SimpleStringProperty expenseDateProperty() { return expenseDate; }
        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty paidByProperty() { return paidBy; }

        // Getters
        public String getExpenseDate() { return expenseDate.get(); }
        public String getCategory() { return category.get(); }
        public String getDescription() { return description.get(); }
        public String getAmount() { return amount.get(); }
        public String getPaidBy() { return paidBy.get(); }
    }

    public static class DailySheetRow {
        private final SimpleStringProperty roomNo;
        private final SimpleStringProperty date;
        private final SimpleStringProperty occupied;
        private final SimpleStringProperty receivedAmount;
        private final SimpleStringProperty pendingAmount;
        private final SimpleStringProperty total;

        public DailySheetRow(String roomNo, String date, String occupied,
                           String receivedAmount, String pendingAmount, String total) {
            this.roomNo = new SimpleStringProperty(roomNo);
            this.date = new SimpleStringProperty(date);
            this.occupied = new SimpleStringProperty(occupied);
            this.receivedAmount = new SimpleStringProperty(receivedAmount);
            this.pendingAmount = new SimpleStringProperty(pendingAmount);
            this.total = new SimpleStringProperty(total);
        }

        // Property methods
        public SimpleStringProperty roomNoProperty() { return roomNo; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty occupiedProperty() { return occupied; }
        public SimpleStringProperty receivedAmountProperty() { return receivedAmount; }
        public SimpleStringProperty pendingAmountProperty() { return pendingAmount; }
        public SimpleStringProperty totalProperty() { return total; }

        // Getters
        public String getRoomNo() { return roomNo.get(); }
        public String getDate() { return date.get(); }
        public String getOccupied() { return occupied.get(); }
        public String getReceivedAmount() { return receivedAmount.get(); }
        public String getPendingAmount() { return pendingAmount.get(); }
        public String getTotal() { return total.get(); }
    }

    // ===== NEW: DAILY SHEET DETAIL ROW MODEL =====
    public static class DailySheetDetailRow {
        private final SimpleStringProperty roomNo;
        private final SimpleStringProperty roomStatus;
        private final SimpleStringProperty guestName;
        private final SimpleStringProperty checkIn;
        private final SimpleStringProperty checkOut;
        private final SimpleStringProperty todayAmount;
        private final SimpleStringProperty todayPayment;
        private final SimpleStringProperty pendingAmount;
        private final SimpleStringProperty totalAmount;
        private final SimpleStringProperty paymentMethods; // NEW

        public DailySheetDetailRow(String roomNo, String roomStatus, String guestName,
                                 String checkIn, String checkOut, String todayAmount,
                                 String todayPayment, String pendingAmount, String totalAmount,
                                 String paymentMethods) { // NEW parameter
            this.roomNo = new SimpleStringProperty(roomNo);
            this.roomStatus = new SimpleStringProperty(roomStatus);
            this.guestName = new SimpleStringProperty(guestName);
            this.checkIn = new SimpleStringProperty(checkIn);
            this.checkOut = new SimpleStringProperty(checkOut);
            this.todayAmount = new SimpleStringProperty(todayAmount);
            this.todayPayment = new SimpleStringProperty(todayPayment);
            this.pendingAmount = new SimpleStringProperty(pendingAmount);
            this.totalAmount = new SimpleStringProperty(totalAmount);
            this.paymentMethods = new SimpleStringProperty(paymentMethods); // NEW
        }

        // Property methods
        public SimpleStringProperty roomNoProperty() { return roomNo; }
        public SimpleStringProperty roomStatusProperty() { return roomStatus; }
        public SimpleStringProperty guestNameProperty() { return guestName; }
        public SimpleStringProperty checkInProperty() { return checkIn; }
        public SimpleStringProperty checkOutProperty() { return checkOut; }
        public SimpleStringProperty todayAmountProperty() { return todayAmount; }
        public SimpleStringProperty todayPaymentProperty() { return todayPayment; }
        public SimpleStringProperty pendingAmountProperty() { return pendingAmount; }
        public SimpleStringProperty totalAmountProperty() { return totalAmount; }
        public SimpleStringProperty paymentMethodsProperty() { return paymentMethods; } // NEW

        // Getters
        public String getRoomNo() { return roomNo.get(); }
        public String getRoomStatus() { return roomStatus.get(); }
        public String getGuestName() { return guestName.get(); }
        public String getCheckIn() { return checkIn.get(); }
        public String getCheckOut() { return checkOut.get(); }
        public String getTodayAmount() { return todayAmount.get(); }
        public String getTodayPayment() { return todayPayment.get(); }
        public String getPendingAmount() { return pendingAmount.get(); }
        public String getTotalAmount() { return totalAmount.get(); }
        public String getPaymentMethods() { return paymentMethods.get(); } // NEW
    }

}
