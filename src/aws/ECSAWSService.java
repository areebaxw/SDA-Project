package aws;

import models.ECSService;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ECSService - AWS ECS operations wrapper
 */
public class ECSAWSService {
    private final EcsClient ecsClient;
    
    public ECSAWSService() {
        this.ecsClient = AWSClientFactory.getInstance().getECSClient();
    }
    
    /**
     * Get all ECS clusters
     */
    public List<String> getAllClusters() {
        List<String> clusterArns = new ArrayList<>();
        
        try {
            ListClustersRequest request = ListClustersRequest.builder().build();
            ListClustersResponse response = ecsClient.listClusters(request);
            
            clusterArns.addAll(response.clusterArns());
            System.out.println("Retrieved " + clusterArns.size() + " ECS clusters");
        } catch (Exception e) {
            System.err.println("Error retrieving ECS clusters: " + e.getMessage());
            e.printStackTrace();
        }
        
        return clusterArns;
    }
    
    /**
     * Get all services in a cluster
     */
    public List<String> getServicesInCluster(String clusterArn) {
        List<String> serviceArns = new ArrayList<>();
        
        try {
            ListServicesRequest request = ListServicesRequest.builder()
                    .cluster(clusterArn)
                    .build();
            
            ListServicesResponse response = ecsClient.listServices(request);
            serviceArns.addAll(response.serviceArns());
            
            System.out.println("Retrieved " + serviceArns.size() + " services in cluster");
        } catch (Exception e) {
            System.err.println("Error retrieving ECS services: " + e.getMessage());
            e.printStackTrace();
        }
        
        return serviceArns;
    }
    
    /**
     * Get all ECS services across all clusters
     */
    public List<ECSService> getAllECSServices() {
        List<ECSService> services = new ArrayList<>();
        
        try {
            List<String> clusterArns = getAllClusters();
            
            for (String clusterArn : clusterArns) {
                String clusterName = extractNameFromArn(clusterArn);
                List<String> serviceArns = getServicesInCluster(clusterArn);
                
                if (!serviceArns.isEmpty()) {
                    DescribeServicesRequest request = DescribeServicesRequest.builder()
                            .cluster(clusterArn)
                            .services(serviceArns)
                            .build();
                    
                    DescribeServicesResponse response = ecsClient.describeServices(request);
                    
                    for (Service service : response.services()) {
                        ECSService ecsService = new ECSService();
                        ecsService.setClusterName(clusterName);
                        ecsService.setServiceName(service.serviceName());
                        ecsService.setServiceArn(service.serviceArn());
                        ecsService.setStatus(service.status());
                        ecsService.setDesiredCount(service.desiredCount());
                        ecsService.setRunningCount(service.runningCount());
                        ecsService.setPendingCount(service.pendingCount());
                        ecsService.setTaskDefinition(service.taskDefinition());
                        
                        services.add(ecsService);
                    }
                }
            }
            
            System.out.println("Retrieved " + services.size() + " ECS services from AWS");
        } catch (Exception e) {
            System.err.println("Error retrieving ECS services: " + e.getMessage());
            e.printStackTrace();
        }
        
        return services;
    }
    
    /**
     * Update service desired count
     */
    public boolean updateServiceDesiredCount(String clusterName, String serviceName, int desiredCount) {
        try {
            UpdateServiceRequest request = UpdateServiceRequest.builder()
                    .cluster(clusterName)
                    .service(serviceName)
                    .desiredCount(desiredCount)
                    .build();
            
            ecsClient.updateService(request);
            System.out.println("Updated ECS service desired count: " + serviceName);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating ECS service: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Extract name from ARN
     */
    private String extractNameFromArn(String arn) {
        if (arn != null && arn.contains("/")) {
            return arn.substring(arn.lastIndexOf("/") + 1);
        }
        return arn;
    }
}
