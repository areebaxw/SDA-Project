package services;

import models.*;
import dao.*;
import aws.*;

import java.util.List;

/**
 * IdleDetectionService - Service for detecting idle resources
 * Uses Strategy Pattern for different detection algorithms
 */
public class IdleDetectionService {
    private IdleDetectionStrategy strategy;
    private final CloudWatchService cloudWatchService;
    private final EC2DAO ec2DAO;
    private final RDSDAO rdsDAO;
    private final SageMakerDAO sageMakerDAO;
    private final AlertService alertService;
    
    public IdleDetectionService() {
        this.strategy = new CPUBasedIdleStrategy(); // Default strategy
        this.cloudWatchService = new CloudWatchService();
        this.ec2DAO = new EC2DAO();
        this.rdsDAO = new RDSDAO();
        this.sageMakerDAO = new SageMakerDAO();
        this.alertService = AlertService.getInstance();
    }
    
    /**
     * Set the idle detection strategy (Strategy Pattern)
     */
    public void setStrategy(IdleDetectionStrategy strategy) {
        this.strategy = strategy;
        System.out.println("Idle detection strategy changed to: " + strategy.getClass().getSimpleName());
    }
    
    /**
     * Detect idle EC2 instances
     */
    public void detectIdleEC2Instances(int daysBack, double cpuThreshold) {
        System.out.println("Detecting idle EC2 instances...");
        
        List<EC2Instance> instances = ec2DAO.getAllEC2Instances();
        
        for (EC2Instance instance : instances) {
            if ("running".equalsIgnoreCase(instance.getInstanceState())) {
                // Get metrics from CloudWatch
                double cpuUtilization = cloudWatchService.getEC2CPUUtilization(
                    instance.getInstanceId(), daysBack
                );
                double networkIn = cloudWatchService.getEC2NetworkIn(
                    instance.getInstanceId(), daysBack
                );
                
                // Update instance with metrics
                instance.setCpuUtilization(cpuUtilization);
                instance.setNetworkIn(networkIn);
                
                // Check if idle using strategy
                boolean isIdle = strategy.isIdle(cpuUtilization, networkIn, cpuThreshold);
                instance.setIdle(isIdle);
                
                // Update database
                ec2DAO.saveOrUpdateEC2Instance(instance);
                
                // Create alert if idle
                if (isIdle) {
                    String message = String.format(
                        "EC2 instance %s is idle (CPU: %.2f%%, Network In: %.2f bytes)",
                        instance.getInstanceId(), cpuUtilization, networkIn
                    );
                    
                    Alert alert = new Alert(
                        instance.getInstanceId(),
                        "EC2",
                        "IDLE_RESOURCE",
                        "medium",
                        message
                    );
                    
                    alertService.createAlert(alert);
                }
            }
        }
        
        System.out.println("Idle EC2 detection completed.");
    }
    
    /**
     * Detect idle RDS instances
     */
    public void detectIdleRDSInstances(int daysBack, int connectionThreshold) {
        System.out.println("Detecting idle RDS instances...");
        
        List<RDSInstance> instances = rdsDAO.getAllRDSInstances();
        
        for (RDSInstance instance : instances) {
            if ("available".equalsIgnoreCase(instance.getDbInstanceStatus())) {
                // Get metrics from CloudWatch
                double cpuUtilization = cloudWatchService.getRDSCPUUtilization(
                    instance.getDbInstanceIdentifier(), daysBack
                );
                int connections = cloudWatchService.getRDSDatabaseConnections(
                    instance.getDbInstanceIdentifier(), daysBack
                );
                
                // Update instance with metrics
                instance.setCpuUtilization(cpuUtilization);
                instance.setDatabaseConnections(connections);
                
                // Check if idle
                boolean isIdle = connections < connectionThreshold && cpuUtilization < 10.0;
                instance.setIdle(isIdle);
                
                // Update database
                rdsDAO.saveOrUpdateRDSInstance(instance);
                
                // Create alert if idle
                if (isIdle) {
                    String message = String.format(
                        "RDS instance %s is idle (Connections: %d, CPU: %.2f%%)",
                        instance.getDbInstanceIdentifier(), connections, cpuUtilization
                    );
                    
                    Alert alert = new Alert(
                        instance.getDbInstanceIdentifier(),
                        "RDS",
                        "IDLE_RESOURCE",
                        "high",
                        message
                    );
                    
                    alertService.createAlert(alert);
                }
            }
        }
        
        System.out.println("Idle RDS detection completed.");
    }
    
    /**
     * Detect idle SageMaker endpoints
     */
    public void detectIdleSageMakerEndpoints(int daysBack, int invocationThreshold) {
        System.out.println("Detecting idle SageMaker endpoints...");
        
        List<SageMakerEndpoint> endpoints = sageMakerDAO.getAllEndpoints();
        
        for (SageMakerEndpoint endpoint : endpoints) {
            if ("InService".equalsIgnoreCase(endpoint.getEndpointStatus())) {
                // Get metrics from CloudWatch
                int invocations = cloudWatchService.getSageMakerInvocations(
                    endpoint.getEndpointName(), daysBack
                );
                
                // Update endpoint with metrics
                endpoint.setInvocations(invocations);
                
                // Check if idle
                boolean isIdle = invocations < invocationThreshold;
                endpoint.setIdle(isIdle);
                
                // Update database
                sageMakerDAO.saveOrUpdateEndpoint(endpoint);
                
                // Create alert if idle
                if (isIdle) {
                    String message = String.format(
                        "SageMaker endpoint %s is idle (Invocations: %d in last %d days)",
                        endpoint.getEndpointName(), invocations, daysBack
                    );
                    
                    Alert alert = new Alert(
                        endpoint.getEndpointName(),
                        "SageMaker",
                        "IDLE_RESOURCE",
                        "high",
                        message
                    );
                    
                    alertService.createAlert(alert);
                }
            }
        }
        
        System.out.println("Idle SageMaker detection completed.");
    }
    
    /**
     * Run complete idle detection for all resources
     */
    public void runCompleteIdleDetection() {
        System.out.println("Running complete idle detection across all resources...");
        
        detectIdleEC2Instances(7, 5.0);
        detectIdleRDSInstances(7, 2);
        detectIdleSageMakerEndpoints(7, 10);
        
        System.out.println("Complete idle detection finished.");
    }
}
