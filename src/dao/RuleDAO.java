package dao;

import database.DBConnection;
import models.Rule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RuleDAO - Data Access Object for Rule operations
 */
public class RuleDAO {
    private final Connection connection;
    
    public RuleDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }
    
    /**
     * Get all active rules
     */
    public List<Rule> getAllActiveRules() {
        List<Rule> rules = new ArrayList<>();
        String query = "SELECT * FROM rules WHERE is_active = TRUE";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                rules.add(mapResultSetToRule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active rules: " + e.getMessage());
            e.printStackTrace();
        }
        return rules;
    }
    
    /**
     * Get all rules
     */
    public List<Rule> getAllRules() {
        List<Rule> rules = new ArrayList<>();
        String query = "SELECT * FROM rules ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                rules.add(mapResultSetToRule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting rules: " + e.getMessage());
            e.printStackTrace();
        }
        return rules;
    }
    
    /**
     * Get rules by resource type
     */
    public List<Rule> getRulesByResourceType(String resourceType) {
        List<Rule> rules = new ArrayList<>();
        String query = "SELECT * FROM rules WHERE resource_type = ? AND is_active = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, resourceType);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                rules.add(mapResultSetToRule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting rules by resource type: " + e.getMessage());
            e.printStackTrace();
        }
        return rules;
    }
    
    /**
     * Create new rule
     */
    public boolean createRule(Rule rule) {
        String query = "INSERT INTO rules (rule_name, rule_type, resource_type, condition_metric, " +
                      "condition_operator, condition_value, condition_duration, action_type, is_active, created_by) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, rule.getRuleName());
            stmt.setString(2, rule.getRuleType());
            stmt.setString(3, rule.getResourceType());
            stmt.setString(4, rule.getConditionMetric());
            stmt.setString(5, rule.getConditionOperator());
            stmt.setDouble(6, rule.getConditionValue());
            stmt.setInt(7, rule.getConditionDuration());
            stmt.setString(8, rule.getActionType());
            stmt.setBoolean(9, rule.isActive());
            stmt.setInt(10, rule.getCreatedBy());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating rule: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update rule
     */
    public boolean updateRule(Rule rule) {
        String query = "UPDATE rules SET rule_name = ?, rule_type = ?, resource_type = ?, " +
                      "condition_metric = ?, condition_operator = ?, condition_value = ?, " +
                      "condition_duration = ?, action_type = ?, is_active = ? WHERE rule_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, rule.getRuleName());
            stmt.setString(2, rule.getRuleType());
            stmt.setString(3, rule.getResourceType());
            stmt.setString(4, rule.getConditionMetric());
            stmt.setString(5, rule.getConditionOperator());
            stmt.setDouble(6, rule.getConditionValue());
            stmt.setInt(7, rule.getConditionDuration());
            stmt.setString(8, rule.getActionType());
            stmt.setBoolean(9, rule.isActive());
            stmt.setInt(10, rule.getRuleId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating rule: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete rule
     */
    public boolean deleteRule(int ruleId) {
        String query = "DELETE FROM rules WHERE rule_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ruleId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting rule: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Toggle rule active status
     */
    public boolean toggleRuleStatus(int ruleId) {
        String query = "UPDATE rules SET is_active = NOT is_active WHERE rule_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ruleId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error toggling rule status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Map ResultSet to Rule object
     */
    private Rule mapResultSetToRule(ResultSet rs) throws SQLException {
        Rule rule = new Rule();
        rule.setRuleId(rs.getInt("rule_id"));
        rule.setRuleName(rs.getString("rule_name"));
        rule.setRuleType(rs.getString("rule_type"));
        rule.setResourceType(rs.getString("resource_type"));
        rule.setConditionMetric(rs.getString("condition_metric"));
        rule.setConditionOperator(rs.getString("condition_operator"));
        rule.setConditionValue(rs.getDouble("condition_value"));
        rule.setConditionDuration(rs.getInt("condition_duration"));
        rule.setActionType(rs.getString("action_type"));
        rule.setActive(rs.getBoolean("is_active"));
        rule.setCreatedBy(rs.getInt("created_by"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            rule.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return rule;
    }
}
