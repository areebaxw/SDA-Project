package models;

import java.time.LocalDateTime;

/**
 * Rule model class
 * Represents a governance rule for resource monitoring
 */
public class Rule {
    private int ruleId;
    private String ruleName;
    private String ruleType;
    private String resourceType;
    private String conditionMetric;
    private String conditionOperator;
    private double conditionValue;
    private int conditionDuration;
    private String actionType;
    private boolean isActive;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Rule() {}
    
    public Rule(String ruleName, String ruleType, String resourceType, String actionType) {
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.resourceType = resourceType;
        this.actionType = actionType;
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public String getRuleType() {
        return ruleType;
    }
    
    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getConditionMetric() {
        return conditionMetric;
    }
    
    public void setConditionMetric(String conditionMetric) {
        this.conditionMetric = conditionMetric;
    }
    
    public String getConditionOperator() {
        return conditionOperator;
    }
    
    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }
    
    public double getConditionValue() {
        return conditionValue;
    }
    
    public void setConditionValue(double conditionValue) {
        this.conditionValue = conditionValue;
    }
    
    public int getConditionDuration() {
        return conditionDuration;
    }
    
    public void setConditionDuration(int conditionDuration) {
        this.conditionDuration = conditionDuration;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
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
        return "Rule{" +
                "ruleId=" + ruleId +
                ", ruleName='" + ruleName + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", actionType='" + actionType + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
