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
            
            // Group by both SERVICE and RECORD_TYPE to filter actual usage costs
            GroupDefinition serviceGroupDefinition = GroupDefinition.builder()
                    .type(GroupDefinitionType.DIMENSION)
                    .key("SERVICE")
                    .build();
            
            GroupDefinition recordTypeGroupDefinition = GroupDefinition.builder()
                    .type(GroupDefinitionType.DIMENSION)
                    .key("RECORD_TYPE")
                    .build();
            
            // Use DAILY granularity and aggregate ourselves to get accurate month-to-date totals
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(dateInterval)
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost")
                    .groupBy(serviceGroupDefinition, recordTypeGroupDefinition)
                    .build();
            
            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);
            
            System.out.println("AWS returned " + response.resultsByTime().size() + " time periods");
            
            
            Map<String, Double> serviceCostMap = new HashMap<>();
            LocalDate periodStart = startDate;
            LocalDate periodEnd = endDate;
            
            for (ResultByTime resultByTime : response.resultsByTime()) {
                System.out.println("Processing time period: " + resultByTime.timePeriod().start() + " to " + resultByTime.timePeriod().end());
                System.out.println("  Number of groups: " + resultByTime.groups().size());
                
                for (Group group : resultByTime.groups()) {
                    // group.keys() contains [SERVICE_NAME, RECORD_TYPE]
                    String serviceName = group.keys().get(0);
                    String recordType = group.keys().size() > 1 ? group.keys().get(1) : "Unknown";
                    
                    // Debug: Print all available metrics
                    System.out.println("  Service: " + serviceName + ", RecordType: " + recordType);
                    System.out.println("    Available metrics: " + group.metrics().keySet());
                    
                    // Only count "Usage" costs (actual usage, excluding credits, refunds, etc.)
                    double cost = 0.0;
                    if ("Usage".equalsIgnoreCase(recordType) && group.metrics().containsKey("UnblendedCost")) {
                        String costStr = group.metrics().get("UnblendedCost").amount();
                        cost = Double.parseDouble(costStr);
                        System.out.println("    Actual Usage Cost: " + costStr + " -> parsed: " + cost);
                        
                        serviceCostMap.put(serviceName, 
                            serviceCostMap.getOrDefault(serviceName, 0.0) + cost);
                    } else {
                        System.out.println("    Skipping non-usage record type: " + recordType);
                    }
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
     * Get total credits used since the beginning of the AWS Free Tier (last 12 months)
     * AWS Free Tier is valid for 12 months from account creation
     * Groups by RECORD_TYPE to get only "Usage" costs (before credits)
     */
    public double getTotalCreditsUsedAllTime() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(1);
            LocalDate startDate = today.minusMonths(12).withDayOfMonth(1);
            
            System.out.println("getTotalCreditsUsedAllTime: Querying from " + startDate + " to " + endDate);
            
            DateInterval dateInterval = DateInterval.builder()
                    .start(startDate.format(formatter))
                    .end(endDate.format(formatter))
                    .build();
            
            // Group by RECORD_TYPE to separate Usage from Credits
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(dateInterval)
                    .granularity(Granularity.MONTHLY)
                    .metrics("UnblendedCost")
                    .groupBy(GroupDefinition.builder()
                            .type(GroupDefinitionType.DIMENSION)
                            .key("RECORD_TYPE")
                            .build())
                    .build();
            
            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);
            
            System.out.println("API returned " + response.resultsByTime().size() + " monthly periods");
            
            double totalUsageCost = 0.0;
            for (ResultByTime resultByTime : response.resultsByTime()) {
                String period = resultByTime.timePeriod().start();
                
                // Process each group (grouped by RECORD_TYPE)
                for (Group group : resultByTime.groups()) {
                    String recordType = group.keys().get(0);
                    double cost = 0.0;
                    
                    if (group.metrics().containsKey("UnblendedCost")) {
                        cost = Double.parseDouble(group.metrics().get("UnblendedCost").amount());
                    }
                    
                    System.out.println("Period: " + period + " | RecordType: " + recordType + " -> $" + String.format("%.4f", cost));
                    
                    // Only count "Usage" costs (not Credits, Refunds, etc.)
                    if ("Usage".equalsIgnoreCase(recordType) && cost > 0) {
                        totalUsageCost += cost;
                    }
                }
            }
            
            System.out.println("Total USAGE cost (all time): $" + String.format("%.2f", totalUsageCost));
            return totalUsageCost;
        } catch (Exception e) {
            System.err.println("Error getting total credits used: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
    
    /**
     * Get actual costs (excluding credits) for current month
     * Note: AWS Cost Explorer data has 24-48 hour delay
     */
    public double getMonthToDateCost() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.withDayOfMonth(1);
            LocalDate endDate = today.plusDays(1);
            
            // If it's the first day of the month, get last month's data
            if (today.getDayOfMonth() == 1) {
                startDate = today.minusMonths(1).withDayOfMonth(1);
                endDate = today;
                System.out.println("First day of month - fetching last month's data instead");
            }
            
            System.out.println("getMonthToDateCost: Querying from " + startDate + " to " + endDate);
            
            DateInterval dateInterval = DateInterval.builder()
                    .start(startDate.format(formatter))
                    .end(endDate.format(formatter))
                    .build();
            
            // Group by RECORD_TYPE to separate Usage from Credits
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(dateInterval)
                    .granularity(Granularity.MONTHLY)
                    .metrics("UnblendedCost")
                    .groupBy(GroupDefinition.builder()
                            .type(GroupDefinitionType.DIMENSION)
                            .key("RECORD_TYPE")
                            .build())
                    .build();
            
            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);
            
            System.out.println("API returned " + response.resultsByTime().size() + " time periods");
            
            double usageCost = 0.0;
            for (ResultByTime resultByTime : response.resultsByTime()) {
                System.out.println("Period: " + resultByTime.timePeriod().start() + " to " + resultByTime.timePeriod().end());
                
                // Process each group (grouped by RECORD_TYPE)
                for (Group group : resultByTime.groups()) {
                    String recordType = group.keys().get(0);
                    double cost = 0.0;
                    
                    if (group.metrics().containsKey("UnblendedCost")) {
                        cost = Double.parseDouble(group.metrics().get("UnblendedCost").amount());
                    }
                    
                    System.out.println("  RecordType: " + recordType + " -> $" + String.format("%.4f", cost));
                    
                    // Only count "Usage" costs (not Credits, Refunds, etc.)
                    if ("Usage".equalsIgnoreCase(recordType) && cost > 0) {
                        usageCost += cost;
                    }
                }
            }
            
            System.out.println("Month-to-date USAGE cost: $" + String.format("%.2f", usageCost));
            return usageCost;
        } catch (Exception e) {
            System.err.println("Error getting month-to-date cost: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
    
    /**
     * Sync billing data from AWS to database
     * Business logic method that orchestrates: fetch from AWS, save to DB, count results
     */
    public int syncFromAWS(LocalDate startDate, LocalDate endDate, int userId) {
        System.out.println("Syncing billing data from AWS...");
        
        dao.BillingDAO billingDAO = new dao.BillingDAO();
        List<BillingRecord> awsRecords = getCostAndUsage(startDate, endDate, userId);
        
        if (awsRecords.isEmpty()) {
            System.out.println("WARNING: No records returned from AWS Cost Explorer");
            return 0;
        }
        
        int savedCount = 0;
        for (BillingRecord record : awsRecords) {
            System.out.println("Syncing: " + record.getServiceName() + " - $" + String.format("%.6f", record.getCostAmount()));
            if (billingDAO.upsertBillingRecord(record)) {
                savedCount++;
            }
        }
        
        System.out.println("Synced " + savedCount + " billing records from AWS");
        return savedCount;
    }
}
