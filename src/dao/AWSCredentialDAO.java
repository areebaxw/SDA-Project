package dao;

import database.DBConnection;
import models.AWSCredential;
import java.sql.*;

/**
 * AWSCredentialDAO - Data Access Object for AWS Credential operations
 */
public class AWSCredentialDAO {
    private final Connection connection;
    
    public AWSCredentialDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }
    
    /**
     * Get active AWS credentials for a user
     */
    public AWSCredential getActiveCredentials(int userId) {
        String query = "SELECT * FROM aws_credentials WHERE user_id = ? AND is_active = TRUE LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCredential(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting active credentials: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Save or update AWS credentials
     */
    public boolean saveCredentials(AWSCredential credential) {
        // Deactivate all existing credentials for this user first
        deactivateAllCredentials(credential.getUserId());
        
        String query = "INSERT INTO aws_credentials (user_id, access_key, secret_key, region, remaining_credits, is_active, validated) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, credential.getUserId());
            stmt.setString(2, credential.getAccessKey());
            stmt.setString(3, credential.getSecretKey());
            stmt.setString(4, credential.getRegion());
            stmt.setDouble(5, credential.getRemainingCredits());
            stmt.setBoolean(6, credential.isActive());
            stmt.setBoolean(7, credential.isValidated());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving credentials: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update credential validation status
     */
    public boolean updateValidationStatus(int credentialId, boolean validated) {
        String query = "UPDATE aws_credentials SET validated = ? WHERE credential_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, validated);
            stmt.setInt(2, credentialId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating validation status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update remaining credits for active credentials
     */
    public boolean updateRemainingCredits(int userId, double remainingCredits) {
        String query = "UPDATE aws_credentials SET remaining_credits = ? WHERE user_id = ? AND is_active = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, remainingCredits);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating remaining credits: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Deactivate all credentials for a user
     */
    private void deactivateAllCredentials(int userId) {
        String query = "UPDATE aws_credentials SET is_active = FALSE WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deactivating credentials: " + e.getMessage());
        }
    }
    
    /**
     * Check if user has validated credentials
     */
    public boolean hasValidatedCredentials(int userId) {
        String query = "SELECT COUNT(*) FROM aws_credentials WHERE user_id = ? AND is_active = TRUE AND validated = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking validated credentials: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Check if AWS access key already exists
     */
    public boolean accessKeyExists(String accessKey) {
        String query = "SELECT COUNT(*) FROM aws_credentials WHERE access_key = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accessKey);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking access key: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Map ResultSet to AWSCredential object
     */
    private AWSCredential mapResultSetToCredential(ResultSet rs) throws SQLException {
        AWSCredential credential = new AWSCredential();
        credential.setCredentialId(rs.getInt("credential_id"));
        credential.setUserId(rs.getInt("user_id"));
        credential.setAccessKey(rs.getString("access_key"));
        credential.setSecretKey(rs.getString("secret_key"));
        credential.setRegion(rs.getString("region"));
        credential.setRemainingCredits(rs.getDouble("remaining_credits"));
        credential.setActive(rs.getBoolean("is_active"));
        credential.setValidated(rs.getBoolean("validated"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            credential.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            credential.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return credential;
    }
}
