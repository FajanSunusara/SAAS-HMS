package application.services.store;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceCatalogStore {

    public static class CatalogItem {
        public String itemName;
        public double unitPrice;

        public CatalogItem() {}

        public CatalogItem(String itemName, double unitPrice) {
            this.itemName = itemName;
            this.unitPrice = unitPrice;
        }

        public String getItemName() {
            return itemName;
        }
        public double getUnitPrice() {
            return unitPrice;
        }

        @Override
        public String toString() {
            return itemName + " (₹" + unitPrice + ")";
        }
    }

    public static class CatalogService {
        public String name;
        public List<CatalogItem> items = new ArrayList<>();

        public CatalogService() {}

        public CatalogService(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (" + items.size() + " items)";
        }
    }

    public static class CatalogCategory {
        public String name;
        public List<CatalogService> services = new ArrayList<>();

        public CatalogCategory() {}

        public CatalogCategory(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (" + services.size() + " services)";
        }
    }

    public static class Catalog {
        public List<CatalogCategory> categories = new ArrayList<>();
        public String lastUpdated;

        public Catalog() {}
    }

    private final Path file;

    public ServiceCatalogStore(Path file) {
        this.file = file;
    }

    public Catalog loadOrInit() throws IOException {
        if (Files.notExists(file)) {
            Catalog c = createDefaultCatalog();
            save(c);
            return c;
        }
        return loadFromProperties();
    }

    public void save(Catalog catalog) throws IOException {
        catalog.lastUpdated = java.time.OffsetDateTime.now().toString();
        if (Files.notExists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        saveToProperties(catalog);
    }

    private Catalog loadFromProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            props.load(input);
        }

        Catalog catalog = new Catalog();
        catalog.lastUpdated = props.getProperty("lastUpdated", "");
        String categoriesStr = props.getProperty("categories", "");

        if (!categoriesStr.isEmpty()) {
            String[] categoryNames = categoriesStr.split(",");
            for (String categoryName : categoryNames) {
                categoryName = categoryName.trim();
                if (categoryName.isEmpty()) continue;

                CatalogCategory category = new CatalogCategory(categoryName);

                String servicesKey = "category." + categoryName + ".services";
                String servicesStr = props.getProperty(servicesKey, "");
                if (!servicesStr.isEmpty()) {
                    String[] serviceNames = servicesStr.split(",");
                    for (String serviceName : serviceNames) {
                        serviceName = serviceName.trim();
                        if (serviceName.isEmpty()) continue;

                        CatalogService service = new CatalogService(serviceName);

                        String itemsKey = "service." + categoryName + "." + serviceName + ".items";
                        String itemsStr = props.getProperty(itemsKey, "");
                        if (!itemsStr.isEmpty()) {
                            String[] items = itemsStr.split(";");
                            for (String item : items) {
                                if (item.trim().isEmpty()) continue;
                                String[] parts = item.split(":");
                                if (parts.length == 2) {
                                    try {
                                        String itemName = parts[0].trim();
                                        double price = Double.parseDouble(parts[1].trim());
                                        service.items.add(new CatalogItem(itemName, price));
                                    } catch (NumberFormatException e) {
                                        System.err.println("Invalid item format: " + item);
                                    }
                                }
                            }
                        }
                        category.services.add(service);
                    }
                }
                catalog.categories.add(category);
            }
        }
        return catalog;
    }

    private void saveToProperties(Catalog catalog) throws IOException {
        Properties props = new Properties();
        props.setProperty("lastUpdated", catalog.lastUpdated);

        String categoryNames = catalog.categories.stream()
                .map(c -> c.name)
                .collect(Collectors.joining(","));
        props.setProperty("categories", categoryNames);

        for (CatalogCategory category : catalog.categories) {
            String servicesKey = "category." + category.name + ".services";
            String serviceNames = category.services.stream()
                    .map(s -> s.name)
                    .collect(Collectors.joining(","));
            props.setProperty(servicesKey, serviceNames);

            for (CatalogService service : category.services) {
                String itemsKey = "service." + category.name + "." + service.name + ".items";
                String items = service.items.stream()
                        .map(i -> i.itemName + ":" + i.unitPrice)
                        .collect(Collectors.joining(";"));
                props.setProperty(itemsKey, items);
            }
        }

        try (OutputStream output = Files.newOutputStream(file)) {
            props.store(output, "Hotel Service Catalog with Detailed Food Menu - Generated on " + java.time.LocalDateTime.now());
        }
    }

    private Catalog createDefaultCatalog() {
        Catalog catalog = new Catalog();

        // FOOD & BEVERAGE
        CatalogCategory food = new CatalogCategory("Food & Beverage");

        CatalogService breakfast = new CatalogService("Breakfast");
        breakfast.items.add(new CatalogItem("Continental Breakfast", 450.0));
        breakfast.items.add(new CatalogItem("Indian Breakfast", 350.0));
        breakfast.items.add(new CatalogItem("Bread Omelet", 120.0));
        breakfast.items.add(new CatalogItem("Butter Toast (2 pcs)", 80.0));
        breakfast.items.add(new CatalogItem("Paratha with Curd", 150.0));
        breakfast.items.add(new CatalogItem("Poha", 100.0));
        breakfast.items.add(new CatalogItem("Upma", 90.0));
        breakfast.items.add(new CatalogItem("Idli Sambar (4 pcs)", 120.0));
        breakfast.items.add(new CatalogItem("Dosa", 140.0));
        breakfast.items.add(new CatalogItem("Vada (2 pcs)", 80.0));
        food.services.add(breakfast);

        CatalogService lunch = new CatalogService("Lunch");
        lunch.items.add(new CatalogItem("Thali (Veg)", 280.0));
        lunch.items.add(new CatalogItem("Thali (Non-Veg)", 350.0));
        lunch.items.add(new CatalogItem("Dal Rice", 180.0));
        lunch.items.add(new CatalogItem("Rajma Rice", 220.0));
        lunch.items.add(new CatalogItem("Chole Rice", 200.0));
        lunch.items.add(new CatalogItem("Chicken Biryani", 420.0));
        lunch.items.add(new CatalogItem("Veg Biryani", 320.0));
        lunch.items.add(new CatalogItem("Fish Curry Rice", 380.0));
        lunch.items.add(new CatalogItem("Sambar Rice", 160.0));
        lunch.items.add(new CatalogItem("Pulao", 200.0));
        food.services.add(lunch);

        CatalogService dinner = new CatalogService("Dinner");
        dinner.items.add(new CatalogItem("Butter Chicken", 380.0));
        dinner.items.add(new CatalogItem("Paneer Butter Masala", 280.0));
        dinner.items.add(new CatalogItem("Dal Makhani", 250.0));
        dinner.items.add(new CatalogItem("Roti (2 pcs)", 60.0));
        dinner.items.add(new CatalogItem("Naan", 80.0));
        dinner.items.add(new CatalogItem("Garlic Naan", 90.0));
        dinner.items.add(new CatalogItem("Jeera Rice", 120.0));
        dinner.items.add(new CatalogItem("Mixed Veg", 200.0));
        dinner.items.add(new CatalogItem("Chicken Curry", 350.0));
        dinner.items.add(new CatalogItem("Fish Fry (2 pcs)", 300.0));
        food.services.add(dinner);

        CatalogService teaSnacks = new CatalogService("Tea & Snacks");
        teaSnacks.items.add(new CatalogItem("Tea", 40.0));
        teaSnacks.items.add(new CatalogItem("Coffee", 50.0));
        teaSnacks.items.add(new CatalogItem("Green Tea", 60.0));
        teaSnacks.items.add(new CatalogItem("Masala Tea", 50.0));
        teaSnacks.items.add(new CatalogItem("Samosa (2 pcs)", 60.0));
        teaSnacks.items.add(new CatalogItem("Pakoda", 80.0));
        teaSnacks.items.add(new CatalogItem("Sandwich", 120.0));
        teaSnacks.items.add(new CatalogItem("Burger", 150.0));
        teaSnacks.items.add(new CatalogItem("French Fries", 100.0));
        teaSnacks.items.add(new CatalogItem("Biscuits (Pack)", 40.0));
        food.services.add(teaSnacks);

        CatalogService minibar = new CatalogService("Minibar");
        minibar.items.add(new CatalogItem("Coca Cola", 60.0));
        minibar.items.add(new CatalogItem("Pepsi", 60.0));
        minibar.items.add(new CatalogItem("Sprite", 60.0));
        minibar.items.add(new CatalogItem("Water Bottle (500ml)", 40.0));
        minibar.items.add(new CatalogItem("Orange Juice", 80.0));
        minibar.items.add(new CatalogItem("Apple Juice", 80.0));
        minibar.items.add(new CatalogItem("Beer (Kingfisher)", 180.0));
        minibar.items.add(new CatalogItem("Wine (Red)", 450.0));
        minibar.items.add(new CatalogItem("Potato Chips", 60.0));
        minibar.items.add(new CatalogItem("Chocolate Bar", 80.0));
        minibar.items.add(new CatalogItem("Mixed Nuts", 120.0));
        minibar.items.add(new CatalogItem("Energy Drink", 100.0));
        food.services.add(minibar);

        catalog.categories.add(food);

        // LAUNDRY
        CatalogCategory laundry = new CatalogCategory("Laundry");

        CatalogService regularLaundry = new CatalogService("Regular Laundry");
        regularLaundry.items.add(new CatalogItem("Shirt", 60.0));
        regularLaundry.items.add(new CatalogItem("T-Shirt", 50.0));
        regularLaundry.items.add(new CatalogItem("Trousers", 80.0));
        regularLaundry.items.add(new CatalogItem("Jeans", 90.0));
        regularLaundry.items.add(new CatalogItem("Dress", 150.0));
        regularLaundry.items.add(new CatalogItem("Saree", 200.0));
        regularLaundry.items.add(new CatalogItem("Kurta", 100.0));
        laundry.services.add(regularLaundry);

        CatalogService dryCleaning = new CatalogService("Dry Cleaning");
        dryCleaning.items.add(new CatalogItem("Suit (2 piece)", 350.0));
        dryCleaning.items.add(new CatalogItem("Blazer", 250.0));
        dryCleaning.items.add(new CatalogItem("Coat", 300.0));
        dryCleaning.items.add(new CatalogItem("Silk Dress", 280.0));
        dryCleaning.items.add(new CatalogItem("Leather Jacket", 500.0));
        laundry.services.add(dryCleaning);

        catalog.categories.add(laundry);

        // SPA & WELLNESS
        CatalogCategory spa = new CatalogCategory("Spa & Wellness");

        CatalogService massage = new CatalogService("Massage");
        massage.items.add(new CatalogItem("Swedish Massage (60min)", 2200.0));
        massage.items.add(new CatalogItem("Deep Tissue Massage (60min)", 2500.0));
        massage.items.add(new CatalogItem("Head & Shoulder Massage (30min)", 1200.0));
        massage.items.add(new CatalogItem("Foot Reflexology (45min)", 1500.0));
        spa.services.add(massage);

        CatalogService facial = new CatalogService("Facial & Beauty");
        facial.items.add(new CatalogItem("Classic Facial (60min)", 1800.0));
        facial.items.add(new CatalogItem("Manicure", 800.0));
        facial.items.add(new CatalogItem("Pedicure", 900.0));
        facial.items.add(new CatalogItem("Hair Wash & Blow Dry", 600.0));
        spa.services.add(facial);

        catalog.categories.add(spa);

        // TRANSPORTATION
        CatalogCategory transport = new CatalogCategory("Transportation");

        CatalogService carRental = new CatalogService("Car Rental");
        carRental.items.add(new CatalogItem("Airport Transfer (One Way)", 1200.0));
        carRental.items.add(new CatalogItem("Half Day City Tour (4hr)", 2800.0));
        carRental.items.add(new CatalogItem("Full Day City Tour (8hr)", 4500.0));
        carRental.items.add(new CatalogItem("Car Rental (4hr)", 2000.0));
        transport.services.add(carRental);

        catalog.categories.add(transport);

        return catalog;
    }

    // Utility methods

    public List<String> getCategoryNames() throws IOException {
        Catalog catalog = loadOrInit();
        return catalog.categories.stream()
                .map(c -> c.name)
                .collect(Collectors.toList());
    }

    public List<String> getServiceNames(String categoryName) throws IOException {
        Catalog catalog = loadOrInit();
        return catalog.categories.stream()
                .filter(c -> c.name.equals(categoryName))
                .findFirst()
                .map(c -> c.services.stream().map(s -> s.name).collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }

    public List<CatalogItem> getItems(String categoryName, String serviceName) throws IOException {
        Catalog catalog = loadOrInit();
        return catalog.categories.stream()
                .filter(c -> c.name.equals(categoryName))
                .findFirst()
                .flatMap(c -> c.services.stream().filter(s -> s.name.equals(serviceName)).findFirst())
                .map(s -> new ArrayList<>(s.items))
                .orElse(new ArrayList<>());
    }
}
