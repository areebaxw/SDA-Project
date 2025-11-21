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
     * Get all SageMaker endpoints
     */
    public List<SageMakerEndpoint> getAllEndpoints() {
        List<SageMakerEndpoint> endpoints = new ArrayList<>();
        
        try {
            ListEndpointsRequest request = ListEndpointsRequest.builder().build();
            ListEndpointsResponse response = sageMakerClient.listEndpoints(request);
            
            for (EndpointSummary summary : response.endpoints()) {
                SageMakerEndpoint endpoint = new SageMakerEndpoint();
                endpoint.setEndpointName(summary.endpointName());
                endpoint.setEndpointArn(summary.endpointArn());
                endpoint.setEndpointStatus(summary.endpointStatus().toString());
                
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
                
                endpoints.add(endpoint);
            }
            
            System.out.println("Retrieved " + endpoints.size() + " SageMaker endpoints from AWS");
        } catch (Exception e) {
            System.err.println("Error retrieving SageMaker endpoints: " + e.getMessage());
            e.printStackTrace();
        }
        
        return endpoints;
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
}
