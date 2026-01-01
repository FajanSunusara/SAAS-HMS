package application.services.store;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ExpenseCatalogStore {

    // ===== INNER CLASSES (Similar to ServiceCatalogStore) =====
    
    public static class ExpenseItem {
        public String itemName;
        public double standardCost;
        public String unit; // per unit, per hour, per month, etc.
        
        public ExpenseItem() {}
        
        public ExpenseItem(String itemName, double standardCost) {
            this.itemName = itemName;
            this.standardCost = standardCost;
            this.unit = "per unit";
        }
        
        public ExpenseItem(String itemName, double standardCost, String unit) {
            this.itemName = itemName;
            this.standardCost = standardCost;
            this.unit = unit;
        }
        
        public String getItemName() {
            return itemName;
        }
        
        public double getStandardCost() {
            return standardCost;
        }
        
        public String getUnit() {
            return unit;
        }
        
        @Override
        public String toString() {
            return itemName + " (₹" + standardCost + " " + unit + ")";
        }
    }

    public static class ExpenseSubcategory {
        public String name;
        public List<ExpenseItem> items = new ArrayList<>();
        
        public ExpenseSubcategory() {}
        
        public ExpenseSubcategory(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name + " (" + items.size() + " items)";
        }
    }

    public static class ExpenseCategory {
        public String name;
        public List<ExpenseSubcategory> subcategories = new ArrayList<>();
        
        public ExpenseCategory() {}
        
        public ExpenseCategory(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name + " (" + subcategories.size() + " subcategories)";
        }
    }

    public static class ExpenseCatalog {
        public List<ExpenseCategory> categories = new ArrayList<>();
        public String lastUpdated;
        
        public ExpenseCatalog() {}
    }

    // ===== STORAGE AND MANAGEMENT =====
    
    private final Path file;
    
    public ExpenseCatalogStore(Path file) {
        this.file = file;
    }
    
    public ExpenseCatalog loadOrInit() throws IOException {
        if (Files.notExists(file)) {
            ExpenseCatalog c = createDefaultExpenseCatalog();
            save(c);
            return c;
        }
        
        return loadFromProperties();
    }
    
    public void save(ExpenseCatalog catalog) throws IOException {
        catalog.lastUpdated = java.time.OffsetDateTime.now().toString();
        if (Files.notExists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        
        saveToProperties(catalog);
    }
    
    private ExpenseCatalog loadFromProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            props.load(input);
        }
        
        ExpenseCatalog catalog = new ExpenseCatalog();
        catalog.lastUpdated = props.getProperty("lastUpdated", "");
        String categoriesStr = props.getProperty("categories", "");
        
        if (!categoriesStr.isEmpty()) {
            String[] categoryNames = categoriesStr.split(",");
            for (String categoryName : categoryNames) {
                categoryName = categoryName.trim();
                if (categoryName.isEmpty()) continue;
                
                ExpenseCategory category = new ExpenseCategory(categoryName);
                String subcategoriesKey = "category." + categoryName + ".subcategories";
                String subcategoriesStr = props.getProperty(subcategoriesKey, "");
                
                if (!subcategoriesStr.isEmpty()) {
                    String[] subcategoryNames = subcategoriesStr.split(",");
                    for (String subcategoryName : subcategoryNames) {
                        subcategoryName = subcategoryName.trim();
                        if (subcategoryName.isEmpty()) continue;
                        
                        ExpenseSubcategory subcategory = new ExpenseSubcategory(subcategoryName);
                        String itemsKey = "subcategory." + categoryName + "." + subcategoryName + ".items";
                        String itemsStr = props.getProperty(itemsKey, "");
                        
                        if (!itemsStr.isEmpty()) {
                            String[] items = itemsStr.split(";");
                            for (String item : items) {
                                if (item.trim().isEmpty()) continue;
                                String[] parts = item.split(":");
                                if (parts.length >= 2) {
                                    try {
                                        String itemName = parts[0].trim();
                                        double cost = Double.parseDouble(parts[1].trim());
                                        String unit = parts.length > 2 ? parts[2].trim() : "per unit";
                                        subcategory.items.add(new ExpenseItem(itemName, cost, unit));
                                    } catch (NumberFormatException e) {
                                        System.err.println("Invalid expense item format: " + item);
                                    }
                                }
                            }
                        }
                        category.subcategories.add(subcategory);
                    }
                }
                catalog.categories.add(category);
            }
        }
        return catalog;
    }
    
    private void saveToProperties(ExpenseCatalog catalog) throws IOException {
        Properties props = new Properties();
        props.setProperty("lastUpdated", catalog.lastUpdated);
        
        String categoryNames = catalog.categories.stream()
                .map(c -> c.name)
                .collect(Collectors.joining(","));
        props.setProperty("categories", categoryNames);
        
        for (ExpenseCategory category : catalog.categories) {
            String subcategoriesKey = "category." + category.name + ".subcategories";
            String subcategoryNames = category.subcategories.stream()
                    .map(s -> s.name)
                    .collect(Collectors.joining(","));
            props.setProperty(subcategoriesKey, subcategoryNames);
            
            for (ExpenseSubcategory subcategory : category.subcategories) {
                String itemsKey = "subcategory." + category.name + "." + subcategory.name + ".items";
                String items = subcategory.items.stream()
                        .map(i -> i.itemName + ":" + i.standardCost + ":" + i.unit)
                        .collect(Collectors.joining(";"));
                props.setProperty(itemsKey, items);
            }
        }
        
        try (OutputStream output = Files.newOutputStream(file)) {
            props.store(output, "Hotel Expense Catalog - Generated on " + java.time.LocalDateTime.now());
        }
    }
    
    // ===== DEFAULT EXPENSE CATALOG CREATION =====
    
    private ExpenseCatalog createDefaultExpenseCatalog() {
        ExpenseCatalog catalog = new ExpenseCatalog();
        
        // MAINTENANCE & REPAIRS
        ExpenseCategory maintenance = new ExpenseCategory("Maintenance & Repairs");
        
        ExpenseSubcategory acMaintenance = new ExpenseSubcategory("AC Servicing");
        acMaintenance.items.add(new ExpenseItem("Window AC Service", 800.0, "per unit"));
        acMaintenance.items.add(new ExpenseItem("Split AC Service", 1200.0, "per unit"));
        acMaintenance.items.add(new ExpenseItem("Central AC Service", 5000.0, "per system"));
        acMaintenance.items.add(new ExpenseItem("AC Gas Refilling", 2500.0, "per unit"));
        acMaintenance.items.add(new ExpenseItem("AC Filter Replacement", 300.0, "per unit"));
        maintenance.subcategories.add(acMaintenance);
        
        ExpenseSubcategory plumbing = new ExpenseSubcategory("Plumbing Work");
        plumbing.items.add(new ExpenseItem("Washbasin Repair", 500.0, "per job"));
        plumbing.items.add(new ExpenseItem("Toilet Repair", 800.0, "per job"));
        plumbing.items.add(new ExpenseItem("Pipe Leakage Fix", 300.0, "per job"));
        plumbing.items.add(new ExpenseItem("Faucet Replacement", 600.0, "per unit"));
        plumbing.items.add(new ExpenseItem("Water Tank Cleaning", 2000.0, "per tank"));
        maintenance.subcategories.add(plumbing);
        
        ExpenseSubcategory electrical = new ExpenseSubcategory("Electrical Work");
        electrical.items.add(new ExpenseItem("Switch Board Repair", 200.0, "per job"));
        electrical.items.add(new ExpenseItem("Fan Installation", 500.0, "per unit"));
        electrical.items.add(new ExpenseItem("Light Fixture Repair", 300.0, "per job"));
        electrical.items.add(new ExpenseItem("Wiring Work", 150.0, "per meter"));
        electrical.items.add(new ExpenseItem("MCB Replacement", 400.0, "per unit"));
        maintenance.subcategories.add(electrical);
        
        ExpenseSubcategory painting = new ExpenseSubcategory("Painting & Renovation");
        painting.items.add(new ExpenseItem("Room Wall Painting", 3000.0, "per room"));
        painting.items.add(new ExpenseItem("Exterior Painting", 20000.0, "per building"));
        painting.items.add(new ExpenseItem("Touch-up Work", 500.0, "per job"));
        painting.items.add(new ExpenseItem("Ceiling Painting", 1500.0, "per room"));
        maintenance.subcategories.add(painting);
        
        catalog.categories.add(maintenance);
        
        // UTILITIES & BILLS
        ExpenseCategory utilities = new ExpenseCategory("Utilities & Bills");
        
        ExpenseSubcategory electricityBills = new ExpenseSubcategory("Electricity Bills");
        electricityBills.items.add(new ExpenseItem("Monthly Electricity Bill", 25000.0, "per month"));
        electricityBills.items.add(new ExpenseItem("Power Factor Penalty", 2000.0, "per month"));
        electricityBills.items.add(new ExpenseItem("Meter Rent", 100.0, "per month"));
        utilities.subcategories.add(electricityBills);
        
        ExpenseSubcategory waterBills = new ExpenseSubcategory("Water Bills");
        waterBills.items.add(new ExpenseItem("Municipal Water Bill", 5000.0, "per month"));
        waterBills.items.add(new ExpenseItem("Tanker Water", 3000.0, "per tanker"));
        waterBills.items.add(new ExpenseItem("Water Testing", 500.0, "per test"));
        utilities.subcategories.add(waterBills);
        
        ExpenseSubcategory internetPhone = new ExpenseSubcategory("Internet & Communication");
        internetPhone.items.add(new ExpenseItem("Broadband Internet", 4500.0, "per month"));
        internetPhone.items.add(new ExpenseItem("Landline Phone", 800.0, "per month"));
        internetPhone.items.add(new ExpenseItem("Mobile Phone Bill", 1500.0, "per month"));
        internetPhone.items.add(new ExpenseItem("Cable TV", 2000.0, "per month"));
        utilities.subcategories.add(internetPhone);
        
        catalog.categories.add(utilities);
        
        // STAFF EXPENSES
        ExpenseCategory staff = new ExpenseCategory("Staff Expenses");
        
        ExpenseSubcategory salaries = new ExpenseSubcategory("Salaries & Wages");
        salaries.items.add(new ExpenseItem("Manager Salary", 45000.0, "per month"));
        salaries.items.add(new ExpenseItem("Reception Staff Salary", 18000.0, "per month"));
        salaries.items.add(new ExpenseItem("Housekeeping Staff Salary", 15000.0, "per month"));
        salaries.items.add(new ExpenseItem("Security Guard Salary", 12000.0, "per month"));
        salaries.items.add(new ExpenseItem("Part-time Worker", 500.0, "per day"));
        staff.subcategories.add(salaries);
        
        ExpenseSubcategory benefits = new ExpenseSubcategory("Benefits & Allowances");
        benefits.items.add(new ExpenseItem("Medical Insurance", 2000.0, "per month"));
        benefits.items.add(new ExpenseItem("Travel Allowance", 1500.0, "per month"));
        benefits.items.add(new ExpenseItem("Festival Bonus", 10000.0, "per festival"));
        benefits.items.add(new ExpenseItem("Uniform Allowance", 2000.0, "per year"));
        staff.subcategories.add(benefits);
        
        catalog.categories.add(staff);
        
        // SUPPLIES & MATERIALS
        ExpenseCategory supplies = new ExpenseCategory("Supplies & Materials");
        
        ExpenseSubcategory cleaning = new ExpenseSubcategory("Cleaning Supplies");
        cleaning.items.add(new ExpenseItem("Floor Cleaner", 200.0, "per bottle"));
        cleaning.items.add(new ExpenseItem("Toilet Cleaner", 150.0, "per bottle"));
        cleaning.items.add(new ExpenseItem("Glass Cleaner", 180.0, "per bottle"));
        cleaning.items.add(new ExpenseItem("Phenyl", 120.0, "per bottle"));
        cleaning.items.add(new ExpenseItem("Toilet Paper Roll", 50.0, "per roll"));
        cleaning.items.add(new ExpenseItem("Garbage Bags", 200.0, "per pack"));
        supplies.subcategories.add(cleaning);
        
        ExpenseSubcategory toiletries = new ExpenseSubcategory("Guest Toiletries");
        toiletries.items.add(new ExpenseItem("Soap Bars", 25.0, "per piece"));
        toiletries.items.add(new ExpenseItem("Shampoo Sachets", 15.0, "per piece"));
        toiletries.items.add(new ExpenseItem("Towel (Bath)", 300.0, "per piece"));
        toiletries.items.add(new ExpenseItem("Towel (Face)", 150.0, "per piece"));
        toiletries.items.add(new ExpenseItem("Toilet Paper", 40.0, "per roll"));
        supplies.subcategories.add(toiletries);
        
        ExpenseSubcategory bedLinens = new ExpenseSubcategory("Bed Linens");
        bedLinens.items.add(new ExpenseItem("Bed Sheet Set", 800.0, "per set"));
        bedLinens.items.add(new ExpenseItem("Pillow Cover", 120.0, "per piece"));
        bedLinens.items.add(new ExpenseItem("Blanket", 1200.0, "per piece"));
        bedLinens.items.add(new ExpenseItem("Pillow", 400.0, "per piece"));
        supplies.subcategories.add(bedLinens);
        
        catalog.categories.add(supplies);
        
        // MARKETING & ADVERTISING
        ExpenseCategory marketing = new ExpenseCategory("Marketing & Advertising");
        
        ExpenseSubcategory onlineMarketing = new ExpenseSubcategory("Online Marketing");
        onlineMarketing.items.add(new ExpenseItem("Google Ads", 5000.0, "per month"));
        onlineMarketing.items.add(new ExpenseItem("Facebook Ads", 3000.0, "per month"));
        onlineMarketing.items.add(new ExpenseItem("Website Maintenance", 2000.0, "per month"));
        onlineMarketing.items.add(new ExpenseItem("SEO Services", 8000.0, "per month"));
        marketing.subcategories.add(onlineMarketing);
        
        ExpenseSubcategory printMedia = new ExpenseSubcategory("Print Media");
        printMedia.items.add(new ExpenseItem("Newspaper Ad", 2000.0, "per ad"));
        printMedia.items.add(new ExpenseItem("Brochure Printing", 5000.0, "per 1000 pieces"));
        printMedia.items.add(new ExpenseItem("Business Cards", 800.0, "per 500 pieces"));
        marketing.subcategories.add(printMedia);
        
        catalog.categories.add(marketing);
        
        // FOOD & KITCHEN OPERATIONS
        ExpenseCategory kitchen = new ExpenseCategory("Food & Kitchen Operations");
        
        ExpenseSubcategory rawMaterials = new ExpenseSubcategory("Raw Materials");
        rawMaterials.items.add(new ExpenseItem("Rice (25kg)", 1200.0, "per bag"));
        rawMaterials.items.add(new ExpenseItem("Dal (25kg)", 2000.0, "per bag"));
        rawMaterials.items.add(new ExpenseItem("Cooking Oil (15L)", 1800.0, "per can"));
        rawMaterials.items.add(new ExpenseItem("Vegetables", 500.0, "per day"));
        rawMaterials.items.add(new ExpenseItem("Milk", 60.0, "per liter"));
        rawMaterials.items.add(new ExpenseItem("Bread Loaves", 40.0, "per loaf"));
        kitchen.subcategories.add(rawMaterials);
        
        ExpenseSubcategory kitchenEquipment = new ExpenseSubcategory("Kitchen Equipment");
        kitchenEquipment.items.add(new ExpenseItem("Gas Cylinder", 900.0, "per cylinder"));
        kitchenEquipment.items.add(new ExpenseItem("Kitchen Utensils", 2000.0, "per set"));
        kitchenEquipment.items.add(new ExpenseItem("Mixer Grinder Service", 500.0, "per service"));
        kitchen.subcategories.add(kitchenEquipment);
        
        catalog.categories.add(kitchen);
        
        // ADMINISTRATION
        ExpenseCategory administration = new ExpenseCategory("Administration");
        
        ExpenseSubcategory office = new ExpenseSubcategory("Office Supplies");
        office.items.add(new ExpenseItem("A4 Paper (Ream)", 300.0, "per ream"));
        office.items.add(new ExpenseItem("Pen Box", 120.0, "per box"));
        office.items.add(new ExpenseItem("Printer Cartridge", 2500.0, "per cartridge"));
        office.items.add(new ExpenseItem("File Folders", 200.0, "per pack"));
        administration.subcategories.add(office);
        
        ExpenseSubcategory legal = new ExpenseSubcategory("Legal & Compliance");
        legal.items.add(new ExpenseItem("Trade License Renewal", 5000.0, "per year"));
        legal.items.add(new ExpenseItem("Fire Safety Certificate", 3000.0, "per year"));
        legal.items.add(new ExpenseItem("CA Consultation", 2000.0, "per visit"));
        legal.items.add(new ExpenseItem("Legal Documentation", 5000.0, "per case"));
        administration.subcategories.add(legal);
        
        catalog.categories.add(administration);
        
        return catalog;
    }
    
    // ===== UTILITY METHODS (Same as ServiceCatalogStore) =====
    
    public List<String> getCategoryNames() throws IOException {
        ExpenseCatalog catalog = loadOrInit();
        return catalog.categories.stream()
                .map(c -> c.name)
                .collect(Collectors.toList());
    }
    
    public List<String> getSubcategoryNames(String categoryName) throws IOException {
        ExpenseCatalog catalog = loadOrInit();
        return catalog.categories.stream()
                .filter(c -> c.name.equals(categoryName))
                .findFirst()
                .map(c -> c.subcategories.stream().map(s -> s.name).collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }
    
    public List<ExpenseItem> getExpenseItems(String categoryName, String subcategoryName) throws IOException {
        ExpenseCatalog catalog = loadOrInit();
        return catalog.categories.stream()
                .filter(c -> c.name.equals(categoryName))
                .findFirst()
                .flatMap(c -> c.subcategories.stream().filter(s -> s.name.equals(subcategoryName)).findFirst())
                .map(s -> new ArrayList<>(s.items))
                .orElse(new ArrayList<>());
    }
    
    public ExpenseItem findExpenseItem(String categoryName, String subcategoryName, String itemName) throws IOException {
        ExpenseCatalog catalog = loadOrInit();
        return catalog.categories.stream()
                .filter(c -> c.name.equals(categoryName))
                .findFirst()
                .flatMap(c -> c.subcategories.stream().filter(s -> s.name.equals(subcategoryName)).findFirst())
                .flatMap(s -> s.items.stream().filter(i -> i.itemName.equals(itemName)).findFirst())
                .orElse(null);
    }
    
    // Search methods
    public List<ExpenseItem> searchExpenseItems(String searchTerm) throws IOException {
        ExpenseCatalog catalog = loadOrInit();
        String searchLower = searchTerm.toLowerCase();
        List<ExpenseItem> results = new ArrayList<>();
        
        for (ExpenseCategory category : catalog.categories) {
            for (ExpenseSubcategory subcategory : category.subcategories) {
                for (ExpenseItem item : subcategory.items) {
                    if (item.itemName.toLowerCase().contains(searchLower) ||
                        category.name.toLowerCase().contains(searchLower) ||
                        subcategory.name.toLowerCase().contains(searchLower)) {
                        results.add(item);
                    }
                }
            }
        }
        return results;
    }
    
    // Add new expense item
    public void addExpenseItem(String categoryName, String subcategoryName, ExpenseItem item) throws IOException {
        ExpenseCatalog catalog = loadOrInit();
        
        // Find or create category
        ExpenseCategory category = catalog.categories.stream()
                .filter(c -> c.name.equals(categoryName))
                .findFirst()
                .orElse(null);
        
        if (category == null) {
            category = new ExpenseCategory(categoryName);
            catalog.categories.add(category);
        }
        
        // Find or create subcategory
        ExpenseSubcategory subcategory = category.subcategories.stream()
                .filter(s -> s.name.equals(subcategoryName))
                .findFirst()
                .orElse(null);
        
        if (subcategory == null) {
            subcategory = new ExpenseSubcategory(subcategoryName);
            category.subcategories.add(subcategory);
        }
        
        // Add item if it doesn't exist
        boolean exists = subcategory.items.stream()
                .anyMatch(i -> i.itemName.equals(item.itemName));
        
        if (!exists) {
            subcategory.items.add(item);
            save(catalog);
        }
    }
}
