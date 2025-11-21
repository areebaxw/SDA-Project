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
    private ObservableList<BillingRecord> billingData;
    
    public BillingController() {
        this.billingDAO = new BillingDAO();
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
        costColumn.setCellValueFactory(new PropertyValueFactory<>("costAmount"));
        periodColumn.setCellValueFactory(cellData -> {
            BillingRecord record = cellData.getValue();
            String period = record.getStartDate() + " to " + record.getEndDate();
            return new javafx.beans.property.SimpleStringProperty(period);
        });
        
        billingTable.setItems(billingData);
    }
    
    private void setupDatePickers() {
        // Set default date range to current month
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now.withDayOfMonth(1));
        endDatePicker.setValue(now);
    }
    
    @FXML
    private void handleRefresh() {
        loadBillingRecords();
        showInfo("Billing records refreshed!");
    }
    
    private void loadBillingRecords() {
        if (currentUser == null) return;
        
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            List<BillingRecord> records = billingDAO.getBillingRecordsByDateRange(
                currentUser.getUserId(),
                Date.valueOf(startDate),
                Date.valueOf(endDate)
            );
            
            billingData.clear();
            billingData.addAll(records);
            
            // Calculate total cost
            double totalCost = billingDAO.getTotalCost(
                currentUser.getUserId(),
                Date.valueOf(startDate),
                Date.valueOf(endDate)
            );
            
            totalCostLabel.setText(String.format("$%.2f", totalCost));
            
            // Update pie chart
            updateCostChart(startDate, endDate);
            
            System.out.println("Loaded " + records.size() + " billing records");
        } catch (Exception e) {
            System.err.println("Error loading billing records: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading billing records");
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
                    entry.getKey() + " ($" + String.format("%.2f", entry.getValue()) + ")",
                    entry.getValue()
                ));
            }
            
            costPieChart.setData(pieChartData);
        } catch (Exception e) {
            System.err.println("Error updating cost chart: " + e.getMessage());
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
