package application.services.store;



import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Enhanced PropertyReader with support for invoice settings and all application properties
 * Supports both bundled resources and external configuration files
 */
public class PropertyReader {
    private static final Logger LOGGER = Logger.getLogger(PropertyReader.class.getName());
    private static PropertyReader instance;
    
    private Properties properties;
    private Path externalPropertiesPath;
    
    // Property file names
    private static final String MAIN_PROPERTIES = "application.properties";
    private static final String NATIONALITY_PROPERTIES = "nationality.properties";
    
    private PropertyReader() {
        properties = new Properties();
        
        // Set external properties path in user home
        String userHome = System.getProperty("user.home");
        externalPropertiesPath = Paths.get(userHome, ".hotelapp", MAIN_PROPERTIES);
        
        loadProperties();
    }
    
    public static PropertyReader getInstance() {
        if (instance == null) {
            synchronized (PropertyReader.class) {
                if (instance == null) {
                    instance = new PropertyReader();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load properties from bundled resources and external file
     */
    private void loadProperties() {
        // Load default properties from bundled resources
        loadBundledProperties();
        
        // Override with external properties if exists
        loadExternalProperties();
        
        // Ensure default values exist
        setDefaultValues();
    }
    
    /**
     * Load properties from bundled resources
     */
    private void loadBundledProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(MAIN_PROPERTIES)) {
            if (input != null) {
                properties.load(input);
                LOGGER.info("Loaded bundled properties from " + MAIN_PROPERTIES);
            }
        } catch (IOException e) {
            LOGGER.warning("Could not load bundled properties: " + e.getMessage());
        }
        
        // Also load nationality properties
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(NATIONALITY_PROPERTIES)) {
            if (input != null) {
                properties.load(input);
                LOGGER.info("Loaded nationality properties");
            }
        } catch (IOException e) {
            LOGGER.warning("Could not load nationality properties: " + e.getMessage());
        }
    }
    
    /**
     * Load properties from external file (user's home directory)
     */
    private void loadExternalProperties() {
        if (Files.exists(externalPropertiesPath)) {
            try (InputStream input = Files.newInputStream(externalPropertiesPath)) {
                properties.load(input);
                LOGGER.info("Loaded external properties from " + externalPropertiesPath);
            } catch (IOException e) {
                LOGGER.warning("Could not load external properties: " + e.getMessage());
            }
        }
    }
    
    /**
     * Set default values for all properties
     */
    private void setDefaultValues() {
        // Nationality defaults
        setPropertyIfAbsent("default.nationality", "India");
        
        // GST defaults
        setPropertyIfAbsent("gst.rate", "18.0");
        setPropertyIfAbsent("gst.cgst.rate", "9.0");
        setPropertyIfAbsent("gst.sgst.rate", "9.0");
        
        // Hotel Information
        setPropertyIfAbsent("hotel.name", "THE GRAND HOTEL");
        setPropertyIfAbsent("hotel.address", "123 Luxury Avenue, Business District, Mumbai - 400001");
        setPropertyIfAbsent("hotel.phone", "+91 22 2345 6789");
        setPropertyIfAbsent("hotel.email", "info@thegrandhotel.com");
        setPropertyIfAbsent("hotel.gst", "27AABCH1234C1Z5");
        setPropertyIfAbsent("hotel.website", "www.thegrandhotel.com");
        
        // Bank Details
        setPropertyIfAbsent("bank.name", "State Bank of India");
        setPropertyIfAbsent("bank.account", "1234567890");
        setPropertyIfAbsent("bank.ifsc", "SBIN0001234");
        setPropertyIfAbsent("bank.branch", "Mumbai Main Branch");
        
        // Invoice Settings - Size Preset
        setPropertyIfAbsent("invoice.size.preset", "STANDARD");
        
        // Invoice Settings - Page Dimensions
        setPropertyIfAbsent("invoice.page.width", "595");
        setPropertyIfAbsent("invoice.page.height", "842");
        
        // Invoice Settings - Margins
        setPropertyIfAbsent("invoice.margin.top", "36");
        setPropertyIfAbsent("invoice.margin.right", "36");
        setPropertyIfAbsent("invoice.margin.bottom", "36");
        setPropertyIfAbsent("invoice.margin.left", "36");
        
        // Invoice Settings - Padding
        setPropertyIfAbsent("invoice.padding.top", "20");
        setPropertyIfAbsent("invoice.padding.right", "20");
        setPropertyIfAbsent("invoice.padding.bottom", "20");
        setPropertyIfAbsent("invoice.padding.left", "20");
        
        // Invoice Settings - Font Sizes
        setPropertyIfAbsent("invoice.font.hotel.name", "20");
        setPropertyIfAbsent("invoice.font.title", "16");
        setPropertyIfAbsent("invoice.font.section", "12");
        setPropertyIfAbsent("invoice.font.normal", "10");
        setPropertyIfAbsent("invoice.font.table.header", "10");
        setPropertyIfAbsent("invoice.font.table.content", "9");
        
        // Invoice Settings - Display Options
        setPropertyIfAbsent("invoice.show.logo", "true");
        setPropertyIfAbsent("invoice.show.address", "true");
        setPropertyIfAbsent("invoice.show.contact", "true");
        setPropertyIfAbsent("invoice.show.gst", "true");
        setPropertyIfAbsent("invoice.show.bankdetails", "true");
        setPropertyIfAbsent("invoice.show.thankyou", "true");
        setPropertyIfAbsent("invoice.show.signature", "true");
        setPropertyIfAbsent("invoice.show.taxbreakdown", "true");
        setPropertyIfAbsent("invoice.show.amountwords", "true");
        
        // Page Access Settings
        setPropertyIfAbsent("page.access.HOUSEKEEPING", "true");
        setPropertyIfAbsent("page.access.SERVICES", "true");
        setPropertyIfAbsent("page.access.EXPENSES", "true");
    }
    
    /**
     * Set property only if it doesn't already exist
     */
    private void setPropertyIfAbsent(String key, String value) {
        if (properties.getProperty(key) == null) {
            properties.setProperty(key, value);
        }
    }
    
    // ========== GETTER METHODS ==========
    
    /**
     * Get property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get property with default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get boolean property
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Get integer property
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid integer property " + key + ": " + value);
            return defaultValue;
        }
    }
    
    /**
     * Get double property
     */
    public double getDoubleProperty(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid double property " + key + ": " + value);
            return defaultValue;
        }
    }
    
