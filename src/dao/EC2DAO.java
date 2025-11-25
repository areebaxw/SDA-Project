package dao;

import database.DBConnection;
import models.EC2Instance;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EC2DAO - Data Access Object for EC2Instance operations
 */
public class EC2DAO {
    private final Connection connection;
    
    public EC2DAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }
    
    /**
     * Save or update EC2 instance
     */
    public boolean saveOrUpdateEC2Instance(EC2Instance instance) {
        // Check if instance exists
        if (instanceExists(instance.getInstanceId())) {
            return updateEC2Instance(instance);
        } else {
            return insertEC2Instance(instance);
        }
    }
    
    /**
     * Check if instance exists
     */
    private boolean instanceExists(String instanceId) {
        String query = "SELECT COUNT(*) FROM ec2_instances WHERE instance_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, instanceId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking instance existence: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Insert new EC2 instance
     */
    private boolean insertEC2Instance(EC2Instance instance) {
        String query = "INSERT INTO ec2_instances (instance_id, instance_type, instance_state, " +
                      "availability_zone, launch_time, cpu_utilization, network_in, network_out, " +
                      "is_idle, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, instance.getInstanceId());
            stmt.setString(2, instance.getInstanceType());
            stmt.setString(3, instance.getInstanceState());
            stmt.setString(4, instance.getAvailabilityZone());
            stmt.setTimestamp(5, instance.getLaunchTime() != null ? 
                            Timestamp.valueOf(instance.getLaunchTime()) : null);
            stmt.setDouble(6, instance.getCpuUtilization());
            stmt.setDouble(7, instance.getNetworkIn());
            stmt.setDouble(8, instance.getNetworkOut());
            // Handle null Boolean for isIdle
            if (instance.isIdle() != null) {
                stmt.setBoolean(9, instance.isIdle());
            } else {
                stmt.setNull(9, java.sql.Types.BOOLEAN);
            }
            stmt.setInt(10, instance.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting EC2 instance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update existing EC2 instance
     */
    private boolean updateEC2Instance(EC2Instance instance) {
        String query = "UPDATE ec2_instances SET instance_type = ?, instance_state = ?, " +
                      "availability_zone = ?, cpu_utilization = ?, network_in = ?, network_out = ?, " +
                      "is_idle = ?, last_checked = NOW() WHERE instance_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, instance.getInstanceType());
            stmt.setString(2, instance.getInstanceState());
            stmt.setString(3, instance.getAvailabilityZone());
            stmt.setDouble(4, instance.getCpuUtilization());
            stmt.setDouble(5, instance.getNetworkIn());
            stmt.setDouble(6, instance.getNetworkOut());
            // Handle null Boolean for isIdle
            if (instance.isIdle() != null) {
                stmt.setBoolean(7, instance.isIdle());
            } else {
                stmt.setNull(7, java.sql.Types.BOOLEAN);
            }
            stmt.setString(8, instance.getInstanceId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating EC2 instance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get all EC2 instances
     */
    public List<EC2Instance> getAllEC2Instances() {
        List<EC2Instance> instances = new ArrayList<>();
        String query = "SELECT * FROM ec2_instances ORDER BY last_checked DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                instances.add(mapResultSetToEC2Instance(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting EC2 instances: " + e.getMessage());
            e.printStackTrace();
        }
        return instances;
    }
    
    /**
     * Get EC2 instance by ID
     */
    public EC2Instance getEC2InstanceById(String instanceId) {
        String query = "SELECT * FROM ec2_instances WHERE instance_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, instanceId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEC2Instance(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting EC2 instance: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get idle EC2 instances
     */
    public List<EC2Instance> getIdleEC2Instances() {
        List<EC2Instance> instances = new ArrayList<>();
        String query = "SELECT * FROM ec2_instances WHERE is_idle = TRUE";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                instances.add(mapResultSetToEC2Instance(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting idle EC2 instances: " + e.getMessage());
            e.printStackTrace();
        }
        return instances;
    }
    
    /**
     * Get total EC2 instance count
     */
    public int getTotalEC2Count() {
        String query = "SELECT COUNT(*) FROM ec2_instances";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting EC2 count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Map ResultSet to EC2Instance object
     */
    private EC2Instance mapResultSetToEC2Instance(ResultSet rs) throws SQLException {
        EC2Instance instance = new EC2Instance();
        instance.setRecordId(rs.getInt("record_id"));
        instance.setInstanceId(rs.getString("instance_id"));
        instance.setInstanceType(rs.getString("instance_type"));
        instance.setInstanceState(rs.getString("instance_state"));
        instance.setAvailabilityZone(rs.getString("availability_zone"));
        
        Timestamp launchTime = rs.getTimestamp("launch_time");
        if (launchTime != null) {
            instance.setLaunchTime(launchTime.toLocalDateTime());
        }
        
        instance.setCpuUtilization(rs.getDouble("cpu_utilization"));
        instance.setNetworkIn(rs.getDouble("network_in"));
        instance.setNetworkOut(rs.getDouble("network_out"));
        instance.setIdle(rs.getBoolean("is_idle"));
        
        Timestamp lastChecked = rs.getTimestamp("last_checked");
        if (lastChecked != null) {
            instance.setLastChecked(lastChecked.toLocalDateTime());
        }
        
        instance.setUserId(rs.getInt("user_id"));
        return instance;
    }
}
