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
            // AWS Cost Explorer end date is EXCLUSIVE, so add 1 day to include the end date
            LocalDate apiEndDate = endDate.plusDays(1);
            
            System.out.println("Fetching costs from " + startDate.format(formatter) + " to " + endDate.format(formatter));
            System.out.println("AWS API call: start=" + startDate.format(formatter) + ", end=" + apiEndDate.format(formatter) + " (exclusive)");
            
            DateInterval dateInterval = DateInterval.builder()
                    .start(startDate.format(formatter))
                    .end(apiEndDate.format(formatter))
                    .build();
            
            GroupDefinition groupDefinition = GroupDefinition.builder()
                    .type(GroupDefinitionType.DIMENSION)
                    .key("SERVICE")
                    .build();
            
            // Use DAILY granularity and aggregate ourselves to get accurate month-to-date totals
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(dateInterval)
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost", "BlendedCost")
                    .groupBy(groupDefinition)
                    .build();
            
            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);
            
            System.out.println("AWS returned " + response.resultsByTime().size() + " time periods");
            
            // Aggregate costs by service across all days
            Map<String, Double> serviceCostMap = new HashMap<>();
            LocalDate periodStart = startDate;
            LocalDate periodEnd = endDate;
            
            for (ResultByTime resultByTime : response.resultsByTime()) {
                System.out.println("Processing time period: " + resultByTime.timePeriod().start() + " to " + resultByTime.timePeriod().end());
                System.out.println("  Number of groups: " + resultByTime.groups().size());
                
                for (Group group : resultByTime.groups()) {
                    String serviceName = group.keys().get(0);
                    
                    // Debug: Print all available metrics
                    System.out.println("  Service: " + serviceName);
                    System.out.println("    Available metrics: " + group.metrics().keySet());
                    
                    // Try UnblendedCost first, then BlendedCost if not available
                    double cost = 0.0;
                    if (group.metrics().containsKey("UnblendedCost")) {
                        String costStr = group.metrics().get("UnblendedCost").amount();
                        cost = Double.parseDouble(costStr);
                        System.out.println("    UnblendedCost: " + costStr + " -> parsed: " + cost);
                    } else if (group.metrics().containsKey("BlendedCost")) {
                        String costStr = group.metrics().get("BlendedCost").amount();
                        cost = Double.parseDouble(costStr);
                        System.out.println("    BlendedCost: " + costStr + " -> parsed: " + cost);
                    } else {
                        System.out.println("    WARNING: No cost metric found!");
                    }
                    
                    serviceCostMap.put(serviceName, 
                        serviceCostMap.getOrDefault(serviceName, 0.0) + cost);
                }
            }
            
            System.out.println("Aggregated " + serviceCostMap.size() + " unique services");
            
            // Create billing records from aggregated costs
            for (Map.Entry<String, Double> entry : serviceCostMap.entrySet()) {
                BillingRecord record = new BillingRecord();
                record.setUserId(userId);
                record.setServiceName(entry.getKey());
                record.setCostAmount(entry.getValue());
                record.setStartDate(periodStart);
                record.setEndDate(periodEnd);
                record.setCurrency("USD");
                record.setRecordType("monthly");
                
                records.add(record);
                System.out.println("  " + entry.getKey() + ": $" + String.format("%.4f", entry.getValue()));
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
    
    /**
     * Get remaining AWS credits for current month
     */
    public double getRemainingCredits() {
        try {
            LocalDate endDate = LocalDate.now().plusDays(1);
            LocalDate startDate = LocalDate.now().withDayOfMonth(1);
            
            DateInterval dateInterval = DateInterval.builder()
                    .start(startDate.format(formatter))
                    .end(endDate.format(formatter))
                    .build();
            
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(dateInterval)
                    .granularity(Granularity.MONTHLY)
                    .metrics("UnblendedCost")
                    .filter(software.amazon.awssdk.services.costexplorer.model.Expression.builder()
                            .dimensions(software.amazon.awssdk.services.costexplorer.model.DimensionValues.builder()
                                    .key(Dimension.RECORD_TYPE)
                                    .values("Credit")
                                    .build())
                            .build())
                    .build();
            
            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);
            
            double totalCredits = 0.0;
            for (ResultByTime resultByTime : response.resultsByTime()) {
                if (resultByTime.total().containsKey("UnblendedCost")) {
                    double creditAmount = Double.parseDouble(
                        resultByTime.total().get("UnblendedCost").amount()
                    );
                    totalCredits += Math.abs(creditAmount); // Credits are negative values
                }
            }
            
            return totalCredits;
        } catch (Exception e) {
            System.err.println("Error getting remaining credits: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
    
    /**
     * Get actual costs (excluding credits) for current month
     */
    public double getMonthToDateCost() {
        try {
            LocalDate startDate = LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = LocalDate.now().plusDays(1);
            
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
                if (resultByTime.total().containsKey("UnblendedCost")) {
                    double cost = Double.parseDouble(
                        resultByTime.total().get("UnblendedCost").amount()
                    );
                    totalCost += cost;
                }
            }
            
            System.out.println("Month-to-date total cost: $" + String.format("%.4f", totalCost));
            return totalCost;
        } catch (Exception e) {
            System.err.println("Error getting month-to-date cost: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
}
