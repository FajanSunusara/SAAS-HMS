package application.services.store;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class RoomCategoryStore {

    public static class RoomType {
        public String typeName;
        public double basePrice;
        public int beds;
        public boolean hasAC;
        public String description;

        public RoomType() {}

        public RoomType(String typeName, double basePrice, int beds, boolean hasAC) {
            this.typeName = typeName;
            this.basePrice = basePrice;
            this.beds = beds;
            this.hasAC = hasAC;
            this.description = "";
        }

        @Override
        public String toString() {
            return typeName + " (₹" + basePrice + ", " + beds + " beds" + (hasAC ? ", AC" : ", Non-AC") + ")";
        }
    }

    public static class RoomCategory {
        public String name;
        public String description;
        public List<RoomType> roomTypes = new ArrayList<>();

        public RoomCategory() {}

        public RoomCategory(String name) {
            this.name = name;
            this.description = "";
        }

        @Override
        public String toString() {
            return name + " (" + roomTypes.size() + " types)";
        }
    }

    public static class RoomCategoryData {
        public List<RoomCategory> categories = new ArrayList<>();
        public String lastUpdated;

        public RoomCategoryData() {}
    }

    private final Path file;

    public RoomCategoryStore(Path file) {
        this.file = file;
    }

    public RoomCategoryData loadOrInit() throws IOException {
        if (Files.notExists(file)) {
            RoomCategoryData data = createDefaultRoomCategories();
            save(data);
            return data;
        }

        return loadFromProperties();
    }

    public void save(RoomCategoryData data) throws IOException {
        data.lastUpdated = java.time.OffsetDateTime.now().toString();
        if (Files.notExists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }

        saveToProperties(data);
    }

    private RoomCategoryData loadFromProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            props.load(input);
        }

        RoomCategoryData data = new RoomCategoryData();
        data.lastUpdated = props.getProperty("lastUpdated", "");

        String categoriesStr = props.getProperty("categories", "");
        if (!categoriesStr.isEmpty()) {
            String[] categoryNames = categoriesStr.split(",");
            for (String categoryName : categoryNames) {
                categoryName = categoryName.trim();
                if (categoryName.isEmpty()) continue;

                RoomCategory category = new RoomCategory(categoryName);
                category.description = props.getProperty("category." + categoryName + ".description", "");

                String roomTypesKey = "category." + categoryName + ".roomTypes";
                String roomTypesStr = props.getProperty(roomTypesKey, "");
                if (!roomTypesStr.isEmpty()) {
                    String[] roomTypeNames = roomTypesStr.split(",");
                    for (String roomTypeName : roomTypeNames) {
                        roomTypeName = roomTypeName.trim();
                        if (roomTypeName.isEmpty()) continue;

                        String typeKey = "roomType." + categoryName + "." + roomTypeName;
                        String priceStr = props.getProperty(typeKey + ".price", "0");
                        String bedsStr = props.getProperty(typeKey + ".beds", "1");
                        String acStr = props.getProperty(typeKey + ".ac", "false");
                        String description = props.getProperty(typeKey + ".description", "");

                        try {
                            double price = Double.parseDouble(priceStr);
                            int beds = Integer.parseInt(bedsStr);
                            boolean hasAC = Boolean.parseBoolean(acStr);

                            RoomType roomType = new RoomType(roomTypeName, price, beds, hasAC);
                            roomType.description = description;
                            category.roomTypes.add(roomType);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid room type format: " + roomTypeName);
                        }
                    }
                }

                data.categories.add(category);
            }
        }

        return data;
    }

    private void saveToProperties(RoomCategoryData data) throws IOException {
        Properties props = new Properties();
        props.setProperty("lastUpdated", data.lastUpdated);

        String categoryNames = data.categories.stream()
                .map(c -> c.name)
                .collect(Collectors.joining(","));
        props.setProperty("categories", categoryNames);

        for (RoomCategory category : data.categories) {
            props.setProperty("category." + category.name + ".description", category.description);

            String roomTypesKey = "category." + category.name + ".roomTypes";
            String roomTypeNames = category.roomTypes.stream()
                    .map(rt -> rt.typeName)
                    .collect(Collectors.joining(","));
            props.setProperty(roomTypesKey, roomTypeNames);

            for (RoomType roomType : category.roomTypes) {
                String typeKey = "roomType." + category.name + "." + roomType.typeName;
                props.setProperty(typeKey + ".price", String.valueOf(roomType.basePrice));
                props.setProperty(typeKey + ".beds", String.valueOf(roomType.beds));
                props.setProperty(typeKey + ".ac", String.valueOf(roomType.hasAC));
                props.setProperty(typeKey + ".description", roomType.description);
            }
        }

        try (OutputStream output = Files.newOutputStream(file)) {
            props.store(output, "Hotel Room Categories - Generated on " +
                    java.time.LocalDateTime.now().toString());
        }
    }

    private RoomCategoryData createDefaultRoomCategories() {
        RoomCategoryData data = new RoomCategoryData();

        // ===== STANDARD ROOMS CATEGORY =====
        RoomCategory standard = new RoomCategory("Standard Rooms");
        standard.description = "Basic comfortable rooms with essential amenities";
        standard.roomTypes.add(new RoomType("Single", 2500.0, 1, true));
        standard.roomTypes.add(new RoomType("Double", 3500.0, 2, true));
        standard.roomTypes.add(new RoomType("Twin", 3200.0, 2, true));
        data.categories.add(standard);

        // ===== DELUXE ROOMS CATEGORY =====
        RoomCategory deluxe = new RoomCategory("Deluxe Rooms");
        deluxe.description = "Spacious rooms with premium amenities and city views";
        deluxe.roomTypes.add(new RoomType("Deluxe Single", 4200.0, 1, true));
        deluxe.roomTypes.add(new RoomType("Deluxe Double", 5500.0, 2, true));
        deluxe.roomTypes.add(new RoomType("Deluxe Twin", 5200.0, 2, true));
        data.categories.add(deluxe);

        // ===== SUITES CATEGORY =====
        RoomCategory suites = new RoomCategory("Suites");
        suites.description = "Luxury suites with separate living areas and premium services";
        suites.roomTypes.add(new RoomType("Junior Suite", 7500.0, 2, true));
        suites.roomTypes.add(new RoomType("Executive Suite", 12000.0, 2, true));
        suites.roomTypes.add(new RoomType("Presidential Suite", 25000.0, 3, true));
        data.categories.add(suites);

        // ===== BUDGET ROOMS CATEGORY =====
        RoomCategory budget = new RoomCategory("Budget Rooms");
        budget.description = "Affordable rooms with basic amenities";
        budget.roomTypes.add(new RoomType("Economy Single", 1800.0, 1, false));
        budget.roomTypes.add(new RoomType("Economy Double", 2800.0, 2, false));
        budget.roomTypes.add(new RoomType("Budget AC Single", 2200.0, 1, true));
        data.categories.add(budget);

        return data;
    }

    // Utility methods
    public List<String> getCategoryNames() throws IOException {
        RoomCategoryData data = loadOrInit();
        return data.categories.stream()
                .map(c -> c.name)
                .collect(Collectors.toList());
    }

    public List<String> getRoomTypeNames(String categoryName) throws IOException {
        RoomCategoryData data = loadOrInit();
        return data.categories.stream()
                .filter(c -> c.name.equals(categoryName))
                .findFirst()
                .map(c -> c.roomTypes.stream().map(rt -> rt.typeName).collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }

    public RoomType getRoomType(String categoryName, String typeName) throws IOException {
        RoomCategoryData data = loadOrInit();
        return data.categories.stream()
                .filter(c -> c.name.equals(categoryName))
                .findFirst()
                .flatMap(c -> c.roomTypes.stream().filter(rt -> rt.typeName.equals(typeName)).findFirst())
                .orElse(null);
    }
}
