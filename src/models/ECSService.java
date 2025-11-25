package models;

import java.time.LocalDateTime;

/**
 * ECSService model class
 * Represents an AWS ECS service
 */
public class ECSService {
    private int recordId;
    private String clusterName;
    private String serviceName;
    private String serviceArn;
    private String status;
    private int desiredCount;
    private int runningCount;
    private int pendingCount;
    private String taskDefinition;
    private double cpuUtilization;
    private double memoryUtilization;
    private Boolean isIdle;
    private LocalDateTime lastChecked;
    private int userId;
    
    // Constructors
    public ECSService() {}
    
    public ECSService(String clusterName, String serviceName, String status) {
        this.clusterName = clusterName;
        this.serviceName = serviceName;
        this.status = status;
    }
    
    // Getters and Setters
    public int getRecordId() {
        return recordId;
    }
    
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }
    
    public String getClusterName() {
        return clusterName;
    }
    
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getServiceArn() {
        return serviceArn;
    }
    
    public void setServiceArn(String serviceArn) {
        this.serviceArn = serviceArn;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getDesiredCount() {
        return desiredCount;
    }
    
    public void setDesiredCount(int desiredCount) {
        this.desiredCount = desiredCount;
    }
    
    public int getRunningCount() {
        return runningCount;
    }
    
    public void setRunningCount(int runningCount) {
        this.runningCount = runningCount;
    }
    
    public int getPendingCount() {
        return pendingCount;
    }
    
    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }
    
    public String getTaskDefinition() {
        return taskDefinition;
    }
    
    public void setTaskDefinition(String taskDefinition) {
        this.taskDefinition = taskDefinition;
    }
    
    public double getCpuUtilization() {
        return cpuUtilization;
    }
    
    public void setCpuUtilization(double cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
    }
    
    public double getMemoryUtilization() {
        return memoryUtilization;
    }
    
    public void setMemoryUtilization(double memoryUtilization) {
        this.memoryUtilization = memoryUtilization;
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
    
    @Override
    public String toString() {
        return "ECSService{" +
                "clusterName='" + clusterName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", status='" + status + '\'' +
                ", desiredCount=" + desiredCount +
                ", runningCount=" + runningCount +
                ", isIdle=" + isIdle +
                '}';
    }
}
