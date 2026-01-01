package application;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DBInit {
    public static void initialize() {
        try (Connection conn = H2DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            String createTable = "CREATE TABLE IF NOT EXISTS rooms (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "room_number VARCHAR(10), " +
                    "status VARCHAR(20)" +
                    ");";

            stmt.execute(createTable);

            String insertData = "INSERT INTO rooms (room_number, status) VALUES " +
                    "('101', 'Available'), ('102', 'Occupied'), ('103', 'Cleaning');";

            stmt.execute(insertData);

            System.out.println("✅ H2 Database initialized.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
