package dao;

import database.DBConnection;
import models.SageMakerEndpoint;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SageMakerDAO - Data Access Object for SageMakerEndpoint operations
 */
public class SageMakerDAO {
    private final Connection connection;
    
    public SageMakerDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }
    
    public boolean saveOrUpdateEndpoint(SageMakerEndpoint endpoint) {
        if (endpointExists(endpoint.getEndpointName())) {
            return updateEndpoint(endpoint);
        } else {
            return insertEndpoint(endpoint);
        }
    }
    
    private boolean endpointExists(String endpointName) {
        String query = "SELECT COUNT(*) FROM sagemaker_endpoints WHERE endpoint_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, endpointName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking SageMaker endpoint existence: " + e.getMessage());
        }
        return false;
    }
    
    private boolean insertEndpoint(SageMakerEndpoint endpoint) {
        String query = "INSERT INTO sagemaker_endpoints (endpoint_name, endpoint_arn, endpoint_status, " +
                      "model_name, instance_type, instance_count, invocations, model_latency, is_idle, " +
                      "creation_time, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, endpoint.getEndpointName());
            stmt.setString(2, endpoint.getEndpointArn());
            stmt.setString(3, endpoint.getEndpointStatus());
            stmt.setString(4, endpoint.getModelName());
            stmt.setString(5, endpoint.getInstanceType());
            stmt.setInt(6, endpoint.getInstanceCount());
            stmt.setInt(7, endpoint.getInvocations());
            stmt.setDouble(8, endpoint.getModelLatency());
            stmt.setBoolean(9, endpoint.isIdle());
            stmt.setTimestamp(10, endpoint.getCreationTime() != null ? 
                            Timestamp.valueOf(endpoint.getCreationTime()) : null);
            stmt.setInt(11, endpoint.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting SageMaker endpoint: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean updateEndpoint(SageMakerEndpoint endpoint) {
        String query = "UPDATE sagemaker_endpoints SET endpoint_status = ?, model_name = ?, " +
                      "instance_type = ?, instance_count = ?, invocations = ?, model_latency = ?, " +
                      "is_idle = ?, last_checked = NOW() WHERE endpoint_name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, endpoint.getEndpointStatus());
            stmt.setString(2, endpoint.getModelName());
            stmt.setString(3, endpoint.getInstanceType());
            stmt.setInt(4, endpoint.getInstanceCount());
            stmt.setInt(5, endpoint.getInvocations());
            stmt.setDouble(6, endpoint.getModelLatency());
            stmt.setBoolean(7, endpoint.isIdle());
            stmt.setString(8, endpoint.getEndpointName());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating SageMaker endpoint: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public List<SageMakerEndpoint> getAllEndpoints() {
        List<SageMakerEndpoint> endpoints = new ArrayList<>();
        String query = "SELECT * FROM sagemaker_endpoints ORDER BY last_checked DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                endpoints.add(mapResultSetToEndpoint(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting SageMaker endpoints: " + e.getMessage());
        }
        return endpoints;
    }
    
    public int getTotalEndpointCount() {
        String query = "SELECT COUNT(*) FROM sagemaker_endpoints";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting SageMaker count: " + e.getMessage());
        }
        return 0;
    }
    
    private SageMakerEndpoint mapResultSetToEndpoint(ResultSet rs) throws SQLException {
        SageMakerEndpoint endpoint = new SageMakerEndpoint();
        endpoint.setRecordId(rs.getInt("record_id"));
        endpoint.setEndpointName(rs.getString("endpoint_name"));
        endpoint.setEndpointArn(rs.getString("endpoint_arn"));
        endpoint.setEndpointStatus(rs.getString("endpoint_status"));
        endpoint.setModelName(rs.getString("model_name"));
        endpoint.setInstanceType(rs.getString("instance_type"));
        endpoint.setInstanceCount(rs.getInt("instance_count"));
        endpoint.setInvocations(rs.getInt("invocations"));
        endpoint.setModelLatency(rs.getDouble("model_latency"));
        endpoint.setIdle(rs.getBoolean("is_idle"));
        
        Timestamp creationTime = rs.getTimestamp("creation_time");
        if (creationTime != null) {
            endpoint.setCreationTime(creationTime.toLocalDateTime());
        }
        
        Timestamp lastChecked = rs.getTimestamp("last_checked");
        if (lastChecked != null) {
            endpoint.setLastChecked(lastChecked.toLocalDateTime());
        }
        
        endpoint.setUserId(rs.getInt("user_id"));
        return endpoint;
    }
}
