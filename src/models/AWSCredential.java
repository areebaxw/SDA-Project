package models;

import java.time.LocalDateTime;

/**
 * AWSCredential model class
 * Represents AWS credentials for API access
 */
public class AWSCredential {
    private int credentialId;
    private int userId;
    private String accessKey;
    private String secretKey;
    private String region;
    private boolean isActive;
    private boolean validated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public AWSCredential() {}
    
    public AWSCredential(int userId, String accessKey, String secretKey, String region) {
        this.userId = userId;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.isActive = true;
        this.validated = false;
    }
    
    // Getters and Setters
    public int getCredentialId() {
        return credentialId;
    }
    
    public void setCredentialId(int credentialId) {
        this.credentialId = credentialId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getAccessKey() {
        return accessKey;
    }
    
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isValidated() {
        return validated;
    }
    
    public void setValidated(boolean validated) {
        this.validated = validated;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "AWSCredential{" +
                "credentialId=" + credentialId +
                ", userId=" + userId +
                ", region='" + region + '\'' +
                ", isActive=" + isActive +
                ", validated=" + validated +
                '}';
    }
}
