package dao;

import database.DBConnection;
import models.SQSQueueResource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQSQueueDAO - Data Access Object for SQS queue monitoring data
 */
public class SQSQueueDAO {
    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    public boolean saveOrUpdate(SQSQueueResource queue) {
        return exists(queue.getQueueUrl()) ? update(queue) : insert(queue);
    }

    private boolean exists(String queueUrl) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "SELECT COUNT(*) FROM sqs_queues WHERE queue_url = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, queueUrl);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking SQS queue existence: " + e.getMessage());
            return false;
        }
    }

    private boolean insert(SQSQueueResource queue) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "INSERT INTO sqs_queues (queue_name, queue_url, queue_arn, message_count, delayed_message_count, is_idle, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, queue.getQueueName());
            stmt.setString(2, queue.getQueueUrl());
            stmt.setString(3, queue.getQueueArn());
            stmt.setLong(4, queue.getMessageCount());
            stmt.setLong(5, queue.getDelayedMessageCount());
            if (queue.isIdle() == null) stmt.setNull(6, Types.BOOLEAN); else stmt.setBoolean(6, queue.isIdle());
            stmt.setInt(7, queue.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting SQS queue: " + e.getMessage());
            return false;
        }
    }

    private boolean update(SQSQueueResource queue) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "UPDATE sqs_queues SET queue_name = ?, queue_arn = ?, message_count = ?, delayed_message_count = ?, is_idle = ?, last_checked = NOW() WHERE queue_url = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, queue.getQueueName());
            stmt.setString(2, queue.getQueueArn());
            stmt.setLong(3, queue.getMessageCount());
            stmt.setLong(4, queue.getDelayedMessageCount());
            if (queue.isIdle() == null) stmt.setNull(5, Types.BOOLEAN); else stmt.setBoolean(5, queue.isIdle());
            stmt.setString(6, queue.getQueueUrl());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating SQS queue: " + e.getMessage());
            return false;
        }
    }

    public List<SQSQueueResource> getAll() {
        List<SQSQueueResource> list = new ArrayList<>();
        Connection connection = conn();
        if (connection == null) return list;
        String sql = "SELECT * FROM sqs_queues ORDER BY last_checked DESC";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SQSQueueResource q = new SQSQueueResource();
                q.setRecordId(rs.getInt("record_id"));
                q.setQueueName(rs.getString("queue_name"));
                q.setQueueUrl(rs.getString("queue_url"));
                q.setQueueArn(rs.getString("queue_arn"));
                q.setMessageCount(rs.getLong("message_count"));
                q.setDelayedMessageCount(rs.getLong("delayed_message_count"));
                q.setIdle((Boolean) rs.getObject("is_idle"));
                q.setUserId(rs.getInt("user_id"));
                list.add(q);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching SQS queues: " + e.getMessage());
        }
        return list;
    }

    public int getTotalCount() {
        Connection connection = conn();
        if (connection == null) return 0;
        String sql = "SELECT COUNT(*) FROM sqs_queues";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Error counting SQS queues: " + e.getMessage());
            return 0;
        }
    }

    public boolean deleteByQueueUrl(String queueUrl) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "DELETE FROM sqs_queues WHERE queue_url = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, queueUrl);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting SQS queue from local cache: " + e.getMessage());
            return false;
        }
    }
}
