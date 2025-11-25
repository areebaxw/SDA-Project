package models;

import java.time.LocalDateTime;

/**
 * SageMakerEndpoint model class
 * Represents an AWS SageMaker endpoint
 */
public class SageMakerEndpoint {
    private int recordId;
    private String endpointName;
    private String endpointArn;
    private String endpointStatus;
    private String modelName;
    private String instanceType;
    private int instanceCount;
    private int invocations;
    private double modelLatency;
    private Boolean isIdle;
    private LocalDateTime creationTime;
    private LocalDateTime lastChecked;
    private String resourceType; // 'endpoint' or 'notebook'
    private int userId;
    
    // Constructors
    public SageMakerEndpoint() {}
    
    public SageMakerEndpoint(String endpointName, String endpointStatus, String resourceType) {
        this.endpointName = endpointName;
        this.endpointStatus = endpointStatus;
        this.resourceType = resourceType;
    }
    
    // Getters and Setters
    public int getRecordId() {
        return recordId;
    }
    
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }
    
    public String getEndpointName() {
        return endpointName;
    }
    
    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }
    
    public String getEndpointArn() {
        return endpointArn;
    }
    
    public void setEndpointArn(String endpointArn) {
        this.endpointArn = endpointArn;
    }
    
    public String getEndpointStatus() {
        return endpointStatus;
    }
    
    public void setEndpointStatus(String endpointStatus) {
        this.endpointStatus = endpointStatus;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public String getInstanceType() {
        return instanceType;
    }
    
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }
    
    public int getInstanceCount() {
        return instanceCount;
    }
    
    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }
    
    public int getInvocations() {
        return invocations;
    }
    
    public void setInvocations(int invocations) {
        this.invocations = invocations;
    }
    
    public double getModelLatency() {
        return modelLatency;
    }
    
    public void setModelLatency(double modelLatency) {
        this.modelLatency = modelLatency;
    }
    
    public Boolean isIdle() {
        return isIdle;
    }
    
    public void setIdle(Boolean idle) {
        isIdle = idle;
    }
    
    public LocalDateTime getCreationTime() {
        return creationTime;
    }
    
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }
    
    public LocalDateTime getLastChecked() {
        return lastChecked;
    }
    
    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "SageMakerEndpoint{" +
                "endpointName='" + endpointName + '\'' +
                ", endpointStatus='" + endpointStatus + '\'' +
                ", modelName='" + modelName + '\'' +
                ", instanceType='" + instanceType + '\'' +
                ", invocations=" + invocations +
                ", isIdle=" + isIdle +
                '}';
    }
}
