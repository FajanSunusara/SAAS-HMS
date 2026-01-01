package application.models;



import javafx.geometry.Insets;

/**
 * Settings model for invoice printing and display configuration
 * All settings controllable from Settings page
 */
public class InvoiceSettings {
    
    // Page dimensions (in points - 1 inch = 72 points)
    private double pageWidth = 595;  // A4 width in points (210mm)
    private double pageHeight = 842; // A4 height in points (297mm)
    
    // Margins (in points)
    private double marginTop = 36;    // 0.5 inch
    private double marginRight = 36;
    private double marginBottom = 36;
    private double marginLeft = 36;
    
    // Content padding
    private double paddingTop = 20;
    private double paddingRight = 20;
    private double paddingBottom = 20;
    private double paddingLeft = 20;
    
    // Font sizes
    private double hotelNameFontSize = 20;
    private double invoiceTitleFontSize = 16;
    private double sectionTitleFontSize = 12;
    private double normalTextFontSize = 10;
    private double tableHeaderFontSize = 10;
    private double tableContentFontSize = 9;
    
    // Logo settings
    private boolean showLogo = true;
    private double logoWidth = 80;
    private double logoHeight = 80;
    
    // Header settings
    private boolean showHotelAddress = true;
    private boolean showHotelContact = true;
    private boolean showHotelGst = true;
    
    // Footer settings
    private boolean showBankDetails = true;
    private boolean showThankYouMessage = true;
    private boolean showSignature = true;
    
    // Table settings
    private boolean showTableBorders = true;
    private boolean alternateRowColors = true;
    
    // Invoice-specific settings
    private boolean autoGenerateInvoiceNumber = true;
    private boolean includeTaxBreakdown = true;
    private boolean showAmountInWords = true;
    
    // Constructors
    public InvoiceSettings() {
    }
    
    // Preset configurations
    public static InvoiceSettings createCompact() {
        InvoiceSettings settings = new InvoiceSettings();
        settings.pageWidth = 595;
        settings.pageHeight = 750;
        settings.marginTop = 20;
        settings.marginBottom = 20;
        settings.marginLeft = 15;
        settings.marginRight = 15;
        settings.paddingTop = 15;
        settings.paddingBottom = 15;
        settings.paddingLeft = 15;
        settings.paddingRight = 15;
        settings.hotelNameFontSize = 16;
        settings.invoiceTitleFontSize = 14;
        settings.sectionTitleFontSize = 11;
        settings.normalTextFontSize = 9;
        return settings;
    }
    
    public static InvoiceSettings createStandard() {
        return new InvoiceSettings(); // Uses default values
    }
    
    public static InvoiceSettings createLarge() {
        InvoiceSettings settings = new InvoiceSettings();
        settings.pageWidth = 700;
        settings.pageHeight = 950;
        settings.marginTop = 40;
        settings.marginBottom = 40;
        settings.marginLeft = 40;
        settings.marginRight = 40;
        settings.paddingTop = 25;
        settings.paddingBottom = 25;
        settings.paddingLeft = 25;
        settings.paddingRight = 25;
        settings.hotelNameFontSize = 24;
        settings.invoiceTitleFontSize = 18;
        settings.sectionTitleFontSize = 14;
        settings.normalTextFontSize = 12;
        return settings;
    }
    
    // Getters and Setters
    public double getPageWidth() { return pageWidth; }
    public void setPageWidth(double pageWidth) { this.pageWidth = pageWidth; }
    
    public double getPageHeight() { return pageHeight; }
    public void setPageHeight(double pageHeight) { this.pageHeight = pageHeight; }
    
    public double getMarginTop() { return marginTop; }
    public void setMarginTop(double marginTop) { this.marginTop = marginTop; }
    
    public double getMarginRight() { return marginRight; }
    public void setMarginRight(double marginRight) { this.marginRight = marginRight; }
    
    public double getMarginBottom() { return marginBottom; }
    public void setMarginBottom(double marginBottom) { this.marginBottom = marginBottom; }
    
    public double getMarginLeft() { return marginLeft; }
    public void setMarginLeft(double marginLeft) { this.marginLeft = marginLeft; }
    
    public double getPaddingTop() { return paddingTop; }
    public void setPaddingTop(double paddingTop) { this.paddingTop = paddingTop; }
    
    public double getPaddingRight() { return paddingRight; }
    public void setPaddingRight(double paddingRight) { this.paddingRight = paddingRight; }
    
