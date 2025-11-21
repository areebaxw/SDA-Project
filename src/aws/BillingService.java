package aws;

import models.BillingRecord;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * BillingService - AWS Cost Explorer operations wrapper
 */
public class BillingService {
    private final CostExplorerClient costExplorerClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public BillingService() {
        this.costExplorerClient = AWSClientFactory.getInstance().getCostExplorerClient();
    }
    
    /**
     * Get cost and usage for a date range
     */
    public List<BillingRecord> getCostAndUsage(LocalDate startDate, LocalDate endDate, int userId) {
        List<BillingRecord> records = new ArrayList<>();
        
        try {
            DateInterval dateInterval = DateInterval.builder()
                    .start(startDate.format(formatter))
                    .end(endDate.format(formatter))
                    .build();
            
            GroupDefinition groupDefinition = GroupDefinition.builder()
                    .type(GroupDefinitionType.DIMENSION)
                    .key("SERVICE")
                    .build();
            
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(dateInterval)
                    .granularity(Granularity.MONTHLY)
                    .metrics("UnblendedCost")
                    .groupBy(groupDefinition)
                    .build();
            
            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);
            
            for (ResultByTime resultByTime : response.resultsByTime()) {
                LocalDate periodStart = LocalDate.parse(resultByTime.timePeriod().start(), formatter);
                LocalDate periodEnd = LocalDate.parse(resultByTime.timePeriod().end(), formatter);
                
                for (Group group : resultByTime.groups()) {
                    String serviceName = group.keys().get(0);
                    double cost = Double.parseDouble(
                        group.metrics().get("UnblendedCost").amount()
                    );
                    
                    BillingRecord record = new BillingRecord();
                    record.setUserId(userId);
                    record.setServiceName(serviceName);
                    record.setCostAmount(cost);
                    record.setStartDate(periodStart);
                    record.setEndDate(periodEnd);
                    record.setCurrency("USD");
                    record.setRecordType("monthly");
                    
                    records.add(record);
                }
            }
            
            System.out.println("Retrieved " + records.size() + " billing records from AWS");
        } catch (Exception e) {
            System.err.println("Error retrieving billing data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return records;
    }
    
    /**
     * Get total cost for a date range
     */
    public double getTotalCost(LocalDate startDate, LocalDate endDate) {
        try {
            DateInterval dateInterval = DateInterval.builder()
                    .start(startDate.format(formatter))
                    .end(endDate.format(formatter))
                    .build();
            
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(dateInterval)
                    .granularity(Granularity.MONTHLY)
                    .metrics("UnblendedCost")
                    .build();
            
            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);
            
            double totalCost = 0.0;
            for (ResultByTime resultByTime : response.resultsByTime()) {
                totalCost += Double.parseDouble(
                    resultByTime.total().get("UnblendedCost").amount()
                );
            }
            
            return totalCost;
        } catch (Exception e) {
            System.err.println("Error getting total cost: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
    
    /**
     * Get cost by service for a specific month
     */
    public Map<String, Double> getCostByService(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> costMap = new HashMap<>();
        
        try {
            List<BillingRecord> records = getCostAndUsage(startDate, endDate, 0);
            
            for (BillingRecord record : records) {
                String service = record.getServiceName();
                double cost = record.getCostAmount();
                
                costMap.put(service, costMap.getOrDefault(service, 0.0) + cost);
            }
        } catch (Exception e) {
            System.err.println("Error getting cost by service: " + e.getMessage());
        }
        
        return costMap;
    }
    
    /**
     * Get monthly cost trend for last N months
     */
    public Map<String, Double> getMonthlyCostTrend(int months) {
        Map<String, Double> trendMap = new HashMap<>();
        
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(months);
            
            DateInterval dateInterval = DateInterval.builder()
                    .start(startDate.format(formatter))
                    .end(endDate.format(formatter))
                    .build();
            
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(dateInterval)
                    .granularity(Granularity.MONTHLY)
                    .metrics("UnblendedCost")
                    .build();
            
            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);
            
            for (ResultByTime resultByTime : response.resultsByTime()) {
                String period = resultByTime.timePeriod().start();
                double cost = Double.parseDouble(
                    resultByTime.total().get("UnblendedCost").amount()
                );
                
                trendMap.put(period, cost);
            }
        } catch (Exception e) {
            System.err.println("Error getting monthly cost trend: " + e.getMessage());
            e.printStackTrace();
        }
        
        return trendMap;
    }
}
