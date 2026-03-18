package dao;

import database.DBConnection;
import models.ALBResource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ALBDAO - Data Access Object for ALB monitoring data
 */
public class ALBDAO {
    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    public boolean saveOrUpdate(ALBResource alb) {
        return exists(alb.getLoadBalancerArn()) ? update(alb) : insert(alb);
    }

    private boolean exists(String arn) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "SELECT COUNT(*) FROM alb_resources WHERE load_balancer_arn = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, arn);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking ALB existence: " + e.getMessage());
            return false;
        }
    }

    private boolean insert(ALBResource alb) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "INSERT INTO alb_resources (load_balancer_name, load_balancer_arn, dns_name, scheme, state, request_count, is_idle, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, alb.getLoadBalancerName());
            stmt.setString(2, alb.getLoadBalancerArn());
            stmt.setString(3, alb.getDnsName());
            stmt.setString(4, alb.getScheme());
            stmt.setString(5, alb.getState());
            stmt.setLong(6, alb.getRequestCount());
            if (alb.isIdle() == null) stmt.setNull(7, Types.BOOLEAN); else stmt.setBoolean(7, alb.isIdle());
            stmt.setInt(8, alb.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting ALB: " + e.getMessage());
            return false;
        }
    }

    private boolean update(ALBResource alb) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "UPDATE alb_resources SET load_balancer_name = ?, dns_name = ?, scheme = ?, state = ?, request_count = ?, is_idle = ?, last_checked = NOW() WHERE load_balancer_arn = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, alb.getLoadBalancerName());
            stmt.setString(2, alb.getDnsName());
            stmt.setString(3, alb.getScheme());
            stmt.setString(4, alb.getState());
            stmt.setLong(5, alb.getRequestCount());
            if (alb.isIdle() == null) stmt.setNull(6, Types.BOOLEAN); else stmt.setBoolean(6, alb.isIdle());
            stmt.setString(7, alb.getLoadBalancerArn());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating ALB: " + e.getMessage());
            return false;
        }
    }

    public List<ALBResource> getAll() {
        List<ALBResource> list = new ArrayList<>();
        Connection connection = conn();
        if (connection == null) return list;
        String sql = "SELECT * FROM alb_resources ORDER BY last_checked DESC";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ALBResource a = new ALBResource();
                a.setRecordId(rs.getInt("record_id"));
                a.setLoadBalancerName(rs.getString("load_balancer_name"));
                a.setLoadBalancerArn(rs.getString("load_balancer_arn"));
                a.setDnsName(rs.getString("dns_name"));
                a.setScheme(rs.getString("scheme"));
                a.setState(rs.getString("state"));
                a.setRequestCount(rs.getLong("request_count"));
                a.setIdle((Boolean) rs.getObject("is_idle"));
                a.setUserId(rs.getInt("user_id"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ALBs: " + e.getMessage());
        }
        return list;
    }

    public int getTotalCount() {
        Connection connection = conn();
        if (connection == null) return 0;
        String sql = "SELECT COUNT(*) FROM alb_resources";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Error counting ALBs: " + e.getMessage());
            return 0;
        }
    }

    public boolean deleteByArn(String arn) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "DELETE FROM alb_resources WHERE load_balancer_arn = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, arn);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting ALB from local cache: " + e.getMessage());
            return false;
        }
    }
}
