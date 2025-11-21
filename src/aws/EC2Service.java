package aws;

import models.EC2Instance;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * EC2Service - AWS EC2 operations wrapper
 */
public class EC2Service {
    private final Ec2Client ec2Client;
    
    public EC2Service() {
        this.ec2Client = AWSClientFactory.getInstance().getEC2Client();
    }
    
    /**
     * Get all EC2 instances
     */
    public List<EC2Instance> getAllInstances() {
        List<EC2Instance> instances = new ArrayList<>();
        
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            DescribeInstancesResponse response = ec2Client.describeInstances(request);
            
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    EC2Instance ec2Instance = new EC2Instance();
                    ec2Instance.setInstanceId(instance.instanceId());
                    ec2Instance.setInstanceType(instance.instanceType().toString());
                    ec2Instance.setInstanceState(instance.state().name().toString());
                    ec2Instance.setAvailabilityZone(instance.placement().availabilityZone());
                    
                    if (instance.launchTime() != null) {
                        ec2Instance.setLaunchTime(
                            instance.launchTime().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        );
                    }
                    
                    instances.add(ec2Instance);
                }
            }
            
            System.out.println("Retrieved " + instances.size() + " EC2 instances from AWS");
        } catch (Exception e) {
            System.err.println("Error retrieving EC2 instances: " + e.getMessage());
            e.printStackTrace();
        }
        
        return instances;
    }
    
    /**
     * Start EC2 instance
     */
    public boolean startInstance(String instanceId) {
        try {
            StartInstancesRequest request = StartInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();
            
            ec2Client.startInstances(request);
            System.out.println("Started EC2 instance: " + instanceId);
            return true;
        } catch (Exception e) {
            System.err.println("Error starting instance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Stop EC2 instance
     */
    public boolean stopInstance(String instanceId) {
        try {
            StopInstancesRequest request = StopInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();
            
            ec2Client.stopInstances(request);
            System.out.println("Stopped EC2 instance: " + instanceId);
            return true;
        } catch (Exception e) {
            System.err.println("Error stopping instance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Terminate EC2 instance
     */
    public boolean terminateInstance(String instanceId) {
        try {
            TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();
            
            ec2Client.terminateInstances(request);
            System.out.println("Terminated EC2 instance: " + instanceId);
            return true;
        } catch (Exception e) {
            System.err.println("Error terminating instance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get instance state
     */
    public String getInstanceState(String instanceId) {
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();
            
            DescribeInstancesResponse response = ec2Client.describeInstances(request);
            
            if (!response.reservations().isEmpty() && 
                !response.reservations().get(0).instances().isEmpty()) {
                return response.reservations().get(0).instances().get(0).state().name().toString();
            }
        } catch (Exception e) {
            System.err.println("Error getting instance state: " + e.getMessage());
        }
        return "unknown";
    }
}
