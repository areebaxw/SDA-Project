package dao;

import database.DBConnection;
import models.BillingRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * BillingDAO - Data Access Object for BillingRecord operations
 */
public class BillingDAO {
    private final Connection connection;
    
    public BillingDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }
    
    /**
     * Get all billing records for a user
     */
    public List<BillingRecord> getBillingRecordsByUser(int userId) {
        List<BillingRecord> records = new ArrayList<>();
        String query = "SELECT * FROM billing_records WHERE user_id = ? ORDER BY end_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                records.add(mapResultSetToBillingRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting billing records: " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }
    
    /**
     * Get billing records by date range
     */
    public List<BillingRecord> getBillingRecordsByDateRange(int userId, Date startDate, Date endDate) {
        List<BillingRecord> records = new ArrayList<>();
        String query = "SELECT * FROM billing_records WHERE user_id = ? AND start_date >= ? AND end_date <= ? ORDER BY end_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                records.add(mapResultSetToBillingRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting billing records by date: " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }
    
    /**
     * Get cost by service for a date range
     */
    public Map<String, Double> getCostByService(int userId, Date startDate, Date endDate) {
        Map<String, Double> costMap = new HashMap<>();
        String query = "SELECT service_name, SUM(cost_amount) as total_cost " +
                      "FROM billing_records WHERE user_id = ? AND start_date >= ? AND end_date <= ? " +
                      "GROUP BY service_name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                costMap.put(rs.getString("service_name"), rs.getDouble("total_cost"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting cost by service: " + e.getMessage());
            e.printStackTrace();
        }
        return costMap;
    }
    
    /**
     * Get total cost for a date range
     */
    public double getTotalCost(int userId, Date startDate, Date endDate) {
        String query = "SELECT SUM(cost_amount) as total FROM billing_records " +
                      "WHERE user_id = ? AND start_date >= ? AND end_date <= ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total cost: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
    
    /**
     * Insert billing record
     */
    public boolean insertBillingRecord(BillingRecord record) {
        String query = "INSERT INTO billing_records (user_id, service_name, cost_amount, currency, " +
                      "start_date, end_date, record_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, record.getUserId());
            stmt.setString(2, record.getServiceName());
            stmt.setDouble(3, record.getCostAmount());
            stmt.setString(4, record.getCurrency());
            stmt.setDate(5, Date.valueOf(record.getStartDate()));
            stmt.setDate(6, Date.valueOf(record.getEndDate()));
            stmt.setString(7, record.getRecordType());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting billing record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get monthly cost trend (last N months)
     */
    public List<BillingRecord> getMonthlyCostTrend(int userId, int months) {
        List<BillingRecord> records = new ArrayList<>();
        String query = "SELECT service_name, SUM(cost_amount) as cost_amount, " +
                      "DATE_FORMAT(start_date, '%Y-%m') as month, MIN(start_date) as start_date, MAX(end_date) as end_date " +
                      "FROM billing_records WHERE user_id = ? AND start_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                      "GROUP BY service_name, month ORDER BY month DESC, service_name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, months);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BillingRecord record = new BillingRecord();
                record.setUserId(userId);
                record.setServiceName(rs.getString("service_name"));
                record.setCostAmount(rs.getDouble("cost_amount"));
                record.setStartDate(rs.getDate("start_date").toLocalDate());
                record.setEndDate(rs.getDate("end_date").toLocalDate());
                records.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Error getting monthly cost trend: " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }
    
    /**
     * Map ResultSet to BillingRecord object
     */
    private BillingRecord mapResultSetToBillingRecord(ResultSet rs) throws SQLException {
        BillingRecord record = new BillingRecord();
        record.setRecordId(rs.getInt("record_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setServiceName(rs.getString("service_name"));
        record.setCostAmount(rs.getDouble("cost_amount"));
        record.setCurrency(rs.getString("currency"));
        
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            record.setStartDate(startDate.toLocalDate());
        }
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            record.setEndDate(endDate.toLocalDate());
        }
        
        record.setRecordType(rs.getString("record_type"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            record.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return record;
    }
}
