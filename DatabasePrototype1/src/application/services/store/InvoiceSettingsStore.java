package application.services.store;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class InvoiceSettingsStore {
    private final Path settingsFile;
    
    public static class InvoiceSettings {
        public String logoPath;
        public String headerText;
        public String footerText;
        public String templateStyle;
        
        public InvoiceSettings() {
            this.logoPath = "";
            this.headerText = "Welcome to Our Hotel\nThank you for choosing us!";
            this.footerText = "We hope you enjoyed your stay!\nPlease visit us again.";
            this.templateStyle = "Classic";
        }
    }
    
    public InvoiceSettingsStore(Path settingsFile) {
        this.settingsFile = settingsFile;
    }
    
    public InvoiceSettings loadOrDefault() throws IOException {
        if (Files.notExists(settingsFile)) {
            InvoiceSettings defaultSettings = new InvoiceSettings();
            save(defaultSettings);
            return defaultSettings;
        }
        
        return load();
    }
    
    private InvoiceSettings load() throws IOException {
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(settingsFile)) {
            props.load(input);
        }
        
        InvoiceSettings settings = new InvoiceSettings();
        settings.logoPath = props.getProperty("logoPath", "");
        settings.headerText = props.getProperty("headerText", settings.headerText);
        settings.footerText = props.getProperty("footerText", settings.footerText);
        settings.templateStyle = props.getProperty("templateStyle", settings.templateStyle);
        
        return settings;
    }
    
    public void save(InvoiceSettings settings) throws IOException {
        if (Files.notExists(settingsFile.getParent())) {
            Files.createDirectories(settingsFile.getParent());
        }
        
        Properties props = new Properties();
        props.setProperty("logoPath", settings.logoPath);
        props.setProperty("headerText", settings.headerText);
        props.setProperty("footerText", settings.footerText);
        props.setProperty("templateStyle", settings.templateStyle);
        
        try (OutputStream output = Files.newOutputStream(settingsFile)) {
            props.store(output, "Hotel Invoice Settings - Last updated: " + 
                java.time.LocalDateTime.now().toString());
        }
    }
}
