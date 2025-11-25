package dao;

import database.DBConnection;
import models.RDSInstance;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RDSDAO - Data Access Object for RDSInstance operations
 */
public class RDSDAO {
    private final Connection connection;
    
    public RDSDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }
    
    /**
     * Save or update RDS instance
     */
    public boolean saveOrUpdateRDSInstance(RDSInstance instance) {
        if (instanceExists(instance.getDbInstanceIdentifier())) {
            return updateRDSInstance(instance);
        } else {
            return insertRDSInstance(instance);
        }
    }
    
    private boolean instanceExists(String identifier) {
        String query = "SELECT COUNT(*) FROM rds_instances WHERE db_instance_identifier = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, identifier);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking RDS instance existence: " + e.getMessage());
        }
        return false;
    }
    
    private boolean insertRDSInstance(RDSInstance instance) {
        String query = "INSERT INTO rds_instances (db_instance_identifier, db_instance_class, engine, " +
                      "engine_version, db_instance_status, allocated_storage, availability_zone, " +
                      "cpu_utilization, database_connections, is_idle, user_id) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, instance.getDbInstanceIdentifier());
            stmt.setString(2, instance.getDbInstanceClass());
            stmt.setString(3, instance.getEngine());
            stmt.setString(4, instance.getEngineVersion());
            stmt.setString(5, instance.getDbInstanceStatus());
            stmt.setInt(6, instance.getAllocatedStorage());
            stmt.setString(7, instance.getAvailabilityZone());
            stmt.setDouble(8, instance.getCpuUtilization());
            stmt.setInt(9, instance.getDatabaseConnections());
            // Handle null Boolean for isIdle
            if (instance.isIdle() != null) {
                stmt.setBoolean(10, instance.isIdle());
            } else {
                stmt.setNull(10, java.sql.Types.BOOLEAN);
            }
            stmt.setInt(11, instance.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting RDS instance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean updateRDSInstance(RDSInstance instance) {
        String query = "UPDATE rds_instances SET db_instance_class = ?, engine = ?, engine_version = ?, " +
                      "db_instance_status = ?, allocated_storage = ?, availability_zone = ?, " +
                      "cpu_utilization = ?, database_connections = ?, is_idle = ?, last_checked = NOW() " +
                      "WHERE db_instance_identifier = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, instance.getDbInstanceClass());
            stmt.setString(2, instance.getEngine());
            stmt.setString(3, instance.getEngineVersion());
            stmt.setString(4, instance.getDbInstanceStatus());
            stmt.setInt(5, instance.getAllocatedStorage());
            stmt.setString(6, instance.getAvailabilityZone());
            stmt.setDouble(7, instance.getCpuUtilization());
            stmt.setInt(8, instance.getDatabaseConnections());
            // Handle null Boolean for isIdle
            if (instance.isIdle() != null) {
                stmt.setBoolean(9, instance.isIdle());
            } else {
                stmt.setNull(9, java.sql.Types.BOOLEAN);
            }
            stmt.setString(10, instance.getDbInstanceIdentifier());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating RDS instance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public List<RDSInstance> getAllRDSInstances() {
        List<RDSInstance> instances = new ArrayList<>();
        String query = "SELECT * FROM rds_instances ORDER BY last_checked DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                instances.add(mapResultSetToRDSInstance(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting RDS instances: " + e.getMessage());
        }
        return instances;
    }
    
    public int getTotalRDSCount() {
        String query = "SELECT COUNT(*) FROM rds_instances";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting RDS count: " + e.getMessage());
        }
        return 0;
    }
    
    private RDSInstance mapResultSetToRDSInstance(ResultSet rs) throws SQLException {
        RDSInstance instance = new RDSInstance();
        instance.setRecordId(rs.getInt("record_id"));
        instance.setDbInstanceIdentifier(rs.getString("db_instance_identifier"));
        instance.setDbInstanceClass(rs.getString("db_instance_class"));
        instance.setEngine(rs.getString("engine"));
        instance.setEngineVersion(rs.getString("engine_version"));
        instance.setDbInstanceStatus(rs.getString("db_instance_status"));
        instance.setAllocatedStorage(rs.getInt("allocated_storage"));
        instance.setAvailabilityZone(rs.getString("availability_zone"));
        instance.setCpuUtilization(rs.getDouble("cpu_utilization"));
        instance.setDatabaseConnections(rs.getInt("database_connections"));
        instance.setIdle(rs.getBoolean("is_idle"));
        
        Timestamp lastChecked = rs.getTimestamp("last_checked");
        if (lastChecked != null) {
            instance.setLastChecked(lastChecked.toLocalDateTime());
        }
        
        instance.setUserId(rs.getInt("user_id"));
        return instance;
    }
}
