package aws;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * CloudWatchService - AWS CloudWatch operations wrapper
 * Used for fetching metrics for idle resource detection
 */
public class CloudWatchService {
    private final CloudWatchClient cloudWatchClient;
    
    public CloudWatchService() {
        this.cloudWatchClient = AWSClientFactory.getInstance().getCloudWatchClient();
    }
    
    /**
     * Get EC2 CPU utilization metric
     */
    public double getEC2CPUUtilization(String instanceId, int daysBack) {
        try {
            Dimension dimension = Dimension.builder()
                    .name("InstanceId")
                    .value(instanceId)
                    .build();
            
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(daysBack, ChronoUnit.DAYS);
            
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/EC2")
                    .metricName("CPUUtilization")
                    .dimensions(dimension)
                    .startTime(startTime)
                    .endTime(endTime)
                    .period(86400) // 1 day in seconds
                    .statistics(Statistic.AVERAGE)
                    .build();
            
            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
            
            if (!response.datapoints().isEmpty()) {
                double sum = 0;
                for (Datapoint datapoint : response.datapoints()) {
                    sum += datapoint.average();
                }
                return sum / response.datapoints().size();
            }
        } catch (Exception e) {
            System.err.println("Error getting EC2 CPU metrics: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Get EC2 Network In metric
     */
    public double getEC2NetworkIn(String instanceId, int daysBack) {
        try {
            Dimension dimension = Dimension.builder()
                    .name("InstanceId")
                    .value(instanceId)
                    .build();
            
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(daysBack, ChronoUnit.DAYS);
            
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/EC2")
                    .metricName("NetworkIn")
                    .dimensions(dimension)
                    .startTime(startTime)
                    .endTime(endTime)
                    .period(86400)
                    .statistics(Statistic.AVERAGE)
                    .build();
            
            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
            
            if (!response.datapoints().isEmpty()) {
                double sum = 0;
                for (Datapoint datapoint : response.datapoints()) {
                    sum += datapoint.average();
                }
                return sum / response.datapoints().size();
            }
        } catch (Exception e) {
            System.err.println("Error getting EC2 Network In metrics: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Get RDS CPU utilization
     */
    public double getRDSCPUUtilization(String dbInstanceIdentifier, int daysBack) {
        try {
            Dimension dimension = Dimension.builder()
                    .name("DBInstanceIdentifier")
                    .value(dbInstanceIdentifier)
                    .build();
            
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(daysBack, ChronoUnit.DAYS);
            
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/RDS")
                    .metricName("CPUUtilization")
                    .dimensions(dimension)
                    .startTime(startTime)
                    .endTime(endTime)
                    .period(86400)
                    .statistics(Statistic.AVERAGE)
                    .build();
            
            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
            
            if (!response.datapoints().isEmpty()) {
                double sum = 0;
                for (Datapoint datapoint : response.datapoints()) {
                    sum += datapoint.average();
                }
                return sum / response.datapoints().size();
            }
        } catch (Exception e) {
            System.err.println("Error getting RDS CPU metrics: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Get RDS Database Connections
     */
    public int getRDSDatabaseConnections(String dbInstanceIdentifier, int daysBack) {
        try {
            Dimension dimension = Dimension.builder()
                    .name("DBInstanceIdentifier")
                    .value(dbInstanceIdentifier)
                    .build();
            
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(daysBack, ChronoUnit.DAYS);
            
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/RDS")
                    .metricName("DatabaseConnections")
                    .dimensions(dimension)
                    .startTime(startTime)
                    .endTime(endTime)
                    .period(86400)
                    .statistics(Statistic.AVERAGE)
                    .build();
            
            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
            
            if (!response.datapoints().isEmpty()) {
                double sum = 0;
                for (Datapoint datapoint : response.datapoints()) {
                    sum += datapoint.average();
                }
                return (int) (sum / response.datapoints().size());
            }
        } catch (Exception e) {
            System.err.println("Error getting RDS connection metrics: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get SageMaker model invocations
     */
    public int getSageMakerInvocations(String endpointName, int daysBack) {
        try {
            Dimension dimension = Dimension.builder()
                    .name("EndpointName")
                    .value(endpointName)
                    .build();
            
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(daysBack, ChronoUnit.DAYS);
            
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/SageMaker")
                    .metricName("Invocations")
                    .dimensions(dimension)
                    .startTime(startTime)
                    .endTime(endTime)
                    .period(86400)
                    .statistics(Statistic.SUM)
                    .build();
            
            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
            
            if (!response.datapoints().isEmpty()) {
                double sum = 0;
                for (Datapoint datapoint : response.datapoints()) {
                    sum += datapoint.sum();
                }
                return (int) sum;
            }
        } catch (Exception e) {
            System.err.println("Error getting SageMaker invocation metrics: " + e.getMessage());
        }
        return 0;
    }
}
