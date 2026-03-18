package models;

import java.time.LocalDateTime;

/**
 * Alert model class
 * Represents an alert for resource monitoring
 */
public class Alert {
    private int alertId;
    private String resourceId;
    private String resourceType;
    private String alertType;
    private String severity;
    private String message;
    private int ruleId;
    private boolean isResolved;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    
    // Constructors
    public Alert() {}
    
    public Alert(String resourceId, String resourceType, String alertType, String severity, String message) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.isResolved = false;
    }
    
    // Getters and Setters
    public int getAlertId() {
        return alertId;
    }
    
    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getAlertType() {
        return alertType;
    }
    
    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }
    
    public boolean isResolved() {
        return isResolved;
    }
    
    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    @Override
    public String toString() {
        return "Alert{" +
                "alertId=" + alertId +
                ", resourceId='" + resourceId + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", alertType='" + alertType + '\'' +
                ", severity='" + severity + '\'' +
                ", message='" + message + '\'' +
                ", isResolved=" + isResolved +
                '}';
    }
}
