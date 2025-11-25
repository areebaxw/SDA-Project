package dao;

import database.DBConnection;
import models.Alert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AlertDAO - Data Access Object for Alert operations
 */
public class AlertDAO {
    private final Connection connection;
    
    public AlertDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }
    
    /**
     * Get all unresolved alerts
     */
    public List<Alert> getUnresolvedAlerts() {
        List<Alert> alerts = new ArrayList<>();
        String query = "SELECT * FROM alerts WHERE is_resolved = FALSE ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                alerts.add(mapResultSetToAlert(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting unresolved alerts: " + e.getMessage());
            e.printStackTrace();
        }
        return alerts;
    }
    
    /**
     * Get all alerts
     */
    public List<Alert> getAllAlerts() {
        List<Alert> alerts = new ArrayList<>();
        String query = "SELECT * FROM alerts ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                alerts.add(mapResultSetToAlert(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting alerts: " + e.getMessage());
            e.printStackTrace();
        }
        return alerts;
    }
    
    /**
     * Get alerts by resource type
     */
    public List<Alert> getAlertsByResourceType(String resourceType) {
        List<Alert> alerts = new ArrayList<>();
        String query = "SELECT * FROM alerts WHERE resource_type = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, resourceType);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                alerts.add(mapResultSetToAlert(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting alerts by resource type: " + e.getMessage());
            e.printStackTrace();
        }
        return alerts;
    }
    
    /**
     * Get alerts count by severity
     */
    public int getAlertCountBySeverity(String severity, boolean unresolvedOnly) {
        String query = "SELECT COUNT(*) FROM alerts WHERE severity = ?" + 
                      (unresolvedOnly ? " AND is_resolved = FALSE" : "");
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, severity);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting alert count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Create new alert
     */
    public boolean createAlert(Alert alert) {
        String query = "INSERT INTO alerts (resource_id, resource_type, alert_type, severity, message, rule_id, is_resolved) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, alert.getResourceId());
            stmt.setString(2, alert.getResourceType());
            stmt.setString(3, alert.getAlertType());
            stmt.setString(4, alert.getSeverity());
            stmt.setString(5, alert.getMessage());
            
            // Handle null or 0 rule_id - set to NULL for database
            if (alert.getRuleId() <= 0) {
                stmt.setNull(6, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(6, alert.getRuleId());
            }
            
            stmt.setBoolean(7, alert.isResolved());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating alert: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Resolve alert
     */
    public boolean resolveAlert(int alertId) {
        String query = "UPDATE alerts SET is_resolved = TRUE, resolved_at = NOW() WHERE alert_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, alertId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error resolving alert: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete alert
     */
    public boolean deleteAlert(int alertId) {
        String query = "DELETE FROM alerts WHERE alert_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, alertId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting alert: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get total alert count
     */
    public int getTotalAlertCount(boolean unresolvedOnly) {
        String query = "SELECT COUNT(*) FROM alerts" + (unresolvedOnly ? " WHERE is_resolved = FALSE" : "");
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total alert count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Map ResultSet to Alert object
     */
    private Alert mapResultSetToAlert(ResultSet rs) throws SQLException {
        Alert alert = new Alert();
        alert.setAlertId(rs.getInt("alert_id"));
        alert.setResourceId(rs.getString("resource_id"));
        alert.setResourceType(rs.getString("resource_type"));
        alert.setAlertType(rs.getString("alert_type"));
        alert.setSeverity(rs.getString("severity"));
        alert.setMessage(rs.getString("message"));
        alert.setRuleId(rs.getInt("rule_id"));
        alert.setResolved(rs.getBoolean("is_resolved"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            alert.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp resolvedAt = rs.getTimestamp("resolved_at");
        if (resolvedAt != null) {
            alert.setResolvedAt(resolvedAt.toLocalDateTime());
        }
        
        return alert;
    }
}
