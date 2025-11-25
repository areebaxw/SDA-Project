package models;

import java.time.LocalDateTime;

/**
 * EC2Instance model class
 * Represents an AWS EC2 instance
 */
public class EC2Instance {
    private int recordId;
    private String instanceId;
    private String instanceType;
    private String instanceState;
    private String availabilityZone;
    private LocalDateTime launchTime;
    private double cpuUtilization;
    private double networkIn;
    private double networkOut;
    private Boolean isIdle;
    private LocalDateTime lastChecked;
    private int userId;
    
    // Constructors
    public EC2Instance() {}
    
    public EC2Instance(String instanceId, String instanceType, String instanceState) {
        this.instanceId = instanceId;
        this.instanceType = instanceType;
        this.instanceState = instanceState;
    }
    
    // Getters and Setters
    public int getRecordId() {
        return recordId;
    }
    
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    
    public String getInstanceType() {
        return instanceType;
    }
    
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }
    
    public String getInstanceState() {
        return instanceState;
    }
    
    public void setInstanceState(String instanceState) {
        this.instanceState = instanceState;
    }
    
    public String getAvailabilityZone() {
        return availabilityZone;
    }
    
    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }
    
    public LocalDateTime getLaunchTime() {
        return launchTime;
    }
    
    public void setLaunchTime(LocalDateTime launchTime) {
        this.launchTime = launchTime;
    }
    
    public double getCpuUtilization() {
        return cpuUtilization;
    }
    
    public void setCpuUtilization(double cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
    }
    
    public double getNetworkIn() {
        return networkIn;
    }
    
    public void setNetworkIn(double networkIn) {
        this.networkIn = networkIn;
    }
    
    public double getNetworkOut() {
        return networkOut;
    }
    
    public void setNetworkOut(double networkOut) {
        this.networkOut = networkOut;
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
        return "EC2Instance{" +
                "instanceId='" + instanceId + '\'' +
                ", instanceType='" + instanceType + '\'' +
                ", instanceState='" + instanceState + '\'' +
                ", cpuUtilization=" + cpuUtilization +
                ", isIdle=" + isIdle +
                '}';
    }
}
