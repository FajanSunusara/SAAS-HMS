package application.services.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:file:./data/hotel-db;AUTO_SERVER=TRUE;MODE=PostgreSQL";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("H2 Driver not found. Add h2.jar to classpath.", e);
            }
            createDataFolder();
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return connection;
    }

    private static void createDataFolder() {
        Path dataDir = Paths.get("./data");
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
            } catch (Exception e) {
                throw new RuntimeException("Could not create ./data folder for database storage", e);
            }
        }
    }
}
