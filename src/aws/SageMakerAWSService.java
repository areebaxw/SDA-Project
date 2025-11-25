package aws;

import models.SageMakerEndpoint;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * SageMakerService - AWS SageMaker operations wrapper
 */
public class SageMakerAWSService {
    private final SageMakerClient sageMakerClient;
    
    public SageMakerAWSService() {
        this.sageMakerClient = AWSClientFactory.getInstance().getSageMakerClient();
    }
    
    /**
     * Get all SageMaker endpoints and notebook instances
     */
    public List<SageMakerEndpoint> getAllEndpoints() {
        List<SageMakerEndpoint> resources = new ArrayList<>();
        
        try {
            System.out.println("Fetching SageMaker endpoints from AWS...");
            
            // Fetch endpoints
            ListEndpointsRequest endpointRequest = ListEndpointsRequest.builder().build();
            ListEndpointsResponse endpointResponse = sageMakerClient.listEndpoints(endpointRequest);
            
            System.out.println("AWS returned " + endpointResponse.endpoints().size() + " endpoints");
            
            for (EndpointSummary summary : endpointResponse.endpoints()) {
                System.out.println("Processing endpoint: " + summary.endpointName());
                
                SageMakerEndpoint endpoint = new SageMakerEndpoint();
                endpoint.setEndpointName(summary.endpointName());
                endpoint.setEndpointArn(summary.endpointArn());
                endpoint.setEndpointStatus(summary.endpointStatus().toString());
                endpoint.setResourceType("endpoint");
                
                if (summary.creationTime() != null) {
                    endpoint.setCreationTime(
                        summary.creationTime().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    );
                }
                
                // Get detailed endpoint information
                DescribeEndpointRequest detailRequest = DescribeEndpointRequest.builder()
                        .endpointName(summary.endpointName())
                        .build();
                
                DescribeEndpointResponse detailResponse = sageMakerClient.describeEndpoint(detailRequest);
                
                if (!detailResponse.productionVariants().isEmpty()) {
                    ProductionVariantSummary variant = detailResponse.productionVariants().get(0);
                    endpoint.setInstanceType(variant.currentInstanceCount().toString());
                    endpoint.setInstanceCount(variant.currentInstanceCount());
                }
                
                resources.add(endpoint);
            }
            
            // Fetch notebook instances
            System.out.println("Fetching SageMaker notebook instances from AWS...");
            ListNotebookInstancesRequest notebookRequest = ListNotebookInstancesRequest.builder().build();
            ListNotebookInstancesResponse notebookResponse = sageMakerClient.listNotebookInstances(notebookRequest);
            
            System.out.println("AWS returned " + notebookResponse.notebookInstances().size() + " notebook instances");
            
            for (NotebookInstanceSummary summary : notebookResponse.notebookInstances()) {
                System.out.println("Processing notebook: " + summary.notebookInstanceName());
                
                SageMakerEndpoint notebook = new SageMakerEndpoint();
                notebook.setEndpointName(summary.notebookInstanceName());
                notebook.setEndpointArn(summary.notebookInstanceArn());
                notebook.setEndpointStatus(summary.notebookInstanceStatus().toString());
                notebook.setResourceType("notebook");
                notebook.setInstanceType(summary.instanceType().toString());
                
                if (summary.creationTime() != null) {
                    notebook.setCreationTime(
                        summary.creationTime().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    );
                }
                
                resources.add(notebook);
            }
            
            System.out.println("Retrieved " + resources.size() + " SageMaker resources from AWS (" + 
                             endpointResponse.endpoints().size() + " endpoints, " + 
                             notebookResponse.notebookInstances().size() + " notebooks)");
        } catch (Exception e) {
            System.err.println("Error retrieving SageMaker resources: " + e.getMessage());
            e.printStackTrace();
        }
        
        return resources;
    }
    
    /**
     * Get endpoint details
     */
    public SageMakerEndpoint getEndpointDetails(String endpointName) {
        try {
            DescribeEndpointRequest request = DescribeEndpointRequest.builder()
                    .endpointName(endpointName)
                    .build();
            
            DescribeEndpointResponse response = sageMakerClient.describeEndpoint(request);
            
            SageMakerEndpoint endpoint = new SageMakerEndpoint();
            endpoint.setEndpointName(response.endpointName());
            endpoint.setEndpointArn(response.endpointArn());
            endpoint.setEndpointStatus(response.endpointStatus().toString());
            
            if (response.creationTime() != null) {
                endpoint.setCreationTime(
                    response.creationTime().atZone(ZoneId.systemDefault()).toLocalDateTime()
                );
            }
            
            if (!response.productionVariants().isEmpty()) {
                ProductionVariantSummary variant = response.productionVariants().get(0);
                endpoint.setInstanceType(variant.currentInstanceCount().toString());
                endpoint.setInstanceCount(variant.currentInstanceCount());
            }
            
            return endpoint;
        } catch (Exception e) {
            System.err.println("Error getting SageMaker endpoint details: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Delete endpoint
     */
    public boolean deleteEndpoint(String endpointName) {
        try {
            DeleteEndpointRequest request = DeleteEndpointRequest.builder()
                    .endpointName(endpointName)
                    .build();
            
            sageMakerClient.deleteEndpoint(request);
            System.out.println("Deleted SageMaker endpoint: " + endpointName);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting SageMaker endpoint: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Stop notebook instance
     */
    public boolean stopNotebookInstance(String notebookInstanceName) {
        try {
            StopNotebookInstanceRequest request = StopNotebookInstanceRequest.builder()
                    .notebookInstanceName(notebookInstanceName)
                    .build();
            
            sageMakerClient.stopNotebookInstance(request);
            System.out.println("Stopped SageMaker notebook instance: " + notebookInstanceName);
            return true;
        } catch (Exception e) {
            System.err.println("Error stopping SageMaker notebook: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Start notebook instance
     */
    public boolean startNotebookInstance(String notebookInstanceName) {
        try {
            StartNotebookInstanceRequest request = StartNotebookInstanceRequest.builder()
                    .notebookInstanceName(notebookInstanceName)
                    .build();
            
            sageMakerClient.startNotebookInstance(request);
            System.out.println("Started SageMaker notebook instance: " + notebookInstanceName);
            return true;
        } catch (Exception e) {
            System.err.println("Error starting SageMaker notebook: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete notebook instance
     */
    public boolean deleteNotebookInstance(String notebookInstanceName) {
        try {
            DeleteNotebookInstanceRequest request = DeleteNotebookInstanceRequest.builder()
                    .notebookInstanceName(notebookInstanceName)
                    .build();
            
            sageMakerClient.deleteNotebookInstance(request);
            System.out.println("Deleted SageMaker notebook instance: " + notebookInstanceName);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting SageMaker notebook: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Sync SageMaker endpoints from AWS to database
     * Business logic method that orchestrates: fetch from AWS, set user, save to DB, count results
     */
    public int syncFromAWS(int userId) {
        System.out.println("Syncing SageMaker endpoints from AWS...");
        
        dao.SageMakerDAO sageMakerDAO = new dao.SageMakerDAO();
        List<SageMakerEndpoint> endpoints = getAllEndpoints();
        
        int savedCount = 0;
        for (SageMakerEndpoint endpoint : endpoints) {
            endpoint.setUserId(userId);
            if (sageMakerDAO.saveOrUpdateEndpoint(endpoint)) {
                savedCount++;
            }
        }
        
        System.out.println("Synced " + savedCount + " SageMaker endpoints from AWS");
        return savedCount;
    }
}