    public double getPaddingBottom() { return paddingBottom; }
    public void setPaddingBottom(double paddingBottom) { this.paddingBottom = paddingBottom; }
    
    public double getPaddingLeft() { return paddingLeft; }
    public void setPaddingLeft(double paddingLeft) { this.paddingLeft = paddingLeft; }
    
    public Insets toInsets() {
        return new Insets(paddingTop, paddingRight, paddingBottom, paddingLeft);
    }
    
    public double getContentWidth() {
        return pageWidth - paddingLeft - paddingRight;
    }
    
    public double getContentHeight() {
        return pageHeight - paddingTop - paddingBottom;
    }
    
    // All other getters and setters
    public double getHotelNameFontSize() { return hotelNameFontSize; }
    public void setHotelNameFontSize(double hotelNameFontSize) { this.hotelNameFontSize = hotelNameFontSize; }
    
    public double getInvoiceTitleFontSize() { return invoiceTitleFontSize; }
    public void setInvoiceTitleFontSize(double invoiceTitleFontSize) { this.invoiceTitleFontSize = invoiceTitleFontSize; }
    
    public double getSectionTitleFontSize() { return sectionTitleFontSize; }
    public void setSectionTitleFontSize(double sectionTitleFontSize) { this.sectionTitleFontSize = sectionTitleFontSize; }
    
    public double getNormalTextFontSize() { return normalTextFontSize; }
    public void setNormalTextFontSize(double normalTextFontSize) { this.normalTextFontSize = normalTextFontSize; }
    
    public double getTableHeaderFontSize() { return tableHeaderFontSize; }
    public void setTableHeaderFontSize(double tableHeaderFontSize) { this.tableHeaderFontSize = tableHeaderFontSize; }
    
    public double getTableContentFontSize() { return tableContentFontSize; }
    public void setTableContentFontSize(double tableContentFontSize) { this.tableContentFontSize = tableContentFontSize; }
    
    public boolean isShowLogo() { return showLogo; }
    public void setShowLogo(boolean showLogo) { this.showLogo = showLogo; }
    
    public double getLogoWidth() { return logoWidth; }
    public void setLogoWidth(double logoWidth) { this.logoWidth = logoWidth; }
    
    public double getLogoHeight() { return logoHeight; }
    public void setLogoHeight(double logoHeight) { this.logoHeight = logoHeight; }
    
    public boolean isShowHotelAddress() { return showHotelAddress; }
    public void setShowHotelAddress(boolean showHotelAddress) { this.showHotelAddress = showHotelAddress; }
    
    public boolean isShowHotelContact() { return showHotelContact; }
    public void setShowHotelContact(boolean showHotelContact) { this.showHotelContact = showHotelContact; }
    
    public boolean isShowHotelGst() { return showHotelGst; }
    public void setShowHotelGst(boolean showHotelGst) { this.showHotelGst = showHotelGst; }
    
    public boolean isShowBankDetails() { return showBankDetails; }
    public void setShowBankDetails(boolean showBankDetails) { this.showBankDetails = showBankDetails; }
    
    public boolean isShowThankYouMessage() { return showThankYouMessage; }
    public void setShowThankYouMessage(boolean showThankYouMessage) { this.showThankYouMessage = showThankYouMessage; }
    
    public boolean isShowSignature() { return showSignature; }
    public void setShowSignature(boolean showSignature) { this.showSignature = showSignature; }
    
    public boolean isShowTableBorders() { return showTableBorders; }
    public void setShowTableBorders(boolean showTableBorders) { this.showTableBorders = showTableBorders; }
    
    public boolean isAlternateRowColors() { return alternateRowColors; }
    public void setAlternateRowColors(boolean alternateRowColors) { this.alternateRowColors = alternateRowColors; }
    
    public boolean isAutoGenerateInvoiceNumber() { return autoGenerateInvoiceNumber; }
    public void setAutoGenerateInvoiceNumber(boolean autoGenerateInvoiceNumber) { this.autoGenerateInvoiceNumber = autoGenerateInvoiceNumber; }
    
    public boolean isIncludeTaxBreakdown() { return includeTaxBreakdown; }
    public void setIncludeTaxBreakdown(boolean includeTaxBreakdown) { this.includeTaxBreakdown = includeTaxBreakdown; }
    
    public boolean isShowAmountInWords() { return showAmountInWords; }
    public void setShowAmountInWords(boolean showAmountInWords) { this.showAmountInWords = showAmountInWords; }
}