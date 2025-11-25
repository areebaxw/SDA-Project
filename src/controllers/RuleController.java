package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.User;
import models.Rule;
import dao.RuleDAO;

import java.util.List;

/**
 * RuleController - Controller for governance rules view
 */
public class RuleController {
    @FXML
    private TableView<Rule> ruleTable;
    
    @FXML
    private TableColumn<Rule, String> ruleNameColumn;
    
    @FXML
    private TableColumn<Rule, String> ruleTypeColumn;
    
    @FXML
    private TableColumn<Rule, String> resourceTypeColumn;
    
    @FXML
    private TableColumn<Rule, String> actionTypeColumn;
    
    @FXML
    private TableColumn<Rule, Boolean> activeColumn;
    
    @FXML
    private TextField ruleNameField;
    
    @FXML
    private ComboBox<String> ruleTypeCombo;
    
    @FXML
    private ComboBox<String> resourceTypeCombo;
    
    @FXML
    private ComboBox<String> actionTypeCombo;
    
    @FXML
    private TextField metricField;
    
    @FXML
    private ComboBox<String> operatorCombo;
    
    @FXML
    private TextField valueField;
    
    @FXML
    private TextField durationField;
    
    @FXML
    private ComboBox<String> durationUnitCombo;
    
    private User currentUser;
    private RuleDAO ruleDAO;
    private ObservableList<Rule> ruleData;
    
    public RuleController() {
        this.ruleDAO = new RuleDAO();
        this.ruleData = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        setupTableColumns();
        setupComboBoxes();
        loadRules();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void setupTableColumns() {
        ruleNameColumn.setCellValueFactory(new PropertyValueFactory<>("ruleName"));
        ruleTypeColumn.setCellValueFactory(new PropertyValueFactory<>("ruleType"));
        resourceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("resourceType"));
        actionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        
        ruleTable.setItems(ruleData);
    }
    
    private void setupComboBoxes() {
        ruleTypeCombo.setItems(FXCollections.observableArrayList(
            "resource_optimization", "cost_optimization", "performance", "security"
        ));
        
        resourceTypeCombo.setItems(FXCollections.observableArrayList(
            "EC2", "RDS", "ECS", "SageMaker"
        ));
        
        actionTypeCombo.setItems(FXCollections.observableArrayList(
            "ALERT", "STOP", "TERMINATE", "NOTIFY"
        ));
        
        operatorCombo.setItems(FXCollections.observableArrayList(
            "<", ">", "=", "<=", ">="
        ));
        
        durationUnitCombo.setItems(FXCollections.observableArrayList(
            "minutes", "hours", "days"
        ));
        durationUnitCombo.setValue("hours"); // Default to hours
    }
    
    @FXML
    private void handleRefresh() {
        loadRules();
        showInfo("Rules refreshed!");
    }
    
    private void loadRules() {
        try {
            List<Rule> rules = ruleDAO.getAllRules();
            ruleData.clear();
            ruleData.addAll(rules);
            
            System.out.println("Loaded " + rules.size() + " rules");
        } catch (Exception e) {
            System.err.println("Error loading rules: " + e.getMessage());
            showError("Error loading rules");
        }
    }
    
    @FXML
    private void handleCreate() {
        try {
            Rule rule = new Rule();
            rule.setRuleName(ruleNameField.getText());
            rule.setRuleType(ruleTypeCombo.getValue());
            rule.setResourceType(resourceTypeCombo.getValue());
            rule.setActionType(actionTypeCombo.getValue());
            rule.setConditionMetric(metricField.getText());
            rule.setConditionOperator(operatorCombo.getValue());
            rule.setConditionValue(Double.parseDouble(valueField.getText()));
            
            // Store duration and unit as-is
            int duration = Integer.parseInt(durationField.getText());
            String unit = durationUnitCombo.getValue();
            rule.setConditionDuration(duration);
            rule.setDurationUnit(unit != null ? unit : "hours");
            
            rule.setActive(true);
            rule.setCreatedBy(currentUser.getUserId());
            
            boolean success = ruleDAO.createRule(rule);
            if (success) {
                showInfo("Rule created successfully");
                clearForm();
                loadRules();
            } else {
                showError("Failed to create rule");
            }
        } catch (NumberFormatException e) {
            showError("Please enter valid numeric values");
        } catch (Exception e) {
            System.err.println("Error creating rule: " + e.getMessage());
            showError("Error creating rule: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleToggleStatus() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a rule");
            return;
        }
        
        boolean success = ruleDAO.toggleRuleStatus(selected.getRuleId());
        if (success) {
            showInfo("Rule status updated");
            loadRules();
        } else {
            showError("Failed to update rule status");
        }
    }
    
    @FXML
    private void handleDelete() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a rule");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Rule");
        confirmation.setContentText("Are you sure you want to delete this rule?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = ruleDAO.deleteRule(selected.getRuleId());
            if (success) {
                showInfo("Rule deleted successfully");
                loadRules();
            } else {
                showError("Failed to delete rule");
            }
        }
    }
    
    private void clearForm() {
        ruleNameField.clear();
        ruleTypeCombo.setValue(null);
        resourceTypeCombo.setValue(null);
        actionTypeCombo.setValue(null);
        metricField.clear();
        operatorCombo.setValue(null);
        valueField.clear();
        durationField.clear();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
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
