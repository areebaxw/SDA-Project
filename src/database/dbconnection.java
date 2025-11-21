package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Singleton database connection manager
 */
public class DBConnection {
    private static DBConnection instance;
    private Connection connection;
    
    private static final String URL = "jdbc:mysql://localhost:3306/aws_governance_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // XAMPP default - UPDATE THIS WITH YOUR MYSQL PASSWORD

    // Private constructor for Singleton pattern
    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database");
            e.printStackTrace();
        }
    }

    /**
     * Get Singleton instance
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Get database connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is valid, reconnect if needed
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try {
            Connection conn = getInstance().getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (instance != null && instance.connection != null && !instance.connection.isClosed()) {
                instance.connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection");
            e.printStackTrace();
        }
    }
}