    // ========== NATIONALITY METHODS ==========
    
    public String getDefaultNationality() {
        return properties.getProperty("default.nationality", "India");
    }
    
    public String[] getAllNationalities() {
        return new String[]{
            "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", 
            "Antigua and Barbuda", "Argentina", "Armenia", "Australia", "Austria", 
            "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", 
            "Belarus", "Belgium", "Belize", "Benin", "Bhutan", 
            "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", 
            "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", 
            "Canada", "Cape Verde", "Central African Republic", "Chad", "Chile", 
            "China", "Colombia", "Comoros", "Congo (Democratic Republic)", "Congo (Republic)", 
            "Costa Rica", "Côte d'Ivoire", "Croatia", "Cuba", "Cyprus", 
            "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", 
            "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", 
            "Estonia", "Eswatini", "Ethiopia", "Fiji", "Finland", 
            "France", "Gabon", "Gambia", "Georgia", "Germany", 
            "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", 
            "Guinea-Bissau", "Guyana", "Haiti", "Honduras", "Hungary", 
            "Iceland", "India", "Indonesia", "Iran", "Iraq", 
            "Ireland", "Israel", "Italy", "Jamaica", "Japan", 
            "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kosovo", 
            "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", 
            "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", 
            "Luxembourg", "Madagascar", "Malawi", "Malaysia", "Maldives", 
            "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", 
            "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia", 
            "Montenegro", "Morocco", "Mozambique", "Myanmar", "Namibia", 
            "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", 
            "Niger", "Nigeria", "North Korea", "North Macedonia", "Norway", 
            "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", 
            "Paraguay", "Peru", "Philippines", "Poland", "Portugal", 
            "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", 
            "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", 
            "São Tomé and Príncipe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", 
            "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", 
            "South Africa", "South Korea", "South Sudan", "Spain", "Sri Lanka", 
            "Sudan", "Suriname", "Sweden", "Switzerland", "Syria", 
            "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", 
            "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", 
            "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", 
            "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", 
            "Vatican City", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"
        };
    }
    
    // ========== GST METHODS ==========
    
    public double getGstRate() {
        return getDoubleProperty("gst.rate", 18.0);
    }
    
    public double getCgstRate() {
        return getDoubleProperty("gst.cgst.rate", 9.0);
    }
    
    public double getSgstRate() {
        return getDoubleProperty("gst.sgst.rate", 9.0);
    }
    
    // ========== SETTER METHODS ==========
    
    /**
     * Set property value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Set boolean property
     */
    public void setBooleanProperty(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
    }
    
    /**
     * Set integer property
     */
    public void setIntProperty(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }
    
    /**
     * Set double property
     */
    public void setDoubleProperty(String key, double value) {
        properties.setProperty(key, String.valueOf(value));
    }
    
    // ========== SAVE METHODS ==========
    
    /**
     * Save properties to external file
     */
    public void saveProperties() throws IOException {
        // Create directory if it doesn't exist
        Files.createDirectories(externalPropertiesPath.getParent());
        
        // Save properties to external file
        try (OutputStream output = Files.newOutputStream(externalPropertiesPath)) {
            properties.store(output, "Hotel Management System Configuration");
            LOGGER.info("Saved properties to " + externalPropertiesPath);
        }
    }
    
    /**
     * Reload properties from files
     */
    public void reload() {
        properties.clear();
        loadProperties();
    }
    
    /**
     * Get all properties (for debugging or export)
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
}