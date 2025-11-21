package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * BillingRecord model class
 * Represents a billing record for AWS services
 */
public class BillingRecord {
    private int recordId;
    private int userId;
    private String serviceName;
    private double costAmount;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String recordType;
    private LocalDateTime createdAt;
    
    // Constructors
    public BillingRecord() {}
    
    public BillingRecord(int userId, String serviceName, double costAmount, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.serviceName = serviceName;
        this.costAmount = costAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currency = "USD";
        this.recordType = "monthly";
    }
    
    // Getters and Setters
    public int getRecordId() {
        return recordId;
    }
    
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public double getCostAmount() {
        return costAmount;
    }
    
    public void setCostAmount(double costAmount) {
        this.costAmount = costAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public String getRecordType() {
        return recordType;
    }
    
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "BillingRecord{" +
                "recordId=" + recordId +
                ", serviceName='" + serviceName + '\'' +
                ", costAmount=" + costAmount +
                ", currency='" + currency + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
