package application.utils.inventory;

import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for validating form inputs and data
 */
public class ValidationUtils {

    // Common regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );
    
    private static final Pattern GST_PATTERN = Pattern.compile(
        "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"
    );
    
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9\\s]+$"
    );

    /**
     * Validates if a string is not null and not empty after trimming
     * @param value String to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates if a string has minimum length
     * @param value String to validate
     * @param minLength Minimum required length
     * @return true if valid, false otherwise
     */
    public static boolean hasMinLength(String value, int minLength) {
        return value != null && value.trim().length() >= minLength;
    }

    /**
     * Validates if a string has maximum length
     * @param value String to validate
     * @param maxLength Maximum allowed length
     * @return true if valid, false otherwise
     */
    public static boolean hasMaxLength(String value, int maxLength) {
        return value == null || value.trim().length() <= maxLength;
    }

    /**
     * Validates if a string length is within range
     * @param value String to validate
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if valid, false otherwise
     */
    public static boolean isLengthInRange(String value, int minLength, int maxLength) {
        return hasMinLength(value, minLength) && hasMaxLength(value, maxLength);
    }

    /**
     * Validates email format
     * @param email Email to validate
     * @return true if valid email format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates phone number format
     * @param phone Phone number to validate
     * @return true if valid phone format, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleanPhone = phone.replaceAll("[\\s()-]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validates GST number format (Indian GST)
     * @param gstNumber GST number to validate
     * @return true if valid GST format, false otherwise
     */
    public static boolean isValidGST(String gstNumber) {
        return gstNumber != null && GST_PATTERN.matcher(gstNumber.trim().toUpperCase()).matches();
    }

    /**
     * Validates if string contains only alphanumeric characters and spaces
     * @param value String to validate
     * @return true if valid, false otherwise
     */
    public static boolean isAlphaNumeric(String value) {
        return value != null && ALPHANUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * Validates if a number is positive
     * @param value Number to validate
     * @return true if positive, false otherwise
     */
    public static boolean isPositive(Number value) {
        return value != null && value.doubleValue() > 0;
    }

    /**
     * Validates if a number is non-negative (zero or positive)
     * @param value Number to validate
     * @return true if non-negative, false otherwise
     */
    public static boolean isNonNegative(Number value) {
        return value != null && value.doubleValue() >= 0;
    }

    /**
     * Validates if an integer is within range
     * @param value Integer to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if in range, false otherwise
     */
    public static boolean isInRange(Integer value, int min, int max) {
        return value != null && value >= min && value <= max;
    }

    /**
     * Validates if a BigDecimal is within range
     * @param value BigDecimal to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if in range, false otherwise
     */
    public static boolean isInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value != null && value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    /**
     * Validates if a date is not in the past
     * @param date Date to validate
     * @return true if date is today or future, false otherwise
     */
    public static boolean isNotPastDate(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    /**
     * Validates if a date is not in the future
     * @param date Date to validate
     * @return true if date is today or past, false otherwise
     */
    public static boolean isNotFutureDate(LocalDate date) {
        return date != null && !date.isAfter(LocalDate.now());
    }

    /**
     * Validates if end date is after start date
     * @param startDate Start date
     * @param endDate End date
     * @return true if end date is after start date, false otherwise
     */
    public static boolean isEndDateAfterStartDate(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null && endDate.isAfter(startDate);
    }

    /**
     * Validates TextField for required input
     * @param textField TextField to validate
     * @param fieldName Name of the field for error messages
     * @param errors List to add error messages to
     */
    public static void validateRequired(TextField textField, String fieldName, List<String> errors) {
        if (!isNotEmpty(textField.getText())) {
            errors.add(fieldName + " is required");
            addErrorStyle(textField);
        } else {
            removeErrorStyle(textField);
        }
    }

    /**
     * Validates TextField for required input with minimum length
     * @param textField TextField to validate
     * @param fieldName Name of the field for error messages
     * @param minLength Minimum required length
     * @param errors List to add error messages to
     */
    public static void validateRequired(TextField textField, String fieldName, int minLength, List<String> errors) {
        String value = textField.getText();
        if (!isNotEmpty(value)) {
            errors.add(fieldName + " is required");
            addErrorStyle(textField);
        } else if (!hasMinLength(value, minLength)) {
            errors.add(fieldName + " must be at least " + minLength + " characters");
            addErrorStyle(textField);
        } else {
            removeErrorStyle(textField);
        }
    }

    /**
     * Validates email TextField
     * @param textField TextField containing email
     * @param fieldName Name of the field for error messages
     * @param required Whether email is required
     * @param errors List to add error messages to
     */
    public static void validateEmail(TextField textField, String fieldName, boolean required, List<String> errors) {
        String value = textField.getText();
        if (required && !isNotEmpty(value)) {
            errors.add(fieldName + " is required");
            addErrorStyle(textField);
        } else if (isNotEmpty(value) && !isValidEmail(value)) {
            errors.add(fieldName + " must be a valid email address");
            addErrorStyle(textField);
        } else {
            removeErrorStyle(textField);
        }
    }

    /**
     * Validates phone TextField
     * @param textField TextField containing phone number
     * @param fieldName Name of the field for error messages
     * @param required Whether phone is required
     * @param errors List to add error messages to
     */
    public static void validatePhone(TextField textField, String fieldName, boolean required, List<String> errors) {
        String value = textField.getText();
        if (required && !isNotEmpty(value)) {
            errors.add(fieldName + " is required");
            addErrorStyle(textField);
        } else if (isNotEmpty(value) && !isValidPhone(value)) {
            errors.add(fieldName + " must be a valid phone number");
            addErrorStyle(textField);
        } else {
            removeErrorStyle(textField);
        }
    }

    /**
     * Validates numeric TextField for positive numbers
     * @param textField TextField containing numeric value
     * @param fieldName Name of the field for error messages
     * @param required Whether value is required
     * @param errors List to add error messages to
     * @return Parsed number or null if invalid
     */
    public static BigDecimal validatePositiveDecimal(TextField textField, String fieldName, boolean required, List<String> errors) {
        String value = textField.getText();
        if (required && !isNotEmpty(value)) {
            errors.add(fieldName + " is required");
            addErrorStyle(textField);
            return null;
        }
        
        if (isNotEmpty(value)) {
            try {
                BigDecimal number = new BigDecimal(value);
                if (!isPositive(number)) {
                    errors.add(fieldName + " must be greater than 0");
                    addErrorStyle(textField);
                    return null;
                }
                removeErrorStyle(textField);
                return number;
            } catch (NumberFormatException e) {
                errors.add(fieldName + " must be a valid number");
                addErrorStyle(textField);
                return null;
            }
        }
        
        removeErrorStyle(textField);
        return null;
    }

    /**
     * Validates integer TextField for positive numbers
     * @param textField TextField containing integer value
     * @param fieldName Name of the field for error messages
     * @param required Whether value is required
     * @param errors List to add error messages to
     * @return Parsed integer or null if invalid
     */
    public static Integer validatePositiveInteger(TextField textField, String fieldName, boolean required, List<String> errors) {
        String value = textField.getText();
        if (required && !isNotEmpty(value)) {
            errors.add(fieldName + " is required");
            addErrorStyle(textField);
            return null;
        }
        
        if (isNotEmpty(value)) {
            try {
                Integer number = Integer.parseInt(value);
                if (!isPositive(number)) {
                    errors.add(fieldName + " must be greater than 0");
                    addErrorStyle(textField);
                    return null;
                }
                removeErrorStyle(textField);
                return number;
            } catch (NumberFormatException e) {
                errors.add(fieldName + " must be a valid integer");
                addErrorStyle(textField);
                return null;
            }
        }
        
        removeErrorStyle(textField);
        return null;
    }

    /**
     * Validates ComboBox for selection
     * @param comboBox ComboBox to validate
     * @param fieldName Name of the field for error messages
     * @param errors List to add error messages to
     */
    public static void validateSelection(ComboBox<?> comboBox, String fieldName, List<String> errors) {
        if (comboBox.getSelectionModel().getSelectedItem() == null) {
            errors.add("Please select " + fieldName);
            addErrorStyle(comboBox);
        } else {
            removeErrorStyle(comboBox);
        }
    }

    /**
     * Validates DatePicker for selection
     * @param datePicker DatePicker to validate
     * @param fieldName Name of the field for error messages
     * @param errors List to add error messages to
     */
    public static void validateDateSelection(DatePicker datePicker, String fieldName, List<String> errors) {
        if (datePicker.getValue() == null) {
            errors.add("Please select " + fieldName);
            addErrorStyle(datePicker);
        } else {
            removeErrorStyle(datePicker);
        }
    }

    /**
     * Validates that end date is after start date
     * @param startDatePicker Start date picker
     * @param endDatePicker End date picker
     * @param errors List to add error messages to
     */
    public static void validateDateRange(DatePicker startDatePicker, DatePicker endDatePicker, List<String> errors) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate != null && endDate != null) {
            if (!isEndDateAfterStartDate(startDate, endDate)) {
                errors.add("End date must be after start date");
                addErrorStyle(endDatePicker);
            } else {
                removeErrorStyle(endDatePicker);
            }
        }
    }

    /**
     * Adds error styling to a control
     * @param control Control to add error style to
     */
    public static void addErrorStyle(javafx.scene.Node control) {
        if (!control.getStyleClass().contains("error-field")) {
            control.getStyleClass().add("error-field");
        }
    }

    /**
     * Removes error styling from a control
     * @param control Control to remove error style from
     */
    public static void removeErrorStyle(javafx.scene.Node control) {
        control.getStyleClass().remove("error-field");
    }

    /**
     * Clears error styling from all controls in a list
     * @param controls List of controls to clear error styling from
     */
    public static void clearErrorStyles(List<javafx.scene.Node> controls) {
        for (javafx.scene.Node control : controls) {
            removeErrorStyle(control);
        }
    }

    /**
     * Validates inventory item data
     * @param itemName Item name field
     * @param category Category selection
     * @param location Location field
     * @param availableQty Available quantity field
     * @param minStock Minimum stock field
     * @param unitPrice Unit price field
     * @return List of validation errors
     */
    public static List<String> validateInventoryItem(TextField itemName, ComboBox<String> category, 
            TextField location, TextField availableQty, TextField minStock, TextField unitPrice) {
        List<String> errors = new ArrayList<>();
        
        validateRequired(itemName, "Item Name", 2, errors);
        validateSelection(category, "Category", errors);
        validateRequired(location, "Location", errors);
        validatePositiveInteger(availableQty, "Available Quantity", false, errors);
        validatePositiveInteger(minStock, "Minimum Stock Level", true, errors);
        validatePositiveDecimal(unitPrice, "Unit Price", true, errors);
        
        return errors;
    }

    /**
     * Validates vendor data
     * @param vendorName Vendor name field
     * @param contactPerson Contact person field
     * @param phone Phone field
     * @param email Email field
     * @return List of validation errors
     */
    public static List<String> validateVendor(TextField vendorName, TextField contactPerson, 
            TextField phone, TextField email) {
        List<String> errors = new ArrayList<>();
        
        validateRequired(vendorName, "Vendor Name", 2, errors);
        validateRequired(contactPerson, "Contact Person", errors);
        validatePhone(phone, "Phone Number", true, errors);
        validateEmail(email, "Email", false, errors);
        
        return errors;
    }

    /**
     * Validates room assignment data
     * @param roomNo Room number selection
     * @param item Item selection
     * @param quantity Quantity field
     * @return List of validation errors
     */
    public static List<String> validateRoomAssignment(ComboBox<String> roomNo, ComboBox<?> item, TextField quantity) {
        List<String> errors = new ArrayList<>();
        
        validateSelection(roomNo, "Room Number", errors);
        validateSelection(item, "Item", errors);
        validatePositiveInteger(quantity, "Quantity", true, errors);
        
        return errors;
    }

    /**
     * Validates purchase order data
     * @param vendor Vendor selection
     * @param orderDate Order date picker
     * @param expectedDate Expected delivery date picker
     * @return List of validation errors
     */
    public static List<String> validatePurchaseOrder(ComboBox<?> vendor, DatePicker orderDate, DatePicker expectedDate) {
        List<String> errors = new ArrayList<>();
        
        validateSelection(vendor, "Vendor", errors);
        validateDateSelection(orderDate, "Order Date", errors);
        
        if (expectedDate.getValue() != null && orderDate.getValue() != null) {
            validateDateRange(orderDate, expectedDate, errors);
        }
        
        return errors;
    }

    /**
     * Utility method to check if any validation errors exist and show them
     * @param errors List of validation errors
     * @return true if there are no errors, false if there are errors
     */
    public static boolean isValid(List<String> errors) {
        if (errors.isEmpty()) {
            return true;
        } else {
            AlertUtils.showValidationErrors(errors);
            return false;
        }
    }

    /**
     * Validates SKU format (alphanumeric, 6-20 characters)
     * @param sku SKU to validate
     * @return true if valid SKU format, false otherwise
     */
    public static boolean isValidSKU(String sku) {
        return sku != null && sku.matches("^[A-Z0-9]{6,20}$");
    }

    /**
     * Validates PAN number format (Indian PAN)
     * @param pan PAN number to validate
     * @return true if valid PAN format, false otherwise
     */
    public static boolean isValidPAN(String pan) {
        return pan != null && pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
    }

    /**
     * Validates Aadhar number format (Indian Aadhar)
     * @param aadhar Aadhar number to validate
     * @return true if valid Aadhar format, false otherwise
     */
    public static boolean isValidAadhar(String aadhar) {
        if (aadhar == null) return false;
        String cleanAadhar = aadhar.replaceAll("[\\s-]", "");
        return cleanAadhar.matches("^[0-9]{12}$");
    }
}
