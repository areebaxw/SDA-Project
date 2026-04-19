package dao;

import database.DBConnection;
import models.User;
import java.sql.*;

/**
 * UserDAO - Data Access Object for User operations
 * Implements DAO pattern for database operations
 */
public class UserDAO {

    private Connection connection;

    public UserDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    private Connection conn() {
        return this.connection;
    }
    
    /**
     * Authenticate user by username and password
     * @param username
     * @param password
     * @return User object if authenticated, null otherwise
     */
    public User authenticateUser(String username, String password) {
        Connection connection = conn();
        if (connection == null) return null;
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                
                // Update last login
                updateLastLogin(user.getUserId());
                
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Update user's last login timestamp
     */
    private void updateLastLogin(int userId) {
        Connection connection = conn();
        if (connection == null) return;
        String query = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        Connection connection = conn();
        if (connection == null) return null;
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Create new user
     */
    public boolean createUser(User user) {
        Connection connection = conn();
        if (connection == null) return false;
        String query = "INSERT INTO users (username, password, email, full_name, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getRole());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Create new user and return generated user ID
     */
    public int createUserAndGetId(User user) {
        Connection connection = conn();
        if (connection == null) return -1;
        String query = "INSERT INTO users (username, password, email, full_name, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getRole());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        Connection connection = conn();
        if (connection == null) return false;
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update user
     */
    public boolean updateUser(User user) {
        Connection connection = conn();
        if (connection == null) return false;
        String query = "UPDATE users SET username = ?, password = ?, email = ?, full_name = ?, role = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getRole());
            stmt.setInt(6, user.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete user
     */
    public boolean deleteUser(int userId) {
        Connection connection = conn();
        if (connection == null) return false;
        String query = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
