package dao;

import database.DBConnection;
import models.ECSService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ECSDAO - Data Access Object for ECSService operations
 */
public class ECSDAO {
    private final Connection connection;
    
    public ECSDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }
    
    public boolean saveOrUpdateECSService(ECSService service) {
        if (serviceExists(service.getClusterName(), service.getServiceName())) {
            return updateECSService(service);
        } else {
            return insertECSService(service);
        }
    }
    
    private boolean serviceExists(String clusterName, String serviceName) {
        String query = "SELECT COUNT(*) FROM ecs_services WHERE cluster_name = ? AND service_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, clusterName);
            stmt.setString(2, serviceName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking ECS service existence: " + e.getMessage());
        }
        return false;
    }
    
    private boolean insertECSService(ECSService service) {
        String query = "INSERT INTO ecs_services (cluster_name, service_name, service_arn, status, " +
                      "desired_count, running_count, pending_count, task_definition, cpu_utilization, " +
                      "memory_utilization, is_idle, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, service.getClusterName());
            stmt.setString(2, service.getServiceName());
            stmt.setString(3, service.getServiceArn());
            stmt.setString(4, service.getStatus());
            stmt.setInt(5, service.getDesiredCount());
            stmt.setInt(6, service.getRunningCount());
            stmt.setInt(7, service.getPendingCount());
            stmt.setString(8, service.getTaskDefinition());
            stmt.setDouble(9, service.getCpuUtilization());
            stmt.setDouble(10, service.getMemoryUtilization());
            // Handle null Boolean for isIdle
            if (service.isIdle() != null) {
                stmt.setBoolean(11, service.isIdle());
            } else {
                stmt.setNull(11, java.sql.Types.BOOLEAN);
            }
            stmt.setInt(12, service.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting ECS service: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean updateECSService(ECSService service) {
        String query = "UPDATE ecs_services SET status = ?, desired_count = ?, running_count = ?, " +
                      "pending_count = ?, task_definition = ?, cpu_utilization = ?, memory_utilization = ?, " +
                      "is_idle = ?, last_checked = NOW() WHERE cluster_name = ? AND service_name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, service.getStatus());
            stmt.setInt(2, service.getDesiredCount());
            stmt.setInt(3, service.getRunningCount());
            stmt.setInt(4, service.getPendingCount());
            stmt.setString(5, service.getTaskDefinition());
            stmt.setDouble(6, service.getCpuUtilization());
            stmt.setDouble(7, service.getMemoryUtilization());
            // Handle null Boolean for isIdle
            if (service.isIdle() != null) {
                stmt.setBoolean(8, service.isIdle());
            } else {
                stmt.setNull(8, java.sql.Types.BOOLEAN);
            }
            stmt.setString(9, service.getClusterName());
            stmt.setString(10, service.getServiceName());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating ECS service: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public List<ECSService> getAllECSServices() {
        List<ECSService> services = new ArrayList<>();
        String query = "SELECT * FROM ecs_services ORDER BY last_checked DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                services.add(mapResultSetToECSService(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting ECS services: " + e.getMessage());
        }
        return services;
    }
    
    public int getTotalECSCount() {
        String query = "SELECT COUNT(*) FROM ecs_services";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting ECS count: " + e.getMessage());
        }
        return 0;
    }
    
    private ECSService mapResultSetToECSService(ResultSet rs) throws SQLException {
        ECSService service = new ECSService();
        service.setRecordId(rs.getInt("record_id"));
        service.setClusterName(rs.getString("cluster_name"));
        service.setServiceName(rs.getString("service_name"));
        service.setServiceArn(rs.getString("service_arn"));
        service.setStatus(rs.getString("status"));
        service.setDesiredCount(rs.getInt("desired_count"));
        service.setRunningCount(rs.getInt("running_count"));
        service.setPendingCount(rs.getInt("pending_count"));
        service.setTaskDefinition(rs.getString("task_definition"));
        service.setCpuUtilization(rs.getDouble("cpu_utilization"));
        service.setMemoryUtilization(rs.getDouble("memory_utilization"));
        service.setIdle(rs.getBoolean("is_idle"));
        
        Timestamp lastChecked = rs.getTimestamp("last_checked");
        if (lastChecked != null) {
            service.setLastChecked(lastChecked.toLocalDateTime());
        }
        
        service.setUserId(rs.getInt("user_id"));
        return service;
    }
}
