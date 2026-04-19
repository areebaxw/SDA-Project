package dao;

import database.DBConnection;
import models.S3BucketResource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * S3BucketDAO - Data Access Object for S3 bucket monitoring data
 */
public class S3BucketDAO {
    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    public boolean saveOrUpdate(S3BucketResource bucket) {
        return exists(bucket.getBucketName()) ? update(bucket) : insert(bucket);
    }

    private boolean exists(String bucketName) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "SELECT COUNT(*) FROM s3_buckets WHERE bucket_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, bucketName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking S3 bucket existence: " + e.getMessage());
            return false;
        }
    }

    private boolean insert(S3BucketResource bucket) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "INSERT INTO s3_buckets (bucket_name, bucket_arn, region, object_count, total_size_gb, is_public, is_idle, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, bucket.getBucketName());
            stmt.setString(2, bucket.getBucketArn());
            stmt.setString(3, bucket.getRegion());
            stmt.setLong(4, bucket.getObjectCount());
            stmt.setDouble(5, bucket.getTotalSizeGb());
            if (bucket.getIsPublic() == null) stmt.setNull(6, Types.BOOLEAN); else stmt.setBoolean(6, bucket.getIsPublic());
            if (bucket.isIdle() == null) stmt.setNull(7, Types.BOOLEAN); else stmt.setBoolean(7, bucket.isIdle());
            stmt.setInt(8, bucket.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting S3 bucket: " + e.getMessage());
            return false;
        }
    }

    private boolean update(S3BucketResource bucket) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "UPDATE s3_buckets SET bucket_arn = ?, region = ?, object_count = ?, total_size_gb = ?, is_public = ?, is_idle = ?, last_checked = NOW() WHERE bucket_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, bucket.getBucketArn());
            stmt.setString(2, bucket.getRegion());
            stmt.setLong(3, bucket.getObjectCount());
            stmt.setDouble(4, bucket.getTotalSizeGb());
            if (bucket.getIsPublic() == null) stmt.setNull(5, Types.BOOLEAN); else stmt.setBoolean(5, bucket.getIsPublic());
            if (bucket.isIdle() == null) stmt.setNull(6, Types.BOOLEAN); else stmt.setBoolean(6, bucket.isIdle());
            stmt.setString(7, bucket.getBucketName());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating S3 bucket: " + e.getMessage());
            return false;
        }
    }

    public List<S3BucketResource> getAll() {
        List<S3BucketResource> list = new ArrayList<>();
        Connection connection = conn();
        if (connection == null) return list;
        String sql = "SELECT * FROM s3_buckets ORDER BY last_checked DESC";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                S3BucketResource b = new S3BucketResource();
                b.setRecordId(rs.getInt("record_id"));
                b.setBucketName(rs.getString("bucket_name"));
                b.setBucketArn(rs.getString("bucket_arn"));
                b.setRegion(rs.getString("region"));
                b.setObjectCount(rs.getLong("object_count"));
                b.setTotalSizeGb(rs.getDouble("total_size_gb"));
                b.setIsPublic((Boolean) rs.getObject("is_public"));
                b.setIdle((Boolean) rs.getObject("is_idle"));
                b.setUserId(rs.getInt("user_id"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching S3 buckets: " + e.getMessage());
        }
        return list;
    }

    public int getTotalCount() {
        Connection connection = conn();
        if (connection == null) return 0;
        String sql = "SELECT COUNT(*) FROM s3_buckets";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Error counting S3 buckets: " + e.getMessage());
            return 0;
        }
    }

    public boolean deleteByBucketName(String bucketName) {
        Connection connection = conn();
        if (connection == null) return false;
        String sql = "DELETE FROM s3_buckets WHERE bucket_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, bucketName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting S3 bucket from local cache: " + e.getMessage());
            return false;
        }
    }
}
