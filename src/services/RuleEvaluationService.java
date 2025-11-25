package services;

import models.Rule;
import models.Alert;
import models.EC2Instance;
import models.RDSInstance;
import models.ECSService;
import models.SageMakerEndpoint;
import dao.*;
import aws.CloudWatchService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RuleEvaluationService - Evaluates rules and generates alerts
 */
public class RuleEvaluationService {
    private final RuleDAO ruleDAO;
    private final AlertService alertService;
    private final EC2DAO ec2DAO;
    private final RDSDAO rdsDAO;
    private final ECSDAO ecsDAO;
    private final SageMakerDAO sageMakerDAO;
    private final CloudWatchService cloudWatchService;
    
    public RuleEvaluationService() {
        this.ruleDAO = new RuleDAO();
        this.alertService = AlertService.getInstance();
        this.ec2DAO = new EC2DAO();
        this.rdsDAO = new RDSDAO();
        this.ecsDAO = new ECSDAO();
        this.sageMakerDAO = new SageMakerDAO();
        this.cloudWatchService = new CloudWatchService();
    }
    
    /**
     * Evaluate all active rules and generate alerts
     */
    public void evaluateAllRules() {
        System.out.println("Starting rule evaluation...");
        List<Rule> activeRules = ruleDAO.getAllActiveRules();
        System.out.println("Found " + activeRules.size() + " active rules to evaluate");
        
        for (Rule rule : activeRules) {
            try {
                evaluateRule(rule);
            } catch (Exception e) {
                System.err.println("Error evaluating rule " + rule.getRuleName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Rule evaluation completed");
    }
    
    /**
     * Evaluate a single rule
     */
    private void evaluateRule(Rule rule) {
        System.out.println("Evaluating rule: " + rule.getRuleName());
        
        String resourceType = rule.getResourceType();
        
        switch (resourceType) {
            case "EC2":
                evaluateEC2Rules(rule);
                break;
            case "RDS":
                evaluateRDSRules(rule);
                break;
            case "ECS":
                evaluateECSRules(rule);
                break;
            case "SageMaker":
                evaluateSageMakerRules(rule);
                break;
            default:
                System.out.println("Unknown resource type: " + resourceType);
        }
    }
    
    /**
     * Evaluate rules for EC2 instances
     */
    private void evaluateEC2Rules(Rule rule) {
        List<EC2Instance> instances = ec2DAO.getAllEC2Instances();
        System.out.println("Evaluating " + instances.size() + " EC2 instances for rule: " + rule.getRuleName());
        
        for (EC2Instance instance : instances) {
            try {
                boolean conditionMet = false;
                String metric = rule.getConditionMetric();
                double threshold = rule.getConditionValue();
                int duration = rule.getConditionDuration();
                String unit = rule.getDurationUnit();
                
                // Convert duration to hours for CloudWatch API
                int durationInHours = convertToHours(duration, unit);
                
                if ("CPU".equalsIgnoreCase(metric)) {
                    double cpuUtilization = cloudWatchService.getEC2CPUUtilization(
                        instance.getInstanceId(), 
                        durationInHours
                    );
                    
                    conditionMet = evaluateCondition(cpuUtilization, rule.getConditionOperator(), threshold);
                    
                    if (conditionMet) {
                        createAlert(
                            rule,
                            instance.getInstanceId(),
                            "EC2",
                            String.format("EC2 instance %s has CPU utilization %.2f%% %s %.2f%% for %d %s",
                                instance.getInstanceId(), cpuUtilization, rule.getConditionOperator(), threshold, duration, unit)
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Error evaluating EC2 instance " + instance.getInstanceId() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Evaluate rules for RDS instances
     */
    private void evaluateRDSRules(Rule rule) {
        List<RDSInstance> instances = rdsDAO.getAllRDSInstances();
        System.out.println("Evaluating " + instances.size() + " RDS instances for rule: " + rule.getRuleName());
        
        for (RDSInstance instance : instances) {
            try {
                boolean conditionMet = false;
                String metric = rule.getConditionMetric();
                double threshold = rule.getConditionValue();
                int duration = rule.getConditionDuration();
                String unit = rule.getDurationUnit();
                
                // Convert duration to hours for CloudWatch API
                int durationInHours = convertToHours(duration, unit);
                
                if ("CPU".equalsIgnoreCase(metric)) {
                    double cpuUtilization = cloudWatchService.getRDSCPUUtilization(
                        instance.getDbInstanceIdentifier(), 
                        durationInHours
                    );
                    
                    conditionMet = evaluateCondition(cpuUtilization, rule.getConditionOperator(), threshold);
                    
                    if (conditionMet) {
                        createAlert(
                            rule,
                            instance.getDbInstanceIdentifier(),
                            "RDS",
                            String.format("RDS instance %s has CPU utilization %.2f%% %s %.2f%% for %d %s",
                                instance.getDbInstanceIdentifier(), cpuUtilization, rule.getConditionOperator(), threshold, duration, unit)
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Error evaluating RDS instance " + instance.getDbInstanceIdentifier() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Evaluate rules for ECS services
     */
    private void evaluateECSRules(Rule rule) {
        List<ECSService> services = ecsDAO.getAllECSServices();
        System.out.println("Evaluating " + services.size() + " ECS services for rule: " + rule.getRuleName());
        
        // ECS evaluation logic can be added here
        // For now, just log that we checked them
        System.out.println("ECS rule evaluation not yet implemented");
    }
    
    /**
     * Evaluate rules for SageMaker endpoints
     */
    private void evaluateSageMakerRules(Rule rule) {
        List<SageMakerEndpoint> endpoints = sageMakerDAO.getAllEndpoints();
        System.out.println("Evaluating " + endpoints.size() + " SageMaker endpoints for rule: " + rule.getRuleName());
        
        for (SageMakerEndpoint endpoint : endpoints) {
            try {
                boolean conditionMet = false;
                String metric = rule.getConditionMetric();
                double threshold = rule.getConditionValue();
                int duration = rule.getConditionDuration();
                String unit = rule.getDurationUnit();
                
                // Convert duration to hours for CloudWatch API
                int durationInHours = convertToHours(duration, unit);
                
                if ("Invocations".equalsIgnoreCase(metric)) {
                    int invocations = cloudWatchService.getSageMakerInvocations(
                        endpoint.getEndpointName(), 
                        durationInHours
                    );
                    
                    conditionMet = evaluateCondition(invocations, rule.getConditionOperator(), threshold);
                    
                    if (conditionMet) {
                        createAlert(
                            rule,
                            endpoint.getEndpointName(),
                            "SageMaker",
                            String.format("SageMaker endpoint %s has %d invocations %s %.0f for %d %s",
                                endpoint.getEndpointName(), invocations, rule.getConditionOperator(), threshold, duration, unit)
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Error evaluating SageMaker endpoint " + endpoint.getEndpointName() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Convert duration to hours based on unit
     */
    private int convertToHours(int duration, String unit) {
        if (unit == null || "hours".equalsIgnoreCase(unit)) {
            return duration;
        } else if ("days".equalsIgnoreCase(unit)) {
            return duration * 24;
        } else if ("minutes".equalsIgnoreCase(unit)) {
            // Convert minutes to hours, minimum 1 hour
            int hours = duration / 60;
            return hours > 0 ? hours : 1;
        }
        return duration; // default to hours
    }
    
    /**
     * Evaluate a condition based on operator
     */
    private boolean evaluateCondition(double actualValue, String operator, double threshold) {
        switch (operator) {
            case "<":
                return actualValue < threshold;
            case ">":
                return actualValue > threshold;
            case "=":
                return Math.abs(actualValue - threshold) < 0.01; // Allow small difference for double comparison
            case "<=":
                return actualValue <= threshold;
            case ">=":
                return actualValue >= threshold;
            default:
                System.err.println("Unknown operator: " + operator);
                return false;
        }
    }
    
    /**
     * Create an alert based on rule evaluation
     */
    private void createAlert(Rule rule, String resourceId, String resourceType, String message) {
        Alert alert = new Alert();
        alert.setResourceId(resourceId);
        alert.setResourceType(resourceType);
        alert.setAlertType(rule.getRuleType());
        alert.setSeverity(determineSeverity(rule));
        alert.setMessage(message);
        alert.setRuleId(rule.getRuleId());
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());
        
        boolean created = alertService.createAlert(alert);
        if (created) {
            System.out.println("✓ Alert created: " + message);
        } else {
            System.err.println("✗ Failed to create alert for resource: " + resourceId);
        }
    }
    
    /**
     * Determine alert severity based on rule type
     */
    private String determineSeverity(Rule rule) {
        switch (rule.getRuleType()) {
            case "security":
                return "HIGH";
            case "cost_optimization":
                return "MEDIUM";
            case "performance":
                return "MEDIUM";
            case "resource_optimization":
                return "LOW";
            default:
                return "LOW";
        }
    }
}
