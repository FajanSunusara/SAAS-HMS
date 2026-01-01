package application;

import org.h2.tools.Server;

import java.sql.SQLException;

public class H2ConsoleLauncher {
    public static void main(String[] args) {
        try {
            Server.startWebServer(H2DatabaseUtil.getConnection());
            System.out.println("✅ H2 Console started. Visit http://localhost:8082");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
