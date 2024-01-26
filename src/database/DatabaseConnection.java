package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + DatabaseConfig.getServerIP() + ":" + DatabaseConfig.getPorta() + "/RubricaTelefonicaDB";
        String username = DatabaseConfig.getUsername();
        String password = DatabaseConfig.getPassword();
        return DriverManager.getConnection(url, username, password);
    }
}
