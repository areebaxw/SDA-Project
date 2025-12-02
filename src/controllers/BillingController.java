package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import models.User;
import models.BillingRecord;
import dao.BillingDAO;
import aws.BillingService;
import aws.AWSClientFactory;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * BillingController - Controller for billing reports view
 */
public class BillingController {
    @FXML
    private TableView<BillingRecord> billingTable;
    
    @FXML
    private TableColumn<BillingRecord, String> serviceColumn;
    
    @FXML
    private TableColumn<BillingRecord, Double> costColumn;
    
    @FXML
    private TableColumn<BillingRecord, String> periodColumn;
    
    @FXML
    private Label totalCostLabel;
    
    @FXML
    private DatePicker startDatePicker;
    
    @FXML
    private DatePicker endDatePicker;
    
    @FXML
    private PieChart costPieChart;
    
    private User currentUser;
    private BillingDAO billingDAO;
    private BillingService billingService;
    private ObservableList<BillingRecord> billingData;
    
    public BillingController() {
        this.billingDAO = new BillingDAO();
        this.billingService = new BillingService();
        this.billingData = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        setupTableColumns();
        setupDatePickers();
        loadBillingRecords();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadBillingRecords();
    }
    
    private void setupTableColumns() {
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        
        // Format cost column to show 4 decimal places
        costColumn.setCellValueFactory(new PropertyValueFactory<>("costAmount"));
        costColumn.setCellFactory(column -> new TableCell<BillingRecord, Double>() {
            @Override
            protected void updateItem(Double cost, boolean empty) {
                super.updateItem(cost, empty);
                if (empty || cost == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.4f", cost));
                }
            }
        });
        
        periodColumn.setCellValueFactory(cellData -> {
            BillingRecord record = cellData.getValue();
            String period = record.getStartDate() + " to " + record.getEndDate();
            return new javafx.beans.property.SimpleStringProperty(period);
        });
        
        billingTable.setItems(billingData);
    }
    
    private void setupDatePickers() {
        // Set default date range to current month minus 2 days (AWS Cost Explorer has 24-48 hour delay)
        LocalDate now = LocalDate.now();
        LocalDate adjustedEnd = now.minusDays(2); // Account for AWS API delay
        
        startDatePicker.setValue(now.withDayOfMonth(1));
        endDatePicker.setValue(adjustedEnd);
        
        System.out.println("Default date range set to: " + now.withDayOfMonth(1) + " to " + adjustedEnd);
        System.out.println("Note: AWS Cost Explorer typically has 24-48 hour delay for cost data");
    }
    
    @FXML
    private void handleRefresh() {
        loadBillingRecords();
        showInfo("Billing records refreshed from AWS!");
    }
    
    private void loadBillingRecords() {
        if (currentUser == null) return;
        
        if (!AWSClientFactory.getInstance().isInitialized()) {
            showError("AWS credentials not configured. Please configure your AWS credentials first.");
            return;
        }
        
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            System.out.println("Loading billing records from AWS for date range: " + startDate + " to " + endDate);
            
            // Fetch actual cost data directly from AWS Cost Explorer
            List<BillingRecord> records = billingService.getCostAndUsage(startDate, endDate, currentUser.getUserId());
            
            System.out.println("Received " + records.size() + " records from AWS");
            
            billingData.clear();
            billingData.addAll(records);
            
            System.out.println("Table now has " + billingData.size() + " items");
            
            // Calculate total cost from AWS records
            double totalCost = 0.0;
            for (BillingRecord record : records) {
                totalCost += record.getCostAmount();
            }
            
            totalCostLabel.setText(String.format("$%.4f", totalCost));
            
            // Update pie chart with AWS data
            updateCostChartFromRecords(records);
            
            System.out.println("Loaded " + records.size() + " billing records from AWS with total cost: $" + String.format("%.4f", totalCost));
        } catch (Exception e) {
            System.err.println("Error loading billing records from AWS: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading billing records from AWS: " + e.getMessage() + "\nNote: AWS Cost Explorer may have a 24-48 hour delay.");
        }
    }
    
    private void updateCostChart(LocalDate startDate, LocalDate endDate) {
        try {
            Map<String, Double> costByService = billingDAO.getCostByService(
                currentUser.getUserId(),
                Date.valueOf(startDate),
                Date.valueOf(endDate)
            );
            
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            for (Map.Entry<String, Double> entry : costByService.entrySet()) {
                pieChartData.add(new PieChart.Data(
                    entry.getKey() + " ($" + String.format("%.4f", entry.getValue()) + ")",
                    entry.getValue()
                ));
            }
            
            costPieChart.setData(pieChartData);
        } catch (Exception e) {
            System.err.println("Error updating cost chart: " + e.getMessage());
        }
    }
    
    private void updateCostChartFromRecords(List<BillingRecord> records) {
        try {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            for (BillingRecord record : records) {
                pieChartData.add(new PieChart.Data(
                    record.getServiceName() + " ($" + String.format("%.4f", record.getCostAmount()) + ")",
                    record.getCostAmount()
                ));
            }
            
            costPieChart.setData(pieChartData);
        } catch (Exception e) {
            System.err.println("Error updating cost chart from records: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSyncFromAWS() {
        if (currentUser == null) {
            showError("User not set");
            return;
        }
        
        if (!AWSClientFactory.getInstance().isInitialized()) {
            showError("AWS credentials not configured. Please configure your AWS credentials first.");
            return;
        }
        
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue(); // Don't add extra day
            
            // Show loading indicator
            totalCostLabel.setText("Syncing...");
            
            System.out.println("Syncing billing data from " + startDate + " to " + endDate);
            
           
            int savedCount = billingService.syncFromAWS(startDate, endDate, currentUser.getUserId());
            
       
            loadBillingRecords();
            
            if (savedCount > 0) {
                showInfo("Successfully synced " + savedCount + " billing records from AWS!");
            } else {
                showInfo("No billing data available from AWS. Note: AWS Cost Explorer may have a 24-48 hour delay.");
            }
            
        } catch (Exception e) {
            System.err.println("Error syncing from AWS: " + e.getMessage());
            e.printStackTrace();
            showError("Error syncing billing data from AWS: " + e.getMessage());
            totalCostLabel.setText("$0.00");
        }
    }
    
    @FXML
    private void handleFilter() {
        loadBillingRecords();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
