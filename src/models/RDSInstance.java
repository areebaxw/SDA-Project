package models;

import java.time.LocalDateTime;

/**
 * RDSInstance model class
 * Represents an AWS RDS database instance
 */
public class RDSInstance {
    private int recordId;
    private String dbInstanceIdentifier;
    private String dbInstanceClass;
    private String engine;
    private String engineVersion;
    private String dbInstanceStatus;
    private int allocatedStorage;
    private String availabilityZone;
    private double cpuUtilization;
    private int databaseConnections;
    private Boolean isIdle;
    private LocalDateTime lastChecked;
    private int userId;
    
    // Constructors
    public RDSInstance() {}
    
    public RDSInstance(String dbInstanceIdentifier, String dbInstanceClass, String engine) {
        this.dbInstanceIdentifier = dbInstanceIdentifier;
        this.dbInstanceClass = dbInstanceClass;
        this.engine = engine;
    }
    
    // Getters and Setters
    public int getRecordId() {
        return recordId;
    }
    
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }
    
    public String getDbInstanceIdentifier() {
        return dbInstanceIdentifier;
    }
    
    public void setDbInstanceIdentifier(String dbInstanceIdentifier) {
        this.dbInstanceIdentifier = dbInstanceIdentifier;
    }
    
    public String getDbInstanceClass() {
        return dbInstanceClass;
    }
    
    public void setDbInstanceClass(String dbInstanceClass) {
        this.dbInstanceClass = dbInstanceClass;
    }
    
    public String getEngine() {
        return engine;
    }
    
    public void setEngine(String engine) {
        this.engine = engine;
    }
    
    public String getEngineVersion() {
        return engineVersion;
    }
    
    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }
    
    public String getDbInstanceStatus() {
        return dbInstanceStatus;
    }
    
    public void setDbInstanceStatus(String dbInstanceStatus) {
        this.dbInstanceStatus = dbInstanceStatus;
    }
    
    public int getAllocatedStorage() {
        return allocatedStorage;
    }
    
    public void setAllocatedStorage(int allocatedStorage) {
        this.allocatedStorage = allocatedStorage;
    }
    
    public String getAvailabilityZone() {
        return availabilityZone;
    }
    
    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }
    
    public double getCpuUtilization() {
        return cpuUtilization;
    }
    
    public void setCpuUtilization(double cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
    }
    
    public int getDatabaseConnections() {
        return databaseConnections;
    }
    
    public void setDatabaseConnections(int databaseConnections) {
        this.databaseConnections = databaseConnections;
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
        return "RDSInstance{" +
                "dbInstanceIdentifier='" + dbInstanceIdentifier + '\'' +
                ", dbInstanceClass='" + dbInstanceClass + '\'' +
                ", engine='" + engine + '\'' +
                ", dbInstanceStatus='" + dbInstanceStatus + '\'' +
                ", isIdle=" + isIdle +
                '}';
    }
}
