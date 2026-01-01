package application;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class RoomService {
    public void printAllRooms() {
        try (Connection conn = H2DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {

            System.out.println("Room List:");
            while (rs.next()) {
                System.out.println("→ Room " + rs.getString("room_number") +
                        " | Status: " + rs.getString("status"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
