package application.utils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PropertyReader {
    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".hotelapp";
    private static final String CONFIG_PATH = CONFIG_DIR + File.separator + CONFIG_FILE;
    
    private static PropertyReader instance;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private Properties properties = new Properties();
    private ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private static final String SIDEBAR_ACCESS_PREFIX = "sidebar.access.";
    private PropertyReader() {
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
    
    private void loadProperties() {
        lock.writeLock().lock();
        try {
            // Create config directory if it doesn't exist
            Path configDirPath = Paths.get(CONFIG_DIR);
            if (!Files.exists(configDirPath)) {
                Files.createDirectories(configDirPath);
            }
            
            // Create default config if it doesn't exist
            Path configFilePath = Paths.get(CONFIG_PATH);
            if (!Files.exists(configFilePath)) {
                createDefaultConfig();
            }
            
            // Load properties
            try (InputStream input = new FileInputStream(CONFIG_PATH)) {
                properties.load(input);
                // Update cache
                cache.clear();
                properties.forEach((key, value) -> cache.put(key.toString(), value.toString()));
            }
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
            setDefaultValues();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }


    
    private void createDefaultConfig() throws IOException {
        properties.setProperty("gst.rate", "18");
        properties.setProperty("currency", "INR");
        properties.setProperty("default.nationality", "India");
        saveProperties();
    }
    
    
    private void setDefaultValues() {
        properties.setProperty("gst.rate", "18");
        properties.setProperty("currency", "INR");
        properties.setProperty("default.nationality", "India");
        cache.put("gst.rate", "18");
        cache.put("currency", "INR");
        cache.put("default.nationality", "India");
    }
    
    public void saveProperties() {
        lock.writeLock().lock();
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {
            properties.store(output, "Hotel Management System Configuration");
            // Update cache
            cache.clear();
            properties.forEach((key, value) -> cache.put(key.toString(), value.toString()));
            properties.store(output, "Updated settings");
        } catch (IOException e) {
            System.err.println("Error saving properties: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
 // Check if sidebar item is enabled, default true if not set
    public boolean isSidebarItemEnabled(String itemName) {
        return Boolean.parseBoolean(get(SIDEBAR_ACCESS_PREFIX + itemName, "true"));
    }

    // Enable or disable sidebar item and save property
    public void setSidebarItemEnabled(String itemName, boolean enabled) {
        set(SIDEBAR_ACCESS_PREFIX + itemName, Boolean.toString(enabled));
    }
    
    public String get(String key) {
        return get(key, "");
    }
    
    public String get(String key, String defaultValue) {
        lock.readLock().lock();
        try {
            return cache.getOrDefault(key, defaultValue);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public double getGstRate() {
        try {
            return Double.parseDouble(get("gst.rate", "18"));
        } catch (NumberFormatException e) {
            return 18.0;
        }
    }
    
    public String getDefaultNationality() {
        return get("default.nationality", "India");
    }
    
    public void set(String key, String value) {
        lock.writeLock().lock();
        try {
            properties.setProperty(key, value);
            cache.put(key, value);
            saveProperties();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void setGstRate(double gstRate) {
        if (gstRate >= 0 && gstRate <= 100) {
            set("gst.rate", String.valueOf(gstRate));
        }
    }
    
    public void reload() {
        loadProperties();
    }
    
    public boolean isValidGstRate(double gstRate) {
        return gstRate >= 0 && gstRate <= 100;
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
            "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "São Tomé and Príncipe", 
            "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", 
            "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", 
            "South Africa", "South Korea", "South Sudan", "Spain", "Sri Lanka", 
            "Sudan", "Suriname", "Sweden", "Switzerland", "Syria", 
            "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", 
            "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", 
            "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", 
            "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", 
            "Vatican City", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"
        };
    }
}
