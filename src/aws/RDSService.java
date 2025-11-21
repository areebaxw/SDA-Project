package aws;

import models.RDSInstance;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * RDSService - AWS RDS operations wrapper
 */
public class RDSService {
    private final RdsClient rdsClient;
    
    public RDSService() {
        this.rdsClient = AWSClientFactory.getInstance().getRDSClient();
    }
    
    /**
     * Get all RDS instances
     */
    public List<RDSInstance> getAllDBInstances() {
        List<RDSInstance> instances = new ArrayList<>();
        
        try {
            DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder().build();
            DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);
            
            for (DBInstance dbInstance : response.dbInstances()) {
                RDSInstance rdsInstance = new RDSInstance();
                rdsInstance.setDbInstanceIdentifier(dbInstance.dbInstanceIdentifier());
                rdsInstance.setDbInstanceClass(dbInstance.dbInstanceClass());
                rdsInstance.setEngine(dbInstance.engine());
                rdsInstance.setEngineVersion(dbInstance.engineVersion());
                rdsInstance.setDbInstanceStatus(dbInstance.dbInstanceStatus());
                rdsInstance.setAllocatedStorage(dbInstance.allocatedStorage());
                
                if (dbInstance.availabilityZone() != null) {
                    rdsInstance.setAvailabilityZone(dbInstance.availabilityZone());
                }
                
                instances.add(rdsInstance);
            }
            
            System.out.println("Retrieved " + instances.size() + " RDS instances from AWS");
        } catch (Exception e) {
            System.err.println("Error retrieving RDS instances: " + e.getMessage());
            e.printStackTrace();
        }
        
        return instances;
    }
    
    /**
     * Get DB instance details
     */
    public RDSInstance getDBInstanceDetails(String dbInstanceIdentifier) {
        try {
            DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder()
                    .dbInstanceIdentifier(dbInstanceIdentifier)
                    .build();
            
            DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);
            
            if (!response.dbInstances().isEmpty()) {
                DBInstance dbInstance = response.dbInstances().get(0);
                RDSInstance rdsInstance = new RDSInstance();
                rdsInstance.setDbInstanceIdentifier(dbInstance.dbInstanceIdentifier());
                rdsInstance.setDbInstanceClass(dbInstance.dbInstanceClass());
                rdsInstance.setEngine(dbInstance.engine());
                rdsInstance.setEngineVersion(dbInstance.engineVersion());
                rdsInstance.setDbInstanceStatus(dbInstance.dbInstanceStatus());
                rdsInstance.setAllocatedStorage(dbInstance.allocatedStorage());
                rdsInstance.setAvailabilityZone(dbInstance.availabilityZone());
                
                return rdsInstance;
            }
        } catch (Exception e) {
            System.err.println("Error getting RDS instance details: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Stop RDS instance
     */
    public boolean stopDBInstance(String dbInstanceIdentifier) {
        try {
            StopDbInstanceRequest request = StopDbInstanceRequest.builder()
                    .dbInstanceIdentifier(dbInstanceIdentifier)
                    .build();
            
            rdsClient.stopDBInstance(request);
            System.out.println("Stopped RDS instance: " + dbInstanceIdentifier);
            return true;
        } catch (Exception e) {
            System.err.println("Error stopping RDS instance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Start RDS instance
     */
    public boolean startDBInstance(String dbInstanceIdentifier) {
        try {
            StartDbInstanceRequest request = StartDbInstanceRequest.builder()
                    .dbInstanceIdentifier(dbInstanceIdentifier)
                    .build();
            
            rdsClient.startDBInstance(request);
            System.out.println("Started RDS instance: " + dbInstanceIdentifier);
            return true;
        } catch (Exception e) {
            System.err.println("Error starting RDS instance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
