package controllers;

import aws.AWSClientFactory;
import aws.SQSMonitoringService;
import dao.AWSCredentialDAO;
import dao.SQSQueueDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.AWSCredential;
import models.SQSQueueResource;
import models.User;

import java.util.List;

/**
 * SQSController - Controller for SQS queue monitoring panel
 */
public class SQSController {
    @FXML private TableView<SQSQueueResource> sqsTable;
    @FXML private TableColumn<SQSQueueResource, String> queueNameColumn;
    @FXML private TableColumn<SQSQueueResource, String> queueArnColumn;
    @FXML private TableColumn<SQSQueueResource, Long> messageCountColumn;
    @FXML private TableColumn<SQSQueueResource, Long> delayedCountColumn;
    @FXML private TableColumn<SQSQueueResource, Boolean> idleColumn;

    @FXML private Button backButton;

    private User currentUser;
    private final SQSQueueDAO sqsQueueDAO = new SQSQueueDAO();
    private final AWSCredentialDAO awsCredentialDAO = new AWSCredentialDAO();
    private SQSMonitoringService sqsMonitoringService;
    private final ObservableList<SQSQueueResource> sqsData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTableColumns();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeAwsClientFromSavedCredentials();
        loadSQSQueues();
    }

    private void initializeAwsClientFromSavedCredentials() {
        try {
            AWSCredential cred = awsCredentialDAO.getActiveCredentials(currentUser.getUserId());
            if (cred != null) {
                AWSClientFactory.getInstance().initializeCredentials(
                        cred.getAccessKey(),
                        cred.getSecretKey(),
                        cred.getRegion()
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize AWS client from saved credentials: " + e.getMessage());
        }
    }

    private SQSMonitoringService requireService() {
        if (!AWSClientFactory.getInstance().isInitialized()) {
            showError("AWS credentials not configured. Please configure credentials first.");
            return null;
        }

        if (sqsMonitoringService == null) {
            try {
                sqsMonitoringService = new SQSMonitoringService();
            } catch (Exception e) {
                showError("Failed to initialize SQS service: " + e.getMessage());
                return null;
            }
        }
        return sqsMonitoringService;
    }

    private void setupTableColumns() {
        queueNameColumn.setCellValueFactory(new PropertyValueFactory<>("queueName"));
        queueArnColumn.setCellValueFactory(new PropertyValueFactory<>("queueArn"));
        messageCountColumn.setCellValueFactory(new PropertyValueFactory<>("messageCount"));
        delayedCountColumn.setCellValueFactory(new PropertyValueFactory<>("delayedMessageCount"));
        idleColumn.setCellValueFactory(new PropertyValueFactory<>("idle"));
        sqsTable.setItems(sqsData);
    }

    @FXML
    private void handleRefresh() {
        loadSQSQueues();
    }

    @FXML
    private void handleSyncFromAWS() {
        if (currentUser == null) {
            showError("User not set");
            return;
        }

        SQSMonitoringService service = requireService();
        if (service == null) return;

        int synced = service.syncFromAWS(currentUser.getUserId());
        loadSQSQueues();
        showInfo("Synced " + synced + " SQS queues from AWS.");
    }

    @FXML
    private void handlePurgeQueue() {
        SQSQueueResource selected = sqsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a queue.");
            return;
        }

        SQSMonitoringService service = requireService();
        if (service == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Purge");
        confirmation.setHeaderText("Purge SQS Queue");
        confirmation.setContentText("This removes all messages from '" + selected.getQueueName() + "'. Continue?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean ok = service.purgeQueue(selected.getQueueUrl());
            if (ok) {
                showInfo("Queue purge requested.");
                handleSyncFromAWS();
            } else {
                showError("Failed to purge queue.");
            }
        }
    }

    @FXML
    private void handleDeleteQueue() {
        SQSQueueResource selected = sqsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a queue.");
            return;
        }

        SQSMonitoringService service = requireService();
        if (service == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete SQS Queue");
        confirmation.setContentText("Delete queue '" + selected.getQueueName() + "'?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean ok = service.deleteQueue(selected.getQueueUrl());
            if (ok) {
                sqsQueueDAO.deleteByQueueUrl(selected.getQueueUrl());
                loadSQSQueues();
                showInfo("Queue deleted.");
            } else {
                showError("Failed to delete queue.");
            }
        }
    }

    private void loadSQSQueues() {
        List<SQSQueueResource> queues = sqsQueueDAO.getAll();
        sqsData.clear();
        sqsData.addAll(queues);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 820);
            DashboardController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Dashboard - " + currentUser.getUsername());
            stage.show();
        } catch (Exception e) {
            showError("Error returning to dashboard: " + e.getMessage());
        }
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
