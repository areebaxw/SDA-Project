package models;

import java.time.LocalDateTime;

/**
 * S3BucketResource model class
 * Represents an AWS S3 bucket being monitored
 */
public class S3BucketResource {
    private int recordId;
    private String bucketName;
    private String bucketArn;
    private String region;
    private long objectCount;
    private double totalSizeGb;
    private Boolean isPublic;
    private Boolean isIdle;
    private LocalDateTime lastChecked;
    private int userId;

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketArn() {
        return bucketArn;
    }

    public void setBucketArn(String bucketArn) {
        this.bucketArn = bucketArn;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getObjectCount() {
        return objectCount;
    }

    public void setObjectCount(long objectCount) {
        this.objectCount = objectCount;
    }

    public double getTotalSizeGb() {
        return totalSizeGb;
    }

    public void setTotalSizeGb(double totalSizeGb) {
        this.totalSizeGb = totalSizeGb;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public Boolean isIdle() {
        return isIdle;
    }

    public void setIdle(Boolean idle) {
        isIdle = idle;
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
